import { parse } from "https://deno.land/std@0.95.0/encoding/yaml.ts";

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
}

export type ReplaceSubject = {
  old: {
    subject: string;
    ignoreMissing: boolean;
  };
  new: {
    label: string;
    subject: string;
    value: { type: string };
  };
};

export type Config = {
  readonly thingsWsEndpoint: string;
  readonly bearerToken: string;
  readonly oauth: {
    readonly tokenUrl: string;
    readonly client: string;
    readonly secret: string;
    readonly scope: string;
  };
  readonly basicAuth: {
    readonly username: string;
    readonly password: string;
  };
  readonly namespaces: [string];
  readonly pageSize: number;

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
    const url = new URL(configWithDefaults.thingsWsEndpoint);

    if (!["ws:", "wss:"].includes(url.protocol)) {
      throw new Error("not a websocket url");
    }

    return configWithDefaults;
  }
}
