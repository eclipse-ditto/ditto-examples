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

import * as log from "https://deno.land/std@0.96.0/log/mod.ts";

export interface TokenGenerator {
  getToken(
    tokenUrl: string,
    client: string,
    secret: string,
    scope: string,
  ): Promise<string>;
}

export class DefaultTokenGenerator implements TokenGenerator {
  private logger = log.getLogger(DefaultTokenGenerator.name);

  async getToken(
    tokenUrl: string,
    client: string,
    secret: string,
    scope: string,
  ): Promise<string> {
    this.logger.debug(`Retrieving token for client ${client}.`);
    try {
      const response = await fetch(tokenUrl, {
        headers: {
          "Content-Type": "application/x-www-form-urlencoded;charset=UTF-8",
        },
        method: "POST",
        body: new URLSearchParams({
          grant_type: "client_credentials",
          client_id: client,
          client_secret: secret,
          scope: scope,
        }).toString(),
      });
      if (!response.ok) {
        throw new Error(`Authentication failed: ${await response.text()}`);
      }
      const jsonResponse = await response.json();
      this.logger.debug(`Token response ${JSON.stringify(jsonResponse)}.`);
      if ("access_token" in jsonResponse) {
        return jsonResponse["access_token"];
      } else {
        throw new Error(`Response contained no access_token: ${jsonResponse}`);
      }
    } catch (reason) {
      throw new Error(reason);
    }
  }
}
