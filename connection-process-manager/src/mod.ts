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

import * as log from "https://deno.land/std@0.89.0/log/mod.ts";
import { parse as yamlParse } from "https://deno.land/std@0.89.0/encoding/yaml.ts";

import { ConnectionRefresher } from "./connection_refresher.ts";
import { ProcessManager } from "./process_manager.ts";

// deno-lint-ignore no-explicit-any
let options: any;
try {
  options = yamlParse(await Deno.readTextFile("config.yml"));
} catch (e) {
  throw new Error(`${e}; config file 'config.yml'`);
}

if (!options.filter) options.filter = "*";
if (!options.connectionMonitorInterval) options.connectionMonitorInterval = 60;
if (!options.processMonitorInterval) options.processMonitorInterval = 5;

await log.setup({
  handlers: {
    console: new log.handlers.ConsoleHandler(options.logging.console, {
      formatter: "{datetime} {levelName} - {msg}",
    }),
    file: new log.handlers.RotatingFileHandler(options.logging.file.level, {
      formatter: "{datetime} {levelName} - {msg}",
      filename: options.logging.file.filename,
      maxBytes: 10e6,
      maxBackupCount: 3,
    }),
  },
  loggers: {
    default: { level: "DEBUG", handlers: ["console", "file"] },
    [ProcessManager.name]: {
      level: "DEBUG",
      handlers: ["console", "file"],
    },
    [ConnectionRefresher.name]: {
      level: "DEBUG",
      handlers: ["console", "file"],
    },
  },
});

const pm = new ProcessManager(options);
const cr = new ConnectionRefresher(options);

await cr.runAndMonitor(pm);
await pm.runAllAndMonitor();
