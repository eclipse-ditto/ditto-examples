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
package org.eclipse.ditto.examples.live;

import static org.eclipse.ditto.model.things.ThingsModelFactory.allPermissions;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.ditto.examples.common.ExamplesBase;
import org.eclipse.ditto.json.JsonObject;
import org.eclipse.ditto.json.JsonValue;
import org.eclipse.ditto.model.base.entity.id.EntityId;
import org.eclipse.ditto.model.things.Permission;
import org.eclipse.ditto.model.things.Thing;
import org.eclipse.ditto.model.things.ThingId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This example shows how the {@link org.eclipse.ditto.client.DittoClient} client can be used to register for and
 * emit {@link org.eclipse.ditto.client.live.Live} changes.
 */
public class RegisterForAndEmitLiveEvents extends ExamplesBase {

    private static final Logger LOGGER = LoggerFactory.getLogger(RegisterForAndEmitLiveEvents.class);
    private static final String FEATURE_ID = "lamp";

    private final ThingId thingId;
    private final CountDownLatch latch;

    private RegisterForAndEmitLiveEvents() {
        super();
        thingId = randomThingId();
        latch = new CountDownLatch(2);

        try {
            registerForAndEmitLiveEvents();
        } catch (final InterruptedException | ExecutionException | TimeoutException e) {
            throw new IllegalStateException(e);
        } finally {
            terminate();
        }
    }

    public static void main(final String... args) {
        new RegisterForAndEmitLiveEvents();
    }

    private void registerForAndEmitLiveEvents() throws InterruptedException, TimeoutException, ExecutionException {

        LOGGER.info("[AT BACKEND] Creating thing {}..", thingId);
        client1.twin().create(thingId).thenCompose(created -> {
            final Thing updated =
                    created.toBuilder()
                            .setPermissions(authorizationSubject1, allPermissions())
                            .setPermissions(authorizationSubject2, Permission.WRITE)
                            .build();
            return client1.twin().update(updated);
        }).get(2, TimeUnit.SECONDS);

        LOGGER.info("[AT BACKEND] register for LIVE attribute changes of attribute 'location'..");
        client1.live()
                .registerForAttributeChanges("locationHandler", "location", change -> {
                    final EntityId thingId = change.getEntityId();
                    LOGGER.info("[AT BACKEND] Received change of attribute 'location' {} for thing {}.",
                            change.getValue().orElse(null), thingId);
                    latch.countDown();
                });

        LOGGER.info("[AT BACKEND] register for LIVE feature property changes of feature '{}'..", FEATURE_ID);
        client1.live()
                .forFeature(thingId, FEATURE_ID)
                .registerForPropertyChanges("lampPropertiesHandler", change -> {
                    LOGGER.info("[AT BACKEND] Received change of Feature '{}' property '{}': {}", FEATURE_ID,
                            change.getPath(), change.getValue().orElse(null));
                    latch.countDown();
                });

        try {
            client1.live().startConsumption().get(10, TimeUnit.SECONDS);
        } catch (final InterruptedException | ExecutionException | TimeoutException e) {
            throw new IllegalStateException("Error creating Things Client.", e);
        }

        LOGGER.info("[AT DEVICE] Emitting LIVE event AttributeModified for attribute 'location'..");
        client2.live()
                .forId(thingId)
                .emitEvent(thingEventFactory ->
                        thingEventFactory.attributeModified("location",
                                JsonObject.newBuilder()
                                        .set("longitude", 42.123)
                                        .set("latitude", 8.123)
                                        .build()
                        )
                );

        LOGGER.info("[AT DEVICE] Emitting LIVE event 'FeaturePropertyModified' for feature '{}', property 'on'..",
                FEATURE_ID);
        client2.live()
                .forId(thingId)
                .forFeature(FEATURE_ID)
                .emitEvent(featureEventFactory ->
                        featureEventFactory.featurePropertyModified("on",
                                JsonValue.of(true)
                        )
                );

        if (latch.await(10, TimeUnit.SECONDS)) {
            LOGGER.info("Received all expected events!");
        } else {
            LOGGER.info("Did not receive all expected events!");
        }
    }

}
