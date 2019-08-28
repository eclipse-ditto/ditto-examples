/*
 * Copyright (c) 2019 Contributors to the Eclipse Foundation
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
package org.eclipse.ditto.examples.common;

import static java.util.Optional.of;
import static org.eclipse.ditto.model.things.ThingBuilder.generateRandomThingId;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.eclipse.ditto.client.DittoClient;
import org.eclipse.ditto.client.DittoClientFactory;
import org.eclipse.ditto.client.configuration.CommonConfiguration;
import org.eclipse.ditto.client.configuration.CredentialsAuthenticationConfiguration;
import org.eclipse.ditto.client.configuration.MessageSerializerConfiguration;
import org.eclipse.ditto.client.configuration.ProxyConfiguration;
import org.eclipse.ditto.client.live.messages.MessageSerializerRegistry;
import org.eclipse.ditto.client.live.messages.MessageSerializers;
import org.eclipse.ditto.client.messaging.MessagingProviders;
import org.eclipse.ditto.client.messaging.websocket.WsProviderConfiguration;
import org.eclipse.ditto.examples.common.model.ExampleUser;
import org.eclipse.ditto.model.base.auth.AuthorizationSubject;
import org.eclipse.ditto.model.base.json.JsonSchemaVersion;

/**
 * Reads configuration properties and instantiates {@link org.eclipse.ditto.client.DittoClient}s.
 */
public abstract class ExamplesBase {

    private static final String CONFIG_PROPERTIES_FILE = "config.properties";

    private final String endpoint;
    private final String proxyHost;
    private final String proxyPort;
    private final String proxyPrincipal;
    private final String proxyPassword;

    protected final String namespace;

    protected final AuthorizationSubject authorizationSubject1;
    protected final DittoClient client1;
    protected final AuthorizationSubject authorizationSubject2;
    protected final DittoClient client2;

    protected ExamplesBase() {
        final Properties props = loadConfigurationFromFile();

        endpoint = props.getProperty("endpoint");
        proxyHost = props.getProperty("proxyHost");
        proxyPort = props.getProperty("proxyPort");
        proxyPrincipal = props.getProperty("proxyPrincipal");
        proxyPassword = props.getProperty("proxyPassword");

        namespace = props.getProperty("namespace");

        authorizationSubject1 = AuthorizationSubject.newInstance("nginx:" + props.getProperty("username1"));
        client1 = buildClient(props.getProperty("username1"), props.getProperty("password1"));
        authorizationSubject2 = AuthorizationSubject.newInstance("nginx:" + props.getProperty("username2"));
        client2 = buildClient(props.getProperty("username2"), props.getProperty("password2"));
    }

    protected String randomThingId() {
        return namespace + generateRandomThingId();
    }

    protected void startConsumeChanges(final DittoClient client) {
        try {
            client.twin().startConsumption().get(10, TimeUnit.SECONDS);
        } catch (final InterruptedException | ExecutionException | TimeoutException e) {
            throw new IllegalStateException("Error subscribing to change events.", e);
        }
    }

    private DittoClient buildClient(final String username, final String password) {
        final CommonConfiguration twinConfiguration = buildConfiguration(username, password);
        final CommonConfiguration liveConfiguration =
                buildConfiguration(username, password, buildMessageSerializerConfiguration());
        return DittoClientFactory.newInstance(twinConfiguration, liveConfiguration);
    }

    private CommonConfiguration buildConfiguration(final String username, final String password) {
        return buildConfiguration(username, password, MessageSerializerConfiguration.newInstance());
    }

    private CommonConfiguration buildConfiguration(final String username, final String password,
            final MessageSerializerConfiguration serializerConfiguration) {
        final WsProviderConfiguration providerConfiguration = MessagingProviders.dittoWebsocketProviderBuilder()
                .endpoint(endpoint)
                .authenticationConfiguration(CredentialsAuthenticationConfiguration.newBuilder()
                        .username(username)
                        .password(password)
                        .build())
                .reconnectionEnabled(false)
                .build();

        final CommonConfiguration.OptionalConfigurationStep configuration =
                DittoClientFactory.configurationBuilder()
                        .providerConfiguration(providerConfiguration)
                        .serializerConfiguration(serializerConfiguration)
                        .schemaVersion(JsonSchemaVersion.V_1);

        proxyConfiguration().ifPresent(configuration::proxyConfiguration);

        return configuration.build();
    }

    /**
     * Sets up a serializer/deserializer for the {@link org.eclipse.ditto.examples.common.model.ExampleUser} model class which uses JAXB in order to serialize
     * and deserialize messages which should directly be mapped to this type.
     */
    private MessageSerializerConfiguration buildMessageSerializerConfiguration() {
        final JAXBContext jaxbContext;
        try {
            jaxbContext = JAXBContext.newInstance(ExampleUser.class);
        } catch (final JAXBException e) {
            throw new RuntimeException("Could not setup JAXBContext", e);
        }

        final MessageSerializerConfiguration configuration = MessageSerializerConfiguration.newInstance();
        final MessageSerializerRegistry serializerRegistry = configuration.getMessageSerializerRegistry();
        serializerRegistry.registerMessageSerializer(
                MessageSerializers.of(ExampleUser.USER_CUSTOM_CONTENT_TYPE, ExampleUser.class, "*",
                        (exampleUser, charset) -> {
                            try {
                                final Marshaller marshaller = jaxbContext.createMarshaller();
                                marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
                                final ByteArrayOutputStream os = new ByteArrayOutputStream();
                                marshaller.marshal(exampleUser, os);
                                return ByteBuffer.wrap(os.toByteArray());
                            } catch (final JAXBException e) {
                                throw new RuntimeException("Could not serialize", e);
                            }
                        }, (byteBuffer, charset) -> {
                            try {
                                final Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
                                final ByteArrayInputStream is = new ByteArrayInputStream(byteBuffer.array());
                                return (ExampleUser) jaxbUnmarshaller.unmarshal(is);
                            } catch (final JAXBException e) {
                                throw new RuntimeException("Could not deserialize", e);
                            }
                        }));

        return configuration;
    }

    private Optional<ProxyConfiguration> proxyConfiguration() {
        if (proxyHost != null && proxyPort != null) {
            final ProxyConfiguration.ProxyOptionalSettable builder = ProxyConfiguration.newBuilder()
                    .proxyHost(proxyHost)
                    .proxyPort(Integer.parseInt(proxyPort));
            if (proxyPrincipal != null && proxyPassword != null) {
                builder.proxyUsername(proxyPrincipal).proxyPassword(proxyPassword);
            }
            return of(builder.build());
        }
        return Optional.empty();
    }

    private Properties loadConfigurationFromFile() {
        final Properties props = new Properties(System.getProperties());
        try (final InputStream in = getClass().getClassLoader().getResourceAsStream(CONFIG_PROPERTIES_FILE)) {
            props.load(in);
        } catch (final IOException ioe) {
            throw new IllegalStateException(
                    "File " + CONFIG_PROPERTIES_FILE + " could not be opened but is required for this example: "
                            + ioe.getMessage());
        }
        return props;
    }

    /**
     * Destroys the client and waits for its graceful shutdown.
     */
    public void terminate() {
        client1.destroy();
        client2.destroy();
    }

}
