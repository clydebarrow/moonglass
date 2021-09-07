package org.moonglass.ui.widgets.recordings

import io.ktor.http.URLBuilder
import kotlinx.browser.window
import org.moonglass.ui.api.Api
import org.moonglass.ui.api.RecList
import org.moonglass.ui.api.setDefaults
import org.moonglass.ui.formatDate
import org.moonglass.ui.formatTime
import org.moonglass.ui.url

data class Stream(val name: String, val metaData: Api.ApiStream, val camera: Api.Camera) {
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

    fun url(recording: RecList.Recording, caption: Boolean): String {
        val params = mutableMapOf<String, String?>(
            "s" to "${recording.startId}-${recording.endId}@${recording.openId}"
        )
        if (caption)
            params["ts"] = null
        return "/api/cameras/${camera.uuid}/${name}/view.mp4".url(params)
    }

    val wsUrl = window.location.let {
        URLBuilder().apply {
            setDefaults("cameras", key, "live.m4s", websocket = true)
        }.build()
    }

    override fun toString(): String {
        return "${camera.shortName} ($name)"
    }
}
