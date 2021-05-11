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

import { assertEquals } from "https://deno.land/std@0.94.0/testing/asserts.ts";
import { Config, ConfigFactory } from "./config.ts";

Deno.test("create config", () => {
  const cfg: Config = ConfigFactory.load({
    wsEndpoint: "ws://localhost",
  });
  assertEquals(cfg.wsEndpoint, "ws://localhost");
});
