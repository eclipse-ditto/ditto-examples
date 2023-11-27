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

import { Policy, PolicyEntry } from "./policy.ts";

export type ModifyPolicy = {
  value: Policy;
};

export type ReplaceSubject = {
  old: string;
  new: string;
  type: string;
};

export type AddSubject = {
  label: string;
  subject: string;
  type: string
};

export type AddEntry = {
  label: string;
  entry: PolicyEntry;
  replace: boolean;
};

export type ReplaceEntries = {
  policyEntries: { [label: string]: PolicyEntry };
}