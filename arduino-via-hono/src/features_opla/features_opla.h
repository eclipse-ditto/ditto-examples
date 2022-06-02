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
#ifndef FEATURES_OPLA_H
#define FEATURES_OPLA_H

#include <Arduino_MKRIoTCarrier.h>

#include "IoTAgent.h"

void setCarrier(MKRIoTCarrier* carrier);
Feature temperatureFeature();
Feature humidityFeature();
Feature pressureFeature();
Feature lightFeature();
Feature buttonsFeature();
Feature buzzerFeature();
Feature ledsFeature();
Feature accelerationFeature();
Feature displayFeature();

#endif
