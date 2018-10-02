<template>
    <div class="card shadow">
        <div class="card-body" v-show="isSelected.thingId === undefined && isSelected !== 'newItem'">No Thing selected</div>
        <div v-show="isSelected.thingId !== undefined || isSelected === 'newItem'">
            <div class="card-header lead" v-show="isSelected.thingId !== undefined && isSelected.thingId !== 'newThing'">{{ isSelected.thingId }}</div>
            <div class="card-header lead" v-show="isSelected.thingId === 'newThing'"><input class="form-control" v-model="isSelected.thingId"></div>
            <codemirror
                :options="cmOptions"
                :value="JSON.stringify(isSelected, null, '\t')"
                @input="updateThing($event)"
                class="border-bottom"
            ></codemirror>
            <div class="card-body">
                <div v-show="success" class="alert alert-success" role="alert">
                    Successfully saved!
                </div>
                <div v-show="error" class="alert alert-danger" role="alert">
                    {{ errorMessage }}
                </div>
                <div class="form-row container-fluid justify-content-between">
                    <div class="col-md-4">
                        <button @click="saveChanges" v-show="isSelected.thingId !== undefined" type="button" class="btn btn-outline-success form-control">Save changes</button>
                    </div>
                    <div class="col-md-4">
                        <button @click="deleteThing" v-show="isSelected.thingId !== undefined && isSelected.thingId !== userData[2].value + ':<newThing>'" type="button" class="btn btn-outline-danger form-control">Delete Thing</button>
                    </div>
                </div>
                <hr/>
                <div class="container-fluid" v-show="isSelected.thingId !== undefined && isSelected.thingId !== userData[2].value + ':<newThing>'">
                    <div class="row justify-content-center">
                        <form class="form-row align-items-end">
                            <div class="form-group col-md-4" style="margin-bottom: 16px">
                                <button id="sendButton" @click="sendMessage" type="button" class="btn btn-outline-primary form-control">Send message</button>
                            </div>
                            <div class="form-group col-md-4">
                                <label for="inputTopic">Topic</label>
                                <input id="inputTopic" type="text" class="form-control" placeholder="Subject" v-model="subject">
                            </div>
                            <div class="form-group col-md-4">
                                <label for="inputPayload">Payload</label>
                                <input id="inputPayload" type="text" class="form-control" placeholder="payload" v-model="payload">
                            </div>
                        </form>
                    </div>
                    <hr />
                    <div class="row" style="margin-top: 15px">
                        <div class="container">
                            <p>
                                <span class="lead">Message preview:</span>
                            </p>
                            <p>
                                <code>
                                    <p>
                                        POST
                                    </p>
                                    <p>
                                        {{ userData[3].value }}/api/2/things/{{ isSelected.thingId }}/inbox/messages/{{ subject }}
                                    </p>
                                    <p>
                                        Payload: {{ payload }}
                                    </p>
                                    <p>
                                        Headers: {{ auth }}
                                    </p>
                                </code>
                            </p>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<script>
/* eslint-disable */

import { codemirror } from 'vue-codemirror'
import 'codemirror/mode/javascript/javascript.js'
import 'codemirror/lib/codemirror.css'
import 'codemirror/theme/idea.css'
import'codemirror/addon/selection/active-line.js'

export default {
    name: "ModifyThing",
    components: {
        codemirror
    },
    data() {
        return {
            localcopy: {},
            cmOptions: {
                theme: 'idea',
                tabSize: 2,
                mode: 'application/json',
                lineNumbers: true,
                matchBrackets: true,
                line: true,
                lineWrapping: true,
            },
            success: false,
            error: false,
            errorMessage: "",
            subject: "LED",
            payload: 'on'
        }
    },
    computed: {
        isSelected: {
            get() {
                return this.$store.getters.getSelected
            },
            set(value) {
                this.$store.commit('setSelected', value)
            }
        },
        userData: {
            get() {
                return this.$store.getters.getUserData
            } 
        },
        auth: {
            get() {
                return this.$store.getters.getAuth
            }
        }
    },
    methods: {
        saveChanges() {
            let newThing = JSON.parse(this.localcopy)
            this.$store.dispatch('saveChanges', newThing)
            .then(res => {
                if (res.status == 201 || res.status == 204) {
                    this.success = true
                    setTimeout(() => {
                        this.$store.dispatch('getAllThings')
                        .then( res => {
                            this.success = false
                        })
                    }, 2000)
                }
            })
            .catch( err => {
                this.errorMessage = err.response.statusText
                this.error = true
                setTimeout(() => {
                    this.errorMessage = ""
                    this.error = false
                }, 2000)
            })
        },
        deleteThing(){
            this.$store.dispatch('deleteThing', this.isSelected)
        },
        updateThing(event) {
            this.localcopy = event
        },
        sendMessage() {
            this.$store.dispatch('sendMessage', [this.subject, this.payload])
            .then( res => {
                console.log(res)
            })
            .catch( err => {
                this.errMessage = err.response.statusText
                this.error = true
                setTimeout(() => {
                    this.errorMessage = ""
                    this.error = false
                })
            })
        }
    },
}
</script>

<style>
</style>