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
#include "features_opla.h"

MKRIoTCarrier* __carrier;

float tempOpla();

float humidityOpla();

float pressureOpla();

long lightLevelOpla();
void lightOpla(JsonObject& out);
long proximityOpla();

bool buttonOpla(long index);
bool buttonDownOpla(long index);
bool buttonUpOpla(long index);
bool buttonChangeOpla(long index);
void buttonStateOpla(JsonDocument& out);

void buzzerSoundOpla(long freq);
void buzzerMuteOpla();

void setLEDColorOpla(uint32_t color);
void setLEDColorOplaJson(JsonObjectConst& params);
void fillLEDsOpla(JsonObjectConst& params);
void setLEDBrightnessOpla(long brightness);
void clearLEDsOpla();

void acceleration(JsonObject& out);
float accelerationXOpla();
float accelerationYOpla();
float accelerationZOpla();

void LCDPrintOpla(String s);
void setLCDCursorOpla(JsonObjectConst& params);
void setLCDColorOpla(uint32_t color);
void setLCDColorOplaJson(JsonObjectConst& params);
void setLCDSizeOpla(long sz);

void setCarrier(MKRIoTCarrier* carrier) {
  __carrier = carrier;
}

float temperatureOpla() {
  return __carrier->Env.readTemperature();
}

float humidityOpla() {
  return __carrier->Env.readHumidity();
}

float pressureOpla() {
  return __carrier->Pressure.readPressure();
}

long lightLevelOpla() {
  int light;
  int none;
  __carrier->Light.readColor(none, none, none, light);
  return light;
}

void lightOpla(JsonObject& out) {
  int light, r, g, b;
  __carrier->Light.readColor(r, g, b, light);
  out["r"] = r;
  out["g"] = g;
  out["b"] = b;
}

long proximityOpla() {
  return __carrier->Light.readProximity();
}

bool buttonOpla(int index) {
  __carrier->Buttons.update();
  switch (index) {
    case 0:
      return __carrier->Buttons.getTouch(TOUCH0);
    case 1:
      return __carrier->Buttons.getTouch(TOUCH1);
    case 2:
      return __carrier->Buttons.getTouch(TOUCH2);
    case 3:
      return __carrier->Buttons.getTouch(TOUCH3);
    case 4:
      return __carrier->Buttons.getTouch(TOUCH4);

  }
}

bool buttonUpOpla(long index) {
  __carrier->Buttons.update();
  switch (index) {
    case 0:
      return __carrier->Buttons.onTouchUp(TOUCH0);
    case 1:
      return __carrier->Buttons.onTouchUp(TOUCH1);
    case 2:
      return __carrier->Buttons.onTouchUp(TOUCH2);
    case 3:
      return __carrier->Buttons.onTouchUp(TOUCH3);
    case 4:
      return __carrier->Buttons.onTouchUp(TOUCH4);

  }
}

bool buttonDownOpla(long index) {
  __carrier->Buttons.update();
  switch (index) {
    case 0:
      return __carrier->Buttons.onTouchDown(TOUCH0);
    case 1:
      return __carrier->Buttons.onTouchDown(TOUCH1);
    case 2:
      return __carrier->Buttons.onTouchDown(TOUCH2);
    case 3:
      return __carrier->Buttons.onTouchDown(TOUCH3);
    case 4:
      return __carrier->Buttons.onTouchDown(TOUCH4);

  }
}

bool buttonChangeOpla(long index) {
  __carrier->Buttons.update();
  switch (index) {
    case 0:
      return __carrier->Buttons.onTouchChange(TOUCH0);
    case 1:
      return __carrier->Buttons.onTouchChange(TOUCH1);
    case 2:
      return __carrier->Buttons.onTouchChange(TOUCH2);
    case 3:
      return __carrier->Buttons.onTouchChange(TOUCH3);
    case 4:
      return __carrier->Buttons.onTouchChange(TOUCH4);

  }
}

void buttonStateOpla(JsonObject& out) {
  out["0"] = buttonOpla(0);
  out["1"] = buttonOpla(1);
  out["2"] = buttonOpla(2);
  out["3"] = buttonOpla(3);
  out["4"] = buttonOpla(4);
}

void buzzerSoundOpla(long freq) {
  __carrier->Buzzer.sound(freq);
}
void buzzerMuteOpla() {
  __carrier->Buzzer.noSound();
}

void setLEDColorOpla(uint32_t color) {
  __carrier->leds.fill(color, 0, 5);
  __carrier->leds.show();
}
void setLEDColorOplaJson(JsonObjectConst& params) {
  uint32_t color = __carrier->leds.Color(params["r"], params["g"], params["b"]);
  setLEDColorOpla(color);
}
void fillLEDsOpla(JsonObjectConst& params) {
  uint32_t color = __carrier->leds.Color(params["r"], params["g"], params["b"]);
  __carrier->leds.fill(color, params["first"], params["count"]);
  __carrier->leds.show();
}
void setLEDBrightnessOpla(long brightness) {
  __carrier->leds.setBrightness(brightness);
  __carrier->leds.show();
}

void clearLEDsOpla() {
  __carrier->leds.clear();
  __carrier->leds.show();
}

void acceleration(JsonObject& out) {
  float aX,aY,aZ;
  __carrier->IMUmodule.readAcceleration(aX, aY, aZ);
  out["x"] = aX;
  out["y"] = aY;
  out["z"] = aZ;
}

float accelerationXOpla() {
  float aX, none;
  __carrier->IMUmodule.readAcceleration(aX, none, none);
  return aX;
}

float accelerationYOpla() {
  float aY, none;
  __carrier->IMUmodule.readAcceleration(none, aY, none);
  return aY;
}

float accelerationZOpla() {
  float aZ, none;
  __carrier->IMUmodule.readAcceleration(none, none, aZ);
  return aZ;
}

void LCDPrintOpla(String s) {
  info("Text to output: %S", s);
  __carrier->display.print(s);
}

void setLCDCursorOpla(JsonObjectConst& params) {
  __carrier->display.setCursor(params["x"], params["y"]);
}

void setLCDColorOpla(uint32_t color) {
  __carrier->display.setTextColor(color);
}
void setLCDColorOplaJson(JsonObjectConst& params) {
  uint32_t color = __carrier->leds.Color(params["r"], params["g"], params["b"]);
    __carrier->display.setTextColor(color);
}
void setLCDSizeOpla(long sz) {
  __carrier->display.setTextSize(sz);
}

Feature temperatureFeatureOpla() {
  return Feature("temperature", std::vector<String>{})
    .addProperty("value", Category::STATUS, QoS::EVENT, temperatureOpla, 10000);
}

Feature humidityFeatureOpla() {
  return Feature("humidity", std::vector<String>{})
    .addProperty("value", Category::STATUS, QoS::EVENT, humidityOpla, 10000);
}

Feature pressureFeatureOpla() {
  return Feature("pressure", std::vector<String>{})
    .addProperty("value", Category::STATUS, QoS::EVENT, pressureOpla, 10000);
}

Feature lightFeatureOpla() {
  return Feature("light", std::vector<String>{})
    .addProperty("brightness", Category::STATUS, QoS::EVENT, lightLevelOpla, 1000)
    .addProperty("color", Category::STATUS, QoS::EVENT, lightOpla, 1000)
    .addProperty("proximity", Category::STATUS, QoS::EVENT, proximityOpla, 1000);
}

Feature buttonsFeatureOpla() {
  return Feature("buttons", std::vector<String>{})
    .addProperty("state", Category::STATUS, QoS::EVENT, buttonStateOpla, 10000);
}

Feature buzzerFeatureOpla() {
  return Feature("buzzer", std::vector<String>{})
    .addCommand("sound", buzzerSoundOpla)
    .addCommand("mute", buzzerMuteOpla);
}

Feature ledsFeatureOpla() {
  return Feature("neopixels", std::vector<String>{})
    .addCommand("setColor", setLEDColorOplaJson)
    .addCommand("fillColor", fillLEDsOpla)
    .addCommand("setBrightness", setLEDBrightnessOpla)
    .addCommand("clear", clearLEDsOpla);
}
Feature accelerationFeatureOpla() {
  return Feature("acceleration", std::vector<String>{})
    .addProperty("x", Category::STATUS, QoS::EVENT, accelerationXOpla)
    .addProperty("y", Category::STATUS, QoS::EVENT, accelerationYOpla)
    .addProperty("z", Category::STATUS, QoS::EVENT, accelerationZOpla);
}

Feature LCDDisplayFeatureOpla() {
  return Feature("display", std::vector<String>{})
    .addCommand("print", LCDPrintOpla)
    .addCommand("setCursor", setLCDCursorOpla)
    .addCommand("setColor", setLCDColorOplaJson)
    .addCommand("setSize", setLCDSizeOpla);
}
