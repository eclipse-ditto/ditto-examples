/*
 * Copyright (c) 2019 Bosch Software Innovations GmbH.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/index.php
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.ditto.mappingfunction.testcase;

import org.eclipse.ditto.connectivity.api.ExternalMessage;
import org.eclipse.ditto.protocol.Adaptable;

interface IncomingPayloadMappingTestCaseBuilder extends AbstractPayloadMappingTestCaseBuilder {

    /**
     * Responsible for providing a method to set the incoming payload mapping function under test.
     */
    final class PayloadMappingFunctionStep {

        private final ExternalMessage message;

        PayloadMappingFunctionStep(final ExternalMessage message) {
            this.message = message;
        }

        /**
         * Sets the incoming payload mapping function under test.
         *
         * @param mappingFunction the incoming payload mapping function.
         * @return the next step of the builder.
         */
        public ExpectedAdaptableStep mappedByJavascriptPayloadMappingFunction(
                final PayloadMappingFunction mappingFunction) {
            return new ExpectedAdaptableStep(mappingFunction, this.message);
        }
    }

    /**
     * Responsible for providing a method to set the expected {@link org.eclipse.ditto.protocoladapter.Adaptable} after mapping.
     */
    final class ExpectedAdaptableStep {

        private final PayloadMappingFunction mappingFunction;
        private final ExternalMessage message;

        ExpectedAdaptableStep(final PayloadMappingFunction mappingFunction,
                final ExternalMessage message) {
            this.mappingFunction = mappingFunction;
            this.message = message;
        }

        /**
         * Sets the expected {@link org.eclipse.ditto.protocoladapter.Adaptable} for an incoming mapping function.
         *
         * @param expectedAdaptable the expected adaptable after the payload mapping.
         * @return the next step of the builder.
         */
        public ConfigStep isEqualTo(final Adaptable expectedAdaptable) {
            final IncomingPayloadMappingTestCase incomingMappingFunctionTestCase =
                    new IncomingPayloadMappingTestCase(mappingFunction, message, expectedAdaptable);
            return new ConfigStep(incomingMappingFunctionTestCase);
        }
    }

}