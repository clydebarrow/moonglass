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
import kotlinx.css.borderColor
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
import kotlinx.css.zIndex
import kotlinx.datetime.Clock
import org.moonglass.ui.ZIndex
import org.moonglass.ui.name
import org.moonglass.ui.utility.Timer
import react.Props
import react.RBuilder
import react.RComponent
import react.State
import react.dom.attrs
import react.dom.onTransitionEnd
import react.setState
import styled.css
import styled.styledDiv

external interface ToastState : State {
    var message: String     // message to show
    var urgency: Toast.Urgency
    var displayState: Toast.State
    var until: Long
}

class Toast : RComponent<Props, ToastState>() {

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

    companion object {

        private var instance: Toast? = null

        private val fadeoutDuration = 600

        fun toast(message: String, urgency: Urgency = Urgency.Normal, durationSecs: Int = 6) {
            console.log(message)
            instance?.setState {
                this.message = message
                this.urgency = urgency
                displayState = State.Shown
                this.until = Clock.System.now().toEpochMilliseconds() + durationSecs * 1000L
            }
        }
    }

    private val toastTimer = Timer()

    override fun ToastState.init(props: Props) {
        init()
    }

    override fun ToastState.init() {
        until = 0
        message = ""
        urgency = Urgency.Normal
        until = 0
        displayState = State.Hidden
    }

    override fun componentDidMount() {
        console.log("Toast: ComponentDidMount")
        instance = this@Toast
    }

    override fun componentWillUnmount() {
        console.log("Toast: ComponentWillUnmount")
        toastTimer.cancel()
        instance = null
    }

    override fun RBuilder.render() {
        styledDiv {
            name = "Toast"
            attrs {
                key = "ToastElement"
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
                backgroundColor = Color.gray
                borderWidth = 1.px
                borderColor = Color.darkBlue
                borderRadius = 10.px
                padding(10.px)
                justifyContent = JustifyContent.center
                maxWidth = 400.px
                fontSize = 1.2.rem
                transition("all", fadeoutDuration.ms)
                opacity = when (state.displayState) {
                    State.Shown -> 1.0
                    else -> 0.0
                }
                display = if (state.displayState == State.Hidden) Display.none else Display.block
            }
            if (state.displayState != State.Hidden) {
                val delay = (state.until - Clock.System.now().toEpochMilliseconds()).toInt()
                if (delay > fadeoutDuration) {
                    toastTimer.start(delay - fadeoutDuration) {
                        setState { displayState = State.Fading }
                    }
                } else
                    toastTimer.start(delay) {
                        setState { displayState = State.Hidden }
                    }
            } else
                toastTimer.cancel()
            +state.message
        }
    }
}
