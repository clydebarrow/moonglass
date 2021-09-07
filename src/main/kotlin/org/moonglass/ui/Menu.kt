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

class ContextStyle(private val horz: Double, private val vert: Double) : MenuStyle {
    override fun CssBuilder.style() {
        padding(1.rem)
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
        backgroundColor = Color("#F0F0F0")
        boxShadow(rgba(0, 0, 0, 0.1), 0.px, 20.px, 25.px, (-5).px)
        boxShadow(rgba(0, 0, 0, 0.04), 0.px, 10.px, 10.px, -5.px)
        zIndex = ZIndex.Menu()
        width = 12.rem
        animation("all")
    }
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
                            color = Color.black
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
