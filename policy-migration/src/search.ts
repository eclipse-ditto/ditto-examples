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
import { Config } from "./config/config.ts";
import { Policy } from "./model/policy.ts";
import { HttpAuth } from "./http/auth.ts";

/**
 * Wraps the search functionality required to stream search results
 */
export class Search {
  private logger = log.getLogger(Search.name);
  private completed = false;
  private config: Config;
  private cursor: string | undefined;
  private httpAuth: HttpAuth;

  constructor(config: Config) {
    this.config = config;
    this.httpAuth = new HttpAuth(config);
  }

  /**
   * @returns true if the search is completed and will not receive further results
   */
  public isComplete(): boolean {
    return this.completed;
  }

  /**
   * Creates a new search subscription.
   */
  public async request(): Promise<Policy[]> {
    const url = new URL(`${this.config.httpEndpoint}/search/things`);
    const headers = new Headers();

    const options = [`size(${this.config.pageSize})`];
    if (this.cursor) {
      options.push(`cursor(${this.cursor})`);
    }

    url.searchParams.append("fields", "thingId,_policy");
    url.searchParams.append("option", options.join());
    if (this.config.namespaces) {
      url.searchParams.append("namespaces", this.config.namespaces.join());
    }
    if (this.config.filter) {
      url.searchParams.append("filter", this.config.filter);
    }

    return this.httpAuth.addAuthHeader(headers).then(_ => {
      this.logger.debug(`Executing search request: ${url}`);
      return fetch(url, {
        method: "GET",
        headers: headers,
      });
    }).then((response) => {
      if (response.status !== 200) {
        throw new Error(
          `Search request failed with status ${response.status}.`,
        );
      }
      return response.json();
    }).then((jr) => {
      const sr = jr as SearchResponse;

      if (sr.cursor) {
        this.cursor = sr.cursor;
      } else {
        this.logger.debug(`No cursor present in response, search completed.`);
        this.cursor = undefined;
        this.completed = true;
      }

      const policiesMap: Map<String, Policy> = new Map<String, Policy>();
      sr.items.forEach((item) => {
        policiesMap.set(item._policy.policyId, item._policy);
        this.logger.debug(`Thing found: ${item.thingId}`);
      });
      const policies: Policy[] = [];
      policiesMap.forEach((value, _) => {
        policies.push(value);
      });

      return policies;
    });
  }
}

type SearchResponse = {
  items: [{ thingId: string; _policy: Policy }];
  cursor: string;
};
