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

import org.moonglass.ui.api.RecList
import org.moonglass.ui.api.getEndTime
import org.moonglass.ui.api.getStartDate
import org.moonglass.ui.api.getStartTime
import org.moonglass.ui.widgets.recordings.Stream

class RecordingSource(stream: Stream, val recording: RecList.Recording) : VideoSource {

    override val caption = "$stream ${recording.getStartDate()} ${recording.getStartTime()}-${recording.getEndTime()}"

    override val srcUrl: String = stream.url(recording, false)
    val aspectRatio: Double = stream.recList.videoSampleEntries[recording.videoSampleEntryId]?.let {
        it.aspectWidth.toDouble() / it.aspectHeight.toDouble()
    } ?: 1.333

    override fun setAspectCallback(callback: (Double) -> Unit) {
        callback(aspectRatio)
    }
}
