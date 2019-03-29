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
 * Responsible for providing a method to set the expected {@link Adaptable} after mapping.
 */
public final class IncomingMappingFunctionTestCaseJavascriptMappingFunctionBuilder {

    private final MappingFunction mappingFunction;
    private final ExternalMessage message;

    IncomingMappingFunctionTestCaseJavascriptMappingFunctionBuilder(final MappingFunction mappingFunction,
            final ExternalMessage message) {
        this.mappingFunction = mappingFunction;
        this.message = message;
    }

    /**
     * Sets the expected {@link Adaptable} for an incoming mapping function.
     *
     * @param expectedAdaptable the expected adaptable after the payload mapping.
     * @return the next step of the builder.
     */
    public MappingFunctionTestCaseConfigBuilder isEqualTo(final Adaptable expectedAdaptable) {
        final IncomingMappingFunctionTestCase incomingMappingFunctionTestCase =
                new IncomingMappingFunctionTestCase(mappingFunction, message, expectedAdaptable);
        return new MappingFunctionTestCaseConfigBuilder(incomingMappingFunctionTestCase);
    }
}
