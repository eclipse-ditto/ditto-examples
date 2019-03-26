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

public final class TextPayloadMappingTest {

    @Test
    public void incomingTextPayloadMapping() throws IOException {
        final Resource incomingMappingFunction = new Resource("TextPayloadMapping/incoming.js");
        final MappingFunction underTest = MappingFunction.fromJavaScript(incomingMappingFunction.getContent());

        final Map<String, String> headers = new HashMap<>();
        headers.put("content-type", ContentTypes.APPLICATION_JSON.toString());
        headers.put("device_id", "the-thing-id");
        final DittoHeaders dittoHeaders = DittoHeaders.of(headers);

        final Resource incomingMessageJson = new Resource("TextPayloadMapping/incoming.json");
        final ExternalMessage incomingMessage = ExternalMessageFactory.newExternalMessageBuilder(dittoHeaders)
                .withText(incomingMessageJson.getContent())
                .build();

        final Resource expectedAdaptableJsonResource = new Resource("TextPayloadMapping/expectedAdaptable.json");
        final JsonObject expectedAdaptableJson = JsonFactory.newObject(expectedAdaptableJsonResource.getContent());
        final Adaptable expectedAdaptable = ProtocolFactory
                .jsonifiableAdaptableFromJson(expectedAdaptableJson)
                .setDittoHeaders(dittoHeaders);

        MappingFunctionTestCase.forIncomingMappingFunction(underTest)
                .withExpectedMappingResult(expectedAdaptable)
                .whenMapping(incomingMessage)
                .run();
    }

    @Test
    public void outgoingTextPayloadMapping() throws IOException {
        final Resource outgoingMappingFunction = new Resource("TextPayloadMapping/outgoing.js");
        final MappingFunction underTest = MappingFunction.fromJavaScript(outgoingMappingFunction.getContent());

        final Map<String, String> headers = new HashMap<>();
        headers.put("content-type", ExternalMessage.PayloadType.TEXT.name());
        headers.put("device_id", "the-device-id");
        final DittoHeaders dittoHeaders = DittoHeaders.of(headers);

        final ExternalMessage expectedExternalMessage = ExternalMessageFactory.newExternalMessageBuilder(dittoHeaders)
                .withText("helloappendix")
                .build();

        final Resource outgoingJson = new Resource("TextPayloadMapping/outgoing.json");
        final JsonObject adaptableJson = JsonFactory.newObject(outgoingJson.getContent());
        final Adaptable adaptable = ProtocolFactory
                .jsonifiableAdaptableFromJson(adaptableJson)
                .setDittoHeaders(dittoHeaders);

        MappingFunctionTestCase.forOutgoingMappingFunction(underTest)
                .withExpectedMappingResult(expectedExternalMessage)
                .whenMapping(adaptable)
                .run();
    }
}
