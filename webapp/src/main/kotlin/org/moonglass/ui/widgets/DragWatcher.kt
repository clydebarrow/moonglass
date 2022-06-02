package org.moonglass.ui.widgets

import kotlinx.browser.document
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.w3c.dom.HTMLElement
import org.w3c.dom.events.Event
import org.w3c.dom.events.MouseEvent
import react.dom.NativeTouchEvent

/**
 * A utility class to intercept mouse operation and deliver a set of drag events
 * @param element The element to attach to
 * @param scope A scope to use to deliver the flow.
 */
class DragWatcher(val element: HTMLElement, private val scope: CoroutineScope = MainScope()) {

    /**
     * Event types
     */
    enum class Type {
        None,       // start of stream only
        Start,      // initial event, has current location as from and to.

        // provides absolute coordinates.
        Move,       // a move event -
        End         // end event, from/to are equal to final position
    }

    /**
     * An object emitted by the flow. The initial event (Start) will always have 0 for all values. Subsequent
     * events have values relative to 0.
     */
    data class DragEvent(
        val type: Type,         // the type of the event
        val fromX: Int,      // where this event started from, X value, will equal previous toX
        val fromY: Int,      // where this event started from, Y value
        val toX: Int,        // the end point of this move
        val toY: Int        // the end point of this move
    ) {
        val deltaX: Int get() = toX - fromX
        val deltaY: Int get() = toY - fromY
    }

    // the internal, mutable source of the flow.
    private val src = MutableSharedFlow<DragEvent>()

    // The public version of the hot event flow.

    val flow = src.asSharedFlow()

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
        touchClean()
        endDrag()
    }


    init {
        element.addEventListener("mousedown", ::mouseDown)
        element.addEventListener("touchstart", ::touchStart)
    }

    private fun cleanup() {
        mouseClean()
        touchClean()
        element.removeEventListener("mousedown", ::mouseDown)
        element.removeEventListener("touchstart", ::touchStart)
    }

    private fun emit(event: DragEvent) {
        if (scope.isActive)
            scope.launch {
                src.emit(event)
            }
        else
            cleanup()       // is this necessary?
    }

    private fun startDrag(x: Int, y: Int) {
        lastX = x
        lastY = y
        emit(DragEvent(Type.Start, x, y, x, y))
    }

    private fun endDrag() {
        emit(DragEvent(Type.End, lastX, lastY, lastX, lastY))
    }

    private fun touchStart(event: Event) {
        event.stopPropagation()
        event.unsafeCast<NativeTouchEvent>().let { touchEvent ->
            touchEvent.touches.item(0)?.let {
                startDrag(it.clientX, it.clientY)
                element.addEventListener("touchmove", touchMoveFun)
                element.addEventListener("touchend", touchEndFun)
            }
        }
    }

    private fun mouseDown(mouseEvent: Event) {
        mouseEvent as MouseEvent
        if (mouseEvent.button.toInt() != 0)      // only left button initiates drag
            return
        mouseEvent.stopPropagation()
        mouseEvent.preventDefault()
        startDrag(mouseEvent.clientX, mouseEvent.clientY)
        document.addEventListener("mousemove", moveFun)
        document.addEventListener("mouseup", upFun)
    }

    private fun touchClean() {
        element.removeEventListener("touchend", upFun)
        element.removeEventListener("touchmove", touchMoveFun)
    }

    private fun mouseClean() {
        document.removeEventListener("mouseup", upFun)
        document.removeEventListener("mousemove", moveFun)
    }

    private fun up() {
        mouseClean()
        endDrag()
    }

    private fun doMove(x: Int, y: Int) {
        emit(DragEvent(Type.Move, lastX, lastY, x, y))
        lastX = x
        lastY = y
    }
}
