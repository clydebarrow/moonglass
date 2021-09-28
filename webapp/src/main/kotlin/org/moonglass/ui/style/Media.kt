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

package org.moonglass.ui.style

object Media {
    fun padding(weight: Int): String {
        return "${weight * 0.25}rem"
    }


    fun mediaWidth(maxWidth: Int = -1, minWidth: Int = -1): String {
        val max = if (maxWidth >= 0) "(max-width: ${maxWidth}px)" else null
        val min = if (minWidth >= 0) "(min-width: ${minWidth}px)" else null
        return listOf("only screen", max, min).filterNotNull().joinToString(" and ") { it }
    }

    val small = mediaWidth(maxWidth = 639)
    val medium = mediaWidth(maxWidth = 767)
    val large = mediaWidth(maxWidth = 1023)
    val extraLarge = mediaWidth(maxWidth = 1279)
}

