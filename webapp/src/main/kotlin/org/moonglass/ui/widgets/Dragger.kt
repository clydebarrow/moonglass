package org.moonglass.ui.widgets

import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.html.CommonAttributeGroupFacade
import kotlinx.html.js.onMouseDownFunction
import kotlinx.serialization.Serializable
import org.w3c.dom.HTMLCanvasElement
import org.w3c.dom.HTMLElement
import org.w3c.dom.events.Event
import org.w3c.dom.events.MouseEvent
import kotlin.math.abs

/**
 * A class representing a screen position of an element.
 * Negative values represent displacements from right or bottom, positive from top or left.
 * The position can be normalised, i.e. each of x and y should have an absolute value which is
 * the minimum required to specify the location, so that as the element moves across the window centre it
 * flips between positive and negative displacements.
 *
 */
@Serializable
data class ScreenPosition(val x: Int, val y: Int) {

    /**
     * Given a move from this position of the specified element, compute a new normalised position, such that the
     * element has at least a specified fraction of each dimension still within the window bounds
     *
     * TODO - allow specification of bounds rather than using the window bounds
     */
    fun update(element: HTMLElement, deltaX: Int, deltaY: Int, frac: Double = 1.0): ScreenPosition {
        val height = element.offsetHeight
        val width = element.offsetWidth
        val xFrac = (width * frac).toInt()
        val yFrac = (height * frac).toInt()
        // if the range is invalid (lowerbound greater than higher bound) just fix the element to top/left corner
        // this could occur if the element won't fit inside the window bounds
        val newX = try {
            (x + deltaX).coerceIn(-width + xFrac, window.innerWidth - xFrac)
        } catch (ex: Exception) {
            0
        }
        val newY = try {
            (y + deltaY).coerceIn(-height + yFrac, window.innerHeight - yFrac)
        } catch (ex: Exception) {
            0
        }
        return ScreenPosition(newX, newY)
    }

    /**
     * Get a normalised version of the position. This works only if the element is inside the window
     */

    fun normalise(element: HTMLElement): ScreenPosition {
        // first make sure it's inside.
        return update(element, 0, 0, 1.0).run {
            val height = element.offsetHeight
            val width = element.offsetWidth
            val wWidth = window.innerWidth
            val wHeight = window.innerHeight
            val rightX = wWidth - x - width // right margin
            val bottomY = wHeight - y - height // bottom margin
            ScreenPosition(if (x < abs(rightX)) x else -rightX, if (y < abs(bottomY)) y else -bottomY)
        }
    }
}

/**
 * A utility class to intercept mouse operation and deliver a set of drag events
 * @param current The initial screen position of the element
 * @param scope A scope to use to deliver the flow.
 */
class Dragger(private var current: ScreenPosition, private val scope: CoroutineScope = MainScope()) {

    private val src = MutableStateFlow(current)

    val flow: StateFlow<ScreenPosition> = src

    private var lastX: Int = 0
    private var lastY: Int = 0
    private var element: HTMLElement? = null

    private val upFun = fun(event: Event): Unit {
        up(event)
    }
    private val moveFun = fun(event: Event) {
        move(event)
    }

    private fun down(event: Event) {
        event.stopPropagation()
        event.preventDefault()
        event.unsafeCast<react.dom.MouseEvent<HTMLCanvasElement, MouseEvent>>().let { mouseEvent ->
            lastX = mouseEvent.clientX.toInt()
            lastY = mouseEvent.clientY.toInt()
            document.addEventListener("mousemove", moveFun)
            document.addEventListener("mouseup", upFun)
            element = mouseEvent.currentTarget
        }
    }

    private fun up(event: Event) {
        console.log("Event up")
        document.removeEventListener("mouseup", upFun)
        document.removeEventListener("mousemove", moveFun)
        scope.launch {
            src.emit(current)
        }
    }

    private fun move(event: Event) {
        event as MouseEvent
        element?.let { el ->
            current = current.update(el, event.clientX - lastX, event.clientY - lastY)
            el.style.left = "${current.x}px"
            el.style.top = "${current.y}px"
            lastX = event.clientX
            lastY = event.clientY
        }
    }

    fun attach(tag: CommonAttributeGroupFacade) {
        tag.onMouseDownFunction = ::down
    }
}
