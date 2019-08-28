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
package org.eclipse.ditto.examples.messages;

import static org.eclipse.ditto.model.things.AccessControlListModelFactory.allPermissions;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.ditto.client.live.LiveFeatureHandle;
import org.eclipse.ditto.client.live.LiveThingHandle;
import org.eclipse.ditto.examples.common.ExamplesBase;
import org.eclipse.ditto.examples.common.model.ExampleUser;
import org.eclipse.ditto.json.JsonFactory;
import org.eclipse.ditto.json.JsonValue;
import org.eclipse.ditto.model.things.Permission;
import org.eclipse.ditto.model.things.Thing;
import org.eclipse.ditto.model.things.ThingsModelFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This examples shows the various possibilities that the {@link org.eclipse.ditto.client.DittoClient} offers to register handlers for {@link
 * org.eclipse.ditto.model.messages.Message}s being sent to/from your {@code Thing}s, and shows how you can send such
 * {@code Message}s using the {@code DittoClient}. NOTE: Make sure to invoke {@code
 * ThingsClient.twin().startConsumption()} once after all message handlers are registered to start receiving events.
 */
public final class RegisterForAndSendMessages extends ExamplesBase {

    private static final Logger LOGGER = LoggerFactory.getLogger(RegisterForAndSendMessages.class);

    private static final String ALL_THINGS_JSON_MESSAGE = "allThings_jsonMessage";
    private static final String ALL_THINGS_RAW_MESSAGE = "allThings_rawMessage";
    private static final String ALL_THINGS_STRING_MESSAGE = "allThings_stringMessage";
    private static final String MY_THING_JSON_MESSAGE = "myThing_jsonMessage";
    private static final String MY_THING_RAW_MESSAGE = "myThing_rawMessage";
    private static final String MY_THING_STRING_MESSAGE = "myThing_stringMessage";
    private static final String CUSTOM_SERIALIZER_EXAMPLE_USER_MESSAGE = "customSerializer_exampleUserMessage";

    private final CountDownLatch countDownLatch;
    private final String fromThingId;
    private final String toThingId;

    private RegisterForAndSendMessages() {
        super();
        countDownLatch = new CountDownLatch(17);
        fromThingId = randomThingId();
        toThingId = randomThingId();

        try {
            LOGGER.info("Creating thing {} as message source.", fromThingId);
            client1.twin().create(fromThingId)
                    .thenCompose(created -> {
                        final Thing updated = created.toBuilder()
                                .setPermissions(authorizationSubject1, allPermissions())
                                .setPermissions(authorizationSubject2, Permission.WRITE)
                                .build();
                        return client1.twin().update(updated);
                    }).get(10, TimeUnit.SECONDS);


            LOGGER.info("Creating thing {} as message sink.", toThingId);
            client1.twin().create(toThingId)
                    .thenCompose(created -> {
                        final Thing updated = created.toBuilder()
                                .setPermissions(authorizationSubject1, ThingsModelFactory.allPermissions())
                                .setPermissions(authorizationSubject2, Permission.WRITE)
                                .build();
                        return client1.twin().update(updated);
                    }).get(10, TimeUnit.SECONDS);

            client1.live().startConsumption().get(10, TimeUnit.SECONDS);
            client2.live().startConsumption().get(10, TimeUnit.SECONDS);

            registerForMessages();
            sendMessages();
        } catch (final InterruptedException | ExecutionException | TimeoutException e) {
            throw new IllegalStateException(e);
        } finally {
            try {
                destroy();
            } catch (final InterruptedException e) {
                LOGGER.error("Error during cleanup", e);
            }
        }
    }

    public static void main(final String... args) {
        new RegisterForAndSendMessages();
    }

    /**
     * Shows various possibilities to register handlers for {@code Message}s of interest.
     */
    private void registerForMessages() {
        /* Register for *all* messages of *all* things and provide payload as String */ /**/
        client1.live().registerForMessage(ALL_THINGS_STRING_MESSAGE, "*", String.class, message -> {
            final String subject = message.getSubject();
            final Optional<String> payload = message.getPayload();
            LOGGER.info("Match all String Messages: message for subject {} with payload {} received", subject, payload);
            countDownLatch.countDown();
        });

        /* Register for *all* messages with subject *jsonMessage* of *all* things and provide payload as JsonValue */
        client1.live().registerForMessage(ALL_THINGS_JSON_MESSAGE, "jsonMessage", JsonValue.class, message -> {
            final String subject = message.getSubject();
            final Optional<JsonValue> payload = message.getPayload();
            LOGGER.info("Match Json Message: message for subject {} with payload {} received", subject, payload);
            countDownLatch.countDown();
        });

        /* Register for messages with subject *rawMessage* of *all* things and provide payload as raw ByteBuffer */
        client1.live().registerForMessage(ALL_THINGS_RAW_MESSAGE, "rawMessage", message -> {
            final String subject = message.getSubject();
            final Optional<ByteBuffer> payload = message.getRawPayload();
            final String payloadAsString = payload.map(p -> StandardCharsets.UTF_8.decode(p).toString()).orElse(null);
            LOGGER.info("Match Raw Message: message for subject {} with payload {} received", subject,
                    payloadAsString);
            countDownLatch.countDown();
        });


        final LiveThingHandle fromThingHandle = client1.live().forId(fromThingId);

        /* Register for *all* messages of a *specific* thing and provide payload as String */
        fromThingHandle.registerForMessage(MY_THING_STRING_MESSAGE, "*", String.class, message -> {
            final String subject = message.getSubject();
            final Optional<String> payload = message.getPayload();
            LOGGER
                    .info("Match all String Messages for fromThingId: message for subject {} with payload {} received",
                            subject,
                            payload);
            countDownLatch.countDown();
        });

        /* Register for *all* messages with subject *myThingJsonMessage* of a *specific* thing of and provide payload as JsonValue */
        /* not used */
        fromThingHandle.registerForMessage(MY_THING_JSON_MESSAGE, "jsonMessage", JsonValue.class, message -> {
            final String subject = message.getSubject();
            final Optional<JsonValue> payload = message.getPayload();
            LOGGER.info("Match Json Messages for fromThingId: message for subject {} with payload {} received", subject,
                    payload);
            countDownLatch.countDown();
        });

        /* Register for *all* messages with subject *myThingRawMessage* of a *specific* thing and provide payload as raw ByteBuffer */
        /* not used */
        fromThingHandle.registerForMessage(MY_THING_RAW_MESSAGE, "rawMessage", message -> {
            final String subject = message.getSubject();
            final Optional<ByteBuffer> payload = message.getRawPayload();
            final String payloadAsString = payload.map(p -> StandardCharsets.UTF_8.decode(p).toString()).orElse(null);
            LOGGER.info("Match Raw Messages for fromThingId: message for subject {} with payload {} received", subject,
                    payloadAsString);
            countDownLatch.countDown();
        });

        /*
         * Custom Message serializer usage:
         */

        /* Register for messages with subject *example.user.created* of *all* things and provide payload as custom type ExampleUser */
        client1.live()
                .registerForMessage(CUSTOM_SERIALIZER_EXAMPLE_USER_MESSAGE, "example.user.created", ExampleUser.class,
                        message -> {
                            final String subject = message.getSubject();
                            final Optional<ExampleUser> user = message.getPayload();
                            LOGGER.info("Match Custom Message: message for subject {} with payload {} received",
                                    subject, user);
                            countDownLatch.countDown();
                        });

    }

    /**
     * Shows how to send a {@code Message} to/from a {@code Thing} using the {@code ThingsClient}.
     */
    private void sendMessages() {
        /* Send a message *from* a thing with the given subject but without any payload */
        client2.live().message()
                .from(fromThingId)
                .subject("some.arbitrary.subject")
                .send();

        /* Send a message *from* a feature with the given subject but without any payload */
        //does not arrive
        client2.live().message()
                .from(fromThingId)
                .featureId("sendFromThisFeature")
                .subject("justWantToLetYouKnow")
                .send();

        /* Send a message *to* a thing with the given subject and text payload */
        /* We won't receive this message because we send it to another Thing Client.*/
        client2.live().message()
                .to(toThingId)
                .subject("monitoring.building.fireAlert")
                .payload("Roof is on fire")
                .contentType("text/plain")
                .send();

        /* Send a message *from* a feature with the given subject and json payload */
        client2.live().message()
                .from(toThingId)
                .featureId("smokeDetector")
                .subject("jsonMessage")
                .payload(JsonFactory.readFrom("{\"action\" : \"call fire department\"}"))
                .contentType("application/json")
                .send();

        /* Send a message *to* a feature with the given subject and raw payload */
        client2.live().message()
                .from(fromThingId)
                .featureId("smokeDetector")
                .subject("rawMessage")
                .payload(ByteBuffer.wrap("foo".getBytes(StandardCharsets.UTF_8)))
                .contentType("application/octet-stream")
                .send();

        final LiveThingHandle thingHandle = client2.live().forId(toThingId);
        /* Send a message *to* a thing (id already defined by the ThingHandle) with the given subject but without any payload */
        thingHandle.message()
                .to()
                .subject("somesubject")
                .send();

        final LiveFeatureHandle featureHandle = client2.live().forFeature(fromThingId, "smokeDetector");
        /* Send a message *from* a feature with the given subject and text payload */
        featureHandle.message()
                .from()
                .subject("somesubject")
                .payload("someContent")
                .contentType("text/plain")
                .send();

        /*
         * Custom Message serializer usage:
         */
        /* Send a message *from* a thing with the given subject and a custom payload type */
        client2.live()
                .message()
                .from(fromThingId)
                .subject("example.user.created")
                .payload(new ExampleUser("karl", "karl@bosch.com"))
                .contentType(ExampleUser.USER_CUSTOM_CONTENT_TYPE)
                .send();
    }

    private void destroy() throws InterruptedException {
        final boolean allMessagesReceived = countDownLatch.await(10, TimeUnit.SECONDS);
        LOGGER.info("All messages received: {}", allMessagesReceived);
        terminate();
    }

}
