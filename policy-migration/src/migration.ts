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
import * as log from "https://deno.land/std@0.109.0/log/mod.ts";
import { v4 } from "https://deno.land/std@0.109.0/uuid/mod.ts";
import { Config, Migration } from "./config/config.ts";
import { HttpErrorResponse, MigrationResult, Progress } from "./model/base.ts";
import { Policy } from "./model/policy.ts";
import { Search } from "./search.ts";
import { HttpAuth } from "./http/auth.ts";
import { AddEntry, AddSubject, ReplaceSubject } from "./model/migration.ts";

/**
 * The policy migration is done in several steps:
 *  - find policies to migrate by executing a Things search in the context of the authenticated user
 *  - extract the policy from the search result (duplicate policies are skipped)
 *  - migrate each policy with the configured steps
 *  - update each modified policy
 */
export class PolicyMigration {
  private logger = log.getLogger(PolicyMigration.name);
  private config: Config;
  private search: Search;
  private migration = new MigrationStep();
  private httpAuth;
  private progress: Progress = new Progress();
  private finished: () => void;
  private failed: () => void;

  constructor(
    config: Config,
    finished: () => void = () => Deno.exit(0),
    failed: () => void = () => Deno.exit(1),
  ) {
    this.config = config;
    this.search = new Search(config);
    this.httpAuth = new HttpAuth(this.config);
    this.finished = finished;
    this.failed = failed;
  }

  /**
   * Starts the migration of policies.
   */
  public start() {
    this.progress.migrationStartedAt = new Date();
    this.logger.info(`Starting migration of policies.`);
    if (this.config.dryRun) {
      this.logger.warning(
        "Note: dry-run mode is enabled, no policy will be modified.",
      );
    }
    this.logger.debug(`Used configuration: ${JSON.stringify(this.config)}`);
    this.requestPolicies();
  }

  private requestPolicies() {
    if (this.search.isComplete() && !this.progress.hasPending()) {
      this.printSummaryAndExit();
    } else if (!this.search.isComplete() && !this.progress.hasPending()) {
      this.search.request()
        .then((policies) => this.onNext(policies))
        .catch((reason) => {
          this.logger.warning(`Failed to request policies: ${reason}`);
          this.failed();
        });
    }
  }

  private onNext(policies: Policy[]): void {
    policies
      .filter((p) => !this.progress.has(p.policyId)) // filter already processed policies
      .forEach((policy) => {
        this.logger.debug(() => `Processing policy: ${policy.policyId}`);
        this.progress.pending(policy.policyId);
        let doModify = false;
        // iterate over all configured migration steps and apply
        this.config.migrations.forEach((element) => {
          Object.keys(element).forEach((label) => {
            doModify = this.migration.applyMigration(policy, label, element[label]);
          });
        });

        if (doModify) {
          this.modifyPolicy(policy);
        } else {
          this.logger.debug(() => `Skipping policy: ${policy.policyId}`);
          this.progress.skipped(policy.policyId);
        }
      });
    this.requestPolicies();
  }

  

  private handleResponse(policyId: string, response: Response) {
    const successCodes = this.config.dryRun ? [412] : [204];
    const cid = this.getHeaderOrDefault(response.headers, "correlation-id", "");
    if (successCodes.includes(response.status)) {
      this.logger.info(() =>
        `${this.config.dryRun ? "[dry-run] " : ""}Migrated policy: ${policyId}`
      );
      this.progress.done(policyId);
    } else {
      this.progress.failed(policyId);
      response.json()
        .then((jr) => jr as HttpErrorResponse)
        .then((httpError) => {
          // store full error response for further analysis
          this.logger.warning(() =>
            `${cid} failed: [${httpError.error}] ${httpError.message}`
          );
          this.progress.failed(policyId, httpError);
        });
    }
    this.requestPolicies();
  }

  private modifyPolicy(migratedPolicy: Policy) {
    const cid = v4.generate();

    const headers = new Headers({
      "content-type": "application/json",
      "correlation-id": cid,
    });

    // set conditional header that will never be true for dry-run mode
    if (this.config.dryRun) {
      headers.append("If-Match", '"rev:0"');
    }

    this.logger.debug(() =>
      `Modifying Policy ${migratedPolicy.policyId}: ${
        JSON.stringify(migratedPolicy)
      }`
    );

    this.httpAuth.addAuthHeader(headers)
      .then((_) =>
        fetch(
          `${this.config.httpEndpoint}/policies/${migratedPolicy.policyId}`,
          {
            method: "PUT",
            body: JSON.stringify(migratedPolicy),
            headers: headers,
          },
        )
      ).then((response) =>
        this.handleResponse(migratedPolicy.policyId, response)
      );
  }

  private getHeaderOrDefault(
    headers: Headers,
    name: string,
    def: string,
  ): string {
    const result = headers.get(name);
    if (result) {
      return result;
    } else {
      return def;
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
      `${
        this.progress.get(MigrationResult.DONE).length
      } migrations successful. `,
    );

    const skipped = this.progress.get(MigrationResult.SKIPPED).length;
    if (skipped > 0) {
      this.logger.info(`${skipped} migrations skipped. `);
    }
    const failed = this.progress.get(MigrationResult.FAILED).length;
    if (failed > 0) {
      Deno.writeTextFileSync(
        "failed.json",
        JSON.stringify(this.progress.getErrors()),
      );

      this.logger.error(
        `${failed} migrations failed. See failed.json for raw error response log.`,
      );
    }
    if (this.config.dryRun) {
      this.logger.warning("This was a dry-run. No policy was modified.");
    }
    this.finished();
  }
}

/**
 * Implements the actual policy migration steps defined by @type {Migration} enum.
 */
export class MigrationStep {

  private logger = log.getLogger(PolicyMigration.name);

  public applyMigration(policy: Policy, label: string, step: unknown) {
    switch(label) {
      case Migration.ReplaceSubject:
        return this.replaceSubject(policy, step as ReplaceSubject);
      case Migration.AddSubject:
        return this.addSubject(policy, step as AddSubject);
      case Migration.AddEntry:
        return this.addEntry(policy, step as AddEntry);
      default:
        this.logger.info(`Unknown migration ${label}. Ignoring.`);
        return false;
    }
  }

  
  private replaceSubject(policy: Policy, replace: ReplaceSubject): boolean {
    let changed = false;
    // iterate over all policy entries (labels)
    for (const [label, _entry] of Object.entries(policy.entries)) {
      // check if the old subject is present
      if (policy.entries[label].subjects[replace.old]) {
        // delete old and add new subject
        delete policy.entries[label].subjects[replace.old];
        policy.entries[label].subjects[replace.new] = {
          type: `${replace.type}`,
        };
        changed = true;
      }
    }
    return changed;
  }

  private addSubject(policy: Policy, add: AddSubject): boolean {
    let changed = false;
    if (policy.entries[add.label]) {
      policy.entries[add.label].subjects[add.subject] = { type: add.type };
      changed = true;
    }
    return changed;
  }

  private addEntry(policy: Policy, add: AddEntry): boolean {
    let changed = false;
    if (add.replace || !policy.entries[add.label]) {
      policy.entries[add.label] = add.entry;
      changed = true;
    }
    return changed;
  }
}
