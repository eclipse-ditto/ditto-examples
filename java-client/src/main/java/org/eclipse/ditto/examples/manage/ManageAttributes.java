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

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.ditto.client.twin.Twin;
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
 * This example shows how a {@link org.eclipse.ditto.client.DittoClient}  can be used to perform
 * CRUD (Create, Read, Update, and Delete) operations on {@link org.eclipse.ditto.model.things.Attributes} of a
 * {@link org.eclipse.ditto.model.things.Thing}.
 */
public class ManageAttributes extends ExamplesBase {

    private static final Logger LOGGER = LoggerFactory.getLogger(ManageAttributes.class);

    private static final JsonPointer ATTRIBUTE_JSON_POINTER1 = JsonFactory.newPointer("location");
    private static final JsonValue ATTRIBUTE_JSON_VALUE1 = JsonFactory.newValue(43.652);
    private static final JsonValue NEW_ATTRIBUTE_JSON_VALUE = JsonFactory.newValue(21.981);
    private static final JsonPointer ATTRIBUTE_JSON_POINTER2 = JsonFactory.newPointer("height");
    private static final JsonValue ATTRIBUTE_JSON_VALUE2 = JsonFactory.newValue(13398);

    private static final int TIMEOUT = 5;

    private ManageAttributes() {
        super();

        try {
            crudAttributes();
        } catch (final InterruptedException | ExecutionException | TimeoutException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(e);
        } finally {
            terminate();
        }
    }

    public static void main(final String[] args) {
        new ManageAttributes();
    }

    private void crudAttributes() throws InterruptedException, ExecutionException, TimeoutException {
        LOGGER.info("Starting: {}()", Thread.currentThread().getStackTrace()[1].getMethodName());

        final ThingId thingId = randomThingId();
        final Thing thing = ThingsModelFactory.newThingBuilder()
                .setId(thingId)
                .setAttribute(ATTRIBUTE_JSON_POINTER1, ATTRIBUTE_JSON_VALUE1)
                .setAttribute(ATTRIBUTE_JSON_POINTER2, ATTRIBUTE_JSON_VALUE2)
                .build();

        final Twin twin = client1.twin();
        twin.create(thing).get(TIMEOUT, SECONDS);
        final TwinThingHandle thingHandle = twin.forId(thingId);

        thingHandle.putAttribute(ATTRIBUTE_JSON_POINTER1, NEW_ATTRIBUTE_JSON_VALUE)
                .thenCompose(aVoid -> thingHandle.retrieve())
                .thenAccept(thing1 -> LOGGER.info("RETRIEVED thing is {}", thing1.toJsonString()))
                .thenCompose(aVoid1 -> thingHandle.deleteAttributes())
                .thenCompose(aVoid2 -> thingHandle.retrieve())
                .thenAccept(
                        thing2 -> LOGGER.info("RETRIEVED thing after attributes where deleted is {}",
                                thing2.toJsonString()))
                .get(5, TimeUnit.SECONDS);
    }

}