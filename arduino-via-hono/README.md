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
Configs are the files that hold the ditto and hono properties, as well as the WiFi network information that the sketch uses to connect to WiFi.

All the templates for the config files can be found in `arduino-via-hono/config_template/`. Copy them in the folder where your sketch is located and fill the wifi and ditto/hono properties.

After that change the extension from `.h_template` to just `.h`

## Compiling and running the sketch
First, import `"IoTAgent.h"` to the sketch. Create an IoTAgent instance. The constructor takes the WiFi network properties and an analog pin, used to generate keys for ssl connections. There can only be one instance of the agent in a single sketch.

Create a method `void setup()`. Inside it you can modify the agent, add features and attributes.

There call the method `agent.connect(MQTT_HOST, MQTT_PORT, TENANT_ID, THING_NAMESPACE, THING_NAME, DEVICE_AUTH_ID, DEVICE_PASSWORD);` and get the properties from the config file

Create a method `void loop()`. Inside call the method `agent.loop()` to keep the mqtt connection open

Then you can upload the sketch to the board and you're all set.

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
A feature is a set of properties and commands. The agent can have multiple features, get values for their properties and also execute commands for them.

`Feature feature("feature id", std::vector<String>{...});`

Feature constructor consists of two arguments - Feature name and a vector of definitions. It has a method for adding a property and for adding a command. 

A feature property has a name, a category, which is either `Category::STATUS` or `Category::CONFIGURATION`, a QoS - `QoS::EVENT` or `QoS::TELEMETRY` and a function to get the value for the property. It can also have a optional argument the report period (in milliseconds).
    
`feature.addProperty("name", Category::STATUS, QoS::EVENT, function, 1000);`

Every time the agent's loop function is called, the agent goes through every feature's property and if the acquisition function yields a different result than the last reported value and the time since the last report exceeds the report period for that property, the property is automatically sent.

A feature command is a command, which the host sends to the feature. After every call of the agent's loop function the agent executes the commands received.
A command has a name and a function corresponding to that command.

`feature.addCommand("name", commandHandler);`

After a feature is set up use the `addFeature` method of the agent

`agent.addFeature(feature);`

And the agent will automatically handle it.

## Debuging
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








