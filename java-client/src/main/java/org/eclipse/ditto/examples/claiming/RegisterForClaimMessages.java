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

import org.eclipse.ditto.base.model.common.HttpStatus;
import org.eclipse.ditto.client.live.LiveThingHandle;
import org.eclipse.ditto.client.live.messages.RepliableMessage;
import org.eclipse.ditto.examples.common.ExamplesBase;
import org.eclipse.ditto.json.JsonFactory;
import org.eclipse.ditto.policies.model.*;
import org.eclipse.ditto.things.model.ThingId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

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
     * https://ditto.eclipseprojects.io/apidoc/ or any other REST client.
     */
    private void registerForClaimMessagesToAllThings() {
        prepareClaimableThing()
                .thenAccept(thingHandle -> {
                    client1.live().registerForClaimMessage(registrationIdAllClaimMessages, this::handleMessage);
                    LOGGER.info("Thing '{}' ready to be claimed", thingHandle.getEntityId());
                });
    }

    /**
     * Registers for claim messages sent to a single Thing.
     * To claim the prepared Thing, you can use our swagger documentation provided at
     * https://ditto.eclipseprojects.io/apidoc/ or any other REST client.
     */
    private void registerForClaimMessagesToSingleThing()
            throws InterruptedException, ExecutionException, TimeoutException {
        client1.live().startConsumption().toCompletableFuture().get(10, TimeUnit.SECONDS);
        prepareClaimableThing()
                .thenAccept(thingHandle -> {
                    thingHandle.registerForClaimMessage(registrationIdClaimMessagesForThing, this::handleMessage);
                    LOGGER.info("Thing '{}' ready to be claimed!", thingHandle.getEntityId());
                });
    }

    private CompletionStage<LiveThingHandle> prepareClaimableThing() {
        final ThingId thingId = randomThingId();
        return client1.twin().create(thingId)
                .thenCompose(created -> {
                    final PolicyId policyId = created.getPolicyId().get();
                    return client1.policies().retrieve(policyId)
                            .thenApply(policy -> policy.toBuilder()
                                    .forLabel("NEW")
                                    .setSubject(authorizationSubject.getId(), SubjectType.UNKNOWN)
                                    .setResource(Resource.newInstance(
                                            PoliciesResourceType.thingResource("/"),
                                            EffectedPermissions.newInstance(Arrays.asList("READ", "WRITE"), null)
                                    ))
                                    .build()
                            ).thenCompose(updatedPolicy -> client1.policies().update(updatedPolicy));
                })
                .thenApply(updatedPolicyVoid -> client1.live().forId(thingId));
    }

    private void handleMessage(final RepliableMessage<?, Object> message) {

        final ThingId thingId = message.getEntityId();

        client1.twin().forId(thingId)
                .retrieve()
                .thenCompose(thing -> client1.twin().update(thing.setAttribute("myAttribute","testValue")))
                .whenComplete((aVoid, throwable) -> {
                    if (null != throwable) {
                        message.reply()
                                .httpStatus(HttpStatus.SERVICE_UNAVAILABLE)
                                .timestamp(OffsetDateTime.now())
                                .payload("Error: Claiming failed. Please try again later.")
                                .contentType("text/plain")
                                .send();
                        LOGGER.info("Update failed: '{}'", throwable.getMessage());
                    } else {
                        message.reply()
                                .httpStatus(HttpStatus.OK)
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
