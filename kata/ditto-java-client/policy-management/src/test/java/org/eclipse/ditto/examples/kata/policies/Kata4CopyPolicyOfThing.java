/*
 * Copyright (c) 2020 Contributors to the Eclipse Foundation
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
package org.eclipse.ditto.examples.kata.policies;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.ditto.model.policies.Policy;
import org.eclipse.ditto.model.policies.PolicyId;
import org.eclipse.ditto.model.things.Thing;
import org.eclipse.ditto.model.things.ThingId;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Use Ditto Java Client for creating a thing with a copied policy of an existing thing.
 */
public final class Kata4CopyPolicyOfThing extends AbstractPolicyManagementKata {

    private static ThingId existingThingId;
    private static ThingId thingId;

    @BeforeClass
    public static void setUpClass() {
        final String namespace = configProperties.getNamespace();
        existingThingId = ThingId.of(namespace, "kata_4_existing");
        thingId = ThingId.of(namespace, "kata_4");
    }

    @Test
    public void createThingWithCopiedPolicyFromThing() throws InterruptedException, ExecutionException, TimeoutException {
        final Thing existingThing = createThing();

        /*
         * TODO
         * Use dittoClient's twin channel to create a thing with the given ID (thingId)
         * and a copy of the policy of an existing thing (existingThingId)
         * (there is an appropriate Option).
         */

        // Assess result
        final Thing retrievedThing = retrieveThing(thingId);
        softly.assertThat(retrievedThing.getPolicyEntityId())
                .as("expected policy ID")
                .hasValue(PolicyId.of(thingId));

        final Policy actualPolicy = retrievePolicy(retrievedThing.getPolicyEntityId().orElseThrow());
        final Policy expectedPolicy = retrievePolicy(existingThing.getPolicyEntityId().orElseThrow());
        softly.assertThat(actualPolicy.getEntriesSet())
                .as("same entries")
                .isEqualTo(expectedPolicy.getEntriesSet());
    }

    private static Thing createThing() throws InterruptedException, ExecutionException, TimeoutException {
        final CompletableFuture<Thing> createPromise = dittoClient.twin().create(existingThingId);
        return createPromise.get(CLIENT_TIMEOUT.getSeconds(), TimeUnit.SECONDS);
    }

}
