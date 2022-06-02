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

long getBrightness();
long numPixels();
void fill(JsonObjectConst& params);
void setPixelColor(JsonObjectConst& params);
void setBrightness(long brightness);
void clear();
void rainbow(JsonObjectConst& params);
float temperature();
float humidity();
float pressure();
long intensity();
void color(JsonObject& out);
long proximity();
String gesture();
void setGestureSensitivity(long sensitivity);
bool setLEDBoost(long boost_mode);
void buttonState(JsonObject& out);
void sound(long freq);
void noSound();
void beep(JsonObjectConst&);
void acceleration(JsonObject& out);
void reset();
void print(String s);
void fillScreen(JsonObjectConst& params);
void setRotation(long value);
void setCursor(JsonObjectConst& params);
void setTextColor(JsonObjectConst& params);
void setTextSize(long size);
long getOrDefault(JsonObjectConst& params, char* name, long defaultValue);

void setCarrier(MKRIoTCarrier* carrier) {
  __carrier = carrier;
}

/* LEDS - 5 digital RGB LEDs */
Feature ledsFeature() {
  return Feature("leds", std::vector<String> {"org.eclipse.ditto.examples.arduino.mkr.carrier:LEDs:1.0.0"})
    .addProperty("brightness", Category::STATUS, QoS::EVENT, getBrightness, 1000)
    .addProperty("numPixels", Category::STATUS, QoS::EVENT, numPixels, -1)
    .addCommand("fill", fill)
    .addCommand("setPixelColor", setPixelColor)
    .addCommand("setBrightness", setBrightness)
    .addCommand("clear", clear);
}

long getBrightness() {
  return __carrier->leds.getBrightness();
}

long numPixels() {
  return __carrier->leds.numPixels();
}

void fill(JsonObjectConst& params) {
  uint32_t color = __carrier->leds.Color(params["color"]["g"], params["color"]["r"], params["color"]["b"]);
  long first = getOrDefault(params, "first", 0);
  long count = getOrDefault(params, "count", numPixels());

  __carrier->leds.fill(color, first, count);
  __carrier->leds.show();
}

void setPixelColor(JsonObjectConst& params) {
  __carrier->leds.setPixelColor(params["index"], params["color"]["g"], params["color"]["r"], params["color"]["b"]);
  __carrier->leds.show();
}

void setBrightness(long brightness) {
  __carrier->leds.setBrightness(brightness);
  __carrier->leds.show();
}

void clear() {
  __carrier->leds.clear();
  __carrier->leds.show();
}

void rainbow(JsonObjectConst& params) {
  uint16_t first_hue = getOrDefault(params, "hue", 0);
  int8_t repetitions = getOrDefault(params, "repetitions", 1);
  uint8_t saturation = getOrDefault(params, "saturation", 255);
  uint8_t brightness = getOrDefault(params, "brightness", 255);
  boolean gammify = getOrDefault(params, "gammify", true);
  __carrier->leds.rainbow(first_hue, repetitions, saturation, brightness, gammify);
}

/* Temperature Sensor
  - temperature sensing range of -40 to 120° C, with an accuracy of ± 0.5 °C,15 to +40 °C
  - temperature value in Celsius
*/
Feature temperatureFeature() {
  return Feature("temperature", std::vector<String> {"org.eclipse.vorto.std.sensor:TemperatureSensor:1.0.0"})
    .addProperty("value", Category::STATUS, QoS::EVENT, temperature, 10000);
}

float temperature() {
  return __carrier->Env.readTemperature();
}

/* Humidity Sensor
  - humidity sensing range of 0-100% and accuracy of ± 3.5% rH (20 to +80% rH)
  - relative humidity (rH) in percentage.
*/
Feature humidityFeature() {
  return Feature("humidity", std::vector<String> {"org.eclipse.vorto.std.sensor:HumiditySensor:1.0.0"})
    .addProperty("value", Category::STATUS, QoS::EVENT, humidity, 10000);
}

float humidity() {
  return __carrier->Env.readHumidity();
}

/* Pressure Sensor
  - the sensor measures absolute pressure range of 260 to 1260 hPa (0.25 to 1.24 atm)
  - pressure value in Kilopascal (kPa).
*/
Feature pressureFeature() {
  return Feature("pressure", std::vector<String> {"org.eclipse.vorto.std.sensor:PressureSensor:1.0.0"})
    .addProperty("value", Category::STATUS, QoS::EVENT, pressure, 10000);
}

float pressure() {
  return __carrier->Pressure.readPressure() * 1000;
}

/* RGB and Gesture Sensor
  - ambient light and RGB color sensing, proximity sensing, and gesture detection
  - the detected proximity that may range from 0 to 255 where 0 is the closest and 255 is the farthest
  - detects gestures - UP, DOWN, RIGHT, LEFT or NONE
*/
Feature lightFeature() {
  return Feature("light", std::vector<String> {"org.eclipse.ditto.examples.arduino.mkr.carrier:Light:1.0.0", "org.eclipse.ditto.examples.arduino:Gesture:1.0.0"})
    .addProperty("intensity", Category::STATUS, QoS::EVENT, intensity, 1000)
    .addProperty("color", Category::STATUS, QoS::EVENT, color, 1000)
    .addProperty("proximity", Category::STATUS, QoS::EVENT, proximity, 1000)
    // .addProperty("gesture", Category::STATUS, QoS::EVENT, gesture, 1000)
    // .addCommand("setGestureSensitivity", setGestureSensitivity)
    .addCommand("setLEDBoost", setLEDBoost);
}

long intensity() {
  int b;
  int none;
  __carrier->Light.readColor(none, none, none, b);
  return b;
}

void color(JsonObject& out) {
  int r, g, b, none;
  __carrier->Light.readColor(r, g, b, none);
  out["r"] = r;
  out["g"] = g;
  out["b"] = b;
}

long proximity() {
  return __carrier->Light.readProximity();
}

String gesture() {
  if (__carrier->Light.gestureAvailable()) {
    uint8_t gesture = __carrier->Light.readGesture();
    switch (gesture) {
      case GESTURE_UP: return String("UP");
      case GESTURE_DOWN: return String("DOWN");
      case GESTURE_LEFT: return String("LEFT");
      case GESTURE_RIGHT: return String("RIGHT");
      default: return String("NONE");
    }
  }
  return String("NONE");
}

//The desired gesture sensitivity a value between 1 and 100 is required
void setGestureSensitivity(long sensitivity) {
  __carrier->Light.setGestureSensitivity(sensitivity);
}

bool setLEDBoost(long boost_mode) {
  return __carrier->Light.setLEDBoost(boost_mode);
}

/* Buttons - touchable pads */
Feature buttonsFeature() {
  return Feature("buttons", std::vector<String> {})
    .addProperty("state", Category::STATUS, QoS::EVENT, buttonState, 1000);
}

void buttonState(JsonObject& out) {
  out["0"] =  __carrier->Buttons.getTouch(TOUCH0);
  out["1"] =  __carrier->Buttons.getTouch(TOUCH1);
  out["2"] =  __carrier->Buttons.getTouch(TOUCH2);
  out["3"] =  __carrier->Buttons.getTouch(TOUCH3);
  out["4"] =  __carrier->Buttons.getTouch(TOUCH4);
}

/* Buzzer - makes the tone with the selected frequency */
Feature buzzerFeature() {
  return Feature("buzzer", std::vector<String> {"org.eclipse.ditto.examples.arduino.mkr.carrier:Buzzer:1.0.0"})
    .addCommand("sound", sound)
    .addCommand("noSound", noSound)
    .addCommand("beep", beep);
}

void sound(long freq) {
  __carrier->Buzzer.sound(freq);
}

void noSound() {
  __carrier->Buzzer.noSound();
}

void beep(JsonObjectConst& params) {
  //TODO replace with __carrier->Buzzer.beep(); when the new version is available
  __carrier->Buzzer.sound(getOrDefault(params, "frequency", 800));
  delay(getOrDefault(params, "duration", 20));
  __carrier->Buzzer.noSound();
}

/* Accelerometer & Gyroscope Sensors
  - 3D digital accelerometer and a 3D digital gyroscope
  - acceleration data on the three axis (x, y & z)
*/
Feature accelerationFeature() {
  return Feature("acceleration", std::vector<String> {"org.eclipse.vorto.std.sensor.mkr.carrier:AccelerationSensor3D:1.0.0"})
    .addProperty("value", Category::STATUS, QoS::EVENT, acceleration);
}

float aX = 0, aY = 0, aZ = 0;
void acceleration(JsonObject& out) {
  if (__carrier->IMUmodule.accelerationAvailable()) {
    __carrier->IMUmodule.readAcceleration(aX, aY, aZ);
  }
  out["x"] = aX;
  out["y"] = aY;
  out["z"] = aZ;
}

/* Display -  rounded 1.3” TFT display, with a 240 x 240 resolution and a diameter of 36 x 40 mm */
Feature displayFeature() {
  return Feature("display", std::vector<String> {"org.eclipse.ditto.examples.arduino.mkr.carrier:ColoredDisplay:1.0.0"})
    .addCommand("reset", reset)
    .addCommand("print", print)
    .addCommand("fillScreen", fillScreen)
    .addCommand("setRotation", setRotation)
    .addCommand("setCursor", setCursor)
    .addCommand("setTextColor", setTextColor)
    .addCommand("setTextSize", setTextSize);
}

void reset() {
  __carrier->display.setRotation(2);
  __carrier->display.fillScreen(ST77XX_BLACK);
  __carrier->display.setCursor(0, 0);
}

void print(String s) {
  __carrier->display.print(s);
}

void fillScreen(JsonObjectConst& params) {
  uint32_t color = __carrier->leds.Color(params["g"], params["r"], params["b"]);
  __carrier->display.fillScreen(color);
}

void setRotation(long value) {
  __carrier->display.setRotation(value);
}

void setCursor(JsonObjectConst& params) {
  __carrier->display.setCursor(params["x"], params["y"]);
}

void setTextColor(JsonObjectConst& params) {
  uint32_t color = __carrier->leds.Color(params["g"], params["r"], params["b"]);
  __carrier->display.setTextColor(color);
}

void setTextSize(long size) {
  __carrier->display.setTextSize(size);
}

long getOrDefault(JsonObjectConst& params, char* name, long defaultValue) {
  if (params[name] == nullptr) {
    return defaultValue;
  }
  return params[name];
}
