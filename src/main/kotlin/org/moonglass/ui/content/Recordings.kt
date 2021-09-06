package org.moonglass.ui.content

import kotlinx.browser.window
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.css.Align
import kotlinx.css.Color
import kotlinx.css.Display
import kotlinx.css.FlexDirection
import kotlinx.css.FlexWrap
import kotlinx.css.JustifyContent
import kotlinx.css.LinearDimension
import kotlinx.css.alignContent
import kotlinx.css.alignItems
import kotlinx.css.borderBottomColor
import kotlinx.css.borderBottomWidth
import kotlinx.css.borderLeftColor
import kotlinx.css.borderLeftWidth
import kotlinx.css.display
import kotlinx.css.flex
import kotlinx.css.flexDirection
import kotlinx.css.flexGrow
import kotlinx.css.flexWrap
import kotlinx.css.height
import kotlinx.css.justifyContent
import kotlinx.css.marginLeft
import kotlinx.css.marginRight
import kotlinx.css.marginTop
import kotlinx.css.maxHeight
import kotlinx.css.padding
import kotlinx.css.paddingBottom
import kotlinx.css.pct
import kotlinx.css.properties.ms
import kotlinx.css.properties.transition
import kotlinx.css.px
import kotlinx.css.rem
import kotlinx.css.width
import kotlinx.serialization.Serializable
import org.moonglass.ui.App
import org.moonglass.ui.ResponsiveLayout
import org.moonglass.ui.api.Api
import org.moonglass.ui.api.RecList
import org.moonglass.ui.applyState
import org.moonglass.ui.as90k
import org.moonglass.ui.name
import org.moonglass.ui.plusHours
import org.moonglass.ui.plusSeconds
import org.moonglass.ui.utility.SavedState
import org.moonglass.ui.video.Player
import org.moonglass.ui.video.RecordingSource
import org.moonglass.ui.video.VideoSource
import org.moonglass.ui.widgets.recordings.CameraList
import org.moonglass.ui.widgets.recordings.DateTimeSelector
import org.moonglass.ui.widgets.recordings.Stream
import org.moonglass.ui.withTime
import react.Props
import react.RBuilder
import react.RComponent
import react.State
import react.setState
import styled.css
import styled.styledDiv
import kotlin.js.Date
import kotlin.time.Duration
import kotlin.time.DurationUnit


external interface RecordingsState : State {
    var apiData: Api?
    var cameras: Map<Api.Camera, List<Stream>>
    var selectedStreams: MutableSet<String>

    // time selector state
    var startDate: Date
    var startTime: Int      // time of day in seconds
    var endTime: Int        // also inseconds
    var maxDuration: Int
    var trimEnds: Boolean
    var caption: Boolean

    var videoSource: VideoSource?
    var selectorShowing: Boolean
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
class Recordings(props: Props) : RComponent<Props, RecordingsState>(props) {


    private val RecordingsState.maxEndDateTime get() = if (trimEnds) endDateTime.as90k else Long.MAX_VALUE
    private val RecordingsState.minStartDateTime get() = if (trimEnds) startDateTime.as90k else 0
    private val RecordingsState.allStreams get() = cameras.values.flatten()

    private val streamsNeedingRefresh = mutableSetOf<String>()

    override fun componentDidMount() {
        instance = this
    }

    override fun componentWillUnmount() {
        instance = null
        SavedState.save(
            saveKey,
            SavedRecordingState.from(state)
        )
    }


    override fun RecordingsState.init(props: Props) {
        selectorShowing = true
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

    override fun RBuilder.render() {
        styledDiv {
            name = "Outer"
            css {
                display = Display.flex
                height = 100.pct
                if (ResponsiveLayout.isPortrait) {
                    flexDirection = FlexDirection.column
                    width = 100.pct
                } else {
                    flexDirection = FlexDirection.row
                }
                alignItems = Align.start
                flexGrow = 1.0
                flexWrap = FlexWrap.nowrap
            }
            styledDiv {
                name = "StreamGroup"
                css {
                    display = Display.flex
                    flexDirection = FlexDirection.column
                    alignItems = Align.start
                    flex(1.0, 0.0, 0.px)
                    if (ResponsiveLayout.isPortrait) {
                        width = 100.pct
                        height = ResponsiveLayout.contentHeight

                    } else {
                        height = 100.pct
                    }
                    paddingBottom = 1.rem
                }
                styledDiv {
                    name = "SelectionGroup"
                    css {
                        display = Display.flex
                        width = 100.pct
                        height = 100.pct
                        flexDirection = FlexDirection.column
                        alignItems = Align.start
                        if (ResponsiveLayout.isPortrait) {
                            borderBottomWidth = 1.px
                            borderBottomColor = Color.gray

                        } else {
                            borderLeftWidth = 1.px
                            borderLeftColor = Color.gray
                        }
                    }
                    styledDiv {
                        name = "CameraList"
                        css {
                            display = Display.flex
                            transition("all", 300.ms)
                            width = 100.pct
                            maxHeight = 100.pct
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
                                showVideo = {
                                    applyState {
                                        videoSource = it
                                        DateTimeSelector.collapse()
                                    }
                                }
                                selectedStreams = state.selectedStreams
                                maxEnd = state.maxEndDateTime
                                minStart = state.minStartDateTime
                                playingRecording = (state.videoSource as? RecordingSource)?.recording
                            }
                        }
                    }
                }
            }
            styledDiv {
                name = "PlayerGroup"
                css {
                    justifyContent = JustifyContent.center
                    alignContent = Align.center
                    padding(0.75.rem)
                    display = Display.flex
                    flex(1.0, 0.0, 0.px)
                    if (ResponsiveLayout.isPortrait) {
                        height = ResponsiveLayout.playerHeight
                        marginLeft = LinearDimension.auto
                        marginRight = LinearDimension.auto
                    } else {
                        marginTop = 0.px
                    }

                }
                child(Player::class) {
                    attrs {
                        key = "MainPlayer"
                        source = state.videoSource
                    }
                }
            }
        }
    }

    companion object {
        const val saveKey = "recordingsKey"

        var instance: Recordings? = null

        fun refreshAll() {
            instance?.refreshList()
        }

        fun <T : Any> notify(value: T, update: Boolean = true, block: RecordingsState.(T) -> Unit) {
            instance?.apply {
                setState {
                    block(value)
                    if (update)
                        updateRecordings()
                }
            }
        }

        fun onStartDate(value: Date) = notify(value) { startDate = value }
        fun onStartTime(value: Int) = notify(value) { startTime = value }      // time of day in seconds
        fun onEndTime(value: Int) = notify(value) { endTime = value }         // also inseconds
        fun onMaxDuration(value: Int) = notify(value) { maxDuration = value }
        fun onTrimEnds(value: Boolean) = notify(value) { trimEnds = value }
        fun onCaption(value: Boolean) = notify(value, false) { caption = value }
    }
}
