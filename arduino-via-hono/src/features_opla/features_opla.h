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

#include "BoschIoTAgent.h"

void setCarrier(MKRIoTCarrier* carrier);
Feature temperatureFeatureOpla();
Feature humidityFeatureOpla();
Feature pressureFeatureOpla();
Feature lightFeatureOpla();
Feature buttonsFeatureOpla();
Feature buzzerFeatureOpla();
Feature ledsFeatureOpla();
Feature accelerationFeatureOpla();
Feature LCDDisplayFeatureOpla();

#endif // FEATURES_OPLA_H
