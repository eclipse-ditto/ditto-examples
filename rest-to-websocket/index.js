/*
 * Copyright (c) 2017 Contributors to the Eclipse Foundation
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

class App {
    constructor() {
        this.config = {
            "thingId": "org.eclipse.ditto:smartcoffee",
            "thingJson": {
                "attributes": {
                    "manufacturer": "Ditto demo device corp.",
                    "model": "Speaking coffee machine"
                },
                "features": {
                    "water-tank": {
                        "properties": {
                            "configuration": {
                                "smartMode": true,
                                "brewingTemp": 87,
                                "tempToHold": 44,
                                "timeoutSeconds": 6000
                            },
                            "status": {
                                "waterAmount": 731,
                                "temperature": 44
                            }
                        }
                    },
                    "coffee-brewer": {
                        "properties": {
                            "brewed-coffees": 0
                        }
                    }
                }
            },
            "waterTank": {
                "feature": "water-tank",
                "onSubject": "startHeating",
                "onPayload": {
                    "temperature": 85
                },
                "offSubject": "stopHeating",
                "offPayload": {},
                "successResponse": true,
                "failureResponse": false
            },
            "coffeeMachine": {
                "makeCoffeeSubject": "makeCoffee",
                "makeCoffeePayload": {
                    "cups": 1,
                    "strength": 0.8,
                    "amount": 230,
                    "captcha": ""
                }
            }
        };

        this.frontend = new FrontendApp(this.config, () => this.getConnectionConfig());
        this.smartCoffeeApp = new SmartCoffeeApp(this.config, () => this.getConnectionConfig());
    }

    getConnectionConfig() {
        return new ConnectionConfig($('#dittoHost').val(),
            $('#dittoUser').val(),
            $('#dittoPassword').val());
    }
}

class ConnectionConfig {
    constructor(host, username, password) {
        this.host = host;
        this.username = username;
        this.password = password;
    }

    getHost() {
        return this.host;
    }

    getUsername() {
        return this.username;
    }

    getPassword() {
        return this.password;
    }
}

// Startup
$(document).ready(new App());
