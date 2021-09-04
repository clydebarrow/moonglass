package org.moonglass.ui.widgets.recordings

import calendar
import kotlinx.css.Align
import kotlinx.css.Display
import kotlinx.css.FlexDirection
import kotlinx.css.GridTemplateColumns
import kotlinx.css.JustifyContent
import kotlinx.css.alignItems
import kotlinx.css.display
import kotlinx.css.flexDirection
import kotlinx.css.gridTemplateColumns
import kotlinx.css.justifyContent
import kotlinx.css.margin
import kotlinx.css.padding
import kotlinx.css.pct
import kotlinx.css.rem
import kotlinx.css.width
import kotlinx.css.zIndex
import kotlinx.html.InputType
import kotlinx.html.id
import kotlinx.html.js.onChangeFunction
import kotlinx.html.js.onClickFunction
import org.moonglass.ui.ZIndex
import org.moonglass.ui.applyState
import org.moonglass.ui.content.Recordings
import org.moonglass.ui.formatDate
import org.moonglass.ui.formatHhMm
import org.moonglass.ui.name
import org.moonglass.ui.style.column
import org.moonglass.ui.style.expandButton
import org.moonglass.ui.style.shrinkable
import org.moonglass.ui.utility.SavedState
import org.moonglass.ui.widgets.timePicker
import org.w3c.dom.HTMLSelectElement
import react.Props
import react.RBuilder
import react.RComponent
import react.State
import react.dom.ReactHTML.label
import react.dom.attrs
import react.setState
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
}

external interface DateTimeSelectorState : State {
    var expanded: Boolean
}

class DateTimeSelector(props: DateTimeSelectorProps) : RComponent<DateTimeSelectorProps, DateTimeSelectorState>(props) {

    override fun componentDidMount() {
        instance = this
    }

    override fun componentWillUnmount() {
        SavedState.save(saveKey, state.expanded)
        instance = null
    }

    override fun DateTimeSelectorState.init(props: DateTimeSelectorProps) {
        expanded = SavedState.restore(saveKey) ?: true
    }

    override fun RBuilder.render() {
        styledDiv {
            name = "selectedDateTime"
            attrs {
                onClickFunction = { applyState { expanded = !expanded } }
            }
            css {
                display = Display.flex
                justifyContent = JustifyContent.spaceBetween
                flexDirection = FlexDirection.row
                width = 100.pct
            }
            expandButton(state.expanded)
            column(props.startDate.formatDate, 0.20, JustifyContent.center)
            column("From: ${props.startTime.formatHhMm} To: ${props.endTime.formatHhMm}", 0.30, JustifyContent.center)
        }
        styledDiv {
            attrs {
                key = "dateTimeSelector"
            }
            shrinkable(state.expanded, 50.rem)
            css {
                display = Display.flex
                flexDirection = FlexDirection.column
                justifyContent = JustifyContent.center
                zIndex = ZIndex.Input()
            }
            // wrap the calendar so we can center it.
            styledDiv {
                css {
                    display = Display.flex
                    justifyContent = JustifyContent.center
                    width = 100.pct
                }
                calendar {
                    attrs {
                        defaultView = "month"
                        defaultActiveStartDate = props.startDate
                        onChange = { Recordings.onStartDate(it) }
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
                    initialTime = props.startTime
                    use24HourTime = true
                    onChange = { Recordings.onStartTime(it) }
                }
                label {
                    +"End time:"
                }
                timePicker {
                    initialTime = props.endTime
                    use24HourTime = true
                    onChange = { Recordings.onEndTime(it) }
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
                        onChangeFunction = { Recordings.onTrimEnds(!props.trimEnds) }
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
                        onChangeFunction = { Recordings.onCaption(!props.caption) }
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
                                Recordings.onMaxDuration(
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

    companion object {
        const val saveKey = "dateTimeSelectorExpanded"
        var instance: DateTimeSelector? = null

        fun collapse() {
            instance?.setState { expanded = false }
        }
    }
}
