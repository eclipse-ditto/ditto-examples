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

package org.eclipse.ditto.examples.scriptrunner.examples;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.ditto.examples.scriptrunner.ScriptRunner;
import org.eclipse.ditto.model.base.headers.DittoHeaders;
import org.eclipse.ditto.protocoladapter.Adaptable;
import org.eclipse.ditto.services.models.connectivity.ExternalMessage;
import org.eclipse.ditto.services.models.connectivity.ExternalMessageFactory;
import org.junit.Test;

import akka.http.javadsl.model.HttpHeader;
import akka.http.javadsl.model.headers.RawHeader;

public class ScriptRunnerExamples {

    // Helper functions
    private DittoHeaders createHeaders(String contentType) {
        final Map<String, String> headers = new HashMap<>();
        headers.put("content-type", contentType);
        headers.put("device_id", "the-thing-id");
        return DittoHeaders.of(headers);
    }

    @Test
    public void testIncomingPayloadMapping() {
        String SCRIPT_PATH = "./javascript/incomingscript.js";
        String CONTENT_TYPE = "TEXT";
        String JSON_FOR_ADAPTABLE = "./json/modifything.json";
        ScriptRunner runner =
                new ScriptRunner.ScriptRunnerBuilder().withContentType(CONTENT_TYPE)
                        .withIncomingScriptOnly(ScriptRunner.readFromFile(SCRIPT_PATH))
                        .build();

        ExternalMessage message = ExternalMessageFactory.newExternalMessageBuilder(DittoHeaders.empty()).withText(
                "hello").build();

        assertThat(runner.adaptableFromJson(ScriptRunner.readFromFile(JSON_FOR_ADAPTABLE), DittoHeaders.empty()))
                .isEqualTo(runner.mapExternalMessage(message));
    }

    @Test
    public void testIncomingPayloadMapping_textPayload() {
        String EXPECTED_MESSAGE_PATH = "./json/expected-bsp1.json";
        String EXTERNAL_MESSAGE_PATH = "./json/incoming-bsp1.json";
        String SCRIPT_PATH = "./javascript/incoming-bsp1.js";
        String CONTENT_TYPE = "application/json";

        ScriptRunner runner =
                new ScriptRunner.ScriptRunnerBuilder().withContentType(CONTENT_TYPE)
                        .withIncomingScriptOnly(ScriptRunner.readFromFile(SCRIPT_PATH))
                        .build();

        // Apply headers -> applied by Eclipse Hono in a real world scenario
        DittoHeaders dittoHeaders = createHeaders(CONTENT_TYPE);

        // Build a valid message out of the incoming external message
        ExternalMessage message =
                ExternalMessageFactory.newExternalMessageBuilder(dittoHeaders)
                        .withText(ScriptRunner.readFromFile(EXTERNAL_MESSAGE_PATH))
                        .build();

        // Apply javascript mapping on the incoming external message:
        Adaptable externalMessage = runner.mapExternalMessage(message);

        // Build Adaptable from external message:
        Adaptable expectedMessage = runner.adaptableFromJson(ScriptRunner.readFromFile(EXPECTED_MESSAGE_PATH), dittoHeaders);

        // Compare the mapped incoming message with the expected result:
        assertThat(externalMessage).isEqualTo(expectedMessage);
    }

    @Test
    public void testIncomingPayloadMapping_bytePayload() {
        String SCRIPT_PATH = "./javascript/incoming-bsp2.js";
        String EXPECTED_JSON = "./json/expected-bsp2.json";
        String CONTENT_TYPE = "application/octet-stream";

        final byte bytePayload[] = new BigInteger("09EF03F72A", 16).toByteArray();

        ScriptRunner runner =
                new ScriptRunner.ScriptRunnerBuilder().withIncomingScriptOnly(ScriptRunner.readFromFile(SCRIPT_PATH))
                        .withContentType(CONTENT_TYPE)
                        .build();

        // Apply headers -> applied by Eclipse Hono in a real world scenario
        DittoHeaders dittoHeaders = createHeaders(CONTENT_TYPE);

        // Build a valid message out of the incoming external message
        ExternalMessage message =
                ExternalMessageFactory.newExternalMessageBuilder(dittoHeaders)
                        .withBytes(bytePayload)
                        .build();

        Adaptable mapped = runner.mapExternalMessage(message);

        Adaptable expected = runner.adaptableFromJson(ScriptRunner.readFromFile(EXPECTED_JSON), dittoHeaders);

        assertThat(mapped).isEqualTo(expected);

    }

    @Test
    public void testOutgoingPayloadMapping() {
        String SCRIPT_PATH = "./javascript/outgoingscript.js";
        String CONTENT_TYPE = "application/json";
        String JSON_FOR_ADAPTABLE = "./json/modifything.json";

        ScriptRunner runner =
                new ScriptRunner.ScriptRunnerBuilder().withContentType(CONTENT_TYPE)
                        .withOutgoingScriptOnly(ScriptRunner.readFromFile(SCRIPT_PATH))
                        .build();

        DittoHeaders headers = DittoHeaders.newBuilder().contentType(CONTENT_TYPE).build();

        ExternalMessage message = ExternalMessageFactory.newExternalMessageBuilder(headers).withText(
                "hello").build();

        assertThat(runner.mapAdaptable(runner.adaptableFromJson(
                ScriptRunner.readFromFile(JSON_FOR_ADAPTABLE), DittoHeaders.empty())))
                .isEqualTo(message);
    }

    @Test
    public void outgoingPayloadMapping_textPayload() {
        String JSON_FOR_ADAPTABLE = "./json/outgoing-bsp1.json";
        String SCRIPT_PATH = "./javascript/outgoing-bsp1.js";
        String CONTENT_TYPE = "TEXT";

        // Apply headers -> e.g. applied by Eclipse Hono in a real world scenario
        DittoHeaders dittoHeaders = createHeaders(CONTENT_TYPE);

        ScriptRunner runner =
                new ScriptRunner.ScriptRunnerBuilder().withContentType(CONTENT_TYPE)
                        .withOutgoingScriptOnly(ScriptRunner.readFromFile(SCRIPT_PATH))
                        .build();

        ExternalMessage expectedExternalMessage =
                ExternalMessageFactory.newExternalMessageBuilder(dittoHeaders).withText(
                        "helloappendix").build();

        Adaptable adaptable = runner.adaptableFromJson(ScriptRunner.readFromFile(JSON_FOR_ADAPTABLE), dittoHeaders);
        ExternalMessage finalMessage = runner.mapAdaptable(adaptable);

        assertThat(expectedExternalMessage).isEqualTo(finalMessage);
    }

    @Test
    public void outgoingPayloadMapping_bytePayload() {
        String SCRIPT_PATH = "./javascript/outgoing-bsp2.js";
        String JSON_FOR_ADAPTABLE = "./json/outgoing-bsp2.json";
        String CONTENT_TYPE = "application/octet-stream";

        // Apply headers -> applied by Eclipse Hono in a real world scenario
        DittoHeaders dittoHeaders = createHeaders(CONTENT_TYPE);

        ScriptRunner runner =
                new ScriptRunner.ScriptRunnerBuilder().withContentType(CONTENT_TYPE)
                        .withOutgoingScriptOnly(ScriptRunner.readFromFile(SCRIPT_PATH))
                        .build();

        byte[] bytes = "HelloBytes".getBytes();

        ExternalMessage expectedExternalMessage =
                ExternalMessageFactory.newExternalMessageBuilder(dittoHeaders).withBytes(bytes).build();

        Adaptable adaptable = runner.adaptableFromJson(ScriptRunner.readFromFile(JSON_FOR_ADAPTABLE), dittoHeaders);

        ExternalMessage finalMessage = runner.mapAdaptable(adaptable);

        assertThat(expectedExternalMessage).isEqualTo(finalMessage);
    }

}
