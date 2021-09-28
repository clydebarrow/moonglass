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
 * Date formats. Kotlin/JS currently has no good support for date formatting,
 * and Javascript's date formatting is crap.
 *
 * @param description Human-readable description (should be short)
 */
enum class DateFormat(val description: String) {

    YYYYMMDD("yyyy-mm-dd") {
        override fun Date.format(): String {
            val day = getDate().digits(2)
            val month = getMonth().digits(2)
            val year = getFullYear().toString()
            return listOf(year, month, day).joinToString("-")
        }
    },
    MMDDYYYY("mm/dd/yyyy") {
        override fun Date.format(): String {
            val day = getDate().digits(2)
            val month = getMonth().digits(2)
            val year = getFullYear().toString()
            return listOf(month, day, year).joinToString("/")
        }
    },
    DDMMYYYY("dd/mm/yyyy") {
        override fun Date.format(): String {
            val day = getDate().digits(2)
            val month = getMonth().digits(2)
            val year = getFullYear().toString()
            return listOf(day, month, year).joinToString("/")
        }
    },
    MMMDDYYYY("MMM dd yyyy") {
        override fun Date.format(): String {
            val day = getDate().digits(2)
            val month = toLocaleString("default", dateLocaleOptions { month = "short" })
            val year = getFullYear().toString()
            return listOf(month, day, year).joinToString(" ")
        }
    },
    DDMMMYYYY("dd MMM yyyy") {
        override fun Date.format(): String {
            val day = getDate().digits(2)
            val month = toLocaleString("default", dateLocaleOptions { month = "short" })
            val year = getFullYear().toString()
            return listOf(day, month, year).joinToString(" ")
        }
    };


    /**
     * function to perform the formatting.
     */
    abstract fun Date.format(): String

    override fun toString(): String = description
}
