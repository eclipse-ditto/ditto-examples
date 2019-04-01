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
package org.eclipse.ditto.examples.mappingfunction.testcase;


import java.io.IOException;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.ditto.json.JsonFactory;
import org.eclipse.ditto.json.JsonObject;
import org.eclipse.ditto.mappingfunction.testcase.PayloadMappingFunction;
import org.eclipse.ditto.mappingfunction.testcase.PayloadMappingTestCase;
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
        final PayloadMappingFunction underTest = PayloadMappingFunction.fromJavaScript(incomingMappingFunction.getContent());

        final Map<String, String> headers = new HashMap<>();
        headers.put("content-type", ContentTypes.APPLICATION_OCTET_STREAM.toString());
        headers.put("device_id", "the-thing-id");

        final byte[] bytePayload = new BigInteger("09EF03F72A", 16).toByteArray();
        final ExternalMessage message = ExternalMessageFactory.newExternalMessageBuilder(headers)
                .withBytes(bytePayload)
                .build();

        final Resource expectedAdaptableJsonResource = new Resource("BytePayloadMapping/expectedAdaptable.json");
        final JsonObject expectedAdaptableJson = JsonFactory.newObject(expectedAdaptableJsonResource.getContent());
        final Adaptable expectedAdaptable = ProtocolFactory
                .jsonifiableAdaptableFromJson(expectedAdaptableJson)
                .setDittoHeaders(DittoHeaders.of(headers));

        PayloadMappingTestCase.assertThat(message)
                .mappedByJavascriptPayloadMappingFunction(underTest)
                .isEqualTo(expectedAdaptable)
                .verify();
    }

    @Test
    public void incomingBytePayloadMappingWithByteBufferJs() throws IOException {
        final Resource incomingMappingFunction = new Resource("BytePayloadMapping/incomingWithByteBuffer.js");
        final PayloadMappingFunction underTest = PayloadMappingFunction.fromJavaScript(incomingMappingFunction.getContent());

        final Map<String, String> headers = new HashMap<>();
        headers.put("content-type", ContentTypes.APPLICATION_OCTET_STREAM.toString());
        headers.put("device_id", "the-thing-id");

        final byte[] bytePayload = new BigInteger("09EF03F72A", 16).toByteArray();
        final ExternalMessage message = ExternalMessageFactory.newExternalMessageBuilder(headers)
                .withBytes(bytePayload)
                .build();

        final Resource expectedAdaptableJsonResource = new Resource("BytePayloadMapping/expectedAdaptable.json");
        final JsonObject expectedAdaptableJson = JsonFactory.newObject(expectedAdaptableJsonResource.getContent());
        final Adaptable expectedAdaptable = ProtocolFactory
                .jsonifiableAdaptableFromJson(expectedAdaptableJson)
                .setDittoHeaders(DittoHeaders.of(headers));

        PayloadMappingTestCase.assertThat(message)
                .mappedByJavascriptPayloadMappingFunction(underTest)
                .isEqualTo(expectedAdaptable)
                .withByteBufferJs()
                .verify();
    }


    @Test
    public void outgoingBytePayloadMapping() throws IOException {
        final Resource outgoingMappingFunction = new Resource("BytePayloadMapping/outgoing.js");
        final PayloadMappingFunction underTest = PayloadMappingFunction.fromJavaScript(outgoingMappingFunction.getContent());

        final Map<String, String> headers = new HashMap<>();
        headers.put("content-type", ContentTypes.APPLICATION_OCTET_STREAM.toString());
        headers.put("device_id", "the-device-id");

        byte[] bytes = "HelloBytes".getBytes();
        final ExternalMessage expectedExternalMessage = ExternalMessageFactory.newExternalMessageBuilder(headers)
                .withBytes(bytes)
                .build();

        final Resource outgoingJsonResource = new Resource("BytePayloadMapping/outgoing.json");
        final JsonObject outgoingJson = JsonFactory.newObject(outgoingJsonResource.getContent());
        final Adaptable adaptableToMap = ProtocolFactory
                .jsonifiableAdaptableFromJson(outgoingJson)
                .setDittoHeaders(DittoHeaders.of(headers));

        PayloadMappingTestCase.assertThat(adaptableToMap)
                .mappedByJavascriptPayloadMappingFunction(underTest)
                .isEqualTo(expectedExternalMessage)
                .verify();
    }
}
