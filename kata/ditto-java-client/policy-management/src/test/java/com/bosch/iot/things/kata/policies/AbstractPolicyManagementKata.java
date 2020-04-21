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

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Supplier;

import org.assertj.core.api.JUnitSoftAssertions;
import org.eclipse.ditto.client.DittoClient;
import org.eclipse.ditto.model.policies.EffectedPermissions;
import org.eclipse.ditto.model.policies.Policy;
import org.eclipse.ditto.model.policies.PolicyEntry;
import org.eclipse.ditto.model.policies.PolicyId;
import org.eclipse.ditto.model.policies.Resource;
import org.eclipse.ditto.model.policies.ResourceKey;
import org.eclipse.ditto.model.policies.Subject;
import org.eclipse.ditto.model.policies.SubjectId;
import org.eclipse.ditto.model.things.Thing;
import org.eclipse.ditto.model.things.ThingId;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;

import com.bosch.iot.things.kata.client.DittoClientSupplier;
import com.bosch.iot.things.kata.client.DittoClientWrapper;
import com.bosch.iot.things.kata.config.ConfigProperties;

/**
 * Abstract framework of a Kata.
 * Its main purpose is to provide common constants as well as to set up and tear down commonly used stuff.
 */
abstract class AbstractPolicyManagementKata {

    protected static final String DEFAULT_LABEL = "DEFAULT";
    protected static final ResourceKey RESOURCE_KEY_THING = ResourceKey.newInstance("thing:/");
    protected static final ResourceKey RESOURCE_KEY_POLICY = ResourceKey.newInstance("policy:/");
    protected static final ResourceKey RESOURCE_KEY_MESSAGE = ResourceKey.newInstance("message:/");
    protected static final Duration CLIENT_TIMEOUT = Duration.ofSeconds(10);

    protected static ConfigProperties configProperties;
    protected static DittoClient dittoClient;

    private static List<Supplier<CompletableFuture<?>>> rememberedDeletions;

    @Rule
    public final JUnitSoftAssertions softly = new JUnitSoftAssertions();

    @BeforeClass
    public static void setUpClassCommon() {
        configProperties = ConfigProperties.getInstance();

        final DittoClientSupplier dittoClientSupplier = DittoClientSupplier.getInstance(configProperties);
        final DittoClientWrapper dittoClientWrapper = DittoClientWrapper.getInstance(dittoClientSupplier.get());
        dittoClient = dittoClientWrapper;

        rememberedDeletions = new ArrayList<>();
        dittoClientWrapper.registerForThingCreation("things", createdThing -> {
            final ThingId thingId = createdThing.getEntityId().orElseThrow();
            rememberedDeletions.add(() -> dittoClient.twin().delete(thingId));
            rememberedDeletions.add(() -> dittoClient.policies().delete(PolicyId.of(thingId)));
        });
        dittoClientWrapper.registerForPolicyCreation("policies", createdPolicy -> {
            final PolicyId policyId = createdPolicy.getEntityId().orElseThrow();
            rememberedDeletions.add(() -> dittoClient.policies().delete(policyId));
        });
    }

    @AfterClass
    public static void tearDownClass() {
        final CompletableFuture<?>[] deletions = rememberedDeletions.stream()
                .map(Supplier::get)
                .toArray(CompletableFuture<?>[]::new);
        CompletableFuture.allOf(deletions)
                .thenRun(dittoClient::destroy)
                .exceptionally(throwable -> {
                    // Ignore irrelevant exceptions of clean up
                    return null;
                })
                .join();
    }

    protected static PolicyEntry getDefaultPolicyEntry() {
        final Set<Subject> subjects = Set.of(Subject.newInstance(SubjectId.newInstance("{{ request:subjectId }}")));

        final EffectedPermissions effectedPermissions = EffectedPermissions.newInstance(List.of("READ", "WRITE"), null);
        final Collection<Resource> resources = new HashSet<>();
        resources.add(Resource.newInstance(RESOURCE_KEY_THING, effectedPermissions));
        resources.add(Resource.newInstance(RESOURCE_KEY_POLICY, effectedPermissions));
        resources.add(Resource.newInstance(RESOURCE_KEY_MESSAGE, effectedPermissions));

        return PolicyEntry.newInstance(DEFAULT_LABEL, subjects, resources);
    }

    protected static Policy retrievePolicy(final PolicyId policyId)
            throws InterruptedException, ExecutionException, TimeoutException {

        final CompletableFuture<Policy> retrievePolicyPromise = dittoClient.policies().retrieve(policyId);
        return retrievePolicyPromise.get(CLIENT_TIMEOUT.getSeconds(), TimeUnit.SECONDS);
    }

    protected static Thing retrieveThing(final ThingId thingId)
            throws InterruptedException, ExecutionException, TimeoutException {

        final CompletableFuture<Thing> retrieveThingPromise = dittoClient.twin().forId(thingId).retrieve();
        return retrieveThingPromise.get(CLIENT_TIMEOUT.getSeconds(), TimeUnit.SECONDS);
    }

}
