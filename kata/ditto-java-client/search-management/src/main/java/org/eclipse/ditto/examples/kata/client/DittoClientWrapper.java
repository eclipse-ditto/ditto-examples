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
package org.eclipse.ditto.examples.kata.client;

import static java.util.Objects.requireNonNull;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;

import org.eclipse.ditto.client.DittoClient;
import org.eclipse.ditto.client.live.Live;
import org.eclipse.ditto.client.policies.Policies;
import org.eclipse.ditto.client.twin.Twin;
import org.eclipse.ditto.model.policies.Policy;
import org.eclipse.ditto.model.things.Thing;
import org.eclipse.ditto.protocoladapter.Adaptable;

/**
 * This wrapper of {@link DittoClient} provides additional methods for registering handlers for created things and
 * policies.
 * This is useful for cleaning up.
 */
public final class DittoClientWrapper implements DittoClient {

    private final DittoClient dittoClient;
    private final Map<String, Consumer<Thing>> thingCreationHandlers;
    private final Map<String, Consumer<Policy>> policyCreationHandlers;
    private Twin twin;
    private Policies policies;

    private DittoClientWrapper(final DittoClient dittoClient) {
        this.dittoClient = dittoClient;
        thingCreationHandlers = new HashMap<>();
        policyCreationHandlers = new HashMap<>();
        twin = null;
        policies = null;
    }

    /**
     * Returns an instance of DittoClientWrapper.
     *
     * @param dittoClient the DittoClient to be wrapped.
     * @return the instance.
     * @throws NullPointerException if {@code dittoClient} is {@code null}.
     */
    public static DittoClientWrapper getInstance(final DittoClient dittoClient) {
        return new DittoClientWrapper(requireNonNull(dittoClient, "dittoClient"));
    }

    @Override
    public Twin twin() {
        Twin result = twin;
        if (null == result) {
            final UnaryOperator<Thing> rememberCreatedThing = createdThing -> {
                thingCreationHandlers.forEach((handlerId, handler) -> handler.accept(createdThing));
                return createdThing;
            };
            result = new TwinWrapper(dittoClient.twin(), rememberCreatedThing);
            twin = result;
        }
        return result;
    }

    @Override
    public Live live() {
        return dittoClient.live();
    }

    @Override
    public Policies policies() {
        Policies result = policies;
        if (null == result) {
            final UnaryOperator<Policy> rememberCreatedPolicy = createdPolicy -> {
                policyCreationHandlers.forEach((handlerId, handler) -> handler.accept(createdPolicy));
                return createdPolicy;
            };
            result = new PoliciesWrapper(dittoClient.policies(), rememberCreatedPolicy);
            policies = result;
        }
        return result;
    }

    @Override
    public CompletableFuture<Adaptable> sendDittoProtocol(final Adaptable dittoProtocolAdaptable) {
        return dittoClient.sendDittoProtocol(dittoProtocolAdaptable);
    }

    public void registerForThingCreation(final CharSequence handlerId, final Consumer<Thing> handler) {
        thingCreationHandlers.put(validateHandlerId(handlerId), validateHandler(handler));
    }

    private static String validateHandlerId(final CharSequence handlerId) {
        return requireNonNull(handlerId, "handlerId").toString();
    }

    private static <T extends Consumer<?>> T validateHandler(final T handler) {
        return requireNonNull(handler, "handler");
    }

    public void deregisterFromThingCreation(final CharSequence handlerId) {
        thingCreationHandlers.remove(validateHandlerId(handlerId));
    }

    public void registerForPolicyCreation(final CharSequence handlerId, final Consumer<Policy> handler) {
        policyCreationHandlers.put(validateHandlerId(handlerId), validateHandler(handler));
    }

    public void deregisterFromPolicyCreation(final CharSequence handlerId) {
        policyCreationHandlers.remove(validateHandlerId(handlerId));
    }

    @Override
    public void destroy() {
        dittoClient.destroy();
    }

}
