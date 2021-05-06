import * as log from "https://deno.land/std@0.95.0/log/mod.ts";
import { v4 } from "https://deno.land/std@0.95.0/uuid/mod.ts";
import { format } from "https://deno.land/std@0.95.0/datetime/mod.ts";
import { ThingsWebSocket } from "./websocket/websocket.ts";
import {
  Config,
  ConfigFactory,
  Migration,
  ReplaceSubject,
} from "./config/config.ts";
import {
  CreateSubscription,
  RequestFromSubscription,
  SubscriptionCompleted,
  SubscriptionCreated,
  SubscriptionFailed,
  SubscriptionNextPage,
} from "./model/search.ts";
import {
  DittoErrorResponse,
  DittoMessage,
  DittoResponse,
} from "./model/base.ts";
import { LogRecord } from "https://deno.land/std@0.95.0/log/logger.ts";

const config: Config = ConfigFactory.loadFromFile();

class PolicyMigration {
  private logger = log.getLogger(
    PolicyMigration.name,
  );

  private ws: ThingsWebSocket;

  private subscriptionId: string | undefined = undefined;
  private subscriptionCompleted = false;
  private subscriptionResults = 0;

  private pendingMigrations: string[] = [];
  private succeededMigrations: string[] = [];
  private failedMigrations: Map<string, DittoErrorResponse> = new Map();

  private migrationStarted = new Date();

  constructor() {
    this.ws = new ThingsWebSocket(config);
  }

  public start() {
    this.migrationStarted = new Date();
    this.logger.info("Starting migration of policies.");
    this.ws.connect(this.onMessage)
      .then((_) => this.createSubscription())
      .catch((reason) => {
        this.logger.warning(`Failed to connect WebSocket: ${reason}`);
        Deno.exit(1);
      });
  }

  private onMessage = (msg: DittoMessage) => {
    this.logger.debug(() => `Message received: ${JSON.stringify(msg)}`);

    const cid: string = msg.headers["correlation-id"];
    const topic: string = msg.topic;

    if (topic.startsWith("_/_/things/twin/search/")) {
      this.handleSearchEvents(msg);
    } else if (this.pendingMigrations.includes(cid)) {
      this.handleResponse(msg as DittoResponse);
    } else {
      this.logger.info(() =>
        `Unexpected message received: ${JSON.stringify(msg)}`
      );
    }
  };

  private handleSearchEvents(msg: DittoMessage) {
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

  private handleResponse(response: DittoResponse) {
    const cid = response.headers["correlation-id"];
    if (response.status === 201 || response.status === 204) {
      this.succeededMigrations.push(cid);
    } else {
      const errorResponse = response as DittoErrorResponse;
      this.failedMigrations.set(cid, errorResponse);
      this.logger.warning(() =>
        `${cid}: failed: ${JSON.stringify(errorResponse.value.error)}`
      );
    }

    this.pendingMigrations = this.pendingMigrations.filter((e) => e !== cid);

    if (this.pendingMigrations.length == 0) {
      if (this.subscriptionCompleted) {
        this.printSummaryAndExit();
      } else {
        this.requestFromSubscription();
      }
    }
  }

  private printSummaryAndExit() {
    this.logger.info(() =>
      `Migration finished in ${
        ((new Date().getTime() -
          this.migrationStarted.getTime()) / 1000).toFixed(2)
      }s`
    );
    this.logger.info(
      `${this.succeededMigrations.length} migrations successful. `,
    );
    if (this.failedMigrations.size > 0) {
      this.logger.error(
        `${this.succeededMigrations.length} migrations failed. `,
      );
      Deno.writeTextFileSync(
        "failed.json",
        JSON.stringify(this.failedMigrations, null, 2),
      );
    }
    this.ws.close();
    Deno.exit();
  }

  private handleSubscriptionCreated(created: SubscriptionCreated) {
    this.subscriptionId = created.value.subscriptionId;
    this.logger.debug(() => `Subscription ${this.subscriptionId} created.`);
    this.requestFromSubscription();
  }

  private handleSubscriptionNext(next: SubscriptionNextPage) {
    this.subscriptionResults += next.value.items.length;

    (next.value.items as {
      thingId: string;
      policyId: string;
    }[]).forEach((pair) => {
      this.migratePolicy(pair.policyId);
    });
  }

  private handleSubscriptionCompleted(completed: SubscriptionCompleted) {
    this.subscriptionCompleted = true;
    this.logger.debug(() =>
      `Subscription ${completed.value.subscriptionId} completed...`
    );

    if (this.subscriptionResults == 0) {
      this.logger.info("Search returned an empty result. No Policy migrated.");
      Deno.exit();
    }
  }

  private handleSubscriptionFailed(failed: SubscriptionFailed) {
    this.logger.error(() =>
      `Subscription ${failed.value.subscriptionId} failed: ${
        JSON.stringify(failed.value.error)
      }`
    );
    this.ws.close();
    Deno.exit(1);
  }

  private createSubscription() {
    const create: CreateSubscription = {
      topic: "_/_/things/twin/search/subscribe",
      headers: {
        "content-type": "application/json",
        "correlation-id": v4.generate(),
      },
      path: "/",
      value: {
        // add filter
        // namespace optional
        namespaces: config.namespaces,
        options: `size(${config.pageSize})`,
      },
      fields: "thingId,policyId",
    };

    this.ws.send(JSON.stringify(create));
  }

  private requestFromSubscription() {
    const request: RequestFromSubscription = {
      topic: "_/_/things/twin/search/request",
      headers: {
        "content-type": "application/json",
      },
      path: "/",
      value: {
        subscriptionId: this.subscriptionId!,
        demand: config.pageSize,
      },
    };

    this.ws.send(JSON.stringify(request));
  }

  private migratePolicy(policyId: string) {
    this.logger.info(() => `Migrating policy with ID ${policyId}.`);
    config.migrations.forEach((element) => {
      Object.keys(element).forEach((label) => {
        switch (label) {
          case Migration.ReplaceSubject:
            this.replaceSubject(policyId, element[label] as ReplaceSubject);
            break;
          default:
            this.logger.info(`Unknown migration ${label}. Ignoring.`);
            break;
        }
      });
    });
  }

  private replaceSubject(policyId: string, replace: ReplaceSubject) {
    const policyIdPath = policyId.replace(":", "/");
    const cid = v4.generate();
    const modifySubjectCommand = {
      topic: `${policyIdPath}/policies/commands/modify`,
      headers: {
        "content-type": "application/json",
        "correlation-id": cid,
      },
      path: `/entries/${replace.new.label}/subjects/${replace.new.subject}`,
      value: replace.new.value,
    };
    this.pendingMigrations.push(cid);
    this.ws.send(JSON.stringify(modifySubjectCommand));
  }
}

function formatLog(logRecord: LogRecord): string {
  return `${
    format(logRecord.datetime, "yyyy-MM-dd HH:mm:ss.SSS")
  }  [${logRecord.levelName}] - ${logRecord.loggerName} - ${logRecord.msg}`;
}

await log.setup({
  handlers: {
    console: new log.handlers.ConsoleHandler(
      config.logging.console.level,
      {
        formatter: formatLog,
      },
    ),
    file: new log.handlers.RotatingFileHandler(
      config.logging.file.level,
      {
        formatter: formatLog,
        filename: config.logging.file.filename,
        maxBytes: 10e6,
        maxBackupCount: 3,
      },
    ),
  },
  loggers: {
    default: { level: "DEBUG", handlers: ["console", "file"] },
    [ThingsWebSocket.name]: {
      level: "DEBUG",
      handlers: ["console", "file"],
    },
    [PolicyMigration.name]: {
      level: "DEBUG",
      handlers: ["console", "file"],
    },
  },
});

const migration = new PolicyMigration();
migration.start();
