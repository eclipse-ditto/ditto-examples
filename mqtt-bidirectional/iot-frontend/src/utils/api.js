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
import axios from 'axios'

export function putSelectedThing (address, config, thing) {
    return axios.put(address, thing, config)
}

export function deleteSelectedThing (address, config) {
    return axios.delete(address, config)
}
