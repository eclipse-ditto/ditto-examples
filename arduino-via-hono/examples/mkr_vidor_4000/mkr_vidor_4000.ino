#include <string.h>

#include "BoschIoTAgent.h"
#include "feature_wifi.h"
#include "feature_test.h"

#include "config_wifi.h"
#include "config_bosch_iot.h"

#define BOARD_TYPE "Arduino MKR Vidor 4000"
#define BOARD_FQBN "arduino:samd:mkrvidor4000"

BoschIoTAgent agent =
  BoschIoTAgent(SECRET_SSID, SECRET_PASS, A0);

void setup() {
  // enable debug in Arduino_DebugUtils, debug levels: DBG_NONE, DBG_ERROR, DBG_WARNING, DBG_INFO ,DBG_DEBUG, DBG_VERBOSE
  setDebugMessageLevel(DBG_DEBUG);
  // enable BoschIoTAgent debug
  setDebugLevel(DebugLevel::TRACE);

  Serial.begin(9600);
  // Waiting for Serial to start
  while (!Serial);

  agent
    .addAttribute("Type", String(BOARD_TYPE))
    .addAttribute("FQBN", String(BOARD_FQBN))
    .addFeature(wifiFeature())
    .addFeature(testFeature());

  // connecting the agent  to the MQTT broker
  agent.connect(
    MQTT_BROKER, MQTT_PORT,
    TENANT_ID, THING_NAMESPACE, THING_NAME,
    DEVICE_AUTH_ID, DEVICE_PASSWORD
  );
}

void loop() {
  agent.loop();
  delay(3000);
}
