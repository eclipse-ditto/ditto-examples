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
#include "printer.h";
#include <Arduino.h>

void Printer::printMsg(const char * header, const String& message) {
    Serial.printf("[%s] ", header);

    if(strlen(header) > 17 ) {
      Serial.println("ERROR: Header is longer than 17 characters!");
    }
    
    for (int i = 0; i < 17-strlen(header); i++) {
      Serial.print(" ");
    }
    
    Serial.print(message);
}

void Printer::printlnMsg(const char * header, const String& message) {
    Printer::printMsg(header, message);
    Serial.println();
}
