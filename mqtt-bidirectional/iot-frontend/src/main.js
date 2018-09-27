import Vue from 'vue'
import App from './App.vue'

// require('./assets/sass/main.scss')

require('bootstrap/scss/bootstrap.scss')

Vue.config.productionTip = false

new Vue({
  render: h => h(App)
}).$mount('#app')
