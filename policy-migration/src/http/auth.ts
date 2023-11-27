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
import * as log from "https://deno.land/std@0.109.0/log/mod.ts";
import { DefaultTokenGenerator, TokenGenerator } from "./token.ts";
import { Config } from "../config/config.ts";
export class HttpAuth {
  private logger = log.getLogger(HttpAuth.name);
  private token: string | undefined;
  readonly cfg: Config;
  readonly tokenGenerator: TokenGenerator;

  constructor(
    cfg: Config,
    getToken: TokenGenerator = new DefaultTokenGenerator(),
  ) {
    this.cfg = cfg;
    this.tokenGenerator = getToken;
  }

  public async addAuthHeader(headers: Headers) {
    if (this.cfg.bearerToken) {
      headers.append("Authorization", "Bearer " + this.cfg.bearerToken);
    } else if (this.cfg.oAuth) {
      headers.append("Authorization", "Bearer " + await this.generateToken());
    } else if (this.cfg.basicAuth) {
      headers.append(
        "Authorization",
        "Basic " +
        btoa(
          this.cfg.basicAuth.username + ":" +
          this.cfg.basicAuth.password,
        ),
      );
    } else if (this.cfg.apiKey) {
      headers.append(
        this.cfg.apiKey.key,
        this.cfg.apiKey.value
      );
    }
  }

  private async generateToken() {
    if (!this.token) {
      this.token = await this.tokenGenerator.getToken(
        this.cfg.oAuth.tokenUrl,
        this.cfg.oAuth.client,
        this.cfg.oAuth.secret,
        this.cfg.oAuth.scope,
      );
    }
    return this.token;
  }
}
