package org.moonglass.ui.content

import kotlinx.browser.window
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.css.Align
import kotlinx.css.Display
import kotlinx.css.FlexDirection
import kotlinx.css.FlexWrap
import kotlinx.css.JustifyContent
import kotlinx.css.alignItems
import kotlinx.css.display
import kotlinx.css.flex
import kotlinx.css.flexDirection
import kotlinx.css.flexGrow
import kotlinx.css.flexWrap
import kotlinx.css.justifyContent
import kotlinx.css.maxHeight
import kotlinx.css.padding
import kotlinx.css.pct
import kotlinx.css.properties.ms
import kotlinx.css.properties.transition
import kotlinx.css.px
import kotlinx.css.rem
import kotlinx.css.vh
import kotlinx.css.width
import kotlinx.serialization.Serializable
import org.moonglass.ui.App
import org.moonglass.ui.api.Api
import org.moonglass.ui.api.RecList
import org.moonglass.ui.api.getEndTime
import org.moonglass.ui.api.getStartDate
import org.moonglass.ui.api.getStartTime
import org.moonglass.ui.applyState
import org.moonglass.ui.as90k
import org.moonglass.ui.live.LiveSource
import org.moonglass.ui.name
import org.moonglass.ui.plusHours
import org.moonglass.ui.plusSeconds
import org.moonglass.ui.utility.SavedState
import org.moonglass.ui.video.Player
import org.moonglass.ui.widgets.recordings.CameraList
import org.moonglass.ui.widgets.recordings.DateTimeSelector
import org.moonglass.ui.widgets.recordings.Stream
import org.moonglass.ui.withTime
import react.Props
import react.RBuilder
import react.RComponent
import react.State
import styled.css
import styled.styledDiv
import kotlin.js.Date
import kotlin.time.Duration
import kotlin.time.DurationUnit


external interface RecordingsState : State {
    var apiData: Api?
    var cameras: Map<Api.Camera, List<Stream>>
    var selectedStreams: MutableSet<String>
    var playingStream: Stream?
    var playingRecording: RecList.Recording?
    var startDate: Date
    var startTime: Int      // time of day in seconds
    var endTime: Int        // also inseconds
    var maxDuration: Int
    var trimEnds: Boolean
    var caption: Boolean
    var liveSource: LiveSource?
}

val RecordingsState.startDateTime: Date
    get() = startDate.plusSeconds(startTime)

val RecordingsState.endDateTime: Date
    get() {
        if (startTime >= endTime)
            return startDate.plusHours(24).plusSeconds(endTime)
        return startDate.plusSeconds(endTime)
    }

@Serializable
data class SavedRecordingState(
    val selectedStreams: List<String> = listOf(),
    var maxDuration: Int = 8, // hours
    var trimEnds: Boolean = true,
    var caption: Boolean = false
) {
    companion object {
        fun from(state: RecordingsState): SavedRecordingState {
            return SavedRecordingState(
                state.selectedStreams.toList(),
                state.maxDuration,
                state.caption
            )
        }
    }
}

fun RecordingsState.copyFrom(saved: SavedRecordingState) {
    maxDuration = saved.maxDuration
    trimEnds = saved.trimEnds
    caption = saved.caption
    selectedStreams = saved.selectedStreams.toMutableSet()
}


@JsExport
class Recordings() : RComponent<Props, RecordingsState>() {

    companion object {
        const val saveKey = "recordingsKey"
    }

    private val RecordingsState.maxEndDateTime get() = if (trimEnds) endDateTime.as90k else Long.MAX_VALUE
    private val RecordingsState.minStartDateTime get() = if (trimEnds) startDateTime.as90k else 0
    private val RecordingsState.allStreams get() = cameras.values.flatten()

    private val streamsNeedingRefresh = mutableSetOf<String>()


    override fun RecordingsState.init() {
        apiData = null
        cameras = mapOf()
        val restored = try {
            SavedState.restore(saveKey) ?: SavedRecordingState()
        } catch (ex: Exception) {
            SavedState.save(saveKey, SavedRecordingState())
            SavedRecordingState()
        }
        copyFrom(restored)
        startDate = Date().withTime(0, 0)
        startTime = 0
        endTime = Duration.days(1).toInt(DurationUnit.SECONDS) - 1
        refreshList()
        window.addEventListener("beforeunload", {
            SavedState.save(saveKey, SavedRecordingState.from(state))
        })
    }

    /**
     * We should be able to just compare prevState with current state to determine the newly selected
     * streams, but this doesn't seem to work - the list is the same. So instead we keep track in the callbacks
     * of any streams changed to selected state, and refresh them here
     */
    override fun componentDidUpdate(prevProps: Props, prevState: RecordingsState, snapshot: Any) {
        updateRecordings(streamsNeedingRefresh)
    }


    private fun refreshList() {
        MainScope().launch {
            App.setRefresh("Api", true)
            val list = Api.fetch()
            applyState {
                apiData = list
                cameras = list.cameras.map { camera ->
                    camera to camera.streams.map {
                        Stream(it.key, it.value, RecList(), camera)
                    }
                }.toMap()
                selectedStreams.intersect(allStreams.map { it.key })
            }
            App.setRefresh("Api", false)
            updateRecordings()
        }
    }

    private fun updateRecordings(needy: Collection<String> = state.selectedStreams) {
        if (needy.isNotEmpty()) {
            val streams = state.cameras.values.flatten().filter { it.key in needy }
            streamsNeedingRefresh.clear()
            MainScope().launch {
                App.setRefresh("UpdateRecordings", true)
                streams.forEach { cameraStream ->
                    val data = cameraStream.fetchRecordings(
                        state.startDateTime,
                        state.endDateTime,
                        Duration.hours(state.maxDuration).as90k
                    )
                    applyState {
                        cameraStream.recList = data
                    }
                }
                App.setRefresh("UpdateRecordings", false)
            }
        }
    }

    override fun componentWillUnmount() {
        SavedState.save(
            saveKey,
            SavedRecordingState.from(state)
        )
    }

    override fun RBuilder.render() {
        styledDiv {
            name = "Outer"
            css {
                display = Display.flex
                flexDirection = FlexDirection.column
                alignItems = Align.start
                width = 100.pct
                flexGrow = 1.0
                flexWrap = FlexWrap.wrap
            }
            styledDiv {
                name = "StreamGroup"
                css {
                    display = Display.flex
                    width = 100.pct
                    maxHeight = 50.vh
                    flexDirection = FlexDirection.row
                    alignItems = Align.start
                    flex(1.0, 0.0, 0.px)
                }
                styledDiv {
                    name = "SelectionGroup"
                    css {
                        display = Display.flex
                        width = 100.pct
                        flexDirection = FlexDirection.row
                        alignItems = Align.start
                    }
                    styledDiv {
                        name = "SelectorColumn"
                        css {
                            display = Display.flex
                            flexDirection = FlexDirection.column
                        }
                        child(DateTimeSelector::class) {
                            attrs {
                                startTime = state.startTime
                                endTime = state.endTime
                                startDate = state.startDate
                                maxDuration = state.maxDuration
                                caption = state.caption
                                onDateChange = {
                                    applyState {
                                        if (startDate.getTime() != it.getTime()) {
                                            startDate = it
                                            updateRecordings()
                                        }
                                    }
                                }
                                onStartChange = {
                                    applyState {
                                        if (startTime != it) {
                                            startTime = it
                                            updateRecordings()
                                        }
                                    }
                                }
                                onEndChange = {
                                    applyState {
                                        if (endTime != it) {
                                            endTime = it
                                            updateRecordings()
                                        }
                                    }
                                }
                                onTrimChange = {
                                    applyState {
                                        trimEnds = it
                                        updateRecordings()
                                    }
                                }
                                onMaxDurationChange = {
                                    applyState {
                                        if (maxDuration != it) {
                                            maxDuration = it
                                            updateRecordings()
                                        }
                                    }
                                }
                                onCaptionChange = { applyState { caption = it } }
                            }
                        }
                    }
                    styledDiv {
                        name = "CameraList"
                        css {
                            display = Display.flex
                            transition("all", 300.ms)
                            flexGrow = 1.0
                        }
                        child(CameraList::class) {
                            attrs {
                                cameras = state.cameras
                                toggleStream = { key ->
                                    applyState {
                                        if (key in selectedStreams)
                                            selectedStreams.remove(key)
                                        else {
                                            selectedStreams.add(key)
                                            streamsNeedingRefresh.add(key)
                                        }
                                    }
                                }
                                showVideo = { stream, recording ->
                                    applyState {
                                        liveSource?.close()
                                        liveSource = null
                                        playingStream = stream
                                        playingRecording = recording
                                    }
                                }
                                showLive = {
                                    applyState {
                                        liveSource = LiveSource(it.wsUrl, it.toString())
                                        playingStream = null
                                    }
                                }
                                selectedStreams = state.selectedStreams
                                maxEnd = state.maxEndDateTime
                                minStart = state.minStartDateTime
                                playingRecording = state.playingRecording
                            }
                        }
                    }
                }
            }
            styledDiv {
                name = "PlayerGroup"
                css {
                    width = 100.pct
                    justifyContent = JustifyContent.center
                    padding(0.75.rem)
                    display = Display.flex
                }
                child(Player::class) {
                    attrs {
                        title = "---"
                        state.liveSource?.let {
                            src = it.srcUrl
                            title = "Live: ${it.title}"

                        } ?: state.playingStream?.let { stream ->
                            state.playingRecording?.let { rec ->
                                src = stream.url(rec, state.caption)
                                title = "$stream ${rec.getStartDate()} ${rec.getStartTime()}-${rec.getEndTime()}"
                            }
                        }
                    }
                }
            }
        }
    }
}
