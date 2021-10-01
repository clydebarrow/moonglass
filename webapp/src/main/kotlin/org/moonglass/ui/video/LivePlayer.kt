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
import kotlinx.css.backgroundColor
import kotlinx.css.height
import kotlinx.css.pct
import kotlinx.css.width
import org.moonglass.ui.Debug
import org.moonglass.ui.Theme
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
import react.createRef
import react.dom.attrs
import styled.css
import styled.styledVideo

external interface LivePlayerProps : Props {
    var height: LinearDimension?
    var playerKey: String
    var showControls: Boolean
    var source: LiveSource?
}

class LivePlayer(props: LivePlayerProps) : RComponent<LivePlayerProps, PlayerState>(props) {

    private val bufferQueue = ArrayDeque<ByteArray>()
    private lateinit var contentType: String

    /**
     * Cache mediaSource for a given source.
     */
    private inner class SourceMemo {
        private var key: String? = null
        private var mediaSource: MediaSource? = null
        private var srcUrl: String = ""

        fun getSrcUrl(key: String): String {
            mediaSource?.let {
                if (key == this.key) return srcUrl
                it.onsourceopen = null
                it.onsourceended = null
                it.onsourceclose = null
            }
            close()
            this.key = key
            mediaSource = MediaSource().apply {
                // attach event listeners
                onsourceended = {
                }
                onsourceclose = {
                    close()
                }
                onsourceopen = {
                    open(it.currentTarget as MediaSource)
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
    private fun open(mediaSource: MediaSource) {
        if (job == null)
            props.source?.let { source ->
                job = scope.launch {
                    source.dataFlow.collect { data ->
                        contentType = data.contentType
                        if (bufferQueue.size < MAX_BUFFER)
                            bufferQueue.add(data.data)
                        else
                            console.log("Dropped buffer")
                        transfer(mediaSource)
                    }
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


    override fun RBuilder.render() {
        styledVideo {
            ref = videoRef
            css {
                backgroundColor = Theme().content.backgroundColor
                width = 100.pct
                height = props.height ?: 100.pct
            }
            attrs {
                set("muted", true)
                key = props.playerKey
                autoPlay = true
                autoBuffer = true
                controls = props.showControls
                poster = "/images/placeholder.jpg"
                src = props.source?.let {
                    sourceMemo.getSrcUrl(it.caption)
                } ?: ""
            }
        }
    }

    companion object {
        const val MAX_BUFFER = 10
    }
}
