/*
 * Copyright Bosch.IO GmbH 2020
 *
 * All rights reserved, also regarding any disposal, exploitation,
 * reproduction, editing, distribution, as well as in the event of
 * applications for industrial property rights.
 *
 * This software is the confidential and proprietary information
 * of Bosch.IO GmbH. You shall not disclose
 * such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you
 * entered into with Bosch.IO GmbH.
 */
package com.bosch.iot.things.kata.config;

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
