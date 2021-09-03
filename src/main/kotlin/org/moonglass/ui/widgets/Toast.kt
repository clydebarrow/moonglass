package org.moonglass.ui.widgets

import kotlinx.css.Align
import kotlinx.css.Color
import kotlinx.css.Display
import kotlinx.css.JustifyContent
import kotlinx.css.LinearDimension
import kotlinx.css.Position
import kotlinx.css.TextAlign
import kotlinx.css.alignContent
import kotlinx.css.backgroundColor
import kotlinx.css.borderRadius
import kotlinx.css.borderWidth
import kotlinx.css.bottom
import kotlinx.css.display
import kotlinx.css.fontSize
import kotlinx.css.height
import kotlinx.css.justifyContent
import kotlinx.css.left
import kotlinx.css.maxWidth
import kotlinx.css.opacity
import kotlinx.css.padding
import kotlinx.css.pct
import kotlinx.css.position
import kotlinx.css.properties.ms
import kotlinx.css.properties.transform
import kotlinx.css.properties.transition
import kotlinx.css.properties.translate
import kotlinx.css.px
import kotlinx.css.rem
import kotlinx.css.textAlign
import kotlinx.css.vh
import kotlinx.css.width
import kotlinx.css.zIndex
import kotlinx.datetime.Clock
import org.moonglass.ui.App
import org.moonglass.ui.ZIndex
import org.moonglass.ui.name
import react.Props
import react.RBuilder
import react.RComponent
import react.State
import react.dom.attrs
import react.dom.onTransitionEnd
import react.setState
import styled.css
import styled.styledDiv
import kotlin.js.Date

external interface ToastProps : Props {
    var message: String     // message to show
    var urgency: Toast.Urgency
    var displayState: Toast.State
}

class Toast : RComponent<ToastProps, State>() {

    companion object {
        val fadeoutDuration = 600

        fun toast(message: String, urgency: Urgency = Urgency.Normal, durationSecs: Int = 6) {
            App.instance?.setState {
                toastMessage = message
                toastUrgency = urgency
                toastState = State.Shown
                toastUntil = Clock.System.now().toEpochMilliseconds() + durationSecs * 1000L
            }
        }
    }

    enum class Urgency(val color: Color) {
        Normal(Color.darkBlue),
        Alert(Color("#A0A000")),        // amber
        Alarm(Color.red)
    }

    enum class State {
        Hidden,
        Fading,
        Shown
    }

    override fun RBuilder.render() {
        styledDiv {
            name = "Toast"
            attrs {
                onTransitionEnd = { forceUpdate() }
            }
            css {
                textAlign = TextAlign.center
                alignContent = Align.center
                zIndex = ZIndex.Toast()
                position = Position.fixed
                height = LinearDimension.auto
                left = 50.pct
                transform { translate(-50.pct, 0.pct) }
                bottom = 20.vh
                backgroundColor = Color.lightGray
                borderWidth = 1.px
                borderRadius = 10.px
                padding(10.px)
                justifyContent = JustifyContent.center
                maxWidth = 400.px
                fontSize = 1.2.rem
                transition("all", fadeoutDuration.ms)
                opacity = when(props.displayState) {
                    State.Shown -> 1.0
                    else -> 0.0
                }
                display = if(props.displayState == State.Hidden) Display.none else Display.block
            }
            +props.message
        }
    }
}
