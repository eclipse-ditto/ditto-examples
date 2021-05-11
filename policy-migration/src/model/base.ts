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
  pending: string[] = [];
  succeeded: string[] = [];
  skipped: string[] = [];
  failed: Map<string, DittoErrorResponse> = new Map();

  migrationStartedAt = new Date();
}

export type StringMap = {
  [key: string]: string;
};

export type DittoMessage = {
  topic: string;
  headers: StringMap;
  path: string;
  value?: unknown;
};

export type DittoResponse = DittoMessage & {
  status: number;
};

export type DittoErrorResponse = DittoResponse & {
  value: {
    status: number;
    error: string;
    message: string;
    description?: string;
    href?: string;
  };
};
