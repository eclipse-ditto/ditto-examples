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

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Supplier;

import org.assertj.core.api.JUnitSoftAssertions;
import org.eclipse.ditto.client.DittoClient;
import org.eclipse.ditto.json.JsonPointer;
import org.eclipse.ditto.json.JsonValue;
import org.eclipse.ditto.model.policies.Policy;
import org.eclipse.ditto.model.policies.PolicyId;
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
abstract class AbstractSearchManagementKata {

    protected static ConfigProperties configProperties;
    protected static DittoClient dittoClient;

    private static List<Supplier<CompletableFuture<?>>> rememberedDeletions;

    private static PolicyId policyId;
    @Rule
    public final JUnitSoftAssertions softly = new JUnitSoftAssertions();

    @BeforeClass
    public static void setUpClassCommon() throws InterruptedException, ExecutionException, TimeoutException {
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

        policyId = createRandomPolicy();
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
        }).get(20L, TimeUnit.SECONDS);
        return policy.getEntityId().orElseThrow();
    }


    protected static Thing createRandomThingWithAttribute(JsonPointer attributePointer,
            JsonValue attributeValue)
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
                }).get(20L, TimeUnit.SECONDS);

        // Wait until search gets updated
        Thread.sleep(2000);
        return thing;
    }
}
