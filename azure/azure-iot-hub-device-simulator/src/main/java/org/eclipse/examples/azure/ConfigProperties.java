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
import java.text.MessageFormat;
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
    private final String thingName;
    private final IotHubClientProtocol connectionProtocol;
    private final String proxyHost;
    private final Integer proxyPort;
    private final String proxyUsername;
    private final String proxyPassword;

    private ConfigProperties(final Properties props) {
        connectionString = getConnectionStringFromConfig(props);
        namespace = props.getProperty("namespace");
        thingName = props.getProperty("thing_id");
        connectionProtocol = getConnectionProtocolFromConfig(props);
        proxyHost = getProxyHostFromConfig(props);
        proxyPort = getProxyPortFromConfig(props);
        proxyUsername = props.getProperty("proxy_username");
        proxyPassword = props.getProperty("proxy_password");
    }

    public static ConfigProperties getInstance() {
        if (null == instance) {
            instance = new ConfigProperties(tryToReadPropertiesFromFile(CONFIG_PROPERTIES_FILE));
            LOGGER.debug("Loaded config properties.");
        }
        return instance;
    }

    private static Properties tryToReadPropertiesFromFile(final String propertiesFileName) {
        try {
            return readPropertiesFromFile(propertiesFileName);
        } catch (final IOException e) {
            final var pattern = "File <{0}> could not be opened but is required for this example: {1}";
            throw new IllegalStateException(
                    MessageFormat.format(pattern, propertiesFileName, e.getMessage()), e);
        }
    }

    private static Properties readPropertiesFromFile(final String fileName) throws IOException {
        final var properties = System.getProperties();
        final var classLoader = ConfigProperties.class.getClassLoader();
        try (final var inputStream = classLoader.getResourceAsStream(fileName)) {
            properties.load(inputStream);
        }
        return properties;
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

    Optional<String> getThingName() {
        return Optional.ofNullable(thingName);
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
