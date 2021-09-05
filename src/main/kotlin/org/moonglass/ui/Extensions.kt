package org.moonglass.ui

import kotlinx.browser.window
import kotlinx.coroutines.await
import kotlinx.css.Color
import kotlinx.css.Display
import kotlinx.css.PointerEvents
import kotlinx.css.Position
import kotlinx.css.backgroundColor
import kotlinx.css.borderRadius
import kotlinx.css.bottom
import kotlinx.css.display
import kotlinx.css.left
import kotlinx.css.margin
import kotlinx.css.padding
import kotlinx.css.pointerEvents
import kotlinx.css.position
import kotlinx.css.properties.boxShadow
import kotlinx.css.px
import kotlinx.css.rem
import kotlinx.css.rgba
import kotlinx.css.right
import kotlinx.css.top
import kotlinx.css.zIndex
import kotlinx.html.js.onClickFunction
import kotlinx.serialization.decodeFromString
import react.Props
import react.RBuilder
import react.RComponent
import react.State
import react.dom.attrs
import styled.StyledDOMBuilder
import styled.css
import styled.styledDiv
import kotlin.js.Date
import kotlin.time.Duration

external fun encodeURIComponent(str: String): String

/**
 * Convenience function for setting state in a component
 */
fun <T : State, P : Props> RComponent<P, T>.applyState(callback: (() -> Unit)? = null, handler: T.() -> Unit) {
    if (callback == null)
        setState({ newState ->
            newState.apply(handler)
        })
    else
        setState({ newState ->
            newState.apply(handler)
        }, callback)
}

/**
 * Add a name to an element
 */
var StyledDOMBuilder<*>.name: String
    get() = attrs["data-name"]
    set(value) {
        attrs["data-name"] = value
    }

var StyledDOMBuilder<*>.tooltip: String
    get() = attrs["data-tooltip"]
    set(value) {
        attrs["data-tooltip"] = value
    }

/**
 * Conversions on 90k intervals
 */

typealias Duration90k = Long
typealias Time90k = Long

val Duration90k?.toDuration: Duration
    get() {
        if (this == null)
            return Duration.ZERO
        return Duration.seconds(this / 90000.0)
    }


val Time90k.asDate get() = Date(this / 90)

val Date.as90k: Duration90k
    get() = (getTime() * 90.0).toLong()

val Duration.as90k: Duration90k
    get() = (inWholeMilliseconds * 90)

val Duration.hours: Double get() = inWholeMinutes / 60.0

data class HttpException(val status: Int, val response: String) : Exception("Http error code $status")

fun String.url(params: Map<String, Any?>): String {
    return if (params.isNotEmpty()) {
        "$this?" + params.map {
            //console.log("Map entry ${it.key} to ${it.value}")
            if (it.value != null)
                encodeURIComponent(it.key) + "=" + encodeURIComponent(it.value.toString())
            else
                encodeURIComponent(it.key)
        }.joinToString("&")
    } else
        this
}

suspend inline fun <reified T : Any> String.fetch(
    params: Map<String, Any?> = mapOf(),
    headers: Map<String, Any> = mapOf()
): T {
    val response = window.fetch(url(params)).await()
    if (response.ok) {
        val text = response.text().await()
        return kotlinx.serialization.json.Json.decodeFromString(text)
    }
    throw HttpException(response.status.toInt(), response.statusText)
}

// give a card style to a thing
fun StyledDOMBuilder<*>.cardStyle() {
    css {
        backgroundColor = Color.white
        borderRadius = 0.2.rem
        display = Display.flex
        margin(0.5.rem)
        padding(0.5.rem)
        boxShadow(rgba(0, 0, 0, 0.2), 0.px, 2.px, 1.px, (-1).px)
        boxShadow(rgba(0, 0, 0, 0.14), 0.px, 1.px, 1.px, 0.px)
        boxShadow(rgba(0, 0, 0, 0.12), 0.px, 1.px, 4.px, 0.px)
    }
}

fun Int.digits(num: Int): String = toString().padStart(num, '0')

//const val timePattern = "dd mmm yyyy HH:MM:ss"
val Date.formatDate: String
    get() {
        val day = getDate().digits(2)
        val month = toLocaleString("default", dateLocaleOptions { month = "short" })
        val year = getFullYear().toString()
        return listOf(day, month, year).joinToString(" ")
    }

val Date.formatHHMM: String
    get() {
        val hours = getHours().digits(2)
        val minutes = getMinutes().digits(2)
        return listOf(hours, minutes).joinToString(":")
    }

val Date.formatTime: String
    get() {
        val hours = getHours().digits(2)
        val minutes = getMinutes().digits(2)
        val seconds = getSeconds().digits(2)
        return listOf(hours, minutes, seconds).joinToString(":")
    }

val Time90k.formatDate get() = asDate.formatDate
val Time90k.formatTime get() = asDate.formatTime

inline fun <reified T : State> RComponent<out Props, T>.updateState(crossinline block: (T) -> Unit) {
    setState({ state.apply(block) })
}

val Int.formatHhMm: String
    get() {
        val minutes = this / 60
        val hours = minutes / 60
        return listOf(hours, minutes - hours * 60).joinToString(":") { it.digits(2) }
    }
val String.toHHMM: List<Int>
    get() = split(':').map { it.toInt() }.take(2)

val String.hhMmToSeconds: Int
    get() = toHHMM.let {
        ((it.firstOrNull()?.toInt() ?: 0) * 60 + (it.drop(1).firstOrNull()?.toInt() ?: 0)) * 60
    }


fun Date.withTime(hours: Int = 0, minutes: Int = 0): Date {
    return Date(getFullYear(), getMonth(), getDate(), hours, minutes)
}

fun Date.withTime(hhmm: String): Date {
    val time = hhmm.toHHMM
    return Date(getFullYear(), getMonth(), getDate(), time.firstOrNull() ?: 0, time.drop(1).firstOrNull() ?: 0)
}

fun Date.withDate(date: Date): Date {
    return Date(date.getFullYear(), date.getMonth(), date.getDate(), getHours(), getMinutes(), getSeconds())
}

fun Date.plusHours(hours: Int): Date {
    return Date(getTime() + hours * 60 * 60 * 1000.0)
}

fun Date.plusMs(ms: Long): Date {
    return Date(getTime() + ms)
}

fun Date.plusSeconds(seconds: Int): Date {
    return Date(getTime() + seconds * 1000.0)
}

fun Date.after(other: Date): Boolean {
    return getTime() > other.getTime()
}


fun RBuilder.dismisser(onDismiss: () -> Unit, visible: Boolean = true, z: Int = ZIndex.Dismisser(), builder: RBuilder.() -> Unit) {
    styledDiv {
        name = "dismisser"
        css {
            position = Position.absolute
            left = 0.px
            top = 0.px
            bottom = 0.px
            right = 0.px
            backgroundColor = Color.transparent
            zIndex = z
            if(!visible) {
                display = Display.none
                pointerEvents = PointerEvents.none
            }
        }
        attrs {
            onClickFunction = {
                onDismiss()
            }
        }
        builder()
    }
}

