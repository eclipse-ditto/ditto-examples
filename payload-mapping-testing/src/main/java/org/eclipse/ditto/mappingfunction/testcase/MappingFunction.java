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

/**
 * Wrapper class for a JavaScript definition of a mapping function as string.
 */
public final class MappingFunction {

    private final String scriptContent;

    private MappingFunction(final String scriptContent) {this.scriptContent = scriptContent;}

    /**
     * Creates a new wrapper instance for the given JavaScript function.
     *
     * @param javaScriptMappingFunction the JavaScript function that should be used to map a payload.
     * @return the new instance.
     */
    public static MappingFunction fromJavaScript(final String javaScriptMappingFunction) {
        return new MappingFunction(javaScriptMappingFunction);
    }

    String asString() {
        return scriptContent;
    }
}
