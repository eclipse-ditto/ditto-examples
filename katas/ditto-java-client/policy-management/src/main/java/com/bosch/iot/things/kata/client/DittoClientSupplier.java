/*
 * Copyright Bosch.IO GmbH 2020
 *
 * All rights reserved, also regarding any disposal, exploitation,
 * reproduction, editing, distribution, as well as in the event of
 * applications for industrial property rights.
 *
 * This software is the confidential and proprietary information
 * of Bosch.IO GmbH. You shall not disclose
 * such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you
 * entered into with Bosch.IO GmbH.
 */
package com.bosch.iot.things.kata.client;

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
import org.eclipse.ditto.model.base.json.JsonSchemaVersion;

import com.neovisionaries.ws.client.WebSocket;

import com.bosch.iot.things.kata.config.ConfigProperties;

/**
 * This class provides a {@link DittoClient} based on the given {@link ConfigProperties}.
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
