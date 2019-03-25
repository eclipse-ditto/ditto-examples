/*
 * Copyright (c) 2017 Bosch Software Innovations GmbH.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/index.php
 *
 * Contributors:
 *    Bosch Software Innovations GmbH - initial contribution
 */

function mapFromDittoProtocolMsg(
    namespace,
    id,
    group,
    channel,
    criterion,
    action,
    path,
    dittoHeaders,
    value
) {

    // ###
    // Insert your mapping logic here:
    // ###

    let buf = new ArrayBuffer(value.length);
    let bufView = new Uint8Array(buf);
    for (let i=0, strLen=value.length; i<strLen; i++) {
        bufView[i] = value.charCodeAt(i);
    }

    headers = dittoHeaders;
    textPayload = null;
    bytePayload =  buf;
    contentType = "application/octet-stream";


    return  Ditto.buildExternalMsg(
        headers,
        textPayload,
        bytePayload,
        contentType
    );
}