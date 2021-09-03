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
        private val menuWidth = 12.rem

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
                top = NavBar.barHeight
                width = menuWidth
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
