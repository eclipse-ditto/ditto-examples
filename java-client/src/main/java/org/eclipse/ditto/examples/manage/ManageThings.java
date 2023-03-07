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
package org.eclipse.ditto.examples.manage;

import org.eclipse.ditto.base.model.json.JsonSchemaVersion;
import org.eclipse.ditto.client.twin.TwinThingHandle;
import org.eclipse.ditto.examples.common.ExamplesBase;
import org.eclipse.ditto.json.JsonFactory;
import org.eclipse.ditto.json.JsonFieldSelector;
import org.eclipse.ditto.json.JsonPointer;
import org.eclipse.ditto.json.JsonValue;
import org.eclipse.ditto.things.model.Feature;
import org.eclipse.ditto.things.model.Thing;
import org.eclipse.ditto.things.model.ThingId;
import org.eclipse.ditto.things.model.ThingsModelFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * This example shows how a {@link org.eclipse.ditto.client.DittoClient} can be used to perform CRUD (Create, Read, Update, and
 * Delete) operations on {@link Thing}s.
 */
public class ManageThings extends ExamplesBase {

    private static final Logger LOGGER = LoggerFactory.getLogger(ManageThings.class);

    private final ThingId complexThingId;
    private final ThingId myThingId;

    private ManageThings() {
        super();
        complexThingId = randomThingId();
        myThingId = randomThingId();

        try {
            createReadUpdateDelete();
            createAComplexThing();
            retrieveThings();
            updateThing();
        } catch (final InterruptedException | ExecutionException | TimeoutException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(e);
        } finally {
            terminate();
        }
    }

    public static void main(final String... args) {
        new ManageThings();
    }

    /**
     * Creates a new {@code Thing} object, updates the thing by adding a new attribute to the thing, retrieves the
     * modified thing, and finally deletes it.
     *
     * @throws ExecutionException if a failure response is received for any of the requests, or if an exception occurs
     * inside the provided result handlers. This root cause can be retrieved using {@link
     * ExecutionException#getCause()}.
     * @throws TimeoutException if not all operations are terminated with a result (success or failure) within the given
     * timeout.
     * @throws InterruptedException if the executing thread is interrupted while waiting for a response.
     */
    private void createReadUpdateDelete() throws InterruptedException, ExecutionException, TimeoutException {
        LOGGER.info("Starting: createReadUpdateDelete()");
        final TwinThingHandle thingHandle = client1.twin().forId(myThingId);
        client1.twin().create(myThingId)
                .thenCompose(thing -> thingHandle.putAttribute(JsonFactory.newPointer("address/city"), "Berlin"))
                .thenCompose(aVoid -> thingHandle.retrieve())
                .thenCompose(thing -> {
                    LOGGER.info("My thing as persisted: {}", thing);
                    return thingHandle.delete();
                }).toCompletableFuture().get(10, TimeUnit.SECONDS);
    }

    /**
     * Creates a complex {@code Thing} object with {@code Policy}s, {@code Feature}s, and {@code Attribute}s, and waits for
     * a success or failure result.
     *
     * @throws ExecutionException if a failure response is received for the requests, or if an exception occurs inside
     * the provided result handler. This root cause can be retrieved using {@link ExecutionException#getCause()}.
     * @throws TimeoutException if the operation is not terminated with a result (success or failure) within the given
     * timeout.
     * @throws InterruptedException if the executing thread is interrupted while waiting for a response.
     */
    private void createAComplexThing() throws InterruptedException, ExecutionException, TimeoutException {
        LOGGER.info("Starting: createAComplexThing()");
        /* Create a new thing with policy, features, attributes and define handlers for success and failure */
        client1.twin().create(complexThingId).thenCompose(created -> {
            final Thing updated =
                    created.toBuilder()
                            .setFeatureProperty("featureId", JsonFactory.newPointer("propertyName"),
                                    JsonFactory.newValue("value"))
                            .setAttribute(JsonFactory.newPointer("attributeName"), JsonFactory.newValue("value"))
                            .build();
            return client1.twin().update(updated);
        }).whenComplete((thing, throwable) -> {
            if (throwable == null) {
                LOGGER.info("Thing created: {}", complexThingId);
            } else {
                LOGGER.error("Create Thing Failed", throwable);
            }
        }).toCompletableFuture().get(1, TimeUnit.SECONDS);
    }

    /**
     * Shows different possibilities to retrieve a {@code Thing} or list of {@code Thing}s using their ids, with or
     * without {@code FieldSelector}s. {@code FieldSelector}s allow you to gain performance and save bandwidth by only
     * retrieving those fields of a that you are interested in.
     *
     * @throws ExecutionException if a failure response is received for the requests, or if an exception occurs inside
     * the provided result handler. This root cause can be retrieved using {@link ExecutionException#getCause()}.
     * @throws TimeoutException if the operation is not terminated with a result (success or failure) within the given
     * timeout.
     * @throws InterruptedException if the executing thread is interrupted while waiting for a response.
     */
    private void retrieveThings() throws InterruptedException, ExecutionException, TimeoutException {
        LOGGER.info("Starting: retrieveThings()");
        /* Retrieve a Single Thing*/
        client1.twin().forId(complexThingId).retrieve().thenAccept(thing -> LOGGER.info("Retrieved thing: {}", thing))
                .toCompletableFuture()
                .get(1, TimeUnit.SECONDS);

        /* Retrieve a List of Things */
        client1.twin().retrieve(myThingId, complexThingId).thenAccept(things -> {
            if (things.isEmpty()) {
                LOGGER.info(
                        "The requested things were not found, or you don't have sufficient permission to read them.");
            } else {
                LOGGER.info("Retrieved things: {}", Arrays.toString(things.toArray()));
            }
        }).toCompletableFuture().get(1, TimeUnit.SECONDS);

        /* Retrieve a List of Things with field selectors */
        client1.twin().retrieve(JsonFieldSelector.newInstance("attributes"), myThingId, complexThingId)
                .thenAccept(things -> {
                    if (things.isEmpty()) {
                        LOGGER.info(
                                "The requested things were not found, or you don't have sufficient permission to read them.");
                    } else {
                        things.forEach(
                                thing -> LOGGER.info("Thing {} has attributes {}.", thing, thing.getAttributes()));
                    }
                }).toCompletableFuture().get(1, TimeUnit.SECONDS);
    }

    private void updateThing() throws InterruptedException, TimeoutException, ExecutionException {
        LOGGER.info("Starting: updateThing()");
        final CountDownLatch countDownLatch = new CountDownLatch(2);
        final ThingId thingId = randomThingId();
        final JsonPointer attributeJsonPointer = JsonFactory.newPointer("foo");
        final JsonValue attributeJsonValue = JsonFactory.newValue("bar");
        final Thing thing = ThingsModelFactory.newThingBuilder()
                .setId(thingId)
                .setAttribute(attributeJsonPointer, attributeJsonValue)
                .build();

        startConsumeChanges(client1);

        LOGGER.info("Registering for changes of thing {}", thingId);
        client1.twin().forId(thingId).registerForThingChanges(UUID.randomUUID().toString(), change -> {
            LOGGER.info("Received Event: {} -> {}", change.getAction(), change.getValue());
            countDownLatch.countDown();
        });

        LOGGER.info("Creating thing {}", thing);
        client2.twin().create(thing)
                .thenCompose(created -> {
                    LOGGER.info("Thing created: {}", created.toJsonString(JsonSchemaVersion.V_2));

                    final Feature feature = ThingsModelFactory.newFeature("myFeature");
                    final Thing updated = created.toBuilder()
                            .removeAllAttributes()
                            .setFeature(feature)
                            .build();

                    LOGGER.info("Updating thing {}", updated);
                    return client2.twin().update(updated);
                }).whenComplete((aVoid, throwable) -> {
            if (null != throwable) {
                LOGGER.info("Update failed: '{}'", throwable.getMessage());
            } else {
                LOGGER.info("Update successful!");
            }
        }).toCompletableFuture().get(2, TimeUnit.SECONDS);

        final boolean allMessagesReceived = countDownLatch.await(10, TimeUnit.SECONDS);
        LOGGER.info("All events received: {}", allMessagesReceived);
    }

}