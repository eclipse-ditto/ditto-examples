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
#include "feature_wifi.h"

#include <Arduino_ConnectionHandler.h>

String ssid() {
  return WiFi.SSID();
}

String bssid() {
  byte out[6];
  WiFi.BSSID(out);
  String bssid;
  for (int i = 0; i < 6; i++) {
    if (i != 0) bssid += ".";
    bssid += String(out[i]);
  }
  return bssid;
}

long rssi() {
  return WiFi.RSSI();
}

unsigned long encryptionType() {
  return WiFi.encryptionType();
}

unsigned long status() {
  return WiFi.status();
}

String macAddress() {
  byte out[6];
  WiFi.macAddress(out);
  String mac;
  for (int i = 0; i < 6; i++) {
    if (i != 0) mac += ".";
    mac += out[i];
  }
  return mac;
}

long localIP() {
  return WiFi.localIP();
}

long subnetMask() {
  return WiFi.subnetMask();
}

String gatewayIP() {
  IPAddress address = WiFi.gatewayIP();
  return String(address[0]) + String(".") + String(address[1]) + String(".") + String(address[2]) + String(".") + String(address[3]) ;
}

Feature wifiFeature() {
  return Feature("wifi", std::vector<String>{"org.eclipse.ditto.examples.arduino.connectivity:WiFi:1.0.0"})
    .addProperty("SSID", Category::STATUS, QoS::EVENT, ssid)
    .addProperty("BSSID", Category::STATUS, QoS::EVENT, bssid)
    .addProperty("RSSI", Category::STATUS, QoS::EVENT, rssi)
    .addProperty("encryption-type", Category::STATUS, QoS::EVENT, encryptionType)
    .addProperty("status", Category::STATUS, QoS::EVENT, status)
    .addProperty("mac-address", Category::STATUS, QoS::EVENT, macAddress)
    .addProperty("local-ip", Category::STATUS, QoS::EVENT, localIP)
    .addProperty("subnet-mask", Category::STATUS, QoS::EVENT, subnetMask)
    .addProperty("gateway-ip", Category::STATUS, QoS::EVENT, gatewayIP);
}
