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
