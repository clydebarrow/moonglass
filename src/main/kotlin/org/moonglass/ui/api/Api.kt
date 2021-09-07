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
import io.ktor.http.URLBuilder
import io.ktor.http.URLProtocol
import io.ktor.http.contentType
import kotlinx.browser.window
import kotlinx.serialization.Serializable
import org.moonglass.ui.App
import org.moonglass.ui.Duration90k
import org.moonglass.ui.as90k
import org.moonglass.ui.widgets.Toast
import org.moonglass.ui.widgets.recordings.Stream
import kotlin.js.Date

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
        val streams: Map<String, ApiStream>,
        val uuid: String // 7f2e2a50-1e68-4647-817b-03089ca2003e
    )

    @Serializable
    data class Day(
        val endTime90k: Long, // 146683224000000
        val startTime90k: Long, // 146675448000000
        val totalDuration90k: Long // 3807838306
    )

    @Serializable
    data class ApiStream(
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
            return apiCall {
                get {
                    url {
                        setDefaults()
                        path("api", "")
                        parameter("days", "true")
                    }
                }
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
                            setDefaults()
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
                        setDefaults()
                        path("api/logout")
                        body = mapOf("csrf" to csrf)
                    }
                }
                Toast.toast("Logged out")
                App.session = null
            }
        }


        // /api/cameras/7f2e2a50-1e68-4647-817b-03089ca2003e/sub/recordings?startTime90k=146706552000000&endTime90k=146714328000000&split90k=324000000
        suspend fun fetchRecording(
            stream: Stream,
            startTime: Date,
            endTime: Date,
            maxDuration: Duration90k
        ): RecList? {
            return apiCall {
                get {
                    url {
                        setDefaults()
                        path("api", "cameras", stream.camera.uuid, stream.name, "recordings")
                    }
                    parameter("startTime90k", startTime.as90k)
                    parameter("endTime90k", endTime.as90k)
                    parameter("split90k", maxDuration)
                }
            }
        }
    }
}

fun URLBuilder.setDefaults(vararg pathSegments: String, websocket: Boolean = false) {
    window.location.also { location ->
        protocol = if (location.protocol.endsWith('s')) {
            if (websocket) URLProtocol.WSS else URLProtocol.HTTPS
        } else {
            if (websocket) URLProtocol.WS else URLProtocol.HTTP
        }
        host = location.host
        location.port.toIntOrNull()?.let { port = it }
        path("api", *pathSegments)
    }
}
