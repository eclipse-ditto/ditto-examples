# Connecting an Arduino Device to Eclipse-Ditto

## Requirements

1. An Arduino or an other developer board like "Funduino" etc. This tutorial will work with an Octopus Board (which is pretty rare but most of the code will work with every other board with an ESP8266 on it).
2. You can eiter use the [Arduino IDE](https://www.arduino.cc/en/Main/Software) or [PlatformIO](https://platformio.org/) (highly recommended) which offers a command-line client - if you're a Visual Studio Code User, check out the PlatformIO IDE Extension. PlatformIO let you use your favourite text editor which is pretty neat instead of using the rigid Arduino IDE.
3. Access to your USB Ports (if you're flashing your device through usb).

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
3. Edit the file `${ArduinoDirectory}/libraries/pubsubclient/src/PubSubClient.h` and set the MQTT_MAX_PACKET_SIZE to 2048.

### PlatformIO

#### Prerequesites
1. Python
2. pip
3. virtualenv

#### Installation

To install the platformIOCli follow the installation instructions [here](https://docs.platformio.org/en/latest/installation.html) or install the Atom|VS Code Extension.
*Hint* VS Code is unable to install an extension behind a corporate proxy.

If you have set up a new project, install the depencies (see above) via `pio lib install <Library>` - in case that any library won't be found, it's possible to search for it manually like:
`pio lib search -i 'Adafruit_BNO055.h` (for example). If *pio lib* has found the library you can install it by it's id.

When you're set and all of the needed dependencies are installed - we have to set the `MQTT_MAX_PACKET_SIZE` in `~/yourProjectPath/.piolibdeps/PubSubClient_ID89/src/PubSubClient.h` to (a minimum of) `512`.

*hint* Go for some of the examples in [here](https://github.com/esp8266/Arduino) and check if they compile (with `pio run`).

## Wifi Connection

## MQTT Connection

## Sensors








