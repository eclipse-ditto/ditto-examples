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
package org.eclipse.ditto.examples.changes;

import static org.eclipse.ditto.model.things.ThingsModelFactory.allPermissions;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.ditto.client.DittoClient;
import org.eclipse.ditto.client.changes.ChangeAction;
import org.eclipse.ditto.client.management.ThingHandle;
import org.eclipse.ditto.examples.common.ExamplesBase;
import org.eclipse.ditto.json.JsonPointer;
import org.eclipse.ditto.json.JsonValue;
import org.eclipse.ditto.model.base.auth.AuthorizationSubject;
import org.eclipse.ditto.model.things.AclEntry;
import org.eclipse.ditto.model.things.Thing;
import org.eclipse.ditto.model.things.ThingId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This example shows the various possibilities that the {@link org.eclipse.ditto.client.DittoClient} offers for
 * registering handlers to be informed about {@link org.eclipse.ditto.client.changes.Change}s of your
 * {@link org.eclipse.ditto.model.things.Thing}s.
 * <p>
 * NOTE: Make sure to invoke {@code twin().startConsumption()} once after all handlers are registered to start
 * receiving events.
 */
public final class RegisterForChanges extends ExamplesBase {

    private static final Logger LOGGER = LoggerFactory.getLogger(RegisterForChanges.class);

    private final CountDownLatch countDownLatch;
    private final ThingId thingId = randomThingId();

    private RegisterForChanges() {
        super();
        this.countDownLatch = new CountDownLatch(2);

        try {
            registerForThingChanges(client1);
            registerForThingChangesWithDeregistration(client1);

            startConsumeChanges(client1);

            createThing(client2, authorizationSubject);
        } finally {
            destroy();
        }
    }

    public static void main(final String... args) {
        new RegisterForChanges();
    }

    /**
     * Register for all {@code ThingChange}s.
     */
    private void registerForThingChanges(final DittoClient client) {
        final ThingHandle thingHandle = client.twin().forId(thingId);

        client.twin().registerForThingChanges(registrationId(), change -> {
            LOGGER.info("For all things: ThingChange received: {}", change);
            countDownLatch.countDown();
        });

        thingHandle.registerForThingChanges(registrationId(),
                change -> LOGGER.info("My Thing: ThingChange received: {}", change));
    }

    /**
     * Register for {@code ThingChange}s and deregister after the created-event has been retrieved.
     */
    private void registerForThingChangesWithDeregistration(final DittoClient client) {
        final ThingHandle thingHandle = client.twin().forId(thingId);

        final String registrationId = registrationId();
        LOGGER.info("RegistrationId: {}", registrationId);

        thingHandle.registerForThingChanges(registrationId, change -> {
            LOGGER.info("{}: ThingChange received: {}", thingId, change);

            /* Deregister when the created-event has been retrieved */
            if (change.getAction() == ChangeAction.CREATED) {
                LOGGER.info("{}: Deregister handler with id: {}", thingId, registrationId);
                thingHandle.deregister(registrationId);
                countDownLatch.countDown();
            }
        });
    }

    private void createThing(final DittoClient client, final AuthorizationSubject... subjects) {
        LOGGER.info("Create thing {} and set required permissions.", thingId);

        final Set<AclEntry> aclEntries = Stream.of(subjects)
                .map(subject -> AclEntry.newInstance(subject, allPermissions()))
                .collect(Collectors.toSet());
        final Thing thing = Thing.newBuilder()
                .setId(thingId)
                .setPermissions(aclEntries)
                .build();

        try {
            client.twin().create(thing)
                    .thenCompose(createdThing -> {
                        final Thing updatedThing = createdThing.toBuilder()
                                .setAttribute(JsonPointer.of("foo"), JsonValue.of("bar"))
                                .build();
                        return client.twin().update(updatedThing);
                    }).get(10, TimeUnit.SECONDS);
        } catch (final InterruptedException | ExecutionException | TimeoutException e) {
            throw new IllegalStateException(e);
        }
    }

    private void destroy() {
        final boolean allMessagesReceived;
        try {
            allMessagesReceived = countDownLatch.await(10, TimeUnit.SECONDS);
        } catch (final InterruptedException e) {
            throw new IllegalStateException(e);
        }
        LOGGER.info("All changes received: {}", allMessagesReceived);
        terminate();
    }

    private static String registrationId() {
        return "registration:" + UUID.randomUUID();
    }

}