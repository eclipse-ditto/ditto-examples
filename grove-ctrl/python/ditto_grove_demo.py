#!/usr/bin/python3
# Copyright (c) 2017 Bosch Software Innovations GmbH.
#
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v2.0
# which accompanies this distribution, and is available at
# https://www.eclipse.org/org/documents/epl-2.0/index.php
#
# Contributors:
#    Bosch Software Innovations GmbH - initial contribution


import raspberry_thing
import websocket
import time
import json
import math

DITTO_IP = "localhost"
DITTO_PORT = "8000"
websocketOpen = False
thing = raspberry_thing.RaspberryDemoThing()


def on_new_illumination_value(illumination):
    if websocketOpen and math.isnan(illumination) == False:
        send_modify_message(thing.create_illumination_change_message(illumination))


def on_new_temperature_value(temperature, humidity):
    if websocketOpen and math.isnan(temperature) == False and math.isnan(humidity) == False:
        send_modify_message(thing.create_temperature_change_message(temperature, humidity))


def on_message(ws, message):
    json_message = json.loads(message)
    thing.handle_websocket_message(json_message)


def send_modify_message(message):
    # convert to JSON
    json_message = json.dumps(message)
    # send via websocket
    ws.send(json_message)


def on_error(ws, error):
    print('An unexpected error happened while using the Websocket connection: {}'.format(error))


def on_close(ws):
    print('Websocket closed - trying to reconnect any second.')
    global websocketOpen
    websocketOpen = False
    time.sleep(5)
    start_websocket()


def on_open(ws):
    print("### Websocket opened ###")
    global websocketOpen
    websocketOpen = True
    # start listening for events and messages
    ws.send("START-SEND-MESSAGES")
    ws.send("START-SEND-EVENTS")


def start_websocket():
    print('Establishing websocket connection ...')
    ws_address = "ws://" + DITTO_IP + ":" + DITTO_PORT + "/ws/1"
    basic_auth = 'Authorization: Basic {}'.format(raspberry_thing.get_b64_auth())
    global ws
    ws = websocket.WebSocketApp(ws_address,
                                header=[basic_auth],
                                on_message=on_message,
                                on_error=on_error,
                                on_close=on_close)
    ws.on_open = on_open
    ws.run_forever()


if __name__ == "__main__":
    # init our raspberry thing
    thing.start_polling_illumination(on_new_illumination_value)
    thing.start_polling_temperatures(on_new_temperature_value)

    # start websocket
    start_websocket()
