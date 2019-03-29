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

/**
 * Responsible for providing a method to set the outgoing payload mapping function under test.
 */
public final class OutgoingMappingFunctionTestCaseAdaptableToMapBuilder {

    private final Adaptable dittoMessage;

    OutgoingMappingFunctionTestCaseAdaptableToMapBuilder(final Adaptable dittoMessage) {
        this.dittoMessage = dittoMessage;
    }

    /**
     * Sets the outgoing payload mapping function under test.
     *
     * @param mappingFunction the outgoing payload mapping function.
     * @return the next step of the builder.
     */
    public OutgoingMappingFunctionTestCaseJavascriptMappingFunctionBuilder mappedByJavascriptPayloadMappingFunction(
            final MappingFunction mappingFunction) {
        return new OutgoingMappingFunctionTestCaseJavascriptMappingFunctionBuilder(mappingFunction, this.dittoMessage);
    }
}
