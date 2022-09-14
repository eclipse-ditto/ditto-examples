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
#include "feature_test.h"

void echoVoid() {
  info(F("echoVoid()"));
}

bool echoBool(bool arg) {
  info(F("echoBool(%d)"), arg);
  return arg;
}

long echoLong(long arg) {
  info(F("echoLong(%d)"), arg);
  return arg;
}

unsigned long echoUnsignedLong(unsigned long arg) {
  info(F("echoUnsignedLong(%d)"), arg);
  return arg;
}

float echoFloat(float arg) {
  info(F("echoFloat(%d)"), arg);
  return arg;
}

String echoString(String arg) {
  info(F("echoString(%s)"), arg.c_str());
  return arg;
}

void echoObject(JsonObjectConst& object, JsonObject& resultJO) {
  String str((char*)0);
  serializeJsonPretty(object, str);
  info(F("echoObject(%s)"), str.c_str());
  resultJO.set(object);
}

bool bProp() {
  return random(0, 100) % 3 == 0;
}

long lProp() {
  return random(-100, 100);
}

unsigned long ulProp() {
  return random(0, 100);
}

float fProp() {
  return random(0, 100) / 3.0;
}

String sProp() {
  return String("str-") + String((random(0, 100) % 3));
}

void joProp(JsonObject& obj) {
  obj["const"] = "const";
  obj["rnd"] = random(0, 100) % 3;
}

Feature testFeature() {
  return Feature("test", std::vector<String>{})
    .addProperty("boolProp", Category::STATUS, QoS::EVENT, bProp)
    .addProperty("longProp", Category::STATUS, QoS::EVENT, lProp, 10000)
    .addProperty("unsignedLongProp", Category::STATUS, QoS::EVENT, ulProp, 10000)
    .addProperty("floatProp", Category::STATUS, QoS::EVENT, fProp, 10000)
    .addProperty("stringProp", Category::STATUS, QoS::EVENT, sProp, 10000)
    .addProperty("jsonObjectProp", Category::STATUS, QoS::EVENT, joProp, 10000)
    .addCommand("echoVoid", echoVoid)
    .addCommand("echoBool", echoBool)
    .addCommand("echoLong", echoLong)
    .addCommand("echoUnsignedLong", echoUnsignedLong)
    .addCommand("echoFloat", echoFloat)
    .addCommand("echoString", echoString)
    .addCommand("echoObject", echoObject);
}
