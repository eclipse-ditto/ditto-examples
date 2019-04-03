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

import org.eclipse.ditto.json.JsonFactory;
import org.eclipse.ditto.json.JsonValue;
import org.eclipse.ditto.protocoladapter.Adaptable;
import org.eclipse.ditto.protocoladapter.DittoProtocolAdapter;
import org.eclipse.ditto.protocoladapter.ProtocolFactory;
import org.eclipse.ditto.signals.base.Signal;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.cloud.stream.messaging.Sink;

@SpringBootApplication
@EnableBinding(Sink.class)
public class DittoAzureEventHubExampleApplication {

  private static final DittoProtocolAdapter DITTO_PROTOCOL_ADAPTER =
      DittoProtocolAdapter.newInstance();

  public static void main(final String[] args) {
    SpringApplication.run(DittoAzureEventHubExampleApplication.class, args);
  }

  @StreamListener(Sink.INPUT)
  void getEvent(final String event) {

    final JsonValue jsonValue = JsonFactory.readFrom(event.substring(8));
    final Adaptable adaptable = ProtocolFactory.jsonifiableAdaptableFromJson(jsonValue.asObject());

    final Signal<?> actual = DITTO_PROTOCOL_ADAPTER.fromAdaptable(adaptable);

    System.out.println("Got signal from Ditto: " + actual);

  }
}
