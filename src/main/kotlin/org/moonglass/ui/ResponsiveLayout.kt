package org.moonglass.ui

import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.css.LinearDimension
import kotlinx.css.rem
import kotlinx.css.vh


object ResponsiveLayout {
    enum class Size(val minWidth: Int, val mobile: Boolean = false) {
        small(0, true),
        medium(650, true),
        large(970),
        extraLarge(1100),
        enormous(1600)
    }

    // any aspect ratio less than this is regarded as "Portrait"
    const val portraitAspect = 1.5

    // show the side menu if the aspect ration is greathr than this
    const val showMenuAspect = 1.9

    val aspectRatio get() = (window.innerWidth.toDouble() / window.innerHeight)

    val isPortrait get() = aspectRatio < portraitAspect

    val showSideMenu: Boolean get() = aspectRatio > showMenuAspect && !current.mobile

    val current: Size
        get() {
            val width = window.innerWidth
            return Size.values().lastOrNull { it.minWidth <= width } ?: Size.small
        }


    /** The height of the navbar
     *
     */
    val navBarEmHeight = 4        // height in ems
    val sideBarEmWidth = 12

    val outerHeight get() = 100.vh - navBarEmHeight.rem

    val contentHeight: LinearDimension
        get() {
            return if (isPortrait)
                (100.vh  - navBarEmHeight.rem) / 2
            else
                100.vh - navBarEmHeight.rem
        }

    val playerHeight get() = contentHeight     // make them the same for now
}
