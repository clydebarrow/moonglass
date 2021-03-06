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
import kotlinx.css.color
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
import kotlinx.css.properties.boxShadow
import kotlinx.css.properties.ms
import kotlinx.css.properties.transform
import kotlinx.css.properties.transition
import kotlinx.css.properties.translate
import kotlinx.css.px
import kotlinx.css.rem
import kotlinx.css.rgba
import kotlinx.css.textAlign
import kotlinx.css.vh
import kotlinx.css.zIndex
import kotlinx.datetime.Clock
import org.moonglass.ui.Theme
import org.moonglass.ui.ZIndex
import org.moonglass.ui.name
import org.moonglass.ui.useColorSet
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
    var displayState: Toast.State
    var until: Long
}

class Toast : RComponent<Props, ToastState>() {

    enum class State {
        Hidden,
        Fading,
        Shown
    }

    companion object {

        private var instance: Toast? = null

        private val fadeoutDuration = 600

        fun toast(message: String, durationSecs: Int = 6) {
            console.log(message)
            instance?.setState {
                this.message = message
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
        until = 0
        displayState = State.Hidden
    }

    override fun componentDidMount() {
        instance = this@Toast
    }

    override fun componentWillUnmount() {
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
                useColorSet(Theme().notifications)
                borderWidth = 1.px
                borderColor = Theme().borderColor
                borderRadius = 10.px
                boxShadow(rgba(0, 0, 0, 0.1), 0.px, 8.px, 15.px)
                padding(10.px)
                justifyContent = JustifyContent.center
                maxWidth = 500.px
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
