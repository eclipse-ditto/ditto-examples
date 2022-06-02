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
#include "Debug.h"
#include <stdarg.h>

static DebugLevel debugLevel = DebugLevel::WARN;
static Stream* debugOut = &Serial;

void setDebugLevel(const DebugLevel level) {
  debugLevel = level;
}

void setDebugStream(Stream* stream) {
  debugOut = stream;
}

Stream* getDebugStream() {
  return debugOut;
}

bool isEnabled(const DebugLevel level) {
  return level <= debugLevel;
}

bool isErrorOn() {
  return isEnabled(DebugLevel::ERROR);
}

bool isWarnOn() {
  return isEnabled(DebugLevel::WARN);
}

bool isInfoOn() {
  return isEnabled(DebugLevel::INFO);
}

bool isDebugOn() {
  return isEnabled(DebugLevel::DEBUG);
}

bool isTraceOn() {
  return isEnabled(DebugLevel::TRACE);
}

bool vargPrint(const char* fmt, va_list args) {
  for (const char* c = fmt; *c != 0; c++) {
    if (*c == '%') {
      switch(*(++c)) {
        case 's':
          debugOut->print(va_arg(args, const char*));
          break;
        case 'S':
          debugOut->print(va_arg(args, String));
          break;
        case 'd':
          debugOut->print(va_arg(args, long));
          break;
        case 'f':
          debugOut->print(va_arg(args, double));
          break;
        case 'j':
          serializeJson(*va_arg(args, DynamicJsonDocument*), *debugOut);
          break;
        case 'J':
          serializeJsonPretty(*va_arg(args, DynamicJsonDocument*), *debugOut);
          break;
        case '%':
          break;
        default:
          return false;
      }
    } else {
      debugOut->print(*c);
    }
  }
  debugOut->print("\n");
  debugOut->flush();
  return true;
}

bool println(const DebugLevel level, const char* fmt, ...) {
  if (!isEnabled(level)) {
    return true;
  }

  va_list args;
  va_start(args, fmt);
  bool out = vargPrint(fmt, args);
  va_end(args);
  return out;
}

bool println(const DebugLevel level, const __FlashStringHelper* fmt, ...) {
  if(!isEnabled(level)) {
    return true;
  }

  String fmtStr(fmt);
  va_list args;
  va_start(args, fmtStr.c_str());
  bool out = vargPrint(fmtStr.c_str(), args);
  va_end(args);
  return out;
}

bool error(const char* fmt, ...) {
  if (!isErrorOn()) {
    return true;
  }

  va_list args;
  va_start(args, fmt);
  bool out = vargPrint(fmt, args);
  va_end(args);
  return out;
}

bool error(const __FlashStringHelper* fmt, ...) {
  if(!isErrorOn()) {
    return true;
  }

  String fmtStr(fmt);
  va_list args;
  va_start(args, fmtStr.c_str());
  bool out = vargPrint(fmtStr.c_str(), args);
  va_end(args);
  return out;
}

bool warn(const char* fmt, ...) {
  if (!isWarnOn()) {
    return true;
  }

  va_list args;
  va_start(args, fmt);
  bool out = vargPrint(fmt, args);
  va_end(args);
  return out;
}

bool warn(const __FlashStringHelper* fmt, ...) {
  if(!isWarnOn()) {
    return true;
  }

  String fmtStr(fmt);
  va_list args;
  va_start(args, fmtStr.c_str());
  bool out = vargPrint(fmtStr.c_str(), args);
  va_end(args);
  return out;
}

bool info(const char* fmt, ...) {
  if (!isInfoOn()) {
    return true;
  }

  va_list args;
  va_start(args, fmt);
  bool out = vargPrint(fmt, args);
  va_end(args);
  return out;
}

bool info(const __FlashStringHelper* fmt, ...) {
  if(!isInfoOn()) {
    return true;
  }

  String fmtStr(fmt);
  va_list args;
  va_start(args, fmtStr.c_str());
  bool out = vargPrint(fmtStr.c_str(), args);
  va_end(args);
  return out;
}

bool debug(const char* fmt, ...) {
  if (!isDebugOn()) {
    return true;
  }

  va_list args;
  va_start(args, fmt);
  bool out = vargPrint(fmt, args);
  va_end(args);
  return out;
}

bool debug(const __FlashStringHelper* fmt, ...) {
  if(!isDebugOn()) {
    return true;
  }

  String fmtStr(fmt);
  va_list args;
  va_start(args, fmtStr.c_str());
  bool out = vargPrint(fmtStr.c_str(), args);
  va_end(args);
  return out;
}

bool trace(const char* fmt, ...) {
  if (!isTraceOn()) {
    return true;
  }

  va_list args;
  va_start(args, fmt);
  bool out = vargPrint(fmt, args);
  va_end(args);
  return out;
}

bool trace(const __FlashStringHelper* fmt, ...) {
  if(!isTraceOn()) {
    return true;
  }

  String fmtStr(fmt);
  va_list args;
  va_start(args, fmtStr.c_str());
  bool out = vargPrint(fmtStr.c_str(), args);
  va_end(args);
  return out;
}
