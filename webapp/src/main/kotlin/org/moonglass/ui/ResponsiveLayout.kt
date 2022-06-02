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

package org.moonglass.ui

import kotlinx.browser.window
import kotlinx.css.LinearDimension
import kotlinx.css.properties.s
import kotlinx.css.rem
import kotlinx.css.vh
import kotlinx.css.vw


object ResponsiveLayout {
    enum class Size(val minWidth: Int, val mobile: Boolean = false) {
        small(0, true),
        medium(650, true),
        large(970),
        extraLarge(1100),
        enormous(1600)
    }

    val hasTouch = window.navigator.maxTouchPoints > 0

    // any aspect ratio less than this is regarded as "Portrait"
    const val portraitAspect = 1.5

    // show the side menu if the aspect ration is greathr than this
    val showMenuAspect get() = 1.8

    val aspectRatio get() = (window.innerWidth.toDouble() / window.innerHeight)

    val isPortrait get() = aspectRatio < portraitAspect

    val showSideMenu: Boolean get() = !hasTouch && aspectRatio > showMenuAspect && !current.mobile

    val menuTransitionTime = 0.3.s

    val current: Size
        get() {
            val width = window.innerWidth
            return Size.values().lastOrNull { it.minWidth <= width } ?: Size.small
        }


    /** The height of the navbar
     *
     */
    val navBarHeight = 4.rem        // height of nav bar
    val sideBarWidth = 12.rem       // width of sidebar.

    val sideBarReserve get() = if (showSideMenu) sideBarWidth else 0.rem

    val outerHeight get() = 100.vh - navBarHeight

    val contentHeight: LinearDimension
        get() {
            return if (isPortrait)
                (100.vh - navBarHeight) / 2
            else
                100.vh - navBarHeight
        }

    val contentWidth
        get() = if (isPortrait)
            100.vw - sideBarWidth
        else
            (100.vw - sideBarWidth) / 2

    val playerHeight get() = contentHeight     // make them the same for now
}
