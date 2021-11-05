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

import { parse } from "https://deno.land/std@0.109.0/encoding/yaml.ts";

type LogLevel = (
  | "NOTSET"
  | "DEBUG"
  | "INFO"
  | "WARNING"
  | "ERROR"
  | "CRITICAL"
);

export enum Migration {
  ReplaceSubject = "replaceSubject",
  AddSubject = "addSubject",
  AddEntry = "addEntry"
}

export type Config = {
  readonly httpEndpoint: string;
  readonly bearerToken: string;
  readonly oAuth: {
    readonly tokenUrl: string;
    readonly client: string;
    readonly secret: string;
    readonly scope: string;
  };
  readonly basicAuth: {
    readonly username: string;
    readonly password: string;
  };
  readonly namespaces?: [string];
  readonly filter?: string;
  readonly pageSize: number;

  readonly dryRun: boolean;

  readonly logging: {
    console: {
      level: LogLevel;
    };
    file: {
      level: LogLevel;
      filename: string;
    };
  };

  readonly migrations: [{ [key: string]: unknown }];
};

const defaults = {
  pageSize: 50,
};

export class ConfigFactory {
  static loadFromFile(): Config {
    const cfg = parse(Deno.readTextFileSync("./config.yml"));
    return this.load(cfg);
  }

  static load(source: unknown): Config {
    const cfg = source as Config;
    const configWithDefaults = { ...defaults, ...cfg };
    return configWithDefaults;
  }
}
