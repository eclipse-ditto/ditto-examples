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
import org.eclipse.ditto.model.things.Permission;
import org.eclipse.ditto.model.things.Thing;
import org.eclipse.ditto.model.things.ThingsModelFactory;
import org.eclipse.ditto.signals.commands.live.modify.ModifyFeaturePropertyLiveCommandAnswerBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This example shows how the {@link org.eclipse.ditto.client.DittoClient} can be used to register for, send and
 * respond to live commands.
 */
public class RegisterForAndSendLiveCommands extends ExamplesBase {

    private static final Logger LOGGER = LoggerFactory.getLogger(RegisterForAndSendLiveCommands.class);
    private static final String FEATURE_ID = "temp-sensor";

    private final String thingId;
    private final CountDownLatch latch;

    private RegisterForAndSendLiveCommands() {
        super();
        thingId = randomThingId();
        latch = new CountDownLatch(2);

        try {
            registerForAndSendLiveCommands();
        } catch (final InterruptedException | ExecutionException | TimeoutException e) {
            throw new IllegalStateException(e);
        } finally {
            terminate();
        }
    }

    public static void main(final String... args) {
        new RegisterForAndSendLiveCommands();
    }

    private void registerForAndSendLiveCommands() throws InterruptedException, TimeoutException, ExecutionException {
        LOGGER.info("[AT BACKEND] create a Thing with required permissions: {}", thingId);
        client1.twin().create(thingId).thenCompose(created -> {
            final Thing updated =
                    created.toBuilder()
                            .setPermissions(authorizationSubject1, allPermissions())
                            .setPermissions(authorizationSubject2, allPermissions())
                            .setFeature(ThingsModelFactory.newFeature(FEATURE_ID))
                            .build();
            return client1.twin().update(updated);
        }).get(2, TimeUnit.SECONDS);

        LOGGER.info("[AT DEVICE] register handler for 'ModifyFeatureProperty' LIVE commands..");
        client2.live()
                .forId(thingId)
                .forFeature(FEATURE_ID)
                .handleModifyFeaturePropertyCommands(command -> {
                    LOGGER.info("[AT DEVICE] Received live command: {}", command.getType());
                    LOGGER.info("[AT DEVICE] Property to modify: '{}' to value: '{}'", command.getPropertyPointer(),
                            command.getPropertyValue());
                    LOGGER.info("[AT DEVICE] Answering ...");
                    latch.countDown();
                    return command.answer()
                            .withResponse(ModifyFeaturePropertyLiveCommandAnswerBuilder.ResponseFactory::modified)
                            .withEvent(ModifyFeaturePropertyLiveCommandAnswerBuilder.EventFactory::modified);
                });

        try {
            client2.live().startConsumption().get(10, TimeUnit.SECONDS);
            client1.live().startConsumption().get(10, TimeUnit.SECONDS);
        } catch (final InterruptedException | ExecutionException | TimeoutException e) {
            throw new IllegalStateException("Error creating Things Client.", e);
        }

        LOGGER.info("[AT BACKEND] put 'temperature' property of 'temp-sensor' LIVE Feature..");
        client1.live()
                .forFeature(thingId, FEATURE_ID)
                .putProperty("temperature", 23.21)
                .whenComplete((_void, throwable) -> {
                    if (throwable != null) {
                        LOGGER.error("[AT BACKEND] Received error when putting the property", throwable);
                    } else {
                        LOGGER.info("[AT BACKEND] Putting the property succeeded");
                    }
                    latch.countDown();
                }).get(10, TimeUnit.SECONDS);

        if (latch.await(10, TimeUnit.SECONDS)) {
            LOGGER.info("Received all expected events!");
        } else {
            LOGGER.info("Did not receive all expected events!");
        }
    }

}