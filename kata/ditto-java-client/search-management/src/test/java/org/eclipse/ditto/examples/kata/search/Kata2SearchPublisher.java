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

import org.eclipse.ditto.base.model.common.ConditionChecker;
import org.eclipse.ditto.json.JsonPointer;
import org.eclipse.ditto.json.JsonValue;
import org.eclipse.ditto.things.model.Thing;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;


/**
 * Create subscription as publisher, request and validate results.
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class Kata2SearchPublisher extends AbstractSearchManagementKata {

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
    public void part1SubscribeToPublisher() throws InterruptedException {
        Subscription subscription = null;
        try {

            final String filter = "or(eq(attributes/counter,1), eq(attributes/counter,2))";

            //TODO Create publisher for search results with given filter
            final Publisher<List<Thing>> publisher = null;


            //TODO subscribe to publisher with subscriber
            final TestSubscriber<List<Thing>> subscriber = new TestSubscriber<>();


            subscription = ConditionChecker.checkNotNull(subscriber.subscriptions.poll(5000L, TimeUnit.MILLISECONDS));


            //TODO Request results from subscription


            // Assertion that subscription contains the correct elements
            assertThat(subscriber.elements.poll(5000L, TimeUnit.MILLISECONDS))
                    .contains(thing1).contains(thing2).doesNotContain(thing3);
        } finally {
            // Cancel subscription regardless whether the test succeeded or not
            if (subscription != null) {
                subscription.cancel();
            }
        }
    }


    private static final class TestSubscriber<T> implements Subscriber<T> {

        private final BlockingQueue<Subscription> subscriptions = new LinkedBlockingQueue<>();
        private final BlockingQueue<T> elements = new LinkedBlockingQueue<>();
        private final BlockingQueue<Throwable> errors = new LinkedBlockingQueue<>();
        private final AtomicInteger completeCounter = new AtomicInteger(0);

        @Override
        public void onSubscribe(final Subscription subscription) {
            subscriptions.add(subscription);
        }

        @Override
        public void onNext(final T t) {
            elements.add(t);
        }

        @Override
        public void onError(final Throwable t) {
            errors.add(t);
        }

        @Override
        public void onComplete() {
            completeCounter.incrementAndGet();
        }
    }
}



