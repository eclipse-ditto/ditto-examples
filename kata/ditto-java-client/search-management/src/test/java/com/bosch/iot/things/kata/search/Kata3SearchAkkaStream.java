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

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.assertj.core.api.Assertions;
import org.eclipse.ditto.json.JsonPointer;
import org.eclipse.ditto.json.JsonValue;
import org.eclipse.ditto.model.things.Thing;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import akka.NotUsed;
import akka.actor.ActorSystem;
import akka.stream.ActorMaterializer;
import akka.stream.javadsl.Source;

/**
 * Create subscription as akka stream and validate results.
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class Kata3SearchAkkaStream extends AbstractSearchManagementKata {

    private static Thing thing1;
    private static Thing thing2;
    private static Thing thing3;

    @BeforeClass
    public static void setUpClass() throws InterruptedException, TimeoutException, ExecutionException {
        thing1 = createRandomThingWithAttribute(JsonPointer.of("counter"), JsonValue.of(1));
        thing2 = createRandomThingWithAttribute(JsonPointer.of("counter"), JsonValue.of(2));
        thing3 = createRandomThingWithAttribute(JsonPointer.of("counter"), JsonValue.of(3));
    }

    @Test
    public void part1CreateAkkaSearchQuery() throws InterruptedException {

        final String filter = "or(eq(attributes/counter,1), eq(attributes/counter,2))";

        final ActorSystem system = ActorSystem.create("thing-search");

        // TODO create Akka source of publisher with above filter

        Source<List<Thing>, NotUsed> things = null;


        // Verify Results
        things.runForeach(t -> {
                    Assertions.assertThat(t).containsAnyOf(thing1, thing2).doesNotContain(thing3);
                    System.out.println(t);
                },
                ActorMaterializer.create(system));

        // Assure all results are retrieved by stream
        Thread.sleep(3000);

    }

}

