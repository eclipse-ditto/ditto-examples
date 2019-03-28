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

/**
 * Provides static constructor functions for mapping function test cases.
 */
public final class MappingFunctionTestCase {

    private MappingFunctionTestCase() {}

    /**
     * Provides the first step of a builder for a mapping function test case to test incoming payload mapping.
     *
     * @param mappingFunction the mapping function under test.
     * @return the first step of the builder.
     */
    public static IncomingMappingFunctionTestCaseExpectedAdaptableBuilder forIncomingMappingFunction(
            final MappingFunction mappingFunction) {
        return new IncomingMappingFunctionTestCaseExpectedAdaptableBuilder(mappingFunction);
    }

    /**
     * Provides the first step of a builder for a mapping function test case to test outgoing payload mapping.
     *
     * @param mappingFunction the mapping function under test.
     * @return the first step of the builder.
     */
    public static OutgoingMappingFunctionTestCaseExpectedMessageBuilder forOutgoingMappingFunction(
            final MappingFunction mappingFunction) {
        return new OutgoingMappingFunctionTestCaseExpectedMessageBuilder(mappingFunction);
    }
}
