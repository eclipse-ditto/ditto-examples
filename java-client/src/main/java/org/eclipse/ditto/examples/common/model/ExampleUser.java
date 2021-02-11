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
package org.eclipse.ditto.examples.common.model;

/**
 * This model class demonstrates how to use a custom serialization (in this case JSON-Serialization with Jackson) for
 * Messages.
 */
public class ExampleUser {

    public static final String USER_CUSTOM_CONTENT_TYPE = "application/vnd.my-company.user+json";

    private String userName;
    private String email;

    public ExampleUser() {
        super();
    }

    public ExampleUser(final String userName, final String email) {
        this.userName = userName;
        this.email = email;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(final String userName) {
        this.userName = userName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(final String email) {
        this.email = email;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " [" +
                "userName=" + userName +
                ", email=" + email +
                "]";
    }
}
