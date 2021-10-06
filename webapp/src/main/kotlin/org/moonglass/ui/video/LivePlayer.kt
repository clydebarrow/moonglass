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

package org.moonglass.ui.video

import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.css.LinearDimension
import kotlinx.css.PointerEvents
import kotlinx.css.Position
import kotlinx.css.TextAlign
import kotlinx.css.backgroundColor
import kotlinx.css.color
import kotlinx.css.height
import kotlinx.css.left
import kotlinx.css.opacity
import kotlinx.css.padding
import kotlinx.css.pct
import kotlinx.css.pointerEvents
import kotlinx.css.position
import kotlinx.css.properties.transition
import kotlinx.css.px
import kotlinx.css.rem
import kotlinx.css.right
import kotlinx.css.textAlign
import kotlinx.css.top
import kotlinx.css.width
import kotlinx.css.zIndex
import kotlinx.html.js.onChangeFunction
import kotlinx.html.js.onMouseOverFunction
import kotlinx.html.js.onTouchStartFunction
import org.moonglass.ui.ResponsiveLayout
import org.moonglass.ui.Theme
import org.moonglass.ui.ZIndex
import org.moonglass.ui.applyState
import org.moonglass.ui.useColorSet
import org.moonglass.ui.utility.StateValue
import org.moonglass.ui.widgets.recordings.Stream
import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLSelectElement
import org.w3c.dom.HTMLVideoElement
import org.w3c.dom.mediasource.AppendMode
import org.w3c.dom.mediasource.MediaSource
import org.w3c.dom.mediasource.OPEN
import org.w3c.dom.mediasource.ReadyState
import org.w3c.dom.mediasource.SEGMENTS
import org.w3c.dom.mediasource.SourceBuffer
import org.w3c.dom.mediasource.get
import org.w3c.dom.url.URL
import react.Props
import react.RBuilder
import react.RComponent
import react.State
import react.createRef
import react.dom.attrs
import react.dom.onMouseLeave
import react.dom.option
import styled.css
import styled.styledDiv
import styled.styledSelect
import styled.styledVideo

external interface LivePlayerProps : Props {
    var height: LinearDimension
    var playerKey: String
    var showControls: Boolean
    var source: StateValue<String>
    var allStreams: Map<String, Stream>
    var overlay: Boolean                    // should the selector overlay the video?
}

external interface PlayerState : State {
    var isSelectorShowing: Boolean
    var wasTouched: Boolean         // set if we saw a touch event
}

class LivePlayer(props: LivePlayerProps) : RComponent<LivePlayerProps, PlayerState>(props) {

    private val bufferQueue = ArrayDeque<ByteArray>()
    private lateinit var contentType: String

    /**
     * Cache mediaSource for a given source.
     */
    private inner class SourceMemo {
        private var source: Stream? = null
        private var mediaSource: MediaSource? = null
        private var srcUrl: String = ""

        fun getSrcUrl(source: Stream): String {
            mediaSource?.let {
                if (source == this.source) return srcUrl
                it.onsourceopen = null
                it.onsourceended = null
                it.onsourceclose = null
            }
            close()
            this.source = source
            mediaSource = MediaSource().apply {
                // attach event listeners
                onsourceended = {
                }
                onsourceclose = {
                    close()
                }
                onsourceopen = {
                    open(it.currentTarget as MediaSource, source)
                }
                srcUrl = URL.createObjectURL(this)
            }
            return srcUrl
        }
    }

    /**
     * A hook for the video element.
     */

    private val videoRef = createRef<HTMLVideoElement>()
    private var sourceMemo: SourceMemo = SourceMemo()

    /**
     * The coroutine job listening for data flow
     */
    private var job: Job? = null

    private val scope = MainScope()

    private val SourceBuffer.timeRange: Pair<Double, Double>
        get() {
            val ranges = buffered
            val length = ranges.length
            val rangeEnd = if (length == 0) 0.0 else ranges.end(length - 1)
            val rangeStart = if (length == 0) 0.0 else ranges.start(0)
            return Pair(rangeStart, rangeEnd)
        }

    /**
     * If a sourceBuffer exceeds the given value, trim it to somewhat less
     */
    private fun SourceBuffer.trimTo(seconds: Int, hysteresis: Double = 0.5): Boolean {
        val range = timeRange
        if (range.second - range.first > seconds && !updating) {
            //console.log("time range ${range.first}-${range.second}, removing some")
            remove(range.first, range.second - (seconds * hysteresis))
            return true
        }
        return false
    }

    /**
     * Get a source buffer.
     */
    private fun getSourceBuffer(mediaSource: MediaSource): SourceBuffer {
        mediaSource.sourceBuffers[0]?.let { return it }
        return mediaSource.addSourceBuffer(contentType).apply {
            mode = AppendMode.SEGMENTS
            onupdate = {
                transfer(mediaSource)
            }
            onupdateend = {
                if (mediaSource.readyState == ReadyState.OPEN)      // in case it's being shut down.
                    trimTo(300)
            }
        }
    }

    private fun transfer(mediaSource: MediaSource) {
        while (bufferQueue.isNotEmpty()) {
            if (mediaSource.readyState != ReadyState.OPEN)
                return
            val srcBuffer = getSourceBuffer(mediaSource)
            if (srcBuffer.updating)
                return
            val range = srcBuffer.timeRange
            srcBuffer.timestampOffset = range.second
            mediaSource.setLiveSeekableRange(
                (range.second - LiveSource.MAX_TIME - 60).coerceAtLeast(range.first),
                range.second
            )
            srcBuffer.appendBuffer(bufferQueue.removeFirst())
            videoRef.current?.play()
        }
    }

    /**
     * Start the dataflow. If already flowing, or nothing to flow, do nothing.
     */
    private fun open(mediaSource: MediaSource, stream: Stream) {
        if (job == null)
            job = scope.launch {
                LiveSourceFactory.getSource(stream).dataFlow.collect { data ->
                    contentType = data.contentType
                    if (bufferQueue.size < MAX_BUFFER)
                        bufferQueue.add(data.data)
                    else
                        console.log("Dropped buffer")
                    transfer(mediaSource)
                }
            }
    }

    /**
     * Stop the dataflow, remove the sourceBuffer if added.
     */
    private fun close() {
        job?.cancel()
        job = null
    }

    override fun componentWillUnmount() {
        close()
    }

    override fun PlayerState.init(props: LivePlayerProps) {
        isSelectorShowing = !props.overlay
        wasTouched = false
    }

    private val selectorRef = createRef<HTMLElement>()

    override fun RBuilder.render() {
        val stream = props.allStreams[props.source.value]
        val videoHeight: LinearDimension
        val selectorPosition: Position
        val selectorOpacity: Double
        if (props.overlay) {
            videoHeight = props.height - 3.rem
            selectorPosition = Position.absolute
            selectorOpacity = if (state.isSelectorShowing) 1.0 else 0.0
        } else {
            videoHeight = props.height
            selectorPosition = Position.relative
            selectorOpacity = 1.0
        }

        styledDiv {
            css {
                width = 100.pct
                height = props.height
                position = Position.relative
            }
            attrs {
                onMouseOverFunction = { applyState { isSelectorShowing = true } }
                onMouseLeave = { applyState { isSelectorShowing = !props.overlay } }
                if (props.overlay)
                    onTouchStartFunction = {
                        console.log("touchstart")
                        applyState { wasTouched = true; isSelectorShowing = !isSelectorShowing }
                    }
            }
            styledSelect {
                ref = selectorRef
                css {
                    position = selectorPosition
                    opacity = selectorOpacity
                    width = 100.pct
                    height = 3.rem
                    left = 0.px
                    top = 0.px
                    right = 0.px
                    textAlign = TextAlign.center
                    backgroundColor = Theme().header.backgroundColor
                    color = Theme().header.textColor
                    padding(0.5.rem)
                    zIndex = ZIndex.Content.index + 2
                    transition("all", ResponsiveLayout.menuTransitionTime)
                    // disable hover effects on touch devices.
                    if (!state.wasTouched) {
                        hover {
                            opacity = 1.0
                        }
                    }
                }
                attrs {
                    value = props.source.value
                    onChangeFunction = {
                        val value = it.currentTarget.unsafeCast<HTMLSelectElement>().value
                        applyState {
                            props.source.value = value
                        }
                    }

                }
                option {
                    attrs {
                        value = ""
                    }
                    +"Select stream"
                }
                props.allStreams.forEach {
                    option {
                        attrs {
                            value = it.key
                        }
                        +it.value.toString()
                    }
                }
            }

            styledVideo {
                ref = videoRef
                css {
                    useColorSet(Theme().content)
                    width = 100.pct
                    height = videoHeight
                    position = Position.relative
                    zIndex = ZIndex.Content.index
                    pointerEvents = PointerEvents.none
                }
                attrs {
                    set("muted", true)
                    key = props.playerKey
                    autoPlay = true
                    autoBuffer = true
                    controls = props.showControls
                    poster = "/images/placeholder.jpg"
                    stream?.let {
                        src = sourceMemo.getSrcUrl(it)
                    }
                }
            }
        }
    }

    companion object {
        const val MAX_BUFFER = 10
    }
}
