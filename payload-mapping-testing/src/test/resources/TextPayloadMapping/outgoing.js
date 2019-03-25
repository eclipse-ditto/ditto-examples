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

    let finalValue = value + "appendix";

    headers = dittoHeaders;
    textPayload = finalValue;
    bytePayload =  null;
    contentType = "TEXT";


    return  Ditto.buildExternalMsg(
        headers,
        textPayload,
        bytePayload,
        contentType
    );
}