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
import org.moonglass.ui.Theme
import org.moonglass.ui.ZIndex
import org.moonglass.ui.cardStyle
import org.moonglass.ui.dismisser
import org.moonglass.ui.formatDate
import org.moonglass.ui.formatHhMm
import org.moonglass.ui.name
import org.moonglass.ui.style.checkBox
import org.moonglass.ui.style.expandButton
import org.moonglass.ui.style.shrinkable
import org.moonglass.ui.useColorSet
import org.moonglass.ui.utility.StateVar
import org.moonglass.ui.widgets.timePicker
import org.w3c.dom.HTMLSelectElement
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
    var expanded: StateVar<Boolean>
    var startTime: StateVar<Int>
    var endTime: StateVar<Int>
    var startDate: StateVar<Date>
    var maxDuration: StateVar<Int>
    var trimEnds: StateVar<Boolean>
    var subTitle: StateVar<Boolean>
}

class DateTimeSelector : RComponent<DateTimeSelectorProps, State>() {

    private fun keyDown(event: KeyboardEvent<*>) {
        event.apply {
            preventDefault()
            stopPropagation()
            when (key) {
                "Escape" -> props.expanded.value = false
            }
        }
    }

    override fun RBuilder.render() {
        styledDiv {
            name = "selectedDateTime"
            attrs {
                onClickFunction = {
                    props.expanded.value = !props.expanded.value
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
                if (props.expanded())
                    zIndex = ZIndex.Input()
            }
            expandButton(props.expanded())
            listOf(
                props.startDate().formatDate,
                "${props.startTime().formatHhMm} - ${props.endTime().formatHhMm}"
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
        dismisser({ props.expanded.value = false }, visible = props.expanded()) {
            css {
                backgroundColor = Theme().overlay
            }
        }
        styledDiv {
            name = "DateTimeSelectorExpandable"
            shrinkable(props.expanded(), 50.rem)
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
                        defaultActiveStartDate = props.startDate()
                        onChange = { props.startDate.value = it }
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
                    initialTime = props.startTime()
                    use24HourTime = true
                    onChange = { props.startTime.value = it }
                }
                label {
                    +"End time:"
                }
                timePicker {
                    initialTime = props.endTime()
                    use24HourTime = true
                    onChange = { props.endTime.value = it }
                }
                checkBox("Trim to start/end", props.trimEnds)
                checkBox("Add timestamp subtitles", props.subTitle)
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
                        value = props.maxDuration().toString()
                        onChangeFunction =
                            {
                                val max = it.currentTarget.unsafeCast<HTMLSelectElement>().value.toInt()
                                props.maxDuration.value = max
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
