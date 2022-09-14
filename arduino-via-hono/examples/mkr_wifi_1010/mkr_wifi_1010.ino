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

#include "IoTAgent.h"
#include "feature_wifi.h"
#include "feature_led.h"

#include "config_wifi.h"
#include "config.h"

#define BOARD_TYPE "Arduino MKR WiFi 1010"
#define BOARD_FQBN "arduino:samd:mkrwifi1010"

IoTAgent agent =
  IoTAgent(SECRET_SSID, SECRET_PASS, A0);

void setup() {
  // enable IoTAgent debug
  setDebugLevel(DebugLevel::TRACE);

  Serial.begin(9600);
  // Waiting for Serial to start
  while (!Serial);

  agent
    .addAttribute("Type", String(BOARD_TYPE))
    .addAttribute("FQBN", String(BOARD_FQBN))
    .addFeature(wifiFeature())
    .addFeature(ledFeature());

  // connecting the agent  to the MQTT host
  agent.connect(
    MQTT_HOST, MQTT_PORT,
    TENANT_ID, THING_NAMESPACE, THING_NAME,
    DEVICE_AUTH_ID, DEVICE_PASSWORD
  );
}

void loop() {
  agent.loop();
  delay(3000);
}
