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
#include <Arduino.h>
#include "settings.h"
#include "printer.h"
#include "octopus.h"
#include "hono.h"
#include "sensorPublish.h"
#include "sensorprint.h"

ADC_MODE(ADC_VCC); // enable reading in VCC of ESP8266

Octopus octopus(DEFAULT_NEOPIXEL_BRIGHTNESS);
Hono hono(hono_mqtt_broker, hono_mqtt_port, hono_mqtt_server_fingerprint, hono_mqtt_server_publickey);

unsigned long lastSensorUpdateMillis = SENSOR_UPDATE_RATE_MS;
unsigned long sensorUpdateRate = SENSOR_UPDATE_RATE_MS;

void setup()
{
  Serial.begin(115200);
  while (!Serial)
    ;

  Serial.println("                             "); // print some spaces to let the Serial Monitor catch up
  Serial.println();

  Printer::printlnMsg("Reset reason", ESP.getResetReason());

  octopus.begin();
  octopus.connectToWifi(wifi_ssid, wifi_password);

  if (!hono.connect())
  {
    Printer::printlnMsg("Main", "ERROR: Could not connect to Hono. Restarting octopus");
    ESP.restart();
  }

  Serial.println();
}

void customMessageHandler(DynamicJsonDocument root, String command, String replyTopic)
{
  const char *topic = root["topic"];
  const char *path = root["path"];

  String toggleLeftLedPath = "/features/LeftLED/inbox/messages/toggle";
  String toggleRightLedPath = "/features/RightLED/inbox/messages/toggle";

  // TODO /features/LeftLED/inbox/messages/switch-on-for-duration

  Printer::printMsg("Cmd", "Received command: ");
  Serial.println(command);
  Printer::printMsg("Cmd", "Containing DittoProtocol topic: ");
  Serial.println(topic);
  Printer::printMsg("Cmd", "With DittoProtocol path: ");
  Serial.println(path);

  if (toggleLeftLedPath.equals(path))
  {
    root["value"] = octopus.toggleLed(LEFT_NEOPIXEL_LED);
    root["status"] = 200;
  }
  else if (toggleRightLedPath.equals(path))
  {
    root["value"] = octopus.toggleLed(RIGHT_NEOPIXEL_LED);
    root["status"] = 200;
  }
  /*else if () 
  {
    JsonObject value = root["value"];
    const long red = value["r"];
    const long green = value["g"];
    const long blue = value["b"];
    const long white = value["w"];
    octopus.showColor(0, red, green, blue, white);
    octopus.showColor(1, red, green, blue, white);

    root["value"] = "\"Command '" + command + "' executed\"";
    root["status"] = 200;
  }
  else if (command.equals("change_update_rate"))
  {
    sensorUpdateRate = root["value"];

    root["value"] = "\"Command '" + command + "' executed\"";
    root["status"] = 200;
  }*/
  else
  {
    root["value"] = "\"Command unknown: '" + command + "'\"";
    root["status"] = 404;
  }

  String output;
  serializeJson(root, output);
  String replyTopicAndStatusCode = replyTopic + "200";
  hono.publish(replyTopicAndStatusCode.c_str(), output);
}

void loop()
{
  if (!hono.deviceIsConnected())
  {
    octopus.showColor(1, 0xff, 0, 0, 0); // red

    char hono_username[strlen(hono_device_auth_id) + strlen(hono_tenant) + 2];
    strcpy(hono_username, hono_device_auth_id);
    strcat(hono_username, "@");
    strcat(hono_username, hono_tenant);
    hono.connectDevice(hono_device_id, hono_username, hono_device_password);
    octopus.showColor(1, 0, 0xff, 0, 0); // green
    hono.subscribe("command///req/#");
    hono.registerOnDittoProtocolMessage(customMessageHandler);
  }

  if (millis() - lastSensorUpdateMillis > sensorUpdateRate)
  {
    lastSensorUpdateMillis = millis();
    static Bme680Values bme680Values;
    static Bno055Values bno055Values;
    memset(&bme680Values, 0, sizeof(bme680Values));
    memset(&bno055Values, 0, sizeof(bno055Values));
    octopus.readBno055(bno055Values);
#ifdef BME280
    octopus.readBme280(bme680Values);
#else
    octopus.readBme680(bme680Values);
#endif
    float vcc = octopus.getVcc();

    LedValues leftLed = octopus.readLed(0);
    LedValues rightLed = octopus.readLed(1);

    hono.publish(buildDittoProtocolMsg(ditto_namespace, ditto_thing_name, vcc, bme680Values, bno055Values, leftLed, rightLed));
  }
  delay(LOOP_DELAY);
  hono.loop();
}
