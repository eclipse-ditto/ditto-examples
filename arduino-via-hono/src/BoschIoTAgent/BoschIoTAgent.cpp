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
#include "BoschIoTAgent.h"
#include "hono.h"
#include "ditto.h"

static BoschIoTAgent* agent;
bool checkSignleton() {
  if (agent == nullptr) {
    return true;
  } else {
    error(F("ERROR: Only single agent is supported!"));
    return false;
  }
}

#if defined(BOARD_HAS_WIFI)
BoschIoTAgent::BoschIoTAgent(const char* ssid, const char* pass, const byte analogPin) : attributes(DynamicJsonDocument(512)) {
  if (!checkSignleton()) return;
  conHndlr = new WiFiConnectionHandler(ssid, pass);
#elif defined(BOARD_HAS_GSM)
BoschIoTAgent::BoschIoTAgent(const char* pin, const char* apn, const char* login, const char* pass, const byte analogPin) {
  if (!checkSignleton()) return;
  conHndlr = new GSMConnectionHandler(pin, apn, login, pass);
#elif defined(BOARD_HAS_NB)
BoschIoTAgent::BoschIoTAgent(const char* pin, const char* apn, const char* login, const char* pass, const byte analogPin) {
  if (!checkSignleton()) return;
  conHndlr = new NBConnectionHandler(pin, apn, login, pass);
#elif defined(BOARD_HAS_LORA)
BoschIoTAgent::BoschIoTAgent(const char* eui, const char* key, const byte analogPin) {
  if (!checkSignleton()) return;
  conHndlr = new LoRaConnectionHandler(eui, key);
#endif
  agent = this;

  //Debug level -> SSL_NONE, SSL_ERROR, SSL_WARN, SSL_INFO, SSL_DUMP
  client = new SSLClient(conHndlr->getClient(), TAs, (size_t)TAs_NUM, analogPin, 1, SSLClient::SSL_WARN);
  hono = new Hono(client);
}

BoschIoTAgent::~BoschIoTAgent() {
  agent = nullptr;
  delete(hono);
  hono = nullptr;
  delete(client);
  client = nullptr;
}

void onReceive(const char* topic, JsonDocument& message) {
  agent->onCommand(String(topic), message);
}
bool connect(ConnectionHandler& conHndlr);
bool BoschIoTAgent::connect(
    const char* mqttBroker, const int mqttPort,
    const char* tenantId, const char* thingNamespace, const char* thingName,
    const char* authId, const char* pass) {
  if (!::connect(*conHndlr)) {
    warn(F("Can't connect to network!"));
    return false;
  }

  info(F("Connecting to: %s:%d ..."), mqttBroker, mqttPort);
  if (client->connect(mqttBroker, mqttPort)) {
    info(F("  Done"));
  } else {
    info(F("  Failed"));
    return false;
  }

  this->thingNamespace = thingNamespace;
  this->thingName = thingName;

  // if there are registered commands
  bool hasCommands = false;
  for (const auto e : features) {
    if (!e.second.commands.empty()) {
      hasCommands = true;
      break;
    }
  }
  if (hasCommands) {
    hono->onCommand(onReceive);
  }

  std::string deviceId = std::string(thingNamespace) + ":" + std::string(thingName);
  std::string user = std::string(authId) + "@" + std::string(tenantId);
  info(F("Connecting %s (via MQTT) ..."), deviceId.c_str());
  bool success = hono->connect(
    mqttBroker, mqttPort,
    deviceId.c_str(), user.c_str(), pass);
  if (success) {
    info(F("  Done"));
    sendAttributes();
    return true;
  } else {
    info(F("  Failed"));
    return false;
  }
}

Feature& Feature::updateValues(std::vector<Property>& props, unsigned long& time) {
  for (auto& prop : props) {
    if (prop.isInitialized) {
      if (prop.minReportPeriodMS != -1 && time - prop.lastReportTime >= prop.minReportPeriodMS) {
        prop.lastReportedValue = prop.currentValue;
        prop.lastReportTime = time;
      } // else not sent, won't update
    } else {
      prop.lastReportedValue = prop.currentValue;
      prop.lastReportTime = time;
      prop.isInitialized = true;
    }
  }
  return *this;
}

long hash(String str) {
  long hash = 0;
  for (int i = str.length(); i-- > 0;) {
    hash *= 31;
    hash += str[i];
  }
  return hash;
}

long hash(JsonObject object) {
  String str((char*)0);
  serializeJson(object, str);
  return hash(str);
}

template<typename T>
long toLong(const T& t) {
    long l;
    memcpy(&l, &t, (sizeof(long) > sizeof(T) ? sizeof(T) : sizeof(long)));
    return l;
}

template<typename T>
void addProperty(Property& prop, T& current, JsonObject& properties) {
  if (!prop.isInitialized || prop.currentValue != prop.lastReportedValue) {
    properties[prop.category == Category::STATUS ? STATUS_PROPERTIES : CONFIGURATION_PROPERTIES][prop.name] = current;
  }
}

void addProperties(
    std::vector<Property>& props,
    JsonObject& properties,
    unsigned long& time) {
  for (auto& prop : props) {
    if (prop.isInitialized && (time - prop.lastReportTime < prop.minReportPeriodMS)) {
      continue; // skip, too frequently
    }
    switch (prop.tval) {
      case Type::BOOL: {
        auto current = ((bool (*)())prop.valueProvider)();
        prop.currentValue = current ? 1 : 0;
        addProperty(prop, current, properties);
        break;
      }
      case Type::LONG: {
        auto current = ((long (*)())prop.valueProvider)();
        prop.currentValue = current;
        addProperty(prop, current, properties);
        break;
      }
      case Type::UNSIGNED_LONG: {
        auto current = ((unsigned long (*)())prop.valueProvider)();
        prop.currentValue = toLong(current);
        addProperty(prop, current, properties);
        break;
      }
      case Type::FLOAT: {
        auto current = ((float (*)())prop.valueProvider)();
        prop.currentValue = toLong(current);
        addProperty(prop, current, properties);
        break;
      }
      case Type::STRING: {
        auto current = ((String (*)())prop.valueProvider)();
        prop.currentValue = hash(current);
        addProperty(prop, current, properties);
        break;
      }
      case Type::OBJECT: {
        StaticJsonDocument<256> tempDoc;
        JsonObject object = tempDoc.to<JsonObject>();
        ((void (*)(JsonObject&))prop.valueProvider)(object);
        prop.currentValue = hash(object);
        addProperty(prop, object, properties);
        break;
      }
    }
  }
}

template <typename T>
void addAttribute(const char* name, const T& value, DynamicJsonDocument& attributes) {
  info(F("addAttribute: %s"), name);
  attributes[name] = value;
}
BoschIoTAgent& BoschIoTAgent::addAttribute(const char* name, const bool& value) {
  ::addAttribute<bool>(name, value, attributes);
  return *this;
}
BoschIoTAgent& BoschIoTAgent::addAttribute(const char* name, const long& value) {
  ::addAttribute<bool>(name, value, attributes);
  return *this;
}
BoschIoTAgent& BoschIoTAgent::addAttribute(const char* name, const unsigned long& value) {
  ::addAttribute<bool>(name, value, attributes);
  return *this;
}
BoschIoTAgent& BoschIoTAgent::addAttribute(const char* name, const float& value) {
  ::addAttribute<bool>(name, value, attributes);
  return *this;
}
BoschIoTAgent& BoschIoTAgent::addAttribute(const char* name, const String& value) {
  ::addAttribute<bool>(name, value, attributes);
  return *this;
}
BoschIoTAgent& BoschIoTAgent::addAttribute(const char* name, const JsonObjectConst& value) {
  ::addAttribute<bool>(name, value, attributes);
  return *this;
}

BoschIoTAgent& BoschIoTAgent::addFeature(Feature feature){
  info(F("addFeature: %s"), feature.featureId.c_str());
  features.insert(std::map<String, Feature>::value_type(feature.featureId, feature));
  return *this;
}

void BoschIoTAgent::sendAttributes() {
  info(F("Sending attributes ..."));
  if (attributes == nullptr || attributes.size() == 0) {
    return;
  }
  DynamicJsonDocument dittoProtocolMsg(1024);
  dittoProtocolMsg[TOPIC] = String(thingNamespace) + "/" + String(thingName) + "/things/twin/commands/merge";
  JsonObject headers = dittoProtocolMsg.createNestedObject(HEADERS);
  headers[RESPONSE_REQUIRED] = false;
  headers[CONTENT_TYPE] = "application/merge-patch+json";
  dittoProtocolMsg[PATH] = "/attributes";
  dittoProtocolMsg[VALUE] = attributes;
  const bool success = hono->sendEvent(dittoProtocolMsg);
  if (success) {
    info(F("Done"));
  } else {
    info(F("Failed"));
  }
}

bool firstTelemetry = true; // if definition sent to telemetry. Send once. Note: if message is lost - would be lost
bool firstEvent = true; // if definition sent to event
void BoschIoTAgent::publish(const QoS qos) {
  DynamicJsonDocument dittoProtocolMsg(2048);
  JsonObject featuresUpdate = dittoProtocolMsg.createNestedObject(VALUE);

  unsigned long time = millis();
  buildFeaturesUpdate(qos, featuresUpdate, time);
  if (featuresUpdate.size() == 0) {
    return;
  }

  dittoProtocolMsg[TOPIC] = String(thingNamespace) + "/" + String(thingName) + "/things/twin/commands/merge";
  JsonObject headers = dittoProtocolMsg.createNestedObject(HEADERS);
  headers[RESPONSE_REQUIRED] = false;
  headers[CONTENT_TYPE] = "application/merge-patch+json";
  dittoProtocolMsg[PATH] = "/features";

  const bool success = qos == QoS::EVENT ? hono->sendEvent(dittoProtocolMsg) : hono->sendTelemetry(dittoProtocolMsg);
  if (success) {
    for (auto& feature : features) {
      feature.second.updateValues(qos == QoS::EVENT ? feature.second.eventProps : feature.second.telemetryProps, time);
    }
    if (qos == QoS::EVENT) {
      if (firstEvent) {
        firstEvent = false;
      }
    } else {
      if (firstTelemetry) {
        firstTelemetry = false;
      }
    }
  }
}

void BoschIoTAgent::buildFeaturesUpdate(const QoS qos, JsonObject& featuresUpdate, unsigned long& time) {
  for (auto& feature : features) {
    JsonObject featureJson = featuresUpdate.createNestedObject(feature.first);

    bool definitionSet = false;
    if (feature.second.definitions.size() > 0) {
      if ((qos == QoS::EVENT && firstEvent) || (qos == QoS::TELEMETRY && firstTelemetry)) {
        JsonArray definition = featureJson.createNestedArray(DEFINITION);
        for (const auto d : feature.second.definitions) {
          definition.add(d);
        }
        definitionSet = true;
      }
    }

    JsonObject properties = featureJson.createNestedObject(PROPERTIES);
    JsonObject status = properties.createNestedObject(STATUS_PROPERTIES);
    JsonObject configuration = properties.createNestedObject(CONFIGURATION_PROPERTIES);

    addProperties(qos == QoS::EVENT ? feature.second.eventProps : feature.second.telemetryProps, properties, time);

    if (status.size() == 0) {
      if (configuration.size() == 0) {
        if (definitionSet) { // definition remain
          properties.remove(STATUS_PROPERTIES);
          properties.remove(CONFIGURATION_PROPERTIES);
        } else { // definition (if it has already been send)
          featuresUpdate.remove(feature.first);
        }
      } else {
        properties.remove(STATUS_PROPERTIES);
      }
    } else if (configuration.size() == 0) {
      properties.remove(CONFIGURATION_PROPERTIES);
    }
  }
}

void BoschIoTAgent::loop() {
  // check must be called regularly to keep the connection open
  if (conHndlr->check() != NetworkConnectionState::CONNECTED) {
    return;
  }
  hono->loop();
  this->publish(QoS::TELEMETRY);
  this->publish(QoS::EVENT);
}

bool BoschIoTAgent::disconnect() {
  hono->disconnect();
  conHndlr->disconnect();
}

const String ERRORS_RESPONSE = String(F("errors-response"));
const JsonVariant VOID_RESULT;
const String DEFAULT_STR((char*)0);
const JsonObject DEFAULT_OBJECT;
void execVoid(Command command, JsonVariantConst& args);
template<typename T>
T execWithResult(Command command, JsonVariantConst& args, T defaultValue);
void execWithJOResult(Command command, JsonVariantConst& args, JsonObject& resultJO);
void BoschIoTAgent::onCommand(const String& topic, JsonDocument& dittoMessage) {
  RequestInfo requestInfo = ::requestInfo(topic);
  if (!requestInfo.valid) {
    error(F("Invalid command!"));
    return;
  }
  if (ERRORS_RESPONSE.equals(requestInfo.commandId)) {
    error(F("Received error response!"));
    return;
  }

  if (dittoMessage.size() > 0 && dittoMessage.containsKey("topic") && dittoMessage.containsKey("path")) {
    const String featureId = dittoMessage[HEADERS]["ditto-message-feature-id"];
    auto featureEntry = features.find(featureId);
    if (featureEntry == features.end()) {
      error(F("Feature %s not registered! Ignore command %s!"), featureId.c_str(), requestInfo.commandId.c_str());
      error(F("Registered features:"));
      for (const auto& feature : features) {
        error(F("  -> %s"), feature.first.c_str());
      }
      hono->sendCommandResponse(requestInfo.reqId, 404, StaticJsonDocument<0>());
      return;
    }

    Feature feature = featureEntry->second;
    if (feature.commands.find(requestInfo.commandId) == feature.commands.end()) {
      error(F("Command handler for %s.%s not registered! Ignore command!"), featureId.c_str(), requestInfo.commandId.c_str());
      error(F("Registered commands:"));
      for (const auto command : feature.commands) {
        error(F("  -> %s"), command.first.c_str());
      }
      hono->sendCommandResponse(requestInfo.reqId, 404, StaticJsonDocument<0>());
      return;
    }

    Command command = feature.commands[requestInfo.commandId];
    JsonVariantConst args = dittoMessage[VALUE];

    if (command.tres == Type::VOID) { // one way command
      execVoid(command, args);
      if (!dittoMessage[HEADERS][RESPONSE_REQUIRED]) {
        return;
      }// else request / response
    }

    const String correlationId = dittoMessage[HEADERS][CORRELATION_ID];
    if (correlationId == nullptr || correlationId.length() == 0) {
      Debug.print(DBG_WARNING, F("No correlation id for non void result command %s! Skip command!"), command.name);
      return; // no tesposne expected, if has reqId - maybe shuold log error
    }

    DynamicJsonDocument dittoMessageResponse(512);
    dittoMessageResponse[TOPIC] = dittoMessage[TOPIC];
    JsonObject headers = dittoMessageResponse.createNestedObject(HEADERS);
    headers[CONTENT_TYPE] = "application/json";
    headers[CORRELATION_ID] = correlationId;
    dittoMessageResponse[PATH] = dittoMessage[PATH];

    switch (command.tres) {
      case Type::VOID: {
        dittoMessageResponse["status"] = 200;
        break;
      }
      case Type::BOOL: {
        dittoMessageResponse[VALUE] = execWithResult<bool>(command, args, false);
        dittoMessageResponse["status"] = 200;
        break;
      }
      case Type::LONG: {
        dittoMessageResponse[VALUE] = execWithResult<long>(command, args, 0);
        dittoMessageResponse["status"] = 200;
        break;
      }
      case Type::UNSIGNED_LONG: {
        dittoMessageResponse[VALUE] = execWithResult<unsigned long>(command, args, 0);
        dittoMessageResponse["status"] = 200;
        break;
      }
      case Type::FLOAT: {
        dittoMessageResponse[VALUE] = execWithResult<float>(command, args, 0.0);
        dittoMessageResponse["status"] = 200;
        break;
      }
      case Type::STRING: {
        String res = execWithResult<String>(command, args, DEFAULT_STR);
        dittoMessageResponse[VALUE] = res;
        dittoMessageResponse["status"] = res.length() == 0 ? 204 : 200;
        break;
      }
      case Type::OBJECT: {
        JsonObject result = dittoMessageResponse.createNestedObject(VALUE);
        execWithJOResult(command, args, result);
        dittoMessageResponse["status"] = 200;
        break;
      }
      default: {
        error(F("Unsupported command types (%d -> %d)!"), command.targ, command.tres);
        return;
      }
    }

    hono->sendCommandResponse(requestInfo.reqId, 200, dittoMessageResponse);
  } else {
    error(F("Badly formated Ditto message!"));
  }
}

// connection handling
void onNetworkConnect() {
  info("Network: Connected");
}
void onNetworkDisconnect() {
  info("Network: disconnected");
}
void onNetworkError() {
  info("Network: Error");
}
bool connect(ConnectionHandler& conHndlr) {
  conHndlr.addCallback(NetworkConnectionEvent::CONNECTED, onNetworkConnect);
  conHndlr.addCallback(NetworkConnectionEvent::DISCONNECTED, onNetworkDisconnect);
  conHndlr.addCallback(NetworkConnectionEvent::ERROR, onNetworkError);
  info("Network: Connecting ... ");

  for (NetworkConnectionState status; (status = conHndlr.check()) != NetworkConnectionState::CONNECTED;) {
    const char* toString;
    bool error = false;
    switch (status) {
      case NetworkConnectionState::INIT :
        toString = "Init";
        break;
      case NetworkConnectionState::CONNECTING :
        toString = "Connecting";
        break;
      case NetworkConnectionState::CONNECTED :
        toString = "Connected";
        break;
      case NetworkConnectionState::DISCONNECTING :
        toString = "Disconnecting";
        error = true;
        break;
      case NetworkConnectionState::DISCONNECTED :
        toString = "Disconnected";
        error = true;
        break;
      case NetworkConnectionState::CLOSED :
      error = true;
        toString = "Closed";
        break;
      case NetworkConnectionState::ERROR :
        toString = "Error";
        error = true;
        break;
      default:
        toString = "Unknown";
        error = true;
    }
    info(F("Network: Status = %s"), toString);
    if (error) return false;

    delay(1000);
  }

  return true;
}

// support for multi types properties and hasCommand
template<typename T>
void Feature::addProperty(
    const char* name, const Category category, const QoS qos, const Type tval, T (*valueProvider)(), unsigned long minReportPeriodMS) {
  Property property(String(name), category, tval, (void*)valueProvider, minReportPeriodMS);
  (qos == QoS::EVENT ? eventProps : telemetryProps).push_back(property);
}
Feature& Feature::addProperty(const char* name, const Category category, const QoS qos, bool (*valueProvider)(), unsigned long minReportPeriodMS) {
  addProperty(name, category, qos, Type::BOOL, valueProvider, minReportPeriodMS);
  return *this;
}
Feature& Feature::addProperty(const char* name, const Category category, const QoS qos, long (*valueProvider)(), unsigned long minReportPeriodMS) {
  addProperty(name, category, qos, Type::LONG, valueProvider, minReportPeriodMS);
  return *this;
}
Feature& Feature::addProperty(const char* name, const Category category,const  QoS qos, unsigned long (*valueProvider)(), unsigned long minReportPeriodMS) {
  addProperty(name, category, qos, Type::UNSIGNED_LONG, valueProvider, minReportPeriodMS);
  return *this;
}
Feature& Feature::addProperty(const char* name, const Category category, const QoS qos, float (*valueProvider)(), unsigned long minReportPeriodMS) {
  addProperty(name, category, qos, Type::FLOAT, valueProvider, minReportPeriodMS);
  return *this;
}
Feature& Feature::addProperty(const char* name, const Category category, const QoS qos, String (*valueProvider)(), unsigned long minReportPeriodMS) {
  addProperty(name, category, qos, Type::STRING, valueProvider, minReportPeriodMS);
  return *this;
}
Feature& Feature::addProperty(const char* name, const Category category, const QoS qos, void (*valueProvider)(JsonObject&), unsigned long minReportPeriodMS) {
  Property property(String(name), category, Type::OBJECT, (void*)valueProvider, minReportPeriodMS);
  (qos == QoS::EVENT ? eventProps : telemetryProps).push_back(property);
  return *this;
}

Feature& Feature::addCommand(const char* name, void (*commandHandler)()) {
  commands[String(name)] = Command(name, Type::VOID, Type::VOID, (void*)commandHandler);
  return *this;
}
Feature& Feature::addCommand(const char* name, void (*commandHandler)(bool)) {
  commands[String(name)] = Command(name, Type::BOOL, Type::VOID, (void*)commandHandler);
  return *this;
}
Feature& Feature::addCommand(const char* name, void (*commandHandler)(long)) {
  commands[String(name)] = Command(name, Type::LONG, Type::VOID, (void*)commandHandler);
  return *this;
}
Feature& Feature::addCommand(const char* name, void (*commandHandler)(unsigned long)) {
  commands[String(name)] = Command(name, Type::UNSIGNED_LONG, Type::VOID, (void*)commandHandler);
  return *this;
}
Feature& Feature::addCommand(const char* name, void (*commandHandler)(float)) {
  commands[String(name)] = Command(name, Type::FLOAT, Type::VOID, (void*)commandHandler);
  return *this;
}
Feature& Feature::addCommand(const char* name, void (*commandHandler)(String)) {
  commands[String(name)] = Command(name, Type::STRING, Type::VOID, (void*)commandHandler);
  return *this;
}
Feature& Feature::addCommand(const char* name, void (*commandHandler)(JsonObjectConst&)) {
  commands[String(name)] = Command(name, Type::OBJECT, Type::VOID, (void*)commandHandler);
  return *this;
}

Feature& Feature::addCommand(const char* name, bool (*commandHandler)()) {
  commands[String(name)] = Command(name, Type::VOID, Type::BOOL, (void*)commandHandler);
  return *this;
}
Feature& Feature::addCommand(const char* name, bool (*commandHandler)(bool)) {
  commands[String(name)] = Command(name, Type::BOOL, Type::BOOL, (void*)commandHandler);
  return *this;
}
Feature& Feature::addCommand(const char* name, bool (*commandHandler)(long)) {
  commands[String(name)] = Command(name, Type::LONG, Type::BOOL, (void*)commandHandler);
  return *this;
}
Feature& Feature::addCommand(const char* name, bool (*commandHandler)(unsigned long)) {
  commands[String(name)] = Command(name, Type::UNSIGNED_LONG, Type::BOOL, (void*)commandHandler);
  return *this;
}
Feature& Feature::addCommand(const char* name, bool (*commandHandler)(float)) {
  commands[String(name)] = Command(name, Type::FLOAT, Type::BOOL, (void*)commandHandler);
  return *this;
}
Feature& Feature::addCommand(const char* name, bool (*commandHandler)(String)) {
  commands[String(name)] = Command(name, Type::STRING, Type::BOOL, (void*)commandHandler);
  return *this;
}
Feature& Feature::addCommand(const char* name, bool (*commandHandler)(JsonObjectConst&)) {
  commands[String(name)] = Command(name, Type::OBJECT, Type::BOOL, (void*)commandHandler);
  return *this;
}

Feature& Feature::addCommand(const char* name, long (*commandHandler)()) {
  commands[String(name)] = Command(name, Type::VOID, Type::LONG, (void*)commandHandler);
  return *this;
}
Feature& Feature::addCommand(const char* name, long (*commandHandler)(bool)) {
  commands[String(name)] = Command(name, Type::BOOL, Type::LONG, (void*)commandHandler);
  return *this;
}
Feature& Feature::addCommand(const char* name, long (*commandHandler)(long)) {
  commands[String(name)] = Command(name, Type::LONG, Type::LONG, (void*)commandHandler);
  return *this;
}
Feature& Feature::addCommand(const char* name, long (*commandHandler)(unsigned long)) {
  commands[String(name)] = Command(name, Type::UNSIGNED_LONG, Type::LONG, (void*)commandHandler);
  return *this;
}
Feature& Feature::addCommand(const char* name, long (*commandHandler)(float)) {
  commands[String(name)] = Command(name, Type::FLOAT, Type::LONG, (void*)commandHandler);
  return *this;
}
Feature& Feature::addCommand(const char* name, long (*commandHandler)(String)) {
  commands[String(name)] = Command(name, Type::STRING, Type::LONG, (void*)commandHandler);
  return *this;
}
Feature& Feature::addCommand(const char* name, long (*commandHandler)(JsonObjectConst&)) {
  commands[String(name)] = Command(name, Type::OBJECT, Type::LONG, (void*)commandHandler);
  return *this;
}

Feature& Feature::addCommand(const char* name, unsigned long (*commandHandler)()) {
  commands[String(name)] = Command(name, Type::VOID, Type::UNSIGNED_LONG, (void*)commandHandler);
  return *this;
}
Feature& Feature::addCommand(const char* name, unsigned long (*commandHandler)(bool)) {
  commands[String(name)] = Command(name, Type::BOOL, Type::UNSIGNED_LONG, (void*)commandHandler);
  return *this;
}
Feature& Feature::addCommand(const char* name, unsigned long (*commandHandler)(long)) {
  commands[String(name)] = Command(name, Type::LONG, Type::UNSIGNED_LONG, (void*)commandHandler);
  return *this;
}
Feature& Feature::addCommand(const char* name, unsigned long (*commandHandler)(unsigned long)) {
  commands[String(name)] = Command(name, Type::UNSIGNED_LONG, Type::UNSIGNED_LONG, (void*)commandHandler);
  return *this;
}
Feature& Feature::addCommand(const char* name, unsigned long (*commandHandler)(float)) {
  commands[String(name)] = Command(name, Type::FLOAT, Type::UNSIGNED_LONG, (void*)commandHandler);
  return *this;
}
Feature& Feature::addCommand(const char* name, unsigned long (*commandHandler)(String)) {
  commands[String(name)] = Command(name, Type::STRING, Type::UNSIGNED_LONG, (void*)commandHandler);
  return *this;
}
Feature& Feature::addCommand(const char* name, unsigned long (*commandHandler)(JsonObjectConst&)) {
  commands[String(name)] = Command(name, Type::OBJECT, Type::UNSIGNED_LONG, (void*)commandHandler);
  return *this;
}

Feature& Feature::addCommand(const char* name, float (*commandHandler)()) {
  commands[String(name)] = Command(name, Type::VOID, Type::FLOAT, (void*)commandHandler);
  return *this;
}
Feature& Feature::addCommand(const char* name, float (*commandHandler)(bool)) {
  commands[String(name)] = Command(name, Type::BOOL, Type::FLOAT, (void*)commandHandler);
  return *this;
}
Feature& Feature::addCommand(const char* name, float (*commandHandler)(long)) {
  commands[String(name)] = Command(name, Type::LONG, Type::FLOAT, (void*)commandHandler);
  return *this;
}
Feature& Feature::addCommand(const char* name, float (*commandHandler)(unsigned long)) {
  commands[String(name)] = Command(name, Type::UNSIGNED_LONG, Type::FLOAT, (void*)commandHandler);
  return *this;
}
Feature& Feature::addCommand(const char* name, float (*commandHandler)(float)) {
  commands[String(name)] = Command(name, Type::FLOAT, Type::FLOAT, (void*)commandHandler);
  return *this;
}
Feature& Feature::addCommand(const char* name, float (*commandHandler)(String)) {
  commands[String(name)] = Command(name, Type::STRING, Type::FLOAT, (void*)commandHandler);
  return *this;
}
Feature& Feature::addCommand(const char* name, float (*commandHandler)(JsonObjectConst&)) {
  commands[String(name)] = Command(name, Type::OBJECT, Type::FLOAT, (void*)commandHandler);
  return *this;
}

Feature& Feature::addCommand(const char* name, String (*commandHandler)()) {
  commands[String(name)] = Command(name, Type::VOID, Type::STRING, (void*)commandHandler);
  return *this;
}
Feature& Feature::addCommand(const char* name, String (*commandHandler)(bool)) {
  commands[String(name)] = Command(name, Type::BOOL, Type::STRING, (void*)commandHandler);
  return *this;
}
Feature& Feature::addCommand(const char* name, String (*commandHandler)(long)) {
  commands[String(name)] = Command(name, Type::LONG, Type::STRING, (void*)commandHandler);
  return *this;
}
Feature& Feature::addCommand(const char* name, String (*commandHandler)(unsigned long)) {
  commands[String(name)] = Command(name, Type::UNSIGNED_LONG, Type::STRING, (void*)commandHandler);
  return *this;
}
Feature& Feature::addCommand(const char* name, String (*commandHandler)(float)) {
  commands[String(name)] = Command(name, Type::FLOAT, Type::STRING, (void*)commandHandler);
  return *this;
}
Feature& Feature::addCommand(const char* name, String (*commandHandler)(String)) {
  commands[String(name)] = Command(name, Type::STRING, Type::STRING, (void*)commandHandler);
  return *this;
}
Feature& Feature::addCommand(const char* name, String (*commandHandler)(JsonObjectConst&)) {
  commands[String(name)] = Command(name, Type::OBJECT, Type::STRING, (void*)commandHandler);
  return *this;
}

Feature& Feature::addCommand(const char* name, void (*commandHandler)(JsonObject&)) {
  commands[String(name)] = Command(name, Type::VOID, Type::OBJECT, (void*)commandHandler);
  return *this;
}
Feature& Feature::addCommand(const char* name, void (*commandHandler)(bool, JsonObject&)) {
  commands[String(name)] = Command(name, Type::BOOL, Type::OBJECT, (void*)commandHandler);
  return *this;
}
Feature& Feature::addCommand(const char* name, void (*commandHandler)(long, JsonObject&)) {
  commands[String(name)] = Command(name, Type::LONG, Type::OBJECT, (void*)commandHandler);
  return *this;
}
Feature& Feature::addCommand(const char* name, void (*commandHandler)(unsigned long, JsonObject&)) {
  commands[String(name)] = Command(name, Type::UNSIGNED_LONG, Type::OBJECT, (void*)commandHandler);
  return *this;
}
Feature& Feature::addCommand(const char* name, void (*commandHandler)(float, JsonObject&)) {
  commands[String(name)] = Command(name, Type::FLOAT, Type::OBJECT, (void*)commandHandler);
  return *this;
}
Feature& Feature::addCommand(const char* name, void (*commandHandler)(String, JsonObject&)) {
  commands[String(name)] = Command(name, Type::STRING, Type::OBJECT, (void*)commandHandler);
  return *this;
}
Feature& Feature::addCommand(const char* name, void (*commandHandler)(JsonObjectConst&, JsonObject&)) {
  commands[String(name)] = Command(name, Type::OBJECT, Type::OBJECT, (void*)commandHandler);
  return *this;
}

// TODO - add type check if (!args.is<int>) ... warn and return default
void execVoid(Command command, JsonVariantConst& args) {
  switch (command.targ) {
    case Type::VOID: {
      ((void (*) ())command.handler)();
      return;
    }
    case Type::BOOL: {
      ((void (*) (bool))command.handler)(args.as<bool>());
      return;
    }
    case Type::LONG: {
      ((void (*) (long))command.handler)(args.as<long>());
      return;
    }
    case Type::UNSIGNED_LONG: {
      ((void (*) (unsigned long))command.handler)(args.as<unsigned long>());
      return;
    }
    case Type::FLOAT: {
      ((void (*) (float))command.handler)(args.as<float>());
      return;
    }
    case Type::STRING: {
      ((void (*) (String))command.handler)(args.as<String>());
      return;
    }
    case Type::OBJECT: {
      JsonObjectConst object = args.as<JsonObjectConst>();
      ((void (*) (JsonObjectConst&))command.handler)(object);
      return;
    }
  }
}

// TODO - add type check if (!args.is<int>) ... warn and return default
template<typename T>
T execWithResult(Command command, JsonVariantConst& args, T defaultValue) {
  switch (command.targ) {
    case Type::VOID: {
      return ((T (*) ())command.handler)();
    }
    case Type::BOOL: {
      return ((T (*) (bool))command.handler)(args.as<bool>());
    }
    case Type::LONG: {
      return ((T (*) (long))command.handler)(args.as<long>());
    }
    case Type::UNSIGNED_LONG: {
      return ((T (*) (unsigned long))command.handler)(args.as<unsigned long>());
    }
    case Type::FLOAT: {
      return ((T (*) (float))command.handler)(args.as<float>());
    }
    case Type::STRING: {
      return ((T (*) (String))command.handler)(args.as<String>());
    }
    case Type::OBJECT: {
      JsonObjectConst object = args.as<JsonObjectConst>();
      return ((T (*) (JsonObjectConst&))command.handler)(object);
    }
    default: {
      error("Unsupported argument type: %d! Return default!", command.tres);
      return defaultValue;
    }
  }
}

void execWithJOResult(Command command, JsonVariantConst& args, JsonObject& resultJO) {
  switch (command.targ) {
    case Type::VOID: {
      ((void (*) (JsonObject&))command.handler)(resultJO);
      return;
    }
    case Type::BOOL: {
      ((void (*) (bool, JsonObject&))command.handler)(args.as<bool>(), resultJO);
      return;
    }
    case Type::LONG: {
      ((void (*) (long, JsonObject&))command.handler)(args.as<long>(), resultJO);
      return;
    }
    case Type::UNSIGNED_LONG: {
      ((void (*) (unsigned long, JsonObject&))command.handler)(args.as<unsigned long>(), resultJO);
      return;
    }
    case Type::FLOAT: {
      ((void (*) (float, JsonObject&))command.handler)(args.as<float>(), resultJO);
      return;
    }
    case Type::STRING: {
      ((void (*) (String, JsonObject&))command.handler)(args.as<String>(), resultJO);
      return;
    }
    case Type::OBJECT: {
      JsonObjectConst object = args.as<JsonObjectConst>();
      ((void (*) (JsonObjectConst&, JsonObject&))command.handler)(object, resultJO);
      return;
    }
  }
}
