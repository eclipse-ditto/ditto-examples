import axios from 'axios'

export function putSelectedThing (address, config, thing) {
    return axios.put(address, thing, config)
}

export function deleteSelectedThing (address, config) {
    return axios.delete(address, config)
}