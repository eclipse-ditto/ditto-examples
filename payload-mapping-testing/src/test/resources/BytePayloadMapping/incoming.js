/*
 * Copyright (c) 2019 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */

function mapToDittoProtocolMsg(
    headers,
    textPayload,
    bytePayload,
    contentType
) {

    if (contentType !== 'application/octet-stream') {
        return null; // only handle messages with content-type application/octet-stream
    }

    var view = new DataView(bytePayload);

    var value = {
        temperature: {
            properties: {
                // interpret the first 2 bytes (16 bit) as signed int and divide through 100.0:
                value: view.getInt16(0) / 100.0
            }
        },
        pressure: {
            properties: {
                // interpret the next 2 bytes (16 bit) as signed int:
                value: view.getInt16(2)
            }
        },
        humidity: {
            properties: {
                // interpret the next 1 bytes (8 bit) as unsigned int:
                value: view.getUint8(4)
            }
        }
    };

    return Ditto.buildDittoProtocolMsg(
        'the.namespace', // in this example always the same
        headers['device_id'], // Eclipse Hono sets the authenticated device_id as AMQP 1.0 header
        'things', // we deal with a Thing
        'twin', // we want to update the twin
        'commands', // we want to create a command to update a twin
        'modify', // modify the twin
        '/features', // modify all features at once
        headers, // pass through the headers from AMQP 1.0
        value
    );
}
