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
import { log } from '../deps.ts';
import { DittoMessage } from '../model/base.ts';
import { WebSocketAuth } from './auth.ts';

/**
 * Wraps a WebSocket connection.
 */
export class DittoWebSocket {
  private readonly logger = log.getLogger('DittoWebSocket');
  private readonly auth: WebSocketAuth;

  private ws: WebSocket | undefined;

  constructor(readonly config: Config) {
    this.auth = new WebSocketAuth(config);
  }

  /**
   * Establish WebSocket connection.
   *
   * @param onMessage message callback
   * @returns promise that resolves/fails when the WebSocket connection is established/failed.
   */
  public async connect(
    onMessage: (payload: DittoMessage) => void
  ): Promise<void> {
    const wsUrl = await this.auth.decorateUrl();

    let connected: (value?: (PromiseLike<void> | void)) => void;
    let failed: (_reason?: unknown) => void;

    this.logger.debug(() => `Connecting to ${this.config.wsEndpoint} ...`);

    this.ws = new WebSocket(wsUrl);

    this.ws.onopen = () => {
      this.logger.info(
        `WebSocket connection to ${this.config.wsEndpoint} established successfully!`
      );

      connected();
      setInterval(this.sendHeartBeat, 15000);
    };

    this.ws.onmessage = (message) => {
      if (message.data.startsWith('{')) {
        const json: DittoMessage = JSON.parse(message.data);
        onMessage(json);
      } else {
        this.logger.debug(
          `Received non-JSON message: ${JSON.stringify(message.data)}`
        );
      }
    };

    this.ws.onerror = (error) => {
      this.logger.warning(
        `WebSocket error: ${JSON.stringify(error)}`
      );
    };

    this.ws.onclose = (closeEvent) => {
      const msg =
        `WebSocket closed. Code: ${closeEvent.code}. Reason: ${closeEvent.reason}`;

      this.logger.warning(msg);
      failed(msg);
    };

    return new Promise<void>((resolve, reject) => {
      connected = resolve;
      failed = reject;
    });
  }

  /**
   * Send message via WebSocket.
   */
  public send(message: string) {
    if (this.ws) {
      this.logger.debug(`Sending message: ${message}`);
      this.ws.send(message);
    } else {
      throw new Error('WebSocket connection not ready.');
    }
  }

  /**
   * Close WebSocket.
   */
  public close() {
    if (this.ws) {
      this.logger.debug('Closing WebSocket connection.');
      this.ws.close();
    } else {
      throw new Error('WebSocket connection not ready.');
    }
  }

  private sendHeartBeat() {
    this.ws?.send(new ArrayBuffer(0));
  }
}
