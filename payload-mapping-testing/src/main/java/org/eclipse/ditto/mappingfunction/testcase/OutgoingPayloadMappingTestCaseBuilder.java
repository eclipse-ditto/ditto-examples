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

import org.eclipse.ditto.protocoladapter.Adaptable;
import org.eclipse.ditto.services.models.connectivity.ExternalMessage;

interface OutgoingPayloadMappingTestCaseBuilder extends AbstractPayloadMappingTestCaseBuilder {

    /**
     * Responsible for providing a method to set the outgoing payload mapping function under test.
     */
    final class PayloadMappingFunctionStep {

        private final Adaptable dittoMessage;

        PayloadMappingFunctionStep(final Adaptable dittoMessage) {
            this.dittoMessage = dittoMessage;
        }

        /**
         * Sets the outgoing payload mapping function under test.
         *
         * @param mappingFunction the outgoing payload mapping function.
         * @return the next step of the builder.
         */
        public ExpectedExternalMessageStep mappedByJavascriptPayloadMappingFunction(
                final PayloadMappingFunction mappingFunction) {
            return new ExpectedExternalMessageStep(mappingFunction, this.dittoMessage);
        }
    }

    /**
     * Responsible for providing a method to set the expected
     * {@link org.eclipse.ditto.services.models.connectivity.ExternalMessage external message} after mapping.
     */
    final class ExpectedExternalMessageStep {

        private final PayloadMappingFunction mappingFunction;
        private final Adaptable adaptableToMap;

        ExpectedExternalMessageStep(final PayloadMappingFunction mappingFunction,
                final Adaptable adaptableToMap) {
            this.mappingFunction = mappingFunction;
            this.adaptableToMap = adaptableToMap;
        }

        /**
         * Sets the expected {@link org.eclipse.ditto.services.models.connectivity.ExternalMessage external message} for
         * an outgoing mapping function.
         *
         * @param expectedExternalMessage the expected ExternalMessage after payload mapping.
         * @return the next step of the builder.
         */
        public ConfigStep isEqualTo(final ExternalMessage expectedExternalMessage) {
            final OutgoingPayloadMappingTestCase outgoingMappingFunctionTestCase =
                    new OutgoingPayloadMappingTestCase(mappingFunction, adaptableToMap, expectedExternalMessage);
            return new ConfigStep(outgoingMappingFunctionTestCase);
        }
    }
}
