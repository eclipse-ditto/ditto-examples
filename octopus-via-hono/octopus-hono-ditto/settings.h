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
#ifndef SETTINGS_H
#define SETTING_H

// ---- WiFi configuration ----
#define WIFI_SSID "XXX"         // The SSID of the WiFi you want your octopus board to connect to
#define WIFI_PASSWORD "XXX"     // The password of the WiFi you want your octopus board to connect to

// ---- Ditto registration properties ----
#define DITTO_NAMESPACE "org.eclipse.ditto" // The namespace to use for your Ditto Thing
#define DITTO_THING_ID "XXX"

// ---- Hono registration properties ----
#define HONO_TENANT "org.eclipse.ditto" // The tenant id of your Hono tenant
#define HONO_DEVICE_ID "XXX"        // The Hono device id
#define HONO_DEVICE_AUTH_ID "XXX"   // The Hono auth id
#define HONO_DEVICE_PASSWORD "XXX"  // The Hono device password

// ---- Update rate of sensors ----
#define LOOP_DELAY 500 // Print+send updated sensor values every 500ms

// ---- Hono MQTT configuration ----
#define MQTT_BROKER "hono.eclipse.org"
#define MQTT_PORT 8883
/* SHA-1 fingerprint of the server certificate of the MQTT broker, UPPERCASE and spacing */
/* openssl s_client -connect hono.eclipse.org:8883 < /dev/null 2>/dev/null | openssl x509 -fingerprint -noout -in /dev/stdin */
#define MQTT_SERVER_FINGERPRINT "8E 64 5C A0 4C C8 8B E7 86 F0 32 64 1D 9A 9E A3 F3 6A 4A DF"

#endif
