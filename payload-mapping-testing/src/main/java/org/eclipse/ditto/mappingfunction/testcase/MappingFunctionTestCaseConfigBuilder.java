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

import java.util.Collections;

import org.eclipse.ditto.services.connectivity.mapping.javascript.JavaScriptMessageMapperConfiguration;
import org.eclipse.ditto.services.connectivity.mapping.javascript.JavaScriptMessageMapperFactory;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

/**
 * Responsible to allow optional configuration of the
 * {@link org.eclipse.ditto.services.connectivity.mapping.MessageMapper} and run the {@link #mappingFunctionTestCase}.
 */
public final class MappingFunctionTestCaseConfigBuilder {

    private static final String DEFAULT_MAPPING_CONFIG = "javascript {\n" +
            "        maxScriptSizeBytes = 50000 # 50kB\n" +
            "        maxScriptExecutionTime = 500ms\n" +
            "        maxScriptStackDepth = 10\n" +
            "        }";

    private final JavaScriptMessageMapperConfiguration.Builder messageMapperConfigBuilder;
    private final AbstractMappingFunctionTestCase mappingFunctionTestCase;

    private String mappingConfig;

    MappingFunctionTestCaseConfigBuilder(final IncomingMappingFunctionTestCase mappingFunctionTestCase) {
        this.mappingFunctionTestCase = mappingFunctionTestCase;
        this.messageMapperConfigBuilder = JavaScriptMessageMapperFactory
                .createJavaScriptMessageMapperConfigurationBuilder(Collections.emptyMap());
        messageMapperConfigBuilder.incomingScript(mappingFunctionTestCase.getMappingFunction().asString());
    }

    MappingFunctionTestCaseConfigBuilder(final OutgoingMappingFunctionTestCase mappingFunctionTestCase) {
        this.mappingFunctionTestCase = mappingFunctionTestCase;
        this.messageMapperConfigBuilder = JavaScriptMessageMapperFactory
                .createJavaScriptMessageMapperConfigurationBuilder(Collections.emptyMap());
        messageMapperConfigBuilder.outgoingScript(mappingFunctionTestCase.getMappingFunction().asString());
    }

    /**
     * Allows to provide config for the {@link org.eclipse.ditto.services.connectivity.mapping.MessageMapper}.
     *
     * @param mappingConfig the config.
     * @return this builder.
     */
    public MappingFunctionTestCaseConfigBuilder withConfig(final String mappingConfig) {
        this.mappingConfig = mappingConfig;
        return this;
    }

    /**
     * Allows to use ByteBuffer.js in your payload mapping script.
     *
     * @return this builder.
     */
    public MappingFunctionTestCaseConfigBuilder withByteBufferJs() {
        messageMapperConfigBuilder.loadBytebufferJS(true);
        return this;
    }

    /**
     * Allows to use Long.js in your payload mapping script.
     *
     * @return this builder.
     */
    public MappingFunctionTestCaseConfigBuilder withLongJs() {
        messageMapperConfigBuilder.loadLongJS(true);
        return this;
    }

    /**
     * Runs the test case and verifies that the outcome equals the expected outcome.
     */
    public void verify() {
        final Config akkaMappingConfig;
        if (mappingConfig == null) {
            akkaMappingConfig = ConfigFactory.parseString(DEFAULT_MAPPING_CONFIG);
        } else {
            akkaMappingConfig = ConfigFactory.parseString(mappingConfig);
        }

        mappingFunctionTestCase.run(akkaMappingConfig, messageMapperConfigBuilder.build());
    }
}
