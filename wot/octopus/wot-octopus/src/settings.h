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

// ---- WiFi configuration ----
char wifi_ssid[]                    = "tp-link";                // The SSID of the WiFi you want your octopus board to connect to
const char *wifi_password           = "uz5,HE2#o0a4stM";        // The password of the WiFi you want your octopus board to connect to

// ---- Ditto registration properties ----
const char *ditto_namespace         = "org.eclipse.ditto";      // The namespace to use for your Ditto Thing
const char *ditto_thing_name        = "octopus-test-device-1";

// ---- Hono registration properties ----
const char *hono_tenant             = "org.eclipse.ditto";                          // The tenant id of your Hono tenant
const char *hono_device_id          = "org.eclipse.ditto:octopus-test-device-1";    // The Hono device id
const char *hono_device_auth_id     = "org.eclipse.ditto_octopus-test-device-1";    // The Hono auth id
const char *hono_device_password    = "my-pwd";                                     // The Hono device password

// ---- Hono MQTT configuration ----
const char *hono_mqtt_broker        = "hono.eclipseprojects.io";
const int hono_mqtt_port            = 8883;

/* SHA-1 fingerprint of the server certificate of the MQTT broker, UPPERCASE and spacing */
/* openssl s_client -connect hono.eclipseprojects.io:8883 < /dev/null 2>/dev/null | openssl x509 -fingerprint -noout -in /dev/stdin */
const char hono_mqtt_server_fingerprint[] PROGMEM = "79:A4:4B:54:AE:95:A0:D3:D0:FC:C5:46:98:C0:80:06:83:FB:FC:93";

/* Public key of the server certificate of the MQTT broker. */
/* openssl s_client -connect hono.eclipseprojects.io:8883 < /dev/null 2>/dev/null | openssl x509 -pubkey -noout -in /dev/stdin */
const char hono_mqtt_server_publickey[] PROGMEM = R"PUBKEY(
-----BEGIN PUBLIC KEY-----
MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAut/3WtsdNFx2QblZyw+Y
tHFv3+1gV/mz+EriFkOFH3Ld1aziquElqllp/JoAqD8pgKw97BF/a9JydNl3jZy0
GL5nYMpNLel2pb2Vc+so1JoXB2QTIyxuLePlrB+BAb7cum0KAPy/fj1cNxq80AYG
csrvNNFPyI/zu/zfSMtWgEBtmIfhb0v/aKljxUxRGFvA2CDBc+McuCQbHWL5UO4K
FPy7RsokIyfWh+Y2v7ZlXOgUINjA1m+ChIGnUffgv9OvUGBmV96rAfIz11BjrIgT
SZItfa8ns0wRMj+oBxuh06WoA8Rh+A4QIH8V3CJLJaUALgLizrBoYbS0OM/rAAe9
tQIDAQAB
-----END PUBLIC KEY-----
)PUBKEY";

//#define BME280 // uncomment this line if your board has a BME280 instead of BME680

// ---- Update rate of sensors ----
#define SENSOR_UPDATE_RATE_MS       60000   // send updated sensor value every 60 seconds
#define LOOP_DELAY                  100     // main loop delay, e.g. also required to be low to consume MQTT messages

#define LEFT_NEOPIXEL_LED           0
#define RIGHT_NEOPIXEL_LED          1
#define DEFAULT_NEOPIXEL_BRIGHTNESS 1       // value from 0-255
