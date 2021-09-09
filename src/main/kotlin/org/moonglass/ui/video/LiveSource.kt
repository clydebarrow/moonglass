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

import io.ktor.client.HttpClient
import io.ktor.client.engine.js.Js
import io.ktor.client.features.HttpTimeout
import io.ktor.client.features.timeout
import io.ktor.client.features.websocket.WebSockets
import io.ktor.client.features.websocket.webSocket
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.get
import io.ktor.client.request.url
import io.ktor.client.statement.HttpResponse
import io.ktor.http.Url
import io.ktor.http.cio.websocket.Frame
import io.ktor.http.cio.websocket.WebSocketSession
import io.ktor.http.cio.websocket.close
import io.ktor.utils.io.core.readBytes
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.khronos.webgl.Uint8Array
import org.moonglass.ui.Duration90k
import org.moonglass.ui.Time90k
import org.moonglass.ui.api.apiConfig
import org.moonglass.ui.widgets.Toast
import org.w3c.dom.mediasource.AppendMode
import org.w3c.dom.mediasource.MediaSource
import org.w3c.dom.mediasource.OPEN
import org.w3c.dom.mediasource.ReadyState
import org.w3c.dom.mediasource.SEGMENTS
import org.w3c.dom.mediasource.SourceBuffer
import org.w3c.dom.url.URL
import kotlin.collections.set


/**
 * A class that receives data from a websocket and streams it into a mediasource.
 * @param wsUrl The websocket url
 * @param caption The video title
 *
 */
class LiveSource(private val wsUrl: Url, override val caption: String) : VideoSource {

    /**
     * The mediasource that buffers the stream
     */
    private val mediaSource: MediaSource = MediaSource().apply {
        // attach event listeners
        onsourceended = {
            console.log("MediaSource ended: ${it.type}")
        }
        onsourceclose = {
            console.log("MediaSource closed: ${it.type}")
        }
        onsourceopen = {
            console.log("MediaSource opened")
            start()
        }
    }

    /**
     * A source url suitable for assignment to a video element's src property
     */
    override val srcUrl: String = URL.createObjectURL(mediaSource)

    private lateinit var srcBuffer: SourceBuffer

    /**
     * The coroutine scope used to run tasks.
     */
    private val scope = MainScope()


    /**
     * A buffer queue to be sent to the media source
     */

    private val bufferQueue = ArrayDeque<DataHeaders>()

    /**
     * The client we will use for the websocket and other network fetches
     */

    private val client = HttpClient(Js) {
        install(WebSockets) { }
        install(HttpTimeout)
    }

    /**
     * Fetch the initialization segment for a stream
     */

    private suspend fun getInitializationSegment(id: Int): ByteArray {
        val url = HttpRequestBuilder().apply {
            timeout { requestTimeoutMillis = 10000 }
            url {
                apiConfig("init", "$id.mp4")
            }
        }
        return client.get<HttpResponse>(url).content.readRemaining(100000, 0).readBytes()
    }

    private val SourceBuffer.timeRange: Pair<Double, Double>
        get() {
            val ranges = buffered
            val length = ranges.length
            val rangeEnd = if (length == 0) 0.0 else ranges.end(length - 1)
            val rangeStart = if (length == 0) 0.0 else ranges.start(0)
            return Pair(rangeStart, rangeEnd)
        }

    /**
     * If a sourcebuffer exceeds the given value, trim it to somewhat less
     */
    private fun SourceBuffer.trimTo(seconds: Int, hysteresis: Double = 0.5): Boolean {
        val range = timeRange
        if (range.second - range.first > seconds) {
            console.log("time range ${range.first}-${range.second}, removing some")
            remove(range.first, range.second - (seconds * hysteresis))
            return true
        }
        return false
    }

    // called to transfer data into the SourceBuffer
    private fun transfer() {
        if (!srcBuffer.updating) {
            if (!srcBuffer.trimTo(MAX_TIME))
                bufferQueue.removeFirstOrNull()?.let { data ->
                    val range = srcBuffer.timeRange
                    srcBuffer.timestampOffset = range.second
                    mediaSource.setLiveSeekableRange(
                        (range.second - MAX_TIME - 60).coerceAtLeast(range.first),
                        range.second
                    )
                    srcBuffer.appendBuffer(data.data)
                }
        }
    }

    private suspend fun WebSocketSession.setup() {
        val message = incoming.receive() as Frame.Binary
        val buffer = ByteBuffer(message.data)
        val data = buffer.parseBuffer()
        if (!MediaSource.isTypeSupported(data.contentType)) {
            Toast.toast("Media type ${data.contentType} not supported")
            throw IllegalArgumentException("Media type not supported")
        }
        if (!::srcBuffer.isInitialized) {
            srcBuffer = mediaSource.addSourceBuffer(data.contentType)
            srcBuffer.mode = AppendMode.SEGMENTS
            srcBuffer.onerror = {
                console.log("Mediasource error: ${it.type}")
            }
            srcBuffer.onabort = {
                console.log("Mediasource abort: ${it.type}")
            }
            srcBuffer.onupdateend = {
                transfer()
            }
        }
        bufferQueue.add(data)
        srcBuffer.appendBuffer(getInitializationSegment(data.sampleId))     // starts the transfer callbacks
    }

    private var job: Job? = null
    /**
     * open the websocket and start streaming data
     */
    private fun start() {
        job = scope.launch {
            client.webSocket({
                url {
                    url(wsUrl)
                }
            }) {
                try {
                    setup()
                    while (isActive && mediaSource.readyState == ReadyState.OPEN) {
                        val message = incoming.receive() as Frame.Binary
                        val data = ByteBuffer(message.data).parseBuffer()
                        if (bufferQueue.size < MAX_BUFFER) {
                            bufferQueue.add(data)
                            transfer()
                        } else
                            console.log("Discarded buffer: Size =${bufferQueue.size}, timeRange=${srcBuffer.timeRange} ")
                    }
                } catch (ex: Exception) {
                    console.log("$ex")
                } finally {
                    console.log("Ended websocket for $srcUrl, mediaSource.state = ${mediaSource.readyState}")
                    bufferQueue.clear()
                    srcBuffer.trimTo(0)
                }
            }
        }
    }


    override fun pause() {
        job?.cancel()
    }

    override fun play() {
        if(job?.isActive != true)
            start()
    }

    /**
     * End the streaming.
     */
    override fun close() {
        try {
            console.log("Closing LiveSource, mediaState = ${mediaSource.readyState}")
            if (mediaSource.readyState == ReadyState.OPEN) {
                mediaSource.removeSourceBuffer(srcBuffer)
                mediaSource.endOfStream()
            }
            job?.cancel()
            scope.cancel()
        } catch (ex: Exception) {
            console.log("Exception in close: ${ex.message}")
        }
    }

    private class ByteBuffer(val data: ByteArray) {
        var offset = 0
        val size = data.size
        val balance get() = Uint8Array(data.drop(offset).toTypedArray())

        fun indexOf(c: Char): Int {
            var i = offset
            while (i != size && data[i].toInt() != c.code)
                i++
            if (i == size)
                return -1
            return i - offset
        }

        fun getLine(): String {
            val index = indexOf('\n')
            if (index < 1 || data[offset + index - 1].toInt() != '\r'.code)
                throw IllegalArgumentException("No well-formed line found")
            val line = data.drop(offset).take(index - 1)
            offset += index + 1
            return line.map { it.toInt().toChar() }.toCharArray().concatToString()
        }

        fun getHeaders(): Map<String, String> {
            val map = mutableMapOf<String, String>()
            while (true) {
                val line = getLine()
                if (line.isEmpty()) {
                    return map
                }
                val parts = line.split(':').map { it.trim() }
                if (parts.size == 2)
                    map[parts[0]] = parts[1]
            }
        }
    }

    private data class DataHeaders(
        val contentType: String,
        val openId: Int,
        val recordingId: Int,
        val recordingStart: Time90k,
        val prevDuration: Duration90k,
        val rangeStart: Duration90k,
        val rangeEnd: Duration90k,
        val sampleId: Int,
        val data: Uint8Array
    ) {
        override fun toString(): String {
            return "DataHeaders(contentType='$contentType', openId=$openId, recordingId=$recordingId, recordingStart=$recordingStart, prevDuration=$prevDuration, rangeStart=$rangeStart, rangeEnd=$rangeEnd, sampleId=$sampleId)"
        }
    }

    private fun ByteBuffer.parseBuffer(): DataHeaders {
        val headers = getHeaders()
        val ids = headers["X-Recording-Id"]?.split('.')?.map { it.toIntOrNull() }
        val range = headers["X-Media-Time-Range"]?.split('-')?.map { it.toLongOrNull() }
        return DataHeaders(
            headers["Content-Type"] ?: "",
            ids?.getOrNull(0) ?: -1,
            ids?.getOrNull(1) ?: -1,
            headers["X-Recording-Start"]?.toLongOrNull() ?: 0,
            headers["X-Prev-Media-Duration"]?.toLongOrNull() ?: 0,
            range?.getOrNull(0) ?: -1,
            range?.getOrNull(1) ?: -1,
            headers["X-Video-Sample-Entry-Id"]?.toIntOrNull() ?: -1,
            balance
        )
    }

    companion object {
        const val MAX_BUFFER = 5
        const val MAX_TIME = 6 * 60       // don't buffer more than 5 minutes
    }
}
