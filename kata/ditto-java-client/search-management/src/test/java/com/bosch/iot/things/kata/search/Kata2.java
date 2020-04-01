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

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.eclipse.ditto.model.policies.PoliciesModelFactory;
import org.eclipse.ditto.model.policies.Policy;
import org.eclipse.ditto.model.policies.PolicyEntry;
import org.eclipse.ditto.model.policies.PolicyId;
import org.eclipse.ditto.model.things.Thing;
import org.eclipse.ditto.model.things.ThingId;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Use Ditto Java Client for creating a thing with a dedicated initial policy.
 */
public final class Kata2 extends AbstractPolicyManagementKata {

    private static final String POLICY_NAME = "kata_2_policy-management";

    private static PolicyId policyId;
    private static ThingId thingId;

    @BeforeClass
    public static void setUpClass() {
        final String namespace = configProperties.getNamespace();
        policyId = PolicyId.of(namespace, POLICY_NAME);
        thingId = ThingId.of(namespace, "kata_2");
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
        softly.assertThat(retrievedThing.getPolicyEntityId())
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
