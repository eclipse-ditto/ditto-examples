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
#ifndef BOSCHIOTAGENT_H
#define BOSCHIOTAGENT_H

#include <vector>
#include <map>

#include <Arduino.h>
#include <Arduino_ConnectionHandler.h>
#include <SSLClient.h>
#include <ArduinoJson.h>

#include "config_ssl.h"

#include "debug.h"
#include "hono.h"

enum Category {
  STATUS,
  CONFIGURATION
};

enum Type {
  VOID,
  BOOL,
  LONG,
  UNSIGNED_LONG,
  FLOAT,
  STRING,
  OBJECT
};

class Property {
public:
  Property(
    const String _name,
    const Category _category,
    const Type _tval,
    void* _valueProvider,
    unsigned long _minReportPeriodMS) :
    name(_name), category(_category), tval(_tval), valueProvider(_valueProvider), minReportPeriodMS(_minReportPeriodMS) {}
  ~Property() = default;

  const String name;
  const Category category;
  const Type tval;
  void* valueProvider;
  unsigned long minReportPeriodMS;

  bool isInitialized = false;
  long currentValue;
  long lastReportedValue;
  unsigned long lastReportTime;
};

class Command {
public:
  // must not be called unless by assignment operator
  Command() {}

  Command(
    const String _name,
    const Type _targ,
    const Type _tres,
    void* _handler) :
    name(_name), targ(_targ), tres(_tres), handler(_handler) {}
  ~Command() = default;

  Command& operator= (const Command& command) {
    name = String("");
    name.concat(command.name);
    targ = command.targ;
    tres = command.tres;
    handler = command.handler;
    return *this;
  }
  String name;
  Type targ;
  Type tres;
  void* handler; // pointer to handler function TRes (*) (TArg)
};

class BoschIoTAgent;

class Feature {
public:
  Feature(const char* _featureId, std::vector<String> _definitions) : featureId(String(_featureId)), definitions(_definitions) {}
  ~Feature() = default;
private:
  friend BoschIoTAgent;

  String featureId;
  std::vector<String> definitions;

  std::vector<Property> eventProps;
  std::vector<Property> telemetryProps;

  std::map<String, Command> commands;

  template<typename T>
  void addProperty(const char* name, const Category category, const QoS qos, const Type tval, T (*valueProvider)(), unsigned long minReportPeriodM);
  Feature& updateValues(std::vector<Property>& props, unsigned long& time);

public:
  Feature& addProperty(const char* name, const Category category, const QoS qos, bool (*valueProvider)(), unsigned long minReportPeriodMS = 0);
  Feature& addProperty(const char* name, const Category category, const QoS qos, long (*valueProvider)(), unsigned long minReportPeriodMS = 0);
  Feature& addProperty(const char* name, const Category category, const QoS qos, unsigned long (*valueProvider)(), unsigned long minReportPeriodMS = 0);
  Feature& addProperty(const char* name, const Category category, const QoS qos, float (*valueProvider)(), unsigned long minReportPeriodMS = 0);
  Feature& addProperty(const char* name, const Category category, const QoS qos, String (*valueProvider)(), unsigned long minReportPeriodMS = 0);
  Feature& addProperty(const char* name, const Category category, const QoS qos, void (*valueProvider)(JsonObject&), unsigned long minReportPeriodMS = 0);

  Feature& addCommand(const char* name, void (*commandHandler)());
  Feature& addCommand(const char* name, void (*commandHandler)(bool));
  Feature& addCommand(const char* name, void (*commandHandler)(long));
  Feature& addCommand(const char* name, void (*commandHandler)(unsigned long));
  Feature& addCommand(const char* name, void (*commandHandler)(float));
  Feature& addCommand(const char* name, void (*commandHandler)(String));
  Feature& addCommand(const char* name, void (*commandHandler)(JsonObjectConst&));

  Feature& addCommand(const char* name, bool (*commandHandler)());
  Feature& addCommand(const char* name, bool (*commandHandler)(bool));
  Feature& addCommand(const char* name, bool (*commandHandler)(long));
  Feature& addCommand(const char* name, bool (*commandHandler)(unsigned long));
  Feature& addCommand(const char* name, bool (*commandHandler)(float));
  Feature& addCommand(const char* name, bool (*commandHandler)(String));
  Feature& addCommand(const char* name, bool (*commandHandler)(JsonObjectConst&));

  Feature& addCommand(const char* name, long (*commandHandler)());
  Feature& addCommand(const char* name, long (*commandHandler)(bool));
  Feature& addCommand(const char* name, long (*commandHandler)(long));
  Feature& addCommand(const char* name, long (*commandHandler)(unsigned long));
  Feature& addCommand(const char* name, long (*commandHandler)(float));
  Feature& addCommand(const char* name, long (*commandHandler)(String));
  Feature& addCommand(const char* name, long (*commandHandler)(JsonObjectConst&));

  Feature& addCommand(const char* name, unsigned long (*commandHandler)());
  Feature& addCommand(const char* name, unsigned long (*commandHandler)(bool));
  Feature& addCommand(const char* name, unsigned long (*commandHandler)(long));
  Feature& addCommand(const char* name, unsigned long (*commandHandler)(unsigned long));
  Feature& addCommand(const char* name, unsigned long (*commandHandler)(float));
  Feature& addCommand(const char* name, unsigned long (*commandHandler)(String));
  Feature& addCommand(const char* name, unsigned long (*commandHandler)(JsonObjectConst&));

  Feature& addCommand(const char* name, float (*commandHandler)());
  Feature& addCommand(const char* name, float (*commandHandler)(bool));
  Feature& addCommand(const char* name, float (*commandHandler)(long));
  Feature& addCommand(const char* name, float (*commandHandler)(unsigned long));
  Feature& addCommand(const char* name, float (*commandHandler)(float));
  Feature& addCommand(const char* name, float (*commandHandler)(String));
  Feature& addCommand(const char* name, float (*commandHandler)(JsonObjectConst&));

  Feature& addCommand(const char* name, String (*commandHandler)());
  Feature& addCommand(const char* name, String (*commandHandler)(bool));
  Feature& addCommand(const char* name, String (*commandHandler)(long));
  Feature& addCommand(const char* name, String (*commandHandler)(unsigned long));
  Feature& addCommand(const char* name, String (*commandHandler)(float));
  Feature& addCommand(const char* name, String (*commandHandler)(String));
  Feature& addCommand(const char* name, String (*commandHandler)(JsonObjectConst&));

  Feature& addCommand(const char* name, void (*commandHandler)(JsonObject&));
  Feature& addCommand(const char* name, void (*commandHandler)(bool, JsonObject&));
  Feature& addCommand(const char* name, void (*commandHandler)(long, JsonObject&));
  Feature& addCommand(const char* name, void (*commandHandler)(unsigned long, JsonObject&));
  Feature& addCommand(const char* name, void (*commandHandler)(float, JsonObject&));
  Feature& addCommand(const char* name, void (*commandHandler)(String, JsonObject&));
  Feature& addCommand(const char* name, void (*commandHandler)(JsonObjectConst&, JsonObject&));
};

class BoschIoTAgent {
public:
  #if defined(BOARD_HAS_WIFI)
  BoschIoTAgent(const char* ssid, const char* pass, const byte analogPin);
  #elif defined(BOARD_HAS_GSM)
  BoschIoTAgent(const char* pin, const char* apn, const char* login, const char* pass, const byte analogPin);
  #elif defined(BOARD_HAS_NB)
  BoschIoTAgent(const char* pin, const char* apn, const char* login, const char* pass, const byte analogPin);
  #elif defined(BOARD_HAS_LORA)
  BoschIoTAgent(const char* eui, const char* key, const byte analogPin);
  #endif
  ~BoschIoTAgent();

  BoschIoTAgent& addAttribute(const char* name, const bool& value);
  BoschIoTAgent& addAttribute(const char* name, const long& value);
  BoschIoTAgent& addAttribute(const char* name, const unsigned long& value);
  BoschIoTAgent& addAttribute(const char* name, const float& value);
  BoschIoTAgent& addAttribute(const char* name, const String& value);
  BoschIoTAgent& addAttribute(const char* name, const JsonObjectConst& value);
  BoschIoTAgent& addFeature(const Feature feature);

  bool connect(
    const char* mqttBroker, const int mqttPort,
    const char* tenantId, const char* thingNamespace, const char* thingName,
    const char* authId, const char* pass);

  void loop();
  bool disconnect();
private:
  #if defined(BOARD_HAS_WIFI)
  WiFiConnectionHandler* conHndlr = nullptr;
  #elif defined(BOARD_HAS_GSM)
  GSMConnectionHandler& conHndlr = nullptr;
  #elif defined(BOARD_HAS_NB)
  NBConnectionHandler& conHndlr = nullptr;
  #elif defined(BOARD_HAS_LORA)
  LoRaConnectionHandler& conHndlr = nullptr;
  #endif
  //ConnectionHandler conHndlr;
  SSLClient* client;
  Hono* hono;
  const char* thingNamespace;
  const char* thingName;

  DynamicJsonDocument attributes;

  std::map<String, Feature> features;

  void sendAttributes();
  void publish(const QoS qos);
  void onCommand(const String& topic, JsonDocument& message);
  void buildFeaturesUpdate(const QoS qos, JsonObject& value, unsigned long& time);

  friend void onReceive(const char* topic, JsonDocument& message);
};

#endif
