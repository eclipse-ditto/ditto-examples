/*
 * Copyright (c) 2017 Contributors to the Eclipse Foundation
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
#ifndef HONO_H
#define HONO_H

#include <ESP8266WiFi.h>
#include <WiFiClientSecure.h>
#include <PubSubClient.h>
#include "octopus.h"

#define MQTT_MAX_PACKET_SIZE 2048

class Hono {
  private:
    WiFiClientSecure wiFiClient;
    PubSubClient mqttClient = PubSubClient(wiFiClient);
  
    const char* mqttBroker;
    const int mqttPort;
    const char* mqttServerFingerprint;
    
  public:
    Hono(const char* mqttBroker, const int mqttPort, const char* mqttServerFingerprint);

    bool connect();
    bool deviceIsConnected();
    void connectDevice(const char* deviceId, const char* authId, const char* devicePassword);
    void publish(String payload);
};

#endif
