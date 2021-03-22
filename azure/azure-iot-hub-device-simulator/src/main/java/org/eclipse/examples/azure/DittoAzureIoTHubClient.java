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
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import javax.annotation.Nullable;

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
import com.microsoft.azure.sdk.iot.device.IotHubConnectionStatusChangeCallback;
import com.microsoft.azure.sdk.iot.device.IotHubConnectionStatusChangeReason;
import com.microsoft.azure.sdk.iot.device.IotHubEventCallback;
import com.microsoft.azure.sdk.iot.device.IotHubMessageResult;
import com.microsoft.azure.sdk.iot.device.IotHubStatusCode;
import com.microsoft.azure.sdk.iot.device.Message;
import com.microsoft.azure.sdk.iot.device.ProxySettings;
import com.microsoft.azure.sdk.iot.device.transport.IotHubConnectionStatus;

public final class DittoAzureIoTHubClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(DittoAzureIoTHubClient.class.getSimpleName());

    private final DeviceClient client;
    private final ConfigProperties properties;

    private DittoAzureIoTHubClient(final ConfigProperties properties, final DeviceClient client) {
        this.properties = properties;
        this.client = client;
    }

    static DittoAzureIoTHubClient newInstance(final ConfigProperties properties, final DeviceClient client) {
        setProxyToDeviceClientIfEnabled(client, properties);
        client.registerConnectionStatusChangeCallback(new ConnectionStatusLogger(), new Object());
        return new DittoAzureIoTHubClient(properties, client);
    }

    void sendExampleEvent() {
        final var optionalNamespace = properties.getNamespace();
        final var optionalThingName = properties.getThingName();

        if (optionalNamespace.isPresent() && optionalThingName.isPresent()) {
            LOGGER.debug("Executing: <sendExampleEvent>.");
            client.sendEventAsync(createEventMessage(optionalNamespace.get(), optionalThingName.get()),
                    new EventCallback(), new Object());
        } else {
            LOGGER.warn("Skipping example event sending, because namespace or thingId config is empty.");
        }
    }

    void registerMessageCallback() {
        LOGGER.debug("Listening for messages.");
        client.setMessageCallback(new MessageCallback(), new Object());
    }

    void registerDirectMethodCallback() throws IOException {
        LOGGER.debug("Listening for method invocations.");
        client.subscribeToDeviceMethod(new MethodCallback(), client, new EventCallback(), new Object());
    }

    private static void setProxyToDeviceClientIfEnabled(final DeviceClient client, final ConfigProperties props) {
        final var optionalProxyHost = props.getProxyHost().orElse(null);
        final var optionalProxyPort = props.getProxyPort().orElse(null);

        if (isProxyEnabled(optionalProxyHost, optionalProxyPort)) {
            LOGGER.debug("Connecting via Proxy: <{}:{}>", optionalProxyHost, optionalProxyPort);
            final ProxySettings proxySettings;
            final var proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(optionalProxyHost, optionalProxyPort));

            if (isProxyWithUserAndPW(props)) {
                proxySettings = new ProxySettings(proxy, props.getProxyUsername().orElseThrow(),
                        props.getProxyPassword().orElseThrow().toCharArray());
            } else {
                proxySettings = new ProxySettings(proxy);
            }

            client.setProxySettings(proxySettings);
        }
    }

    private static boolean isProxyEnabled(@Nullable final String optionalProxyHost,
            final @Nullable Integer optionalProxyPort) {
        return optionalProxyHost != null && optionalProxyPort != null;
    }

    private static boolean isProxyWithUserAndPW(final ConfigProperties props) {
        return props.getProxyUsername().isPresent() && props.getProxyPassword().isPresent();
    }

    private static Message createEventMessage(final String namespace, final String thingName) {
        final var dittoHeaders = DittoHeaders.newBuilder().correlationId("azure-iot-hub-example-event").build();
        final var adaptable = ProtocolFactory.newAdaptableBuilder(getExampleTopicPath(namespace, thingName))
                .withPayload(getExamplePayload())
                .withHeaders(dittoHeaders)
                .build();
        final var jsonifiableAdaptable = ProtocolFactory.wrapAsJsonifiableAdaptable(adaptable);
        final var messageBody = jsonifiableAdaptable.toJsonString();
        return new Message(messageBody.getBytes(StandardCharsets.UTF_8));
    }

    private static TopicPath getExampleTopicPath(final String namespace, final String thingName) {
        return ProtocolFactory.newTopicPathBuilder(
                ThingId.of(namespace, thingName))
                .things()
                .twin()
                .commands()
                .modify()
                .build();
    }

    private static Payload getExamplePayload() {
        return ProtocolFactory.newPayloadBuilder(JsonPointer.of("/features/hub"))
                .withValue(JsonObject.newBuilder()
                        .set(Feature.JsonFields.PROPERTIES, JsonObject.newBuilder().set("switch", false).build())
                        .build())
                .build();
    }

    private static final class EventCallback implements IotHubEventCallback {

        private static final Logger LOGGER = LoggerFactory.getLogger(EventCallback.class.getSimpleName());

        @Override
        public void execute(final IotHubStatusCode iotHubStatusCode, final Object o) {
            LOGGER.info("Published Event and got status: <{}>.", iotHubStatusCode);
        }
    }

    private static class MessageCallback implements com.microsoft.azure.sdk.iot.device.MessageCallback {

        private static final Logger LOGGER = LoggerFactory.getLogger(MessageCallback.class.getSimpleName());

        @Override
        public IotHubMessageResult execute(final Message message, final Object o) {
            LOGGER.info("Received message with content: <{}>.", serializeByteMessage(message.getBytes()));

            return IotHubMessageResult.COMPLETE;
        }

        private static String serializeByteMessage(final byte[] bytes) {
            return Base64.getEncoder().encodeToString(bytes);
        }

    }

    private static class MethodCallback implements DeviceMethodCallback {

        private static final Logger LOGGER = LoggerFactory.getLogger(MethodCallback.class.getSimpleName());

        @Override
        public DeviceMethodData call(final String methodName, final Object methodData, final Object context) {
            LOGGER.info("Received invocation of method: <{}>, with body: <{}>", methodName, methodData);

            if (methodName.equals("reboot")) {
                tryRebootDevice((DeviceClient) context);
                return new DeviceMethodData(HttpStatusCode.OK.toInt(), "Reboot triggered.");
            }
            return new DeviceMethodData(HttpStatusCode.BAD_REQUEST.toInt(), "No such method.");
        }

        private static void tryRebootDevice(final DeviceClient client) {
            try {
                rebootDevice(client);
            } catch (final IOException e) {
                LOGGER.error("Received error, while rebooting the device", e);
            }
        }

        private static void rebootDevice(final DeviceClient client) throws IOException {
            LOGGER.info("Initiating reboot.");
            client.closeNow();
            LOGGER.info("Closed client.");
            client.open();
            LOGGER.info("Reopened client.");
        }

    }

    private static final class ConnectionStatusLogger implements IotHubConnectionStatusChangeCallback {

        private static final Logger LOGGER = LoggerFactory.getLogger(ConnectionStatusLogger.class.getSimpleName());

        @Override
        public void execute(final IotHubConnectionStatus iotHubConnectionStatus,
                final IotHubConnectionStatusChangeReason iotHubConnectionStatusChangeReason,
                final Throwable throwable,
                final Object o) {

            if (throwable != null) {
                LOGGER.error("Encountered error in connection", throwable);
            }

            if (iotHubConnectionStatus == IotHubConnectionStatus.DISCONNECTED) {
                LOGGER.error("The connection was lost, and is not being re-established." +
                        " Look at provided exception for how to resolve this issue." +
                        " Cannot send messages until this issue is resolved, and you manually re-open the device client.");
            } else if (iotHubConnectionStatus == IotHubConnectionStatus.DISCONNECTED_RETRYING) {
                LOGGER.warn("The connection was lost, but is being re-established." +
                        " Can still send messages, but they won't be sent until the connection is re-established.");
            } else if (iotHubConnectionStatus == IotHubConnectionStatus.CONNECTED) {
                LOGGER.info("The connection was successfully established. Can send messages.");
            }
        }

    }

}
