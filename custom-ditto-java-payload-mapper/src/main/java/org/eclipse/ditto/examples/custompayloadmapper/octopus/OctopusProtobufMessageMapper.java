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

import akka.actor.ActorSystem;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Timestamp;
import com.typesafe.config.Config;
import org.eclipse.ditto.base.model.common.CharsetDeterminer;
import org.eclipse.ditto.base.model.headers.DittoHeaders;
import org.eclipse.ditto.base.model.headers.DittoHeadersBuilder;
import org.eclipse.ditto.base.model.headers.contenttype.ContentType;
import org.eclipse.ditto.connectivity.api.ExternalMessage;
import org.eclipse.ditto.connectivity.api.ExternalMessageFactory;
import org.eclipse.ditto.connectivity.model.MessageMappingFailedException;
import org.eclipse.ditto.connectivity.service.mapping.AbstractMessageMapper;
import org.eclipse.ditto.connectivity.service.mapping.MessageMapper;
import org.eclipse.ditto.examples.custompayloadmapper.octopus.protobuf.*;
import org.eclipse.ditto.internal.utils.akka.logging.DittoLogger;
import org.eclipse.ditto.internal.utils.akka.logging.DittoLoggerFactory;
import org.eclipse.ditto.json.JsonObject;
import org.eclipse.ditto.json.JsonPointer;
import org.eclipse.ditto.json.JsonValue;
import org.eclipse.ditto.json.JsonValueContainer;
import org.eclipse.ditto.protocol.Adaptable;
import org.eclipse.ditto.protocol.Payload;
import org.eclipse.ditto.protocol.TopicPath;
import org.eclipse.ditto.protocol.TopicPathBuilder;
import org.eclipse.ditto.things.model.ThingId;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Custom, protobuf based {@link org.eclipse.ditto.connectivity.service.mapping.MessageMapper} mapping protobuf payloads
 * from/to DittoProtocol {@link Adaptable}s.
 */
public final class OctopusProtobufMessageMapper extends AbstractMessageMapper {

    public static final String MAPPER_ALIAS = "CustomOctopusProtobuf";

    private static final DittoLogger LOGGER = DittoLoggerFactory.getLogger(OctopusProtobufMessageMapper.class);

    OctopusProtobufMessageMapper(final ActorSystem actorSystem, final Config config) {
        super(actorSystem, config);
        LOGGER.info("!!! Started custom OctopusProtobufMessageMapper !!!");
    }

    OctopusProtobufMessageMapper(final OctopusProtobufMessageMapper copyFromMapper) {
        super(copyFromMapper);
        LOGGER.info("Copied custom OctopusProtobufMessageMapper");
    }

    @Override
    public String getAlias() {
        return MAPPER_ALIAS;
    }

    @Override
    public boolean isConfigurationMandatory() {
        return false;
    }

    @Override
    public MessageMapper createNewMapperInstance() {
        return new OctopusProtobufMessageMapper(this);
    }

    @Override
    public List<Adaptable> map(final ExternalMessage externalMessage) {

        LOGGER.withCorrelationId(externalMessage.getInternalHeaders())
                .debug("Mapping received ExternalMessage: <{}>", externalMessage);

        final OctopusOutboundMessage octopusMessage = externalMessage.getBytePayload()
                .map(data -> {
                    try {
                        return OctopusOutboundMessage.parseFrom(data);
                    } catch (final InvalidProtocolBufferException e) {
                        throw MessageMappingFailedException.newBuilder(externalMessage.findContentType().orElse("?"))
                                .cause(e)
                                .build();
                    }
                })
                .orElseGet(() -> {
                            try {
                                return OctopusOutboundMessage.parseFrom(
                                        ByteString.copyFrom(externalMessage.getTextPayload().get(),
                                                externalMessage.findContentType()
                                                        .map(ct -> CharsetDeterminer.getInstance().apply(ct))
                                                        .orElse(StandardCharsets.UTF_8)));
                            } catch (final InvalidProtocolBufferException e) {
                                throw MessageMappingFailedException.newBuilder(externalMessage.findContentType().orElse("?"))
                                        .cause(e)
                                        .build();
                            }
                        }
                );

        final String deviceId = octopusMessage.getDeviceId();
        final TopicPathBuilder topicPathBuilder = TopicPath.newBuilder(ThingId.of(deviceId));
        final Timestamp ts = octopusMessage.getTimestamp();

        final List<Adaptable> adaptables = switch (octopusMessage.getPayloadCase()) {
            case DATA -> List.of(
                    buildBME680DataAdaptableFromDevice(externalMessage, topicPathBuilder, ts, octopusMessage.getData(),
                            octopusMessage.getCurrentVoltage())
            );
            case EVENT -> List.of(
                    buildEventMessageFromDevice(externalMessage, topicPathBuilder, ts, octopusMessage.getEvent())
            );
            case PAYLOAD_NOT_SET -> List.of();
        };

        LOGGER.withCorrelationId(externalMessage.getInternalHeaders())
                .debug("Mapped ExternalMessage to Adaptables: <{}>", adaptables);
        return adaptables;
    }

    private static Adaptable buildBME680DataAdaptableFromDevice(final ExternalMessage externalMessage,
                                                                final TopicPathBuilder topicPathBuilder,
                                                                final Timestamp timestamp,
                                                                final BME680Data bme680Data,
                                                                final float currentVoltage) {

        final TopicPath topicPath = topicPathBuilder.things()
                .twin()
                .commands()
                .merge()
                .build();

        final DittoHeadersBuilder<?, ?> dittoHeadersBuilder = externalMessage.getInternalHeaders()
                .toBuilder()
                .contentType(ContentType.APPLICATION_MERGE_PATCH_JSON);
        dittoHeadersBuilder.putHeaders(externalMessage.getHeaders());

        return Adaptable.newBuilder(topicPath)
                .withHeaders(dittoHeadersBuilder.build())
                .withPayload(Payload.newBuilder()
                        .withPath(JsonPointer.of("/features"))
                        .withValue(JsonObject.newBuilder()
                                .set("/voltage/properties/value", currentVoltage)
                                .set("/temperature/properties/value", bme680Data.getTemperature())
                                .set("/humidity/properties/value", bme680Data.getHumidity())
                                .set("/pressure/properties/value", bme680Data.getPressure())
                                .set("/gas_resistance/properties/value", bme680Data.getGasResistance())
                                .set("/altitude/properties/value", bme680Data.getAltitude())
                                .build()
                        )
                        .build()
                )
                .build();
    }

    private static Adaptable buildEventMessageFromDevice(final ExternalMessage externalMessage,
                                                         final TopicPathBuilder topicPathBuilder,
                                                         final Timestamp timestamp,
                                                         final Event event) {

        final TopicPath topicPath = topicPathBuilder.live()
                .messages()
                .subject(event.getName())
                .build();

        final DittoHeadersBuilder<?, ?> dittoHeadersBuilder = externalMessage.getInternalHeaders()
                .toBuilder();
        dittoHeadersBuilder.putHeaders(externalMessage.getHeaders());

        return Adaptable.newBuilder(topicPath)
                .withHeaders(dittoHeadersBuilder.build())
                .withPayload(Payload.newBuilder()
                        .withPath(JsonPointer.of(
                                "/features/temperature/outbox/messages/" + event.getName()
                        ))
                        .withTimestamp(Instant.ofEpochSecond(timestamp.getSeconds(), timestamp.getNanos()))
                        .withValue(JsonValue.of(event.getPayload()))
                        .build()
                )
                .build();
    }

    @Override
    public DittoHeaders getAdditionalInboundHeaders(final ExternalMessage externalMessage) {
        return DittoHeaders.empty();
    }

    @Override
    public List<ExternalMessage> map(final Adaptable adaptable) {

        LOGGER.withCorrelationId(adaptable)
                .debug("Mapping outbound Adaptable: <{}>", adaptable);

        final List<ExternalMessage> externalMessages;
        if (adaptable.getTopicPath().isCriterion(TopicPath.Criterion.MESSAGES)) {
            externalMessages = List.of(buildActionMessageToDevice(adaptable));
        } else if (adaptable.getTopicPath().isCriterion(TopicPath.Criterion.COMMANDS)) {
            if (adaptable.getPayload().getPath().toString().equals("/features/configuration")) {
                externalMessages = List.of(buildConfigurationMessageToDevice(adaptable));
            } else {
                externalMessages = List.of();
            }
        } else {
            externalMessages = List.of();
        }

        LOGGER.withCorrelationId(adaptable)
                .debug("Mapped Adaptable to ExternalMessages: <{}>", externalMessages);
        return externalMessages;
    }

    private static ExternalMessage buildActionMessageToDevice(final Adaptable adaptable) {

        final OctopusInboundMessage octopusInboundMessage = OctopusInboundMessage.newBuilder()
                .setDeviceId(
                        adaptable.getTopicPath().getNamespace() + ":" + adaptable.getTopicPath().getEntityName()
                )
                .setAction(Action.newBuilder()
                        .setRequiringResponse(adaptable.getDittoHeaders().isResponseRequired())
                        .setName(adaptable.getTopicPath().getSubject().orElseThrow())
                        .setPayload(adaptable.getPayload().getValue().orElse(JsonValue.nullLiteral()).formatAsString())
                        .build()
                )
                .build();

        return ExternalMessageFactory.newExternalMessageBuilder(adaptable.getDittoHeaders())
                .withBytes(octopusInboundMessage.toByteArray())
                .withTopicPath(adaptable.getTopicPath())
                .build();
    }

    private static ExternalMessage buildConfigurationMessageToDevice(final Adaptable adaptable) {

        final Optional<JsonObject> configurationProperties = adaptable.getPayload().getValue()
                .filter(JsonValue::isObject)
                .map(JsonValue::asObject)
                .flatMap(obj -> obj.getValue("properties"))
                .filter(JsonValue::isObject)
                .map(JsonValue::asObject);
        final Configuration.Builder configurationBuilder = Configuration.newBuilder();
        configurationProperties.stream().flatMap(JsonValueContainer::stream)
                .forEach(configField -> configurationBuilder.putConfigEntry(
                        configField.getKeyName(),
                        configField.getValue().formatAsString()
                ));
        final OctopusInboundMessage octopusInboundMessage = OctopusInboundMessage.newBuilder()
                .setDeviceId(
                        adaptable.getTopicPath().getNamespace() + ":" + adaptable.getTopicPath().getEntityName()
                )
                .setConfig(configurationBuilder.build())
                .build();

        return ExternalMessageFactory.newExternalMessageBuilder(adaptable.getDittoHeaders())
                .withBytes(octopusInboundMessage.toByteArray())
                .withTopicPath(adaptable.getTopicPath())
                .build();
    }
}
