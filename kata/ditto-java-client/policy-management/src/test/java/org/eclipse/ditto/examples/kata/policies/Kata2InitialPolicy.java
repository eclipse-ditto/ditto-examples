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

import org.eclipse.ditto.policies.model.PoliciesModelFactory;
import org.eclipse.ditto.policies.model.Policy;
import org.eclipse.ditto.policies.model.PolicyEntry;
import org.eclipse.ditto.policies.model.PolicyId;
import org.eclipse.ditto.things.model.Thing;
import org.eclipse.ditto.things.model.ThingId;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

/**
 * Use Ditto Java Client for creating a thing with a dedicated initial policy.
 */
public final class Kata2InitialPolicy extends AbstractPolicyManagementKata {

    private static final String POLICY_NAME = "kata_2_policy-management";

    private static PolicyId policyId;
    private static ThingId thingId;

    @BeforeClass
    public static void setUpClass() {
        final String namespace = configProperties.getNamespace();
        policyId = PolicyId.of(namespace, POLICY_NAME);
        thingId = ThingId.of(namespace, "kata_2");
    }

    @After
    public void tearDown() {
        dittoClient.policies().delete(policyId).toCompletableFuture().join();
    }

    @Test
    public void createThingWithInitialPolicy() throws InterruptedException, ExecutionException, TimeoutException {
        final PolicyEntry defaultPolicyEntry = getDefaultPolicyEntry();
        final Policy policy = PoliciesModelFactory.newPolicy(policyId, defaultPolicyEntry);

        /*
         * TODO
         * Use dittoClient's twin channel to create a thing with the given ID (thingId)
         * and the given policy as its initial policy.
         */

        // Assess result
        final Thing retrievedThing = retrieveThing(thingId);
        softly.assertThat(retrievedThing.getPolicyId())
                .as("expected policy ID")
                .hasValue(policyId);

        final Policy retrievedPolicy = retrievePolicy(policyId);
        softly.assertThat(retrievedPolicy)
                .as("expected size")
                .hasSize(policy.getSize());
        softly.assertThat(retrievedPolicy.getEntryFor(DEFAULT_LABEL))
                .hasValueSatisfying(policyEntry -> {
                    softly.assertThat(policyEntry.getSubjects())
                            .as("one subject")
                            .hasSize(1);
                    softly.assertThat(policyEntry.getResources())
                            .as("expected resources")
                            .isEqualTo(defaultPolicyEntry.getResources());
                });
    }

}
