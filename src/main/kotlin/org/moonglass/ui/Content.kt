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
import react.setState
import styled.css
import styled.styledDiv

external interface ContentState : State {
    var requested: MainMenu.MainMenuItem
}

class Content : RComponent<Props, ContentState>() {

    override fun componentDidMount() {
        instance = this
    }

    override fun componentWillUnmount() {
        instance = null
    }

    override fun ContentState.init() {
        requested = MainMenu.menu.first().items.first()

    }
    override fun ContentState.init(props: Props) {
        init()
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
            }
            state.requested.apply {
                console.log("Showing content $menuId")
                state.requested.clazz?.also {
                    child(it) {}
                } ?: +title
            }
        }
    }

    companion object {
        var instance: Content? = null

        val selectedItemId: String? get() = instance?.state?.requested?.menuId

        fun requestContent(item: MainMenu.MainMenuItem) {
            instance?.apply {
                if (item.clazz == null)
                    Toast.toast("No content implemented for ${item.title}")
                setState {
                    requested = item
                }
            }
        }
    }
}

