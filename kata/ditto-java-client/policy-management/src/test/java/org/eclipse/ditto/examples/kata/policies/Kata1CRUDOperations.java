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

import org.eclipse.ditto.policies.model.*;
import org.eclipse.ditto.policies.model.signals.commands.exceptions.PolicyNotAccessibleException;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

/**
 * Use Ditto Java Client for CRUD operations on policies.
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public final class Kata1CRUDOperations extends AbstractPolicyManagementKata {

    private static final String POLICY_NAME = "kata_1_policy-management";

    private static PolicyId policyId;

    @BeforeClass
    public static void setUpClass() {
        policyId = PolicyId.of(configProperties.getNamespace(), POLICY_NAME);
    }

    @Test
    public void part1CreatePolicy() throws InterruptedException, ExecutionException, TimeoutException {
        final PolicyEntry defaultPolicyEntry = getDefaultPolicyEntry();
        final Policy policy = PoliciesModelFactory.newPolicy(policyId, defaultPolicyEntry);

        // TODO use dittoClient to create the policy (and wait for its creation).

        // Assess result
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

    /**
     * This method depends on {@link #part1CreatePolicy()}.
     */
    @Test
    public void part2UpdatePolicy() throws InterruptedException, ExecutionException, TimeoutException {
        // TODO use dittoClient to update the policy in a way that "message:/" resource has only READ permission.

        // Assess result
        final CompletionStage<Policy> policyPromise = dittoClient.policies().retrieve(policyId);
        final Policy retrievedPolicy = policyPromise.toCompletableFuture().get(CLIENT_TIMEOUT.getSeconds(), TimeUnit.SECONDS);

        softly.assertThat(retrievedPolicy.getNamespace())
                .as("expected namespace")
                .contains(configProperties.getNamespace());
        softly.assertThat(retrievedPolicy)
                .as("expected size")
                .hasSize(1);
        softly.assertThat(retrievedPolicy.getEntryFor(DEFAULT_LABEL))
                .hasValueSatisfying(policyEntry -> {
                    softly.assertThat(policyEntry.getSubjects())
                            .as("one subject")
                            .hasSize(1);

                    final Resources resources = policyEntry.getResources();
                    softly.assertThat(resources)
                            .as("expected resources size")
                            .hasSize(3);
                    softly.assertThat(resources.getResource(RESOURCE_KEY_THING))
                            .hasValueSatisfying(hasPermission("READ", "WRITE"));
                    softly.assertThat(resources.getResource(RESOURCE_KEY_POLICY))
                            .hasValueSatisfying(hasPermission("READ", "WRITE"));
                    softly.assertThat(resources.getResource(RESOURCE_KEY_MESSAGE))
                            .hasValueSatisfying(hasPermission("READ"));
                });
    }

    private Consumer<Resource> hasPermission(final String ... expectedPermissions) {
        return resource -> {
            final EffectedPermissions effectedPermissions = resource.getEffectedPermissions();
            softly.assertThat(effectedPermissions.getGrantedPermissions())
                    .as("%s has expected permissions", resource.getResourceKey())
                    .containsOnly(expectedPermissions);
        };
    }

    /**
     * This test depends on {@link #part2UpdatePolicy()}.
     */
    @Test
    public void part3DeletePolicy() {
        // TODO use dittoClient to delete the policy.

        // Assess result
        final RuntimeException expectedException = PolicyNotAccessibleException.newBuilder(policyId).build();

        assertThatExceptionOfType(ExecutionException.class)
                .isThrownBy(() -> {
                    final CompletionStage<Policy> policyPromise = dittoClient.policies().retrieve(policyId);
                    policyPromise.toCompletableFuture().get(CLIENT_TIMEOUT.getSeconds(), TimeUnit.SECONDS);
                })
                .withCause(expectedException);
    }

}
