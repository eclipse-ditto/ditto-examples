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
