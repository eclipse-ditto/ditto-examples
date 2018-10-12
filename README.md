# Eclipse Ditto :: Examples

[![Join the chat at https://gitter.im/eclipse/ditto](https://badges.gitter.im/eclipse/ditto.svg)](https://gitter.im/eclipse/ditto?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)

This repository contains examples and demonstrations of how to use [Eclipse Ditto](https://github.com/eclipse/ditto).

# Projects

## [grove control](grove-ctrl/)

The grove control project shows the different communication possibilities
using Eclipse Ditto on your local machine and a Rasperry Pi with GrovePi+ board
as IoT device. This project uses Python for the Raspberry Pi code and jQuery for the Web UI.

## [Rest to WebSocket demo](rest-to-websocket/)

This example shows how to combine the REST and WebSocket API of Eclipse Ditto.
This is demonstrated using a Frontend that sends REST requests and
a Thing that has a WebSocket connection to Ditto and uses it to receive
and respond to Messages. This project requires a running Eclipse Ditto
instance and a modern web browser.

## [Octopus via Hono to Ditto](octopus-via-hono/)

Arduino based example on a ESP8266 board publishing read out sensor values in Ditto Protocol via 
the MQTT endpoint of [Eclipse Hono](https://www.eclipse.org/hono/) to a digital twin in Ditto:

* BME680 sensor
    * temperature
    * humidity
    * barometer
* BNO055 sensor
    * temperature
    * linear acceleration
    * angular velocity
    * gravity
    * absolute orientation
    * accelerometer
    * magnetometer
* power voltage

## [IoT-Device connected directly to Ditto via MQTT - controlled by a custom solution](mqtt-bidirectional/)

This example demonstrates how to connect an Arduino based device to Eclipse Ditto and how
payload mapping can be utilized to transform its telemetry data into a valid digital twin representation.
Furthermore a simple front-end allows manipulating the digital twin and receives twin updates
via SSE (Server Sent Events).

### Parts of this example
* Arduino
    * How to establish a network connection
    * How to establish a MQTT connection
    * How to receive and publish valid JSON data
* Front-end
    * How to use Ditto's HTTP API for
        * Create policy and things
        * Create a connection (MQTT)
        * Send live messages to device
        * Listen to server sent events
* Eclipse Ditto
    * How to set up Eclipse Ditto with Docker (alternatively use Ditto's Sandbox)
    * How to apply payload mapping on incoming messages from a connection