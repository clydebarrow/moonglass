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
     * Restore state from localstorage
     * @param key The storage key to use
     * @return The saved object, or null if not saved
     */
    inline fun <reified T : Any> restore(key: String): T? {
        return try {
            window.localStorage.getItem(key)?.let { Json.decodeFromString(it) }
        } catch (ex: Exception) {
            console.log(ex.toString())
            null
        }
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

    fun onUnload(callback: () -> Unit) {
        savers.add(callback)
    }

    /**
     * Save all savers
     */
    private fun saveAll(event: Event) {
        savers.forEach { it() }
    }

    private val savers: MutableList<() -> Unit> by lazy {
        window.addEventListener("beforeunload", ::saveAll)
        mutableListOf()
    }
}
