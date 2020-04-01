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

import static org.eclipse.ditto.model.base.common.ConditionChecker.checkNotNull;

import java.util.Optional;
import java.util.function.Supplier;

import javax.annotation.concurrent.Immutable;

import org.eclipse.ditto.client.configuration.BasicAuthenticationConfiguration;
import org.eclipse.ditto.client.configuration.ClientCredentialsAuthenticationConfiguration;
import org.eclipse.ditto.client.configuration.ClientCredentialsAuthenticationConfiguration.ClientCredentialsAuthenticationConfigurationBuilder;
import org.eclipse.ditto.client.configuration.DummyAuthenticationConfiguration;
import org.eclipse.ditto.client.configuration.ProxyConfiguration;
import org.eclipse.ditto.client.messaging.AuthenticationProvider;
import org.eclipse.ditto.client.messaging.AuthenticationProviders;

import com.neovisionaries.ws.client.WebSocket;

import com.bosch.iot.things.kata.config.ConfigProperties;
import com.bosch.iot.things.kata.config.ConfigError;

/**
 * This class provides the AuthenticationProvider for WebSocket connection which is required for building a DittoClient.
 */
@Immutable
final class WebSocketAuthenticationProviderSupplier implements Supplier<AuthenticationProvider<WebSocket>> {

    private final ConfigProperties configProperties;

    private WebSocketAuthenticationProviderSupplier(final ConfigProperties configProperties) {
        this.configProperties = checkNotNull(configProperties, "configProperties");
    }

    /**
     * Returns an instance of WebSocketAuthenticationProviderSupplier.
     *
     * @param configProperties the configuration properties from which the AuthenticationProvider is derived.
     * @return the instance.
     * @throws NullPointerException if {@code configProperties} is {@code null}.
     */
    static WebSocketAuthenticationProviderSupplier getInstance(final ConfigProperties configProperties) {
        return new WebSocketAuthenticationProviderSupplier(configProperties);
    }

    @Override
    public AuthenticationProvider<WebSocket> get() {
        final AuthenticationProvider<WebSocket> result;
        if (configProperties.isDummyAuth()) {
            result = getDummyAuthenticationProvider();
        } else if (configProperties.isBasicAuth()) {
            result = getBasicAuthenticationProvider();
        } else if (configProperties.isClientAuth()) {
            result = getClientCredentialsAuthenticationProvider();
        } else {
            throw new ConfigError("No authentication mechanism configured at all!");
        }
        return result;
    }

    private AuthenticationProvider<WebSocket> getDummyAuthenticationProvider() {
        return AuthenticationProviders.dummy(DummyAuthenticationConfiguration.newBuilder()
                .dummyUsername(configProperties.getDummyAuthUsernameOrThrow())
                .build());
    }

    private AuthenticationProvider<WebSocket> getBasicAuthenticationProvider() {
        return AuthenticationProviders.basic(BasicAuthenticationConfiguration.newBuilder()
                .username(configProperties.getUsernameOrThrow())
                .password(configProperties.getPasswordOrThrow())
                .build());
    }

    private AuthenticationProvider<WebSocket> getClientCredentialsAuthenticationProvider() {
        final ClientCredentialsAuthenticationConfigurationBuilder configurationBuilder =
                ClientCredentialsAuthenticationConfiguration.newBuilder()
                        .clientId(configProperties.getClientIdOrThrow())
                        .clientSecret(configProperties.getClientSecretOrThrow())
                        .scopes(configProperties.getClientScopes())
                        .tokenEndpoint(configProperties.getClientTokenEndpointOrThrow());

        final ProxyConfigurationSupplier proxyConfigSupplier = ProxyConfigurationSupplier.getInstance(configProperties);
        final Optional<ProxyConfiguration> proxyConfiguration = proxyConfigSupplier.get();
        proxyConfiguration.ifPresent(configurationBuilder::proxyConfiguration);

        return AuthenticationProviders.clientCredentials(configurationBuilder.build());
    }

}
