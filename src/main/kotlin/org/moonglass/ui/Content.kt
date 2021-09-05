package org.moonglass.ui

import kotlinx.css.Align
import kotlinx.css.BorderCollapse
import kotlinx.css.Color
import kotlinx.css.Display
import kotlinx.css.FlexDirection
import kotlinx.css.FlexWrap
import kotlinx.css.JustifyContent
import kotlinx.css.Overflow
import kotlinx.css.alignContent
import kotlinx.css.backgroundColor
import kotlinx.css.borderCollapse
import kotlinx.css.borderColor
import kotlinx.css.borderRightWidth
import kotlinx.css.display
import kotlinx.css.flexDirection
import kotlinx.css.flexGrow
import kotlinx.css.flexWrap
import kotlinx.css.height
import kotlinx.css.justifyContent
import kotlinx.css.overflow
import kotlinx.css.px
import react.Props
import react.RBuilder
import react.RComponent
import react.State
import styled.css
import styled.styledDiv

external interface ContentProps : Props {
    var contentShowing: MainMenu.MainMenuItem
}

class Content : RComponent<ContentProps, State>() {

    override fun componentDidMount() {
        instance = this
    }

    override fun componentWillUnmount() {
        instance = null
    }

    override fun RBuilder.render() {
        styledDiv {
            name = "content"
            css {
                flexGrow = 1.0
                display = Display.flex
                flexDirection = FlexDirection.row
                flexWrap = FlexWrap.wrap
                backgroundColor = Color.white
                borderCollapse = BorderCollapse.collapse
                borderRightWidth = 1.px
                borderColor = Color.lightGray
                //padding(0.5.rem)
                overflow = Overflow.auto
                justifyContent = JustifyContent.center
                alignContent = Align.start
                height = ResponsiveLayout.outerHeight
            }
            props.contentShowing.apply {
                console.log("Showing content $menuId")
                contentComponent?.also {
                    child(it) {}
                } ?: +title
            }
        }
    }

    companion object {
        var instance: Content? = null

    }
}

