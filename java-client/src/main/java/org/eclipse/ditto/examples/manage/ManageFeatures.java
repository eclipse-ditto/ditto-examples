/*
 * Copyright (c) 2019 Contributors to the Eclipse Foundation
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
package org.eclipse.ditto.examples.manage;

import static java.util.concurrent.TimeUnit.SECONDS;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.eclipse.ditto.client.management.FeatureHandle;
import org.eclipse.ditto.client.management.ThingHandle;
import org.eclipse.ditto.client.twin.Twin;
import org.eclipse.ditto.client.twin.TwinFeatureHandle;
import org.eclipse.ditto.client.twin.TwinThingHandle;
import org.eclipse.ditto.examples.common.ExamplesBase;
import org.eclipse.ditto.json.JsonFactory;
import org.eclipse.ditto.json.JsonPointer;
import org.eclipse.ditto.json.JsonValue;
import org.eclipse.ditto.model.things.Thing;
import org.eclipse.ditto.model.things.ThingId;
import org.eclipse.ditto.model.things.ThingsModelFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This example shows how a {@link org.eclipse.ditto.client.DittoClient} can be used to perform CRUD (Create, Read,
 * Update, and Delete) operations on {@link org.eclipse.ditto.model.things.Features} and {@link org.eclipse.ditto.model.things.FeatureProperties}.
 */
public class ManageFeatures extends ExamplesBase {

    private static final Logger LOGGER = LoggerFactory.getLogger(ManageFeatures.class);

    private static final int TIMEOUT = 5;

    private static final String FEATURE_ID = "smokeDetector";
    private static final String FEATURE_ID2 = "elevator";
    private static final JsonPointer PROPERTY_JSON_POINTER = JsonFactory.newPointer("density");
    private static final JsonValue PROPERTY_JSON_VALUE = JsonFactory.newValue(0.7);

    private ManageFeatures() {
        super();

        try {
            crudFeature();
            crudFeatureProperty();
            crudFeatureProperties();
            deleteFeatures();
        } catch (final InterruptedException | ExecutionException | TimeoutException e) {
            throw new IllegalStateException(e);
        } finally {
            terminate();
        }
    }

    public static void main(final String... args) {
        new ManageFeatures();
    }

    private void crudFeature() throws InterruptedException, ExecutionException, TimeoutException {
        LOGGER.info("Create, read, update and delete a Feature of a Thing.");

        final ThingId thingId = randomThingId();
        final Thing thing = ThingsModelFactory.newThingBuilder()
                .setId(thingId)
                .setFeature(ThingsModelFactory.newFeature("foo",
                        ThingsModelFactory.newFeatureProperties(JsonFactory.newObjectBuilder().set("foo", 1).build())))
                .build();

        startConsumeChanges(client1);

        client1.twin().registerForFeatureChanges(UUID.randomUUID().toString(), featureChange -> {
            final String featureId = featureChange.getFeature().getId();
            final JsonPointer path = featureChange.getPath();
            final Optional<JsonValue> value = featureChange.getValue()
                    .map(JsonValue::asObject) // "feature" is a JsonObject
                    .flatMap(jsonObj -> path.isEmpty() ? featureChange.getValue() : jsonObj.getValue(path));
            LOGGER.info("FeatureChange for featureId {} received on path {} - value was: {}", featureId, path, value);
        });

        client1.twin().create(thing).get(TIMEOUT, SECONDS);

        final ThingHandle<TwinFeatureHandle> thingHandle = client1.twin().forId(thingId);

        thingHandle.registerForFeatureChanges(UUID.randomUUID().toString(),
                featureChange -> LOGGER.info("{} Feature '{}'", featureChange.getAction(), featureChange.getFeature()));

        thingHandle.putFeature(ThingsModelFactory.newFeature(FEATURE_ID))
                .thenCompose(aVoid -> thingHandle.forFeature(FEATURE_ID).retrieve())
                .thenCompose(feature -> {
                    LOGGER.info("RETRIEVED Feature '{}'", feature);
                    return thingHandle.putFeature(ThingsModelFactory.newFeature(FEATURE_ID)
                            .setProperty(PROPERTY_JSON_POINTER, PROPERTY_JSON_VALUE));
                }).thenCompose(aVoid -> thingHandle.forFeature(FEATURE_ID).delete())
                .get(TIMEOUT, SECONDS);
    }

    private void crudFeatureProperty() throws InterruptedException, ExecutionException, TimeoutException {
        LOGGER.info("Create, read, update and delete a property of a Feature.");

        final ThingId thingId = randomThingId();
        final Thing thing = ThingsModelFactory.newThingBuilder()
                .setId(thingId)
                .setFeature(ThingsModelFactory.newFeature(FEATURE_ID))
                .build();

        startConsumeChanges(client1);

        client1.twin().create(thing).get(TIMEOUT, SECONDS);

        final FeatureHandle featureHandle = client1.twin().forFeature(thingId, FEATURE_ID);

        client1.twin().registerForFeaturePropertyChanges(UUID.randomUUID().toString(), FEATURE_ID,
                featurePropertyChange -> LOGGER
                        .info("Things Client handler: {} Property '{}:{}'", featurePropertyChange.getAction(),
                                featurePropertyChange.getPath(), featurePropertyChange.getValue()));

        client1.twin()
                .registerForFeaturePropertyChanges(UUID.randomUUID().toString(), FEATURE_ID, PROPERTY_JSON_POINTER,
                        featurePropertyChange -> LOGGER
                                .info("Things Client handler for property {}: {} Property '{}:{}'",
                                        PROPERTY_JSON_POINTER,
                                        featurePropertyChange.getAction(), featurePropertyChange.getPath(),
                                        featurePropertyChange.getValue()));

        featureHandle.registerForPropertyChanges(UUID.randomUUID().toString(), PROPERTY_JSON_POINTER,
                featurePropertyChange -> LOGGER.info("Feature handler: {} Property '{}:{}'",
                        featurePropertyChange.getAction(),
                        featurePropertyChange.getPath(), featurePropertyChange.getValue()));

        featureHandle.putProperty(PROPERTY_JSON_POINTER, PROPERTY_JSON_VALUE)
                .thenCompose(aVoid -> featureHandle.retrieve())
                .thenCompose(feature -> {
                    LOGGER.info("RETRIEVED Property '{}'", feature.getProperty(PROPERTY_JSON_POINTER));
                    return featureHandle.putProperty(PROPERTY_JSON_POINTER, 0.9);
                })
                .thenCompose(aVoid -> featureHandle.deleteProperty(PROPERTY_JSON_POINTER))
                .get(TIMEOUT, SECONDS);
    }

    private void crudFeatureProperties() throws InterruptedException, ExecutionException, TimeoutException {
        LOGGER.info("Create, read, update and delete all properties of a Feature.");

        final ThingId thingId = randomThingId();
        final Thing thing = ThingsModelFactory.newThingBuilder()
                .setId(thingId)
                .setFeature(ThingsModelFactory.newFeature(FEATURE_ID))
                .build();

        startConsumeChanges(client1);

        client1.twin().create(thing).get(TIMEOUT, SECONDS);

        final FeatureHandle featureHandle = client1.twin().forFeature(thingId, FEATURE_ID);

        featureHandle.registerForPropertyChanges(UUID.randomUUID().toString(), featurePropertyChange -> LOGGER
                .info("{} Properties '{}:{}'", featurePropertyChange.getAction(), featurePropertyChange.getPath(),
                        featurePropertyChange.getValue()));

        featureHandle.setProperties(ThingsModelFactory.newFeaturePropertiesBuilder()
                .set(PROPERTY_JSON_POINTER, PROPERTY_JSON_VALUE)
                .build())
                .thenCompose(aVoid -> featureHandle.retrieve())
                .thenCompose(feature -> {
                    LOGGER.info("RETRIEVED Properties '{}'", feature.getProperties());
                    return featureHandle.setProperties(ThingsModelFactory.newFeaturePropertiesBuilder()
                            .set(PROPERTY_JSON_POINTER, 0.9)
                            .build());
                }).thenCompose(aVoid -> featureHandle.deleteProperties())
                .get(TIMEOUT, SECONDS);
    }

    private void deleteFeatures() throws InterruptedException, ExecutionException, TimeoutException {
        LOGGER.info("Deleting all features of a Thing.");

        final ThingId thingId = randomThingId();

        final Thing thing = ThingsModelFactory.newThingBuilder()
                .setId(thingId)
                .setFeature(ThingsModelFactory.newFeature(FEATURE_ID))
                .setFeature(ThingsModelFactory.newFeature(FEATURE_ID2))
                .build();

        startConsumeChanges(client1);

        final Twin twin = client1.twin();

        twin.create(thing).get(TIMEOUT, SECONDS);

        final TwinThingHandle thingHandle = twin.forId(thingId);

        thingHandle.registerForFeaturesChanges(UUID.randomUUID().toString(),
                featuresChange -> LOGGER.info("{} Features '{}:{}'", featuresChange.getAction(),
                        featuresChange.getPath(), featuresChange.getValue()));

        thingHandle.deleteFeatures().thenCompose(aVoid -> thingHandle.retrieve())
                .thenAccept(thing1 -> LOGGER.info("Features have been deleted: {}", thing1.toJsonString()))
                .get(TIMEOUT, SECONDS);
    }

}
