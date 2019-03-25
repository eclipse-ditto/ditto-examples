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


import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;

import org.eclipse.ditto.protocoladapter.Adaptable;
import org.eclipse.ditto.services.connectivity.mapping.MessageMapper;
import org.eclipse.ditto.services.connectivity.mapping.MessageMappers;
import org.eclipse.ditto.services.connectivity.mapping.javascript.JavaScriptMessageMapperConfiguration;
import org.eclipse.ditto.services.models.connectivity.ExternalMessage;

import com.typesafe.config.Config;

/**
 * Test case for an outgoing payload mapping function.
 */
final class OutgoingMappingFunctionTestCase extends AbstractMappingFunctionTestCase {

    private final ExternalMessage expectedExternalMessage;
    private final Adaptable adaptableToMap;
    private final MessageMapper messageMapper;

    OutgoingMappingFunctionTestCase(final MappingFunction mappingFunction, final Adaptable adaptableToMap,
            final ExternalMessage expectedExternalMessage) {
        super(mappingFunction);
        this.messageMapper = MessageMappers.createJavaScriptMessageMapper();
        this.expectedExternalMessage = expectedExternalMessage;
        this.adaptableToMap = adaptableToMap;
    }

    /**
     * Uses the {@link #messageMapper} holding an outgoing mapping function to map the {@link #adaptableToMap} and
     * expects the outcome to be {@link #expectedExternalMessage}.
     *
     * @param config the akka config to configure the {@link #messageMapper}.
     * @param mappingConfig the config to configure the {@link #messageMapper}.
     */
    @Override
    void run(final Config config, final JavaScriptMessageMapperConfiguration mappingConfig) {
        messageMapper.configure(config, mappingConfig);
        final Optional<ExternalMessage> mappedExternalMessage = messageMapper.map(adaptableToMap);
        assertThat(mappedExternalMessage).contains(expectedExternalMessage);
    }
}
