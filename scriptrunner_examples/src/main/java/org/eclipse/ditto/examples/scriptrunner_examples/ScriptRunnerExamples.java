package org.eclipse.ditto.examples.scriptrunner_examples;

import static org.assertj.core.api.Assertions.assertThat;

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

    @Test
    public void testIncomingPayloadMapping() {
        ScriptRunner runner = new ScriptRunner.ScriptRunnerBuilder().build();

        String SCRIPT_PATH = "./javascript/incomingscript.js";
        String CONTENT_TYPE = "TEXT";

        ExternalMessage message = ExternalMessageFactory.newExternalMessageBuilder(DittoHeaders.empty()).withText(
                "hello").build();

        assertThat(runner.handleDittoProtocolMessageFromJson("./json/modifything.json", DittoHeaders.empty()))
                .isEqualTo(runner.handleExternalMessageWithMappingFromFile(message, SCRIPT_PATH, CONTENT_TYPE));
    }

    @Test
    public void testComplexIncomingPayloadMapping() {
        ScriptRunner runner = new ScriptRunner.ScriptRunnerBuilder().build();

        String EXPECTED_MESSAGE_PATH = "./json/expected-bsp1.json";
        String EXTERNAL_MESSAGE_PATH = "./json/incoming-bsp1.json";
        String SCRIPT_PATH = "./javascript/incoming-bsp1.js";
        String CONTENT_TYPE = "application/json";

        // Apply headers -> applied by Eclipse Hono in a real world scenario
        final Map<String, String> headers = new HashMap<>();
        headers.put("content-type", "application/json");
        headers.put("device_id", "the-thing-id");
        DittoHeaders dittoHeaders = DittoHeaders.of(headers);

        // Build a valid message out of the incoming external message
        ExternalMessage message =
                ExternalMessageFactory.newExternalMessageBuilder(dittoHeaders)
                        .withText(runner.readFromFile(EXTERNAL_MESSAGE_PATH))
                        .build();

        // Apply javascript mapping on the incoming external message:
        Adaptable externalMessage = runner.handleExternalMessageWithMappingFromFile(message, SCRIPT_PATH, CONTENT_TYPE);

        // Build Adaptable from external message:
        Adaptable expectedMessage = runner.handleDittoProtocolMessageFromJson(EXPECTED_MESSAGE_PATH, dittoHeaders);

        // Compare the mapped incoming message with the expected result:
        assertThat(externalMessage).isEqualTo(expectedMessage);
    }

    @Test
    public void testOutgoingPayloadMapping() {
        ScriptRunner runner = new ScriptRunner.ScriptRunnerBuilder().build();

        String SCRIPT_PATH = "./javascript/outgoingscript.js";
        String CONTENT_TYPE = "application/json";

        DittoHeaders headers = DittoHeaders.newBuilder().contentType(CONTENT_TYPE).build();

        ExternalMessage message = ExternalMessageFactory.newExternalMessageBuilder(headers).withText(
                "hello").build();

        assertThat(runner.messageFromAdaptableMappedFromFile(SCRIPT_PATH, runner.handleDittoProtocolMessageFromJson(
                "./json/modifything.json", DittoHeaders.empty()), "application/json"))
                .isEqualTo(message);
    }

}
