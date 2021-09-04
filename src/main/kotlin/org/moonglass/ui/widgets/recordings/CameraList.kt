package org.moonglass.ui.widgets.recordings

import kotlinx.css.Color
import kotlinx.css.Cursor
import kotlinx.css.Display
import kotlinx.css.FlexDirection
import kotlinx.css.FlexWrap
import kotlinx.css.FontWeight.Companion.bolder
import kotlinx.css.JustifyContent
import kotlinx.css.LinearDimension
import kotlinx.css.Overflow
import kotlinx.css.TextAlign
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
import kotlinx.css.maxHeight
import kotlinx.css.opacity
import kotlinx.css.overflow
import kotlinx.css.padding
import kotlinx.css.px
import kotlinx.css.rem
import kotlinx.css.textAlign
import kotlinx.css.vh
import kotlinx.css.width
import kotlinx.html.DIV
import kotlinx.html.js.onClickFunction
import org.moonglass.ui.ResponsiveLayout
import org.moonglass.ui.Time90k
import org.moonglass.ui.api.Api
import org.moonglass.ui.api.RecList
import org.moonglass.ui.api.bitrate
import org.moonglass.ui.api.fps
import org.moonglass.ui.api.getEndTime
import org.moonglass.ui.api.getStartDate
import org.moonglass.ui.api.getStartTime
import org.moonglass.ui.api.storage
import org.moonglass.ui.cardStyle
import org.moonglass.ui.name
import org.moonglass.ui.style.column
import org.moonglass.ui.style.expandButton
import org.moonglass.ui.style.shrinkable
import org.moonglass.ui.toDuration
import org.moonglass.ui.video.LiveSource
import org.moonglass.ui.video.RecordingSource
import org.moonglass.ui.video.VideoSource
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
import kotlin.math.roundToInt


external interface CameraListState : State

external interface CameraListProps : Props {
    var cameras: Map<Api.Camera, List<Stream>>
    var selectedStreams: Set<String>
    var minStart: Time90k
    var maxEnd: Time90k
    var playingRecording: RecList.Recording?

    // callbacks
    var toggleStream: (String) -> Unit
    var showVideo: (VideoSource) -> Unit
}

class CameraList(props: CameraListProps) : RComponent<CameraListProps, CameraListState>(props) {


    private fun StyledDOMBuilder<DIV>.cameraHeader(camera: Api.Camera) {
        name = "cameraHeader"
        styledDiv {
            css {
                flexGrow = 1.0
                display = Display.flex
                flexDirection = FlexDirection.row
                backgroundColor = Color.lightBlue
            }
            styledDiv {
                css {
                    display = Display.flex
                    flexGrow = 0.3
                    color = Color.black
                    fontWeight = bolder
                    padding(left = 1.rem, right = 0.5.rem)
                }
                +camera.shortName
            }
            styledDiv {
                css {
                    display = Display.flex
                    flexGrow = 0.7
                    color = Color.black
                    textAlign = TextAlign.right
                    justifyContent = JustifyContent.center
                    padding(left = 1.rem, right = 0.5.rem)
                }
                +camera.description
            }
        }
    }

    private fun StyledDOMBuilder<DIV>.streamHeader(stream: Stream) {
        name = "streamHeader"
        val isSelected = stream.key in props.selectedStreams
        val recordings = stream.recList.recordings
        styledDiv {
            attrs {
                onClickFunction = { props.toggleStream(stream.key) }
            }
            css {
                flexGrow = 1.0
                display = Display.flex
                flexDirection = FlexDirection.row
                backgroundColor = Color.lightBlue.lighten(20)
                padding(left = 1.rem, right = 6.px)
                color = Color.black
                flexWrap = FlexWrap.wrap
            }
            // show expanded state
            expandButton(isSelected)
            styledDiv {
                css {
                    display = Display.flex
                    flex(1.0, 0.0, 0.px)
                }
                +stream.name
            }
            styledDiv {
                css {
                    display = Display.flex
                    flex(1.0, 0.0, 0.px)
                    padding(left = 0.5.rem, right = 0.5.rem)
                }
                +"${stream.metaData.days.size} days"
            }
            styledDiv {
                css {
                    flex(1.0, 0.0, 0.px)
                    padding(left = 0.5.rem, right = 0.5.rem)
                }
                +"${stream.metaData.totalDuration90k.toDuration.inWholeHours} hours"
            }
            styledImg(src = "/images/liveView.svg") {
                attrs {
                    if (stream.metaData.record)
                        onClickFunction = {
                            it.preventDefault()
                            it.stopPropagation()
                            props.showVideo(
                                LiveSource(
                                    stream.wsUrl,
                                    "Live: ${stream.camera.shortName} (${stream.name}"
                                )
                            )
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
            shrinkable(isSelected, (recordings.size * 2).rem)
            css {
                display = Display.flex
                flexGrow = 1.0
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
                    attrs {
                        onClickFunction = { props.showVideo(RecordingSource(stream, recording)) }
                    }
                    css {
                        display = Display.flex
                        flexDirection = FlexDirection.row
                        if (props.playingRecording == recording)
                            backgroundColor = Color.lightSalmon
                    }
                    with(recording) {
                        column(getStartDate(props.minStart), 0.20, JustifyContent.start)
                        column("${getStartTime(props.minStart)} - ${getEndTime(props.maxEnd)}", 0.30)
                        column(resolution, 0.125)
                        column("${fps}fps", 0.125)
                        column(storage, 0.125)
                        val bitrate = bitrate
                        val bitwhole = bitrate.toInt()
                        val bitfrac = ((bitrate - bitwhole.toDouble()) * 10).roundToInt()
                        column("$bitwhole.$bitfrac Mbps", 0.125)
                    }
                    styledA(href = stream.url(recording, false)) {
                        attrs {
                            onClickFunction = { it.stopPropagation() }
                        }
                        css {
                            display = Display.flex
                            width = LinearDimension.auto
                        }
                        styledImg(src = "/images/download.svg") { }
                    }
                }
            }
        }
    }


    override fun RBuilder.render() {
        styledDiv {
            name = "CameraScroller"
            css {
                if (ResponsiveLayout.isPortrait)
                    maxHeight = 40.vh
                else
                    maxHeight = 80.vh
                overflow = Overflow.auto
                display = Display.flex
                flexDirection = FlexDirection.column
                flexGrow = 1.0
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
