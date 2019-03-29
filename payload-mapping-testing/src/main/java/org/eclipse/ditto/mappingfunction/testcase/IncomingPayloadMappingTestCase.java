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


import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;

import org.eclipse.ditto.protocoladapter.Adaptable;
import org.eclipse.ditto.services.connectivity.mapping.MessageMapper;
import org.eclipse.ditto.services.connectivity.mapping.MessageMappers;
import org.eclipse.ditto.services.connectivity.mapping.javascript.JavaScriptMessageMapperConfiguration;
import org.eclipse.ditto.services.models.connectivity.ExternalMessage;

import com.typesafe.config.Config;

/**
 * Test case for an incoming payload mapping function.
 */
final class IncomingPayloadMappingTestCase extends AbstractPayloadMappingTestCase {

    private final ExternalMessage externalMessageToMap;
    private final Adaptable expectedAdaptable;
    private final MessageMapper messageMapper;

    IncomingPayloadMappingTestCase(final PayloadMappingFunction mappingFunction, final ExternalMessage externalMessageToMap,
            final Adaptable expectedAdaptable) {
        super(mappingFunction);
        this.messageMapper = MessageMappers.createJavaScriptMessageMapper();
        this.externalMessageToMap = externalMessageToMap;
        this.expectedAdaptable = expectedAdaptable;
    }

    /**
     * Uses the {@link #messageMapper} holding an incoming mapping function to map the {@link #externalMessageToMap} and
     * expects the outcome to be {@link #expectedAdaptable}.
     *
     * @param config the akka config to configure the {@link #messageMapper}.
     * @param mappingConfig the config to configure the {@link #messageMapper}.
     */
    @Override
    void run(final Config config, final JavaScriptMessageMapperConfiguration mappingConfig) {
        messageMapper.configure(config, mappingConfig);
        final Optional<Adaptable> mappedAdaptable = messageMapper.map(externalMessageToMap);
        assertThat(mappedAdaptable).contains(expectedAdaptable);
    }
}
