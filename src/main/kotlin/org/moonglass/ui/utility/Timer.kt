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
