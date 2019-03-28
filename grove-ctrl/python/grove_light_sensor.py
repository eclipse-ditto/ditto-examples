# Copyright (c) 2017 Contributors to the Eclipse Foundation
#
# See the NOTICE file(s) distributed with this work for additional
# information regarding copyright ownership.
#
# This program and the accompanying materials are made available under the
# terms of the Eclipse Public License 2.0 which is available at
# http://www.eclipse.org/legal/epl-2.0
#
# SPDX-License-Identifier: EPL-2.0

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
