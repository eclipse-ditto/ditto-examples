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

function mapToDittoProtocolMsg(
    headers, 
    textPayload, 
    bytePayload, 
    contentType 
) { 
 
    // ### 
    // Insert your mapping logic here 
    let namespace = "org.eclipse.ditto";
    let id = "fancy-car-11";
    let group = "things"; 
    let channel = "twin"; 
    let criterion = "commands"; 
    let action = "modify"; 
    let path = "/attributes/foo";
    let dittoHeaders = {}; 
    dittoHeaders["correlation-id"] = headers["correlation-id"]; 
    let value = textPayload; 
    // ### 
 
    return Ditto.buildDittoProtocolMsg( 
        namespace, 
        id, 
        group, 
        channel, 
        criterion, 
        action, 
        path, 
        dittoHeaders, 
        value 
    ); 
};