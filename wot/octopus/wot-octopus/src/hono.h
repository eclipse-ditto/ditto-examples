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

#include <ESP8266WiFi.h>
#include <WiFiClientSecure.h>
#include <PubSubClient.h>
#include <ArduinoJson.h>
#include "octopus.h"

#define MQTT_BUFFER_SIZE 2048
#define INCOMING_MSG_LENGTH 1024

#define HONO_COMMAND_CALLBACK_SIGNATURE void (*callback)(DynamicJsonDocument, String, String)

class Hono
{
private:
  const char *mqttBroker;
  const int mqttPort;
  const char *mqttServerFingerprint;
  const char *mqttServerPublicKey;

  WiFiClientSecure wiFiClient;
  PubSubClient mqttClient = PubSubClient(mqttBroker, mqttPort, wiFiClient);

public:
  Hono(const char *mqttBroker, const int mqttPort, const char *mqttServerFingerprint, const char *mqttServerPublicKey);

  bool connect();
  bool deviceIsConnected();
  void connectDevice(const char *deviceId, const char *authId, const char *devicePassword);
  void publish(String payload);
  void publish(const char *topic, String payload);
  void subscribe(const char *topic);
  void loop();
  void registerOnDittoProtocolMessage(HONO_COMMAND_CALLBACK_SIGNATURE);
};
