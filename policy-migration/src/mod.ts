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

import { Config, ConfigFactory } from './config/config.ts';
import { initLog } from './log.ts';
import { PolicyMigration } from './migration.ts';

const config: Config = ConfigFactory.loadFromFile();

await initLog(config);
const migration = new PolicyMigration(config);
migration.start();
