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
        String SCRIPT_PATH = "./javascript/incomingscript.js";
        String CONTENT_TYPE = "TEXT";
        ScriptRunner runner =
                new ScriptRunner.ScriptRunnerBuilder().withContentType(CONTENT_TYPE)
                        .withIncomingScriptOnly(ScriptRunner.readFromFile(SCRIPT_PATH))
                        .build();

        ExternalMessage message = ExternalMessageFactory.newExternalMessageBuilder(DittoHeaders.empty()).withText(
                "hello").build();

        assertThat(runner.adaptableFromJson("./json/modifything.json", DittoHeaders.empty()))
                .isEqualTo(runner.mapExternalMessage(message));
    }

    @Test
    public void testComplexIncomingPayloadMapping() {
        String EXPECTED_MESSAGE_PATH = "./json/expected-bsp1.json";
        String EXTERNAL_MESSAGE_PATH = "./json/incoming-bsp1.json";
        String SCRIPT_PATH = "./javascript/incoming-bsp1.js";
        String CONTENT_TYPE = "application/json";

        ScriptRunner runner =
                new ScriptRunner.ScriptRunnerBuilder().withContentType(CONTENT_TYPE)
                        .withIncomingScriptOnly(ScriptRunner.readFromFile(SCRIPT_PATH))
                        .build();

        // Apply headers -> applied by Eclipse Hono in a real world scenario
        final Map<String, String> headers = new HashMap<>();
        headers.put("content-type", "application/json");
        headers.put("device_id", "the-thing-id");
        DittoHeaders dittoHeaders = DittoHeaders.of(headers);

        // Build a valid message out of the incoming external message
        ExternalMessage message =
                ExternalMessageFactory.newExternalMessageBuilder(dittoHeaders)
                        .withText(ScriptRunner.readFromFile(EXTERNAL_MESSAGE_PATH))
                        .build();

        // Apply javascript mapping on the incoming external message:
        Adaptable externalMessage = runner.mapExternalMessage(message);

        // Build Adaptable from external message:
        Adaptable expectedMessage = runner.adaptableFromJson(EXPECTED_MESSAGE_PATH, dittoHeaders);

        // Compare the mapped incoming message with the expected result:
        assertThat(externalMessage).isEqualTo(expectedMessage);
    }

    @Test
    public void testOutgoingPayloadMapping() {
        String SCRIPT_PATH = "./javascript/outgoingscript.js";
        String CONTENT_TYPE = "application/json";

        ScriptRunner runner =
                new ScriptRunner.ScriptRunnerBuilder().withContentType(CONTENT_TYPE)
                        .withOutgoingScriptOnly(SCRIPT_PATH)
                        .build();

        DittoHeaders headers = DittoHeaders.newBuilder().contentType(CONTENT_TYPE).build();

        ExternalMessage message = ExternalMessageFactory.newExternalMessageBuilder(headers).withText(
                "hello").build();

        assertThat(runner.mapAdaptable(runner.adaptableFromJson(
                "./json/modifything.json", DittoHeaders.empty())))
                .isEqualTo(message);
    }

}
