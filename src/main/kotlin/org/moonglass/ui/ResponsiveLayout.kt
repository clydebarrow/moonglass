package org.moonglass.ui

import kotlinx.browser.window
import react.createContext


object ResponsiveLayout {
    enum class Size(val minWidth: Int, val mobile: Boolean = false) {
        small(0, true),
        medium(650, true),
        large(970),
        extraLarge(1100),
        enormous(1600)
    }

    val context = createContext(Size.medium)

    val current: Size
        get() {
            val width = window.innerWidth
            return Size.values().lastOrNull { it.minWidth <= width } ?: Size.small
        }
}
