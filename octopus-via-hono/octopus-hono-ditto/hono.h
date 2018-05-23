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
