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
#ifndef PRINTER_H
#define PRINTER_H
#include <WString.h>

class Printer {

  public: 
    static void printMsg(const char* header, const String& message);
    static void printlnMsg(const char* header, const String& message);
};

#endif
