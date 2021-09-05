package org.moonglass.ui.widgets.recordings

import calendar
import kotlinx.css.Align
import kotlinx.css.Display
import kotlinx.css.FlexDirection
import kotlinx.css.GridTemplateColumns
import kotlinx.css.JustifyContent
import kotlinx.css.Position
import kotlinx.css.alignContent
import kotlinx.css.alignItems
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
import kotlinx.serialization.Serializable
import org.moonglass.ui.ZIndex
import org.moonglass.ui.applyState
import org.moonglass.ui.cardStyle
import org.moonglass.ui.content.Recordings
import org.moonglass.ui.dismisser
import org.moonglass.ui.formatDate
import org.moonglass.ui.formatHhMm
import org.moonglass.ui.name
import org.moonglass.ui.style.expandButton
import org.moonglass.ui.style.shrinkable
import org.moonglass.ui.utility.SavedState
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
import react.setState
import styled.css
import styled.styledDiv
import styled.styledInput
import styled.styledOption
import styled.styledSelect
import kotlin.js.Date

external interface DateTimeSelectorState : State {
    var expanded: Boolean
    var startTime: Int  // times of day in seconds
    var endTime: Int
    var startDate: Date
    var maxDuration: Int        // in hours
    var trimEnds: Boolean
    var caption: Boolean
}

class DateTimeSelector : RComponent<Props, DateTimeSelectorState>() {

    @Serializable
    data class Saver(
        var expanded: Boolean = true,
        var startTime: Int = 0,
        var endTime: Int = 24 * 60 * 60 - 1,
        var startDate: Double = Date().let { Date(it.getFullYear(), it.getMonth(), it.getDate()) }.getTime(),
        var maxDuration: Int = 1,
        var trimEnds: Boolean = true,
        var caption: Boolean = false
    )

    override fun componentDidMount() {
        instance = this
        SavedState.onUnload(::saveMyStuff)
    }

    private fun saveMyStuff() {
        SavedState.save(saveKey,
            state.run {
                Saver(expanded, startTime, endTime, startDate.getTime(), maxDuration, trimEnds, caption)
            }
        )
    }

    override fun componentWillUnmount() {
        saveMyStuff()
        instance = null
    }

    override fun DateTimeSelectorState.init() {
        val saved = SavedState.restore(saveKey) ?: Saver()
        expanded = saved.expanded
        startDate = Date(saved.startDate)
        startTime = saved.startTime
        endTime = saved.endTime
        trimEnds = saved.trimEnds
        caption = saved.caption
        maxDuration = saved.maxDuration
    }

    private fun keyDown(event: KeyboardEvent<*>) {
        event.apply {
            preventDefault()
            stopPropagation()
            when (key) {
                "Escape" -> setState { expanded = false }
            }
        }
    }

    override fun RBuilder.render() {
        styledDiv {
            name = "selectedDateTime"
            attrs {
                onClickFunction = { applyState { expanded = !expanded } }
                onKeyDown = ::keyDown
            }
            css {
                display = Display.flex
                justifyContent = JustifyContent.center
                alignContent = Align.center
                flexDirection = FlexDirection.row
                position = Position.relative
            }
            expandButton(state.expanded)
            listOf(
                state.startDate.formatDate,
                "${state.startTime.formatHhMm} - ${state.endTime.formatHhMm}"
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
        dismisser({ collapse() }, visible = state.expanded) {}
        styledDiv {
            attrs {
                key = "dateTimeSelector"
            }
            shrinkable(state.expanded, 50.rem)
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
                        defaultActiveStartDate = state.startDate
                        onChange = {
                            setState {
                                startDate = it
                            }
                            Recordings.onStartDate(it)
                        }
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
                    initialTime = state.startTime
                    use24HourTime = true
                    onChange = {
                        setState {
                            startTime = it
                        }
                        Recordings.onStartTime(it)
                    }
                }
                label {
                    +"End time:"
                }
                timePicker {
                    initialTime = state.endTime
                    use24HourTime = true
                    onChange = {
                        setState {
                            endTime = it
                        }
                        Recordings.onEndTime(it)
                    }
                }
                label {
                    attrs {
                        htmlFor = "trimCheckbox"
                    }
                    +"Trim to start/end:"
                }
                styledInput(InputType.checkBox) {
                    if (state.trimEnds)
                        attrs["checked"] = ""
                    attrs {
                        id = "trimCheckbox"
                        onChangeFunction = {
                            setState {
                                trimEnds = !trimEnds
                            }
                            Recordings.onTrimEnds(state.trimEnds)
                        }
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
                        checked = state.caption
                        onChangeFunction = {
                            setState {
                                caption = !caption
                            }
                            Recordings.onCaption(state.caption)
                        }
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
                        value = state.maxDuration.toString()
                        onChangeFunction =
                            {
                                val max = it.currentTarget.unsafeCast<HTMLSelectElement>().value.toInt()
                                setState {
                                    maxDuration = max
                                }
                                Recordings.onMaxDuration(max)
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
        const val saveKey = "dateTimeSelectorSavedState"
        var instance: DateTimeSelector? = null

        fun collapse() {
            instance?.setState { expanded = false }
        }
    }
}
