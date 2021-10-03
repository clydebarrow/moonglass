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
import kotlinx.css.FontWeight
import kotlinx.css.LinearDimension
import kotlinx.css.Overflow
import kotlinx.css.Position
import kotlinx.css.TextTransform
import kotlinx.css.backgroundColor
import kotlinx.css.borderRadius
import kotlinx.css.bottom
import kotlinx.css.color
import kotlinx.css.em
import kotlinx.css.fontSize
import kotlinx.css.fontWeight
import kotlinx.css.height
import kotlinx.css.left
import kotlinx.css.letterSpacing
import kotlinx.css.maxHeight
import kotlinx.css.overflow
import kotlinx.css.padding
import kotlinx.css.position
import kotlinx.css.properties.boxShadow
import kotlinx.css.properties.transition
import kotlinx.css.px
import kotlinx.css.rem
import kotlinx.css.rgba
import kotlinx.css.right
import kotlinx.css.textTransform
import kotlinx.css.top
import kotlinx.css.vh
import kotlinx.css.width
import kotlinx.css.zIndex
import org.moonglass.ui.utility.StateVar
import react.RBuilder
import react.RComponent
import react.State
import styled.css
import styled.styledDiv

external interface MenuProps : react.Props {
    var groups: List<MenuGroup<out MenuItemTemplate>>
    var style: MenuStyle
    var isShowing: StateVar<Boolean>?
}

interface MenuStyle {
    fun CssBuilder.style(isVisible: Boolean)
    val dismisserColor: Color
        get() = Color.transparent
}

@JsExport
class Menu(props: MenuProps) : RComponent<MenuProps, State>(props) {
    override fun RBuilder.render() {
        props.isShowing.let { dismiss ->
            if (dismiss == null)
                addMenu()
            else {
                dismisser(
                    background = props.style.dismisserColor,
                    onDismiss = { dismiss.value = false },
                    visible = dismiss.value
                ) { addMenu() }
            }
        }
    }

    private fun RBuilder.addMenu() {
        styledDiv {
            name = "menuOuter"
            css {
                props.style.apply {
                    style(props.isShowing?.value != false)
                }
            }
            props.groups.forEach { group ->
                if (group.title.isNotBlank()) {
                    styledDiv {
                        css {
                            textTransform = TextTransform.capitalize
                            color = Theme().content.textColor
                            fontSize = 1.1.rem
                            fontWeight = FontWeight.w200
                            padding(top = 0.5.rem)
                            letterSpacing = 0.05.em
                        }
                        +group.title
                    }
                }
                group.items.forEach {
                    child(MenuItem::class) {
                        attrs { copyFrom(it) }
                    }
                }
            }
        }
    }
}

/**
 * Style for a context menu
 */
class ContextStyle(private val horz: Double, private val vert: Double) : MenuStyle {
    override fun CssBuilder.style(isVisible: Boolean) {
        position = Position.absolute
        if (vert >= 0)
            top = vert.rem
        else
            bottom = -vert.rem
        if (horz >= 0)
            left = horz.rem
        else
            right = -horz.rem
        borderRadius = 0.25.rem
        useColorSet(Theme().menu)
        boxShadow(rgba(0, 0, 0, 0.1), 0.px, 20.px, 25.px, (-5).px)
        boxShadow(rgba(0, 0, 0, 0.04), 0.px, 10.px, 10.px, -5.px)
        transition("all", ResponsiveLayout.menuTransitionTime)
        height = LinearDimension.auto
        zIndex = ZIndex.Menu()
        // hide by changing width to zero. This is animatable.
        overflow = Overflow.hidden
        width = if(isVisible) 12.rem else 0.px
    }
}

/**
 * Data for a context menu
 */

class ContextMenuData(
    val groups: List<MenuGroup<out MenuItemTemplate>> = listOf(),
    val style: ContextStyle = ContextStyle(0.0, 0.0)
)
