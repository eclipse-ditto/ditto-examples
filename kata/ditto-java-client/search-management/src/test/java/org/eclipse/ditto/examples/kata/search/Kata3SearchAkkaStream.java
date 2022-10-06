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

import akka.NotUsed;
import akka.actor.ActorSystem;
import akka.stream.ActorMaterializer;
import akka.stream.javadsl.Keep;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;
import org.assertj.core.api.Assertions;
import org.eclipse.ditto.json.JsonPointer;
import org.eclipse.ditto.json.JsonValue;
import org.eclipse.ditto.things.model.Thing;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

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

        // Wait until search gets updated
        Thread.sleep(5000);
    }

    @Test
    public void part1CreateAkkaSearchQuery() {
        final ActorSystem system = ActorSystem.create("thing-search");
        try {

            final String filter = "or(eq(attributes/counter,1), eq(attributes/counter,2))";


            // TODO create Akka source of publisher with above filter
            final Source<List<Thing>, NotUsed> things = null;


            // Verify Results
            things.flatMapConcat(Source::from)
                    .toMat(Sink.seq(), Keep.right())
                    .run(ActorMaterializer.create(system))
                    .thenAccept(t -> Assertions.assertThat(t).containsAnyOf(thing1, thing2).doesNotContain(thing3))
                    .toCompletableFuture()
                    .join();
        } finally {
            system.terminate();
        }
    }

}

