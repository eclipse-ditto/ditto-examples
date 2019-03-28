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
 * Responsible for providing a method to define the {@link ExternalMessage} that will be mapped by an incoming mapping
 * function.
 */
public final class IncomingMappingFunctionTestCaseMessageToMapBuilder {

    private final MappingFunction mappingFunction;
    private final Adaptable expectedAdaptable;

    IncomingMappingFunctionTestCaseMessageToMapBuilder(final MappingFunction mappingFunction,
            final Adaptable expectedAdaptable) {
        this.mappingFunction = mappingFunction;
        this.expectedAdaptable = expectedAdaptable;
    }

    /**
     * Sets the {@link ExternalMessage} that should be mapped by an incoming mapping function.
     *
     * @param externalMessageToMap the external message that should be mapped.
     * @return the next step of the builder.
     */
    public MappingFunctionTestCaseConfigBuilder whenMapping(final ExternalMessage externalMessageToMap) {
        final IncomingMappingFunctionTestCase incomingMappingFunctionTestCase =
                new IncomingMappingFunctionTestCase(mappingFunction, externalMessageToMap, expectedAdaptable);
        return new MappingFunctionTestCaseConfigBuilder(incomingMappingFunctionTestCase);
    }
}
