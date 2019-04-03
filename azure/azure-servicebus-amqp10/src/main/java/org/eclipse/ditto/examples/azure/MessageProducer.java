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
package org.eclipse.ditto.examples.azure;

import java.io.IOException;
import java.net.URL;
import java.util.UUID;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Session;
import javax.jms.TextMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.stereotype.Component;
import com.google.common.base.CharMatcher;
import com.google.common.base.Charsets;
import com.google.common.io.Resources;

@Component
public class MessageProducer implements CommandLineRunner {

  @Autowired
  private JmsTemplate jmsTemplate;

  @Override
  public void run(final String... args) throws Exception {
    sendMessageFromFile("deleteThingSample.json");
    sendMessageFromFile("createThingSample.json");
  }

  private void sendMessageFromFile(String fileName) throws IOException {
    final URL file = Resources.getResource(fileName);
    final String content =
        CharMatcher.whitespace().removeFrom(Resources.toString(file, Charsets.UTF_8));


    jmsTemplate.send("dittoinbound", (MessageCreator) session -> {
      final TextMessage message = session.createTextMessage(content);

      setResponse(session, message);

      return message;
    });

    System.out.println("Sent message to Ditto: " + content);
  }

  private void setResponse(Session session, final TextMessage message) throws JMSException {
    final Destination responseDestination = jmsTemplate.getDestinationResolver()
        .resolveDestinationName(session, "dittoresponses", true);

    message.setJMSCorrelationID(UUID.randomUUID().toString());
    message.setJMSReplyTo(responseDestination);
  }

  @JmsListener(destination = "dittoresponses", containerFactory = "myFactory")
  public void processDittoResponse(final String message) {

    System.out.println("Response from Ditto: " + message);
  }
}
