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
package org.eclipse.ditto.mappingfunction.testcase;

import org.eclipse.ditto.connectivity.api.ExternalMessage;
import org.eclipse.ditto.protocol.Adaptable;

/**
 * Provides static constructor functions for mapping function test cases.
 */
public final class PayloadMappingTestCase {

    private PayloadMappingTestCase() {}

    /**
     * Provides the first step of a builder for a mapping function test case to test incoming payload mapping.
     *
     * @param message the incoming message which the payload mapping is applied on.
     * @return the first step of the builder.
     */
    public static IncomingPayloadMappingTestCaseBuilder.PayloadMappingFunctionStep assertThat(
            final ExternalMessage message) {
        return new IncomingPayloadMappingTestCaseBuilder.PayloadMappingFunctionStep(message);
    }

    /**
     * Provides the first step of a builder for a mapping function test case to test outgoing payload mapping.
     *
     * @param dittoMessage the Adaptable which the payload mapping is applied on.
     * @return the first step of the builder.
     */
    public static OutgoingPayloadMappingTestCaseBuilder.PayloadMappingFunctionStep assertThat(
            final Adaptable dittoMessage) {
        return new OutgoingPayloadMappingTestCaseBuilder.PayloadMappingFunctionStep(dittoMessage);
    }
}
