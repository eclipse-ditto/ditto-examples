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
package org.eclipse.ditto.examples.mappingfunction.testcase;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.ditto.model.base.headers.DittoHeaders;

class Utils {

    private Utils() {}

    // Helper functions
    static DittoHeaders createHeaders(String contentType) {
        final Map<String, String> headers = new HashMap<>();
        headers.put("content-type", contentType);
        headers.put("device_id", "the-thing-id");
        return DittoHeaders.of(headers);
    }
}
