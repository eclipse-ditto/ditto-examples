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
#include <ESP8266WiFi.h>
#include <WiFiClientSecure.h>
#include "octopus.h"
#include "printer.h"

Octopus::Octopus(const int defaultNeopixelBrightness_) : defaultNeopixelBrightness(defaultNeopixelBrightness_)
{
}

void Octopus::begin()
{
  Serial.println("--- Initializing Octopus --- ");

  this->initLights();

  delay(1000); // give sensors some time to start up
#ifdef BME280
  this->initBme280();
#else
  this->initBme680();
#endif
  this->initBno055();
  delay(500);

  this->showColor(0, 0, 0xff, 0, 0); // green
}

void Octopus::connectToWifi(char *ssid, const char *password)
{

  WiFi.mode(WIFI_STA);
  this->showColor(0, 0, 0, 0, 0xff); // white

  Printer::printMsg("WiFi", "Device MAC address: ");
  Serial.println(WiFi.macAddress());

  Printer::printMsg("WiFi", String("Connecting to WiFi with SSID: '") + String(ssid) + String("' and password '") + String(password) + String("'"));
  WiFi.begin(ssid, password);
  while (WiFi.status() != WL_CONNECTED)
  {
    Serial.print(".");
    delay(500);
  }
  Serial.println();
  this->showColor(0, 0xff, 0xff, 0, 0); // yellow
  randomSeed(micros());
  Printer::printMsg("WiFi", "MAC address: ");
  Serial.println(WiFi.macAddress());
  Printer::printMsg("WiFi", "Connected. IP address: ");
  Serial.println(WiFi.localIP());
  this->showColor(0, 0, 0, 0xff, 0); // blue
  Octopus::setupNTP();
}

void Octopus::setupNTP()
{
  configTime(3 * 3600, 0, "pool.ntp.org", "time.nist.gov");
  time_t now = 0;
  while (now < 100000)
  {
    delay(500);
    time(&now);
  }
  Printer::printMsg("WiFi", "NTP. Time: ");
  Serial.println(ctime(&now));
}

void Octopus::showColor(int led, uint8_t red, uint8_t green, uint8_t blue, uint8_t white)
{
  this->strip.setPixelColor(led, red, green, blue, white);
  this->strip.setBrightness(defaultNeopixelBrightness);
  this->strip.show();
}

uint8_t white(uint32_t c)
{
  return (uint8_t) ((c >> 24) & 0xFF);
}

uint8_t red(uint32_t c)
{
  return (uint8_t) ((c >> 16) & 0xFF);
}

uint8_t green(uint32_t c)
{
  return (uint8_t) ((c >> 8) & 0xFF);
}

uint8_t blue(uint32_t c)
{
  return (uint8_t) (c & 0xFF);
}

LedValues Octopus::readLed(int led)
{
  uint32_t color = this->strip.getPixelColor(led);

  LedValues values = {};
  values.w = white(color);
  values.r = red(color);
  values.g = green(color);
  values.b = blue(color);
  values.brightness = this->strip.getBrightness();

  return values;
}

bool Octopus::toggleLed(int led)
{
  uint32_t color = this->strip.getPixelColor(led);

  bool isOn = (uint8_t)(color >> 24) > 0 ||
              (uint8_t)(color >> 16) > 0 ||
              (uint8_t)(color >> 8) > 0  ||
              (uint8_t)color > 0;

  if (isOn)
  {
    this->showColor(led, 0, 0, 0, 0);
  }
  else
  {
    this->showColor(led, 0, 0, 0, 0xff);
  }

  return true;
}

float Octopus::getVcc()
{
  return ESP.getVcc() / 1000.0;
}

bool Octopus::readBno055(Bno055Values &values)
{
  if (!bno055Ready)
    return false;

  this->bno055.getCalibration(&values.calibrationSys, &values.calibrationGyro, &values.calibrationAccel, &values.calibrationMag);
  values.temperature = this->bno055.getTemp();

  imu::Vector<3> bnoAccel = this->bno055.getVector(Adafruit_BNO055::VECTOR_ACCELEROMETER);
  values.accelerationX = bnoAccel.x();
  values.accelerationY = bnoAccel.y();
  values.accelerationZ = bnoAccel.z();

  imu::Quaternion bnoQuaternion = this->bno055.getQuat();
  values.orientationW = bnoQuaternion.w();
  values.orientationX = bnoQuaternion.x();
  values.orientationY = bnoQuaternion.y();
  values.orientationZ = bnoQuaternion.z();

  imu::Vector<3> bnoGravity = this->bno055.getVector(Adafruit_BNO055::VECTOR_GRAVITY);
  values.gravityX = bnoGravity.x();
  values.gravityY = bnoGravity.y();
  values.gravityZ = bnoGravity.z();

  imu::Vector<3> bnoGyro = this->bno055.getVector(Adafruit_BNO055::VECTOR_GYROSCOPE);
  values.angularVelocityX = bnoGyro.x();
  values.angularVelocityY = bnoGyro.y();
  values.angularVelocityZ = bnoGyro.z();

  imu::Vector<3> bnoLinearAccel = this->bno055.getVector(Adafruit_BNO055::VECTOR_LINEARACCEL);
  values.linearAccelerationX = bnoLinearAccel.x();
  values.linearAccelerationY = bnoLinearAccel.y();
  values.linearAccelerationZ = bnoLinearAccel.z();

  imu::Vector<3> bnoMagnet = this->bno055.getVector(Adafruit_BNO055::VECTOR_MAGNETOMETER);
  values.magneticFieldStrengthX = bnoMagnet.x();
  values.magneticFieldStrengthY = bnoMagnet.y();
  values.magneticFieldStrengthZ = bnoMagnet.z();

  return true;
}

#ifdef BME280
void Octopus::initBme280()
{
  Printer::printMsg("Octopus", "Initializing BME280: ");
  if (this->bme280.begin())
  {
    bme280Ready = true;
    Serial.println("OK");
  }
  else
  {
    bme280Ready = false;
    Serial.println("Not found");
  }
}

bool Octopus::readBme280(Bme680Values &values)
{
  if (!bme280Ready)
    return false;

  this->bme280.begin(0x77);
  values.temperature = this->bme280.readTemperature();
  values.pressure = this->bme280.readPressure();
  values.humidity = this->bme280.readHumidity();
  values.gas_resistance = 0;
  values.altitude = this->bme280.readAltitude(SEALEVELPRESSURE_HPA);
  return true;
}

#else

bool Octopus::readBme680(Bme680Values &values)
{
  if (!bme680Ready)
    return false;

  if (!this->bme680.performReading())
  {
    Serial.println("Sensor reading failure");
    return false;
  }
  else
  {
    values.temperature = bme680.temperature;
    values.pressure = bme680.pressure;
    values.humidity = bme680.humidity;
    values.gas_resistance = bme680.gas_resistance;
    values.altitude = bme680.readAltitude(SEALEVELPRESSURE_HPA);
    return true;
  }
}

void Octopus::initBme680()
{
  Printer::printMsg("Octopus", "Initializing BME680: ");
  if (this->bme680.begin(0x76))
  {
    this->bme680.setTemperatureOversampling(BME680_OS_8X);
    this->bme680.setHumidityOversampling(BME680_OS_2X);
    this->bme680.setPressureOversampling(BME680_OS_4X);
    this->bme680.setIIRFilterSize(BME680_FILTER_SIZE_3);
    this->bme680.setGasHeater(320, 150); // 320*C for 150 ms
    bme680Ready = true;
    Serial.println("OK");
  }
  else
  {
    bme680Ready = false;
    Serial.println("Not found");
  }
}

#endif

void Octopus::initBno055()
{
  Printer::printMsg("Octopus", "Initializing BNO055: ");
  if (this->bno055.begin())
  {
    this->bno055.setExtCrystalUse(true);
    bno055Ready = true;
    Serial.println("OK");
  }
  else
  {
    bno055Ready = false;
    Serial.println("Not found");
  }
}

void Octopus::initLights()
{
  Printer::printlnMsg("Octopus", "Initializing Neopixels");
  this->strip.begin();
  this->strip.show();
  // Initialize all pixels to 'off'
  this->strip.setPixelColor(0, 0, 0, 0);
  this->strip.setPixelColor(1, 0, 0, 0);
  this->strip.show();
}
