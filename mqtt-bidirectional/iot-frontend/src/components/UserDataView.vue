<template>
    <div class="card shadow">
        <div class="card-header lead">
            USER-CONFIGURATION
        </div>
        <div class="card-body">
            <div class="row">
                <div class="col-sm-6">
                    <form>
                        <div class="form-group" v-for="value in data" v-bind:key="value.label">
                            <label :for="value.key">{{value.label}}</label>
                            <input 
                                :id="value.key" 
                                :type="value.type" 
                                :placeholder="value.placeholder" 
                                :value="value.value"
                                @input="setUserData($event)"
                                class="form-control"
                                >
                        </div>
                    </form>
                </div>
                <div class="col-sm-6">
                    <div class="jumbotron" style="height: 100%">
                        <p class="lead">Connection informations ..</p>
                        <hr class="my-4">
                        <p>Connected to Server -
                            <span v-show="connection == true" class="badge badge-success">Established</span>
                            <span v-show="connection == false" class="badge badge-danger">Error</span>
                        </p>
                        <p>Server sent events -
                            <span class="badge badge-info">{{ telemetryCount }}</span>
                        </p>
                        <button @click="initSSE" type="button" class="btn btn-primary">Init SSE</button>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<script>
    export default {
        name: "UserDataView",
        computed: {
            data: {
                get() {
                    return this.$store.getters.getUserData
                },
                set(value) {
                    this.$store.commit('setUserData', { value })
                }
            },
            connection: { 
                get() {
                    return this.$store.getters.getConnectionOkay
                }
            },
            telemetryCount: {
                get(){
                    return this.$store.getters.getTelemetryCount
                }
            }
        },
        methods: {
            setUserData(event){
                this.data.filter(object => { return object.key == event.target.id })[0].value = event.target.value
                this.$store.commit('setUserData', { userdata: this.data })
            },
            initSSE(event){
                Event.fire('initSSE')
            }
        }
    }
</script>

<style>
</style>