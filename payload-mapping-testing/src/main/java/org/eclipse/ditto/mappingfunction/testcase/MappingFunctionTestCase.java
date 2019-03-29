/*
 * Copyright (c) 2017 Contributors to the Eclipse Foundation
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

import org.eclipse.ditto.protocoladapter.Adaptable;
import org.eclipse.ditto.services.models.connectivity.ExternalMessage;

/**
 * Provides static constructor functions for mapping function test cases.
 */
public final class MappingFunctionTestCase {

    private MappingFunctionTestCase() {}

    /**
     * Provides the first step of a builder for a mapping function test case to test incoming payload mapping.
     *
     * @param message the incoming message which the payload mapping is applied on.
     * @return the first step of the builder.
     */
    public static IncomingMappingFunctionTestCaseExternalMessageBuilder assertThat(final ExternalMessage message) {
        return new IncomingMappingFunctionTestCaseExternalMessageBuilder(message);
    }

    /**
     * Provides the first step of a builder for a mapping function test case to test outgoing payload mapping.
     *
     * @param dittoMessage the Adaptable which the payload mapping is applied on.
     * @return the first step of the builder.
     */
    public static OutgoingMappingFunctionTestCaseAdaptableToMapBuilder assertThat(final Adaptable dittoMessage) {
        return new OutgoingMappingFunctionTestCaseAdaptableToMapBuilder(dittoMessage);
    }
}
