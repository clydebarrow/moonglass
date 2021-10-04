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
import kotlinx.css.CssBuilder
import kotlinx.css.backgroundColor
import kotlinx.css.color
import org.moonglass.ui.user.UserPreferences

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
                override val textColor: Color = header.textColor
                override val backgroundColor = header.backgroundColor.lighten(15)
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
            override val menu = object : ColorSet {
                override val textColor: Color = content.textColor
                override val backgroundColor = Color("#f0f0f0")
            }
            override val button: ColorSet = object : ColorSet {
                override val textColor: Color = content.textColor
                override val backgroundColor: Color = Color("#ffe0e0")      // used for cancel button
                override val selectedBackgroundColor = Color("#e0e0ff")     // used for active non-cancel buttons
                override val disabledBackgroundColor = Color("#f0f0f0")     // disabled buttons
            }
        },
        Dark {
            override val isDark: Boolean = true
            override val borderColor: Color = Color.darkGray
            override val selectedBorderColor: Color = Color("#000070")

            override val header = object : ColorSet {
                override val textColor: Color = Color.white
                override val backgroundColor = Color("#10105b")
            }
            override val subHeader = object : ColorSet {
                override val textColor: Color = Color.white
                override val backgroundColor = header.backgroundColor.darken(40)
                override val selectedBackgroundColor: Color = Color.darkViolet
            }
            override val notifications = object : ColorSet {
                override val textColor: Color = Color.black
                override val backgroundColor = Color.white
            }
            override val content = object : ColorSet {
                override val textColor: Color = Color("#e0e0e0")
                override val backgroundColor = Color("#101010")
            }
            override val menu = object : ColorSet {
                override val textColor: Color = content.textColor
                override val backgroundColor = Color("#202020")
            }
            override val button: ColorSet = object : ColorSet {
                override val textColor: Color = content.textColor
                override val backgroundColor: Color = Color("#502020")      // used for cancel button
                override val selectedBackgroundColor = Color("#205020")     // used for active non-cancel buttons
                override val disabledBackgroundColor = Color("#303030")     // disabled buttons
            }

            override val overlay: Color = Color("#80808030")
        };

        abstract val header: ColorSet
        abstract val subHeader: ColorSet
        abstract val content: ColorSet
        abstract val menu: ColorSet
        abstract val notifications: ColorSet
        abstract val borderColor: Color
        abstract val selectedBorderColor: Color
        abstract val button: ColorSet
        open val overlay = Color("#00000030")       // modal mask color
        open val spinner = Color.white
        open val isDark: Boolean = false
    }

    val current: Mode get() = UserPreferences.current.theme

    operator fun invoke() = current
}

fun CssBuilder.useColorSet(colorSet: Theme.ColorSet) {
    this.backgroundColor = colorSet.backgroundColor
    this.color = colorSet.textColor
}
