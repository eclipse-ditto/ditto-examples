/*
 * Copyright (c) 2017 Contributors to the Eclipse Foundation
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
#ifndef OCTOPUS_H
#define OCTOPUS_H

#include <Adafruit_Sensor.h>  // Make sure you have the Adafruit Sensor library installed
#include <Adafruit_BME680.h>  // Make sure you have the Adafruit BME680 library installed
#include <Adafruit_BNO055.h>  // Make sure you have the Adafruit BNO055 library installed
#include <utility/imumaths.h>
#include <Adafruit_NeoPixel.h> // Make sure you have the Adafruit NeoPixel library installed

#define PIN_NEOPIXEL      13
#define SEALEVELPRESSURE_HPA (1013.25)

struct Bno055Values {
  float temperature;
  float accelerationX;
  float accelerationY;
  float accelerationZ;
  float orientationX;
  float orientationY;
  float orientationZ;
  float angularVelocityX;
  float angularVelocityY;
  float angularVelocityZ;
  float LinearAccelerationX;
  float LinearAccelerationY;
  float LinearAccelerationZ;
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

struct Bme680Values {
  float temperature;
  float pressure;
  float humidity;
  float gas_resistance;
  float altitude;
};

class Octopus {
 
  Adafruit_BME680 bme680; // I2C
  Adafruit_BNO055 bno055 = Adafruit_BNO055(55);
  Adafruit_NeoPixel strip = Adafruit_NeoPixel(2, PIN_NEOPIXEL, NEO_GRBW + NEO_KHZ800);
  
  void initLights();
  void initBme680();
  void initBno055();
  
  public:
    void begin();
    void connectToWifi(char* ssid, const char* password);
    void showColor(char led, char red, char green, char blue, char white);
    float getVcc ();
    const Bno055Values readBno055();
    const Bme680Values readBme680();
};

#endif
