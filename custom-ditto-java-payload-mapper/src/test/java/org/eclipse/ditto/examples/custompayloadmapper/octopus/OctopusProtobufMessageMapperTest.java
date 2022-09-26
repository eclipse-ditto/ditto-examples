/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
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
package org.eclipse.ditto.examples.custompayloadmapper.octopus;

import org.eclipse.ditto.connectivity.api.ExternalMessage;
import org.eclipse.ditto.connectivity.api.ExternalMessageFactory;
import org.eclipse.ditto.examples.custompayloadmapper.octopus.protobuf.BME680Data;
import org.eclipse.ditto.examples.custompayloadmapper.octopus.protobuf.OctopusOutboundMessage;
import org.eclipse.ditto.internal.utils.akka.logging.DittoLogger;
import org.eclipse.ditto.internal.utils.akka.logging.DittoLoggerFactory;
import org.eclipse.ditto.json.JsonObject;
import org.eclipse.ditto.json.JsonValue;
import org.eclipse.ditto.protocol.Adaptable;
import org.eclipse.ditto.protocol.ProtocolFactory;
import org.eclipse.ditto.things.model.Features;
import org.eclipse.ditto.things.model.ThingId;
import org.eclipse.ditto.things.model.ThingsModelFactory;
import org.junit.Test;

import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public final class OctopusProtobufMessageMapperTest {

    private static final DittoLogger LOGGER = DittoLoggerFactory.getLogger(OctopusProtobufMessageMapperTest.class);

    private static final String NAMESPACE = "org.eclipse.ditto";
    private static final String ENTITY_NAME = "thing-1";
    private static final ThingId THING_ID = ThingId.of(NAMESPACE + ":" + ENTITY_NAME);
    private static final float KNOWN_VOLTAGE = 3.3f;
    private static final double KNOWN_TEMPERATURE = 24.2;
    private static final double KNOWN_HUMIDITY = 48.43;

    private static final double KNOWN_PRESSURE = 1000.2;

    private static final double KNOWN_GAS_RESISTANCE = 0.32;

    private static final double KNOWN_ALTITUDE = 412.3;

    private final OctopusProtobufMessageMapper sut = new OctopusProtobufMessageMapper(null, null);

    @Test
    public void mapMessageFromDeviceCorrectly() throws java.io.IOException {
        final OctopusOutboundMessage octopusOutboundMessage = OctopusOutboundMessage.newBuilder()
                .setDeviceId(THING_ID.toString())
                .setCurrentVoltage(KNOWN_VOLTAGE)
                .setData(BME680Data.newBuilder()
                        .setTemperature(KNOWN_TEMPERATURE)
                        .setHumidity(KNOWN_HUMIDITY)
                        .setPressure(KNOWN_PRESSURE)
                        .setGasResistance(KNOWN_GAS_RESISTANCE)
                        .setAltitude(KNOWN_ALTITUDE)
                        .build()
                ).build();

        final byte[] bytes = octopusOutboundMessage.toByteArray();
        // the following line writes the protobuf message to a local file:
        // octopusOutboundMessage.writeTo(new FileOutputStream("example-protobuf-msg"));
        LOGGER.info("Protobuf byte[] size: " + bytes.length);
        final ExternalMessage externalMessage = ExternalMessageFactory.newExternalMessageBuilder(Map.of())
                .withBytes(bytes)
                .build();

        final List<Adaptable> mappedAdaptables = sut.map(externalMessage);
        assertThat(mappedAdaptables).hasSize(1);

        final Adaptable adaptable = mappedAdaptables.get(0);
        assertThat(adaptable.getTopicPath().getPath())
                .isEqualTo(NAMESPACE + "/" + ENTITY_NAME + "/things/twin/commands/merge");

        final var jsonifiableAdaptable = ProtocolFactory.wrapAsJsonifiableAdaptable(adaptable);
        LOGGER.info("DittoProtocol Adaptable size: " +
                jsonifiableAdaptable.toJsonString().getBytes(StandardCharsets.UTF_8).length);

        assertThat(adaptable.getPayload().getValue())
                .containsInstanceOf(JsonObject.class);
        final Features features =
                ThingsModelFactory.newFeatures(adaptable.getPayload().getValue().orElseThrow().asObject());
        assertThat(features.getFeature("voltage")).isPresent();
        assertThat(features.getFeature("temperature")).isPresent();
        assertThat(features.getFeature("humidity")).isPresent();
        assertThat(features.getFeature("pressure")).isPresent();
        assertThat(features.getFeature("gas_resistance")).isPresent();
        assertThat(features.getFeature("altitude")).isPresent();

        assertThat(features.getFeature("voltage").orElseThrow().getProperty("value"))
                .contains(JsonValue.of(KNOWN_VOLTAGE));
        assertThat(features.getFeature("temperature").orElseThrow().getProperty("value"))
                .contains(JsonValue.of(KNOWN_TEMPERATURE));
        assertThat(features.getFeature("humidity").orElseThrow().getProperty("value"))
                .contains(JsonValue.of(KNOWN_HUMIDITY));
        assertThat(features.getFeature("pressure").orElseThrow().getProperty("value"))
                .contains(JsonValue.of(KNOWN_PRESSURE));
        assertThat(features.getFeature("gas_resistance").orElseThrow().getProperty("value"))
                .contains(JsonValue.of(KNOWN_GAS_RESISTANCE));
        assertThat(features.getFeature("altitude").orElseThrow().getProperty("value"))
                .contains(JsonValue.of(KNOWN_ALTITUDE));
    }
}