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

import org.eclipse.ditto.client.changes.Change;
import org.eclipse.ditto.client.changes.FeatureChange;
import org.eclipse.ditto.client.changes.FeaturesChange;
import org.eclipse.ditto.client.changes.ThingChange;
import org.eclipse.ditto.client.options.Option;
import org.eclipse.ditto.client.twin.Twin;
import org.eclipse.ditto.client.twin.TwinFeatureHandle;
import org.eclipse.ditto.client.twin.TwinSearchHandle;
import org.eclipse.ditto.client.twin.TwinThingHandle;
import org.eclipse.ditto.json.JsonFieldSelector;
import org.eclipse.ditto.json.JsonObject;
import org.eclipse.ditto.json.JsonPointer;
import org.eclipse.ditto.policies.model.Policy;
import org.eclipse.ditto.things.model.Thing;
import org.eclipse.ditto.things.model.ThingId;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletionStage;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;

import static java.util.Objects.requireNonNull;

/**
 * An extended implementation of Twin for remembering created things.
 */
final class TwinWrapper implements Twin {

    private final Twin twin;
    private final UnaryOperator<Thing> observeCreatedThing;

    /**
     * Constructs a new {@code TwinWrapper} object.
     *
     * @param twin the Twin instance to be wrapped.
     * @param observeCreatedThing informs handler which are interested in thing creations.
     * @throws NullPointerException if any argument is {@code null}.
     */
    TwinWrapper(final Twin twin, final UnaryOperator<Thing> observeCreatedThing) {
        this.twin = requireNonNull(twin, "twin");
        this.observeCreatedThing = requireNonNull(observeCreatedThing, "observeCreatedThing");
    }

    @Override
    public TwinThingHandle forId(final ThingId thingId) {
        return twin.forId(thingId);
    }

    @Override
    public TwinFeatureHandle forFeature(final ThingId thingId, final String featureId) {
        return twin.forFeature(thingId, featureId);
    }

    @Override
    public CompletionStage<Void> startConsumption() {
        return twin.startConsumption();
    }

    @Override
    public CompletionStage<Void> startConsumption(final Option<?>... consumptionOptions) {
        return twin.startConsumption(consumptionOptions);
    }

    @Override
    public TwinSearchHandle search() {
        return twin.search();
    }

    @Override
    public CompletionStage<Void> suspendConsumption() {
        return twin.suspendConsumption();
    }

    @Override
    public CompletionStage<Thing> create(final Option<?>... options) {
        return twin.create(options).thenApply(observeCreatedThing);
    }

    @Override
    public CompletionStage<Thing> create(final ThingId thingId, final Option<?>... options) {
        return twin.create(thingId, options).thenApply(observeCreatedThing);
    }

    @Override
    public CompletionStage<Thing> create(final Thing thing, final Option<?>... options) {
        return twin.create(thing, options).thenApply(observeCreatedThing);
    }

    @Override
    public CompletionStage<Thing> create(final JsonObject thing, final Option<?>... options) {
        return twin.create(thing, options).thenApply(observeCreatedThing);
    }

    @Override
    public CompletionStage<Thing> create(final Policy initialPolicy, final Option<?>... options) {
        return twin.create(initialPolicy, options).thenApply(observeCreatedThing);
    }

    @Override
    public CompletionStage<Thing> create(final Thing thing, final JsonObject initialPolicy,
            final Option<?>... options) {

        return twin.create(thing, initialPolicy, options).thenApply(observeCreatedThing);
    }

    @Override
    public CompletionStage<Thing> create(final ThingId thingId, final JsonObject initialPolicy,
            final Option<?>... options) {

        return twin.create(thingId, initialPolicy, options).thenApply(observeCreatedThing);
    }

    @Override
    public CompletionStage<Thing> create(final JsonObject thing, final JsonObject initialPolicy,
            final Option<?>... options) {

        return twin.create(thing, initialPolicy, options).thenApply(observeCreatedThing);
    }

    @Override
    public CompletionStage<Thing> create(final Thing thing, final Policy initialPolicy, final Option<?>... options) {
        return twin.create(thing, initialPolicy, options).thenApply(observeCreatedThing);
    }

    @Override
    public CompletionStage<Thing> create(final ThingId thingId, final Policy initialPolicy,
            final Option<?>... options) {

        return twin.create(thingId, initialPolicy, options).thenApply(observeCreatedThing);
    }

    @Override
    public CompletionStage<Thing> create(final JsonObject thing, final Policy initialPolicy,
            final Option<?>... options) {

        return twin.create(thing, initialPolicy, options).thenApply(observeCreatedThing);
    }

    @Override
    public CompletionStage<Void> merge(ThingId thingId, Thing thing, Option<?>... options) {

        return twin.merge(thingId, thing, options);
    }

    @Override
    public CompletionStage<Void> merge(ThingId thingId, JsonObject jsonObject, Option<?>... options) {

        return twin.merge(thingId, jsonObject, options);
    }

    @Override
    public CompletionStage<Optional<Thing>> put(final Thing thing, final Option<?>... options) {
        return twin.put(thing, options)
                .thenApply(optionalThing -> {
                    optionalThing.ifPresent(observeCreatedThing::apply);
                    return optionalThing;
                });
    }

    @Override
    public CompletionStage<Optional<Thing>> put(final JsonObject thing, final Option<?>... options) {
        return twin.put(thing, options)
                .thenApply(optionalThing -> {
                    optionalThing.ifPresent(observeCreatedThing::apply);
                    return optionalThing;
                });
    }

    @Override
    public CompletionStage<Optional<Thing>> put(final Thing thing, final JsonObject initialPolicy,
            final Option<?>... options) {

        return twin.put(thing, initialPolicy, options)
                .thenApply(optionalThing -> {
                    optionalThing.ifPresent(observeCreatedThing::apply);
                    return optionalThing;
                });
    }

    @Override
    public CompletionStage<Optional<Thing>> put(final JsonObject thing, final JsonObject initialPolicy,
            final Option<?>... options) {

        return twin.put(thing, initialPolicy, options)
                .thenApply(optionalThing -> {
                    optionalThing.ifPresent(observeCreatedThing::apply);
                    return optionalThing;
                });
    }

    @Override
    public CompletionStage<Optional<Thing>> put(final Thing thing, final Policy initialPolicy,
            final Option<?>... options) {

        return twin.put(thing, initialPolicy, options)
                .thenApply(optionalThing -> {
                    optionalThing.ifPresent(observeCreatedThing::apply);
                    return optionalThing;
                });
    }

    @Override
    public CompletionStage<Optional<Thing>> put(final JsonObject thing, final Policy initialPolicy,
            final Option<?>... options) {

        return twin.put(thing, initialPolicy, options)
                .thenApply(optionalThing -> {
                    optionalThing.ifPresent(observeCreatedThing::apply);
                    return optionalThing;
                });
    }

    @Override
    public CompletionStage<Void> update(final Thing thing, final Option<?>... options) {
        return twin.update(thing, options);
    }

    @Override
    public CompletionStage<Void> update(final JsonObject thing, final Option<?>... options) {
        return twin.update(thing, options);
    }

    @Override
    public CompletionStage<Void> delete(final ThingId thingId, final Option<?>... options) {
        return twin.delete(thingId, options);
    }

    @Override
    public CompletionStage<List<Thing>> retrieve(final ThingId thingId, final ThingId... thingIds) {
        return twin.retrieve(thingId, thingIds);
    }

    @Override
    public CompletionStage<List<Thing>> retrieve(final JsonFieldSelector fieldSelector, final ThingId thingId,
            final ThingId... thingIds) {

        return twin.retrieve(fieldSelector, thingId, thingIds);
    }

    @Override
    public CompletionStage<List<Thing>> retrieve(final Iterable<ThingId> thingIds) {
        return twin.retrieve(thingIds);
    }

    @Override
    public CompletionStage<List<Thing>> retrieve(final JsonFieldSelector fieldSelector,
            final Iterable<ThingId> thingIds) {

        return twin.retrieve(fieldSelector, thingIds);
    }

    @Override
    public void registerForFeatureChanges(final String registrationId, final Consumer<FeatureChange> handler) {
        twin.registerForFeatureChanges(registrationId, handler);
    }

    @Override
    public void registerForFeatureChanges(final String registrationId, final String featureId,
            final Consumer<FeatureChange> handler) {

        twin.registerForFeatureChanges(registrationId, featureId, handler);
    }

    @Override
    public void registerForFeaturesChanges(final String registrationId, final Consumer<FeaturesChange> handler) {
        twin.registerForFeaturesChanges(registrationId, handler);
    }

    @Override
    public void registerForAttributesChanges(final String registrationId, final Consumer<Change> handler) {
        twin.registerForAttributesChanges(registrationId, handler);
    }

    @Override
    public void registerForAttributeChanges(final String registrationId, final JsonPointer path,
            final Consumer<Change> handler) {

        twin.registerForAttributeChanges(registrationId, path, handler);
    }

    @Override
    public void registerForThingChanges(final String registrationId, final Consumer<ThingChange> handler) {
        twin.registerForThingChanges(registrationId, handler);
    }

    @Override
    public void registerForFeaturePropertyChanges(final String registrationId, final String featureId,
            final Consumer<Change> handler) {

        twin.registerForFeaturePropertyChanges(registrationId, featureId, handler);
    }

    @Override
    public void registerForFeaturePropertyChanges(final String registrationId,
            final String featureId,
            final JsonPointer path,
            final Consumer<Change> handler) {

        twin.registerForFeaturePropertyChanges(registrationId, featureId, path, handler);
    }

    @Override
    public boolean deregister(final String registrationId) {
        return twin.deregister(registrationId);
    }

}
