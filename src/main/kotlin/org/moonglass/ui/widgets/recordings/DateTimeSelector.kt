package org.moonglass.ui.widgets.recordings

import calendar
import kotlinx.css.Align
import kotlinx.css.Display
import kotlinx.css.FlexDirection
import kotlinx.css.GridTemplateColumns
import kotlinx.css.alignItems
import kotlinx.css.display
import kotlinx.css.flexDirection
import kotlinx.css.gridTemplateColumns
import kotlinx.css.margin
import kotlinx.css.padding
import kotlinx.css.rem
import kotlinx.html.InputType
import kotlinx.html.id
import kotlinx.html.js.onChangeFunction
import org.moonglass.ui.cardStyle
import org.moonglass.ui.widgets.timePicker
import org.w3c.dom.HTMLSelectElement
import react.Props
import react.RBuilder
import react.RComponent
import react.State
import react.dom.ReactHTML.label
import react.dom.attrs
import styled.css
import styled.styledDiv
import styled.styledInput
import styled.styledOption
import styled.styledSelect
import kotlin.js.Date

external interface DateTimeSelectorProps : Props {
    var startTime: Int  // times of day in seconds
    var endTime: Int
    var startDate: Date
    var maxDuration: Int        // in hours
    var trimEnds: Boolean
    var caption: Boolean
    var onStartChange: (Int) -> Unit
    var onEndChange: (Int) -> Unit
    var onDateChange: (Date) -> Unit
    var onMaxDurationChange: (Int) -> Unit
    var onTrimChange: (Boolean) -> Unit
    var onCaptionChange: (Boolean) -> Unit
}

class DateTimeSelector(props: DateTimeSelectorProps) : RComponent<DateTimeSelectorProps, State>(props) {

    override fun RBuilder.render() {
        styledDiv {
            cardStyle()
            css {
                flexDirection = FlexDirection.column
            }
            calendar {
                attrs {
                    defaultView = "month"
                    defaultActiveStartDate = props.startDate
                    onChange = { props.onDateChange(it) }
                }
            }
            styledDiv {
                css {
                    display = Display.grid
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
                    initialTime = props.startTime
                    use24HourTime = true
                    onChange = props.onStartChange
                }
                label {
                    +"End time:"
                }
                timePicker {
                    initialTime = props.endTime
                    use24HourTime = true
                    onChange = props.onEndChange
                }
                label {
                    attrs {
                        htmlFor = "trimCheckbox"
                    }
                    +"Trim to start/end:"
                }
                styledInput(InputType.checkBox) {
                    if (props.trimEnds)
                        attrs["checked"] = ""
                    attrs {
                        id = "trimCheckbox"
                        onChangeFunction = { props.onTrimChange(!props.trimEnds) }
                    }
                    css {
                        margin(left = 1.5.rem)
                    }
                }
                label {
                    attrs {
                        htmlFor = "captionCheckBox"
                    }
                    +"Add timestamp subtitles:"
                }
                styledInput(InputType.checkBox) {
                    attrs {
                        id = "captionCheckBox"
                        checked = props.caption
                        onChangeFunction = { props.onCaptionChange(!props.caption) }
                    }
                    css {
                        margin(left = 1.5.rem)
                    }
                }
                label {
                    attrs {
                        htmlFor = "maxDurationSelect"
                    }
                    +"Max duration:"
                }
                styledSelect {
                    attrs {
                        id = "maxDurationSelect"
                        value = props.maxDuration.toString()
                        onChangeFunction =
                            {
                                props.onMaxDurationChange(
                                    it.currentTarget
                                        .unsafeCast<HTMLSelectElement>().value.toInt()
                                )
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
