/*
 * Copyright (c) 2017 Bosch Software Innovations GmbH.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/index.php
 *
 * Contributors:
 *    Bosch Software Innovations GmbH - initial contribution
 */
#include <ESP8266WiFi.h>
#include <WiFiClientSecure.h>
#include <Arduino.h>
#include "octopus.h"
#include "printer.h"

void Octopus::begin() {
   Serial.println("--- Initializing Octopus --- ");  

  this->initLights();
  
  delay(1000); // give sensors some time to start up
  this->initBme680();
  this->initBno055();
  delay(500);
  
  this->showColor(0, 0, 0x80, 0, 0); // green
}

void Octopus::connectToWifi(char* ssid, const char* password) {

  WiFi.mode(WIFI_STA);
  this->showColor(0, 0, 0, 0, 0x80); // white
  Printer::printMsg("Octopus::WiFi", String("Connecting to WiFi with SSID: '") + String(ssid) + String("' and password '") + String(password) + String("'"));
  WiFi.begin(ssid, password);
  while (WiFi.status() != WL_CONNECTED) {
    Serial.print(".");
    delay(500);
  }
  Serial.println();
  this->showColor(0, 0x80, 0x80, 0, 0); // yellow
  randomSeed(micros());
  Printer::printMsg("WiFi", "Connected. IP address: ");
  Serial.println(WiFi.localIP());
  this->showColor(0, 0, 0, 0x80, 0); // blue
}

void Octopus::showColor(char led, char red, char green, char blue, char white) {
  this->strip.setPixelColor(led, red, green, blue, white);
  this->strip.show();
}

float Octopus::getVcc () {
  return ESP.getVcc() / 1000.0;
}

const Bno055Values Octopus::readBno055() {
  Bno055Values values;
  
  this->bno055.getCalibration(&values.calibrationSys, &values.calibrationGyro, &values.calibrationAccel, &values.calibrationMag);
  values.temperature = this->bno055.getTemp();
  
  imu::Vector<3> bnoAccel = this->bno055.getVector(Adafruit_BNO055::VECTOR_ACCELEROMETER);
  values.accelerationX = bnoAccel.x();
  values.accelerationY = bnoAccel.y();
  values.accelerationZ = bnoAccel.z();

  imu::Vector<3> bnoEuler = this->bno055.getVector(Adafruit_BNO055::VECTOR_EULER);
  values.orientationX = bnoEuler.x();
  values.orientationY = bnoEuler.y();
  values.orientationZ = bnoEuler.z();

  imu::Vector<3> bnoGravity = this->bno055.getVector(Adafruit_BNO055::VECTOR_GRAVITY);
  values.gravityX = bnoGravity.x();
  values.gravityY = bnoGravity.y();
  values.gravityZ = bnoGravity.z();
  
  imu::Vector<3> bnoGyro = this->bno055.getVector(Adafruit_BNO055::VECTOR_GYROSCOPE);
  values.angularVelocityX = bnoGyro.x();
  values.angularVelocityY = bnoGyro.y();
  values.angularVelocityZ = bnoGyro.z();

  imu::Vector<3> bnoLinearAccel = this->bno055.getVector(Adafruit_BNO055::VECTOR_LINEARACCEL);
  values.LinearAccelerationX = bnoLinearAccel.x();
  values.LinearAccelerationY = bnoLinearAccel.y();
  values.LinearAccelerationZ = bnoLinearAccel.z();

  imu::Vector<3> bnoMagnet = this->bno055.getVector(Adafruit_BNO055::VECTOR_MAGNETOMETER);
  values.magneticFieldStrengthX = bnoMagnet.x();
  values.magneticFieldStrengthY = bnoMagnet.y();
  values.magneticFieldStrengthZ = bnoMagnet.z();

  return values;
}

const Bme680Values Octopus::readBme680() {
  Bme680Values values;
  if (!this->bme680.performReading()) { 
    Serial.println("Sensor reading failure");
    return values;
  } else {
    values.temperature = bme680.temperature;
    values.pressure = bme680.pressure;
    values.humidity = bme680.humidity;
    values.gas_resistance = bme680.gas_resistance;
    values.altitude = bme680.readAltitude(SEALEVELPRESSURE_HPA);
    return values;
  }
}

void Octopus::initBme680() {
  Printer::printMsg("Octopus", "Initializing BME680: ");
  if (this->bme680.begin(118)) {
    this->bme680.setTemperatureOversampling(BME680_OS_8X);
    this->bme680.setHumidityOversampling(BME680_OS_2X);
    this->bme680.setPressureOversampling(BME680_OS_4X);
    this->bme680.setIIRFilterSize(BME680_FILTER_SIZE_3);
    this->bme680.setGasHeater(320, 150); // 320*C for 150 ms
    Serial.println("OK");
  } else {
    Serial.println("Not found");
  }
}

void Octopus::initBno055() {
  Printer::printMsg("Octopus", "Initializing BNO055: ");
  if (this->bno055.begin()) {
    this->bno055.setExtCrystalUse(true);
    Serial.println("OK");
  } else {
    Serial.println("Not found");
  }
}

void Octopus::initLights() {
  Printer::printlnMsg("Octopus", "Initializing Neopixels");
  this->strip.begin();
  this->strip.show(); 
  // Initialize all pixels to 'off'
  this->strip.setPixelColor(0,0,0,0);
  this->strip.setPixelColor(1,0,0,0);
  this->strip.show(); 
}
