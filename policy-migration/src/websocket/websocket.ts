import * as log from "https://deno.land/std@0.95.0/log/mod.ts";
import { Config } from "../config/config.ts";
import { WebSocketAuth } from "./auth.ts";
import { DittoMessage } from "../model/base.ts";

export class ThingsWebSocket {
  private logger = log.getLogger(ThingsWebSocket.name);
  private ws: WebSocket | undefined;
  private auth: WebSocketAuth;

  readonly config: Config;

  constructor(config: Config) {
    this.config = config;
    this.auth = new WebSocketAuth(config);
  }

  public async connect(
    onMessage: (payload: DittoMessage) => void,
  ): Promise<void> {
    const wsUrl = await this.auth.decorateUrl();

    this.logger.debug(() =>
      `Connecting to ${this.config.thingsWsEndpoint} ...`
    );

    this.ws = new WebSocket(wsUrl);

    this.ws.onopen = () => {
      this.logger.info(
        `WebSocket connection to ${this.config.thingsWsEndpoint} established successfully!`,
      );

      this.connected();
      setInterval(this.sendHeartBeat, 15000);
    };

    this.ws.onmessage = (message) => {
      if (message.data.startsWith("{")) {
        const json: DittoMessage = JSON.parse(message.data);
        onMessage(json);
      } else {
        this.logger.debug(
          `Received non-JSON message: ${JSON.stringify(message.data)}`,
        );
      }
    };

    this.ws.onerror = (error) => {
      this.logger.warning(
        `WebSocket error: ${JSON.stringify(error)}`,
      );
    };

    this.ws.onclose = (closeEvent) => {
      const msg =
        `WebSocket closed. Code: ${closeEvent.code}. Reason: ${closeEvent.reason}`;

      this.logger.warning(msg);
      this.failed(msg);
    };

    return new Promise<void>((resolve, reject) => {
      this.connected = resolve;
      this.failed = reject;
    });
  }

  private connected() {}
  private failed(_reason?: unknown) {}

  /**
   * Send message via WebSocket.
   */
  public send(message: string) {
    if (this.ws) {
      this.logger.debug(`Sending message: ${message}`);
      this.ws.send(message);
    } else {
      throw new Error("WebSocket connection not ready.");
    }
  }
  /**
   * Close WebSocket.
   */
  public close() {
    if (this.ws) {
      this.logger.debug("Closing WebSocket connection.");
      this.ws.close();
    } else {
      throw new Error("WebSocket connection not ready.");
    }
  }

  private sendHeartBeat() {
    if (this.ws) {
      this.ws.send(new ArrayBuffer(0));
    }
  }
}
