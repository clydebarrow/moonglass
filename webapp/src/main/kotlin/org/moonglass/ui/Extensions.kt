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

package org.moonglass.ui

import kotlinx.browser.window
import kotlinx.coroutines.await
import kotlinx.css.Color
import kotlinx.css.Display
import kotlinx.css.LinearDimension
import kotlinx.css.PointerEvents
import kotlinx.css.Position
import kotlinx.css.backgroundColor
import kotlinx.css.borderRadius
import kotlinx.css.bottom
import kotlinx.css.display
import kotlinx.css.height
import kotlinx.css.left
import kotlinx.css.margin
import kotlinx.css.opacity
import kotlinx.css.padding
import kotlinx.css.pointerEvents
import kotlinx.css.position
import kotlinx.css.properties.LineHeight
import kotlinx.css.properties.boxShadow
import kotlinx.css.properties.transition
import kotlinx.css.px
import kotlinx.css.rem
import kotlinx.css.rgba
import kotlinx.css.right
import kotlinx.css.top
import kotlinx.css.width
import kotlinx.css.zIndex
import kotlinx.html.DIV
import kotlinx.html.IMG
import kotlinx.html.js.onClickFunction
import kotlinx.serialization.decodeFromString
import org.moonglass.ui.user.UserPreferences
import react.Props
import react.RBuilder
import react.RComponent
import react.State
import react.dom.attrs
import styled.StyledDOMBuilder
import styled.css
import styled.styledDiv
import kotlin.js.Date
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.time.Duration

external fun encodeURIComponent(str: String): String

object Extensions {
    class Size(val name: String, val value: Double) {
        fun format(number: Double) = "${(number / value).minTwo} $name"
    }

    val sizes = listOf("bytes", "KiB", "MiB", "GiB", "TiB")
        .mapIndexed { index, s -> Size(s, 1024.0.pow(index)) }
}

/**
 * Convenience function for setting state in a component. It takes care of returning the mutated state.
 * Remember that state updates are asynchronous!
 *
 * @param callback An optional callback that will be executed after the state update is done.
 * @param handler A block to mutate the state.
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

// TODO - the css approach to tooltips doesn't work well on mobile. Need to allow an alternative such as long-press
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
            if (it.value != null)
                encodeURIComponent(it.key) + "=" + encodeURIComponent(it.value.toString())
            else
                encodeURIComponent(it.key)
        }.joinToString("&")
    } else
        this
}

// give a card style to a thing
fun StyledDOMBuilder<*>.cardStyle() {
    css {
        useColorSet(Theme().content)
        borderRadius = 0.4.rem
        display = Display.flex
        margin(0.5.rem)
        padding(0.5.rem)
        boxShadow(rgba(0, 0, 0, 0.2), 0.px, 2.px, 1.px, (-1).px)
        boxShadow(rgba(0, 0, 0, 0.14), 0.px, 1.px, 1.px, 0.px)
        boxShadow(rgba(0, 0, 0, 0.12), 0.px, 1.px, 4.px, 0.px)
    }
}

// convert this number to a string with at least `num` digits, zero padding the start
// as required.
fun Int.digits(num: Int): String = toString().padStart(num, '0')

fun Double.precision(digits: Int): String {
    val whole = toLong()
    val frac = ((this - whole) * 10.0.pow(digits)).toInt().digits(digits)
    return "$whole.$frac"
}

val Double.minTwo: String
    get() {
        if (this > 10.0)
            return roundToInt().toString()
        return precision(1)

    }

// get a size as a string like "125 bytes" or "1.5 GiB"
val Long.asSize: String
    get() {
        return Extensions.sizes.let { sizes ->
            (sizes.firstOrNull { (this / it.value).toInt() in (1..999) } ?: sizes.last()).format(this.toDouble())
        }
    }

// get a string representation of the number (expressed as bytes/sec) in a format scaled depending on the
// magnitude.
val Double.asBitRate: String
    get() {
        val unit: String
        val value: Double
        if (this < 500000 / 8) {
            unit = "kbps"
            value = this / 1024 * 8
        } else {
            unit = "Mbps"
            value = this / 1024 / 1024 * 8
        }
        val whole = value.toInt()
        val frac = ((value - whole.toDouble()) * 10).roundToInt()
        return "$whole.$frac $unit"
    }

//const val timePattern = "dd mmm yyyy HH:MM:ss"
val Date.formatDate: String
    get() = UserPreferences.current.dateFormat.run { format() }

val Date.formatTime: String
    get() = UserPreferences.current.timeFormat.run { format() }

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


fun RBuilder.dismisser(
    onDismiss: () -> Unit,
    visible: Boolean = true,
    background: Color = Theme().overlay,
    z: Int = ZIndex.Dismisser(),
    builder: StyledDOMBuilder<DIV>.() -> Unit
) {
    styledDiv {
        name = "dismisser"
        css {
            position = Position.fixed
            left = 0.px
            top = 0.px
            bottom = 0.px
            right = 0.px
            transition("all", ResponsiveLayout.menuTransitionTime)
            backgroundColor = background
            zIndex = z
            if (!visible) {
                opacity = 0.0
                zIndex = -1000
                pointerEvents = PointerEvents.none
            }
        }
        attrs {
            onClickFunction = { onDismiss() }
        }
        builder()
    }
}

/**
 * Construct an image src attribute
 * @param image The image name. Can be a full path, a simple name or a basename (recommended)
 * @param width The desired width of the image
 * @param height The height, defaults to the same as the width
 */

fun StyledDOMBuilder<IMG>.imageSrc(image: String, width: LinearDimension, height: LinearDimension = width) {
    val path = image.let {
        if (it.contains('/'))
            it
        else
            "/images/$it"
    }.let {
        if (it.contains('.'))
            it
        else
            "$it.svg"
    }
    css {
        this.height = width
        this.width = width
    }
    attrs {
        src = path.replace(".svg", ".png")     // TODO This is just because the Moonfire http server doesn't like svg
    }
}
