/* eslint-disable */
<!--
  ~ Copyright (c) 2017 Contributors to the Eclipse Foundation
  ~
  ~ See the NOTICE file(s) distributed with this work for additional
  ~ information regarding copyright ownership.
  ~
  ~ This program and the accompanying materials are made available under the
  ~ terms of the Eclipse Public License 2.0 which is available at
  ~ http://www.eclipse.org/legal/epl-2.0
  ~
  ~ SPDX-License-Identifier: EPL-2.0
  -->

<template>

    <div class="list-group shadow">
        <a v-bind:key="item.thingId" v-for="item in items" @click="select(item, $event)" href="#/" :class="item.thingId === isActiveId ? cssIsActive : cssIsNotActive">
            <div class="d-flex w-100 justify-content-between">
            <h5 class="mb-1">{{ item.thingId }}</h5>
            <small>{{ item.policyId }}</small>
            </div>
            <p class="mb-1" >{{ item.attributes.type || 'no type' }}</p>
        </a>
        <a @click="select('newThing', $event)" class="list-group-item list-group-item-action flex-column align-items-start">
            <div class="d-flex w-100 justify-content-between">
                <p class="h5">Create new thing</p>
            </div>
        </a>
    </div>

</template>

<script>

export default {
    name: "ThingList",
    data() {
        return {
            cssIsActive: "list-group-item list-group-item-action flex-column align-items-start active",
            cssIsNotActive: "list-group-item list-group-item-action flex-column align-items-start",
            isActiveId: ''
        };
    },
    methods: {
        select(thing) {
            if (thing === 'newThing') thing = this.createNewThingTemplate()
            this.$store.dispatch('handleSelected', thing)
            .then( res => {
                if (res.status == 200){
                    this.isActiveId = thing.thingId
                }
            })
        },
        createNewThingTemplate() {
            let namespace = this.userdata.filter(object => { return object.key == 'namespace' })[0].value
            let template = `{"thingId": "${namespace}:<newThing>", "policyId": "${namespace}:<policy>", "attributes": {"type": null}, "features": {"featureA": {"properties": {"propertyA": "Hello World"}}}}`
            return JSON.parse(template)
        }
    },
    mounted() {
        this.$store.dispatch('getAllThings')
    },
    computed: {
        items: {
            get() {
                return this.$store.getters.getItems
            }
        },
        userdata: {
            get() {
                return this.$store.getters.getUserData
            }
        }
    }
};
</script>

<style>

</style>
