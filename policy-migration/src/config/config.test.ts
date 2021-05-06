import { assertEquals } from "https://deno.land/std@0.94.0/testing/asserts.ts";
import { Config, ConfigFactory } from "./config.ts";

// Simple name and function, compact form, but not configurable
Deno.test("create config", () => {
  const cfg: Config = ConfigFactory.load({
    thingsWsEndpoint: "ws://localhost",
  });
  assertEquals(cfg.thingsWsEndpoint, "ws://localhost");
});
