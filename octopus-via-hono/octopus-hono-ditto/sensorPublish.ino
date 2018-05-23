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

float humidityMin = 1E+20;
float humidityMax = 1E-20;
float tempMin = 1E+20;
float tempMax = 1E-20;
float barometerMin = 1E+20;
float barometerMax = 1E-20;
float powerMin = 1E+20;
float powerMax = 1E-20;
float tempBnoMin = 1E+20;
float tempBnoMax = 1E-20;

String publishSensorDataString(float power, const Bme680Values& bme680Values, const Bno055Values& bno055Values) {
  String output = "{\"topic\": \"";
  output += DITTO_NAMESPACE;
  output += "/";
  output += DITTO_THING_ID;
  output += "/things/twin/commands/modify\",\"headers\": {\"response-required\": false, \"content-type\":\"application/vnd.eclipse.ditto+json\"},";
  output += "\"path\": \"/features\", \"value\":{";
  output += sensorMinMaxValueString("Power", power, powerMin, powerMax, "V") += ",";
  output += sensorMinMaxValueString("Humidity", bme680Values.humidity, humidityMin, humidityMax, "%") += ",";
  output += sensorMinMaxValueString("Temperature", bme680Values.temperature, tempMin, tempMax, "°C") += ",";
  output += sensorMinMaxValueString("Pressure", bme680Values.pressure / 100.0, barometerMin, barometerMax, "hPa") += ",";
  output += sensor3dValueString("Acceleration", bno055Values.accelerationX, bno055Values.accelerationY, bno055Values.accelerationZ, "m/s^2") += ",";
  output += sensor3dValueString("LinearAcceleration", bno055Values.LinearAccelerationX, bno055Values.LinearAccelerationY, bno055Values.LinearAccelerationZ, "m/s^2") += ",";
  output += sensor3dValueString("Orientation", bno055Values.orientationX, bno055Values.orientationY, bno055Values.orientationZ, "°") += ",";
  output += sensor3dValueString("Gravity", bno055Values.gravityX, bno055Values.gravityY, bno055Values.gravityZ, "m/s^2") += ",";
  output += sensor3dValueString("AngularVelocity", bno055Values.angularVelocityX, bno055Values.angularVelocityY, bno055Values.angularVelocityZ, "rad/s") += ",";
  output += sensor3dValueString("MagneticFieldStrength", bno055Values.magneticFieldStrengthX, bno055Values.magneticFieldStrengthY, bno055Values.magneticFieldStrengthZ, "uT");
  output += "}}";
  return output;
}

String sensorMinMaxValueString(const String& featureName, float sensorValue, float minValue, float maxValue, const String& units) {
  String output = "\"" + featureName + "\": { \"properties\": { \"status\":";
  output += "{\"sensorValue\": ";
  output += sensorValue;
  output += ", \"minMeasuredValue\": ";
  output += minValue;
  output += ", \"maxMeasuredValue\": ";
  output += maxValue;
  output += ", \"sensorUnits\": \"";
  output += units;
  output += "\"}}}";
  return output;
}

String sensor3dValueString(const String& featureName, float xValue, float yValue, float zValue, const String& units) {
  String output = "\"" + featureName + "\": { \"properties\": { \"status\":";
  output += "{\"xValue\": ";
  output += xValue;
  output += ", \"yValue\": ";
  output += yValue;
  output += ", \"zValue\": ";
  output += zValue;
  output += ", \"sensorUnits\": \"";
  output += units;
  output += "\"}}}";
  return output;
}

void publishSensorData(float power, const Bme680Values& bme680Values, const Bno055Values& bno055Values) {

  updateMinMax(power, bme680Values, bno055Values);
  hono.publish(publishSensorDataString(power, bme680Values, bno055Values));
}

void updateMinMax(float power, const Bme680Values& bme680Values, const Bno055Values& bno055Values) {
  if (powerMin > power) {
    powerMin = power;
  }
  if (powerMax < power) {
    powerMax = power;
  }

  float humidity = bme680Values.humidity;
  if (humidityMin > humidity) {
    humidityMin = humidity;
  }
  if (humidityMax < humidity) {
    humidityMax = humidity;
  }

  float temp = bme680Values.temperature;
  if (tempMin > temp) {
    tempMin = temp;
  }
  if (tempMax < temp) {
    tempMax = temp;
  }

  float barometer = bme680Values.pressure / 100.0;
  if (barometerMin > barometer) {
    barometerMin = barometer;
  }
  if (barometerMax < barometer) {
    barometerMax = barometer;
  }

  float tempBno = bno055Values.temperature;
  if (tempBnoMin > tempBno) {
    tempBnoMin = tempBno;
  }
  if (tempBnoMax < tempBno) {
    tempBnoMax = tempBno;
  }
}
