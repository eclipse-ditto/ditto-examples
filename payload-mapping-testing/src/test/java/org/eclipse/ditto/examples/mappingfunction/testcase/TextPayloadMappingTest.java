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
import java.util.HashMap;
import java.util.Map;

import org.eclipse.ditto.base.model.headers.DittoHeaders;
import org.eclipse.ditto.connectivity.api.ExternalMessage;
import org.eclipse.ditto.connectivity.api.ExternalMessageFactory;
import org.eclipse.ditto.json.JsonFactory;
import org.eclipse.ditto.json.JsonObject;
import org.eclipse.ditto.mappingfunction.testcase.PayloadMappingFunction;
import org.eclipse.ditto.mappingfunction.testcase.PayloadMappingTestCase;
import org.eclipse.ditto.mappingfunction.testcase.Resource;
import org.eclipse.ditto.protocol.Adaptable;
import org.eclipse.ditto.protocol.ProtocolFactory;
import org.junit.Test;

import akka.http.javadsl.model.ContentTypes;

public final class TextPayloadMappingTest {

    @Test
    public void incomingTextPayloadMapping() throws IOException {
        final Resource incomingMappingFunction = new Resource("TextPayloadMapping/incoming.js");
        final PayloadMappingFunction underTest = PayloadMappingFunction.fromJavaScript(incomingMappingFunction.getContent());

        final Map<String, String> headers = new HashMap<>();
        headers.put("content-type", ContentTypes.APPLICATION_JSON.toString());
        headers.put("device_id", "the-thing-id");


        final Resource incomingMessageJson = new Resource("TextPayloadMapping/incoming.json");
        final ExternalMessage incomingMessage = ExternalMessageFactory.newExternalMessageBuilder(headers)
                .withText(incomingMessageJson.getContent())
                .build();

        final Resource expectedAdaptableJsonResource = new Resource("TextPayloadMapping/expectedAdaptable.json");
        final JsonObject expectedAdaptableJson = JsonFactory.newObject(expectedAdaptableJsonResource.getContent());
        final Adaptable expectedAdaptable = ProtocolFactory
                .jsonifiableAdaptableFromJson(expectedAdaptableJson)
                .setDittoHeaders(DittoHeaders.of(headers));

        PayloadMappingTestCase.assertThat(incomingMessage)
                .mappedByJavascriptPayloadMappingFunction(underTest)
                .isEqualTo(expectedAdaptable)
                .verify();

    }

    @Test
    public void outgoingTextPayloadMapping() throws IOException {
        final Resource outgoingMappingFunction = new Resource("TextPayloadMapping/outgoing.js");
        final PayloadMappingFunction underTest = PayloadMappingFunction.fromJavaScript(outgoingMappingFunction.getContent());

        final Map<String, String> headers = new HashMap<>();
        headers.put("content-type", ExternalMessage.PayloadType.TEXT.name());
        headers.put("device_id", "the-device-id");

        final ExternalMessage expectedExternalMessage = ExternalMessageFactory.newExternalMessageBuilder(headers)
                .withText("helloappendix")
                .build();

        final Resource outgoingJson = new Resource("TextPayloadMapping/outgoing.json");
        final JsonObject adaptableJson = JsonFactory.newObject(outgoingJson.getContent());
        final Adaptable adaptable = ProtocolFactory
                .jsonifiableAdaptableFromJson(adaptableJson)
                .setDittoHeaders(DittoHeaders.of(headers));

        PayloadMappingTestCase.assertThat(adaptable)
                .mappedByJavascriptPayloadMappingFunction(underTest)
                .isEqualTo(expectedExternalMessage)
                .verify();
    }
}
