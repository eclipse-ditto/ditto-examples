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
#include <string.h>

#include "BoschIoTAgent.h"
#include "feature_wifi.h"
#include "feature_test.h"

#include "config_wifi.h"
#include "config_bosch_iot.h"

#define BOARD_TYPE "Arduino MKR 1000 WiFi"
#define BOARD_FQBN "arduino:samd:mkr1000"

BoschIoTAgent agent =
  BoschIoTAgent(SECRET_SSID, SECRET_PASS, A0);

void setup() {
  // enable debug in Arduino_DebugUtils, debug levels: DBG_NONE, DBG_ERROR, DBG_WARNING, DBG_INFO ,DBG_DEBUG, DBG_VERBOSE
  setDebugMessageLevel(DBG_DEBUG);
  // enable BoschIoTAgent debug
  setDebugLevel(DebugLevel::TRACE);

  Serial.begin(9600);
  // Waiting for Serial to start
  while (!Serial);

  agent
    .addAttribute("Type", String(BOARD_TYPE))
    .addAttribute("FQBN", String(BOARD_FQBN))
    .addFeature(wifiFeature())
    .addFeature(testFeature());
  // connecting the agent  to the MQTT broker
  agent.connect(
    MQTT_BROKER, MQTT_PORT,
    TENANT_ID, THING_NAMESPACE, THING_NAME,
    DEVICE_AUTH_ID, DEVICE_PASSWORD
  );
}

void loop() {
  agent.loop();
  delay(3000);
}
