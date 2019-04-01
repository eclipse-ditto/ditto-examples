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
