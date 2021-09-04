package org.moonglass.ui

import kotlinx.browser.document
import kotlinx.browser.window
import org.w3c.dom.events.Event


object ResponsiveLayout {
    enum class Size(val minWidth: Int, val mobile: Boolean = false) {
        small(0, true),
        medium(650, true),
        large(970),
        extraLarge(1100),
        enormous(1600)
    }

    // we regard a landscape presentation as something with width significantly wider than height.
    val isPortrait get() = (window.innerWidth.toDouble() / window.innerHeight) < 1.2

    val showSideMenu: Boolean get() = !isPortrait && !current.mobile

    val current: Size
        get() {
            val width = window.innerWidth
            return Size.values().lastOrNull { it.minWidth <= width } ?: Size.small
        }

    /**
     * The calculated size of an M in pixels
     */

    var emPixels: Double = 12.0
        private set


    private fun setPixels(event: Event) {
        document.removeEventListener("load", ::setPixels)
        document.getElementById("fontSizer")?.let { el ->
            emPixels = window.getComputedStyle(el, " ").fontSize.toDouble()
            console.log("calculated emPixels = $emPixels")
        }
    }

    init {
        document.addEventListener("load", ::setPixels)
    }

    /** The height of the navbar
     *
     */
    val navBarEmHeight = 4        // height in ems
    val sideBarEmWidth = 12

    val contentHeight: Int
        get() {
            if (isPortrait)
                return (window.innerHeight - navBarEmHeight) / 2
            else
                return window.innerHeight - navBarEmHeight
        }

    val wrapperWidth: Int
        get() = if (showSideMenu) (window.innerWidth - sideBarEmWidth * emPixels).toInt() else window.innerWidth

    val contentWidth: Int
        get() {
            if (isPortrait)
                return wrapperWidth
            else
                return wrapperWidth / 2
        }
    val playerWidth: Int
        get() {
            if (isPortrait)
                return wrapperWidth
            else
                return wrapperWidth / 2
        }

    val playerHeight: Int get() = contentHeight     // make them the same for now
}
