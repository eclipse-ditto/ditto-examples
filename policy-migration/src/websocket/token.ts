export interface TokenGenerator {
  getToken(
    tokenUrl: string,
    client: string,
    secret: string,
    scope: string,
  ): Promise<string>;
}

export class DefaultTokenGenerator implements TokenGenerator {
  async getToken(
    tokenUrl: string,
    client: string,
    secret: string,
    scope: string,
  ): Promise<string> {
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
      throw new Error(`Authentication failed; ${response.text()}`);
    }
    const jsonResponse = await response.json();
    if ("access_token" in jsonResponse) {
      return jsonResponse["access_token"];
    } else {
      throw new Error(`Response contained no access_token: ${jsonResponse}`);
    }
  }
}
