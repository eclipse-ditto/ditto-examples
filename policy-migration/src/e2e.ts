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
import { parse as parseFlags } from "https://deno.land/std@0.109.0/flags/mod.ts";
import { initLog } from "./log.ts";
import { Config, Migration, ReplaceSubject } from "./config/config.ts";
import { ConfigFactory } from "./config/config.ts";
import { PolicyMigration } from "./migration.ts";
import { Policy } from "./model/policy.ts";
import { HttpAuth } from "./http/auth.ts";

const config: Config = ConfigFactory.loadFromFile();
const httpAuth = new HttpAuth(config);
let pending = new Set<string>();
const errors: string[] = [];
const exp = expectedSubjects();
const flags = parseFlags(Deno.args, {
  default: {
    namespace: "test",
    maxInFlight: 10,
    policies: 10,
    prefix: "e2e.migration",
  },
});
const namespace = flags["namespace"];
const maxInflight = flags["maxInFlight"];
const cnt = flags["policies"];
const prefix = flags["prefix"];
const start = new Date().getTime();

let created = 0;
let retrieved = 0;
let migrated = 0;
let deletedThings = 0;
let deletedPolicies = 0;

await initLog(config);

log.info("Starting e2e test...");

next();

/**
 * Triggers the next test step.
 */
function next() {
  if (pending.size >= maxInflight) {
    // rate limit
    return;
  }

  // all create commands sent, but some still pending
  if (created === cnt && retrieved === 0 && pending.size !== 0) {
    log.debug("waiting for creates to finish...");
    return;
  }

  // all create commands finished, start migration
  if (
    created === cnt && retrieved === 0 && pending.size === 0 && migrated === 0
  ) {
    log.info("starting migration...");

    new PolicyMigration(
      config,
      () => {
        log.info("finished migration");
        migrated = 1;
        next();
      },
      () => {
        throw new Error("migration failed");
      },
    ).start();

    return;
  }

  // migration finished, retrieves pending
  if (
    migrated === 1 && retrieved === cnt && deletedThings === 0 &&
    pending.size !== 0
  ) {
    log.debug("waiting for retrieves to finish...");
    return;
  }

  // delete all created policies
  if (
    migrated === 1 && retrieved === cnt && deletedThings === cnt &&
    deletedPolicies === cnt &&
    pending.size !== 0
  ) {
    log.debug("waiting for deletes to finish...");
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
    if (pending.size === 0) {
      if (errors.length === 0) {
        log.info("Test finished successfully.");
        Deno.exit(0);
      } else {
        log.error("Test finished with failures.");
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
  const RW = ["READ", "WRITE"];
  const policy = {
    "entries": {
      "DEFAULT": {
        "subjects": {},
        "resources": {
          "thing:/": {
            "grant": RW,
            "revoke": [],
          },
          "policy:/": {
            "grant": RW,
            "revoke": [],
          },
          "message:/": {
            "grant": RW,
            "revoke": [],
          },
        },
      },
    },
  };

  var subjects = {
    "{{ request:subjectId }}": {
      "type": "generated",
    },
  };

  config.migrations.forEach((element) => {
    Object.keys(element).forEach((label) => {
      switch (label) {
        case Migration.ReplaceSubject:
          subjects = {
            ...subjects,
            [(element[label] as ReplaceSubject).old]: {
              "type": "old subject will be removed",
            },
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
  const thingName = id(created);
  const thingId = namespace + ":" + thingName;
  const cid = "create." + start + "." + created;
  log.debug(`Creating thing: ${thingId}`);
  doCreateThing(thingId, cid)
    .then((response) => {
      if (response.status !== 204) { // 204 because of search-persisted ack
        response.text()
          .then((txt) => {
            log.warning(
              `[${cid}] Creation of ${thingId} not successful [${response.status}]: ${txt}`,
            );
            errors.push(
              `Create command ${cid} was not successful ${response.status}: ${txt}`,
            );
          })
          .catch((reason) => {
            log.warning("Failed to parse: " + reason);
          });
      }
    });
}

/**
 * Retrieve next policy to verify the expected subjects.
 */
function retrievePolicyAndAssertExpectedSubjects() {
  const cid = "retrieve." + start + "." + retrieved;
  const policyId = `${namespace}:${id(retrieved)}`;
  log.debug(`Retrieve policy: ${policyId}`);
  doGetPolicy(policyId, cid).then((response) => {
    const jsonResponse = response.json();
    if (response.status !== 200) {
      jsonResponse.then((jr) => {
        log.warning(
          `Retrieval of ${cid} not successful. ${JSON.stringify(jr)}`,
        );
        errors.push(jr);
      });
    } else {
      jsonResponse.then((jr) =>
        assertPolicyContainsExpectedSubjects(jr as Policy)
      );
    }
  });
}

function doCreateThing(thingId: string, cid: string): Promise<Response> {
  created++;
  const headers = new Headers({
    "correlation-id": cid,
    "requested-acks": '["search-persisted"]',
  });
  return doFetch(
    cid,
    `/things/${thingId}`,
    "PUT",
    headers,
    JSON.stringify({
      "_policy": getInitialPolicy(),
    }),
  );
}
function doGetPolicy(policyId: string, cid: string): Promise<Response> {
  const headers = new Headers({
    "correlation-id": cid,
  });
  retrieved++;
  return doFetch(cid, "/policies/" + policyId, "GET", headers);
}
function doDeletePolicy(policyId: string, cid: string): Promise<Response> {
  log.debug(`Delete policy: ${policyId}`);
  const headers = new Headers({
    "correlation-id": cid,
  });
  deletedPolicies++;
  return doFetch(cid, "/policies/" + policyId, "DELETE", headers);
}
function doDeleteThing(thingId: string, cid: string): Promise<Response> {
  log.debug(`Delete thing: ${thingId}`);
  const headers = new Headers({
    "correlation-id": cid,
  });
  deletedThings++;
  return doFetch(cid, "/things/" + thingId, "DELETE", headers);
}

function doFetch(
  cid: string,
  path: string,
  method: string,
  headers: Headers,
  body?: string,
): Promise<Response> {
  const url = config.httpEndpoint + path;
  
  if (body) {
    headers.set("content-type", "application/json");
  }
  pending.add(cid);

  return httpAuth.addAuthHeader(headers)
    .then((_) =>
      fetch(url, {
        method: method,
        headers: headers,
        body: body,
      })
    ).then((r) => {
      pending.delete(cid);
      next();
      return r;
    });
}

/**
 * Deletes the next policy to cleanup after test.
 */
function deleteThings() {
  const cid = `${namespace}:${id(deletedThings)}`;
  doDeleteThing(
    cid,
    `delete.thing.${start}.${deletedThings}`,
  ).then((response) => {
    if (response.status !== 204) {
      response.json().then((jr) =>
        log.warning(
          `Deletion of ${cid} not successful. ${JSON.stringify(jr)}`,
        )
      );
    }
  });
}

/**
 * Deletes the next policy to cleanup after test.
 */
function deletePolicies() {
  const cid = `${namespace}:${id(deletedPolicies)}`;
  doDeletePolicy(
    `${namespace}:${id(deletedPolicies)}`,
    `delete.policy.${start}.${deletedPolicies}`,
  ).then((response) => {
    if (response.status !== 204) {
      response.json().then((jr) =>
        log.warning(
          `Deletion of ${cid} not successful. ${JSON.stringify(jr)}`,
        )
      );
    }
  });
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
      !Object.keys(policy.entries["DEFAULT"].subjects).includes(subject)
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
