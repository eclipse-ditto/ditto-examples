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

import java.util.List;

import org.eclipse.ditto.connectivity.api.ExternalMessage;
import org.eclipse.ditto.connectivity.service.config.mapping.MappingConfig;
import org.eclipse.ditto.connectivity.service.mapping.MessageMapper;
import org.eclipse.ditto.connectivity.service.mapping.javascript.JavaScriptMessageMapperConfiguration;
import org.eclipse.ditto.connectivity.service.mapping.javascript.JavaScriptMessageMapperFactory;
import org.eclipse.ditto.protocol.Adaptable;

/**
 * Test case for an outgoing payload mapping function.
 */
final class OutgoingPayloadMappingTestCase extends AbstractPayloadMappingTestCase {

    private final ExternalMessage expectedExternalMessage;
    private final Adaptable adaptableToMap;
    private final MessageMapper messageMapper;

    OutgoingPayloadMappingTestCase(final PayloadMappingFunction mappingFunction, final Adaptable adaptableToMap,
            final ExternalMessage expectedExternalMessage) {
        super(mappingFunction);
        this.messageMapper = JavaScriptMessageMapperFactory.createJavaScriptMessageMapperRhino();
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
    void run(final MappingConfig config, final JavaScriptMessageMapperConfiguration mappingConfig) {
        messageMapper.configure(config, mappingConfig);
        final List<ExternalMessage> mappedExternalMessage = messageMapper.map(adaptableToMap);
        assertThat(mappedExternalMessage).contains(expectedExternalMessage);
    }
}
