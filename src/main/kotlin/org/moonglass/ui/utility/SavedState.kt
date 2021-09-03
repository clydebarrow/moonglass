package org.moonglass.ui.utility

import kotlinx.browser.window
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json


object SavedState {

    inline fun <reified T : Any> restore(key: String): T? {
        return window.localStorage.getItem(key)?.let { Json.decodeFromString(it) }
    }

    inline fun <reified T: Any> save(key: String, data: T) {
        try {
            window.localStorage.setItem(key, Json.encodeToString(data))
        } catch (ex: Exception) {
            console.log("${ex.message}, $data")
        }
    }
}
