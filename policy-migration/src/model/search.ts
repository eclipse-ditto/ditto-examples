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

import { DittoMessage } from "./base.ts";

export type CreateSubscription = DittoMessage & {
  value: {
    filter?: string;
    namespaces?: [string];
    options: string;
  };
  fields: string;
};

export type SubscriptionCreated = DittoMessage & {
  value: {
    subscriptionId: string;
  };
};

export type SubscriptionCompleted = DittoMessage & {
  value: {
    subscriptionId: string;
  };
};

export type SubscriptionNextPage = DittoMessage & {
  value: {
    subscriptionId: string;
    items: [unknown];
  };
};

export type SubscriptionFailed = DittoMessage & {
  value: {
    subscriptionId: string;
    error: {
      status: number;
      error: string;
      message: string;
      description: string;
    };
  };
};

export type RequestFromSubscription = DittoMessage & {
  value: {
    subscriptionId: string;
    demand: number;
  };
};
