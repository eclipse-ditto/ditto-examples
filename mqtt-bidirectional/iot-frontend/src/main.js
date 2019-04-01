/*
 * Copyright (c) 2017 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import Vue from 'vue'
import App from './App.vue'

require('./assets/sass/main.scss')

Vue.config.productionTip = false

new Vue({
  render: h => h(App)
}).$mount('#app')
