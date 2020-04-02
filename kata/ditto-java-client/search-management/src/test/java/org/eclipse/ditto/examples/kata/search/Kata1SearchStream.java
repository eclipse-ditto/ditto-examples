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

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.stream.Stream;

import org.assertj.core.api.Assertions;
import org.eclipse.ditto.json.JsonPointer;
import org.eclipse.ditto.json.JsonValue;
import org.eclipse.ditto.model.things.Thing;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

/**
 * Create subscription as stream and validate results.
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public final class Kata1SearchStream extends AbstractSearchManagementKata {

    private static Thing thing1;
    private static Thing thing2;
    private static Thing thing3;

    @BeforeClass
    public static void setUpClass() throws InterruptedException, TimeoutException, ExecutionException {
        thing1 = createRandomThingWithAttribute(JsonPointer.of("/counter"), JsonValue.of(1));
        thing2 = createRandomThingWithAttribute(JsonPointer.of("/counter"), JsonValue.of(2));
        thing3 = createRandomThingWithAttribute(JsonPointer.of("/counter"), JsonValue.of(3));

        // Wait until search gets updated
        Thread.sleep(5000);
    }

    @Test
    public void part1CreateSearchQuery() {
        // TODO create search filters and options, which deliver only thing1 and thing2
        final String filter = "";
        final String options = "";


        // TODO create search stream with the filter and options above
        final Stream<Thing> stream = Stream.empty();


        // Verify results
        Assertions.assertThat(stream.map(thing -> thing.getEntityId().orElseThrow(AssertionError::new)))
                .contains(thing1.getEntityId().orElseThrow())
                .contains(thing2.getEntityId().orElseThrow())
                .doesNotContain(thing3.getEntityId().orElseThrow());
    }
}
