# Programing Arduino using Eclipse Ditto and Hono
In order to program an Arduino, you must download the [Arduino IDE](https://www.arduino.cc/en/software) and set it up to compile and send your code to the board.
## Setup the Arduino IDE
1. Configure the SAMD Board support by going to `Tools > Board > Boards Manager...` and searching `Arduino SAMD Boards`
2. Install required libraries by going to `Tools > Manage Libraries`. The libraries are as follows:
    - [ArduinoJson](https://www.arduino.cc/reference/en/libraries/arduinojson/)
    - [ArduinoMqttClient](https://www.arduino.cc/reference/en/libraries/arduinomqttclient/)
    - [SSLClient](https://www.arduino.cc/reference/en/libraries/sslclient/)
    - [Arduino_ConnectionHandler](https://www.arduino.cc/reference/en/libraries/arduino_connectionhandler/)
    - [Arduino_DebugUtils](https://www.arduino.cc/reference/en/libraries/arduino_debugutils/)

## Setting up the library
Copy the folder `arduino-via-hono/src/IoTAgent/` to your libraries folder `${ArduinoDirectory}/libraries`

## Creating configs
Configs are the files that hold the ditto and hono properties and the WiFi network information that the sketch uses to connect to WiFi.

All the templates for the config files can be found in `arduino-via-hono/config_template/`. Copy them in your sketch folder and fill in the WiFi, ditto, and hono properties.

After that change the extension from `.h_template` to just `.h`

## Compiling and running the sketch
Import `"IoTAgent.h"` to the sketch. Create an IoTAgent instance. The constructor takes the WiFi network properties and an analog pin to be used to generate keys for SSL connections. There can only be one instance of the agent in a single sketch.

 Inside the `setup()` function, you can configure the device by adding attributes and features.

There call the method `agent.connect(MQTT_HOST, MQTT_PORT, TENANT_ID, THING_NAMESPACE, THING_NAME, DEVICE_AUTH_ID, DEVICE_PASSWORD);` using the properties from the config file

Inside the `loop()` function, call the method `agent.loop()` to keep the mqtt connection open

Then you can upload the sketch to the board, and you're all set.

### Sample sketch

```c++
#include "IoTAgent.h"

#include "config_wifi.h"
#include "config.h"

#define BOARD_TYPE "Arduino MKR WiFi 1010"
#define BOARD_FQBN "arduino:samd:mkrwifi1010"

IoTAgent agent =
  IoTAgent(SECRET_SSID, SECRET_PASS, A0);

void setup() {
  // enable BoschIoTAgent debug
  setDebugLevel(DebugLevel::INFO);

  Serial.begin(9600);
  // Waiting for Serial to start
  while (!Serial);

  agent
    .addAttribute("Type", String(BOARD_TYPE))
    .addAttribute("FQBN", String(BOARD_FQBN));

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
```

## Creating features and adding them to the agent
A feature is a set of properties and commands. The agent can have multiple features, get values for their properties, and execute commands for them.

`Feature feature("feature id", std::vector<String>{...});`

Feature constructor consists of two arguments - Feature name and a vector of definitions. The Feature class has a method for adding a property and a command. 

A feature property has a name, a category (either `Category::STATUS` or `Category::CONFIGURATION`), a QoS (`QoS::EVENT` or `QoS::TELEMETRY`), and a function to get the value for the property. It can also have an optional argument for the reporting period (in milliseconds).
    
`feature.addProperty("name", Category::STATUS, QoS::EVENT, function, 1000);`

Every time the agent's loop function is called, the agent goes through every feature's property. If the acquisition function yields a different result than the last reported value and the time since the previous report exceeds the reporting period for that property, the property is automatically sent as telemetry or event depending on the property QoS.

A feature command is a command which a backend application sends to the feature. After every agent's loop function call, the agent executes the commands received.
A command has a name and a function corresponding to that command.

`feature.addCommand("name", commandHandler);`

After a feature is set up, use the `addFeature` method of the agent

`agent.addFeature(feature);`

And the agent will automatically handle it.

## Debugging
The IoTAgent library comes with it's own debuging library. The debug level can be set using the `setDebugLevel` method. The debug levels are:
  - `DebugLevel::OFF`
  - `DebugLevel::ERROR`
  - `DebugLevel::WARN`
  - `DebugLevel::INFO`
  - `DebugLevel::DEBUG`
  - `DebugLevel::TRACE`
  
`setDebugLevel(DebugLevel::DEBUG);`

The default debug level is `WARN`

The user can also set the stream in which the debug messages will be sent by using the `setDebugStream` method. The default debug stream is `Serial`.

`setDebugStream(&Serial);`
