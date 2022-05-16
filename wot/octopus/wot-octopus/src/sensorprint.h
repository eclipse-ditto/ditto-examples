/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
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
#pragma once

#include <Arduino.h>
#include "octopus.h"

void printSensorData(float vcc, const Bme680Values &bme680Values, const Bno055Values &bno055Values);
void printBme680(const Bme680Values &bme680Values);
void printBno055(const Bno055Values &bno055Values);
void printVcc(float power);
