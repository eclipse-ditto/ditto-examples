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
#include "hono.h"
#include "printer.h"

Hono::Hono(const char *mqttBroker_,
           const int mqttPort_,
           const char *mqttServerFingerprint_,
           const char *mqttServerPublicKey_) : mqttBroker(mqttBroker_),
                                               mqttPort(mqttPort_),
                                               mqttServerFingerprint(mqttServerFingerprint_),
                                               mqttServerPublicKey(mqttServerPublicKey_)
{
}

void (*onDittoProtocolMessage)(DynamicJsonDocument, String, String);

void Hono::registerOnDittoProtocolMessage(HONO_COMMAND_CALLBACK_SIGNATURE)
{
  onDittoProtocolMessage = callback;
}

void honoCommandReceived(char *topic, byte *payload, unsigned int length)
{
  Printer::printMsg("Hono", "Received command on topic: ");
  Serial.println(topic);

  String topicStr = String(topic);
  topicStr.replace("command///req/", "");
  String replyTopic = "command///res/" + topicStr.substring(0, topicStr.indexOf("/")) + "/";
  String command = topicStr.substring(topicStr.lastIndexOf("/") + 1, topicStr.length());

  DynamicJsonDocument root(INCOMING_MSG_LENGTH);
  deserializeJson(root, payload, length);

  if (root.size() > 0 && root.containsKey("topic") && root.containsKey("path"))
  {
    if (onDittoProtocolMessage)
    {
      onDittoProtocolMessage(root, command, replyTopic);
    }
    else
    {
      Printer::printlnMsg("Hono", "onDittoProtocolMessage function is not defined");
    }
  }
  else if (!root.containsKey("topic"))
  {
    Printer::printlnMsg("Hono", "[error] - Invalid JSON Object  - not a DittoProtocol message.");
  }
}

bool Hono::connect()
{
  wiFiClient.setFingerprint(mqttServerFingerprint);
  if (!wiFiClient.connect(mqttBroker, mqttPort))
  {
    Printer::printlnMsg("Hono", "Connect via WiFiClient failed (bad fingerprint?).");
    return false;
  }
  else
  {
    Printer::printlnMsg("Hono", "Secure connection established");
  }

  return true;
}

bool Hono::deviceIsConnected()
{
  bool wifiConnected = wiFiClient.connected();
  bool mqttConnected = mqttClient.connected();
  if (!wifiConnected || !mqttConnected)
  {
    Printer::printMsg("Hono", "deviceIsConnected() check: wifiConnected: ");
    Serial.println(wifiConnected);
    Printer::printMsg("Hono", "mqttConnected: ");
    Serial.println(mqttConnected);
  }
  return mqttConnected;
}

void Hono::connectDevice(const char *deviceId, const char *authId, const char *devicePassword)
{
  Printer::printlnMsg("Hono", "Broker login");
  while (!deviceIsConnected())
  {
    /* If connected to the MQTT broker... */
    Printer::printlnMsg("Hono", "Connecting deviceId: " + String(deviceId) + " - authId: " + String(authId) + " - pw: " + String(devicePassword));
    if (mqttClient.connect(deviceId, authId, devicePassword))
    {
      Printer::printlnMsg("Hono", "Connected to MQTT endpoint");
      mqttClient.setBufferSize(MQTT_BUFFER_SIZE);
      mqttClient.setCallback(honoCommandReceived);
    }
    else
    {
      /* otherwise wait for 1 second before retrying */
      delay(1000);
    }
  }

  mqttClient.loop();
}

void Hono::publish(String payload)
{
  const char *topic = "telemetry";
  this->publish(topic, payload);
}

void Hono::publish(const char *topic, String payload)
{
  Printer::printMsg("Hono", "Publishing on topic: ");
  Serial.println(topic);
  Printer::printlnMsg("Hono", payload);

  const size_t requiredLength = 5 + 2 + strlen(topic) + payload.length();

  if (requiredLength > mqttClient.getBufferSize())
  {
    Printer::printlnMsg("Hono", "Cannot publish: Message is too big.");
    Printer::printMsg("Hono", "Increase MQTT_BUFFER_SIZE in hono.h to at least ");
    Serial.println(requiredLength);
    return;
  }

  int publishResult = mqttClient.publish(topic, payload.c_str());
  if (!publishResult)
  {
    Printer::printMsg("Hono", "Publish failed with result: ");
    Serial.println(publishResult);
    this->deviceIsConnected();
  }
  else
  {
    Printer::printlnMsg("Hono", "Publish success!");
  }
}

void Hono::subscribe(const char *topic)
{
  Printer::printMsg("Hono", "Subscribing to topic: ");
  Serial.println(topic);
  mqttClient.subscribe(topic);
}

void Hono::loop()
{
  mqttClient.loop();
}
