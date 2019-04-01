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
    <div>
        <nav class="navbar fixed-top navbar-light bg-primary shadow">
            <a class="navbar-brand" href="#">
                <img src="../public/ditto_allwhite_symbolonly.svg" width="80" height="40" alt="">
                <img src="../public/ditto_allwhite_textonly.svg" width="60" height="40" alt="">
            </a>
            <span class="navbar-text lead" style="color: white">
                MQTT Example - Frontend
            </span>
        </nav>
        <div class="container" style="margin-top: 95px; margin-bottom: 45px">
            <div class="row">
                <div class="col">
                    <UserDataView></UserDataView>
                </div>
            </div>
            <div class="row" style="margin-top: 25px">
                <div class="col-lg-6">
                    <ModifyThing></ModifyThing>
                </div>
                <div class="col-lg-6">
                    <ThingList></ThingList>
                </div>
            </div>
        </div>
            <nav class="navbar fixed-bottom navbar-light bg-primary" style="margin-top: 25px">
        </nav>
    </div>
</template>

<script>
    import UserDataView from "./components/UserDataView.vue";
    import ModifyThing from "./components/ModifyThing.vue";
    import ThingList from "./components/ThingList.vue";
    import store from "./store";

    import "es6-promise/auto";
    import "./utils/bus.js";

    export default {
  name: "app",
  store,
  components: {
    UserDataView,
    ModifyThing,
    ThingList
  }
};
</script>

<style>
</style>
