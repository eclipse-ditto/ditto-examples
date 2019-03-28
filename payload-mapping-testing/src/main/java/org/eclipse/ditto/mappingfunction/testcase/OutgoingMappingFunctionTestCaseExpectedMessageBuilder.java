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

import org.eclipse.ditto.services.models.connectivity.ExternalMessage;

/**
 * Responsible for providing a method to define the expected {@link ExternalMessage} as result of an outgoing mapping
 * function.
 */
public final class OutgoingMappingFunctionTestCaseExpectedMessageBuilder {

    private final MappingFunction mappingFunction;

    OutgoingMappingFunctionTestCaseExpectedMessageBuilder(final MappingFunction mappingFunction) {
        this.mappingFunction = mappingFunction;
    }

    /**
     * Sets the expected {@link ExternalMessage external message} for an outgoing mapping function.
     *
     * @param expectedExternalMessage the message that is expected to be returned by an outgoing mapping function.
     * @return the next step of the builder.
     */
    public OutgoingMappingFunctionTestCaseAdaptableToMapBuilder withExpectedMappingResult(
            final ExternalMessage expectedExternalMessage) {
        return new OutgoingMappingFunctionTestCaseAdaptableToMapBuilder(mappingFunction, expectedExternalMessage);
    }
}
