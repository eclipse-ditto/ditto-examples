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
        // start listening with a little timeout
        setTimeout(() => {
            this.source = []
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
        }, 1000);
    }

    fire(event, data = null) {
        this.vue.$emit(event, data)
    }

    listen(event, callback) {
        this.vue.$on(event, callback)
    }
}