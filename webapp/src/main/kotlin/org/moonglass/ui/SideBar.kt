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
import kotlinx.css.Overflow
import kotlinx.css.Position
import kotlinx.css.backgroundColor
import kotlinx.css.borderColor
import kotlinx.css.borderRightWidth
import kotlinx.css.borderStyle
import kotlinx.css.bottom
import kotlinx.css.display
import kotlinx.css.flexDirection
import kotlinx.css.flexGrow
import kotlinx.css.flexWrap
import kotlinx.css.left
import kotlinx.css.margin
import kotlinx.css.opacity
import kotlinx.css.overflow
import kotlinx.css.padding
import kotlinx.css.position
import kotlinx.css.properties.boxShadow
import kotlinx.css.properties.s
import kotlinx.css.properties.transition
import kotlinx.css.px
import kotlinx.css.rgba
import kotlinx.css.top
import kotlinx.css.width
import kotlinx.css.zIndex
import org.moonglass.ui.style.Media
import org.moonglass.ui.utility.StateVar
import react.Props
import react.RBuilder
import react.RComponent
import react.State
import styled.css
import styled.styledDiv

external interface SideBarProps : Props {
    var isSideBarShowing: StateVar<Boolean>
}

@JsExport
class SideBar : RComponent<SideBarProps, State>() {

    private val nowShowing get() = props.isSideBarShowing() || ResponsiveLayout.showSideMenu

    override fun RBuilder.render() {
        // outer responsive div
        dismisser(
            onDismiss = { props.isSideBarShowing.value = false },
            visible = !ResponsiveLayout.showSideMenu && props.isSideBarShowing(),
            z = ZIndex.SideBar()-1
        ) { }

        styledDiv {
            name = "sideBar"
            css {
                if (ResponsiveLayout.showSideMenu)
                    position = Position.relative
                else {
                    position = Position.absolute
                    boxShadow(rgba(0, 0, 0, 0.2), 2.px, 2.px, 2.px, 1.px)
                }
                top = 0.px
                bottom = 0.px
                left = 0.px
                margin(top = ResponsiveLayout.navBarEmHeight)
                if (nowShowing)
                    width = ResponsiveLayout.sideBarEmWidth
                else {
                    opacity = 0.0
                    width = 0.px
                }
                overflow = Overflow.hidden
                borderRightWidth = 1.px
                borderStyle = BorderStyle.solid
                borderColor = Theme().borderColor
                zIndex = ZIndex.SideBar()
                useColorSet(Theme().content)
                transition("all", 0.3.s)
            }
            child(Menu::class) {
                attrs {
                    style = menuStyle
                    groups = MainMenu.menu
                }
            }
        }
    }

    companion object {

        var instance: SideBar? = null


        private val menuStyle = object : MenuStyle {
            override val dismisserColor: Color = Theme().overlay
            override fun CssBuilder.style(isVisible: Boolean) {
                flexGrow = 1.0
                display = Display.flex
                flexDirection = FlexDirection.column
                flexWrap = FlexWrap.wrap
                useColorSet(Theme().content)
                padding = Media.padding(6)
            }
        }
    }
}
