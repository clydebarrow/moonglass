package org.moonglass.ui.api

import kotlinx.css.properties.Time
import kotlinx.serialization.Serializable
import org.moonglass.ui.Duration90k
import org.moonglass.ui.Time90k
import org.moonglass.ui.as90k
import org.moonglass.ui.fetch
import org.moonglass.ui.formatDate
import org.moonglass.ui.formatTime
import org.moonglass.ui.toDuration
import org.moonglass.ui.widgets.recordings.Stream
import kotlin.js.Date

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
        // /api/cameras/7f2e2a50-1e68-4647-817b-03089ca2003e/sub/recordings?startTime90k=146706552000000&endTime90k=146714328000000&split90k=324000000
        suspend fun fetchRecording(
            stream: Stream,
            startTime: Date,
            endTime: Date,
            maxDuration: Duration90k
        ): RecList {
            return "/api/cameras/${stream.camera.uuid}/${stream.name}/recordings".fetch(
                mapOf(
                    "startTime90k" to startTime.as90k,
                    "endTime90k" to endTime.as90k,
                    "split90k" to maxDuration
                )
            )
        }
    }
}

val Long.asSize: String
    get() {
        val kb = this / 1024
        if (kb == 0L)
            return "$this bytes"
        val mb = kb / 1024
        if (mb == 0L)
            return "$kb kB"
        val tb = mb / 1024
        if (tb == 0L)
            return "$mb MiB"
        return "$tb TiB"
    }


val RecList.Recording.duration get() = (endTime90k - startTime90k).toDuration
val RecList.Recording.fps get() = videoSamples / duration.inWholeSeconds.coerceAtLeast(1)
val RecList.Recording.storage get() = sampleFileBytes.asSize
val RecList.Recording.bitrate get() = sampleFileBytes * 8.0 / 1024 / 1024 / duration.inWholeSeconds.coerceAtLeast(1)
fun RecList.Recording.getStartDate(min: Time90k = startTime90k) = startTime90k.coerceAtLeast(min).formatDate
fun RecList.Recording.getStartTime(min: Time90k = startTime90k) = startTime90k.coerceAtLeast(min).formatTime
fun RecList.Recording.getEndTime(max: Time90k = endTime90k) = endTime90k.coerceAtMost(max).formatTime
