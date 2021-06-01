/*
 * Copyright (c) 2021 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */

import { Config, Migration, ReplaceSubject } from './config/config.ts';
import { log, uuidV4 } from './deps.ts';
import { DittoErrorResponse, DittoMessage, DittoResponse, Progress } from './model/base.ts';
import { ModifyPolicy } from './model/migration.ts';
import { Policy } from './model/policy.ts';
import { Search, SearchHandler } from './search.ts';
import { DittoWebSocket } from './websocket/websocket.ts';

/**
 * The policy migration is done in several steps:
 *  - find policies to migrate by executing a Things search in the context of the authenticated user
 *  - extract the policy from the search result (duplicate policies are skipped)
 *  - migrate each policy with the configured steps
 *  - update each modified policy
 */
export class PolicyMigration implements SearchHandler {
  private readonly logger = log.getLogger('PolicyMigration');
  private readonly ws: DittoWebSocket;
  private readonly search: Search;
  private readonly progress: Progress = new Progress();

  constructor(
    readonly config: Config,
    readonly finished: () => void = () => Deno.exit(0),
    readonly failed: () => void = () => Deno.exit(1)
  ) {
    this.ws = new DittoWebSocket(config);
    this.search = new Search(config, this.ws, this);
  }

  /**
   * Starts the migration of policies.
   */
  public start() {
    this.progress.migrationStartedAt = new Date();
    this.logger.info(`Starting migration of policies.`);
    if (this.config.dryRun) {
      this.logger.warning(
        'Note: dry-run mode is enabled, no policy will be modified.'
      );
    }
    this.logger.debug(`Used configuration: ${JSON.stringify(this.config)}`);
    this.connect();
  }

  private connect(attempt = 0) {
    this.ws.connect(this.onMessage)
      .then((_) => this.search.createSubscription())
      .catch((reason) => {
        this.logger.warning(
          `Failed to connect WebSocket (attempt ${attempt}): ${reason}`
        );
        if (attempt < 5) {
          const delay = attempt * 1000;
          this.logger.debug(`Retry after ${delay}ms.`);
          setTimeout(() => this.connect(attempt + 1), delay);
        } else {
          this.logger.error('Max number of retries. Connection failed.');
          this.failed();
        }
      });
  }

  private onMessage = (msg: DittoMessage) => {
    this.logger.debug(() => `Message received: ${JSON.stringify(msg)}`);

    const cid: string = msg.headers['correlation-id'];
    const topic: string = msg.topic;

    if (topic.startsWith('_/_/things/twin/search/')) {
      this.search.handleSearchEvents(msg);
    } else if (this.progress.pending.includes(cid)) {
      this.handleResponse(msg as DittoResponse);
    } else {
      this.logger.info(() =>
        `Unexpected message received: ${JSON.stringify(msg)}`
      );
    }
  };

  private handleResponse(response: DittoResponse) {
    const cid = response.headers['correlation-id'];
    const successCodes = this.config.dryRun ? [412] : [204];
    if (successCodes.includes(response.status)) {
      this.logger.info(() =>
        `${this.config.dryRun ? '[dry-run] ' : ''}Migrated policy: ${
          this.extractIdFromTopic(response.topic)
        }`
      );
      this.progress.succeeded.push(cid);
    } else {
      const errorResponse = response as DittoErrorResponse;
      // store full error response for further analysis
      this.progress.failed.set(cid, errorResponse);
      this.logger.warning(() =>
        `${cid} failed: [${errorResponse.value.error}] ${errorResponse.value.message}`
      );
    }

    // remove the correlation-id from the outstanding responses
    this.progress.pending = this.progress.pending.filter((
      e
    ) => e !== cid);

    // check if we are finished or request more results
    if (this.progress.pending.length == 0) {
      if (this.search.isComplete()) {
        this.printSummaryAndExit();
      } else {
        this.search.requestFromSubscription();
      }
    }
  }

  private printSummaryAndExit() {
    this.logger.info(() =>
      `Migration finished in ${
        ((new Date().getTime() -
          this.progress.migrationStartedAt.getTime()) / 1000).toFixed(2)
      }s`
    );
    this.logger.info(
      `${this.progress.succeeded.length} migrations successful. `
    );
    if (this.progress.skipped.length > 0) {
      this.logger.info(
        `${this.progress.skipped.length} migrations skipped. `
      );
    }
    if (this.progress.failed.size > 0) {
      Deno.writeTextFileSync(
        'failed.json',
        JSON.stringify(Array.from(this.progress.failed.values()))
      );

      this.logger.error(
        `${this.progress.failed.size} migrations failed. See failed.json for raw error response log.`
      );
    }
    if (this.config.dryRun) {
      this.logger.warning('This was a dry-run. No policy was modified.');
    }
    this.ws.close();
    this.finished();
  }

  private replaceSubject(policy: Policy, replace: ReplaceSubject): boolean {
    let changed = false;
    // iterate over all policy entries (labels)
    for (const [label, entry] of Object.entries(policy.entries)) {
      // check if the old subject is present
      if (policy.entries[label].subjects[replace.old]) {
        // delete old and add new subject
        delete policy.entries[label].subjects[replace.old];
        policy.entries[label].subjects[replace.new] = {
          type: `${replace.type}`
        };
        changed = true;
      }
    }
    return changed;
  }

  private modifyPolicy(migratedPolicy: Policy) {
    const policyIdPath = migratedPolicy.policyId.replace(':', '/');
    const cid = uuidV4.generate();
    const modifyPolicyCommand: ModifyPolicy = {
      topic: `${policyIdPath}/policies/commands/modify`,
      headers: {
        'content-type': 'application/json',
        'correlation-id': cid
      },
      path: `/`,
      value: migratedPolicy
    };

    // set conditional header that will never be true for dry-run mode
    if (this.config.dryRun) {
      modifyPolicyCommand.headers['If-Match'] = '"rev:0"';
    }

    this.logger.debug(() =>
      `Modifying Policy ${migratedPolicy.policyId}: ${
        JSON.stringify(migratedPolicy)
      }`
    );
    // send modify policy command and remember pending request
    this.progress.pending.push(cid);
    this.ws.send(JSON.stringify(modifyPolicyCommand));
  }

  onNext(policy: Policy): void {
    this.logger.debug(() => `Processing policy: ${policy.policyId}`);
    let doModify = false;
    // iterate over all configured migration steps and apply
    this.config.migrations.forEach((element) => {
      Object.keys(element).forEach((label) => {
        switch (label) {
          case Migration.ReplaceSubject:
            doModify = doModify ||
              this.replaceSubject(policy, element[label] as ReplaceSubject);
            break;
          default:
            this.logger.info(`Unknown migration ${label}. Ignoring.`);
            break;
        }
      });
    });

    if (doModify) {
      this.modifyPolicy(policy);
    } else {
      this.logger.debug(() => `Skipping policy: ${policy.policyId}`);
      this.progress.skipped.push(policy.policyId);
    }
  }

  onError(): void {
    this.ws.close();
    this.failed();
  }

  onComplete(): void {
    // no policies returned, nothing to migrate
    if (this.search.getResultCount() === 0) {
      this.logger.info('Search returned an empty result. No Policy migrated.');
      this.finished();
    }

    // no outstanding responses + search is complete, we are done
    if (
      this.progress.pending.length === 0 &&
      this.search.isComplete()
    ) {
      this.printSummaryAndExit();
    }
  }

  private extractIdFromTopic(path: string): string {
    const segments = path.split('/');
    return `${segments[0]}:${segments[1]}`;
  }
}
