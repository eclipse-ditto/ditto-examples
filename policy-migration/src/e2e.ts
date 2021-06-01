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

import { Config, ConfigFactory, Migration, ReplaceSubject } from './config/config.ts';
import { log, parseFlags } from './deps.ts';
import { initLog } from './log.ts';
import { PolicyMigration } from './migration.ts';
import { DittoMessage, DittoResponse } from './model/base.ts';
import { Policy } from './model/policy.ts';
import { DittoWebSocket } from './websocket/websocket.ts';

const config: Config = ConfigFactory.loadFromFile();
const ws = new DittoWebSocket(config);
let pending: string[] = [];
const errors: string[] = [];
const exp = expectedSubjects();
const flags = parseFlags(Deno.args, {
  default: {
    namespace: 'test',
    maxInFlight: 10,
    policies: 10,
    prefix: 'e2e.migration'
  }
});
const namespace = flags['namespace'];
const maxInflight = flags['maxInFlight'];
const cnt = flags['policies'];
const prefix = flags['prefix'];
const start = new Date().getTime();

let created = 0;
let retrieved = 0;
let migrated = 0;
let deletedThings = 0;
let deletedPolicies = 0;

await initLog(config);

// handles received websocket messages
const onMessage = (msg: DittoMessage) => {
  const cid: string = msg.headers['correlation-id'];
  const response = (msg as DittoResponse);
  log.debug(`Received ${cid} - ${response.status}`);

  if (cid.startsWith('create')) {
    if (response.status !== 204) { // 204 because of search-persisted ack
      log.warning(`Creation of ${cid} not successful.`);
      errors.push(
        `Create command ${cid} was not successfull ${response.status}. ${
          JSON.stringify(response.value)
        }`
      );
    }
  } else if (cid.startsWith('retrieve')) {
    if (response.status !== 200) {
      log.warning(
        `Retrieval of ${cid} not successful. ${JSON.stringify(response.value)}`
      );
    } else {
      assertPolicyContainsExpectedSubjects(response.value as Policy);
    }
  } else if (cid.startsWith('delete')) {
    if (response.status !== 204) {
      log.warning(
        `Deletion of ${cid} not successful. ${JSON.stringify(response.value)}`
      );
    }
  }

  pending = pending.filter((e) => e !== cid);

  next();
};

ws.connect(onMessage)
  .then((_: unknown) => {
    log.debug('WebSocket connection established, starting e2e test...');
    next();
  })
  .catch((reason: unknown) => {
    log.debug('Failed to connect WebSocket:', reason);
    Deno.exit(1);
  });

/**
 * Triggers the next test step.
 */
function next() {
  if (pending.length >= maxInflight) {
    // rate limit
    return;
  }

  // all create commands sent, but some still pending
  if (created === cnt && retrieved === 0 && pending.length !== 0) {
    log.debug('waiting for creates to finish...');
    return;
  }

  // all create commands finished, start migration
  if (
    created === cnt && retrieved === 0 && pending.length === 0 && migrated === 0
  ) {
    log.info('starting migration...');

    new PolicyMigration(
      config,
      () => {
        log.info('finished migration');
        migrated = 1;
        next();
      },
      () => {
        throw new Error('migration failed');
      }
    ).start();

    return;
  }

  // migration finished, retrieves pending
  if (
    migrated === 1 && retrieved === cnt && deletedThings === 0 &&
    pending.length !== 0
  ) {
    log.debug('waiting for retrieves to finish...');
    return;
  }

  // delete all created policies
  if (
    migrated === 1 && retrieved === cnt && deletedThings === cnt &&
    deletedPolicies === cnt &&
    pending.length !== 0
  ) {
    log.debug('waiting for deletes to finish...');
    return;
  }

  if (created < cnt) {
    if (created === 0) {
      log.info(`Creating ${cnt} things/policies.`);
    }
    createThingAndPolicy();
  } else if (retrieved < cnt) {
    if (retrieved === 0) {
      log.info(`Verifying ${cnt} policies.`);
    }
    retrievePolicyAndAssertExpectedSubjects();
  } else if (deletedThings < cnt) {
    if (deletedThings === 0) {
      log.info(`Deleting ${cnt} things.`);
    }
    deleteThings();
  } else if (deletedPolicies < cnt) {
    if (deletedPolicies === 0) {
      log.info(`Deleting ${cnt} policies.`);
    }
    deletePolicies();
  } else {
    if (pending.length === 0) {
      if (errors.length === 0) {
        log.info('Test finished successfully.');
        Deno.exit(0);
      } else {
        log.error('Test finished with failures.');
        log.error(JSON.stringify(errors, null, 2));
        Deno.exit(1);
      }
    } else {
      return;
    }
  }

  next();
}

/**
 * Builds the initial policy sent with the create command. It contains the old subject that should be replaced.
 * @returns initial policy
 */
function getInitialPolicy(): unknown {
  const RW = ['READ', 'WRITE'];
  const policy = {
    'entries': {
      'DEFAULT': {
        'subjects': {},
        'resources': {
          'thing:/': {
            'grant': RW,
            'revoke': []
          },
          'policy:/': {
            'grant': RW,
            'revoke': []
          },
          'message:/': {
            'grant': RW,
            'revoke': []
          }
        }
      }
    }
  };

  var subjects = {
    '{{ request:subjectId }}': {
      'type': 'generated'
    }
  };

  config.migrations.forEach((element) => {
    Object.keys(element).forEach((label) => {
      switch (label) {
        case Migration.ReplaceSubject:
          subjects = {
            ...subjects,
            [(element[label] as ReplaceSubject).old]: {
              'type': 'old subject will be removed'
            }
          };
          break;
      }
    });
  });

  policy.entries.DEFAULT.subjects = subjects;

  return policy;
}

/**
 * Creates a new thing with an initial policy.
 */
function createThingAndPolicy() {
  const cid = 'create.' + start + '.' + created;
  const createThing: DittoMessage = {
    topic: `${namespace}/${id(created)}/things/twin/commands/create`,
    headers: {
      'correlation-id': cid,
      'requested-acks': '["search-persisted"]'
    },
    path: '/',
    value: {
      '_policy': getInitialPolicy()
    }
  };

  created++;
  pending.push(cid);
  const createCmd = JSON.stringify(createThing);
  log.debug(`Sending: ${createCmd}`);
  ws.send(createCmd);
}

/**
 * Retrieve next policy to verify the expected subjects.
 */
function retrievePolicyAndAssertExpectedSubjects() {
  const cid = 'retrieve.' + start + '.' + retrieved;
  const retrievePolicy: DittoMessage = {
    topic: `${namespace}/${id(retrieved)}/policies/commands/retrieve`,
    headers: {
      'correlation-id': cid
    },
    path: '/'
  };

  retrieved++;
  pending.push(cid);
  const retrieveCmd = JSON.stringify(retrievePolicy);
  log.debug(`Sending: ${retrieveCmd}`);
  ws.send(retrieveCmd);
}

/**
 * Deletes the next policy to cleanup after test.
 */
function deleteThings() {
  deleteEntity(
    `delete.thing.${start}.${deletedThings}`,
    `${namespace}/${id(deletedThings)}/things/twin/commands/delete`
  );

  deletedThings++;
}

/**
 * Deletes the next policy to cleanup after test.
 */
function deletePolicies() {
  deleteEntity(
    `delete.policy.${start}.${deletedPolicies}`,
    `${namespace}/${id(deletedPolicies)}/policies/commands/delete`
  );

  deletedPolicies++;
}

function deleteEntity(cid: string, topic: string) {
  pending.push(cid);
  log.debug('Sending:' + cid);
  ws.send(JSON.stringify({
    'topic': topic,
    headers: {
      'correlation-id': cid
    },
    path: '/'
  } as DittoMessage));
}

/**
 * @returns an array of subjects the DEFAULT policy entry must contain after the migration
 */
function expectedSubjects(): string[] {
  const expected: string[] = [];
  config.migrations.forEach((element) => {
    Object.keys(element).forEach((label) => {
      switch (label) {
        case Migration.ReplaceSubject:
          expected.push((element[label] as ReplaceSubject).new);
          break;
      }
    });
  });
  return expected;
}

/**
 * Asserts that the given policy contains all expected subjects.
 * @param policy
 */
function assertPolicyContainsExpectedSubjects(policy: Policy) {
  exp.forEach((subject) => {
    if (
      !Object.keys(policy.entries['DEFAULT'].subjects).includes(subject)
    ) {
      const err =
        `Policy ${policy.policyId} is missing the subject ${subject}.`;
      log.warning(err);
      errors.push(err);
    }
  });
}

function id(index: number) {
  return `${prefix}-${start}-${index}`;
}
