<template>
    <div class="card shadow">
        <div class="card-body" v-show="isSelected.thingId === undefined && isSelected !== 'newItem'">No Thing selected</div>
        <div v-show="isSelected.thingId !== undefined || isSelected === 'newItem'">
            <div class="card-header lead" v-show="isSelected.thingId !== undefined && isSelected.thingId !== 'newThing'">{{ isSelected.thingId }}</div>
            <div class="card-header lead" v-show="isSelected.thingId === 'newThing'"><input class="form-control" v-model="isSelected.thingId"></div>
            <div class="card-body">
                <codemirror
                    :options="cmOptions"
                    :value="JSON.stringify(isSelected, null, '\t')"
                    @input="updateThing($event)"
                    style="margin-top: 15px; margin-bottom: 25px"
                ></codemirror>
                <hr/>
                <div v-show="success" class="alert alert-success" role="alert">
                    Successfully saved!
                </div>
                <div v-show="error" class="alert alert-danger" role="alert">
                    {{ errorMessage }}
                </div>
                <div class="row justify-content-center">
                    <div class="col-4">
                        <button @click="saveChanges" v-show="isSelected.thingId !== undefined" type="button" class="btn btn-outline-success">Save changes</button>
                    </div>
                    <div class="col-4">
                        <!-- TODO -> Check for non valid delete button states -->
                        <button @click="deleteThing" v-show="isSelected.thingId !== undefined && isSelected.thingId !== isSelected.policyId + ':<newThing>'" type="button" class="btn btn-outline-danger">Delete Thing</button>
                    </div>
                </div>
                <hr/>
                <div class="row justify-content-center">
                    <div class="col-4">
                        <button @click="sendMessage" type="button" class="btn btn-primary">Send Message</button>
                    </div>
                    <div class="col-4">
                        <input type="text" class="form-control" placeholder="Subject" v-model="subject">
                    </div>
                    <div class="col-4">
                        <input type="text" class="form-control" placeholder="payload" v-model="payload">
                    </div>
                </div>
                <div class="row" style="margin-top: 15px">
                    <div class="container">
                        <span class="lead">Message preview:</span>
                        <p>
                            <code>
                                POST
                                <br/> 
                                {{ userData[3].value }}/api/2/things/{{ isSelected.thingId }}/inbox/messages/{{ subject }}
                                <br/>
                                Payload: {{ payload }}
                                <br/>
                                Headers: Authorization: {{ auth }}
                            </code>
                        </p>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<script>
import { codemirror } from 'vue-codemirror'
import 'codemirror/mode/javascript/javascript.js'
import 'codemirror/lib/codemirror.css'
import 'codemirror/theme/idea.css'
import'codemirror/addon/selection/active-line.js'

import axios from 'axios'

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
                        this.success = false
                    }, 3000)
                }
            })
            .catch( err => {
                this.errorMessage = err.response.statusText
                this.error = true
                setTimeout(() => {
                    this.errorMessage = ""
                    this.error = false
                }, 3000)
            })
        },
        deleteThing(){
            this.$store.dispatch('deleteThing', this.isSelected)
        },
        updateThing(event) {
            this.localcopy = event
        },
        sendMessage() {
            axios
            .post(`${this.userData[3].value}/api/2/things/${this.isSelected.thingId}/inbox/messages/${this.subject}`, '"' + this.payload + '"', {
                headers: {
                    Authorization: this.auth,
                    'content-type': 'application/json'
                }
            })
            .then( res => {
                console.log(res)
            })
            .catch( err => {
                console.log(JSON.stringify(err))
            })
        }
    },
}
</script>

<style>

</style>