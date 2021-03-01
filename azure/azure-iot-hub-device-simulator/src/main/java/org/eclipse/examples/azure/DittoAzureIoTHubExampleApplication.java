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
import java.net.URISyntaxException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.microsoft.azure.sdk.iot.device.DeviceClient;
import com.microsoft.azure.sdk.iot.device.IotHubConnectionStatusChangeCallback;
import com.microsoft.azure.sdk.iot.device.IotHubConnectionStatusChangeReason;
import com.microsoft.azure.sdk.iot.device.ProxySettings;
import com.microsoft.azure.sdk.iot.device.transport.IotHubConnectionStatus;

public final class DittoAzureIoTHubExampleApplication {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(DittoAzureIoTHubExampleApplication.class.getSimpleName());

    public static void main(final String[] args) throws URISyntaxException, IOException {
        final var client = createClient();
        ClientSampleOperations.receiveSampleMessages(client);
        client.open();
        LOGGER.info("Initiated client.");
        ClientSampleOperations.receiveDirectMethodInvocations(client);
        ClientSampleOperations.sendSampleEvent(client);
    }

    private static DeviceClient createClient() throws URISyntaxException {
        final var props = ConfigProperties.getInstance();
        final var client = new DeviceClient(props.getConnectionString(), props.getConnectionProtocol());
        setProxyToDeviceClientIfEnabled(client, props);
        client.registerConnectionStatusChangeCallback(new ConnectionStatusLogger(), new Object());
        return client;
    }

    private static void setProxyToDeviceClientIfEnabled(final DeviceClient client,
            final ConfigProperties props) {

        if (isProxyEnabled(props)) {
            final ProxySettings proxySettings;
            final var proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress("localhost", 3128));
            if (isProxyWithUserAndPW(props)) {
                proxySettings = new ProxySettings(proxy, props.getProxyUsername().orElseThrow(),
                        props.getProxyPassword().orElseThrow().toCharArray());
            } else {
                proxySettings = new ProxySettings(proxy);
            }
            client.setProxySettings(proxySettings);
        }
    }

    private static boolean isProxyEnabled(final ConfigProperties props) {
        return props.getProxyHost().isPresent() && props.getProxyPort().isPresent();
    }

    private static boolean isProxyWithUserAndPW(final ConfigProperties props) {
        return props.getProxyUsername().isPresent() && props.getProxyPassword().isPresent();
    }

    static final class ConnectionStatusLogger implements IotHubConnectionStatusChangeCallback {

        private static final Logger LOGGER = LoggerFactory.getLogger(ConnectionStatusLogger.class.getSimpleName());

        @Override
        public void execute(final IotHubConnectionStatus iotHubConnectionStatus,
                final IotHubConnectionStatusChangeReason iotHubConnectionStatusChangeReason, final Throwable throwable,
                final Object o) {

            if (throwable != null) {
                throwable.printStackTrace();
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
