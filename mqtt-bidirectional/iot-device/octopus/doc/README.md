# Connecting an Arduino Device to Eclipse Ditto

## Requirements

1. An Arduino or another developer board like "Funduino" etc. This tutorial will work with an
Octopus-board (which is pretty rare but most of the code will work with every other board with an ESP8266 on it).
2. You can either use the [Arduino IDE](https://www.arduino.cc/en/Main/Software) or
[PlatformIO](https://platformio.org/) (highly recommended) which offers a command-line client -
if you're a Visual Studio Code user, check out the PlatformIO IDE Extension. PlatformIO lets
you use your favourite text editor, which is pretty neat instead of using the rigid Arduino IDE.

## Preparing your IDE

### Arduino IDE
1. Add the ESP8266 Platform to the IDE, see [here](https://github.com/esp8266/Arduino).
2. Install the following libraries (Sketch -> Include Library -> Manage Libraries)
    1. [Adafruit Unified Sensor Library](https://github.com/adafruit/Adafruit_Sensor)
    2. [Adafruit BME680 library](https://github.com/adafruit/Adafruit_BME680)
    3. [Adafruit BME280 library](https://github.com/adafruit/Adafruit_BME280)
    4. [Adafruit BNO055 library](https://github.com/adafruit/Adafruit_BNO055)
    5. [Adafruit NeoPixel library](https://github.com/adafruit/Adafruit_NeoPixel)
    6. [PubSubClient library](https://github.com/knolleary/pubsubclient)
    7. [ArduinoJson](https://github.com/bblanchon/ArduinoJson)
    7. [ESP8266Ping](https://github.com/dancol90/ESP8266Ping)
3. Edit the file `${ArduinoDirectory}/libraries/pubsubclient/src/PubSubClient.h` and set the
`MQTT_MAX_PACKET_SIZE` to `2048`.

### PlatformIO

#### Prerequisites
1. Python
2. pip
3. virtualenv

#### Installation

To install the platformIOCli follow the [installation instructions](https://docs.platformio.org/en/latest/installation.html)
or install the Atom|VS Code Extension. *Hint:* VS Code is unable to install extensions behind a corporate proxy.

If you have set up a new project, install the dependencies (see above respectively [Arduino IDE](#Arduino_IDE))
via `pio lib install <Library>` - in case that a library can't be found, it's possible to install it 
from the github repository (see the platformIO manual).

When you're set and all of the needed dependencies are installed - we have to set the
`MQTT_MAX_PACKET_SIZE` in `~/yourProjectPath/.piolibdeps/PubSubClient_ID89/src/PubSubClient.h` to `2048`.

*Hint:* To check if your setup is working as expected, you can go for some of the examples in the
[Arduino ESP8266 git repository](https://github.com/esp8266/Arduino) and check if they compile (with `pio run`).

## Getting started

The Octopus board needs to be connected to the internet to be able to send messages to `test.mosquitto.org`.
Therefore we set valid Wifi credentials in lines `16-17` in `iot-device/octopus/src/main.ino`.

In this demo, we use PlatformIO for compiling and uploading the code to our Octopus board:
```bash
$ cd iot-device/octopus
$ pio run --target upload
// Serial Monitor:
$ pio device monitor -b 115200
```

> You can check the logs using the serial monitor to verify that your board is able to establish
a Wifi connection and can access `test.mosquitto.org`.

## Using MQTT to send/receive Messages on your IoT-Device

The file `iot-device/octopus/src/main.ino` contains the Arduino Code.

### Receive

There are many options to receive messages on your device. We could apply a payload mapping function
for outgoing messages from Eclipse Ditto (as well as incoming mapping - see [here](#Payloadmapping))
but in this tutorial we will receive the full Ditto-Protocol message for the sake of simplicity.

For now we just accept the fact, that our device gets messages in the main topic (defined in the
connection - see [Create MQTT Connection](#Create_MQTT_Connection)): `ditto-tutorial/` and it's
thingId `my.test:octopus/` plus a "command" topic (in this example:) `LED`

> It's not part of this tutorial to show how to establish a wireless or a MQTT connection. For
further questions, see the code in `main.ino` and it's comments.

In the `messageReceived` callback, we can now handle the incoming message:
```cpp
void messageReceived(char* topic, byte* payload, unsigned int length) {

    JsonObject& root = jsonBuffer.parseObject(payload);

    if (root.size() > 0 && root.containsKey("path") && root.containsKey("value")){
        // Get feature to handle and it's value
        const char* path = root["path"];
        const char* payload = root["value"];

        char* command = subStr(path, "/", 3);

        if (strcmp(command, "LED") == 0){
            setLED(payload);
        }
    } else if(!root.containsKey("temp")) {
        Serial.println("[error] - Invalid JSON Object.");
    }
    jsonBuffer.clear();
}
```
Due to the fact, that we just control the built-in LED, this callback has nothing to do except
parsing the incoming message for it's `command` and it's `payload`. The function above parses the full
MQTT topic `ditto-tutorial/my.test:octopus/LED` for it's last substring, check's if it's `LED` and if
yes, set the LED to the payload of the MQTT Message (`"on"` | `"off"`):

```cpp
void setLED(const char* powerState){
    if (strcmp(powerState, "on") == 0){
        // Set LED to red
        strip.setPixelColor(0, 255, 0, 0);
    } else {
        // Set LED to no light -> #off
        strip.setPixelColor(0, 0, 0, 0);
    }
    strip.show();
}
```

### Send

We want to send the sensor data of our device as telemetry data to ditto, which updates the digital
twin and is therefore always accessible for our front-end. Because we have applied a payload mappin 
function before, we can just send our data simple as:
```json
{
    temp: 30.67,
    alt: 360.341
}
```
This means, we send our sensor data periodically to the MQTT Topic `/ditto-tutorial/my.test:octopus`
with a function like
```cpp
void readSensors(){
    // Readable sensors -> reduced on temp and altitude
    JsonObject& root = jsonBuffer.createObject();
    root["temp"] = bme680.readTemperature();
    root["alt"] = bme680.readAltitude(SEALEVELPRESSURE_HPA);
    root["thingId"] = thingId;

    // Transform JSON Object to const char*
    char jsonChar[100];
    root.printTo((char*)jsonChar, root.measureLength() + 1);

    client.publish(outTopic, jsonChar); // outTopic = /ditto-tutorial/my.test:octopus

    // Clear JSON buffer for further use
    jsonBuffer.clear();
}
```








