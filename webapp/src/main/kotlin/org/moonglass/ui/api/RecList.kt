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

package org.moonglass.ui.api

import kotlinx.serialization.Serializable
import org.moonglass.ui.Time90k
import org.moonglass.ui.asSize
import org.moonglass.ui.formatDate
import org.moonglass.ui.formatTime
import org.moonglass.ui.toDuration
import kotlin.math.pow

@Serializable
data class RecList(
    val recordings: List<Recording> = listOf(),
    val videoSampleEntries: Map<Int, SampleEntry> = mapOf()
) {
    @Serializable
    data class Recording(
        val startId: Int, // 5706
        val endId: Int = startId, // 5765
        val startTime90k: Time90k, // 146706547571287
        val endTime90k: Time90k, // 146706871528240
        val firstUncommitted: Int = 0, // 6700
        val growing: Boolean = false, // true
        val openId: Int, // 180
        val sampleFileBytes: Long, // 4131765
        val videoSampleEntryId: Int, // 1
        val videoSamples: Int // 54000
    )

    @Serializable
    data class SampleEntry(
        val aspectHeight: Int, // 95
        val aspectWidth: Int, // 168
        val height: Int, // 480
        val paspHSpacing: Int, // 126
        val paspVSpacing: Int, // 95
        val width: Int // 640
    )

    companion object {
    }
}



val RecList.Recording.duration get() = (endTime90k - startTime90k).toDuration
val RecList.Recording.fps get() = videoSamples / duration.inWholeSeconds.coerceAtLeast(1)
val RecList.Recording.storage get() = sampleFileBytes.asSize
val RecList.Recording.bitrate get() = sampleFileBytes * 8.0 / 1024 / 1024 / duration.inWholeSeconds.coerceAtLeast(1)
fun RecList.Recording.getStartDate(min: Time90k = startTime90k) = startTime90k.coerceAtLeast(min).formatDate
fun RecList.Recording.getStartTime(min: Time90k = startTime90k) = startTime90k.coerceAtLeast(min).formatTime
fun RecList.Recording.getEndTime(max: Time90k = endTime90k) = endTime90k.coerceAtMost(max).formatTime
