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
package org.eclipse.ditto.examples.kata.search;

import org.assertj.core.api.JUnitSoftAssertions;
import org.eclipse.ditto.client.DittoClient;
import org.eclipse.ditto.examples.kata.client.DittoClientSupplier;
import org.eclipse.ditto.examples.kata.client.DittoClientWrapper;
import org.eclipse.ditto.examples.kata.config.ConfigProperties;
import org.eclipse.ditto.json.JsonPointer;
import org.eclipse.ditto.json.JsonValue;
import org.eclipse.ditto.policies.model.*;
import org.eclipse.ditto.things.model.Thing;
import org.eclipse.ditto.things.model.ThingId;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Abstract framework of a Kata.
 * Its main purpose is to provide common constants as well as to set up and tear down commonly used stuff.
 */
abstract class AbstractSearchManagementKata {

    protected static ConfigProperties configProperties;
    protected static DittoClient dittoClient;

    private static final List<Supplier<CompletionStage<?>>> REMEMBERED_FOR_DELETION = new ArrayList<>();

    private static PolicyId policyId;
    @Rule
    public final JUnitSoftAssertions softly = new JUnitSoftAssertions();

    @BeforeClass
    public static void setUpClassCommon() throws InterruptedException, ExecutionException, TimeoutException {
        configProperties = ConfigProperties.getInstance();

        final DittoClientSupplier dittoClientSupplier = DittoClientSupplier.getInstance(configProperties);
        final DittoClientWrapper dittoClientWrapper = DittoClientWrapper.getInstance(dittoClientSupplier.get());
        dittoClient = dittoClientWrapper;

        dittoClientWrapper.registerForThingCreation("things", createdThing -> {
            final ThingId thingId = createdThing.getEntityId().orElseThrow();
            REMEMBERED_FOR_DELETION.add(() -> dittoClient.twin().delete(thingId));
            REMEMBERED_FOR_DELETION.add(() -> dittoClient.policies().delete(PolicyId.of(thingId)));
        });
        dittoClientWrapper.registerForPolicyCreation("policies", createdPolicy -> {
            final PolicyId policyId = createdPolicy.getEntityId().orElseThrow();
            REMEMBERED_FOR_DELETION.add(() -> dittoClient.policies().delete(policyId));
        });

        policyId = createRandomPolicy();
    }

    @AfterClass
    public static void tearDownClass() {
        final CompletableFuture<?>[] deletions = REMEMBERED_FOR_DELETION.stream()
                .map(Supplier::get)
                .map(CompletionStage::toCompletableFuture)
                .toArray(CompletableFuture<?>[]::new);
        CompletableFuture.allOf(deletions)
                .thenRun(dittoClient::destroy)
                .exceptionally(throwable -> {
                    // Ignore irrelevant exceptions of clean up
                    return null;
                })
                .join();
    }

    protected static PolicyId createRandomPolicy() throws InterruptedException, ExecutionException, TimeoutException {
        final Policy policy =
                Policy.newBuilder(PolicyId.of(configProperties.getNamespace() + ":user.policy"))
                        .forLabel("specialLabel")
                        .setSubject(Subject.newInstance(SubjectId.newInstance("{{ request:subjectId }}")))
                        .setGrantedPermissions(ResourceKey.newInstance("thing:/"), "READ", "WRITE")
                        .setGrantedPermissions(ResourceKey.newInstance("policy:/"), "READ", "WRITE")
                        .build();

        dittoClient.policies().create(policy).whenComplete((commandResponse, throwable) -> {
            assertThat(throwable).isNull();
            assertThat(commandResponse).isInstanceOf(Policy.class);
        }).toCompletableFuture().get(20L, TimeUnit.SECONDS);
        return policy.getEntityId().orElseThrow();
    }


    protected static Thing createRandomThingWithAttribute(
            final JsonPointer attributePointer,
            final JsonValue attributeValue)
            throws InterruptedException, TimeoutException, ExecutionException {

        final ThingId thingId = ThingId.of(configProperties.getNamespace() + ":" + UUID.randomUUID().toString());
        final Thing thing = Thing.newBuilder()
                .setId(thingId)
                .setPolicyId(policyId)
                .setAttribute(attributePointer, attributeValue)
                .build();

        dittoClient.twin()
                .create(thing)
                .whenComplete((commandResponse, throwable) -> {
                    assertThat(throwable).isNull();
                    assertThat(commandResponse).isInstanceOf(Thing.class);
                }).toCompletableFuture().get(20L, TimeUnit.SECONDS);

        return thing;
    }
}
