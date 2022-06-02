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

package org.moonglass.ui.api

import com.soywiz.krypto.md5
import io.ktor.client.HttpClient
import io.ktor.client.features.ClientRequestException
import io.ktor.client.features.HttpTimeout
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.features.json.serializer.KotlinxSerializer
import  kotlinx.serialization.json.Json
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.URLBuilder
import io.ktor.http.URLProtocol
import io.ktor.http.contentType
import io.ktor.utils.io.core.toByteArray
import kotlinx.browser.window
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.moonglass.ui.App
import org.moonglass.ui.Duration90k
import org.moonglass.ui.Time90k
import org.moonglass.ui.as90k
import org.moonglass.ui.plusSeconds
import org.moonglass.ui.url
import org.moonglass.ui.user.User
import org.moonglass.ui.widgets.Toast
import org.moonglass.ui.widgets.recordings.Stream
import kotlin.js.Date
import kotlin.time.Duration

@Serializable
data class Api(
    val cameras: List<Camera> = listOf(),
    val session: Session? = null,
    @SerialName("user")
    val userData: UserData? = null,
    val signals: List<Signal> = listOf(),
    val signalTypes: List<SignalType> = listOf(),
    val timeZoneName: String = "",
    val serverVersion: String = ""

) {
    @Serializable
    data class Camera(
        val id: Int = 0,
        val description: String = "", // Hikvision Driveway Camera
        val shortName: String = "", // Driveway
        val streams: Map<String, StreamData> = mapOf(),
        val uuid: String = "" // 7f2e2a50-1e68-4647-817b-03089ca2003e
    )

    @Serializable
    data class Day(
        val endTime90k: Time90k, // 146683224000000
        val startTime90k: Time90k, // 146675448000000
        val totalDuration90k: Duration90k = 0,
        val states: List<Duration90k> = listOf()
    )

    @Serializable
    data class StreamData(
        val id: Int = 0,
        val days: Map<String, Day> = mapOf(),
        val fsBytes: Long = 0, // 38785380352
        val maxEndTime90k: Long? = null, // 146704410690765
        val minStartTime90k: Long? = null, // 146678775676182
        val record: Boolean = false, // true
        val retainBytes: Long = 0, // 107374182400
        val totalDuration90k: Long = 0, // 24637266927
        val totalSampleFileBytes: Long = 0 // 38776002681
    )


    @Serializable
    data class Session(
        val username: String? = null,
        val csrf: String
    )

    @Serializable
    data class UserData(
        val id: Int, // 1
        val name: String, // user@example.com
        val preferences: Map<String, String> = mapOf(),
        val session: Session
    )

    companion object {

        val client = HttpClient {
            install(HttpTimeout) {
                requestTimeoutMillis = 10000
            }
            install(JsonFeature) {
                serializer = KotlinxSerializer(Json {
                    ignoreUnknownKeys = true        // allows for API changes
                })
            }
        }

        /**
         * Helper function to make an api call with timeout and exception trapping.
         * If the return type is nullable, a failure will return null, otherwise the exception will be rethrown
         */
        private inline fun <reified T : Any> apiCall(rethrow: Boolean = false, block: HttpClient.() -> T): T? {
            console.log("In apiCall")
            App.setRefresh(T::class.simpleName.toString(), true)
            return try {
                client.run {
                    block()
                }
            } catch (ex: Exception) {
                if (ex !is CancellationException) {
                    if (rethrow)
                        throw(ex)
                    Toast.toast("${ex.message}")
                } else
                    console.log(ex.toString())
                null
            } finally {
                App.setRefresh(T::class.simpleName.toString(), false)
            }
        }

        /**
         * A list of all the streams, mapped by key, in the latest version of the API data found.
         *
         */
        var allStreams: Map<String, Stream> = mapOf()
            private set

        /**
         * Updates `allStreams` after the Api is refreshed.
         */
        private fun Api.updateAllStreams() {
            allStreams = cameras.map { camera ->
                camera.streams.map {
                    Stream(it.key, it.value, camera)
                }
            }
                .flatten()
                .associateBy { it.key }
        }

        /**
         * Fetch the top-level Moonfire API data.
         */
        suspend fun fetchApi(): Api? {
            return try {
                apiCall<Api>(true) {
                    get {
                        url {
                            apiConfig("")
                            parameter("days", "true")
                        }
                    }
                }?.also { it.updateAllStreams() }
            } catch (ex: ClientRequestException) {
                if (ex.response.status == HttpStatusCode.Unauthorized)
                    User.showLoginDialog()
                else
                    Toast.toast(ex.message.substringAfterLast("Text:"))
                null
            }
        }

        @Serializable
        data class LoginData(val username: String, val password: String)

        /**
         * Login. If successful a cookie will be set.
         */
        suspend fun login(username: String, password: String): Boolean {
            return try {
                apiCall(true) {
                    post<HttpResponse> {
                        contentType(ContentType.Application.Json)
                        url {
                            apiConfig()
                            path("api", "login")
                        }
                        body = LoginData(username, password)
                    }
                }
                true
            } catch (ex: ClientRequestException) {
                Toast.toast(ex.message.substringAfterLast("Text:"))
                false
            }
        }

        /**
         * Tell the backend to cancel this login token
         * @param csrf The login token
         */
        suspend fun logout(csrf: String) {
            apiCall {
                post<HttpResponse> {
                    contentType(ContentType.Application.Json)
                    url {
                        apiConfig()
                        path("api/logout")
                        body = mapOf("csrf" to csrf)
                    }
                }
                Toast.toast("Logged out")
                App.clearApiData()
            }
        }


        /**
         * Fetch a recording set for a given stream.
         *
         * @param stream The stream in question.
         * @param startTime Only fetch recordings after this time
         * @param endTime The upper time limit
         * @param maxDuration Split the recordings into chunks of this size.
         * @return A RecList or null if something went wrong.
         */
        suspend fun fetchRecording(
            stream: Stream,
            startTime: Date,
            endTime: Date,
            maxDuration: Duration? = null
        ): RecList? {
            return apiCall {
                get {
                    url {
                        apiConfig()
                        path("api", "cameras", stream.camera.uuid, stream.name, "recordings")
                    }
                    parameter("startTime90k", startTime.as90k)
                    parameter("endTime90k", endTime.as90k)
                    maxDuration?.let {
                        parameter("split90k", it.as90k)
                    }
                }
            }
        }

        /**
         * As above, with a base date, a start and end time.
         *
         * @param stream The stream to fetch
         * @param startDate The date to fetch - expected to represent 00:00 on a given date
         * @param startTime The start time in seconds offset from the startDate
         * @param duration The recording duration in seconds
         */
        suspend fun fetchRecording(
            stream: Stream,
            startDate: Date,
            startTime: Int,     // seconds
            duration: Int
        ): RecList? {
            return fetchRecording(stream, startDate.plusSeconds(startTime), startDate.plusSeconds(startTime + duration))
        }

        /**
         * Construct a URL to refer to a recording, possibly spanning ids
         *
         * @param startId The ID of the start of the recording
         * @param endId The ID of the last recording segment - defaults to the startId
         * @param openId An optional openId to disambiguate recordings across reboots
         * @param subTitle If set, will add timestamp captions
         * @param startOffset The offset in seconds from the start of the recording to commence playback
         * @param endOffset The offset in seconds for the end of the recording.
         *
         */
        fun recordingUrl(
            key: String,
            startId: Int,
            endId: Int = startId,
            openId: Int? = null,
            subTitle: Boolean = false,
            startOffset: Int? = null,
            endOffset: Int? = null
        ): String {
            val builder = StringBuilder("$startId-$endId")
            openId?.let {
                builder.append("@$openId")
            }
            builder.append(".")
            startOffset?.let {
                builder.append("${it * 90000L}")
            }
            builder.append("-")
            endOffset?.let {
                builder.append("${it * 90000L}")
            }
            val params = mutableMapOf<String, String?>(
                "s" to builder.toString()
            )
            if (subTitle)
                params["ts"] = null
            return "/api/cameras/$key/view.mp4".url(params)
        }
    }
}

/**
 * Retrieve the user data, allowing old and new formats
 */
val Api.user: Api.UserData?
    get() = userData ?: session?.username?.let {
        Api.UserData(0, it, mapOf(), session)
    }
val Api.UserData.gravatarUrl: String
    get() {
        val hash = name.trim().lowercase().toByteArray().md5().hexLower
        return "https://www.gravatar.com/avatar/$hash.jpg?d=mp"
    }


fun URLBuilder.apiConfig(vararg pathSegments: String, websocket: Boolean = false) {
    window.location.also { location ->
        protocol = if (location.protocol.endsWith('s')) {
            if (websocket) URLProtocol.WSS else URLProtocol.HTTPS
        } else {
            if (websocket) URLProtocol.WS else URLProtocol.HTTP
        }
        host = location.hostname
        location.port.toIntOrNull()?.let { port = it }
        path("api", *pathSegments)
    }
}
