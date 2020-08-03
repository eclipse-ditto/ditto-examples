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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.write.Point;

@Service
public class InfluxDBService {

  @Autowired
  InfluxDBClient client;

  public void save(String deviceId, String featureId, Double value) {
    client.getWriteApi().writePoint(Point.measurement("weather").addField(featureId, value).addTag("deviceId", deviceId));
  }

}
