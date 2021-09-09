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

import kotlinx.browser.document
import kotlinx.css.Align
import kotlinx.css.Color
import kotlinx.css.CssBuilder
import kotlinx.css.Cursor
import kotlinx.css.Display
import kotlinx.css.FlexDirection
import kotlinx.css.JustifyContent
import kotlinx.css.Position
import kotlinx.css.RuleSet
import kotlinx.css.TextAlign
import kotlinx.css.alignItems
import kotlinx.css.backgroundColor
import kotlinx.css.borderColor
import kotlinx.css.borderRadius
import kotlinx.css.borderWidth
import kotlinx.css.color
import kotlinx.css.cursor
import kotlinx.css.display
import kotlinx.css.flexDirection
import kotlinx.css.justifyContent
import kotlinx.css.left
import kotlinx.css.margin
import kotlinx.css.padding
import kotlinx.css.pct
import kotlinx.css.position
import kotlinx.css.properties.deg
import kotlinx.css.properties.rotate
import kotlinx.css.properties.transform
import kotlinx.css.properties.translate
import kotlinx.css.px
import kotlinx.css.rem
import kotlinx.css.textAlign
import kotlinx.css.top
import kotlinx.css.width
import kotlinx.css.zIndex
import kotlinx.html.DIV
import kotlinx.html.js.onClickFunction
import kotlinx.html.js.onFocusFunction
import kotlinx.html.tabIndex
import org.moonglass.ui.ZIndex
import org.moonglass.ui.applyState
import org.moonglass.ui.dismisser
import org.w3c.dom.CENTER
import org.w3c.dom.CanvasLineCap
import org.w3c.dom.CanvasRenderingContext2D
import org.w3c.dom.CanvasTextAlign
import org.w3c.dom.CanvasTextBaseline
import org.w3c.dom.HTMLCanvasElement
import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLSpanElement
import org.w3c.dom.MIDDLE
import org.w3c.dom.ROUND
import org.w3c.dom.events.Event
import react.Props
import react.RBuilder
import react.RComponent
import react.State
import react.createRef
import react.dom.MouseEvent
import react.dom.attrs
import react.dom.onKeyDown
import react.fc
import react.useEffect
import styled.StyledDOMBuilder
import styled.css
import styled.styledCanvas
import styled.styledDiv
import styled.styledImg
import styled.styledSpan
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

external interface StyleableProps : Props {
    var cssBlock: (CssBuilder) -> Unit
}

/**
 * Specify css to be passed to the component
 */
fun StyleableProps.css(handler: RuleSet) {
    cssBlock = handler
}

external interface ClockProps : Props {
    var range: IntRange
    var value: Int
    var onSelect: (Int) -> Unit

}

external interface TimePickerProps : StyleableProps {
    var initialTime: Int    // time of day in seconds
    var use24HourTime: Boolean
    var onChange: (Int) -> Unit // returns time in seconds
}

external interface TimePickerState : State {
    var cursorPos: Int
    var hours: Int
    var minutes: Int
    var focused: Boolean
}


/**
 * Convenience function to insert a timepicker
 */
fun RBuilder.timePicker(handler: TimePickerProps.() -> Unit) {
    child(TimePicker::class) {
        attrs(handler)
    }
}

/**
 * A time picker. Allow selection of a time in hours and minutes
 */
class TimePicker(props: TimePickerProps) : RComponent<TimePickerProps, TimePickerState>(props) {
    private val digitRefs = Array(4) { createRef<HTMLSpanElement>() }
    private val canvasRef = createRef<HTMLCanvasElement>()

    val TimePickerState.isPm: Boolean get() = hours >= 12
    val TimePickerState.onHours: Boolean get() = cursorPos < 2
    val TimePickerState.hourRange get() = if (props.use24HourTime) (0..23) else (0..12)
    val TimePickerState.hourSpan get() = hourRange.let { it.last - it.first + 1 }


    override fun TimePickerState.init(props: TimePickerProps) {
        cursorPos = 0
        hours = props.initialTime / 3600
        minutes = (props.initialTime / 60) % 60
    }

    private fun onFocus(index: Int, newState: Boolean) {
        if (index >= 0)
            applyState {
                cursorPos = index
                focused = newState
            }
    }

    private fun update() {
        applyState {
            focused = false
            cursorPos = 0
            props.onChange((state.hours * 60 + state.minutes) * 60)
        }
    }

    /**
     * Handle a keypress that is a digit.
     */
    private fun digit(c: Char) {
        val value = c.code - '0'.code
        when (state.cursorPos) {
            0 -> setHours(state.hours % 10 + value * 10)
            1 -> setHours(state.hours / 10 * 10 + value)
            2 -> setMinutes(state.minutes % 10 + value * 10)
            3 -> setMinutes(state.minutes / 10 * 10 + value)
        }
        applyState({
            // callback after update
            if (state.cursorPos == 0)
                update()
        }) {
            cursorPos = (cursorPos + 1) % 4
            if (cursorPos != 0)
                focused = true
        }
    }

    /**
     * Handle plus and minus
     */

    private fun plusMinus(c: Char) {
        val increment = when(c) {
            '+' -> 1
            '-' -> -1
            else -> return
        }
        when (state.cursorPos) {
            0,1 -> setHours(state.hours + increment)
            2,3 -> setMinutes(state.minutes + increment)
        }
    }

    /*
    move the cursor left or right.
     */
    private fun moveCursor(distance: Int) {
        applyState { cursorPos += distance }
    }

    private fun keyDown(event: react.dom.KeyboardEvent<*>) {
        event.apply {
            preventDefault()
            stopPropagation()
            when (key) {
                "Escape", "Enter" -> {
                    document.activeElement?.unsafeCast<HTMLElement>()?.blur()
                    update()
                }
                "0", "1", "2", "3", "4", "5", "6", "7", "8", "9" -> digit(key.first())
                "ArrowLeft" -> moveCursor(-1)
                "ArrowRight" -> moveCursor(1)
            }
        }
    }

    private fun setHours(value: Int) {
        applyState {
            val range = hourRange
            hours = value.let {
                when {
                    it < range.first -> it + hourSpan
                    it > range.last -> it - hourSpan
                    else -> it
                }
            }
        }
    }

    private fun addMinute(up: Boolean) {
        if (up)
            setMinutes(state.minutes + 1)
        else
            setMinutes(state.minutes - 1)
        applyState {
            cursorPos = 3
            focused = true
        }
    }

    private fun addHour(up: Boolean) {
        if (up)
            setHours(state.hours + 1)
        else
            setHours(state.hours - 1)
        applyState {
            cursorPos = 1
            focused = true
        }

    }

    private fun setMinutes(value: Int) {
        applyState {
            minutes = value.coerceIn(0..59)
        }
    }

    override fun componentDidUpdate(prevProps: TimePickerProps, prevState: TimePickerState, snapshot: Any) {
        if (state.focused)
            digitRefs[state.cursorPos].current?.focus()
    }

    private fun StyledDOMBuilder<DIV>.oneChar(value: String, index: Int = -1) {
        styledSpan {
            if (index >= 0)
                ref = digitRefs[index]
            attrs {
                if (index == 0)
                    set("autoFocus", "")
                onKeyDown = ::keyDown
                if (index != -1)
                    tabIndex = "0"
                onFocusFunction = { onFocus(index, true) }
            }
            css {
                display = Display.flex
                textAlign = TextAlign.center
                width = 1.rem
                padding(.1.rem)
                //fontSize = 1.5.rem
                backgroundColor = Color.transparent
                focus {
                    backgroundColor = Color.lightBlue
                }
            }
            +value
        }
    }


    fun StyledDOMBuilder<DIV>.arrow(up: Boolean, onClick: (Boolean) -> Unit) {
        styledImg(src = "/images/triangle.svg") {
            attrs {
                onClickFunction = { onClick(up) }
            }

            css {
                margin(0.px)
                padding(0.px)
                display = Display.flex
                cursor = Cursor.pointer
                color = Color.gray
                active {
                    color = Color.royalBlue
                }
                if (!up)
                    transform {
                        rotate(180.deg)
                    }
            }
        }
    }

    private fun StyledDOMBuilder<DIV>.arrows(onClick: (Boolean) -> Unit) {
        styledDiv {
            css {
                display = Display.flex
                flexDirection = FlexDirection.column
            }

            arrow(true, onClick)
            arrow(false, onClick)
        }
    }


    private data class Xy(val x: Double, val y: Double)

    private fun xy(angle: Double, length: Double): Xy = Xy(sin(angle) * length, -cos(angle) * length)

    /**
     * Calculate the direction of a click from the center of the canvas. Range will be 0-1.0 with straight
     * up represented by 0.
     */
    private fun Event.getRadial(): Double {
        unsafeCast<MouseEvent<HTMLCanvasElement, org.w3c.dom.events.MouseEvent>>().let { mouseEvent ->
            val rect = mouseEvent.currentTarget.getBoundingClientRect()
            val x = mouseEvent.clientX - rect.x - rect.width / 2    // centre-relative posx
            val y = (mouseEvent.clientY - rect.y - rect.width / 2)
            val angle = (atan2(-x, y) + PI) / PI / 2
            return angle
        }
    }

    private val clockFace = fc<ClockProps>("Clockface") { clockProps ->
        val range = (clockProps.range.last - clockProps.range.first + 1).toDouble()
        styledCanvas {
            ref = canvasRef
            attrs {
                width = clockRadius.toString()
                height = clockRadius.toString()
                onClickFunction = { clockProps.onSelect((it.getRadial() * range).roundToInt()) }
            }
            css {
                backgroundColor = Color.lightCyan
                borderColor = Color.black
                borderWidth = 1.px
                borderRadius = clockRadius.px
                padding(3.px)
                position = Position.absolute
                top = 4.rem
                left = 50.pct       // center horizontally
                transform { translate((-50).pct, 0.pct) }
                cursor = Cursor.pointer
            }
        }

        useEffect {
            canvasRef.current?.let { canvas ->
                canvas.getContext("2d")?.unsafeCast<CanvasRenderingContext2D>()?.run {
                    resetTransform()
                    val width = canvas.width.toDouble()
                    val height = canvas.height.toDouble()
                    clearRect(0.0, 0.0, width, height)
                    translate(width / 2, height / 2)
                    textAlign = CanvasTextAlign.CENTER
                    textBaseline = CanvasTextBaseline.MIDDLE
                    fillStyle = Color.black
                    val textSize = height / 16.0
                    font = "normal ${textSize}px sans-serif"

                    clockProps.range.let { if (range > 24) it.step(5) else it }.forEach {
                        val xy = xy(it / range * 2 * PI, height / 2 - textSize)
                        fillText(it.toString(), xy.x, xy.y)
                    }

                    lineWidth = 4.0
                    strokeStyle = Color.black
                    hand(clockProps.value.toDouble() / range, height / 2 - textSize * 2)
                }
            }
        }
    }

    private fun CanvasRenderingContext2D.hand(frac: Double, len: Double) {
        val angle = 2 * PI * frac
        val x = sin(angle) * len
        val y = -cos(angle) * len
        beginPath()
        lineCap = CanvasLineCap.ROUND
        moveTo(0.0, 0.0)
        lineTo(x, y)
        stroke()
    }

    override fun RBuilder.render() {
        fun compose(z: Int?  = null) = styledDiv {
            css {
                display = Display.flex
                position = Position.relative
                flexDirection = FlexDirection.column
                justifyContent = JustifyContent.center
                z?.let { zIndex = it }
            }
            styledDiv {
                css {
                    padding(1.rem)
                    display = Display.flex
                    flexDirection = FlexDirection.row
                    alignItems = Align.center
                }
                oneChar((state.hours / 10).toString(), 0)
                oneChar((state.hours % 10).toString(), 1)
                arrows(::addHour)
                oneChar(":")
                oneChar((state.minutes / 10).toString(), 2)
                oneChar((state.minutes % 10).toString(), 3)
                arrows(::addMinute)
            }
            if (state.focused)
                clockFace {
                    attrs {
                        if (state.onHours) {
                            range = state.hourRange
                            value = state.hours
                            onSelect = {
                                setHours(it)
                                applyState { cursorPos = 2 }
                            }
                        } else {
                            range = (0..59)
                            value = state.minutes
                            onSelect = {
                                setMinutes(it)
                                update()
                            }
                        }
                    }
                }
        }
        // when focused, insert a dismisser and a clockface
        if (state.focused) {
            dismisser({
                applyState({
                    update()        // callback
                }) {
                    focused = false
                }
            }) {
                compose(ZIndex.Input())
            }
        } else
            compose()
    }

    companion object {
        const val clockRadius = 200.0
    }
}

