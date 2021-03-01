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

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.microsoft.azure.sdk.iot.device.IotHubClientProtocol;

public final class ConfigProperties {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigProperties.class.getSimpleName());
    private static final String CONFIG_PROPERTIES_FILE = "config.properties";
    private static ConfigProperties instance;

    private final String connectionString;
    private final String namespace;
    private final String thingId;
    private final IotHubClientProtocol connectionProtocol;
    private final String proxyHost;
    private final Integer proxyPort;
    private final String proxyUsername;
    private final String proxyPassword;

    private ConfigProperties(final String connectionString,
            final String namespace,
            final String thingId,
            final IotHubClientProtocol connectionProtocol,
            final String proxyHost,
            final Integer proxyPort,
            final String proxyUsername,
            final String proxyPassword) {

        this.connectionString = connectionString;
        this.namespace = namespace;
        this.thingId = thingId;
        this.connectionProtocol = connectionProtocol;
        this.proxyHost = proxyHost;
        this.proxyPort = proxyPort;
        this.proxyUsername = proxyUsername;
        this.proxyPassword = proxyPassword;
    }

    public static ConfigProperties getInstance() {
        if (null == instance) {
            final Properties props = readPropertiesFromFile();

            final var connectionString = getConnectionStringFromConfig(props);
            final var namespace = props.getProperty("namespace");
            final var thingId = props.getProperty("thing_id");
            final var connectionProtocol = getConnectionProtocolFromConfig(props);
            final var proxyHost = getProxyHostFromConfig(props);
            final var proxyPort = getProxyPortFromConfig(props);
            final var proxyUsername = props.getProperty("proxy_username");
            final var proxyPassword = props.getProperty("proxy_password");

            instance =
                    new ConfigProperties(connectionString, namespace, thingId, connectionProtocol, proxyHost, proxyPort,
                            proxyUsername, proxyPassword);
            LOGGER.debug("Loaded config properties.");
        }
        return instance;
    }

    private static Properties readPropertiesFromFile() {
        final Properties props = new Properties(System.getProperties());

        try (final InputStream in = ConfigProperties.class.getClassLoader()
                .getResourceAsStream(CONFIG_PROPERTIES_FILE)) {
            props.load(in);
        } catch (final IOException ioe) {
            throw new IllegalStateException(
                    "File " + CONFIG_PROPERTIES_FILE + " could not be opened but is required for this example: "
                            + ioe.getMessage());
        }

        return props;
    }

    private static String getConnectionStringFromConfig(final Properties props) {
        final var connectionString = props.getProperty("connection_string");
        requireNonNull(connectionString, "connection_string");
        return connectionString;
    }

    private static IotHubClientProtocol getConnectionProtocolFromConfig(final Properties props) {
        final var protocolString = props.getProperty("connection_protocol");
        requireNonNull(protocolString, "connection_protocol");
        return IotHubClientProtocol.valueOf(protocolString);
    }

    private static String getProxyHostFromConfig(final Properties props) {
        final var proxyHost = props.getProperty("proxy_host");
        requireNonNull(proxyHost, "proxy_host");
        return proxyHost;
    }

    private static Integer getProxyPortFromConfig(final Properties props) {
        final var portString = props.getProperty("proxy_port");
        if (portString != null) {
            return Integer.parseInt(portString);
        }
        return null;
    }

    String getConnectionString() {
        return connectionString;
    }

    Optional<String> getNamespace() {
        return Optional.ofNullable(namespace);
    }

    Optional<String> getThingId() {
        return Optional.ofNullable(thingId);
    }

    IotHubClientProtocol getConnectionProtocol() {
        return connectionProtocol;
    }

    Optional<String> getProxyHost() {
        return Optional.ofNullable(proxyHost);
    }

    Optional<Integer> getProxyPort() {
        return Optional.ofNullable(proxyPort);
    }

    Optional<String> getProxyUsername() {
        return Optional.ofNullable(proxyUsername);
    }

    Optional<String> getProxyPassword() {
        return Optional.ofNullable(proxyPassword);
    }

}
