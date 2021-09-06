package org.moonglass.ui.api

import kotlinx.serialization.Serializable
import org.moonglass.ui.fetch

@Serializable
data class Api(
    val cameras: List<Camera>,
    val signalTypes: List<String>,
    val session: Session? = null,
    val signals: List<String>,
    val timeZoneName: String // Australia/Sydney
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
        suspend fun fetch(): Api {
            return "/api/".fetch(mapOf("days" to true))
        }
    }
}
