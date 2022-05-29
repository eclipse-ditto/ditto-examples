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
#include "hono.h"

#include "debug.h"

RequestInfo requestInfo(const String& topic) {
  static int commandRequestPrefix = strlen(COMMAND_REQUEST_TOPIC) - 1;
  const int pos = topic.indexOf('/', commandRequestPrefix);
  const int len = topic.length();
  if (pos == -1) {
    // reply id and command separator not found!
    RequestInfo ri = {
      .valid = false,
      .reqId = String((char*)0),
      .commandId = String((char*)0)
    };
    return ri;
  } else {
    // valid
    RequestInfo ri = {
      .valid = true,
      .reqId = topic.substring(commandRequestPrefix, pos),
      .commandId = topic.substring(pos + 1 , len)};
    return ri;
  }
}

String commandResponseTopic(const String& reqId, const int& status) {
  String  commandResponseTopic = COMMAND_RESPONSE_TOPIC_PREFIX;
  commandResponseTopic += reqId;
  commandResponseTopic += "/";
  commandResponseTopic.concat(status);
  return commandResponseTopic;
}

// DOESN'T work .. seems stateful lambda.binds could not be cast to function*
// 1. options
// using namespace std::placeholders;
// auto callback = std::bind(&BoschIoTAgent::onCommand, this, _1, _2, _3);
// 2. option
// void (*lambda) (char* topic, uint8_t* payload, unsigned int length) = [this](char* topic, uint8_t* payload, unsigned int length) -> void {
//     this->onCommand(topic, payload, length);
// };
static Handler handler = nullptr;
static MqttClient* mqttClient = nullptr;

bool subscribe(String topic, MqttClient& mqttClient);
bool unsubscribe(String topic, MqttClient& mqttClient);

void dumpSending(const char* topic, const JsonDocument& message) {
  if (isTraceOn()) {
    if (message.isNull()) {
      trace(F("Sending null to %s ..."), topic);
    } else {
      trace(F("Sending to %s:"), topic);
      #if defined(JSON_PRETTY)
      serializeJsonPretty
      #else
      serializeJson
      #endif
        (message, *getDebugStream());
      trace(F(" ..."));
    }
  } else {
    debug(F("Sending to %s ..."), topic);
  }
}
bool Hono::send(const char* topic, const JsonDocument& message, const int qos) {
  dumpSending(topic, message);
  unsigned long size = message.isNull() ? 0 : (unsigned long)
    #if defined(JSON_PRETTY)
    measureJsonPretty
    #else
    measureJson
    #endif
      (message);
  if (!mqttClient.beginMessage(topic, size, false, qos)) {
    debug(F("  Failed (start message)!"));
    return true;
  }
  if (!message.isNull()) {
    if (!
        #if defined(JSON_PRETTY)
        serializeJsonPretty
        #else
        serializeJson
        #endif
          (message, mqttClient)) {
      debug(F("  Failed (write message)!"));
      return true;
    }
  }
  if (mqttClient.endMessage()) {
    info(F("  Done"));
    return true;
  } else {
    info(F("  Failed"));
    return false;
  }
}
bool Hono::send(const QoS qos, const JsonDocument& message) {
  send(qos == QoS::EVENT ? EVENT_TOPIC : TELEMETRY_TOPIC, message, qos == QoS::EVENT ? 1 : 0);
}

void dumpReceive(const char* topic, const JsonDocument& message) {
  if (isTraceOn()) {
    if (message.isNull()) {
      trace(F("Received null from %s."), topic);
    } else {
      trace(F("Received from %s:"), topic);
      #if defined(JSON_PRETTY)
      serializeJsonPretty
      #else
      serializeJson
      #endif
        (message, *getDebugStream());
    }
  } else {
    debug(F("Received from %s."), topic);
  }
}
void onReceive(const String& topic, JsonDocument& message, MqttClient* mqttClient) {
  // TODO - try filtering ... seems not working correctly. If working the extra size of the json doc could be deminished
  // StaticJsonDocument<64> filter;
  // filter["topic"] = true;
  // filter["path"] = true;
  // filter["value"] = true;
  // filter["headers"]["ditto-message-feature-id"] = true;
  // filter["headers"]["correlation-id"] = true;
  // DeserializationOption::Filter options(filter);
  // DeserializationError error = deserializeJson(message, *mqttClient, options);

  DeserializationError deserialieError = deserializeJson(message, *mqttClient);
  if (deserialieError) {
    error(F("Failed to deserialie message: %s!"), deserialieError.c_str());
  } else {
    dumpReceive(topic.c_str(), message);
    handler(topic.c_str(), message);
  }
}
void onReceive(int messageSize) {
  if (messageSize > 0) {
    const String topic = mqttClient->messageTopic();

    if (messageSize > 256) {
      if (messageSize > 512) {
        if (messageSize > 1024) {
          if (messageSize > 2048) {
            error(F("Too big message received (size %d)! Skip it"), messageSize);
            for (int i = messageSize; i-- > 0; mqttClient->read()); // skip
          } else {
            StaticJsonDocument<2048> message;
            onReceive(topic, message, mqttClient);
          }
        } else {
          StaticJsonDocument<1536> message;
          onReceive(topic, message, mqttClient);
        }
      } else {
        StaticJsonDocument<1024> message;
        onReceive(topic, message, mqttClient);
      }
    } else {
      StaticJsonDocument<512> message;
      onReceive(topic, message, mqttClient);
    }
  }
}
void Hono::onCommand(const Handler& handler) {
  ::handler = handler; // last handler win / receieves all
  ::mqttClient = &mqttClient;
}

bool Hono::connect(
    const char* mqttBroker, const int mqttPort,
    const char* clientId, const char* user, const char* pass) {
  mqttClient.setId(clientId);
  mqttClient.setUsernamePassword(user, pass);

  // info(F("+--+-> mqttclient %d"), &mqttClient);
  // info(F("+--+-> server %s : %d"), mqttBroker, mqttPort);
  // info(F("+--+-> connect %s %s / %s"), clientId, user, pass);
  if (mqttClient.connect(mqttBroker, mqttPort)) {
    if (handler != nullptr) {  // subscribing so the device could gets commands
      info(F("Subscribe for commands"));
      mqttClient.onMessage(onReceive);
      subscribe(COMMAND_REQUEST_TOPIC, mqttClient);
    }
    return true;
  } else {
    return false;
  }
}

void Hono::loop() {
  mqttClient.poll();
}

void Hono::disconnect() {
  if (handler != nullptr) {
    unsubscribe(COMMAND_REQUEST_TOPIC, mqttClient);
  }
  mqttClient.stop();
}

bool subscribe(const String topic, MqttClient& mqttClient) {
  info(F("Subscribing to topic %s ... "), topic.c_str());
  if (mqttClient.subscribe(topic.c_str())) {
    info(F("  Done"));
    return true;
  } else {
    info(F("  Failed"));
    return false;
  }
}

bool unsubscribe(const String topic, MqttClient& mqttClient) {
  info(F("Unsubscribing from topic %s ... "), topic);
  if (mqttClient.unsubscribe(topic.c_str())) {
    info(F("  Done"));
    mqttClient.poll();
    return true;
  } else {
    info(F("  Failed"));
    return false;
  }
}
