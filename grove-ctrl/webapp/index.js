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
    constructor(baseUrl = 'http://localhost:8080', username = 'ditto', password = 'ditto') {
        this.baseUrl = baseUrl

        this.username = username
        this.password = password
        
        this.thingId = "org.eclipse.ditto.example:raspberry"
        this.useSSE = false
        this.refreshIntervalMsecs = 100

        this.intervalId = null
        this.eventSource = null
        this.eventListener = (e) => { this.handleMessage(e) }
    }


    onRefresh() {
        this.requestGetFeature('IlluminanceSensor_0',
            (data, textStatus, jqXHR) => { this.updateIlluminance(data, textStatus, jqXHR) },
            (jqXHR, textStatus, errorThrown) => {
                this.enableAutoRefresh(false)
                this.pushLog('danger', `Error retrieving illuminance: ${errorThrown}. Auto refresh stopped, please reload page.`)
            }
        )
        this.requestGetFeature('Buzzer_0',
            (data, textStatus, jqXHR) => { this.updateBuzzer(data, textStatus, jqXHR) },
            (jqXHR, textStatus, errorThrown) => {
                this.enableAutoRefresh(false)
                this.pushLog('danger', `Error retrieving buzzer: ${errorThrown}. Auto refresh stopped, please reload page.`)
            }
        )
        this.requestGetFeature('TemperatureHumiditySensor_0',
            (data, textStatus, jqXHR) => { this.updateTemperatureHumidity(data, textStatus, jqXHR) },
            (jqXHR, textStatus, errorThrown) => {
                this.enableAutoRefresh(false)
                this.pushLog('danger', `Error retrieving temperature and humidity: ${errorThrown}. Auto refresh stopped, please reload page.`)
            }
        )
    }

    onSaveChanges() {
        this.enableAutoRefresh(false)
        this.enableEventSource(false)
        this.updateConfig()
        this.applyUpdateStrategy()
        $('#configureModal').modal('hide')
    }

    onConfigure() {
        this.updateModal()
    }

    onEnable() {
        this.requestMessageToFeature('Buzzer_0', 'doEnable', JSON.stringify(true),
            () => { },
            (jqXHR, textStatus, errorThrown) => { this.pushLog('danger', `Error sending enable message: ${errorThrown}`) }
        );
    }

    onDisable() {
        this.requestMessageToFeature('Buzzer_0', 'doEnable', JSON.stringify(false),
            () => { },
            (jqXHR, textStatus, errorThrown) => { this.pushLog('danger', `Error sending disable message: ${errorThrown}`) }
        );
    }

    onApplyTemperatureHumiditySampingRate() {
        this.requestSetProperty('TemperatureHumiditySensor_0', 'samplingRate', JSON.stringify(parseInt($("#selectTemperatureHumiditySampleRate option:selected").val())),
            () => { },
            (jqXHR, textStatus, errorThrown) => { this.pushLog('danger', `Error sending sample rate update: ${errorThrown}`) }
        );
    }

    onApply() {
        this.requestSetProperty('IlluminanceSensor_0', 'samplingRate', JSON.stringify(parseInt($("#selectSampleRate option:selected").val())),
            () => { },
            (jqXHR, textStatus, errorThrown) => { this.pushLog('danger', `Error sending sample rate update: ${errorThrown}`) }
        );
    }

    onEvent(data) {
        doIfDefined(data.features.IlluminanceSensor_0, this.updateIlluminance)
        doIfDefined(data.features.Buzzer_0, this.updateBuzzer)
        doIfDefined(data.features.TemperatureHumiditySensor_0, this.updateTemperatureHumidity)
    }

    requestGetAttributes(success, error) {
        $.getJSON(`${this.baseUrl}/api/1/things/${this.thingId}/attributes`)
            .fail((jqXHR, textStatus, errorThrown) => { error(jqXHR, textStatus, errorThrown) })
            .done((data, textStatus, jqXHR) => { success(data, textStatus, jqXHR) })
    }

    requestGetFeature(featureId, success, error) {
        $.getJSON(`${this.baseUrl}/api/1/things/${this.thingId}/features/${featureId}`)
            .fail((jqXHR, textStatus, errorThrown) => { error(jqXHR, textStatus, errorThrown) })
            .done((data, textStatus, jqXHR) => { success(data, textStatus, jqXHR) })
    }


    requestSetProperty(featureId, propertyId, data, success, error) {
        $.post({
            type: "PUT",
            url: `${this.baseUrl}/api/1/things/${this.thingId}/features/${featureId}/properties/${propertyId}`,
            data: data,
            contentType: "application/json; charset=utf-8",
        })
            .fail((jqXHR, textStatus, errorThrown) => { error(jqXHR, textStatus, errorThrown) })
            .done((data, textStatus, jqXHR) => { success(data, textStatus, jqXHR) })
    }

    requestMessageToFeature(featureId, messageSubject, data, success, error) {
        $.post({
            type: "POST",
            url: `${this.baseUrl}/api/1/things/${this.thingId}/features/${featureId}/inbox/messages/${messageSubject}?timeout=0`,
            data: data,
            contentType: "application/json; charset=utf-8",
        })
            .fail((jqXHR, textStatus, errorThrown) => { error(jqXHR, textStatus, errorThrown) })
            .done((data, textStatus, jqXHR) => { success(data, textStatus, jqXHR) })
    }


    updateIlluminance(data, textStatus, jqXHR) {
        doIfDefined(data.properties.sensorValue, (d) => {$('#illuminanceValue').html(`<span>${d}</span>`)})
        doIfDefined(data.properties.lastUpdate, (d) => {$('#illuminanceUpdate').html(`<span>${d}</span>`)})
        doIfDefined(data.properties.samplingRate, (d) => {$('#samplingRate').html(`<span>${d} Hz</span>`)})
    }

    updateBuzzer(data, textStatus, jqXHR) {
        if (isDefined(data.properties.buzz) && data.properties.buzz) {
            $("#buzzValue").removeClass('fa-volume-off')
            $("#buzzValue").addClass('fa-volume-up')
        } else {
            $("#buzzValue").removeClass('fa-volume-up')
            $("#buzzValue").addClass('fa-volume-off')
        }
    }

    updateTemperatureHumidity(data, textStatus, jqXHR) {
        doIfDefined(data.properties.temperatureValue, (d) => {$('#temperatureValue').html(`<span>${d} °</span>`)})
        doIfDefined(data.properties.humidityValue, (d) => {$('#humidityValue').html(`<span>${d} %</span>`)})
        doIfDefined(data.properties.lastUpdate, (d) => {$('#temperatureHumidityUpdate').html(`<span>${d}</span>`)})
        doIfDefined(data.properties.samplingRate, (d) => {$('#temperatureHumiditySamplingRate').html(`<span>${d} Hz</span>`)})
    }

    updateDeviceInfo(data, textStatus, jqXHR) {
        $("#deviceInfo").html('')
        for (var k in data) {
            if (data.hasOwnProperty(k)) {
                $("#deviceInfo").append(`<dt>${k}</dt><dd>${data[k]}</dd>`)
            }
        }
    }

    pushLog(role, message) {
        $("#alerts").append(
            `<div class="alert alert-${role} alert-dismissible fade show" role="alert">
                <button type="button" class="close" data-dismiss="alert" aria-label="Close">
                    <span aria-hidden="true">&times;</span>
                    </button>
                    ${message}
             </div>`)
    }

    enableAutoRefresh(enabled = true) {
        if (enabled) {
            this.intervalId = setInterval(() => { this.onRefresh(); }, this.refreshIntervalMsecs)
        } else {
            clearInterval(this.intervalId)
        }
    }

    applyUpdateStrategy() {
        if (this.useSSE) {
            this.enableEventSource()
        } else {
            this.enableAutoRefresh()
        }
    }

    enableEventSource(enabled = true) {
        if (enabled) {
            this.eventSource = new EventSource(`${this.baseUrl}/api/1/things?ids=${this.thingId}`)
            this.eventSource.addEventListener('message', this.eventListener)
        } else if (this.eventSource != null) {
            this.eventSource.removeEventListener('message', this.eventListener)
            this.eventSource.close()
            this.eventSource = null
        }
    }

    handleMessage(e) {
        if (e.data != null && !e.data == "") {
            var data = JSON.parse(e.data)
            if (data.thingId == this.thingId) {
                this.onEvent(data)
            }

        }
    }


    updateConfig() {
        this.baseUrl = $('#dittoUrl').val()
        this.username = $('#dittoUser').val()
        this.password = $('#dittoPassword').val()
        this.thingId = $('#dittoThingId').val()
        this.refreshIntervalMsecs = $('#refreshInterval').val()
        this.useSSE = $("#useSSE").prop('checked')

        $.ajaxSetup({
            headers: {
                Authorization: 'Basic ' + btoa(`${this.username}:${this.password}`),
            }
        })
    }

    updateModal() {
        $('#dittoUrl').val(this.baseUrl)
        $('#dittoUser').val(this.username)
        $('#dittoPassword').val(this.password)
        $('#dittoThingId').val(this.thingId)
        $('#refreshInterval').val(this.refreshIntervalMsecs)
        $("#useSSE").prop('checked', this.useSSE)
    }

    main() {
        $('#saveChanges').click(() => { this.onSaveChanges() })
        $('#configure').click(() => { this.onConfigure() })

        $('#setBuzzerEnabledTrue').click(() => { this.onEnable() })
        $('#setBuzzerEnabledFalse').click(() => { this.onDisable() })
        $('#applySampingRate').click(() => { this.onApply() })
        $('#applyTemperatureHumiditySampingRate').click(() => { this.onApplyTemperatureHumiditySampingRate() })

        this.updateModal()
        this.updateConfig()

        // initial load of attributes and features
        this.requestGetAttributes(this.updateDeviceInfo,
            () => { },
            (jqXHR, textStatus, errorThrown) => { this.pushLog('danger', `Error retrieving device info: ${errorThrown}`) }
        )
        this.onRefresh()

        this.applyUpdateStrategy()
    }
}

isDefined = function(ref) {
    return typeof ref != 'undefined' 
}

doIfDefined = function(ref, action) {
    if (isDefined(ref)) {
        action(ref)
    }
}

// Startup
$(document).ready(new App().main());
