/*
 * Copyright Bosch.IO GmbH 2020
 *
 * All rights reserved, also regarding any disposal, exploitation,
 * reproduction, editing, distribution, as well as in the event of
 * applications for industrial property rights.
 *
 * This software is the confidential and proprietary information
 * of Bosch.IO GmbH. You shall not disclose
 * such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you
 * entered into with Bosch.IO GmbH.
 */
package com.bosch.iot.things.kata.search;

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
public final class Kata4 extends AbstractPolicyManagementKata {

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
