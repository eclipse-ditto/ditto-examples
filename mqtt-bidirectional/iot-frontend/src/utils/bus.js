import Vue from 'vue'
import store from '../store'

export default window.Event = new class {
    constructor() {
        this.vue = new Vue({
            name: 'eventbus',
            store
        });

        this.source = new EventSource('http://localhost:8080/api/2/things?ids=joos.test:octopus', { 
            withCredentials: true
        })
        this.source.onmessage = event => {
            this.vue.$store.commit('incrementTelemetryCount')
            this.vue.$store.dispatch('telemetryUpdate')
        }
    }

    fire(event, data = null) {
        this.vue.$emit(event, data)
    }

    listen(event, callback) {
        this.vue.$on(event, callback)
    }
}