# Copyright (c) 2017 Bosch Software Innovations GmbH.
#
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v2.0
# which accompanies this distribution, and is available at
# https://www.eclipse.org/org/documents/epl-2.0/index.php
#
# Contributors:
#    Bosch Software Innovations GmbH - initial contribution

# To 'mock' the calls to grovepi script, change this line to 'import grovepi_mock as grovepi'
import base64
import time
import threading
from grove_buzzer import Buzzer
from grove_light_sensor import LightSensor
from grove_temp_sensor import TemperatureHumiditySensor

# User and password needed for providing new sensor values
THING_USER = "raspberry"
THING_PASSWORD = "raspberry"
# The id of our raspberry Thing
THING_ID = "org.eclipse.ditto.example/raspberry"
# Message / Event paths.
THING_EVENT_TOPIC = THING_ID + "/things/twin/events"
THING_COMMAND_TOPIC = THING_ID + "/things/twin/commands"
THING_MESSAGE_TOPIC = THING_ID + "/things/live/messages"

THING_MODIFY_COMMAND_TOPIC = THING_COMMAND_TOPIC + "/modify"
ILLUMINANCE_SENSOR_PROPERTIES_PATH = "/features/IlluminanceSensor_0/properties"
TEMPERATURE_SENSOR_PROPERTIES_PATH = "/features/TemperatureHumiditySensor_0/properties"
ILLUMINANCE_SENSOR_SAMPLINGRATE_PATH = ILLUMINANCE_SENSOR_PROPERTIES_PATH + "/samplingRate"
TEMPERATURE_SENSOR_SAMPLINGRATE_PATH = TEMPERATURE_SENSOR_PROPERTIES_PATH + "/samplingRate"
BUZZER_ENABLE_MESSAGE_TOPIC = THING_MESSAGE_TOPIC + "/doEnable"
BUZZER_ENABLE_MESSAGE_PATH = "/features/Buzzer_0/inbox/messages/doEnable"
FREQUENCY_CHANGE_EVENT_TOPIC = THING_ID + "/things/twin/events/modified"

# Digital Port D8 on the GrovePi+ is connected to the buzzer
BUZZER_PORT = 8
# Analog Port A0 on the GrovePi+ is connected to the light sensor
LIGHT_SENSOR_PORT = 0
# Digital Port D4 on the GrovePi+ is connected to the temp sensor
TEMP_SENSOR_PORT = 4


def get_b64_auth():
    """
    Get the base 64 auth string for the thin user.
    """
    userpw_string = THING_USER + ":" + THING_PASSWORD
    utf_str = userpw_string.encode("utf-8")
    return base64.b64encode(utf_str).decode('utf-8')


class RaspberryDemoThing:
    def __init__(self):
        self.buzzer = Buzzer(BUZZER_PORT)
        self.lightSensor = LightSensor(LIGHT_SENSOR_PORT)
        self.tempSensor = TemperatureHumiditySensor(TEMP_SENSOR_PORT)

    def handle_websocket_message(self, message):
        if message and 'topic' in message:
            if message['topic'].startswith(THING_EVENT_TOPIC):
                # handle event message
                self.__handle_event(message)
            elif message['topic'].startswith(THING_MESSAGE_TOPIC):
                self.__handle_message(message)

    def create_illumination_change_message(self, illumination):
        """
            Create the modify message that is used to notify Ditto about a new sensor value.
            :return: The message as a json object.
            """
        return {
            "topic": THING_MODIFY_COMMAND_TOPIC,
            "path": ILLUMINANCE_SENSOR_PROPERTIES_PATH,
            "value": self.lightSensor.get_properties_json(illumination)
        }

    def create_temperature_change_message(self, temperature, humidity):
        """
            Create the modify message that is used to notify Ditto about a new sensor value.
            :return: The message as a json object.
            """
        return {
            "topic": THING_MODIFY_COMMAND_TOPIC,
            "path": TEMPERATURE_SENSOR_PROPERTIES_PATH,
            "value": self.tempSensor.get_properties_json(temperature, humidity)
        }

    def start_polling_illumination(self, callback):
        """
            this function will repeatedly queries illumination values and send them back to the callback.
            :param callback: callback function for new sensor values
            :return: None
            """
        threading._start_new_thread(self.__poll_illumination_values, (callback,))

    def start_polling_temperatures(self, callback):
        """
            this function will repeatedly queries temperature values and send them back to the callback.
            :param callback: callback function for new sensor values
            :return: None
            """
        threading._start_new_thread(self.__poll_temperature_values, (callback,))

    def __poll_illumination_values(self, callback):
        while True:
            try:
                # read sensor
                illumination = self.lightSensor.get_illumination()
                # call callback
                callback(illumination)
            except Exception:
                print('Error when providing illumination values. Trying again')
            finally:
                # wait for next read
                time.sleep(1 / self.lightSensor.get_sampling_rate())

    def __poll_temperature_values(self, callback):
        while True:
            try:
                # read sensor
                [temperature, illumination] = self.tempSensor.get_temperature_and_humidity()
                # call callback
                callback(temperature, illumination)
            except Exception:
                print('Error when providing illumination values. Trying again')
            finally:
                # wait for next read
                time.sleep(1 / self.tempSensor.get_sampling_rate())

    def __handle_event(self, event):
        if self.__is_illuminance_sampling_rate_change(event):
            """
            update the frequency in which the sensor values of the thing should be queried
            :param message: the modify event that should contain the new frequency
            """
            samplingRate = event['value']
            print('setting samplingRate of light sensor to {} Hz'.format(samplingRate))
            self.lightSensor.set_sampling_rate(samplingRate)
        elif self.__is_temperature_sampling_rate_change(event):
            """
            update the frequency in which the sensor values of the thing should be queried
            :param message: the modify event that should contain the new frequency
            """
            samplingRate = event['value']
            print('setting samplingRate of temperature sensor to {} Hz'.format(samplingRate))
            self.tempSensor.set_sampling_rate(samplingRate)

    def __handle_message(self, message):
        if self.__is_buzzer_property_change(message):
            """
                Activate/Deactivate the buzzer.
                :param message: The message that should contain if the buzzer should be active oder inactive
                """
            buzz = message['value']
            print('{} buzzer'.format('enabling' if buzz else 'disabling'))
            self.buzzer.set_enabled(buzz)

    def __is_buzzer_property_change(self, message):
        """
        Check if the message is for activating or deactivating the buzzer.
        :return: true if should be activated.
        """
        return BUZZER_ENABLE_MESSAGE_TOPIC == message['topic'] \
               and BUZZER_ENABLE_MESSAGE_PATH == message['path']

    def __is_illuminance_sampling_rate_change(self, message):
        """
        Check if the message is for changing the frequency in which sensor values are requested.
        :return: True if it is such kind of a message
        """
        return 'path' in message \
               and 'value' in message \
               and FREQUENCY_CHANGE_EVENT_TOPIC == message['topic'] \
               and ILLUMINANCE_SENSOR_SAMPLINGRATE_PATH == message['path']

    def __is_temperature_sampling_rate_change(self, message):
        """
        Check if the message is for changing the frequency in which sensor values are requested.
        :return: True if it is such kind of a message
        """
        return 'path' in message \
               and 'value' in message \
               and FREQUENCY_CHANGE_EVENT_TOPIC == message['topic'] \
               and TEMPERATURE_SENSOR_SAMPLINGRATE_PATH == message['path']
