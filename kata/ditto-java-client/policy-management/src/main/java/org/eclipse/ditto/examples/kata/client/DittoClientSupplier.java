/*
 * Copyright (c) 2020 Contributors to the Eclipse Foundation
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
package org.eclipse.ditto.examples.kata.client;

import static java.util.Objects.requireNonNull;

import java.util.Optional;
import java.util.function.Supplier;

import javax.annotation.concurrent.Immutable;

import org.eclipse.ditto.client.DittoClient;
import org.eclipse.ditto.client.DittoClients;
import org.eclipse.ditto.client.configuration.MessagingConfiguration;
import org.eclipse.ditto.client.configuration.ProxyConfiguration;
import org.eclipse.ditto.client.configuration.WebSocketMessagingConfiguration;
import org.eclipse.ditto.client.messaging.AuthenticationProvider;
import org.eclipse.ditto.client.messaging.MessagingProvider;
import org.eclipse.ditto.client.messaging.MessagingProviders;
import org.eclipse.ditto.examples.kata.config.ConfigProperties;
import org.eclipse.ditto.model.base.json.JsonSchemaVersion;

import com.neovisionaries.ws.client.WebSocket;

/**
 * This class provides a {@link DittoClient} based on the given {@link org.eclipse.ditto.examples.kata.config.ConfigProperties}.
 */
@Immutable
public final class DittoClientSupplier implements Supplier<DittoClient> {

    private final ConfigProperties configProperties;

    private DittoClientSupplier(final ConfigProperties configProperties) {
        this.configProperties = requireNonNull(configProperties, "configProperties");
    }

    /**
     * Returns an instance of DittoClientSupplier.
     *
     * @param configProperties the configuration properties to be used for building a DittoClient.
     * @return the instance.
     * @throws NullPointerException if {@code configProperties} is {@code null}.
     */
    public static DittoClientSupplier getInstance(final ConfigProperties configProperties) {
        return new DittoClientSupplier(configProperties);
    }

    @Override
    public DittoClient get() {
        return DittoClients.newInstance(getMessagingProvider());
    }

    private MessagingProvider getMessagingProvider() {
        return MessagingProviders.webSocket(getMessagingConfiguration(), getAuthenticationProvider());
    }

    private MessagingConfiguration getMessagingConfiguration() {
        final MessagingConfiguration.Builder messagingConfigBuilder = WebSocketMessagingConfiguration.newBuilder()
                .jsonSchemaVersion(JsonSchemaVersion.V_2)
                .reconnectEnabled(false)
                .endpoint(configProperties.getEndpoint());

        final ProxyConfigurationSupplier proxyConfigSupplier = ProxyConfigurationSupplier.getInstance(configProperties);
        final Optional<ProxyConfiguration> proxyConfiguration = proxyConfigSupplier.get();
        proxyConfiguration.ifPresent(messagingConfigBuilder::proxyConfiguration);

        return messagingConfigBuilder.build();
    }

    private AuthenticationProvider<WebSocket> getAuthenticationProvider() {
        return WebSocketAuthenticationProviderSupplier.getInstance(configProperties).get();
    }

}
