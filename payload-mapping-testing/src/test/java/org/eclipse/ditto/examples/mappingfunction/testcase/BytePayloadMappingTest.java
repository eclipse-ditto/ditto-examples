/*
 * Copyright (c) 2017-2018 Bosch Software Innovations GmbH.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/index.php
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.ditto.examples.mappingfunction.testcase;


import java.io.IOException;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.ditto.json.JsonFactory;
import org.eclipse.ditto.json.JsonObject;
import org.eclipse.ditto.mappingfunction.testcase.MappingFunction;
import org.eclipse.ditto.mappingfunction.testcase.MappingFunctionTestCase;
import org.eclipse.ditto.mappingfunction.testcase.Resource;
import org.eclipse.ditto.model.base.headers.DittoHeaders;
import org.eclipse.ditto.protocoladapter.Adaptable;
import org.eclipse.ditto.protocoladapter.ProtocolFactory;
import org.eclipse.ditto.services.models.connectivity.ExternalMessage;
import org.eclipse.ditto.services.models.connectivity.ExternalMessageFactory;
import org.junit.Test;

import akka.http.javadsl.model.ContentTypes;

public final class BytePayloadMappingTest {

    @Test
    public void incomingBytePayloadMapping() throws IOException {
        final Resource incomingMappingFunction = new Resource("BytePayloadMapping/incoming.js");
        final MappingFunction underTest = MappingFunction.fromJavaScript(incomingMappingFunction.getContent());

        final Map<String, String> headers = new HashMap<>();
        headers.put("content-type", ContentTypes.APPLICATION_OCTET_STREAM.toString());
        headers.put("device_id", "the-thing-id");
        final DittoHeaders dittoHeaders = DittoHeaders.of(headers);

        final byte[] bytePayload = new BigInteger("09EF03F72A", 16).toByteArray();
        final ExternalMessage message = ExternalMessageFactory.newExternalMessageBuilder(dittoHeaders)
                .withBytes(bytePayload)
                .build();

        final Resource expectedAdaptableJsonResource = new Resource("BytePayloadMapping/expectedAdaptable.json");
        final JsonObject expectedAdaptableJson = JsonFactory.newObject(expectedAdaptableJsonResource.getContent());
        final Adaptable expectedAdaptable = ProtocolFactory
                .jsonifiableAdaptableFromJson(expectedAdaptableJson)
                .setDittoHeaders(dittoHeaders);

        MappingFunctionTestCase.forIncomingMappingFunction(underTest)
                .withExpectedMappingResult(expectedAdaptable)
                .whenMapping(message)
                .run();
    }


    @Test
    public void outgoingBytePayloadMapping() throws IOException {
        final Resource outgoingMappingFunction = new Resource("BytePayloadMapping/outgoing.js");
        final MappingFunction underTest = MappingFunction.fromJavaScript(outgoingMappingFunction.getContent());

        final Map<String, String> headers = new HashMap<>();
        headers.put("content-type", ContentTypes.APPLICATION_OCTET_STREAM.toString());
        headers.put("device_id", "the-device-id");
        final DittoHeaders dittoHeaders = DittoHeaders.of(headers);

        byte[] bytes = "HelloBytes".getBytes();
        final ExternalMessage expectedExternalMessage = ExternalMessageFactory.newExternalMessageBuilder(dittoHeaders)
                .withBytes(bytes)
                .build();

        final Resource outgoingJsonResource = new Resource("BytePayloadMapping/outgoing.json");
        final JsonObject outgoingJson = JsonFactory.newObject(outgoingJsonResource.getContent());
        final Adaptable adaptableToMap = ProtocolFactory
                .jsonifiableAdaptableFromJson(outgoingJson)
                .setDittoHeaders(dittoHeaders);


        MappingFunctionTestCase.forOutgoingMappingFunction(underTest)
                .withExpectedMappingResult(expectedExternalMessage)
                .whenMapping(adaptableToMap)
                .run();
    }
}
