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

import kotlinx.css.BorderStyle
import kotlinx.css.Color
import kotlinx.css.CssBuilder
import kotlinx.css.Display
import kotlinx.css.FlexDirection
import kotlinx.css.FlexWrap
import kotlinx.css.backgroundColor
import kotlinx.css.borderColor
import kotlinx.css.borderRightWidth
import kotlinx.css.borderStyle
import kotlinx.css.display
import kotlinx.css.flexDirection
import kotlinx.css.flexGrow
import kotlinx.css.flexWrap
import kotlinx.css.padding
import kotlinx.css.px
import kotlinx.css.rem
import kotlinx.css.top
import kotlinx.css.width
import org.moonglass.ui.style.Media
import react.Props
import react.RBuilder
import react.RComponent
import react.State
import styled.css
import styled.styledDiv

@JsExport
class SideBar : RComponent<Props, State>() {

    companion object {

        private val menuStyle = object : MenuStyle {
            override fun CssBuilder.style() {
                flexGrow = 1.0
                display = Display.flex
                flexDirection = FlexDirection.column
                flexWrap = FlexWrap.wrap
                backgroundColor = Color.white
                padding = Media.padding(6)
            }
        }
    }

    override fun RBuilder.render() {
        // outer responsive div
        styledDiv {
            name = "sideBar"
            css {
                top = ResponsiveLayout.navBarEmHeight
                width = ResponsiveLayout.sideBarEmWidth
                borderRightWidth = 1.px
                borderStyle = BorderStyle.solid
                borderColor = Color.grey
            }
            child(Menu::class) {
                attrs {
                    style = menuStyle
                    groups = MainMenu.menu
                }
            }
        }
    }
}
