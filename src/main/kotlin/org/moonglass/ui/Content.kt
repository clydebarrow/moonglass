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
import kotlinx.css.justifyContent
import kotlinx.css.overflow
import kotlinx.css.padding
import kotlinx.css.px
import kotlinx.css.rem
import org.moonglass.ui.content.Recordings
import org.moonglass.ui.widgets.Toast
import react.Props
import react.RBuilder
import react.RComponent
import react.State
import styled.css
import styled.styledDiv

external interface ContentProps: Props {
    var requested: MainMenu.MainMenuItem
}

class Content : RComponent<ContentProps, State>() {

    override fun shouldComponentUpdate(nextProps: ContentProps, nextState: State): Boolean {
        if(nextProps.requested.menuId != props.requested.menuId) {
            when(nextProps.requested.menuId) {
                "recordings" -> Unit
                else -> {
                    Toast.toast("${nextProps.requested.title} not implemented")
                }
            }
        }
        return true
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
                padding(0.5.rem)
                overflow = Overflow.auto
                justifyContent = JustifyContent.center
                alignContent = Align.start
            }
            props.requested.apply {
                console.log("Showing content $menuId")
                when (menuId) {
                    "recordings" -> child(Recordings::class) {}
                    else -> {
                        +title
                    }
                }
            }
        }
    }
}

