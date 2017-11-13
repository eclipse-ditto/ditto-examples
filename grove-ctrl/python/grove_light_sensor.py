# Copyright (c) 2017 Bosch Software Innovations GmbH.
#
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v2.0
# which accompanies this distribution, and is available at
# https://www.eclipse.org/org/documents/epl-2.0/index.php
#
# Contributors:
#    Bosch Software Innovations GmbH - initial contribution

import grovepi
from datetime import datetime


class LightSensor:
    """A simple abstraction for using the grovepi light sensor"""
    port = None
    lastUpdate = None

    def __init__(self, analogPort, samplingRate=1):
        self.port = analogPort
        self.samplingRate = samplingRate
        grovepi.pinMode(self.port, "INPUT")

    def get_illumination(self):
        self.lastUpdate = datetime.now().__str__()
        return grovepi.analogRead(self.port)

    def get_sampling_rate(self):
        return self.samplingRate

    def get_last_update(self):
        return self.lastUpdate

    def set_sampling_rate(self, samplingRate):
        self.samplingRate = samplingRate

    def get_properties_json(self, illumination):
        return {
            "sensorValue": illumination,
            "lastUpdate": self.get_last_update(),
            "samplingRate": self.get_sampling_rate()
        }
