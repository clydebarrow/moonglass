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

package org.moonglass.ui.video

import org.moonglass.ui.api.Api
import org.moonglass.ui.api.RecList
import org.moonglass.ui.api.start
import org.moonglass.ui.plusSeconds
import org.moonglass.ui.widgets.recordings.Stream
import kotlin.js.Date

class RecordingBuffer : StreamSource {

    var endTime = 0
        set(value) {
            if (field != value) {
                field = value
                recList = null
            }
        }
    var startTime = 0
        set(value) {
            if (field != value) {
                field = value
                recList = null
            }
        }
    var startDate = Date()
        set(value) {
            if (field != value) {
                field = value
                recList = null
            }
        }

    var offset: Int = 0     // an offset for start time, to cater for
    val offsetStartTime get() = startTime + offset

    val startDateTime: Date get() = startDate.plusSeconds(offsetStartTime)

    // duration in seconds, must allow for wrap over end of day.
    val durationSecs: Int
        get() = (endTime - startTime).let {
            if (it < 0)
                it + 24 * 60 * 60
            else
                it
        }

    private var source: Stream = Stream("", Api.StreamData(), Api.Camera())

    private var recList: RecList? = null

    override suspend fun getSrcUrl(source: Stream?): String? {
        if (source == null) return null
        if (this.source != source) {
            this.source = source
            recList = null
        }
        val recs =
            recList ?: (Api.fetchRecording(source, startDate, startTime, endTime) ?: RecList()).also { recList = it }
        console.log("Fetched recordings: $recList")
        if (recs.recordings.isEmpty())
            return null
        val firstRec = recs.recordings.first()
        val maxLen = (recs.recordings.last().endTime90k - firstRec.startTime90k)/ 90000
        val startOffset = ((startDateTime.getTime() - firstRec.start.getTime()) / 1000.0).toInt()
            .coerceAtLeast(0)     // base time in seconds
        val endOffset = (startOffset + durationSecs).coerceAtMost(maxLen.toInt())
            return Api.recordingUrl(
                source.key,
                firstRec.startId,
                recs.recordings.last().endId,
                firstRec.openId,
                startOffset = startOffset,
                endOffset = endOffset
            )
    }

    override fun close() {
    }
}
