/*
 * Copyright (c) 2020 Contributors to the Eclipse Foundation
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
package org.eclipse.ditto.examples.kata.config;

import javax.annotation.Nullable;

/**
 * An error indicating problems with configuration.
 */
public class ConfigError extends Error {

    private static final long serialVersionUID = 6624886773860944830L;

    public ConfigError(final String message) {
        super(message);
    }

    public ConfigError(final Throwable cause) {
        super(cause);
    }

    public ConfigError(@Nullable final String message, @Nullable final Throwable cause) {
        super(message, cause);
    }

}
