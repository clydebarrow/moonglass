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

package org.moonglass.ui.content

import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.css.Align
import kotlinx.css.Display
import kotlinx.css.FlexDirection
import kotlinx.css.FlexWrap
import kotlinx.css.JustifyContent
import kotlinx.css.JustifyItems
import kotlinx.css.LinearDimension
import kotlinx.css.Position
import kotlinx.css.TextAlign
import kotlinx.css.alignContent
import kotlinx.css.alignItems
import kotlinx.css.backgroundColor
import kotlinx.css.borderBottomColor
import kotlinx.css.borderBottomWidth
import kotlinx.css.borderLeftColor
import kotlinx.css.borderLeftWidth
import kotlinx.css.color
import kotlinx.css.display
import kotlinx.css.flex
import kotlinx.css.flexDirection
import kotlinx.css.flexGrow
import kotlinx.css.flexWrap
import kotlinx.css.fontSize
import kotlinx.css.height
import kotlinx.css.justifyContent
import kotlinx.css.justifyItems
import kotlinx.css.marginLeft
import kotlinx.css.marginRight
import kotlinx.css.marginTop
import kotlinx.css.maxHeight
import kotlinx.css.padding
import kotlinx.css.paddingBottom
import kotlinx.css.paddingTop
import kotlinx.css.pct
import kotlinx.css.position
import kotlinx.css.properties.ms
import kotlinx.css.properties.transition
import kotlinx.css.px
import kotlinx.css.rem
import kotlinx.css.textAlign
import kotlinx.css.width
import kotlinx.css.zIndex
import kotlinx.serialization.Serializable
import org.moonglass.ui.Content
import org.moonglass.ui.ContentProps
import org.moonglass.ui.ResponsiveLayout
import org.moonglass.ui.Theme
import org.moonglass.ui.ZIndex
import org.moonglass.ui.api.Api
import org.moonglass.ui.api.RecList
import org.moonglass.ui.applyState
import org.moonglass.ui.as90k
import org.moonglass.ui.name
import org.moonglass.ui.useColorSet
import org.moonglass.ui.utility.SavedState
import org.moonglass.ui.utility.SavedState.restore
import org.moonglass.ui.utility.StateValue
import org.moonglass.ui.utility.StateVar
import org.moonglass.ui.video.RecordingSource
import org.moonglass.ui.video.StreamPlayer
import org.moonglass.ui.video.UrlPlayer
import org.moonglass.ui.widgets.recordings.CameraList
import org.moonglass.ui.widgets.recordings.DateTimeSelector
import org.moonglass.ui.widgets.recordings.Stream
import react.RBuilder
import react.State
import styled.css
import styled.styledDiv
import kotlin.js.Date
import kotlin.time.Duration

external interface RecordingsState : State {
    var selectedStreams: MutableSet<String>
    var recLists: MutableMap<String, RecList>

    // time selector state
    var selectorState: DateTimeSelector.SelectorState

    var recordingSource: RecordingSource?
    var liveSource: StateValue<String>
}

val RecordingsState.startDateTime: Date
    get() = selectorState.startDateTime

val RecordingsState.endDateTime: Date
    get() {
        return selectorState.endDateTime
    }

@Serializable
data class SavedRecordingState(
    val selectedStreams: List<String> = listOf(),
    var dateTimeState: DateTimeSelector.Saved = DateTimeSelector.Saved()

) {
    companion object {
        fun from(state: RecordingsState): SavedRecordingState {
            return SavedRecordingState(
                state.selectedStreams.toList(),
                DateTimeSelector.Saved.from(state.selectorState)
            )
        }
    }
}


@Suppress("NON_EXPORTABLE_TYPE")
@JsExport
class Recordings(props: ContentProps) : Content<ContentProps, RecordingsState>(props) {

    private fun saveMyStuff() {
        SavedState.save(
            saveKey,
            SavedRecordingState.from(state)
        )
    }

    private fun RecordingsState.copyFrom(saved: SavedRecordingState) {
        selectedStreams = saved.selectedStreams.toMutableSet()
        selectorState = saved.dateTimeState.getState(this@Recordings)
    }


    override fun componentDidMount() {
        SavedState.addOnUnload(::saveMyStuff)
    }

    override fun componentWillUnmount() {
        saveMyStuff()
        SavedState.removeOnUnload(::saveMyStuff)
    }

    private val cameras: Map<Api.Camera, List<Stream>>
        get() {
            return props.api.cameras.associateWith { camera ->
                camera.streams.map {
                    Stream(it.key, it.value, camera)
                }
            }
        }

    override fun RecordingsState.init(props: ContentProps) {
        recLists = mutableMapOf()
        copyFrom(saveKey.restore { SavedRecordingState() })
        selectorState.listener = { updateRecordings(true) }
        liveSource = StateVar("", this@Recordings)
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
     * @param force If true, refresh all expanded streams.
     */
    private fun updateRecordings(force: Boolean = false) {
        // filter out unselected streams, and those for which we already have a set of recordings.
        val needy =
            cameras.values.flatten().filter { it.key in state.selectedStreams && (force || it.key !in state.recLists) }
        if (needy.isNotEmpty()) {
            // insert an empty list to prevent repeating the fetch below if render is called again before we finish.
            needy.forEach { state.recLists[it.key] = RecList() }
            MainScope().launch {
                val updates = needy.mapNotNull { cameraStream ->
                    Api.fetchRecording(
                        cameraStream,
                        state.startDateTime,
                        state.endDateTime,
                        Duration.hours(state.selectorState.maxDuration())
                    )?.let { data ->
                        console.log("Fetched ${data.recordings.size} recordings for $cameraStream")
                        Pair(cameraStream.key, data)
                    }
                }
                applyState {
                    selectedStreams =
                        selectedStreams.filter { stream -> stream in Api.allStreams.map { it.key } }.toMutableSet()
                    recLists.putAll(updates)
                }
            }
        }
    }

    override fun RBuilder.renderNavBarWidget() {
        child(DateTimeSelector::class) {
            attrs {
                data = state.selectorState
                showDuration = true
            }
        }
    }

    override fun RBuilder.renderContent() {
        styledDiv {
            css {
                zIndex = ZIndex.Content()
                width = 100.pct
                height = 100.pct
                paddingTop = ResponsiveLayout.navBarEmHeight
            }
            name = "RecordingsContent"
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
                    useColorSet(Theme().content)
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
                                borderBottomColor = Theme().borderColor

                            } else {
                                borderLeftWidth = 1.px
                                borderLeftColor = Theme().borderColor
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
                                            } else {
                                                recLists.remove(key)
                                                selectedStreams.add(key)
                                            }
                                        }
                                    }
                                    showRecording = {
                                        state.selectorState.expanded.value = false        //
                                        state.liveSource.value = ""
                                        applyState {
                                            recordingSource = it
                                        }
                                    }
                                    showLive = {
                                        state.selectorState.expanded.value = false        //
                                        state.liveSource.value = it
                                        applyState {
                                            recordingSource = null
                                        }
                                    }
                                    selectedStreams = state.selectedStreams
                                    maxEnd = state.maxEndDateTime
                                    minStart = state.minStartDateTime
                                    playingRecording = state.recordingSource?.recording
                                    subTitle = state.selectorState.subTitle()
                                }
                            }
                        }
                    }
                }
                styledDiv {
                    name = "PlayerGroup"
                    css {
                        useColorSet(Theme().content)
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
                    styledDiv {
                        css {
                            display = Display.flex
                            position = Position.relative
                            flexDirection = FlexDirection.column
                            justifyItems = JustifyItems.center
                            width = 100.pct
                            height = 100.pct
                        }

                        if (state.recordingSource != null) {
                            styledDiv {
                                css {
                                    fontSize = 1.2.rem
                                    textAlign = TextAlign.center
                                    backgroundColor = Theme().header.backgroundColor
                                    color = Theme().header.textColor
                                }
                                +(state.recordingSource?.caption ?: state.liveSource.value)
                            }

                            child(UrlPlayer::class) {
                                attrs {
                                    height = ResponsiveLayout.contentHeight - 3.rem
                                    source = state.recordingSource
                                    showControls = true
                                }
                            }
                        } else {
                            child(StreamPlayer::class) {
                                attrs {
                                    height = ResponsiveLayout.contentHeight - 4.rem
                                    source = state.liveSource
                                    overlay = false
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    companion object {
        const val saveKey = "recordingsSaveKey"
    }
}

val RecordingsState.maxEndDateTime get() = if (selectorState.trimEnds()) endDateTime.as90k else Long.MAX_VALUE
val RecordingsState.minStartDateTime get() = if (selectorState.trimEnds()) startDateTime.as90k else 0
