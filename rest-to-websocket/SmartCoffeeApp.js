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

class SmartCoffeeApp {

    constructor(config, connectionConfigFunction) {
        this.config = config;
        this.features = this.config.thingJson.features;
        this.connectionConfigFunction = connectionConfigFunction;
        this.initUI();
        this.initWebSocket();
    }

    initUI() {
        this.firstConnection = true;
        this.uiSmartCoffeeId = "#smartCoffee";
        this.registerUICallbacks();
    }

    initWebSocket() {
        const pingInterval = 60000;
        this.thing = new DittoWebSocket(pingInterval);
        this.thing.setOnMessage((message) => this.onMessage(message));
        this.thing.setOnClosed(() => this.onClosed());
    }


    registerForMessages() {
        this.thing.sendRaw('START-SEND-MESSAGES');
        this.logSendToUI(undefined, 'START-SEND-MESSAGES', '', 'tell Ditto that i want to receive Messages');
    }

    /* ***************************************************************** */
    /* ********************* UI callbacks ****************************** */
    /* ***************************************************************** */

    registerUICallbacks() {
        $('#connect').click(() => this.connect(this.connectionConfigFunction()));
        $(`${this.uiSmartCoffeeId} .dismiss-all`).click(() => this.dismissAllUILogs());
    }

    connect(connectionConfig) {
        if (!this.firstConnection) {
            this.thing.close();
        }

        this.markAsConnecting();
        this.thing.connect(connectionConfig, () => this.onConnected());
    }

    dismissAllUILogs() {
        $(`${this.uiSmartCoffeeId} .alert>button`).click();
    }

    /* ***************************************************************** */
    /* ******************* WebSocket callbacks ************************* */
    /* ***************************************************************** */

    onConnected() {
        this.logToConsole('connected');
        this.registerForMessages();
    }

    onClosed() {
        this.markAsClosed();
        this.logToConsole('connection closed.');
    }

    onMessage(message) {
        const data = message.data;
        this.logToConsole(`received: ${data}`);
        this.logReceiveToUI(data);

        if (this.isStartSendMessagesAck(data)) {
            this.onRegisteredForMessages();
        } else {
            try {
                const jsonMessage = JSON.parse(message.data);
                this.onJsonMessage(jsonMessage);
            } catch (err) {
                this.logToConsole(`error while handling message: ${err}`);
            }
        }
    }

    /* ***************************************************************** */
    /* ******************* Message handling **************************** */
    /* ***************************************************************** */

    onJsonMessage(jsonMessage) {
        if (this.isMakeCoffeeMessage(jsonMessage)) {
            this.onMakeCoffeeMessage(jsonMessage);
        } else if (this.isTurnOnWaterTankMessage(jsonMessage)) {
            this.onTurnOnWaterTankMessage(jsonMessage);
        } else if (this.isTurnOffWaterTankMessage(jsonMessage)) {
            this.onTurnOffWaterTankMessage(jsonMessage);
        } else {
            this.logToConsole(`unhandled topic: ${jsonMessage.topic}`);
        }
    }

    isMakeCoffeeMessage(jsonMessage) {
        const thingIdAsPath = this.toPath(this.config.thingId);
        const makeCoffeeSubject = this.config.coffeeMachine.makeCoffeeSubject;
        const makeCoffeeTopic = `${thingIdAsPath}/things/live/messages/${makeCoffeeSubject}`;

        return makeCoffeeTopic === jsonMessage.topic;
    }

    isTurnOnWaterTankMessage(jsonMessage) {
        const thingIdAsPath = this.toPath(this.config.thingId);
        const turnOnWaterTankSubject = this.config.waterTank.onSubject;
        const waterTankFeature = this.config.waterTank.feature;
        const turnOnWaterTankTopic = `${thingIdAsPath}/things/live/messages/${turnOnWaterTankSubject}`;
        const turnOnWaterPath = `/features/${waterTankFeature}/inbox/messages/${turnOnWaterTankSubject}`;

        return turnOnWaterTankTopic === jsonMessage.topic &&
            turnOnWaterPath === jsonMessage.path;
    }

    isTurnOffWaterTankMessage(jsonMessage) {
        const thingIdAsPath = this.toPath(this.config.thingId);
        const turnOffWaterTankSubject = this.config.waterTank.offSubject;
        const waterTankFeature = this.config.waterTank.feature;

        const turnOffWaterTankTopic = `${thingIdAsPath}/things/live/messages/${turnOffWaterTankSubject}`;
        const turnOffWaterPath = `/features/${waterTankFeature}/inbox/messages/${turnOffWaterTankSubject}`;

        return turnOffWaterTankTopic === jsonMessage.topic &&
            turnOffWaterPath === jsonMessage.path;
    }

    onMakeCoffeeMessage(jsonMessage) {
        if (this.isMakeCoffeeCaptchaCorrect(jsonMessage)) {
            // captcha was correctly solved -> brew the coffee
            this.brewCoffee();

            const successResponse = JSON.stringify({
                'captchaSolved': true
            });
            this.logSendToUI(successResponse, 200, jsonMessage.topic, 'inform that coffee is brewed');
            this.thing.reply(jsonMessage,
                successResponse,
                "application/json",
                200);
        } else {
            // captcha is incorrect -> reply with a new captcha image
            const captcha = this.createCaptchaMessage(b64Captcha());
            this.logSendToUI(this.createCaptchaMessage('[b64-encoded-captcha-image]'), 400, jsonMessage.topic, 'tell requester to solve captcha to be able to brew a coffee');
            this.thing.reply(jsonMessage,
                captcha,
                'image/png',
                400);
        }
    }

    isMakeCoffeeCaptchaCorrect(jsonMessage) {
        return 'ditto' === jsonMessage.value.captcha;
    }

    brewCoffee() {
        // update ui
        this.markCoffeeAsBrewing();

        // update brewing counter
        this.features['coffee-brewer'].properties['brewed-coffees']++;

        // update brewing counter of twin
        const thingIdAsPath = this.toPath(this.config.thingId);
        const updateFeatureMessage = this.thing.protocolEnvelope(
            `${thingIdAsPath}/things/twin/commands/modify`,
            'features/coffee-brewer/properties/brewed-coffees',
            this.features['coffee-brewer'].properties['brewed-coffees']
        );
        this.logSendToUI(JSON.stringify(updateFeatureMessage), '', updateFeatureMessage.topic, 'update the brewing counter of my twin representation');
        this.thing.send(updateFeatureMessage);
    }

    createCaptchaMessage(imageToSolve) {
        return imageToSolve;
    }

    onTurnOnWaterTankMessage(jsonMessage) {
        this.thing.reply(jsonMessage, this.config.waterTank.successResponse, "application/json", 200);
        this.logSendToUI(this.config.waterTank.successResponse, 200, jsonMessage.topic, 'tell requester that water tank was successfully turned on');
        this.markWaterAsActive();
    }

    onTurnOffWaterTankMessage(jsonMessage) {
        this.thing.reply(jsonMessage, this.config.waterTank.successResponse, "application/json", 200);
        this.logSendToUI(this.config.waterTank.successResponse, 200, jsonMessage.topic, 'tell requester that water tank was successfully turned off');
        this.markWaterAsInactive();
    }

    onRegisteredForMessages() {
        this.logToConsole('registered for messages');
        this.markAsConnected();
        if (this.firstConnection) {
            this.renameConnectButton('Reconnect');
            this.firstConnection = false;
        }
    }

    toPath(thingId) {
        return thingId.replace(':', '/');
    }

    isStartSendMessagesAck(message) {
        return message === 'START-SEND-MESSAGES:ACK';
    }

    /* ********************************************************************** */
    /* ******************               UI            *********************** */
    /* ********************************************************************** */

    logToConsole(message) {
        console.log(`[${this.config.thingId}] ${message}`);
    }

    renameConnectButton(newText) {
        $('#connect').html(newText);
    }

    markCoffeeAsBrewing() {
        $("#coffee").addClass("active");

        if (isDefined(this.timeoutId)) {
            clearTimeout(this.timeoutId);
            this.timeoutId = undefined;
        }
        // stop brewing after some seconds
        const newTimeoutId = setTimeout(
            () => {
                $("#coffee").removeClass("active");
                clearTimeout(this.timeoutId);
            },
            5000
        );
        this.timeoutId = newTimeoutId;
    }

    markWaterAsActive() {
        $("#water").addClass("active");
    }

    markWaterAsInactive() {
        $("#water").removeClass("active");
    }

    markAsConnected() {
        $("#wsinfo>.closed").hide();
        $("#wsinfo>.opening").hide();
        $("#wsinfo>.opened").show();
    }

    markAsClosed() {
        $("#wsinfo>.closed").show();
        $("#wsinfo>.opening").hide();
        $("#wsinfo>.opened").hide();
    }

    markAsConnecting() {
        $("#wsinfo>.closed").hide();
        $("#wsinfo>.opening").show();
        $("#wsinfo>.opened").hide();
    }

    logSendToUI(data, statusCode, topic, doc) {
        const htmlCmd = `<h4>⇦ ${isDefined(statusCode) ? statusCode : ''}</h4>
                      <p class="break-word">${isDefined(data) ? data : ''}</p>
                      <hr>
                      <div><small class="break-word">${topic}</small></div>
                      ${isDefined(doc) ? `<div><small class="break-word text-muted">${doc}</small></div>`: ''}`;
        this.logToUi('info', htmlCmd);
    }

    logReceiveToUI(data) {
        const htmlReceive = `<h4>⇨</h4>
                          <div class="break-word">${data}</div>`;
        this.logToUi('success', htmlReceive);
    }

    logToUi(role, message) {
        $("#sc-alerts").append(
            `<div class="alert alert-${role} alert-dismissible fade show" role="alert">
                <button type="button" class="close" data-dismiss="alert" aria-label="Close">
                    <span aria-hidden="true">&times;</span>
                    </button>
                    ${message}
             </div>`);
    }

}


class DittoWebSocket {

    constructor(pingInterval = 60000) {
        this.pingInterval = pingInterval;
        this.stopPing = false;
        this.clientCallbacks = {};
    }

    connect(connectionConfig, callback) {
        const baseUrl = `ws://${connectionConfig.getUsername()}:${connectionConfig.getPassword()}@${connectionConfig.getHost()}/ws/2`;
        this.ws = new WebSocket(baseUrl);
        this.ws.onopen = () => this.onOpen(callback);
    }

    close(callback) {
        this.ws.onclose = callback;
        this.ws.close();
    }

    onOpen(callback) {
        // define as functions, so the message is executed in current context
        this.ws.onmessage = (message) => this.onMessage(message);
        this.ws.onclose = () => this.onClosed();
        this.ws.onerror = (error) => this.onError(error);
        if (isDefined(callback)) {
            callback();
        }
        this.schedulePingMessage();
    }

    onMessage(message) {
        if (isDefined(this.clientCallbacks.onMessage)) {
            this.clientCallbacks.onMessage(message);
        }
    }

    onClosed() {
        this.stopPing = true;
        if (isDefined(this.clientCallbacks.onClosed)) {
            this.clientCallbacks.onClosed();
        }
    }

    onError(error) {
        console.log(`error: ${e}`);
        if (isDefined(this.clientCallbacks.onError)) {
            this.clientCallbacks.onError(error);
        }
    }


    sendRaw(content) {
        this.ws.send(content);
    }

    send(json) {
        console.log(`sending JSON ${JSON.stringify(json)}`);
        this.sendRaw(JSON.stringify(json));
    }

    reply(message, payload, contentType, status) {
        const response = Object.assign({
            status
        }, this.protocolMessage(
            message.headers['thing-id'],
            message.topic,
            message.headers.subject,
            message.headers['correlation-id'],
            contentType,
            "FROM",
            message.path.replace("inbox", "outbox"),
            payload
        ));

        this.send(response);
    }

    /**
     * Create a Ditto protocol WebSocket API Message.
     */
    protocolMessage(thingId, topic, subject, correlationId, contentType, direction, path, payload) {
        return Object.assign({
                "headers": {
                    "thing-id": thingId,
                    subject,
                    "correlation-id": correlationId,
                    "content-type": contentType,
                    direction
                }
            },
            this.protocolEnvelope(topic, path, payload)
        );
    }

    /*
     * Create a ditto protocol envelope.
     */
    protocolEnvelope(topic, path, value) {
        return {
            topic,
            path,
            value
        };
    }

    setOnMessage(onMessage) {
        this.clientCallbacks.onMessage = onMessage;
    }

    setOnClosed(onClosed) {
        this.clientCallbacks.onClosed = onClosed;
    }

    setOnError(onError) {
        this.clientCallbacks.onError = onError;
    }

    schedulePingMessage() {
        setTimeout(() => this.sendPingMessage(), this.pingInterval);
    }

    sendPingMessage() {
        if (this.stopPing) {
            this.stopPing = false;
        } else {
            this.sendRaw(new ArrayBuffer(0));
            this.schedulePingMessage();
        }
    }
}

isDefined = (arg) => typeof arg !== 'undefined';

b64Captcha = () => 'data:image/png;base64,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';
