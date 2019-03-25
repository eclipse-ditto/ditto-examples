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


import org.eclipse.ditto.services.connectivity.mapping.javascript.JavaScriptMessageMapperConfiguration;

import com.typesafe.config.Config;

abstract class AbstractMappingFunctionTestCase {

    private final MappingFunction mappingFunction;

    AbstractMappingFunctionTestCase(final MappingFunction mappingFunction) {
        this.mappingFunction = mappingFunction;
    }

    MappingFunction getMappingFunction() {
        return mappingFunction;
    }

    abstract void run(final Config config, final JavaScriptMessageMapperConfiguration mappingConfig);

}
