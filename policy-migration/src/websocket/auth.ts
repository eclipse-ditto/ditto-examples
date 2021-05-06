import { DefaultTokenGenerator, TokenGenerator } from "./token.ts";
import { Config } from "../config/config.ts";
export class WebSocketAuth {
  readonly cfg: Config;
  readonly tokenGenerator: TokenGenerator;

  constructor(
    cfg: Config,
    getToken: TokenGenerator = new DefaultTokenGenerator(),
  ) {
    this.cfg = cfg;
    this.tokenGenerator = getToken;
  }

  /**
  * decorate
  */
  public async decorateUrl(): Promise<string> {
    const url = new URL(this.cfg.thingsWsEndpoint);
    if (this.cfg.bearerToken) {
      return this.addAccessTokenParameter(url, this.cfg.bearerToken);
    } else if (this.cfg.oauth) {
      const token = await this.tokenGenerator.getToken(
        this.cfg.oauth.tokenUrl,
        this.cfg.oauth.client,
        this.cfg.oauth.secret,
        this.cfg.oauth.scope,
      );
      return this.addAccessTokenParameter(url, token);
    } else if (this.cfg.basicAuth) {
      // use basic auth
      url.username = this.cfg.basicAuth.username;
      url.password = this.cfg.basicAuth.password;
      return url.toString();
    }
    return this.cfg.thingsWsEndpoint;
  }

  private addAccessTokenParameter(url: URL, token: string): string {
    const query = new URLSearchParams(url.search);
    query.append("access_token", token);
    url.search = query.toString();
    return url.toString();
  }
}
