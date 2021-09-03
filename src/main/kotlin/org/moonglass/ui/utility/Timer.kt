package org.moonglass.ui.utility

import kotlinx.browser.window
import kotlinx.css.del

/**
 * A one-shot timer.
 */
class Timer {

    private var handle: Int? = null

    /**
     * Is the timer running?
     */
    val isRunning get() = handle != null

    /**
     * Start the timer.
     * @param delayMs The time  in ms to wait before executing the callback
     * @param handler The callback to execute on completion.
     *
     */

    fun start(delayMs: Int, handler: (Timer) -> Unit) {
        handle?.let { window.clearTimeout(it) }
        handle = window.setTimeout({
            handle = null
            console.log("timer completed")
            handler(this)
        }, delayMs.coerceAtLeast(1))
    }

    fun cancel() {
        handle?.let { window.clearTimeout(it) }
        handle = null
    }
}
