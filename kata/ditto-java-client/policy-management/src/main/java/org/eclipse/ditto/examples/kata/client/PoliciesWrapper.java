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

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.UnaryOperator;

import org.eclipse.ditto.client.options.Option;
import org.eclipse.ditto.client.policies.Policies;
import org.eclipse.ditto.json.JsonObject;
import org.eclipse.ditto.model.policies.Policy;
import org.eclipse.ditto.model.policies.PolicyId;

/**
 * An extended implementation of Policies for remembering created policies.
 */
final class PoliciesWrapper implements Policies {

    private final Policies policies;
    private final UnaryOperator<Policy> observeCreatedPolicy;

    /**
     * Constructs a new {@code PoliciesWrapper} object.
     *
     * @param policies the Policies instance to be wrapped.
     * @param observeCreatedPolicy informs handler which are interested in thing creations.
     * @throws NullPointerException if any argument is {@code null}.
     */
    PoliciesWrapper(final Policies policies, final UnaryOperator<Policy> observeCreatedPolicy) {
        this.policies = requireNonNull(policies, "policies");
        this.observeCreatedPolicy = requireNonNull(observeCreatedPolicy, "observeCreatedPolicy");
    }

    @Override
    public CompletableFuture<Policy> create(final Policy policy, final Option<?>... options) {
        return policies.create(policy, options).thenApply(observeCreatedPolicy);
    }

    @Override
    public CompletableFuture<Policy> create(final JsonObject jsonObject, final Option<?>... options) {
        return policies.create(jsonObject, options).thenApply(observeCreatedPolicy);
    }

    @Override
    public CompletableFuture<Optional<Policy>> put(final Policy policy, final Option<?>... options) {
        return policies.put(policy, options)
                .thenApply(createdPolicy -> {
                    createdPolicy.ifPresent(observeCreatedPolicy::apply);
                    return createdPolicy;
                });
    }

    @Override
    public CompletableFuture<Optional<Policy>> put(final JsonObject jsonObject, final Option<?>... options) {
        return policies.put(jsonObject, options)
                .thenApply(createdPolicy -> {
                    createdPolicy.ifPresent(observeCreatedPolicy::apply);
                    return createdPolicy;
                });
    }

    @Override
    public CompletableFuture<Void> update(final Policy policy, final Option<?>... options) {
        return policies.update(policy, options);
    }

    @Override
    public CompletableFuture<Void> update(final JsonObject jsonObject, final Option<?>... options) {
        return policies.update(jsonObject, options);
    }

    @Override
    public CompletableFuture<Void> delete(final PolicyId policyId, final Option<?>... options) {
        return policies.delete(policyId, options);
    }

    @Override
    public CompletableFuture<Policy> retrieve(final PolicyId policyId) {
        return policies.retrieve(policyId);
    }

}
