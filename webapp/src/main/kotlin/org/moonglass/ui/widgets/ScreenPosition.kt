package org.moonglass.ui.widgets

import kotlinx.browser.window
import kotlinx.css.CssBuilder
import kotlinx.css.bottom
import kotlinx.css.left
import kotlinx.css.px
import kotlinx.css.right
import kotlinx.css.top
import kotlinx.serialization.Serializable
import org.w3c.dom.HTMLElement
import kotlin.math.abs

/**
 * A class representing a screen position of an element.
 * Negative values represent displacements from right or bottom, positive from top or left.
 * The position can be normalised, i.e. each of x and y should have an absolute value which is
 * the minimum required to specify the location, so that as the element moves across the window centre it
 * flips between positive and negative displacements.
 *
 */
@Serializable
data class ScreenPosition(val x: Int, val y: Int) {

    /**
     * Given a move from this position of the specified element, compute a new position, such that the
     * element is completely contained within the window bounds
     *
     * TODO - allow specification of bounds rather than using the window bounds
     */
    fun update(element: HTMLElement, deltaX: Int, deltaY: Int): ScreenPosition {
        // if the range is invalid (lowerbound greater than higher bound) just fix the element to top/left corner
        // this could occur if the element won't fit inside the window bounds
        val newX = try {
            (x + deltaX).coerceIn(0, window.innerWidth - element.clientWidth)
        } catch (ex: Exception) {
            0
        }
        val newY = try {
            (y + deltaY).coerceIn(0, window.innerHeight - element.clientHeight)
        } catch (ex: Exception) {
            0
        }
        return ScreenPosition(newX, newY)
    }

    /**
     * Get a version that is guaranteed to be contained onscreen
     */

    fun onScreen(element: HTMLElement): ScreenPosition {
        if (x in (0..window.innerWidth - element.clientWidth) && y in (0..window.innerHeight - element.clientHeight))
            return this
        return ScreenPosition(
            x.coerceIn(0, window.innerWidth - element.clientWidth - 10),
            y.coerceIn(0, window.innerHeight - element.clientHeight - 10)
        )
    }

    /**
     * Get a normalised version of the position. This works only if the element is inside the window
     * A normalized version is relative to the nearest border.
     */

    fun normalise(element: HTMLElement): ScreenPosition {
        // first make sure it's inside.
        return update(element, 0, 0).run {
            val height = element.offsetHeight
            val width = element.offsetWidth
            val wWidth = window.innerWidth
            val wHeight = window.innerHeight
            val rightX = wWidth - x - width // right margin
            val bottomY = wHeight - y - height // bottom margin
            ScreenPosition(if (x < abs(rightX)) x else -rightX, if (y < abs(bottomY)) y else -bottomY)
        }
    }

    /**
     * Convert a normalised version to absolute
     */

    fun deNormalise(element: HTMLElement): ScreenPosition {
        if (x >= 0 && y >= 0)
            return this
        return ScreenPosition(
            if (x >= 0) x else window.innerWidth + x - element.clientWidth,
            if (y >= 0) y else window.innerHeight + y - element.clientHeight
        )
    }

    /**
     * Move an element to this position
     */

    fun CssBuilder.moveTo() {
        if (x < 0)
            right = -x.px
        else
            left = x.px
        if (y < 0)
            bottom = -y.px
        else
            top = y.px
    }
}
