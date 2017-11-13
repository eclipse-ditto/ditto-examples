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


class Buzzer:
    """A simple abstraction for using the grovepi buzzer"""
    port = None

    def __init__(self, digitalPort, enabled=False):
        self.port = digitalPort
        self.enabled = enabled
        grovepi.pinMode(self.port, "OUTPUT")

    def is_enabled(self):
        return self.enabled

    def set_enabled(self, enabled):
        self.enabled = enabled
        int_to_write = 1 if self.enabled else 0
        grovepi.digitalWrite(self.port, int_to_write)
