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

import * as log from "https://deno.land/std@0.88.0/log/mod.ts";
import { equal } from "https://deno.land/std@0.88.0/testing/asserts.ts";

/**
 * Management of execution of sub-processes.
 * Makes sure that configured sub-processes are always running and are re-started when they are stopped or when the command line should be replaced.
 **/
export class ProcessManager {
  private readonly logger = log.getLogger(ProcessManager.name);
  private readonly processes: { [id: string]: ManagedProcess } = {};

  constructor(
    public options: {
      processMonitorInterval: number;
    },
  ) {
    if (options.processMonitorInterval < 1) {
      throw new Error(
        "Invalid value for 'processMonitorInterval'; must be at least 1",
      );
    }
  }

  /** Execute and monitor all managed processes. Monitoring is restarted automatically in the background in the configured interval. */
  async runAllAndMonitor() {
    try {
      for (const id in this.processes) {
        const process = this.processes[id];

        if (process.proc) {
          // process is already running: await result
          const s = await process.proc.status();
          const stdout = new TextDecoder().decode(await process.proc.output());
          const stderr = new TextDecoder().decode(await process.proc.stderrOutput());
          this.logger.info(
            `Command for ${id} terminated. PID ${process.proc.pid}: status code ${s.code}`,
          );
          if (stdout) this.logger.debug(() => `Command output: ${stdout}`);
          if (stderr) this.logger.debug(() => `Command error: ${stderr}`);

          process.proc = undefined;
        }

        // (re-)start process
        try {
          process.proc = Deno.run({
            cmd: process.cmd,
            stdout: "piped",
            stderr: "piped",
            stdin: "null",
          });
        } catch (e) {
          throw new Error(`${e}; command: ${process.cmd.join(" ")}`);
        }
        this.logger.info(
          `Command for ${id} started.    PID ${process.proc.pid}: ${process.cmd.join(" ")}`,
        );
      }

      // Flush all log handlers from time to time - otherwise logs may be missed too long
      // deno-lint-ignore no-explicit-any
      log.getLogger().handlers.map((h: any) => h.flush && h.flush());
    } finally {
      // wait a short moment and restart monitoring
      setTimeout(
        () => this.runAllAndMonitor(),
        this.options.processMonitorInterval * 1000,
      );
    }
  }

  /**
   * Set or update a process to be managed.
   * If process is currently running and the settings are changed, then it will be terminated and restarted.
   **/
  set(id: string, cmd: string[]): this {
    if (id in this.processes) {
      // already managed process
      const process = this.processes[id];

      if (!equal(process.cmd, cmd)) {
        // configuration changed
        this.kill(id, process);
        process.cmd = cmd;
      }
    } else {
      // new managed process
      this.processes[id] = new ManagedProcess(cmd);
    }
    return this;
  }

  /** Kill and remove process. */
  delete(id: string): boolean {
    if (id in this.processes) {
      this.kill(id, this.processes[id]);
      delete this.processes[id];
      return true;
    }
    return false;
  }

  /** Lists all ids of managed processes. */
  keys() {
    return Object.keys(this.processes).values();
  }

  private kill(id: string, process: ManagedProcess) {
    if (process.proc) {
      // process running, so request to terminate (SIGTERM)
      process.proc.kill(15);
      this.logger.info(`Send SIGTERM for ${id}. PID ${process.proc.pid}`);
    }
  }
}

class ManagedProcess {
  proc?: Deno.Process;

  constructor(public cmd: string[]) {}
}
