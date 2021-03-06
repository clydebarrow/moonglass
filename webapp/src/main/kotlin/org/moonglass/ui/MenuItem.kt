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

import kotlinx.css.Align
import kotlinx.css.Color
import kotlinx.css.Display
import kotlinx.css.FontWeight
import kotlinx.css.JustifyContent
import kotlinx.css.LinearDimension
import kotlinx.css.TextTransform
import kotlinx.css.alignItems
import kotlinx.css.backgroundColor
import kotlinx.css.color
import kotlinx.css.display
import kotlinx.css.fontSize
import kotlinx.css.fontWeight
import kotlinx.css.justifyContent
import kotlinx.css.letterSpacing
import kotlinx.css.marginRight
import kotlinx.css.padding
import kotlinx.css.properties.ms
import kotlinx.css.properties.transition
import kotlinx.css.px
import kotlinx.css.rem
import kotlinx.css.textTransform
import kotlinx.css.width
import kotlinx.html.js.onClickFunction
import react.RBuilder
import react.RComponent
import react.State
import react.dom.attrs
import styled.css
import styled.styledButton
import styled.styledImg

/**
 * The state of a menu. The title and enabled state can change.
 */

external interface MenuItemProps : react.Props {
    var menuId: String
    var title: String
    var enabled: Boolean
    var selected: Boolean
    var image: String
    var action: (String) -> Unit
}

@JsExport
class MenuItem(props: MenuItemProps) : RComponent<MenuItemProps, State>(props) {
    override fun RBuilder.render() {
        styledButton {
            css {
                padding(0.5.rem)
                display = Display.flex
                alignItems = Align.center
                justifyContent = JustifyContent.start
                textTransform = TextTransform.capitalize
                fontWeight = if (props.selected) FontWeight.w700 else FontWeight.normal
                fontSize = 0.875.rem
                letterSpacing = 0.025.rem
                width = LinearDimension.fillAvailable
                color = Theme().content.textColor
                hover {
                    backgroundColor = Color.lightGray
                    color = Color.gray
                }
                transition("all", 300.ms)
            }
            attrs {
                onClickFunction = { props.action(props.menuId) }
            }
            if (props.image.isNotBlank())
                styledImg {
                    imageSrc(props.image, 16.px)
                    css {
                        marginRight = 0.25.rem
                        display = Display.inline
                    }
                }
            +props.title
        }
    }
}

fun MenuItemProps.copyFrom(template: MenuItemTemplate) {
    menuId = template.menuId
    title = template.title
    enabled = template.enabled
    image = template.image.let {
        if (it.startsWith("/"))
            it
        else
            "/images/$it"
    }
    action = template.action
    selected = template.menuId == App.selectedItemId
}

/**
 * A menu item
 *
 * @param menuId An internal name
 * @param title The title displayed to the user
 * @param enabled If the menu is enabled.
 * @param image The name of the image file to display
 * @param action The action to take when the menu is selected. Called with the menu key as an argument
 */
open class MenuItemTemplate(
    val menuId: String,
    val title: String,
    open val enabled: Boolean = true,
    val image: String,
    open val action: (String) -> Unit
)

open class MenuGroup<T : MenuItemTemplate>(val title: String, val items: List<T>)
