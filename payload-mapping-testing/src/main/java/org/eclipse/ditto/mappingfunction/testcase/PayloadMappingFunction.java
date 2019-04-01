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

/**
 * Wrapper class for a JavaScript definition of a mapping function as string.
 */
public final class PayloadMappingFunction {

    private final String scriptContent;

    private PayloadMappingFunction(final String scriptContent) {this.scriptContent = scriptContent;}

    /**
     * Creates a new wrapper instance for the given JavaScript function.
     *
     * @param javaScriptMappingFunction the JavaScript function that should be used to map a payload.
     * @return the new instance.
     */
    public static PayloadMappingFunction fromJavaScript(final String javaScriptMappingFunction) {
        return new PayloadMappingFunction(javaScriptMappingFunction);
    }

    String asString() {
        return scriptContent;
    }
}
