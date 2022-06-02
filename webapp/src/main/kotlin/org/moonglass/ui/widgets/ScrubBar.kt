/*
 *
 *  * Copyright (c) 2021. Clyde Stubbs
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  * http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 *
 */

package org.moonglass.ui.widgets

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.css.Color
import kotlinx.css.Position
import kotlinx.css.TextAlign
import kotlinx.css.backgroundColor
import kotlinx.css.borderRadius
import kotlinx.css.height
import kotlinx.css.padding
import kotlinx.css.pct
import kotlinx.css.position
import kotlinx.css.rem
import kotlinx.css.textAlign
import kotlinx.css.width
import org.moonglass.ui.Theme
import org.moonglass.ui.formatTime
import org.moonglass.ui.name
import org.moonglass.ui.useColorSet
import org.moonglass.ui.widgets.recordings.DateTimeSelector
import org.w3c.dom.CENTER
import org.w3c.dom.CanvasRenderingContext2D
import org.w3c.dom.CanvasTextAlign
import org.w3c.dom.CanvasTextBaseline
import org.w3c.dom.HTMLCanvasElement
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.MIDDLE
import react.Props
import react.RBuilder
import react.RComponent
import react.State
import react.createRef
import styled.css
import styled.styledCanvas
import styled.styledDiv
import kotlin.js.Date

external interface ScrubBarProps : Props {
    var dateTimeSelector: DateTimeSelector.SelectorState
}

class ScrubBar(props: ScrubBarProps) : RComponent<ScrubBarProps, State>(props) {

    private lateinit var scope: CoroutineScope

    private var updatedTime = 0.0        // time in seconds


    // ratio of pixels movement to seconds
    private val timeScale: Double
        get() {
            val width = textRef.current?.clientWidth ?: 1000
            return props.dateTimeSelector.duration().toDouble() / width
        }

    private val textRef = createRef<HTMLDivElement>()
    private val canvasRef = createRef<HTMLCanvasElement>()
    private val barRef = createRef<HTMLCanvasElement>()

    override fun componentDidMount() {
        scope = MainScope()
        barRef.current?.let { div ->
            scope.launch {
                DragWatcher(div, scope).flow.collect {
                    when (it.type) {
                        DragWatcher.Type.None -> Unit
                        DragWatcher.Type.Start -> {
                            div.style.backgroundColor = Theme().header.selectedBackgroundColor.toString()
                        }
                        DragWatcher.Type.Move -> {
                            updatedTime -= it.deltaX * timeScale
                            textRef.current?.textContent = Date(updatedTime * 1000).formatTime
                            drawTimeTicks()
                        }
                        DragWatcher.Type.End -> {
                            props.dateTimeSelector.startDateTime = Date(updatedTime * 1000)
                        }
                    }
                }
            }
        }
    }

    override fun componentWillUnmount() {
        scope.cancel()
    }

    override fun componentDidUpdate(prevProps: ScrubBarProps, prevState: State, snapshot: Any) {
        updatedTime = props.dateTimeSelector.startDateTime.getTime() / 1000.0
        drawTimeTicks()
    }

    override fun RBuilder.render() {
        styledDiv {
            css {
                width = 100.pct
                padding(1.rem)
                backgroundColor = Theme().content.backgroundColor
            }

            styledDiv {
                name = "ScrubBar"
                ref = barRef
                css {
                    position = Position.relative
                    width = 100.pct
                    borderRadius = 0.5.rem
                    useColorSet(Theme().subHeader)
                }
                // current time label
                styledDiv {
                    ref = textRef
                    css {
                        width = 100.pct
                        height = 1.5.rem
                        textAlign = TextAlign.center
                        useColorSet(Theme().header)
                    }
                    +props.dateTimeSelector.startDateTime.formatTime
                }
                // area for time labels and signal bars
                styledCanvas {
                    ref = canvasRef
                    css {
                        width = 100.pct
                        height = 3.rem
                    }
                }
            }
        }
    }

    private fun drawTimeTicks() {
        canvasRef.current?.let { canvas ->
            (canvas.getContext("2d") as? CanvasRenderingContext2D)?.apply {
                val colorSet = Theme().scrubBar
                val duration = props.dateTimeSelector.duration
                val width = canvas.clientWidth.toDouble() / 2
                val height = canvas.clientHeight.toDouble() / 2
                canvas.width = (width * 2).toInt()
                canvas.height = (height * 2).toInt()
                console.log("width/height=$width/$height")
                val tickSpacing = getInterval(duration(), 8)
                val scale = width / duration()
                resetTransform()
                fillStyle = colorSet.backgroundColor
                fillRect(0.0, 0.0, width * 2, height * 2)
                val tickCount = duration() / tickSpacing + 1
                beginPath()
                lineWidth = 1.0
                strokeStyle = colorSet.textColor
                textBaseline = CanvasTextBaseline.MIDDLE
                textAlign = CanvasTextAlign.CENTER
                font = "${height / 2}px Roboto"
                fillStyle = colorSet.textColor
                val x = updatedTime - updatedTime % tickSpacing     // round to correct interval
                console.log("tickcount = $tickCount, duration = $duration, spacing=$tickSpacing, time=$updatedTime, x=$x")
                (-tickCount..tickCount).forEach {
                    val offset = x + it * tickSpacing
                    val label = Date(offset * 1000.0).formatTime
                    val pos = (offset - updatedTime) * scale + width
                    moveTo(pos, 0.0)
                    lineTo(pos, height)
                    fillText(label, pos, height / 2)
                }
                stroke()
                beginPath()
                strokeStyle = Color.black
                moveTo(width, 0.0)
                lineTo(width, height)
                stroke()
            }
        }
    }

    companion object {
        // table of acceptable intervals for tick marks, in minutes.

        private val tickIntervals = arrayOf(
            2,
            6,
            12,
            30,
            60,     // 1 hour
            90,
            120,
            240,
            360     // 6 hours
        )

        /**
         * Given a range of time in seconds, and a maximum number of tick marks, return a suitable increment (in seconds)
         */
        fun getInterval(rangeSecs: Int, maxTicks: Int): Int {
            val minutes = rangeSecs / 60
            return (tickIntervals.firstOrNull { minutes / it <= maxTicks } ?: tickIntervals.last()) * 60
        }
    }
}
