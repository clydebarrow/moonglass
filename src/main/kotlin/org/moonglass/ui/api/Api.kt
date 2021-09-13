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

import io.ktor.client.HttpClient
import io.ktor.client.features.ClientRequestException
import io.ktor.client.features.HttpTimeout
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.URLBuilder
import io.ktor.http.URLProtocol
import io.ktor.http.contentType
import kotlinx.browser.window
import kotlinx.serialization.Serializable
import org.moonglass.ui.App
import org.moonglass.ui.as90k
import org.moonglass.ui.user.User
import org.moonglass.ui.widgets.Toast
import org.moonglass.ui.widgets.recordings.Stream
import kotlin.js.Date
import kotlin.time.Duration

@Serializable
data class Api(
    val cameras: List<Camera> = listOf(),
    val signalTypes: List<String> = listOf(),
    val session: Session? = null,
    val signals: List<String> = listOf(),
    val timeZoneName: String = ""// Australia/Sydney
) {
    @Serializable
    data class Camera(
        val description: String, // Hikvision Driveway Camera
        val shortName: String, // Driveway
        val streams: Map<String, StreamData>,
        val uuid: String // 7f2e2a50-1e68-4647-817b-03089ca2003e
    )

    @Serializable
    data class Day(
        val endTime90k: Long, // 146683224000000
        val startTime90k: Long, // 146675448000000
        val totalDuration90k: Long // 3807838306
    )

    @Serializable
    data class StreamData(
        val days: Map<String, Day> = mapOf(),
        val fsBytes: Long, // 38785380352
        val maxEndTime90k: Long, // 146704410690765
        val minStartTime90k: Long, // 146678775676182
        val record: Boolean, // true
        val retainBytes: Long, // 107374182400
        val totalDuration90k: Long, // 24637266927
        val totalSampleFileBytes: Long // 38776002681
    )

    @Serializable
    data class Session(
        val username: String,
        val csrf: String
    )

    companion object {
        val client = HttpClient {
            install(HttpTimeout) {
                requestTimeoutMillis = 10000
            }
            install(JsonFeature)
        }

        /**
         * Helper function to make an api call with timeout and exception trapping.
         * If the return type is nullable, a failure will return null, otherwise the exception will be rethrown
         */
        private inline fun <reified T : Any> apiCall(rethrow: Boolean = false, block: HttpClient.() -> T): T? {
            App.setRefresh(T::class.simpleName.toString(), true)
            return try {
                client.run {
                    block()
                }
            } catch (ex: Exception) {
                if (rethrow)
                    throw(ex)
                Toast.toast("${ex.message}")
                null
            } finally {
                App.setRefresh(T::class.simpleName.toString(), false)
            }
        }

        suspend fun fetchApi(): Api? {
            return try {
                apiCall(true) {
                    get {
                        url {
                            apiConfig("")
                            parameter("days", "true")
                        }
                    }
                }
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
            maxDuration: Duration
        ): RecList? {
            return apiCall {
                get {
                    url {
                        apiConfig()
                        path("api", "cameras", stream.camera.uuid, stream.name, "recordings")
                    }
                    parameter("startTime90k", startTime.as90k)
                    parameter("endTime90k", endTime.as90k)
                    parameter("split90k", maxDuration.as90k)
                }
            }
        }
    }
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
