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
