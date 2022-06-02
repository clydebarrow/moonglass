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

package org.moonglass.ui.widgets.recordings

import calendar
import kotlinx.css.Align
import kotlinx.css.Cursor
import kotlinx.css.Display
import kotlinx.css.FlexDirection
import kotlinx.css.GridTemplateColumns
import kotlinx.css.JustifyContent
import kotlinx.css.Position
import kotlinx.css.alignContent
import kotlinx.css.alignItems
import kotlinx.css.backgroundColor
import kotlinx.css.color
import kotlinx.css.cursor
import kotlinx.css.display
import kotlinx.css.flexDirection
import kotlinx.css.gridTemplateColumns
import kotlinx.css.justifyContent
import kotlinx.css.padding
import kotlinx.css.pct
import kotlinx.css.position
import kotlinx.css.rem
import kotlinx.css.top
import kotlinx.css.width
import kotlinx.css.zIndex
import kotlinx.html.id
import kotlinx.html.js.onChangeFunction
import kotlinx.html.js.onClickFunction
import kotlinx.serialization.Serializable
import org.moonglass.ui.Theme
import org.moonglass.ui.ZIndex
import org.moonglass.ui.cardStyle
import org.moonglass.ui.dismisser
import org.moonglass.ui.formatDate
import org.moonglass.ui.formatHhMm
import org.moonglass.ui.name
import org.moonglass.ui.plusSeconds
import org.moonglass.ui.style.checkBox
import org.moonglass.ui.style.expandButton
import org.moonglass.ui.style.shrinkable
import org.moonglass.ui.useColorSet
import org.moonglass.ui.utility.StateVar
import org.moonglass.ui.widgets.timePicker
import org.moonglass.ui.withTime
import org.w3c.dom.HTMLSelectElement
import react.Component
import react.Props
import react.RBuilder
import react.RComponent
import react.State
import react.dom.KeyboardEvent
import react.dom.ReactHTML.label
import react.dom.attrs
import react.dom.onKeyDown
import styled.css
import styled.styledDiv
import styled.styledOption
import styled.styledSelect
import kotlin.js.Date

external interface DateTimeSelectorProps : Props {
    var showDuration: Boolean                   // if max duration should be shown
    var data: DateTimeSelector.SelectorState
}

class DateTimeSelector : RComponent<DateTimeSelectorProps, State>() {

    private fun keyDown(event: KeyboardEvent<*>) {
        event.apply {
            preventDefault()
            stopPropagation()
            when (key) {
                "Escape" -> props.data.expanded.value = false
            }
        }
    }

    override fun RBuilder.render() {
        styledDiv {
            name = "selectedDateTime"
            attrs {
                onClickFunction = {
                    props.data.expanded.value = !props.data.expanded.value
                }
                onKeyDown = ::keyDown
            }
            css {
                color = Theme().content.textColor
                display = Display.flex
                justifyContent = JustifyContent.center
                alignContent = Align.center
                flexDirection = FlexDirection.row
                position = Position.relative
                cursor = Cursor.pointer
                if (props.data.expanded())
                    zIndex = ZIndex.Input()
            }
            expandButton(props.data.expanded())
            listOf(
                props.data.startDate().formatDate,
                "${props.data.startTime().formatHhMm} - ${props.data.endTime.formatHhMm}"
            ).forEach {
                styledDiv {
                    css {
                        display = Display.flex
                        alignContent = Align.center
                        padding(left = 1.rem, right = 1.rem)
                    }
                }
                +it
            }
        }
        dismisser({ props.data.expanded.value = false }, visible = props.data.expanded()) {
            css {
                backgroundColor = Theme().overlay
            }
        }
        styledDiv {
            name = "DateTimeSelectorExpandable"
            shrinkable(props.data.expanded(), 50.rem)
            cardStyle()
            css {
                useColorSet(Theme().content)
                position = Position.absolute
                top = 3.rem
                display = Display.flex
                flexDirection = FlexDirection.column
                justifyContent = JustifyContent.center
                zIndex = ZIndex.Input()
            }
            // wrap the calendar so we can center it.
            styledDiv {
                css {
                    useColorSet(Theme().content)
                    display = Display.flex
                    justifyContent = JustifyContent.center
                    width = 100.pct
                }
                calendar {
                    attrs {
                        defaultView = "month"
                        defaultActiveStartDate = props.data.startDate()
                        onChange = { props.data.startDate.value = it }
                    }
                }
            }
            styledDiv {
                css {
                    display = Display.grid
                    justifyContent = JustifyContent.center
                    gridTemplateColumns = GridTemplateColumns("max-content max-content")
                    alignItems = Align.center
                    padding(0.25.rem)
                }
                label {
                    attrs {

                    }
                    +"Start time:"
                }
                timePicker {
                    initialTime = props.data.startTime()
                    use24HourTime = true
                    onChange = { props.data.startTime.value = it }
                }
                label {
                    +"End time:"
                }
                timePicker {
                    initialTime = props.data.endTime
                    use24HourTime = true
                    onChange = {
                        props.data.duration.value = (it - props.data.startTime()).let {
                            if (it < 0)
                                it + 24 * 60 * 60
                            else it
                        }
                    }
                }
                checkBox("Add timestamp subtitles", props.data.subTitle)
                if (props.showDuration) {
                    checkBox("Trim to start/end", props.data.trimEnds)
                    props.data.maxDuration.let { maxDur ->

                        label {
                            attrs {
                                htmlFor = "maxDurationSelect"
                            }
                            +"Max duration:"
                        }
                        styledSelect {
                            css {
                                useColorSet(Theme().content)
                            }
                            attrs {
                                id = "maxDurationSelect"
                                value = maxDur.toString()
                                onChangeFunction =
                                    {
                                        val max = it.currentTarget.unsafeCast<HTMLSelectElement>().value.toInt()
                                        maxDur.value = max
                                    }
                            }
                            listOf(1, 4, 8, 24).forEach {
                                styledOption {
                                    attrs {
                                        value = "$it"
                                    }
                                    +"$it hour${if (it == 1) "" else "s"}"
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    interface SelectorState {
        var startDate: StateVar<Date>
        var startTime: StateVar<Int>      // time of day in seconds
        var duration: StateVar<Int>        // also in seconds
        var maxDuration: StateVar<Int>
        var trimEnds: StateVar<Boolean>
        var subTitle: StateVar<Boolean>
        var expanded: StateVar<Boolean>
        var listener: ((Any) -> Unit)?

        var startDateTime: Date
            get() = startDate().plusSeconds(startTime())
            set(value) {
                val dur = duration
                val datePart = value.withTime(0, 0)
                startTime.value = ((value.getTime() - datePart.getTime()) / 1000).toInt()
                duration = dur
            }

        val endDateTime: Date
            get() = startDate().plusSeconds(startTime() + duration())

        val endTime: Int
            get() = (startTime() + duration()) % (24 * 60 * 60)
    }

    @Serializable
    data class Saved(
        var startTime: Int = 0,
        var duration: Int = 24 * 60 * 60 - 1,
        var maxDuration: Int = 1,
        var trimEnds: Boolean = true,
        var subTitle: Boolean = false,
    ) {


        fun getState(component: Component<out Props, out State>): SelectorState {
            val result = object : SelectorState {
                override var startDate: StateVar<Date> = StateVar(Date().withTime(0, 0), component)
                override var startTime: StateVar<Int> = StateVar(this@Saved.startTime, component)
                override var duration: StateVar<Int> = StateVar(this@Saved.duration, component)
                override var maxDuration: StateVar<Int> = StateVar(this@Saved.maxDuration, component)
                override var trimEnds: StateVar<Boolean> = StateVar(this@Saved.trimEnds, component)
                override var subTitle: StateVar<Boolean> = StateVar(this@Saved.subTitle, component)
                override var expanded: StateVar<Boolean> = StateVar(false, component)
                override var listener: ((Any) -> Unit)? = null
            }
            listOf(result.startTime, result.duration, result.maxDuration, result.trimEnds).forEach { item ->
                item.listener = { result.listener?.invoke(it) }
            }
            result.startDate.listener = {
                result.expanded.value = false            // close on date selection
                result.listener?.invoke(it)
            }
            return result
        }

        companion object {
            fun from(src: SelectorState): Saved {
                return Saved(
                    src.startTime(),
                    src.duration(),
                    src.maxDuration(),
                    src.trimEnds(),
                    src.subTitle(),
                )
            }
        }
    }
}
