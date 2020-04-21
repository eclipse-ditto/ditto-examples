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
package org.eclipse.ditto.examples.kata.config;

import static org.eclipse.ditto.model.base.common.ConditionChecker.argumentNotEmpty;

import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

/**
 * This class provides easy access to well-known configuration properties.
 */
@Immutable
public final class ConfigProperties {

    /**
     * Default name of the configuration properties file.
     */
    public static final String CONFIG_PROPS_FILENAME = "config.properties";

    private final Properties properties;

    private ConfigProperties(final Properties properties) {
        this.properties = properties;
    }

    /**
     * Returns an instance of ConfigProperties based on the content of a file named {@value #CONFIG_PROPS_FILENAME}.
     *
     * @return the instance.
     */
    public static ConfigProperties getInstance() {
        return getInstance(CONFIG_PROPS_FILENAME);
    }

    /**
     * Returns an instance of ConfigProperties based on the content of the file with the provided name.
     *
     * @param propertiesFileName name of the file which contains the configuration properties.
     * @return the instance.
     * @throws NullPointerException if {@code propertiesFileName} is {@code null}.
     * @throws IllegalArgumentException if {@code propertiesFileName} is empty.
     * @throws ConfigError if a required config property cannot be found.
     */
    public static ConfigProperties getInstance(final CharSequence propertiesFileName) {
        final PropertiesSupplier propertiesSupplier = new PropertiesSupplier(propertiesFileName);
        final Properties properties = propertiesSupplier.get();
        validateRequiredProperties(properties);

        return new ConfigProperties(properties);
    }

    private static void validateRequiredProperties(final Map<Object, Object> properties) {
        for (final ConfigProperty knownProperty : ConfigProperty.values()) {
            final String propertyKey = knownProperty.getPropertyKey();
            if (knownProperty.isRequired() && !properties.containsKey(propertyKey)) {
                final String p = "The provided config properties do not contain a value for the required <{0}>!";
                throw new ConfigError(MessageFormat.format(p, propertyKey));
            }
        }
    }

    public boolean isClientAuth() {
        return null != getPropertyValue(ConfigProperty.CLIENT_ID);
    }

    public String getClientIdOrThrow() {
        return getPropertyValueOrThrow(ConfigProperty.CLIENT_ID);
    }

    public List<String> getClientScopes() {
        final Function<String, List<String>> parseClientScopes =
                propertyValue -> Arrays.stream(propertyValue.split(","))
                        .map(String::trim)
                        .collect(Collectors.toList());

        return getOptionalPropertyValue(ConfigProperty.CLIENT_SCOPES)
                .map(parseClientScopes)
                .orElseGet(Collections::emptyList);
    }

    public String getClientSecretOrThrow() {
        return getPropertyValueOrThrow(ConfigProperty.CLIENT_SECRET);
    }

    public String getClientTokenEndpointOrThrow() {
        return getPropertyValueOrThrow(ConfigProperty.CLIENT_TOKEN_ENDPOINT);
    }

    public boolean isDummyAuth() {
        return null != getPropertyValue(ConfigProperty.DUMMY_AUTH_USERNAME);
    }

    public String getDummyAuthUsernameOrThrow() {
        return getPropertyValueOrThrow(ConfigProperty.DUMMY_AUTH_USERNAME);
    }

    private String getPropertyValueOrThrow(final ConfigProperty knownProperty) {
        final String result = getPropertyValue(knownProperty);
        if (null == result) {
            final String mPtrn = "No value configured for key <{0}>!";
            throw new ConfigError(MessageFormat.format(mPtrn, knownProperty.getPropertyKey()));
        }
        return result;
    }

    private Optional<String> getOptionalPropertyValue(final ConfigProperty knownProperty) {
        return Optional.ofNullable(getPropertyValue(knownProperty));
    }

    @Nullable
    private String getPropertyValue(final ConfigProperty knownProperty) {
        return properties.getProperty(knownProperty.getPropertyKey());
    }

    public String getEndpoint() {
        return getPropertyValue(ConfigProperty.ENDPOINT);
    }

    public String getNamespace() {
        return getPropertyValue(ConfigProperty.NAMESPACE);
    }

    public String getPasswordOrThrow() {
        return getPropertyValueOrThrow(ConfigProperty.PASSWORD);
    }

    public boolean isBasicAuth() {
        return null != getPropertyValue(ConfigProperty.USERNAME);
    }

    public String getUsernameOrThrow() {
        return getPropertyValueOrThrow(ConfigProperty.USERNAME);
    }

    public Optional<String> getProxyHost() {
        return getOptionalPropertyValue(ConfigProperty.PROXY_HOST);
    }

    public Optional<Integer> getProxyPort() {
        final ConfigProperty proxyPortConfigProperty = ConfigProperty.PROXY_PORT;
        @SuppressWarnings("java:S4276")
        final Function<String, Integer> parsePortNumber = propertyValue -> {
            try {
                return Integer.parseInt(propertyValue);
            } catch (final IllegalArgumentException e) {
                final String messagePattern = "Failed to parse value <{0}> for <{1}> as integer!";
                final String propertyKey = proxyPortConfigProperty.getPropertyKey();
                final String message = MessageFormat.format(messagePattern, propertyValue, propertyKey);
                throw new ConfigError(message, e);
            }
        };

        return getOptionalPropertyValue(proxyPortConfigProperty)
                .map(parsePortNumber);
    }

    public Optional<String> getProxyPassword() {
        return getOptionalPropertyValue(ConfigProperty.PROXY_PASSWORD);
    }

    public Optional<String> getProxyUsername() {
        return getOptionalPropertyValue(ConfigProperty.PROXY_USERNAME);
    }

    private static final class PropertiesSupplier implements Supplier<Properties> {

        private final String propertiesFileName;

        private PropertiesSupplier(final CharSequence propertiesFileName) {
            this.propertiesFileName = argumentNotEmpty(propertiesFileName, "propertiesFileName").toString();
        }

        @Override
        public Properties get() {
            return tryToReadProperties();
        }

        private Properties tryToReadProperties() {
            try {
                return readProperties();
            } catch (final IOException e) {
                final String messagePattern = "Failed to read config properties file <{0}>!";
                throw new ConfigError(MessageFormat.format(messagePattern, propertiesFileName), e);
            }
        }

        private Properties readProperties() throws IOException {
            final Properties result = new Properties(System.getProperties());
            try (final InputStream configPropsStream = getPropertiesFileInputStream()) {
                result.load(configPropsStream);
            }
            return result;
        }

        private InputStream getPropertiesFileInputStream() {
            final ClassLoader classLoader = PropertiesSupplier.class.getClassLoader();
            final InputStream result = classLoader.getResourceAsStream(propertiesFileName);
            if (null == result) {
                final String messagePattern = "Resource with name <{0}> cannot be found!";
                throw new ConfigError(MessageFormat.format(messagePattern, propertiesFileName));
            }
            return result;
        }

    }

    private enum Requisite {
        REQUIRED,
        OPTIONAL
    }

    private enum ConfigProperty {

        CLIENT_ID("auth.client.id", Requisite.OPTIONAL),
        CLIENT_SCOPES("auth.client.scopes", Requisite.OPTIONAL),
        CLIENT_SECRET("auth.client.secret", Requisite.OPTIONAL),
        CLIENT_TOKEN_ENDPOINT("auth.client.tokenEndpoint", Requisite.OPTIONAL),
        DUMMY_AUTH_USERNAME("auth.dummy.username", Requisite.OPTIONAL),
        ENDPOINT("endpoint", Requisite.REQUIRED),
        NAMESPACE("namespace", Requisite.REQUIRED),
        PASSWORD("auth.basic.password", Requisite.OPTIONAL),
        PROXY_HOST("proxy.host", Requisite.OPTIONAL),
        PROXY_PORT("proxy.port", Requisite.OPTIONAL),
        PROXY_PASSWORD("proxy.password", Requisite.OPTIONAL),
        PROXY_USERNAME("proxy.username", Requisite.OPTIONAL),
        USERNAME("auth.basic.username", Requisite.OPTIONAL);

        private final String propertyKey;
        private final Requisite requisite;

        private ConfigProperty(final String propertyKey, final Requisite requisite) {
            this.propertyKey = propertyKey;
            this.requisite = requisite;
        }

        public String getPropertyKey() {
            return propertyKey;
        }

        public boolean isRequired() {
            return Requisite.REQUIRED == requisite;
        }

    }

}
