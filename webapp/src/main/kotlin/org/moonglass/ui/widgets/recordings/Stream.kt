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

import io.ktor.http.URLBuilder
import kotlinx.browser.window
import org.moonglass.ui.api.Api
import org.moonglass.ui.api.RecList
import org.moonglass.ui.api.apiConfig
import org.moonglass.ui.formatDate
import org.moonglass.ui.formatTime
import org.moonglass.ui.url

data class Stream(val name: String, val metaData: Api.StreamData, val camera: Api.Camera) {
    val key = "${camera.uuid}/${name}"
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Stream) return false

        if (key != other.key) return false

        return true
    }

    override fun hashCode(): Int {
        return key.hashCode()
    }

    fun filename(recording: RecList.Recording): String {
        return camera.shortName + "-" + name + "-" +
            recording.startTime90k.formatDate + "-" +
            recording.startTime90k.formatTime + recording.endTime90k.formatTime + ".mp4"
    }

    fun url(recording: RecList.Recording, subTitle: Boolean): String {
        val params = mutableMapOf<String, String?>(
            "s" to "${recording.startId}-${recording.endId}@${recording.openId}"
        )
        if (subTitle)
            params["ts"] = null
        return "/api/cameras/$key/view.mp4".url(params)
    }

    val wsUrl = window.location.let {
        URLBuilder().apply {
            apiConfig("cameras", key, "live.m4s", websocket = true)
        }.build()
    }

    override fun toString(): String {
        return "${camera.shortName} ($name)"
    }
}

/**
 * Find a stream from an Api record.
 * @param key The stream key, in the form <cameraUuid>/<streamName>
 * @return The stream, or null if not found
 *
 */
fun Api.streamFor(key: String): Stream? {
    val pieces = key.split('/')
    if (pieces.size != 2)
        return null
    return cameras.firstOrNull { it.uuid == pieces[0] }?.let { camera ->
        camera.streams[pieces[1]]?.let {
            Stream(pieces[1], it, camera)
        }
    }
}
