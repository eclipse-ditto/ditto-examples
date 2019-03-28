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
 * Responsible for providing a method to define the expected {@link Adaptable} as result of an incoming mapping
 * function.
 */
public final class IncomingMappingFunctionTestCaseExternalMessageBuilder {

    private final ExternalMessage message;

    IncomingMappingFunctionTestCaseExternalMessageBuilder(final ExternalMessage message) {
        this.message = message;
    }

    /**
     * Sets the expected {@link Adaptable} for an incoming mapping function.
     *
     * @param mappingFunction the adaptable that is expected to be returned by an incoming mapping function.
     * @return the next step of the builder.
     */
    public IncomingMappingFunctionTestCaseJavascriptMappingFunctionBuilder mappedByJavascriptPayloadMappingFunction(
            final MappingFunction mappingFunction) {
        return new IncomingMappingFunctionTestCaseJavascriptMappingFunctionBuilder(mappingFunction, this.message);
    }
}
