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
import { v4 } from "https://deno.land/std@0.96.0/uuid/mod.ts";
import { Config } from "./config/config.ts";
import { DittoMessage } from "./model/base.ts";
import {
  CreateSubscription,
  RequestFromSubscription,
  SubscriptionCompleted,
  SubscriptionCreated,
  SubscriptionFailed,
  SubscriptionNextPage,
} from "./model/search.ts";
import { Policy } from "./model/policy.ts";
import { DittoWebSocket } from "./websocket/websocket.ts";

/**
 * Wraps the search functionality required to stream search results 
 */
export class Search {
  private logger = log.getLogger(Search.name);
  private completed = false;
  private policyIds = new Set<string>();

  private ws: DittoWebSocket;
  private config: Config;
  private handler: SearchHandler;
  private subscriptionId: string | undefined;

  constructor(config: Config, ws: DittoWebSocket, handler: SearchHandler) {
    this.config = config;
    this.ws = ws;
    this.handler = handler;
  }

  /**
   * Creates a new search subscription.
   */
  public createSubscription() {
    const create: CreateSubscription = {
      topic: "_/_/things/twin/search/subscribe",
      headers: {
        "content-type": "application/json",
        "correlation-id": v4.generate(),
      },
      path: "/",
      value: {
        filter: this.config.filter,
        namespaces: this.config.namespaces,
        options: `size(${this.config.pageSize})`,
      },
      fields: "thingId,_policy",
    };

    this.ws.send(JSON.stringify(create));
  }

  /**
   * Request more results for the existing subscription.
   */
  public requestFromSubscription() {
    if (this.subscriptionId) {
      const request: RequestFromSubscription = {
        topic: "_/_/things/twin/search/request",
        headers: {
          "content-type": "application/json",
        },
        path: "/",
        value: {
          subscriptionId: this.subscriptionId,
          demand: this.config.pageSize,
        },
      };
      this.ws.send(JSON.stringify(request));
    } else {
      this.logger.warning("Subscription not yet created.");
    }
  }

  /**
   * Handles events related to search.
   * @param msg the message to handle
   */
  public handleSearchEvents(msg: DittoMessage) {
    switch (msg.topic) {
      case "_/_/things/twin/search/created":
        this.handleSubscriptionCreated(msg as SubscriptionCreated);
        break;
      case "_/_/things/twin/search/next":
        this.handleSubscriptionNext(msg as SubscriptionNextPage);
        break;
      case "_/_/things/twin/search/complete":
        this.handleSubscriptionCompleted(msg as SubscriptionCompleted);
        break;
      case "_/_/things/twin/search/failed":
        this.handleSubscriptionFailed(msg as SubscriptionFailed);
        break;
      default:
        this.logger.error(`Unexpected message: ${JSON.stringify(msg)}`);
        break;
    }
  }

  /**
   * @returns true if the search is completed and will not receive further results
   */
  public isComplete(): boolean {
    return this.completed;
  }

  /**
   * @returns the count of policies returned by the search
   */
  public getResultCount(): number {
    return this.policyIds.size;
  }

  private handleSubscriptionCreated(created: SubscriptionCreated) {
    this.subscriptionId = created.value.subscriptionId;
    this.logger.debug(() => `Subscription ${this.subscriptionId} created.`);
    this.requestFromSubscription();
  }

  private handleSubscriptionNext(next: SubscriptionNextPage) {
    (next.value.items as {
      thingId: string;
      _policy: Policy;
    }[]).forEach((pair) => {
      if (!this.policyIds.has(pair._policy.policyId)) {
        this.policyIds.add(pair._policy.policyId);
        this.handler.onNext(pair._policy);
      } else {
        this.logger.debug(
          `The policy ${pair._policy.policyId} was already referenced by another Thing.`,
        );
      }
    });
  }

  private handleSubscriptionCompleted(completed: SubscriptionCompleted) {
    this.completed = true;
    this.logger.debug(() =>
      `Subscription ${completed.value.subscriptionId} completed `
    );

    this.handler.onComplete();
  }

  private handleSubscriptionFailed(failed: SubscriptionFailed) {
    this.logger.error(() =>
      `Subscription ${failed.value.subscriptionId} failed: ${
        JSON.stringify(failed.value.error)
      }`
    );
    this.handler.onError();
  }
}

export interface SearchHandler {
  /**
   * Is called for every policy search result.
   * @param policy a policy returned by the search request
   */
  onNext(policy: Policy): void;

  /**
   * Is called in case of an search error.
   */
  onError(): void;

  /**
   * Is called when then dearch is complete and no more search results are available.
   */
  onComplete(): void;
}
