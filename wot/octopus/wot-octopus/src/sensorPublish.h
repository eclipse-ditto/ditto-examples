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

String buildDittoProtocolMsg(const char *ditto_namespace,
                             const char *ditto_thing_name,
                             float power,
                             const Bme680Values &bme680Values,
                             const Bno055Values &bno055Values,
                             const LedValues &leftLed,
                             const LedValues &rightLed);

void setVoltageAsAttribute(const String &attributeName, float sensorValue, DynamicJsonDocument &thing);
void setSensorMinMaxPropertiesForFeature(const String &featureId, const String &sensorSuffix, float sensorValue, float minValue, float maxValue, JsonObject &features);
void setSensor3dPropertiesForFeature(const String &featureId, const String &sensorSuffix, float xValue, float yValue, float zValue, JsonObject &features);
void setSensor4dPropertiesForFeature(const String &featureId, const String &sensorSuffix, float wValue, float xValue, float yValue, float zValue, JsonObject &features);
void setLedPropertiesForFeature(const String &featureId, const LedValues &ledValues, JsonObject &features);
void updateMinMax(float power, const Bme680Values &bme680Values, const Bno055Values &bno055Values);
