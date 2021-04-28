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

// @deno-types="https://cdn.jsdelivr.net/npm/@types/jsonpath-plus@5.0.1/index.d.ts"
import { JSONPath } from "https://cdn.jsdelivr.net/npm/jsonpath-plus@5.0.3/dist/index-browser-esm.min.js";
// @deno-types="https://cdn.jsdelivr.net/npm/@types/mustache@4.1.0/index.d.ts"
import Mustache from "https://cdn.jsdelivr.net/npm/mustache@4.1.0/mustache.mjs";
import * as log from "https://deno.land/std@0.89.0/log/mod.ts";

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
    this.logger.debug(() => `Run connection refresher to monitor connections.`);
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
      const connectionsWithUndefined: Info[] = [];
      const connectionIds = await this.listConnections(auth)
      this.logger.debug(() => `Retrieved following connection ids: ${connectionIds}`);
      for (const id of connectionIds) {
        this.logger.debug(() => `Retrieve info for connection with id: ${id}`);
        connectionsWithUndefined.push(await this.retrieveInfo(auth, id));
      }
      const connections = connectionsWithUndefined.filter(value => value != undefined);
      this.logger.debug(() => `Filtering all connections with filter: ${this.options.filter}`);
      const filtered = JSONPath({
        path: this.options.filter,
        json: connections,
        wrap: false,
      }) as Info[];

      let connIds: string[];
      if (filtered !== undefined) {
        connIds = filtered.map((c) => c.id);
        this.logger.debug(() => `Filtered connection ids: ${JSON.stringify(connIds)}`);
      } else {
        connIds = [];
      }

      const removedIds = Array.from(processManager.keys()).filter((x) => !connIds.includes(x));

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
    let connectionList;
    try {
      connectionList = await this.fetchExt(this.options.listConnections, {}, auth);
    } catch (e) {
      this.logger.error(() => `Retrieving connection list failed because of ${e}`);
    }
    return connectionList;
  }

  /** Retrieve connection info of a specific connection. */
  private async retrieveInfo(
    auth: { Authorization: string },
    id: string,
  ): Promise<Info> {
    const param = { id: id, idEncoded: encodeURIComponent(id) };
    let connectionInfo;
    // lookup connection info
    try {
      connectionInfo = await this.fetchExt(
        this.options.retrieveConnection,
          param,
          auth,
      );
    } catch (e) {
      this.logger.error(() => `Retrieving connection info failed for connection with id: ${id}`);
      return undefined;
    }

    // lookup connection status also and add it as top-level "status" property
    try {
      connectionInfo.connectionStatusDetails = await this.fetchExt(
        this.options.retrieveStatus,
        param,
        auth,
      );
    } catch (e) {
      this.logger.error(() => `Retrieving connection status details failed for connection with id: ${id}`);
      return undefined;
    }

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
    const responseStatus = JSONPath({
      path: '?.?.status',
      json: resultJson,
      wrap: false,
    });
    if (Number(responseStatus) >= 400) {
      const errorMessage = JSONPath({
        path: '?.?.payload.message',
        json: resultJson,
        wrap: false,
      });
      throw new Error(
          `API call to ${api.url} failed; Reason: ${errorMessage}`,
      );
    }

    if (api.unwrapJsonPath) {
      resultJson = JSONPath({
        path: api.unwrapJsonPath,
        json: resultJson,
        wrap: false,
      });
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
    if (connectionInfo.sshTunnel && connectionInfo.sshTunnel.uri) {
      const url = new URL(connectionInfo.sshTunnel.uri);
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
        username: connectionInfo.sshTunnel.credentials.username,
        password: connectionInfo.sshTunnel.credentials.password,
        pathname: url.pathname,
      };
    }
  }
}

// deno-lint-ignore no-explicit-any
export type Info = any;

// disable Mustache escaping
Mustache.escape = (s) => s;
