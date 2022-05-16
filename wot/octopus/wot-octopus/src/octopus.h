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
#pragma once

#include <Adafruit_Sensor.h>
#include <Adafruit_BME680.h>
#include <Adafruit_BNO055.h>
#include <utility/imumaths.h>
#include <Adafruit_NeoPixel.h>

#define PIN_NEOPIXEL 13
#define SEALEVELPRESSURE_HPA 1013.25

struct Bno055Values
{
  float temperature;
  float accelerationX;
  float accelerationY;
  float accelerationZ;
  float orientationW;
  float orientationX;
  float orientationY;
  float orientationZ;
  float angularVelocityX;
  float angularVelocityY;
  float angularVelocityZ;
  float linearAccelerationX;
  float linearAccelerationY;
  float linearAccelerationZ;
  float magneticFieldStrengthX;
  float magneticFieldStrengthY;
  float magneticFieldStrengthZ;
  float gravityX;
  float gravityY;
  float gravityZ;
  uint8_t calibrationSys;
  uint8_t calibrationGyro;
  uint8_t calibrationAccel;
  uint8_t calibrationMag;
};

struct Bme680Values
{
  float temperature;
  float pressure;
  float humidity;
  float gas_resistance;
  float altitude;
};

struct LedValues
{
  uint8_t r;
  uint8_t g;
  uint8_t b;
  uint8_t w;
  uint8_t brightness;
};

class Octopus
{
private:
#ifdef BME280
  Adafruit_BME280 bme280; // I2C
  void initBme280();
  bool bme280Ready;
#else

  Adafruit_BME680 bme680; // I2C
  void initBme680();
  bool bme680Ready;
#endif

  Adafruit_BNO055 bno055 = Adafruit_BNO055(55);
  void initBno055();
  bool bno055Ready;

  Adafruit_NeoPixel strip = Adafruit_NeoPixel(2, PIN_NEOPIXEL, NEO_GRBW + NEO_KHZ800);
  void initLights();
  void setupNTP();

  const int defaultNeopixelBrightness;

public:
  Octopus(const int defaultNeopixelBrightness);

  void begin();
  void connectToWifi(char *ssid, const char *password);
  void showColor(int led, uint8_t red, uint8_t green, uint8_t blue, uint8_t white);
  LedValues readLed(int led);
  bool toggleLed(int led);
  float getVcc();
  bool readBno055(Bno055Values &values);

#ifdef BME280
  bool readBme280(Bme680Values &values);
#else
  bool readBme680(Bme680Values &values);
#endif
};
