package org.moonglass.ui.widgets

import kotlinx.browser.document
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.w3c.dom.HTMLElement
import org.w3c.dom.events.Event
import org.w3c.dom.events.MouseEvent
import react.dom.NativeTouchEvent

/**
 * A utility class to intercept mouse operation and deliver a set of drag events
 * @param element The element to attach to
 * @param initial The initial screen position of the element
 * @param scope A scope to use to deliver the flow.
 */
class Dragger(val element: HTMLElement, initial: ScreenPosition, private val scope: CoroutineScope = MainScope()) {

    private val src = MutableStateFlow(initial)

    private var current = initial


    val flow: StateFlow<ScreenPosition> = src

    private var lastX: Int = 0
    private var lastY: Int = 0

    private val upFun = fun(_: Event) {
        up()
    }

    // fun turned into an object to make sure removeEventListener works properly.
    private val moveFun = fun(event: Event) {
        event as MouseEvent
        // check left button is still down, finish if not
        if ((event.buttons.toInt() and 1) == 0) {
            up()
            return
        }
        doMove(event.clientX, event.clientY)
    }

    private val touchMoveFun = fun(event: Event) {
        event as NativeTouchEvent
        event.touches.item(0)?.let { touch ->
            doMove(touch.clientX, touch.clientY)
        }
    }
    private val touchEndFun = fun(_: Event) {
        element.removeEventListener("touchend", upFun)
        element.removeEventListener("touchmove", touchMoveFun)
        update(current)
    }


    private fun update(position: ScreenPosition) {
        scope.launch {
            src.emit(position)
        }
    }

    init {
        element.addEventListener("mousedown", ::mouseDown)
        element.addEventListener("touchstart", ::touchStart)
    }

    private fun touchStart(event: Event) {
        event.stopPropagation()
        event.preventDefault()
        event.unsafeCast<NativeTouchEvent>().let { touchEvent ->
            touchEvent.touches.item(0)?.let {
                lastX = it.clientX
                lastY = it.clientY
                element.addEventListener("touchmove", touchMoveFun)
                element.addEventListener("touchend", touchEndFun)
                current = ScreenPosition(element.offsetLeft, element.offsetTop)
            }
        }
    }

    private fun mouseDown(mouseEvent: Event) {
        mouseEvent as MouseEvent
        if (mouseEvent.button.toInt() != 0)      // only left button initiates drag
            return
        mouseEvent.stopPropagation()
        mouseEvent.preventDefault()
        lastX = mouseEvent.clientX
        lastY = mouseEvent.clientY
        current = ScreenPosition(element.offsetLeft, element.offsetTop)
        document.addEventListener("mousemove", moveFun)
        document.addEventListener("mouseup", upFun)
    }

    private fun up() {
        document.removeEventListener("mouseup", upFun)
        document.removeEventListener("mousemove", moveFun)
        update(current)
    }

    private fun doMove(x: Int, y: Int) {
        current = current.update(element, x - lastX, y - lastY)
        element.style.left = "${current.x}px"
        element.style.top = "${current.y}px"
        lastX = x
        lastY = y
    }
}
