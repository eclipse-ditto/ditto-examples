/*
 * Copyright (c) 2021 Contributors to the Eclipse Foundation
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
package org.eclipse.examples.azure;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.eclipse.ditto.json.JsonObject;
import org.eclipse.ditto.json.JsonPointer;
import org.eclipse.ditto.model.base.common.HttpStatusCode;
import org.eclipse.ditto.model.base.headers.DittoHeaders;
import org.eclipse.ditto.model.things.Feature;
import org.eclipse.ditto.model.things.ThingId;
import org.eclipse.ditto.protocoladapter.Payload;
import org.eclipse.ditto.protocoladapter.ProtocolFactory;
import org.eclipse.ditto.protocoladapter.TopicPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.microsoft.azure.sdk.iot.device.DeviceClient;
import com.microsoft.azure.sdk.iot.device.DeviceTwin.DeviceMethodCallback;
import com.microsoft.azure.sdk.iot.device.DeviceTwin.DeviceMethodData;
import com.microsoft.azure.sdk.iot.device.IotHubEventCallback;
import com.microsoft.azure.sdk.iot.device.IotHubMessageResult;
import com.microsoft.azure.sdk.iot.device.IotHubStatusCode;
import com.microsoft.azure.sdk.iot.device.Message;

public final class ClientSampleOperations {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClientSampleOperations.class.getSimpleName());
    private static final ConfigProperties PROPERTIES = ConfigProperties.getInstance();

    private ClientSampleOperations() {
        throw new IllegalStateException();
    }

    static void sendSampleEvent(final DeviceClient client) {
        if (PROPERTIES.getNamespace().isPresent() && PROPERTIES.getThingId().isPresent()) {
            LOGGER.debug("Executing: <sendSampleEvent>.");
            client.sendEventAsync(createEventMessage(), new EventCallback(), new Object());
        } else {
            LOGGER.warn("Skipping sample event sending, because namespace or thingId config is empty.");
        }
    }

    static void receiveSampleMessages(final DeviceClient client) {
        LOGGER.debug("Listening for messages.");
        client.setMessageCallback(new MessageCallback(), new Object());
    }

    static void receiveDirectMethodInvocations(final DeviceClient client) throws IOException {
        LOGGER.debug("Listening for method invocations.");
        client.subscribeToDeviceMethod(new MethodCallback(), client, new EventCallback(), new Object());
    }

    private static Message createEventMessage() {
        final var messageBody =
                ProtocolFactory.wrapAsJsonifiableAdaptable(ProtocolFactory.newAdaptableBuilder(getSampleTopicPath())
                        .withPayload(getSamplePayload()).build()).toJson(DittoHeaders.empty()).toString();
        return new Message(messageBody.getBytes(StandardCharsets.UTF_8));
    }

    private static TopicPath getSampleTopicPath() {
        return ProtocolFactory.newTopicPathBuilder(
                ThingId.of(PROPERTIES.getNamespace().orElseThrow(), PROPERTIES.getThingId().orElseThrow()))
                .things()
                .twin()
                .commands()
                .build();
    }

    private static Payload getSamplePayload() {
        return ProtocolFactory.newPayloadBuilder(JsonPointer.of("/features/hub"))
                .withValue(JsonObject.newBuilder()
                        .set(Feature.JsonFields.PROPERTIES, JsonObject.newBuilder().set("switch", true).build())
                        .build())
                .build();
    }

    private static void rebootDevice(final DeviceClient client) throws IOException {
        LOGGER.info("Initiating reboot.");
        client.closeNow();
        LOGGER.info("Closed client.");
        client.open();
        LOGGER.info("Reopened client.");
    }

    private static final class EventCallback implements IotHubEventCallback {

        private static final Logger LOGGER = LoggerFactory.getLogger(ClientSampleOperations.class.getSimpleName());

        @Override
        public void execute(final IotHubStatusCode iotHubStatusCode, final Object o) {
            LOGGER.info("Published Event and got status: <{}>.", iotHubStatusCode);
        }
    }

    private static class MessageCallback implements com.microsoft.azure.sdk.iot.device.MessageCallback {

        private static final Logger LOGGER = LoggerFactory.getLogger(MessageCallback.class.getSimpleName());

        @Override
        public IotHubMessageResult execute(final Message message, final Object o) {
            LOGGER.info("Received message with content: <{}>.", message);

            return IotHubMessageResult.COMPLETE;
        }
    }

    private static class MethodCallback implements DeviceMethodCallback {

        @Override
        public DeviceMethodData call(final String methodName, final Object methodData, final Object context) {
            LOGGER.info("Received invocation of method: <{}>, with body: <{}>", methodName, methodData);

            if (methodName.equals("reboot")) {
                try {
                    rebootDevice((DeviceClient) context);
                } catch (final IOException e) {
                    e.printStackTrace();
                }
                return new DeviceMethodData(HttpStatusCode.OK.toInt(), "Reboot triggered.");
            }
            return new DeviceMethodData(HttpStatusCode.BAD_REQUEST.toInt(), "No such method.");
        }
    }

}
