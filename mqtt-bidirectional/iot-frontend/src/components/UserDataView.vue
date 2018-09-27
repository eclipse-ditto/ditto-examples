<template>
    <div class="card shadow">
        <div class="card-header">
            User configuration
        </div>
        <div class="card-body">
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
                <!-- <button @click="commitChanges" type="button" class="btn btn-primary">Commit</button> -->
            </form>
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
            }
        },
        methods: {
            setUserData(event){
                this.data.filter(object => { return object.key == event.target.id })[0].value = event.target.value
                this.$store.commit('setUserData', { userdata: this.data })
            },
            commitChanges(){
                console.log("Userdata: ", this.data)
            }
        }
    }
</script>

<style>
</style>