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
import java.net.URISyntaxException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.microsoft.azure.sdk.iot.device.DeviceClient;

public final class DittoAzureIoTHubExampleApplication {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(DittoAzureIoTHubExampleApplication.class.getSimpleName());

    private static final ConfigProperties PROPERTIES = ConfigProperties.getInstance();

    public static void main(final String[] args) throws URISyntaxException, IOException {

        final DeviceClient client = createClient();
        final DittoAzureIoTHubClient dittoAzureIotHubClient = DittoAzureIoTHubClient.newInstance(PROPERTIES, client);

        dittoAzureIotHubClient.registerMessageCallback();
        client.open();
        LOGGER.info("Initiated client.");
        dittoAzureIotHubClient.registerDirectMethodCallback();
        dittoAzureIotHubClient.sendExampleEvent();
    }

    private static DeviceClient createClient() throws URISyntaxException {
        return new DeviceClient(PROPERTIES.getConnectionString(), PROPERTIES.getConnectionProtocol());
    }

}
