/*
 * Copyright (c) 2021. Clyde Stubbs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.moonglass.ui.api

import kotlinx.serialization.Serializable

@Serializable
data class SignalType(
    val states: List<State>,
    val uuid: String // ee66270f-d9c6-4819-8b33-9720d4cbca6b
) {
    @Serializable
    data class State(
        val color: String, // #000000
        val name: String, // on
        val motion: Boolean,
        val value: Int
    )
}

@Serializable
data class Signal(
    val id: Int, // 1
    val shortName: String, // driveway motion
    val source: String,
    val cameras: Map<String, Association>,
    val days: Map<String, Api.Day> = mapOf(),
    val type: String // ee66270f-d9c6-4819-8b33-9720d4cbca6b
) {

    enum class Association {
        direct,
        indirect
    }
}
