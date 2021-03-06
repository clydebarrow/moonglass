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
import io.ktor.client.features.websocket.DefaultClientWebSocketSession
import io.ktor.client.features.websocket.WebSockets
import io.ktor.client.features.websocket.webSocket
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.get
import io.ktor.client.request.url
import io.ktor.client.statement.HttpResponse
import io.ktor.utils.io.core.readBytes
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onSubscription
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.moonglass.ui.Duration90k
import org.moonglass.ui.Time90k
import org.moonglass.ui.api.apiConfig
import org.moonglass.ui.widgets.Toast
import org.moonglass.ui.widgets.recordings.Stream
import org.w3c.dom.mediasource.MediaSource
import kotlin.collections.set
import kotlin.js.Date


/**
 * A class that receives data from a websocket and streams via a Kotlin flow to be consumed by one ore more
 * mediasource objects.
 *
 * @param stream The stream to use
 *
 */
class LiveSource(private val stream: Stream) : VideoSource {

    override val caption: String = stream.toString()

    /**
     * The coroutine scope used to run tasks.
     */
    private val scope = MainScope()

    /**
     * Cache the initialization segment
     */

    private lateinit var initSegment: Data

    /**
     * the content-type can be cached for each stream.
     */

    private var contentType: String = ""

    /**
     * The class used to deliver data
     */
    open class Data(val contentType: String, val data: ByteArray)

    private val flow = MutableSharedFlow<Data>(0, MAX_BUFFER, BufferOverflow.DROP_LATEST)

    /**
     * Publicly accessible flow.
     */

    val dataFlow: SharedFlow<Data>
        get() = flow.onSubscription {
            if (::initSegment.isInitialized)
                emit(initSegment)
        }

    /**
     * The client we will use for the websocket and other network fetches
     */

    private val client = HttpClient(Js) {
        install(WebSockets) { }
        install(HttpTimeout)
    }

    private suspend fun readSocket(block: suspend DefaultClientWebSocketSession.() -> Unit) {
        client.webSocket({
            url {
                url(stream.wsUrl)
            }
        }, block)
    }

    /**
     * Fetch the initialization segment for the stream. Do this by opening the websocket and reading one packet
     * just to get the sampleId.
     */

    private suspend fun getInitializationSegment(): Data {
        if (!::initSegment.isInitialized) {
            readSocket {
                val message = incoming.receive()
                val buffer = ByteBuffer(message.data)
                val data = buffer.parseBuffer()
                contentType = data.contentType
                if (!MediaSource.isTypeSupported(contentType)) {
                    Toast.toast("Media type $contentType not supported")
                    throw IllegalArgumentException("Media type not supported")
                }
                this.cancel()
                // got the header, now request the init segment
                val url = HttpRequestBuilder().apply {
                    timeout { requestTimeoutMillis = 10000 }
                    url {
                        apiConfig("init", "${data.sampleId}.mp4")
                    }
                }
                initSegment =
                    Data(
                        contentType,
                        client.get<HttpResponse>(url).content.readRemaining(100000, 0).readBytes()
                    )
            }
        }
        return initSegment
    }


    private var job: Job? = null

    /**
     * open the websocket and start streaming data
     */
    private fun open() {
        if (job == null)
            job = scope.launch {
                flow.emit(getInitializationSegment())
                readSocket {
                    try {
                        while (isActive) {
                            val message = incoming.receive()
                            val data = message.data.stripHeaders()
                            flow.emit(Data(contentType, data))
                            updateStats(data.size)
                        }
                    } catch (ex: Exception) {
                        console.log("$caption: $ex")
                    }
                }
            }
    }

    /**
     * End the streaming.
     */
    private fun close() {
        try {
            job?.cancel()
            job = null
        } catch (ex: Exception) {
            console.log("$caption: Exception in close: ${ex.message}")
        }
    }

    private class ByteBuffer(val data: ByteArray) {
        var offset = 0
        val size = data.size
        val balance get() = data.drop(offset).toByteArray()

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

    var clientCount: Int = 0
        private set

    /**
     * Accumulate statistics
     *
     */
    var totalBytes: Long = 0L    // total number of bytes streamed
        private set
    var rate: Double = 0.0       // Latest available data rate in bytes/sec
        private set
    private var lastTime: Double = 0.0     // the last time we saw data

    /**
     * Update the stats
     */
    private fun updateStats(size: Int) {
        totalBytes += size
        val now = Date().getTime()
        if (lastTime != 0.0 && now != lastTime) {
            val thisRate = size.toDouble() / (now - lastTime) * 1000.0
            if (rate == 0.0)
                rate = thisRate
            else {
                rate = rate * (1 - SMOOTHING) + thisRate * SMOOTHING
            }
        }
        lastTime = now
    }

    init {
        // listen for changes in subscriber count, open/close the websocket as required.
        flow.subscriptionCount.onEach {
            if (it > clientCount)
                close()     // force restart of stream
            if (it == 0)
                close()
            else
                open()
            clientCount = it
            LiveSourceFactory.updateFlow()
        }.launchIn(scope)
    }

    private class DataHeaders(
        contentType: String,
        val openId: Int,
        val recordingId: Int,
        val recordingStart: Time90k,
        val prevDuration: Duration90k,
        val rangeStart: Duration90k,
        val rangeEnd: Duration90k,
        val sampleId: Int,
        data: ByteArray
    ) : Data(contentType, data) {
        override fun toString(): String {
            return "DataHeaders(contentType='$contentType', openId=$openId, recordingId=$recordingId, recordingStart=$recordingStart, prevDuration=$prevDuration, rangeStart=$rangeStart, rangeEnd=$rangeEnd, sampleId=$sampleId)"
        }
    }

    /**
     * Parse a buffer retrieving headers.
     */
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

    /**
     * Strip headers from a data array
     */
    private fun ByteArray.stripHeaders(): ByteArray {
        var pos = 0
        while (pos < size - 4) {
            if (this[pos] == 0xA.toByte() && this[pos + 2] == 0xA.toByte()) {
                return sliceArray(pos + 3 until size)
            }
            pos++
        }
        return byteArrayOf()
    }

    companion object {
        const val MAX_BUFFER = 5
        const val MAX_TIME = 6 * 60       // don't buffer more than 5 minutes
        const val SMOOTHING = 0.1           // use this factor for smoothing data rates
    }
}
