# Copyright (c) 2017 Bosch Software Innovations GmbH.
#
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v2.0
# which accompanies this distribution, and is available at
# https://www.eclipse.org/org/documents/epl-2.0/index.php
#
# Contributors:
#    Bosch Software Innovations GmbH - initial contribution

analogReadIndex = 0
analogReadValues = [130.12, 113199.13, 939992.3, 57.5]

tempReadIndex = 0
tempReadValues = [[17.2, 55.0], [18.0, 65.3], [16.1, 33.3], [10.7, 47.5]]


def pinMode(port, value):
    pass


def digitalWrite(port, value):
    pass


def analogRead(port):
    global analogReadIndex
    value = analogReadValues[analogReadIndex % analogReadValues.__len__()]
    analogReadIndex += 1
    return value


def dht(port, module_type):
    global tempReadIndex
    [t, h] = tempReadValues[tempReadIndex % tempReadValues.__len__()]
    tempReadIndex += 1
    return [t, h]
