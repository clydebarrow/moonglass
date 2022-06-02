package org.moonglass.ui.widgets

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import org.w3c.dom.HTMLElement

/**
 * A utility class to intercept mouse operation and move an element on screen.
 *
 * @param element The element to attach to
 * @param initial The initial screen position of the element
 * @param scope A scope to use to deliver the flow.
 */
class Dragger(val element: HTMLElement, initial: ScreenPosition, scope: CoroutineScope = MainScope()) {

    private var current = initial

    val flow: StateFlow<ScreenPosition> = DragWatcher(element, scope).flow
        .onEach { event ->
            if (event.type == DragWatcher.Type.Move) {
                current = current.update(element, event.deltaX, event.deltaY)
                element.style.left = "${current.x}px"
                element.style.top = "${current.y}px"
            }
        }
        .filter { it.type == DragWatcher.Type.End }
        .map { current }
        .stateIn(scope, SharingStarted.WhileSubscribed(), initial)
}
