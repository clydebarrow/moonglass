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
import org.moonglass.ui.Content
import org.moonglass.ui.ContentProps
import org.moonglass.ui.NavBar
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
    var selectedStreams: MutableSet<String>
    var cameras: Map<Api.Camera, List<Stream>>
    var recLists: MutableMap<String, RecList>

    // time selector state
    var startDate: Date
    var startTime: Int      // time of day in seconds
    var endTime: Int        // also inseconds
    var maxDuration: Int
    var trimEnds: Boolean
    var caption: Boolean
    var expanded: Boolean

    var videoSource: VideoSource?
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
    var expanded: Boolean = true,
    var startTime: Int = 0,
    var endTime: Int = 24 * 60 * 60 - 1,
    var startDate: Double = Date().let { Date(it.getFullYear(), it.getMonth(), it.getDate()) }.getTime(),
    var maxDuration: Int = 1,
    var trimEnds: Boolean = true,
    var caption: Boolean = false

) {
    companion object {
        fun from(state: RecordingsState): SavedRecordingState {
            return SavedRecordingState(
                state.selectedStreams.toList(),
                state.expanded,
                state.startTime,
                state.endTime,
                state.startDate.getTime(),
                state.maxDuration,
                state.trimEnds,
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
    startDate = Date(saved.startDate)
    startTime = saved.startTime
    endTime = saved.endTime
    expanded = saved.expanded
}


@JsExport
class Recordings(props: ContentProps) : Content<ContentProps, RecordingsState>(props) {

    private val RecordingsState.maxEndDateTime get() = if (trimEnds) endDateTime.as90k else Long.MAX_VALUE
    private val RecordingsState.minStartDateTime get() = if (trimEnds) startDateTime.as90k else 0
    private val RecordingsState.allStreams get() = cameras.values.flatten()

    private fun saveMyStuff() {
        SavedState.save(
            saveKey,
            SavedRecordingState.from(state)
        )
    }

    override fun componentDidMount() {
        instance = this@Recordings
        SavedState.onUnload(::saveMyStuff)
    }

    override fun componentWillUnmount() {
        state.videoSource?.close()
        saveMyStuff()
        instance = null
        SavedState.save(
            saveKey,
            SavedRecordingState.from(state)
        )
    }

    private val cameras: Map<Api.Camera, List<Stream>>
        get() {
            return props.api.cameras.map { camera ->
                camera to camera.streams.map {
                    Stream(it.key, it.value, camera)
                }
            }.toMap()
        }

    override fun RecordingsState.init(props: ContentProps) {
        expanded = true
        recLists = mutableMapOf()
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
        window.addEventListener("beforeunload", {
            SavedState.save(saveKey, SavedRecordingState.from(state))
        })
    }

    /**
     * We should be able to just compare prevState with current state to determine the newly selected
     * streams, but this doesn't seem to work - the list is the same. So instead we keep track in the callbacks
     * of any streams changed to selected state, and refresh them here
     */
    override fun componentDidUpdate(prevProps: ContentProps, prevState: RecordingsState, snapshot: Any) {
        updateRecordings()
    }

    /**
     * Update the recordings for each selected (i.e. expanded) stream.
     */
    private fun updateRecordings() {
        // filter out unselected streams, and those for which we already have a set of recordings.
        val needy = cameras.values.flatten().filter { it.key in state.selectedStreams && it.key !in state.recLists }
        if (needy.isNotEmpty()) {
            // insert an empty list to prevent repeating the fetch below if render is called again before we finish.
            needy.forEach { state.recLists[it.key] = RecList() }
            MainScope().launch {
                val updates = needy.map { cameraStream ->
                    Api.fetchRecording(
                        cameraStream,
                        state.startDateTime,
                        state.endDateTime,
                        Duration.hours(state.maxDuration)
                    )?.let { data ->
                        console.log("Fetched ${data.recordings.size} recordings for $cameraStream")
                        Pair(cameraStream.key, data)
                    }
                }.filterNotNull()
                applyState {
                    recLists.putAll(updates)
                }
            }
        }
    }

    // update state, optionally calling refreshList() on completion of the state update.
    private fun <T : State, P : Props> RComponent<P, T>.notify(doRefresh: Boolean = true, handler: T.() -> Unit) {
        if (doRefresh)
            applyState(App::refreshAll) { handler() }
        else
            applyState { handler() }
    }

    override fun RBuilder.render() {
        styledDiv {
            css {
                width = 100.pct
                height = 100.pct
            }
            name = "RecordingsContent"
            child(NavBar::class) {
                attrs {
                    api = props.api
                    renderWidget = {
                        it.child(DateTimeSelector::class) {
                            attrs {
                                expanded = state.expanded
                                startTime = state.startTime
                                endTime = state.endTime
                                startDate = state.startDate
                                maxDuration = state.maxDuration
                                trimEnds = state.trimEnds
                                caption = state.caption

                                setStartTime = { notify { state.startTime = it } }
                                setEndTime = { notify { state.endTime = it } }
                                setStartDate = { notify { state.startDate = it } }
                                setMaxDuration = { notify { state.maxDuration = it } }
                                setTrimEnds = { notify { state.trimEnds = it } }
                                setExpanded = { notify(false) { state.expanded = it } }
                                setCaption = { notify(false) { state.caption = it } }
                            }
                        }
                    }
                }
            }
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
                                    cameras = this@Recordings.cameras
                                    recLists = state.recLists
                                    toggleStream = { key ->
                                        applyState {
                                            if (key in selectedStreams) {
                                                selectedStreams.remove(key)
                                                recLists.remove(key)
                                            } else {
                                                selectedStreams.add(key)
                                            }
                                        }
                                    }
                                    showVideo = {
                                        console.log("Showvideo ${it.caption}/${it.srcUrl}")
                                        applyState {
                                            videoSource?.close()
                                            videoSource = it
                                            setState { expanded = false }
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
    }

    companion object {
        const val saveKey = "recordingsSaveKey"

        var instance: Recordings? = null

    }
}
