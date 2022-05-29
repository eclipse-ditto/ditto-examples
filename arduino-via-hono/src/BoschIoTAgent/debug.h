#ifndef DEBUG_H
#define DEBUG_H

#include <ArduinoJson.h>

enum DebugLevel {
  OFF,
  ERROR,
  WARN,
  INFO,
  DEBUG,
  TRACE
};

void setDebugLevel(const DebugLevel level);
void setDebugStream(Stream* stream);
Stream* getDebugStream();

bool isErrorOn();
bool isWarnOn();
bool isInfoOn();
bool isDebugOn();
bool isTraceOn();

bool error(const char* fmt, ...);
bool error(const __FlashStringHelper* fmt, ...);

bool warn(const char* fmt, ...);
bool warn(const __FlashStringHelper* fmt, ...);

bool info(const char* fmt, ...);
bool info(const __FlashStringHelper* fmt, ...);

bool debug(const char* fmt, ...);
bool debug(const __FlashStringHelper* fmt, ...);

bool trace(const char* fmt, ...);
bool trace(const __FlashStringHelper* fmt, ...);


#endif
