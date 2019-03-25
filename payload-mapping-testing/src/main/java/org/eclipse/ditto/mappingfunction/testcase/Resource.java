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


import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Helper class to load content from files within the classpath.
 */
public final class Resource {

    private final File resourceFile;

    /**
     * Creates a new Resource from a file at the specified path.
     *
     * @param path the path to the file relative to the root of the classpath.
     */
    public Resource(final String path) {
        resourceFile = new File(path);
    }

    /**
     * Loads the content of this resource.
     *
     * @return The content of this resource as string.
     * @throws IOException forwarded when opening the stream to the resource.
     */
    public String getContent() throws IOException {
        try (InputStream in = Resource.class.getClassLoader().getResourceAsStream(resourceFile.getPath())) {
            if (in == null) {
                throw new FileNotFoundException(String.format("File <%s> not found.", resourceFile.getPath()));
            }
            final BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(in));
            final StringBuilder fileContent = new StringBuilder();
            String lineContent;
            while ((lineContent = bufferedReader.readLine()) != null) {
                fileContent.append(lineContent);
                fileContent.append("\n");
            }
            return fileContent.toString();
        }
    }
}
