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
import { format } from "https://deno.land/std@0.109.0/datetime/mod.ts";
import { LogRecord } from "https://deno.land/std@0.109.0/log/logger.ts";
import * as log from "https://deno.land/std@0.109.0/log/mod.ts";
import { Config } from "./config/config.ts";
import { PolicyMigration } from "./migration.ts";
import { Search } from "./search.ts";
import { DefaultTokenGenerator } from "./http/token.ts";

function formatLog(logRecord: LogRecord): string {
  return `${
    format(logRecord.datetime, "yyyy-MM-dd HH:mm:ss.SSS")
  }  [${logRecord.levelName}] - ${logRecord.loggerName} - ${logRecord.msg}`;
}

/**
 * Initialize logging.
 * @param config the config containing the log settings
 * @returns promise that resolves when logging is ready.
 */
export function initLog(config: Config): Promise<void> {
  return log.setup({
    handlers: {
      console: new log.handlers.ConsoleHandler(
        config.logging.console.level,
        {
          formatter: formatLog,
        },
      ),
      file: new log.handlers.RotatingFileHandler(
        config.logging.file.level,
        {
          formatter: formatLog,
          filename: config.logging.file.filename,
          maxBytes: 10e6,
          maxBackupCount: 3,
        },
      ),
    },
    loggers: {
      default: { level: "DEBUG", handlers: ["console", "file"] },
      [PolicyMigration.name]: {
        level: "DEBUG",
        handlers: ["console", "file"],
      },
      [DefaultTokenGenerator.name]: {
        level: "DEBUG",
        handlers: ["console", "file"],
      },
      [Search.name]: {
        level: "DEBUG",
        handlers: ["console", "file"],
      }
    },
  });
}
