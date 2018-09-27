import Vue from 'vue'
import Vuex from 'vuex'
import axios from 'axios'
import { putSelectedThing, deleteSelectedThing } from '../utils/api'

Vue.use(Vuex)

const createAuthHeader = (username, password) => {
    return "Basic " + btoa(encodeURIComponent(username + ":" + password).replace(/%([0-9A-F]{2})/g,
        function toSolidBytes(match, p1) {
            return String.fromCharCode('0x' + p1);
    }))
}

const accessUserData = userData => {
    let username = userData.filter(object => { return object.key == 'username' })[0].value
    let password = userData.filter(object => { return object.key == 'password' })[0].value
    let hostaddress = userData.filter(object => { return object.key == 'hostaddress' })[0].value
    let base64Auth = createAuthHeader(username, password)
    let config = {
        headers: {
            Authorization: base64Auth
        }
    }
    return { hostaddress, config }
}

const store = new Vuex.Store({

    state: {
        userdata: [
            { key: "username", label: "Ditto Auth User", value: "dave", type: "text", placeholder: "e.g John Doe"},
            { key: "password", label: "Ditto Auth Pass", value: "bertabella", type: "password", placeholder: "your password"},
            { key: "namespace", label: "Ditto Namespace", value: "joos.test", type: "text", placeholder: "e.g com.bosch"},
            { key: "hostaddress", label: "Ditto Hostaddr.", value: "http://localhost:8080", type: "text", placeholder: "e.g http://mydittoinstance.com"},
        ],
        items: [],
        selected: "No Thing selected",
    },
    
    getters: {
        getUserData: state => {
            return state.userdata
        },
        getSelected: state => {
            return state.selected
        },
        getItems: state => {
            return state.items
        },
        getAuth: state => {
            let username = state.userdata.filter(object => { return object.key == 'username' })[0].value
            let password = state.userdata.filter(object => { return object.key == 'password' })[0].value
            let base64Auth = createAuthHeader(username, password)
            return base64Auth
        }
    },

    mutations: {
        setSelected(state, thing){
            state.selected = thing
        },
        setItems(state, items) {
            state.items = items
        },
        setUserData(state, value) {
            state.userdata = Object.assign([], state.userdata, value.userdata)
        }
    },

    actions: {
        getAllThings ({ commit, state }) {
            return new Promise((resolve, reject) => {
                let { hostaddress, config } = accessUserData(state.userdata)
                axios
                .get(hostaddress + "/api/2/search/things", config)
                .then(response => {
                    if (response == null){
                        reject('[error] - Error getting things.')
                    }
                    this.commit('setItems', response.data.items)
                    resolve(200)
                })
                .catch(err => { reject(err) })
            })
        },
        handleSelected({commit}, thing){
            return new Promise((resolve, reject) => {
                this.commit('setSelected', thing)
                resolve({status: 200})
            })
        },
        saveChanges({commit, state}, thing){
            return new Promise((resolve, reject) => {
                let { hostaddress, config } = accessUserData(state.userdata)
                hostaddress = hostaddress + '/api/2/things/' + thing.thingId
                putSelectedThing(hostaddress, config, thing)
                .then(res => {
                    if (res.status == 201 || res.status == 204) {
                        this.dispatch('getAllThings')
                        resolve(res)
                    }
                })
                .catch(err => {
                    reject(err)
                })
            })
        },
        deleteThing({commit, state}, thing){
            return new Promise((resolve, reject) => {
                let { hostaddress, config } = accessUserData(state.userdata)
                hostaddress = hostaddress + '/api/2/things/' + thing.thingId
                deleteSelectedThing(hostaddress, config)
                .then( res => {
                    if (res.status == 204 ){
                        this.dispatch('getAllThings')
                        resolve(res)
                    }
                })
                .catch( err => {
                    console.log(err)
                    reject(err)
                })
            }) 
        },
        telemetryUpdate({commit}){
            this.dispatch('getAllThings')
        }
    }
})

export default store