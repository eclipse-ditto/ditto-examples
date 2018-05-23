/*
 * Copyright (c) 2017 Bosch Software Innovations GmbH.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/index.php
 *
 * Contributors:
 *    Bosch Software Innovations GmbH - initial contribution
 */
#include <Arduino.h>
#include "hono.h"
#include "printer.h"

Hono::Hono(const char* mqttBroker_, const int mqttPort_, const char* mqttServerFingerprint_)
  : mqttBroker(mqttBroker_), mqttPort(mqttPort_), mqttServerFingerprint(mqttServerFingerprint_) {
}

bool Hono::connect() {
  mqttClient.setServer(mqttBroker, mqttPort);
  if (!wiFiClient.connect(mqttBroker, mqttPort)) {
    Printer::printlnMsg("Hono", "Connect failed.");
    return false;
  } else {
    Printer::printlnMsg("Hono", "Secure connection established");
  }
         
  if (!wiFiClient.verify(mqttServerFingerprint, mqttBroker)) {
    Printer::printlnMsg("Hono", "Failed to verify certificate.");
    return false;
  } else {
    Printer::printlnMsg("Hono", "Server certificate verified");
  }

  return true;
}

bool Hono::deviceIsConnected() {
  return mqttClient.connected();
}

void Hono::connectDevice(const char* deviceId, const char* authId, const char* devicePassword) {
    Printer::printMsg("Hono", "Broker login");
    
    while (!deviceIsConnected())
    {
        Serial.print(".");
        /* If connected to the MQTT broker... */
        if (mqttClient.connect(deviceId, authId, devicePassword))
        {
            Serial.println("OK");
        } else {
            /* otherwise wait for 1 second before retrying */
            delay(1000);
        }
    }
    
    mqttClient.loop();
}

void Hono::publish(String payload) {
  Printer::printlnMsg("Hono", payload);
  /* Publish all available data to the MQTT broker */
  const char* topic = "telemetry";
  const size_t requiredLength = 5 + 2+strlen(topic) + payload.length();

  if (requiredLength > MQTT_MAX_PACKET_SIZE) {
    Printer::printlnMsg("Hono", "Cannot publish: Message is too big.");
    Printer::printMsg("Hono", "Increase MQTT_MAX_PACKET_SIZE in PubSubClient.h to at least ");
    Serial.println(requiredLength);
  }

  int publishResult = mqttClient.publish(topic, payload.c_str());
  if (!publishResult) {
    Printer::printMsg("Hono", "Publish failed");
    Serial.println(publishResult);
  }
}
