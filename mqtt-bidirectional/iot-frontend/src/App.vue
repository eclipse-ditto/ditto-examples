<template>
    <div>
        <!-- As a heading -->
        <nav class="navbar navbar-light bg-light shadow">
            <span class="navbar-brand mb-0 h1">Ditto</span>
        </nav>
        <div class="container-fluid" style="margin-top: 25px">
            <div class="row">
                <div class="col">
                    <UserDataView></UserDataView>
                </div>
                <div class="col">
                    <ModifyThing></ModifyThing>
                </div>
                <div class="col">
                    <ThingList></ThingList>
                </div>
            </div>
        </div>
    </div>
</template>

<script>

import UserDataView from './components/UserDataView.vue'
import ModifyThing from './components/ModifyThing.vue'
import ThingList from './components/ThingList.vue'
import store from './store'

import 'es6-promise/auto'
import './utils/bus.js'

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
#app {
  font-family: "Avenir", Helvetica, Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  text-align: center;
  color: #2c3e50;
  margin-top: 60px;
}
</style>
