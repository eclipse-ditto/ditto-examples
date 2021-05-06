import { assertEquals } from "https://deno.land/std@0.94.0/testing/asserts.ts";
import { Config, ConfigFactory } from "../config/config.ts";
import { WebSocketAuth } from "./auth.ts";
import { TokenGenerator } from "./token.ts";

class DummyTokenGenerator implements TokenGenerator {
  getToken(
    _tokenUrl: string,
    _client: string,
    _secret: string,
    _scope: string,
  ): Promise<string> {
    return Promise.resolve("token_from_client_credentials");
  }
}

Deno.test("decorate url (anonymous)", async () => {
  const cfg: Config = ConfigFactory.load({
    thingsWsEndpoint: "ws://localhost/ws/2",
  });
  const auth = new WebSocketAuth(cfg);
  const url = await auth.decorateUrl();
  assertEquals(url, "ws://localhost/ws/2");
});

Deno.test("decorate url (basic)", async () => {
  const cfg: Config = ConfigFactory.load({
    thingsWsEndpoint: "ws://localhost/ws/2",
    basicAuth: {
      username: "username",
      password: "password",
    },
  });
  const auth = new WebSocketAuth(cfg);
  const url = await auth.decorateUrl();
  assertEquals(url, "ws://username:password@localhost/ws/2");
});

Deno.test("decorate url (bearer)", async () => {
  const cfg: Config = ConfigFactory.load({
    thingsWsEndpoint: "ws://localhost/ws/2",
    bearerToken: "abc1234",
  });
  const auth = new WebSocketAuth(cfg);
  const url = await auth.decorateUrl();
  assertEquals(url, "ws://localhost/ws/2?access_token=abc1234");
});

Deno.test("decorate url with query parameter (bearer)", async () => {
  const cfg: Config = ConfigFactory.load({
    thingsWsEndpoint: "ws://localhost/ws/2?foo=bar",
    bearerToken: "abc1234",
  });
  const auth = new WebSocketAuth(cfg);
  const url = await auth.decorateUrl();
  assertEquals(url, "ws://localhost/ws/2?foo=bar&access_token=abc1234");
});

Deno.test("decorate url (client credentials)", async () => {
  const cfg: Config = ConfigFactory.load({
    thingsWsEndpoint: "ws://localhost/ws/2",
    oauth: {
      tokenUrl: "http://localhost/token",
      client: "client",
      secret: "secret",
      scope: "scope",
    },
  });
  const auth = new WebSocketAuth(cfg, new DummyTokenGenerator());
  const url = await auth.decorateUrl();
  assertEquals(
    url,
    "ws://localhost/ws/2?access_token=token_from_client_credentials",
  );
});
