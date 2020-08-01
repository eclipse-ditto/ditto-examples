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

package org.eclipse.ditto.examples.influxdb.config;

import java.util.concurrent.ExecutionException;
import org.eclipse.ditto.client.DittoClient;
import org.eclipse.ditto.client.DittoClients;
import org.eclipse.ditto.client.configuration.BasicAuthenticationConfiguration;
import org.eclipse.ditto.client.configuration.WebSocketMessagingConfiguration;
import org.eclipse.ditto.client.messaging.AuthenticationProvider;
import org.eclipse.ditto.client.messaging.AuthenticationProviders;
import org.eclipse.ditto.client.messaging.MessagingProvider;
import org.eclipse.ditto.client.messaging.MessagingProviders;
import org.eclipse.ditto.client.options.Options;
import org.eclipse.ditto.model.base.json.JsonSchemaVersion;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import com.neovisionaries.ws.client.WebSocket;

@Service
public class DittoConfig {

  @Value("${ditto.endpoint}")
  String endpoint;

  @Value("${ditto.username}")
  String username;

  @Value("${ditto.password}")
  String password;

  @Value("${ditto.namespace}")
  String namespace;

  @Bean
  public DittoClient dittoClient() throws InterruptedException, ExecutionException {
    AuthenticationProvider<WebSocket> authenticationProvider = AuthenticationProviders
        .basic(BasicAuthenticationConfiguration.newBuilder().username(username).password(password).build());

    MessagingProvider messagingProvider = MessagingProviders.webSocket(WebSocketMessagingConfiguration.newBuilder()
        .endpoint(endpoint).jsonSchemaVersion(JsonSchemaVersion.V_2).build(), authenticationProvider);

    DittoClient client = DittoClients.newInstance(messagingProvider);

    client.twin().startConsumption(Options.Consumption.namespaces(namespace)).get();

    return client;
  }

}
