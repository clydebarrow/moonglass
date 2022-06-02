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
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.w3c.dom.events.Event

/**
 * Helper for saving state.
 */

object SavedState {

    /**
     * Restore state from localstorage. If no matching key is found or the deserialisation throws an exception, a
     * specified default value is created and returned.
     * @receiver The storage key to use
     * @param default A block to create a default value
     * @return The saved object, or the default
     */

    inline fun <reified T : Any> String.restore(default: () -> T): T {
        console.log("Restoring from key $this")
        return try {
            window.localStorage.getItem(this)?.let {
                console.log("Read $it")
                Json.decodeFromString(it)
            }
        } catch (ex: Exception) {
            console.log(ex.toString())
            null
        } ?: default()
    }

    /**
     * Save an object to localstorage
     * @param key The storage key to use
     * @param data The object to save. Must be a serialized type
     */
    inline fun <reified T : Any> save(key: String, data: T) {
        try {
            window.localStorage.setItem(key, Json.encodeToString(data))
        } catch (ex: Exception) {
            console.log("${ex.message}, $data")
        }
    }

    /**
     * Register a callback for window unload
     */

    fun addOnUnload(callback: () -> Unit) {
        savers.add(callback)
    }

    /**
     * Remove a callback
     */

    fun removeOnUnload(callback: () -> Unit) {
        savers.remove(callback)
    }


    /**
     * Save all savers
     */
    private fun saveAll(@Suppress("unused") event: Event) {
        savers.forEach { it() }
    }

    private val savers: MutableList<() -> Unit> by lazy {
        window.addEventListener("beforeunload", ::saveAll)
        mutableListOf()
    }
}
