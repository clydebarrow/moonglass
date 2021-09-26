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
import kotlinx.css.Position
import kotlinx.css.TextTransform
import kotlinx.css.backgroundColor
import kotlinx.css.borderRadius
import kotlinx.css.bottom
import kotlinx.css.color
import kotlinx.css.em
import kotlinx.css.fontSize
import kotlinx.css.fontWeight
import kotlinx.css.left
import kotlinx.css.letterSpacing
import kotlinx.css.padding
import kotlinx.css.position
import kotlinx.css.properties.animation
import kotlinx.css.properties.boxShadow
import kotlinx.css.px
import kotlinx.css.rem
import kotlinx.css.rgba
import kotlinx.css.right
import kotlinx.css.textTransform
import kotlinx.css.top
import kotlinx.css.width
import kotlinx.css.zIndex
import react.RBuilder
import react.RComponent
import react.State
import styled.css
import styled.styledDiv

external interface MenuProps : react.Props {
    var groups: List<MenuGroup<out MenuItemTemplate>>
    var style: MenuStyle
    var dismiss: ((Menu) -> Unit)?
}

interface MenuStyle {
    fun CssBuilder.style()
}

@JsExport
class Menu(props: MenuProps) : RComponent<MenuProps, State>(props) {
    override fun RBuilder.render() {
        props.dismiss.let { dismiss ->
            if (dismiss == null)
                addMenu()
            else {
                dismisser({ dismiss(this@Menu) }) { addMenu() }
            }
        }
    }

    private fun RBuilder.addMenu() {
        styledDiv {
            name = "menuOuter"
            css {
                props.style.apply {
                    style()
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
