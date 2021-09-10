/*
 * Copyright (c) 2019 Contributors to the Eclipse Foundation
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


import org.eclipse.ditto.connectivity.service.config.mapping.MappingConfig;
import org.eclipse.ditto.connectivity.service.mapping.javascript.JavaScriptMessageMapperConfiguration;

abstract class AbstractPayloadMappingTestCase {

    private final PayloadMappingFunction mappingFunction;

    AbstractPayloadMappingTestCase(final PayloadMappingFunction mappingFunction) {
        this.mappingFunction = mappingFunction;
    }

    PayloadMappingFunction getMappingFunction() {
        return mappingFunction;
    }

    abstract void run(final MappingConfig config, final JavaScriptMessageMapperConfiguration mappingConfig);

}
