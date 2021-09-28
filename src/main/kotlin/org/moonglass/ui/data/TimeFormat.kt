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

package org.moonglass.ui.data

import org.moonglass.ui.digits
import kotlin.js.Date

/**
 * Time formats. Just 24hr and am/pm
 * @param description Human-readable description (should be short)
 */
enum class TimeFormat(val description: String) {

    TwentyFourHr("24hr") {
        override fun Date.format(): String {
            val hours = getHours().digits(2)
            val minutes = getMinutes().digits(2)
            return listOf(hours, minutes).joinToString(":")
        }
    },
    AMPM("12hr") {
        override fun Date.format(): String {
            val hours = getHours()
            val ampm = if (hours >= 12) "PM" else "AM"
            val hour = (hours % 12).let {
                if (it == 0) 12 else it
            }.digits(2)
            val minutes = getMinutes().digits(2)
            return "$hour:$minutes$ampm"
        }
    };

    abstract fun Date.format(): String

}
