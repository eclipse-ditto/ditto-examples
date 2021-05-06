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
