/*
 * Copyright (c) 2017-2018 Bosch Software Innovations GmbH.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/index.php
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
