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

import { Config } from '../config/config.ts';
import { DefaultTokenGenerator, TokenGenerator } from './token.ts';

export class WebSocketAuth {

  constructor(readonly cfg: Config,
    readonly tokenGenerator: TokenGenerator = new DefaultTokenGenerator()) {
  }

  public async decorateUrl(): Promise<string> {
    const url = new URL(this.cfg.wsEndpoint);
    url.pathname = '/ws/2';
    if (this.cfg.bearerToken) {
      WebSocketAuth.addAccessTokenParameter(url, this.cfg.bearerToken);
    } else if (this.cfg.oAuth) {
      const token = await this.tokenGenerator.getToken(
        this.cfg.oAuth.tokenUrl,
        this.cfg.oAuth.client,
        this.cfg.oAuth.secret,
        this.cfg.oAuth.scope
      );
      WebSocketAuth.addAccessTokenParameter(url, token);
    } else if (this.cfg.basicAuth) {
      url.username = this.cfg.basicAuth.username;
      url.password = this.cfg.basicAuth.password;
    }
    return url.toString();
  }

  private static addAccessTokenParameter(url: URL, token: string): void {
    const query = new URLSearchParams(url.search);
    query.append('access_token', token);
    url.search = query.toString();
  }
}
