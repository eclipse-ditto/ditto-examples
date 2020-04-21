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
package com.bosch.iot.things.kata.policies;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.ditto.model.policies.PoliciesModelFactory;
import org.eclipse.ditto.model.policies.Policy;
import org.eclipse.ditto.model.policies.PolicyId;
import org.eclipse.ditto.model.things.Thing;
import org.eclipse.ditto.model.things.ThingId;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Use Ditto Java Client for creating a thing with a copied policy.
 */
public final class Kata3CopyExistingPolicy extends AbstractPolicyManagementKata {

    private static final String POLICY_NAME = "kata_3_policy-management";

    private static PolicyId policyId;
    private static ThingId thingId;

    @BeforeClass
    public static void setUpClass() {
        final String namespace = configProperties.getNamespace();
        policyId = PolicyId.of(namespace, POLICY_NAME);
        thingId = ThingId.of(namespace, "kata_3");
    }

    @Test
    public void createThingWithCopiedPolicy() throws InterruptedException, ExecutionException, TimeoutException {
        final Policy existingPolicy = createPolicy();

        /*
         * TODO
         * Use dittoClient's twin channel to create a thing with the given ID (thingId)
         * and a copy of an existing policy (policyId) (there is an appropriate Option).
         */

        // Assess result
        final Thing retrievedThing = retrieveThing(thingId);
        softly.assertThat(retrievedThing.getPolicyEntityId())
                .as("expected policy ID")
                .hasValue(PolicyId.of(thingId));

        final Policy thingPolicy = retrievePolicy(retrievedThing.getPolicyEntityId().orElseThrow());
        softly.assertThat(thingPolicy.getEntriesSet())
                .as("same entries")
                .isEqualTo(existingPolicy.getEntriesSet());
    }

    private static Policy createPolicy() throws InterruptedException, ExecutionException, TimeoutException {
        final Policy policy = PoliciesModelFactory.newPolicy(policyId, getDefaultPolicyEntry());
        final CompletableFuture<Policy> createPromise = dittoClient.policies().create(policy);
        return createPromise.get(CLIENT_TIMEOUT.getSeconds(), TimeUnit.SECONDS);
    }

}
