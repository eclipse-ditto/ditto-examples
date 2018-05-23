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

void printSensorData(float vcc, const Bme680Values& bme680Values, const Bno055Values& bno055Values) {
  printVcc(vcc);
  printBme680(bme680Values);
  printBno055(bno055Values);
}

void printBme680(const Bme680Values& bme680Values) {
  Printer::printMsg("BME680", "temp: ");
  Serial.print(bme680Values.temperature);
  Serial.print(" °C, pressure: ");
  Serial.print(bme680Values.pressure / 100.0);
  Serial.print(" hPa, humidity: ");
  Serial.print(bme680Values.humidity);
  Serial.print(" %, Gas: ");
  Serial.print(bme680Values.gas_resistance / 1000.0);
  Serial.print(" KOhms, altitude = ");
  Serial.print(bme680Values.altitude);
  Serial.println(" m");
}

void printBno055(const Bno055Values& bno055Values) {
  // Only use values if calibration status > 0
  Printer::printMsg("BNO055", "Calibration status: Sys=");
  Serial.print(bno055Values.calibrationSys, DEC);
  Serial.print(", Gyro=");
  Serial.print(bno055Values.calibrationGyro, DEC);
  Serial.print(", Accel=");
  Serial.print(bno055Values.calibrationAccel, DEC);
  Serial.print(", Mag=");
  Serial.println(bno055Values.calibrationMag, DEC);

  Printer::printMsg("BNO055", "Ambient Temperature: ");
  Serial.print(bno055Values.temperature);
  Serial.println("°C");

  Printer::printMsg("BNO055", "Acceleration (m/s^2) x=");
  Serial.print(bno055Values.accelerationX);
  Serial.print(", y=");
  Serial.print(bno055Values.accelerationY);
  Serial.print(", z=");
  Serial.println(bno055Values.accelerationZ);
  
  Printer::printMsg("BNO055", "Abs Orientation (°, Euler) x=");
  Serial.print(bno055Values.orientationX);
  Serial.print(", y=");
  Serial.print(bno055Values.orientationY);
  Serial.print(", z=");
  Serial.println(bno055Values.orientationZ);
  
  Printer::printMsg("BNO055", "Gravity (m/s^2) x=");
  Serial.print(bno055Values.gravityX);
  Serial.print(", y=");
  Serial.print(bno055Values.gravityY);
  Serial.print(", z=");
  Serial.println(bno055Values.gravityZ);

  Printer::printMsg("BNO055", "Angular velocity (rad/s) x=");
  Serial.print(bno055Values.angularVelocityX);
  Serial.print(", y=");
  Serial.print(bno055Values.angularVelocityY);
  Serial.print(", z=");
  Serial.println(bno055Values.angularVelocityZ);

  Printer::printMsg("BNO055", "Linear Acceleration (m/s^2) x=");
  Serial.print(bno055Values.LinearAccelerationX);
  Serial.print(", y=");
  Serial.print(bno055Values.LinearAccelerationY);
  Serial.print(", z=");
  Serial.println(bno055Values.LinearAccelerationZ);

  Printer::printMsg("BNO055", "Magnetic Field Strength (uT) x=");
  Serial.print(bno055Values.magneticFieldStrengthX);
  Serial.print(", y=");
  Serial.print(bno055Values.magneticFieldStrengthY);
  Serial.print(", z=");
  Serial.println(bno055Values.magneticFieldStrengthZ);
}

void printVcc(float power) {
  Printer::printMsg("VCC", "ESP8266 supply voltage: ");
  Serial.println(power);
}
