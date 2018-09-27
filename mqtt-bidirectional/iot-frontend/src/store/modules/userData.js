/* eslint-disable */

const state = {
    userdata: [
        { key: "username", label: "Ditto Auth User", value: "dave", type: "text", placeholder: "e.g John Doe"},
        { key: "password", label: "Ditto Auth Pass", value: "bertabella", type: "password", placeholder: "your password"},
        { key: "namespace", label: "Ditto Namespace", value: "joos.test", type: "text", placeholder: "e.g com.bosch"},
        { key: "hostaddress", label: "Ditto Hostaddr.", value: "http://localhost:8080", type: "text", placeholder: "e.g http://mydittoinstance.com"},
    ]
}

const getters = {
    getUserData: state => {
        return state.userdata
    }
}

// const mutations = {

// }

// const actions = {
    
// }