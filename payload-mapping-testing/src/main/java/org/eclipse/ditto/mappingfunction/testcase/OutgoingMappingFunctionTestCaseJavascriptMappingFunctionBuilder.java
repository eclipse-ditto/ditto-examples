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

import org.eclipse.ditto.protocoladapter.Adaptable;
import org.eclipse.ditto.services.models.connectivity.ExternalMessage;

/**
 * Responsible for providing a method to set the expected {@link ExternalMessage external message} after mapping.
 */
public final class OutgoingMappingFunctionTestCaseJavascriptMappingFunctionBuilder {

    private final MappingFunction mappingFunction;
    private final Adaptable adaptableToMap;

    OutgoingMappingFunctionTestCaseJavascriptMappingFunctionBuilder(final MappingFunction mappingFunction,
            final Adaptable adaptableToMap) {
        this.mappingFunction = mappingFunction;
        this.adaptableToMap = adaptableToMap;
    }

    /**
     * Sets the expected {@link ExternalMessage external message} for an outgoing mapping function.
     *
     * @param expectedExternalMessage the expected ExternalMessage after payload mapping.
     * @return the next step of the builder.
     */
    public MappingFunctionTestCaseConfigBuilder isEqualTo(final ExternalMessage expectedExternalMessage) {
        final OutgoingMappingFunctionTestCase outgoingMappingFunctionTestCase =
                new OutgoingMappingFunctionTestCase(mappingFunction, adaptableToMap, expectedExternalMessage);
        return new MappingFunctionTestCaseConfigBuilder(outgoingMappingFunctionTestCase);
    }
}
