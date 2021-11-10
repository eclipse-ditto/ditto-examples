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

import { assertEquals } from "https://deno.land/std@0.113.0/testing/asserts.ts";
import { Migration } from "./config/config.ts";
import { MigrationStep } from "./migration.ts";
import { AddEntry, AddSubject, ReplaceSubject } from "./model/migration.ts";
import { Policy, PolicyEntry } from "./model/policy.ts";

const step = new MigrationStep();

const base: Policy = {
  policyId: "namespace:name",
  entries: {
    existing: {
      subjects: {
        theSubject: { type: "theType" },
      },
      resources: {
        "policy:/": {
          grant: ["READ", "WRITE"],
          revoke: [],
        },
      },
    },
  },
};

function copyPolicy(source: Policy): Policy {
  return JSON.parse(JSON.stringify(source));
}

Deno.test("addEntryAddsNewEntry", () => {
  const policy = copyPolicy(base);
  const newEntry = {
    subjects: { theSubject: { type: "type" } },
    resources: { "thing:/": { grant: ["READ", "WRITE"], revoke: [] } },
  } as PolicyEntry;

  const changed = step.applyMigration(policy, Migration.AddEntry, {
    label: "added",
    entry: newEntry,
    replace: true,
  } as AddEntry);

  assertEquals(changed, true);
  assertEquals(
    policy.entries,
    {
      added: newEntry,
      existing: base.entries.existing,
    },
  );
});

Deno.test("addEntryDoesNotReplaceExistingEntry", () => {
  const policy = copyPolicy(base);
  const entry2 = {
    subjects: { otherSubject: { type: "type" } },
    resources: { "thing:/": { grant: [], revoke: ["READ"] } },
  } as PolicyEntry;
  const changed = step.applyMigration(policy, Migration.AddEntry, {
    label: "existing",
    entry: entry2,
    replace: false,
  } as AddEntry);

  assertEquals(changed, false);
  assertEquals(policy, base);
});
Deno.test("addEntryReplacesExistingEntry", () => {
  const policy = copyPolicy(base);
  const entry2 = {
    subjects: { otherSubject: { type: "type" } },
    resources: { "thing:/": { grant: [], revoke: ["READ"] } },
  } as PolicyEntry;
  const changed = step.applyMigration(policy, Migration.AddEntry, {
    label: "existing",
    entry: entry2,
    replace: true,
  } as AddEntry);

  assertEquals(changed, true);
  assertEquals(
    policy.entries,
    { existing: entry2 },
  );
});

Deno.test("replaceSubject", () => {
  const policy = copyPolicy(base);
  const changed = step.applyMigration(policy, Migration.ReplaceSubject, {
    old: "theSubject",
    new: "newSubject",
    type: "migrated",
  } as ReplaceSubject);

  assertEquals(changed, true);
  assertEquals(
    policy.entries.existing.subjects,
    { newSubject: { type: "migrated" } },
  );
});

Deno.test("replaceSubjectDoesNotReplaceIfOldSubjectIsNotFound", () => {
  const policy = copyPolicy(base);
  const changed = step.applyMigration(policy, Migration.ReplaceSubject, {
    old: "notFound",
    new: "newSubject",
    type: "migrated",
  } as ReplaceSubject);

  assertEquals(changed, false);
  assertEquals(policy, base);
});

Deno.test("addSubjectAddsNewSubject", () => {
  const policy = copyPolicy(base);
  const changed = step.applyMigration(policy, Migration.AddSubject, {
    label: "existing",
    subject: "newSubject",
    type: "added",
  } as AddSubject);

  assertEquals(changed, true);
  assertEquals(policy.entries.existing.subjects, {
    theSubject: base.entries.existing.subjects.theSubject,
    newSubject: { type: "added" },
  });
});

Deno.test("addSubjectDoesNotAddNewSubjectIfLabelDoesNotExist", () => {
  const policy = copyPolicy(base);
  const changed = step.applyMigration(policy, Migration.AddSubject, {
    label: "unknown",
    subject: "newSubject",
    type: "added",
  } as AddSubject);

  assertEquals(changed, false);
  assertEquals(policy, base);
});
