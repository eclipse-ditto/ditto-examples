<template>
    <div class="card shadow">
        <div class="card-body">
            <button @click="sendClaim" type="button" class="btn btn-primary">Claim</button>
            <button @click="sendMsg" type="button" class="btn btn-primary">sendMsg</button>
        </div>
    </div>
</template>

<script>

import axios from 'axios'

export default {
    name: 'MessageView',
    data() {
        return {
            baseAddress: "http://localhost:8080/api/2",
            auth: {
                "Authorization": "Basic ZGF2ZTpiZXJ0YWJlbGxh"
            },
            thingId: "joos.test:thing",
        }
    },
    methods: {
        sendClaim(){

            console.log("send: ", `${this.baseAddress}/things/${this.thingId}/inbox/claim`)
            let message = "hello world"
            axios
            .post(`${this.baseAddress}/things/${this.thingId}/inbox/claim`, message, {
                headers: this.auth,
                'content-type': 'text/plain'
            })
            .then( res => {
                alert("Yeah man!")
                console.log(res)
            })
            .catch( err => {
                console.log(JSON.stringify(err))
            })


        },
        sendMsg(){
            console.log("send: ", `${this.baseAddress}/things/${this.thingId}/inbox/messages/ditto-tutorial`)

            let message = {
                "Kompletter": "Test"
            }
            axios
            .post(`${this.baseAddress}/things/${this.thingId}/inbox/messages/ditto-test`, message, {
                headers: this.auth,
                'content-type': 'application/json'
            })
            .then( res => {
                alert("Yeah man!")
                console.log(res)
            })
            .catch( err => {
                console.log(JSON.stringify(err))
            })
        }
    }
}

</script>

<style>

</style>