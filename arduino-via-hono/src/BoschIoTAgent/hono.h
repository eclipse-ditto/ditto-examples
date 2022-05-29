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
#ifndef HONO_H_INCLUDED
#define HONO_H_INCLUDED

#include <String.h>
#include <SSLClient.h>
#include <ArduinoMqttClient.h>
#include <ArduinoJson.h>

#define HONO_SHORT
#define JSON_PRETTY

#ifdef HONO_SHORT
  #define EVENT_TOPIC  "e"
#else
  #define EVENT_TOPIC  "event"
#endif // HONO_SHORT

#ifdef HONO_SHORT
  #define TELEMETRY_TOPIC "t"
#else
  #define TELEMETRY_TOPIC "telemetry"
#endif // HONO_SHORT

#ifdef HONO_SHORT
  #define COMMAND_REQUEST_TOPIC "c///q/#"
#else
  #define COMMAND_REQUEST_TOPIC "command///req/#"
#endif // HONO_SHORT

#ifdef HONO_SHORT
  #define COMMAND_RESPONSE_TOPIC_PREFIX "c///s/"
#else
  #define COMMAND_RESPONSE_TOPIC_PREFIX "command///res/"
#endif // HONO_SHORT

enum QoS {
  TELEMETRY,
  EVENT
};

typedef struct  {
  const bool valid;
  const String reqId;
  const String commandId;
} RequestInfo;

RequestInfo requestInfo(const String& topic);
String commandResponseTopic(const String& reqId, const int& status);

typedef void (*Handler)(const char* topic, JsonDocument& message);
// private class for internal user, abstracts hono operations
class Hono {
protected:
  bool send(const char* topic, const JsonDocument& message, const int qos = 0);
  bool send(const QoS qos, const JsonDocument& message);
public:
  Hono(SSLClient* client) : mqttClient(MqttClient(client)) {};
  ~Hono() = default;

  bool sendTelemetry(const JsonDocument& message) {
    loop();
    return send(QoS::TELEMETRY, message);
  }
  bool sendEvent(const JsonDocument& message) {
    loop();
    return send(QoS::EVENT, message);
  }
  void onCommand(const Handler& handler);
  void sendCommandResponse(const String& reqId, const int status, const JsonDocument& response) {
    loop();
    if (reqId.length() != 0) {
      const String topic = commandResponseTopic(reqId, status);
      send(topic.c_str(), response);
    }
  }

  bool connect(
    const char* mqttBroker, const int mqttPort,
    const char* clientId, const char* user, const char* pass);
  void loop();
  void disconnect();
private:
  MqttClient mqttClient;
};

#endif // HONO_H_INCLUDED
