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
import Vue from 'vue'
import store from '../store'

export default window.Event = new class {
    constructor() {
        this.vue = new Vue({
            name: 'eventbus',
            store,
            computed: {
                userData: {
                    get() {
                        return this.$store.getters.getUserData
                    }
                },
                selected: {
                    get() {
                        return this.$store.getters.getSelected
                    }
                },
                items: {
                    get() {
                        return this.$store.getters.getItems
                    }
                }
            }
        })
        this.source = []
    }

    fire(event, data = null) {
        if (event === 'initSSE'){
            // start listening with a little timeout
            this.vue.items.forEach(element => {
                this.source.push(new EventSource(`${this.vue.userData[3].value}/api/2/things?=${element.thingId}`, {
                    withCredentials: true
                }))
            })
            this.source.forEach(element => {
                element.onmessage = () => {
                    this.vue.$store.commit('incrementTelemetryCount')
                    this.vue.$store.dispatch('telemetryUpdate')
                }
            })
        } else if (event === 'connectionError') {
            this.source.forEach(element => {
                element.close()
            })
        } else {
            this.vue.$emit(event, data)
        }
    }

    listen(event, callback) {
        this.vue.$on(event, callback)
    }
}
