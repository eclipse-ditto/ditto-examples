/*
 * Copyright (c) 2019 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional information regarding copyright
 * ownership.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse
 * Public License 2.0 which is available at http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.ditto.examples.azure;

import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Session;
import javax.jms.TextMessage;
import org.awaitility.Awaitility;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.stereotype.Component;
import com.google.common.base.CharMatcher;
import com.google.common.base.Charsets;
import com.google.common.io.Resources;

@Component
@ConditionalOnProperty(prefix = "ditto.sample.producer", name = "enabled", matchIfMissing = true)
public class MessageProducer implements CommandLineRunner {

  @Value("${ditto.sample.queue:dittoinbound}")
  private String destination;

  @Value("${ditto.sample.response-queue:dittoresponses}")
  private String response;

  private static final String PROP_DITTO_SAMPLE_RESPONSE_QUEUE =
      "${ditto.sample.response-queue:dittoresponses}";

  @Autowired
  private JmsTemplate jmsTemplate;

  private final Set<String> sendMessages = Collections.synchronizedSet(new HashSet<>());

  @Override
  public void run(final String... args) throws Exception {
    for (int i = 0; i < 10; i++) {
      final String thingAndCorrId = UUID.randomUUID().toString();
      sendMessageFromFile("createThingSample.json", thingAndCorrId);
      sendMessageFromFile("deleteThingSample.json", thingAndCorrId);
    }

    Awaitility.await().atMost(1, TimeUnit.MINUTES).pollDelay(50, TimeUnit.MILLISECONDS)
        .pollInterval(50, TimeUnit.MILLISECONDS).until(sendMessages::isEmpty);

    System.out.println("Responses from Ditto complete!");
  }

  private void sendMessageFromFile(final String fileName, final String correlationId)
      throws IOException {
    final URL file = Resources.getResource(fileName);
    final String content =
        CharMatcher.whitespace().removeFrom(Resources.toString(file, Charsets.UTF_8))
            .replace("fancy-car-test", correlationId);

    jmsTemplate.send(destination, (MessageCreator) session -> {
      final TextMessage message = session.createTextMessage(content);

      setResponse(session, message, correlationId);
      sendMessages.add(correlationId);

      return message;
    });


    System.out.println("Sent message to Ditto: " + content);
  }

  private void setResponse(final Session session, final TextMessage message,
      final String correlationId) throws JMSException {
    final Destination responseDestination = jmsTemplate.getDestinationResolver()
        .resolveDestinationName(session, response, true);

    message.setJMSCorrelationID(correlationId);
    message.setJMSReplyTo(responseDestination);
  }

  @JmsListener(destination = PROP_DITTO_SAMPLE_RESPONSE_QUEUE, containerFactory = "myFactory")
  public void processDittoResponse(final TextMessage message) throws JMSException {

    sendMessages.remove(message.getJMSCorrelationID());

    System.out.println("Response from Ditto " + message.getBody(String.class));
  }
}
