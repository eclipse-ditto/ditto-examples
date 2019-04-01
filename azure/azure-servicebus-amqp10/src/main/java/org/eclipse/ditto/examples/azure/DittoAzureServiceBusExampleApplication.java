/*
 * Copyright (c) 2019 Microsoft
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/index.php
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.ditto.examples.azure;

import javax.jms.ConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jms.DefaultJmsListenerContainerFactoryConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.config.JmsListenerContainerFactory;

@SpringBootApplication
@EnableJms
public class DittoAzureServiceBusExampleApplication {
  private static final Logger LOG =
      LoggerFactory.getLogger(DittoAzureServiceBusExampleApplication.class);

  public static void main(final String[] args) {
    SpringApplication.run(DittoAzureServiceBusExampleApplication.class, args);
  }

  @Bean
  public JmsListenerContainerFactory<?> myFactory(final ConnectionFactory connectionFactory,
      final DefaultJmsListenerContainerFactoryConfigurer configurer) {
    final DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();

    // anonymous class
    factory.setErrorHandler(t -> LOG.error("An error has occurred in the transaction", t));

    configurer.configure(factory, connectionFactory);
    factory.setSessionTransacted(false);
    return factory;
  }
}
