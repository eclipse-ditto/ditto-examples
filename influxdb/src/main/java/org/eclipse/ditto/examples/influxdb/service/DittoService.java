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

package org.eclipse.ditto.examples.influxdb.service;

import java.util.concurrent.ExecutionException;
import org.eclipse.ditto.client.DittoClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

@Service
public class DittoService {

  private static final Logger logger = LoggerFactory.getLogger(DittoService.class);

  @Autowired
  DittoClient client;

  @Autowired
  InfluxDBService influxDbService;

  @EventListener(ApplicationReadyEvent.class)
  private void registerForChanges() throws InterruptedException, ExecutionException {

    client.twin().registerForFeaturesChanges("globalFeaturesHandler", change -> {
      logger.info("Received features update from device '{}': {}", change.getEntityId(),
          change.getFeatures().toJsonString());

      change.getFeatures().forEach(f -> influxDbService.save(change.getEntityId().toString(), f.getId(),
          f.getProperty("value").get().asDouble()));
    });
  }

}
