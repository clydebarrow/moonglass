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

import kotlinx.css.Align
import kotlinx.css.Cursor
import kotlinx.css.Display
import kotlinx.css.FlexDirection
import kotlinx.css.FlexWrap
import kotlinx.css.FontWeight.Companion.bolder
import kotlinx.css.JustifyContent
import kotlinx.css.LinearDimension
import kotlinx.css.Overflow
import kotlinx.css.Position
import kotlinx.css.TextAlign
import kotlinx.css.alignContent
import kotlinx.css.backgroundColor
import kotlinx.css.color
import kotlinx.css.cursor
import kotlinx.css.display
import kotlinx.css.flex
import kotlinx.css.flexDirection
import kotlinx.css.flexGrow
import kotlinx.css.flexWrap
import kotlinx.css.fontWeight
import kotlinx.css.justifyContent
import kotlinx.css.marginRight
import kotlinx.css.opacity
import kotlinx.css.overflowY
import kotlinx.css.padding
import kotlinx.css.pct
import kotlinx.css.position
import kotlinx.css.px
import kotlinx.css.rem
import kotlinx.css.textAlign
import kotlinx.css.width
import kotlinx.html.DIV
import kotlinx.html.js.onClickFunction
import org.moonglass.ui.Theme
import org.moonglass.ui.Time90k
import org.moonglass.ui.api.Api
import org.moonglass.ui.api.RecList
import org.moonglass.ui.api.bitrate
import org.moonglass.ui.api.fps
import org.moonglass.ui.api.getEndTime
import org.moonglass.ui.api.getStartDate
import org.moonglass.ui.api.getStartTime
import org.moonglass.ui.api.storage
import org.moonglass.ui.asBitRate
import org.moonglass.ui.asSize
import org.moonglass.ui.imageSrc
import org.moonglass.ui.name
import org.moonglass.ui.style.column
import org.moonglass.ui.style.expandButton
import org.moonglass.ui.style.shrinkable
import org.moonglass.ui.toDuration
import org.moonglass.ui.tooltip
import org.moonglass.ui.useColorSet
import org.moonglass.ui.video.RecordingSource
import react.Props
import react.RBuilder
import react.RComponent
import react.State
import react.dom.attrs
import styled.StyledDOMBuilder
import styled.css
import styled.styledA
import styled.styledButton
import styled.styledDiv
import styled.styledImg

/**
 * Present a list of cameras and their streams, expandable to show recordings for each stream.
 */

class CameraList(props: CameraListProps) : RComponent<CameraListProps, CameraListState>(props) {

    /**
     * Build the header line for a camera
     */
    private fun StyledDOMBuilder<DIV>.cameraHeader(camera: Api.Camera) {
        styledDiv {
            name = "cameraHeader"
            css {
                display = Display.flex
                flexDirection = FlexDirection.row
                justifyContent = JustifyContent.spaceBetween
                backgroundColor = Theme().header.backgroundColor
            }
            styledDiv {
                css {
                    display = Display.flex
                    flexGrow = 0.3
                    color = Theme().header.textColor
                    fontWeight = bolder
                    padding(left = 1.rem, right = 0.5.rem)
                }
                +camera.shortName
            }
            styledDiv {
                css {
                    display = Display.flex
                    flexGrow = 0.7
                    color = Theme().header.textColor
                    textAlign = TextAlign.right
                    justifyContent = JustifyContent.center
                    padding(left = 1.rem, right = 0.5.rem)
                }
                +camera.description
            }
        }
    }

    // get the recList for a given stream (by key)
    private val Stream.recList get() = props.recLists[key] ?: RecList()

    // construct a line describing a stream.
    private fun StyledDOMBuilder<DIV>.streamHeader(stream: Stream) {
        val isSelected = stream.key in props.selectedStreams
        val recordings = stream.recList.recordings
        styledDiv {
            name = "streamHeader"
            attrs {
                onClickFunction = { props.toggleStream(stream.key) }
            }
            css {
                display = Display.flex
                flexDirection = FlexDirection.row
                useColorSet(Theme().subHeader)
                alignContent = Align.center
                padding(left = 1.rem, right = 6.px)
                color = Theme().subHeader.textColor
                flexWrap = FlexWrap.wrap
            }
            // show expanded state
            expandButton(isSelected)
            styledDiv {
                css {
                    cursor = Cursor.pointer
                    display = Display.flex
                    flex(1.0, 0.0, 0.px)
                }
                +stream.name
            }
            styledDiv {
                val hours = "${stream.metaData.totalDuration90k.toDuration.inWholeHours} hours"
                css {
                    display = Display.flex
                    flex(1.0, 0.0, 0.px)
                    padding(left = 0.5.rem, right = 0.5.rem)
                }
                +"$hours / ${stream.metaData.days.size} days"
            }
            styledDiv {
                css {
                    display = Display.flex
                    flex(1.0, 0.0, 0.px)
                    padding(left = 0.5.rem, right = 0.5.rem)
                    marginRight = LinearDimension.auto
                }
                +"${stream.metaData.fsBytes.asSize} / ${stream.metaData.retainBytes.asSize}"
            }
            styledImg {
                imageSrc("/images/camera.svg", 16.px)
                attrs {
                    if (stream.metaData.record)
                        onClickFunction = {
                            it.preventDefault()
                            it.stopPropagation()
                            props.showLive(stream.key)
                        }
                }
                css {
                    if (!stream.metaData.record)
                        opacity = 0.3
                    else
                        cursor = Cursor.pointer
                }
            }
        }
        styledDiv {
            shrinkable(isSelected, (recordings.size * 2.5).rem)
            css {
                display = Display.flex
                flexDirection = FlexDirection.column
                padding(left = 2.rem, right = 6.px)
                // hide the recording rows using maxHeight, with transition, so opening and closing is smooth.
            }

            recordings.sortedByDescending { it.endTime90k }.forEach { recording ->
                val resolution =
                    stream.recList.videoSampleEntries[recording.videoSampleEntryId]?.let {
                        "${it.width}x${it.height}"
                    } ?: ""
                styledButton {
                    tooltip = "$resolution ${recording.fps}fps ${recording.storage} ${recording.bitrate.asBitRate}"
                    attrs {
                        onClickFunction = { props.showRecording(RecordingSource(stream, recording, props.subTitle)) }
                    }
                    css {
                        useColorSet(Theme().content)
                        position = Position.relative    // required to make tooltip work.
                        display = Display.flex
                        flexDirection = FlexDirection.row
                        justifyContent = JustifyContent.spaceBetween
                        if (props.playingRecording == recording)
                            backgroundColor = Theme().subHeader.selectedBackgroundColor
                    }
                    with(recording) {
                        column(getStartDate(props.minStart), 0.30, JustifyContent.start)
                        column(
                            "${getStartTime(props.minStart)} - ${getEndTime(props.maxEnd)}",
                            0.40,
                            JustifyContent.center
                        )
                    }
                    styledA(href = stream.url(recording, false)) {
                        attrs["download"] = stream.filename(recording)
                        attrs {
                            onClickFunction = { it.stopPropagation() }
                        }
                        css {
                            display = Display.flex
                            width = LinearDimension.auto
                        }
                        styledImg {
                            imageSrc("/images/download.svg", 16.px)
                        }
                    }
                }
            }
        }
    }


    override fun RBuilder.render() {
        styledDiv {
            name = "CameraScroller"
            css {
                backgroundColor = Theme().content.backgroundColor
                overflowY = Overflow.scroll
                display = Display.flex
                flexDirection = FlexDirection.column
                width = 100.pct
                padding(0.5.rem)
            }
            props.cameras.toList().sortedBy { it.first.shortName }.forEach { camera ->
                cameraHeader(camera.first)
                camera.second.sortedBy { it.name }.forEach {
                    streamHeader(it)
                }
            }
        }
    }
}

external interface CameraListState : State

external interface CameraListProps : Props {
    var cameras: Map<Api.Camera, List<Stream>>
    var recLists: Map<String, RecList>
    var selectedStreams: Set<String>
    var minStart: Time90k
    var maxEnd: Time90k
    var playingRecording: RecList.Recording?
    var subTitle: Boolean

    // callbacks
    var toggleStream: (String) -> Unit
    var showRecording: (RecordingSource) -> Unit
    var showLive: (String) -> Unit
}

