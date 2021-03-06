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
// @deno-types="https://cdn.jsdelivr.net/npm/@types/mustache@4.1.0/index.d.ts"
import Mustache from "https://cdn.jsdelivr.net/npm/mustache@4.1.0/mustache.mjs";
// @deno-types="https://cdn.jsdelivr.net/npm/@types/jsonpath-plus@5.0.1/index.d.ts"
import { JSONPath } from "https://cdn.jsdelivr.net/npm/jsonpath-plus@5.0.3/dist/index-browser-esm.min.js";

import { ProcessManager } from "./process_manager.ts";

interface API {
  url: string;
  method: "GET" | "POST";
  body?: string;
  unwrapJsonPath?: string;
}

/**
 * Lookup infos about managed connections and execute&monitor commands for each of them.
 **/
export class ConnectionRefresher {
  private readonly logger = log.getLogger(ConnectionRefresher.name);

  constructor(
    public options: {
      listConnections: API;
      retrieveConnection: API;
      retrieveStatus: API;
      basicAuth?: {
        user: string;
        password: string;
      };
      oAuth?: {
        client: string;
        secret: string;
        scope: string;
        tokenUrl: string;
      };
      filter: string;
      connectionMonitorInterval: number;
      cmdPattern: string[];
    },
  ) {
    if (options.connectionMonitorInterval < 1) {
      throw new Error(
        "Invalid value for 'connectionMonitorInterval'; must be at least 1",
      );
    }
  }

  /**
   * Retrieves connection list and spawns processes for all connections.
   * Monitoring is restarted automatically in the background in the configured interval.
   **/
  async runAndMonitor(processManager: ProcessManager) {
    try {
      // prepare authentication header
      let auth;
      if (this.options.basicAuth) {
        auth = {
          "Authorization": "Basic " +
            btoa(
              this.options.basicAuth.user + ":" +
                this.options.basicAuth.password,
            ),
        };
      } else {
        const accessToken = await this.authenticateOauthClientCredentialsFlow();
        auth = { "Authorization": "Bearer " + accessToken };
      }

      // retrieve list of all managed connections and filter it
      const connections: Info[] = [];
      for (const id of await this.listConnections(auth)) {
        this.logger.debug(() => `Retrieve info for connection with id: ${id}`);
        connections.push(await this.retrieveInfo(auth, id));
      }
      this.logger.debug(() =>
        `Filtering connections: ${this.options.filter} on ${
          JSON.stringify(connections)
        }`
      );
      const filtered = JSONPath({
        path: this.options.filter,
        json: connections,
      }) as Info[];
      const connIds = filtered.map((c) => c.id);
      this.logger.debug(() => `Connections: ${JSON.stringify(connIds)}`);

      const removedIds = Array.from(processManager.keys()).filter((x) =>
        !connIds.includes(x)
      );

      // update processes for all current connections
      for (const id of connIds) {
        const info = await this.retrieveInfo(auth, id);

        // construct effective command line based on command pattern by replacing placeholders
        const cmd = this.options.cmdPattern.map((c) =>
          Mustache.render(c, info)
        );

        if (!cmd.every((c) => !c || c === "")) {
          // not fully empty cmd: create/update process
          processManager.set(id, cmd);
        } else {
          // empty cmd: delete/skip it
          processManager.delete(id);
        }
      }

      // remove processes for all old, removed connections
      for (const id of removedIds) {
        processManager.delete(id);
      }
    } finally {
      // wait a short moment and restart monitoring
      setTimeout(
        () => this.runAndMonitor(processManager),
        this.options.connectionMonitorInterval * 1000,
      );
    }
  }

  /** Retrieve list of all relevant connections. */
  private async listConnections(auth: Record<string, string>) {
    return await this.fetchExt(this.options.listConnections, {}, auth);
  }

  /** Retrieve connection info of a specific connection. */
  private async retrieveInfo(
    auth: { Authorization: string },
    id: string,
  ): Promise<Info> {
    const param = { id: id, idEncoded: encodeURIComponent(id) };
    this.logger.debug(() => `Retrieve info for connection with id: ${id}`);
    // lookup connection info
    const connectionInfo = await this.fetchExt(
      this.options.retrieveConnection,
      param,
      auth,
    );

    // lookup connection status also and add it as top-level "status" property
    connectionInfo.connectionStatusDetails = await this.fetchExt(
      this.options.retrieveStatus,
      param,
      auth,
    );

    // enrich further infos
    this.enrichInfo(connectionInfo);
    return connectionInfo;
  }

  /** Execute HTTP API by inlining parameters into URL or body and optionally unwrapping result via JsonPath. */
  private async fetchExt(
    api: API,
    params: Record<string, unknown>,
    headers: Record<string, string>,
  ): Promise<Info> {
    const response = await fetch(
      Mustache.render(api.url, params),
      {
        headers: headers,
        method: api.method,
        body: api.body ? Mustache.render(api.body, params) : undefined,
      },
    );
    if (!response.ok) {
      throw new Error(
        `API call to ${api.url} failed; ${await response.text()}`,
      );
    }
    let resultJson = await response.json();
    if (api.unwrapJsonPath) {
      resultJson = JSONPath({ path: api.unwrapJsonPath, json: resultJson });
      resultJson = (resultJson as Array<unknown>).shift();
    }
    return resultJson;
  }

  /** Execution authentication for OAuth2 Client Credential Flow. */
  private async authenticateOauthClientCredentialsFlow() {
    const response = await fetch(this.options.oAuth!.tokenUrl, {
      headers: {
        "Content-Type": "application/x-www-form-urlencoded;charset=UTF-8",
      },
      method: "POST",
      body: new URLSearchParams({
        grant_type: "client_credentials",
        client_id: this.options.oAuth!.client,
        client_secret: this.options.oAuth!.secret,
        scope: this.options.oAuth!.scope,
      }).toString(),
    });
    if (!response.ok) {
      throw new Error(`Authentication failed; ${await response.text()}`);
    }
    const resultJson = await response.json();
    return resultJson.access_token;
  }

  /** Enrich connection info with additional convenience information. */
  private enrichInfo(connectionInfo: Info) {
    // expand "uri" content as explicit content of a object "uriDetails"
    if (connectionInfo.uri) {
      const url = new URL(connectionInfo.uri);
      let port = url.port;
      if (!url.port || url.port === "") {
        if (url.protocol === "https:") port = "443";
        else if (url.protocol === "http:") port = "80";
      }
      connectionInfo.uriDetails = {
        port: port,
        host: url.host,
        hostname: url.hostname,
        protocol: url.protocol,
        username: url.username,
        password: url.password,
        pathname: url.pathname,
      };
    }
  }
}

// deno-lint-ignore no-explicit-any
export type Info = any;

// disable Mustache escaping
Mustache.escape = (s) => s;
