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

import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.ditto.model.base.common.ConditionChecker.checkNotNull;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.ditto.json.JsonPointer;
import org.eclipse.ditto.json.JsonValue;
import org.eclipse.ditto.model.policies.PolicyId;
import org.eclipse.ditto.model.things.Thing;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;


/**
 * Create subscription as publisher, request and validate results.
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class Kata2 extends AbstractSearchManagementKata {

    private static Thing thing1;
    private static Thing thing2;
    private static Thing thing3;

    @BeforeClass
    public static void setUpClass() throws InterruptedException {
        PolicyId policyId = createRandomPolicy();
        thing1 = createRandomThingWithAttribute(JsonPointer.of("/counter"), JsonValue.of(1), policyId);
        thing2 = createRandomThingWithAttribute(JsonPointer.of("/counter"), JsonValue.of(2), policyId);
        thing3 = createRandomThingWithAttribute(JsonPointer.of("/counter"), JsonValue.of(3), policyId);
    }

    @Test
    public void part1SubscribeToPublisher() throws InterruptedException {

        final String filter = "or(eq(attributes/counter, 1),eq(attributes/counter,2))";

        //TODO Create publisher for search results with given filter
        final Publisher<List<Thing>> publisher;


        //TODO subscribe to publisher with subscriber
        final TestSubscriber<List<Thing>> subscriber = new TestSubscriber<>();


        final Subscription subscription =
                checkNotNull(subscriber.subscriptions.poll(5000L, TimeUnit.MILLISECONDS));

        //TODO Request 3 results from subscription


        // Assertion that subscription contains the correct elements
        assertThat(subscriber.elements.poll(5000L, TimeUnit.MILLISECONDS))
                .contains(thing1).contains(thing2).doesNotContain(thing3);

    }


    private static final class TestSubscriber<T> implements Subscriber<T> {

        private final BlockingQueue<Subscription> subscriptions = new LinkedBlockingQueue<>();
        private final BlockingQueue<T> elements = new LinkedBlockingQueue<>();
        private final BlockingQueue<Throwable> errors = new LinkedBlockingQueue<>();
        private final AtomicInteger completeCounter = new AtomicInteger(0);

        @Override
        public void onSubscribe(final Subscription s) {
            subscriptions.add(s);
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



