/*
    Copyright (c) 2017 Bosch Software Innovations GmbH.

    All rights reserved. This program and the accompanying materials
    are made available under the terms of the Eclipse Public License v2.0
    which accompanies this distribution, and is available at
    https://www.eclipse.org/org/documents/epl-2.0/index.php

    Contributors:
    Bosch Software Innovations GmbH - initial contribution
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
