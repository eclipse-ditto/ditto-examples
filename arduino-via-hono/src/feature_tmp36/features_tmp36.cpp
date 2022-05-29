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
#include "features_opla.h"

float tmp36();

const byte tmp36Pin = A0;
MKRIoTCarrier __carrier;
float tmp36() {
  pinMode(tmp36Pin, INPUT);
  float voltage = analogRead(tmp36Pin) * 3.3;
  voltage /= 1024;
  Debug.print(DBG_INFO, F("%f"), voltage);
  float tmp36 = (voltage - 0.5) * 100;
  Debug.print(DBG_INFO, F("%f"), tmp36);
  return tmp36;
}

Feature tmp36Feature() {
  return Feature("tmp36", std::vector<String>{})
    .addProperty("value", Category::STATUS, QoS::EVENT, temp);
}
