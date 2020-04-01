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

import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

import org.eclipse.ditto.client.configuration.ProxyConfiguration;

import com.bosch.iot.things.kata.config.ConfigProperties;

/**
 * This class provides the {@link ProxyConfiguration} for building a DittoClient.
 */
@Immutable
final class ProxyConfigurationSupplier implements Supplier<Optional<ProxyConfiguration>> {

    private final ConfigProperties configProperties;

    private ProxyConfigurationSupplier(final ConfigProperties configProperties) {
        this.configProperties = requireNonNull(configProperties, "configProperties");
    }

    /**
     * Returns an instance of ProxyConfigurationSupplier.
     *
     * @param configProperties the configuration properties from which the proxy configuration is derived.
     * @return the instance.
     * @throws NullPointerException if {@code configProperties} is {@code null}.
     */
    static ProxyConfigurationSupplier getInstance(final ConfigProperties configProperties) {
        return new ProxyConfigurationSupplier(configProperties);
    }

    @Override
    public Optional<ProxyConfiguration> get() {
        @Nullable ProxyConfiguration proxyConfiguration = null;

        final Optional<String> proxyHost = configProperties.getProxyHost();
        final Optional<Integer> proxyPort = configProperties.getProxyPort();
        if (proxyHost.isPresent() && proxyPort.isPresent()) {
            final ProxyConfiguration.ProxyOptionalSettable proxyOptionalSettable = ProxyConfiguration.newBuilder()
                    .proxyHost(proxyHost.get())
                    .proxyPort(proxyPort.get());

            final Optional<String> proxyUsername = configProperties.getProxyUsername();
            final Optional<String> proxyPassword = configProperties.getProxyPassword();
            if (proxyUsername.isPresent() && proxyPassword.isPresent()) {
                proxyOptionalSettable.proxyUsername(proxyUsername.get()).proxyPassword(proxyPassword.get());
            }
            proxyConfiguration = proxyOptionalSettable.build();
        }

        return Optional.ofNullable(proxyConfiguration);
    }

}
