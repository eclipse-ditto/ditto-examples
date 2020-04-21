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
package org.eclipse.ditto.examples.claiming;

import static org.eclipse.ditto.model.things.AccessControlListModelFactory.allPermissions;

import java.time.OffsetDateTime;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.ditto.client.live.LiveThingHandle;
import org.eclipse.ditto.client.live.messages.RepliableMessage;
import org.eclipse.ditto.examples.common.ExamplesBase;
import org.eclipse.ditto.json.JsonFactory;
import org.eclipse.ditto.model.base.common.HttpStatusCode;
import org.eclipse.ditto.model.things.AccessControlListModelFactory;
import org.eclipse.ditto.model.things.AclEntry;
import org.eclipse.ditto.model.things.Thing;
import org.eclipse.ditto.model.things.ThingId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This example shows how to register for- and reply to claim messages with the {@link org.eclipse.ditto.client.DittoClient}.
 */
public final class RegisterForClaimMessages extends ExamplesBase {

    private static final Logger LOGGER = LoggerFactory.getLogger(RegisterForClaimMessages.class);

    private final String registrationIdAllClaimMessages;
    private final String registrationIdClaimMessagesForThing;

    private RegisterForClaimMessages() {
        super();
        registrationIdAllClaimMessages = UUID.randomUUID().toString();
        registrationIdClaimMessagesForThing = UUID.randomUUID().toString();

        try {
            registerForClaimMessagesToSingleThing();
        } catch (final InterruptedException | ExecutionException | TimeoutException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(e);
        } finally {
            terminate();
        }
    }

    public static void main(final String... args) {
        new RegisterForClaimMessages();
    }

    /**
     * Registers for claim messages sent to all things.
     * To claim the prepared Thing, you can use our swagger documentation provided at
     * https://ditto.eclipse.org/apidoc/ or any other REST client.
     */
    private void registerForClaimMessagesToAllThings() {
        prepareClaimableThing()
                .thenAccept(thingHandle -> {
                    client1.live().registerForClaimMessage(registrationIdAllClaimMessages, this::handleMessage);
                    LOGGER.info("Thing '{}' ready to be claimed", thingHandle.getThingEntityId());
                });
    }

    /**
     * Registers for claim messages sent to a single Thing.
     * To claim the prepared Thing, you can use our swagger documentation provided at
     * https://ditto.eclipse.org/apidoc/ or any other REST client.
     */
    private void registerForClaimMessagesToSingleThing()
            throws InterruptedException, ExecutionException, TimeoutException {
        client1.live().startConsumption().get(10, TimeUnit.SECONDS);
        prepareClaimableThing()
                .thenAccept(thingHandle -> {
                    thingHandle.registerForClaimMessage(registrationIdClaimMessagesForThing, this::handleMessage);
                    LOGGER.info("Thing '{}' ready to be claimed!", thingHandle.getThingEntityId());
                });
    }

    private CompletableFuture<LiveThingHandle> prepareClaimableThing() {
        final ThingId thingId = randomThingId();
        return client1.twin().create(thingId)
                .thenCompose(created -> {
                    final Thing updated = created.toBuilder()
                            .setPermissions(authorizationSubject, allPermissions())
                            .build();
                    return client1.twin().update(updated);
                })
                .thenApply(created -> client1.live().forId(thingId));
    }

    private void handleMessage(final RepliableMessage<?, Object> message) {

        final ThingId thingId = message.getThingEntityId();

        client1.twin().forId(thingId)
                .retrieve()
                .thenCompose(thing -> client1.twin().update(thing.setAttribute("myAttribute","testValue")))
                .whenComplete((aVoid, throwable) -> {
                    if (null != throwable) {
                        message.reply()
                                .statusCode(HttpStatusCode.BAD_GATEWAY)
                                .timestamp(OffsetDateTime.now())
                                .payload("Error: Claiming failed. Please try again later.")
                                .contentType("text/plain")
                                .send();
                        LOGGER.info("Update failed: '{}'", throwable.getMessage());
                    } else {
                        message.reply()
                                .statusCode(HttpStatusCode.OK)
                                .timestamp(OffsetDateTime.now())
                                .payload(JsonFactory.newObjectBuilder().set("success", true).build())
                                .contentType("application/json")
                                .send();
                        LOGGER.info("Thing '{}' claimed from authorization subject '{}'", thingId,
                                authorizationSubject);
                    }
                });
    }
}
