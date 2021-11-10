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

export class Progress {
  progress: Map<string, MigrationResult> = new Map();
  errors: any[] = [];
  /**
   * get
   */
  public get(type: MigrationResult): string[] {
    const result: string[] = [];
    for (const [key, value] of this.progress) {
      if (value === type) {
        result.push(key);
      }
    }
    return result;
  }

  public pending(policyId: string) {
    this.progress.set(policyId, MigrationResult.PENDING);
  }

  public skipped(policyId: string) {
    this.progress.set(policyId, MigrationResult.SKIPPED);
  }

  public failed(policyId: string, error?: any) {
    this.progress.set(policyId, MigrationResult.FAILED);
    if (error) {
      this.errors.push(error);
    }
  }

  public done(policyId: string) {
    this.progress.set(policyId, MigrationResult.DONE);
  }

  public has(policyId: string):boolean {
    return this.progress.has(policyId);
  }

  public count():number {
    return this.progress.size;
  }

  public hasPending() {
    return this.get(MigrationResult.PENDING).length > 0;
  }

  public getErrors():string[] {
    return this.errors;
  }

  migrationStartedAt = new Date();

  searchCompleted = false;
}

export type StringMap = {
  [key: string]: string;
};

export type ResultMap = {
  [key: string]: MigrationResult;
};

export type HttpErrorResponse = {
  status: number;
  error: string;
  message: string;
  description?: string;
  href?: string;
};

export enum MigrationResult {
  PENDING,
  SKIPPED,
  FAILED,
  DONE,
}
