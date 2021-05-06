import { assertEquals } from "https://deno.land/std@0.95.0/testing/asserts.ts";
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
