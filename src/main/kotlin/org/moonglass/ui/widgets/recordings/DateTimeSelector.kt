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
import kotlinx.css.cursor
import kotlinx.css.display
import kotlinx.css.flexDirection
import kotlinx.css.gridTemplateColumns
import kotlinx.css.justifyContent
import kotlinx.css.margin
import kotlinx.css.padding
import kotlinx.css.pct
import kotlinx.css.position
import kotlinx.css.rem
import kotlinx.css.top
import kotlinx.css.width
import kotlinx.css.zIndex
import kotlinx.html.InputType
import kotlinx.html.id
import kotlinx.html.js.onChangeFunction
import kotlinx.html.js.onClickFunction
import org.moonglass.ui.ZIndex
import org.moonglass.ui.cardStyle
import org.moonglass.ui.dismisser
import org.moonglass.ui.formatDate
import org.moonglass.ui.formatHhMm
import org.moonglass.ui.name
import org.moonglass.ui.style.expandButton
import org.moonglass.ui.style.shrinkable
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
import styled.styledInput
import styled.styledOption
import styled.styledSelect
import kotlin.js.Date

external interface DateTimeSelectorProps : Props {
    var expanded: Boolean
    var startTime: Int
    var endTime: Int
    var startDate: Date
    var maxDuration: Int
    var trimEnds: Boolean
    var caption: Boolean
    var setExpanded: (Boolean) -> Unit
    var setStartTime: (Int) -> Unit
    var setEndTime: (Int) -> Unit
    var setStartDate: (Date) -> Unit
    var setMaxDuration: (Int) -> Unit
    var setTrimEnds: (Boolean) -> Unit
    var setCaption: (Boolean) -> Unit
}

class DateTimeSelector : RComponent<DateTimeSelectorProps, State>() {

    private fun keyDown(event: KeyboardEvent<*>) {
        event.apply {
            preventDefault()
            stopPropagation()
            when (key) {
                "Escape" -> props.setExpanded(false)
            }
        }
    }

    override fun RBuilder.render() {
        styledDiv {
            name = "selectedDateTime"
            attrs {
                onClickFunction = {
                    console.log("onclick expanded ${props.expanded}")
                    props.setExpanded(!props.expanded)
                }
                onKeyDown = ::keyDown
            }
            css {
                display = Display.flex
                justifyContent = JustifyContent.center
                alignContent = Align.center
                flexDirection = FlexDirection.row
                position = Position.relative
                cursor = Cursor.pointer
                if(props.expanded)
                    zIndex = ZIndex.Input()
            }
            expandButton(props.expanded)
            listOf(
                props.startDate.formatDate,
                "${props.startTime.formatHhMm} - ${props.endTime.formatHhMm}"
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
        dismisser({ props.setExpanded(false) }, visible = props.expanded) {}
        styledDiv {
            name = "DateTimeSelectorExpandable"
            shrinkable(props.expanded, 50.rem)
            cardStyle()
            css {
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
                    display = Display.flex
                    justifyContent = JustifyContent.center
                    width = 100.pct
                }
                calendar {
                    attrs {
                        defaultView = "month"
                        defaultActiveStartDate = props.startDate
                        onChange = { props.setStartDate(it) }
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
                    onChange = { props.setStartTime(it) }
                }
                label {
                    +"End time:"
                }
                timePicker {
                    initialTime = props.endTime
                    use24HourTime = true
                    onChange = { props.setEndTime(it) }
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
                        onChangeFunction = { props.setTrimEnds(!props.trimEnds) }
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
                        onChangeFunction = { props.setCaption(!props.caption) }
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
                                val max = it.currentTarget.unsafeCast<HTMLSelectElement>().value.toInt()
                                props.setMaxDuration(max)
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
