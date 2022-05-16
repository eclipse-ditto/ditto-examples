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
#include <ArduinoJson.h>
#include "octopus.h"
#include "printer.h"
#include "sensorPublish.h"

const float absoluteMin = -3.4028235E+38;
const float absoluteMax = 3.4028235E+38;

bool humidityMinChanged = true;
float humidityMin = absoluteMax;
bool humidityMaxChanged = true;
float humidityMax = absoluteMin;

bool tempMinChanged = true;
float tempMin = absoluteMax;
bool tempMaxChanged = true;
float tempMax = absoluteMin;

bool barometerMinChanged = true;
float barometerMin = absoluteMax;
bool barometerMaxChanged = true;
float barometerMax = absoluteMin;

bool gasMinChanged = true;
float gasMin = absoluteMax;
bool gasMaxChanged = true;
float gasMax = absoluteMin;

bool tempBnoMinChanged = true;
float tempBnoMin = absoluteMax;
bool tempBnoMaxChanged = true;
float tempBnoMax = absoluteMin;

bool altitudeMinChanged = true;
float altitudeMin = absoluteMax;
bool altitudeMaxChanged = true;
float altitudeMax = absoluteMin;

// memory allocation of the thing json object
// calculated via: https://arduinojson.org/v6/assistant/
const size_t DITTO_PROTOCOL_CAPACITY = 2048;

String buildDittoProtocolMsg(const char *ditto_namespace,
                             const char *ditto_thing_name,
                             float power,
                             const Bme680Values &bme680Values,
                             const Bno055Values &bno055Values,
                             const LedValues &leftLed,
                             const LedValues &rightLed);

void updateMinMax(float power, const Bme680Values &bme680Values, const Bno055Values &bno055Values)
{

  float humidity = bme680Values.humidity;
  if (humidityMin > humidity)
  {
    humidityMinChanged = true;
    humidityMin = humidity;
  }
  if (humidityMax < humidity)
  {
    humidityMaxChanged = true;
    humidityMax = humidity;
  }

  float temp = bme680Values.temperature;
  if (tempMin > temp)
  {
    tempMinChanged = true;
    tempMin = temp;
  }
  if (tempMax < temp)
  {
    tempMaxChanged = true;
    tempMax = temp;
  }

  float barometer = bme680Values.pressure / 100.0;
  if (barometerMin > barometer)
  {
    barometerMinChanged = true;
    barometerMin = barometer;
  }
  if (barometerMax < barometer)
  {
    barometerMaxChanged = true;
    barometerMax = barometer;
  }

  float tempBno = bno055Values.temperature;
  if (tempBnoMin > tempBno)
  {
    tempBnoMinChanged = true;
    tempBnoMin = tempBno;
  }
  if (tempBnoMax < tempBno)
  {
    tempBnoMaxChanged = true;
    tempBnoMax = tempBno;
  }

  float altitude = bme680Values.altitude;
  if (altitudeMin > altitude)
  {
    altitudeMinChanged = true;
    altitudeMin = altitude;
  }
  if (altitudeMax < altitude)
  {
    altitudeMaxChanged = true;
    altitudeMax = altitude;
  }

  float gas = bme680Values.gas_resistance / 1000.0;
  if (gasMin > gas)
  {
    gasMinChanged = true;
    gasMin = gas;
  }
  if (gasMax < gas)
  {
    gasMaxChanged = true;
    gasMax = gas;
  }
}

void setVoltageAsAttribute(const String &attributeName, float sensorValue, JsonObject &thing)
{
  thing["attributes"]["currentVoltage"] = sensorValue;
}

void setSensorMinMaxPropertiesForFeature(const String &featureId, const String &sensorSuffix, float sensorValue, float minValue, float maxValue, JsonObject &features)
{
  JsonObject feature = features.createNestedObject(featureId);
  JsonObject properties = feature.createNestedObject("properties");
  properties["current" + sensorSuffix] = sensorValue;
  if (minValue < absoluteMax)
  {
    properties["minMeasured" + sensorSuffix] = minValue;
  }
  if (maxValue > absoluteMin)
  {
    properties["maxMeasured" + sensorSuffix] = maxValue;
  }
}

void setSensor3dPropertiesForFeature(const String &featureId, const String &sensorSuffix, float xValue, float yValue, float zValue, JsonObject &features)
{
  JsonObject feature = features.createNestedObject(featureId);
  JsonObject properties = feature.createNestedObject("properties");
  JsonObject subObject = properties.createNestedObject("current" + sensorSuffix);
  subObject["x"] = xValue;
  subObject["y"] = yValue;
  subObject["z"] = zValue;
}

void setSensor4dPropertiesForFeature(const String &featureId, const String &sensorSuffix, float wValue, float xValue, float yValue, float zValue, JsonObject &features)
{
  JsonObject feature = features.createNestedObject(featureId);
  JsonObject properties = feature.createNestedObject("properties");
  JsonObject subObject = properties.createNestedObject("current" + sensorSuffix);
  subObject["w"] = wValue;
  subObject["x"] = xValue;
  subObject["y"] = yValue;
  subObject["z"] = zValue;
}

// rounds a number to 3 decimal places
double round3(double value) {
   return (int)(value * 1000 + 5.0) / 1000.0;
}

void setLedPropertiesForFeature(const String &featureId, const LedValues &ledValues, JsonObject &features)
{
  JsonObject feature = features.createNestedObject(featureId);
  JsonObject properties = feature.createNestedObject("properties");
  properties["on"] = (ledValues.r > 0 || ledValues.g > 0 ||  ledValues.b > 0 || ledValues.w > 0) && ledValues.brightness > 0;
  JsonObject color = properties.createNestedObject("color");
  color["r"] = ledValues.r;
  color["g"] = ledValues.g;
  color["b"] = ledValues.b;
  color["w"] = ledValues.w;
  double brightnessPercent = round3(ledValues.brightness / 255.0);
  properties["dimmer-level"] = serialized(String(brightnessPercent, brightnessPercent == 1.0 ? 1 : 3));
}

String buildDittoProtocolMsg(const char *ditto_namespace,
                             const char *ditto_thing_name,
                             float power,
                             const Bme680Values &bme680Values,
                             const Bno055Values &bno055Values,
                             const LedValues &leftLed,
                             const LedValues &rightLed)
{

  updateMinMax(power, bme680Values, bno055Values);

  DynamicJsonDocument dittoProtocolMsg(DITTO_PROTOCOL_CAPACITY);

  dittoProtocolMsg["topic"] = String(ditto_namespace) + "/" + String(ditto_thing_name) + "/things/twin/commands/merge";
  JsonObject headers = dittoProtocolMsg.createNestedObject("headers");
  headers["response-required"] = false;
  headers["content-type"] = "application/merge-patch+json";
  dittoProtocolMsg["path"] = "/";

  JsonObject thing = dittoProtocolMsg.createNestedObject("value");
  setVoltageAsAttribute("currentVoltage", power, thing);

  JsonObject features = thing.createNestedObject("features");
  setSensorMinMaxPropertiesForFeature("Humidity", "RelativeHumidity",
                                      bme680Values.humidity,
                                      humidityMinChanged ? humidityMin : absoluteMax,
                                      humidityMaxChanged ? humidityMax : absoluteMin,
                                      features);
  humidityMinChanged = false;
  humidityMaxChanged = false;

  setSensorMinMaxPropertiesForFeature("Temperature", "Temperature",
                                      bme680Values.temperature,
                                      tempMinChanged ? tempMin : absoluteMax,
                                      tempMaxChanged ? tempMax : absoluteMin,
                                      features);
  tempMinChanged = false;
  tempMaxChanged = false;

  setSensorMinMaxPropertiesForFeature("BarometricPressure", "BarometricPressure",
                                      bme680Values.pressure / 100.0,
                                      barometerMinChanged ? barometerMin : absoluteMax,
                                      barometerMaxChanged ? barometerMax : absoluteMin,
                                      features);
  barometerMinChanged = false;
  barometerMaxChanged = false;

  setSensorMinMaxPropertiesForFeature("GasResistance", "Voc",
                                      bme680Values.gas_resistance / 1000.0,
                                      gasMinChanged ? gasMin : absoluteMax,
                                      gasMaxChanged ? gasMax : absoluteMin,
                                      features);
  gasMinChanged = false;
  gasMaxChanged = false;

  setSensorMinMaxPropertiesForFeature("Altitude", "Altitude",
                                      bme680Values.altitude,
                                      altitudeMinChanged ? altitudeMin : absoluteMax,
                                      altitudeMaxChanged ? altitudeMax : absoluteMin,
                                      features);
  altitudeMinChanged = false;
  altitudeMaxChanged = false;

  setSensorMinMaxPropertiesForFeature("AmbientTemperature", "Temperature",
                                      bno055Values.temperature,
                                      tempBnoMinChanged ? tempBnoMin : absoluteMax,
                                      tempBnoMaxChanged ? tempBnoMax : absoluteMin,
                                      features);
  tempBnoMinChanged = false;
  tempBnoMaxChanged = false;

  setSensor4dPropertiesForFeature("Orientation", "Orientation",
                                  bno055Values.orientationW,
                                  bno055Values.orientationX,
                                  bno055Values.orientationY,
                                  bno055Values.orientationZ,
                                  features);
  setSensor3dPropertiesForFeature("Acceleration", "Acceleration",
                                  bno055Values.accelerationX,
                                  bno055Values.accelerationY,
                                  bno055Values.accelerationZ,
                                  features);
  setSensor3dPropertiesForFeature("Gravity", "Acceleration",
                                  bno055Values.gravityX,
                                  bno055Values.gravityY,
                                  bno055Values.gravityZ,
                                  features);
  setSensor3dPropertiesForFeature("Gyrometer", "AngularVelocity",
                                  bno055Values.angularVelocityX,
                                  bno055Values.angularVelocityY,
                                  bno055Values.angularVelocityZ,
                                  features);
  setSensor3dPropertiesForFeature("LinearAcceleration", "Acceleration",
                                  bno055Values.linearAccelerationX,
                                  bno055Values.linearAccelerationY,
                                  bno055Values.linearAccelerationZ,
                                  features);
  setSensor3dPropertiesForFeature("Magnetometer", "MagneticFieldStrength",
                                  bno055Values.magneticFieldStrengthX,
                                  bno055Values.magneticFieldStrengthY,
                                  bno055Values.magneticFieldStrengthZ,
                                  features);
  setLedPropertiesForFeature("LeftLED", leftLed, features);
  setLedPropertiesForFeature("RightLED", rightLed, features);

  String output;
  serializeJson(dittoProtocolMsg, output);
  return output;
}
