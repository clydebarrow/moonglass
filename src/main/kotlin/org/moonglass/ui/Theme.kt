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

package org.moonglass.ui

import kotlinx.css.Color

/*
Site them variables
 */
object Theme {

    const val icon = "/images/logo.png"
    const val title = "Moonfire NVR"

    interface ColorSet {
        val textColor: Color
        val backgroundColor: Color
        val selectedBackgroundColor: Color
            get() = backgroundColor
        val disabledBackgroundColor: Color
            get() = backgroundColor

    }

    enum class Mode {

        Light {
            override val borderColor: Color = Color.lightGray
            override val selectedBorderColor: Color = Color.royalBlue
            override val header = object : ColorSet {
                override val textColor: Color = Color.black
                override val backgroundColor = Color.lightBlue
            }
            override val subHeader = object : ColorSet {
                override val textColor: Color = Color.black
                override val backgroundColor = header.backgroundColor.lighten(20)
                override val selectedBackgroundColor: Color = Color.lightSalmon
            }
            override val notifications = object : ColorSet {
                override val textColor: Color = Color.white
                override val backgroundColor = Color.black
            }
            override val content = object : ColorSet {
                override val textColor: Color = Color.black
                override val backgroundColor = Color.white
            }
            override val button: ColorSet = object : ColorSet {
                override val textColor: Color = content.textColor
                override val backgroundColor: Color = Color("#ffe0e0")      // used for cancel button
                override val selectedBackgroundColor = Color("#e0e0ff")     // used for active non-cancel buttons
                override val disabledBackgroundColor = Color("#f0f0f0")     // disabled buttons
            }
        };

        abstract val header: ColorSet
        abstract val subHeader: ColorSet
        abstract val content: ColorSet
        abstract val notifications: ColorSet
        abstract val borderColor: Color
        abstract val selectedBorderColor: Color
        abstract val button: ColorSet
        open val overlay = Color("#00000030")       // modal mask color
    }

    var current: Mode = Mode.values().first()

    operator fun invoke() = current
}
