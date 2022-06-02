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
#include "feature_led.h"

#include <utility/wifi_drv.h>

const bool setLEDColor(const byte r, const byte g, const byte b) {
  debug("setLEDColor(r: %i, g: %i, b: %i)", r, g, b);
  if (r > 255 || g > 255 || b > 255) {
    Debug.print(DBG_ERROR, "Led: Incorrect values");
    return false;
  }

  WiFiDrv::pinMode(25, OUTPUT); //define green pin
  WiFiDrv::pinMode(26, OUTPUT); //define red pin
  WiFiDrv::pinMode(27, OUTPUT); //define blue pin

  WiFiDrv::analogWrite(25, r);
  WiFiDrv::analogWrite(26, g);
  WiFiDrv::analogWrite(27, b);
  return true;
}

bool setColor(JsonObjectConst& json) {
  Debug.print(DBG_INFO, "setColor");
  //TODO return error if setLEDColor is false
  return setLEDColor(json["r"], json["g"], json["b"]);
}

Feature ledFeature() {
  return Feature("led", std::vector<String>{"org.eclipse.ditto.examples.arduino:LED:1.0.0"})
    .addCommand("setColor", setColor);
}
