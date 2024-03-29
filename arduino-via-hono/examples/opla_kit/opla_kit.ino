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
#include "feature_test.h"
#include "features_opla.h"

#include "config_wifi.h"
#include "config.h"

#define BOARD_TYPE "Arduino MKR WiFi 1010"
#define BOARD_FQBN "arduino:samd:mkrwifi1010"

IoTAgent agent =
  IoTAgent(SECRET_SSID, SECRET_PASS, A0);

MKRIoTCarrier carrier;

void setup() {

  Serial.begin(9600);
  // Waiting for Serial to start
  while (!Serial);

  CARRIER_CASE = true;
  carrier.begin();
  setCarrier(&carrier);

  // enable IoTAgent debug
  setDebugLevel(DebugLevel::TRACE);

  agent
    .addAttribute("Type", String(BOARD_TYPE))
    .addAttribute("FQBN", String(BOARD_FQBN))
    .addFeature(testFeature())
    .addFeature(temperatureFeature())
    .addFeature(humidityFeature())
    .addFeature(pressureFeature())
//    .addFeature(lightFeature())
    .addFeature(buttonsFeature())
    .addFeature(buzzerFeature())
    .addFeature(ledsFeature())
    .addFeature(accelerationFeature())
    .addFeature(displayFeature());

  // connecting the agent to the MQTT host
  agent.connect(
    MQTT_HOST, MQTT_PORT,
    TENANT_ID, THING_NAMESPACE, THING_NAME,
    DEVICE_AUTH_ID, DEVICE_PASSWORD
  );
}

void loop() {
  while(!carrier.Light.colorAvailable()) {
    delay(5);
  }
  agent.loop();
  delay(3000);
}
