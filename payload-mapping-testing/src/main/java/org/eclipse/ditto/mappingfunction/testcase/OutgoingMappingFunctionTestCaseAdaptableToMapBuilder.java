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
 * Responsible for providing a method to define the {@link Adaptable} that will be mapped by an outgoing mapping
 * function.
 */
public final class OutgoingMappingFunctionTestCaseAdaptableToMapBuilder {

    private final MappingFunction mappingFunction;
    private final ExternalMessage expectedExternalMessage;

    OutgoingMappingFunctionTestCaseAdaptableToMapBuilder(final MappingFunction mappingFunction,
            final ExternalMessage expectedExternalMessage) {
        this.mappingFunction = mappingFunction;
        this.expectedExternalMessage = expectedExternalMessage;
    }

    /**
     * Sets the {@link Adaptable} that should be mapped by an outgoing mapping function.
     *
     * @param adaptableToMap the adaptable that should be mapped.
     * @return the next step of the builder.
     */
    public MappingFunctionTestCaseConfigBuilder whenMapping(final Adaptable adaptableToMap) {
        final OutgoingMappingFunctionTestCase outgoingMappingFunctionTestCase =
                new OutgoingMappingFunctionTestCase(mappingFunction, adaptableToMap, expectedExternalMessage);
        return new MappingFunctionTestCaseConfigBuilder(outgoingMappingFunctionTestCase);
    }
}
