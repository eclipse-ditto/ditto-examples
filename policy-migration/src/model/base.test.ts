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

import { assertEquals } from "https://deno.land/std@0.96.0/testing/asserts.ts";
import { DittoErrorResponse, DittoMessage, DittoResponse } from "./base.ts";

const msg = {
  topic: "theTopic",
  headers: { "correlation-id": "1234" },
  path: "/",
  value: {
    status: 400,
    error: "bad.format",
    message: "invalid request",
  },
  status: 400,
};

Deno.test("DittoMessage", () => {
  const dittoMsg: DittoMessage = msg;
  assertEquals(dittoMsg.topic, "theTopic");
  assertEquals(dittoMsg.headers, { "correlation-id": "1234" });
  assertEquals(dittoMsg.path, "/");
});

Deno.test("DittoResponse", () => {
  const dittoMsg: DittoResponse = msg;
  assertEquals(dittoMsg.topic, "theTopic");
  assertEquals(dittoMsg.headers, { "correlation-id": "1234" });
  assertEquals(dittoMsg.path, "/");
  assertEquals(dittoMsg.status, 400);
});

Deno.test("DittoErrorResponse", () => {
  const dittoMsg: DittoErrorResponse = msg;
  assertEquals(dittoMsg.topic, "theTopic");
  assertEquals(dittoMsg.headers, { "correlation-id": "1234" });
  assertEquals(dittoMsg.path, "/");
  assertEquals(dittoMsg.status, 400);
  assertEquals(dittoMsg.value, {
    status: 400,
    error: "bad.format",
    message: "invalid request",
  });
});
