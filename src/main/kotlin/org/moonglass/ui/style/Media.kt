package org.moonglass.ui.style

object Media {
    fun padding(weight: Int): String {
        return "${weight * 0.25}rem"
    }


    fun mediaWidth(maxWidth: Int = -1, minWidth: Int = -1): String {
        val max = if (maxWidth >= 0) "(max-width: ${maxWidth}px)" else null
        val min = if (minWidth >= 0) "(min-width: ${minWidth}px)" else null
        return listOf("only screen", max, min).filterNotNull().joinToString(" and ") { it }
    }

    val small = mediaWidth(maxWidth = 639)
    val medium = mediaWidth(maxWidth = 767)
    val large = mediaWidth(maxWidth = 1023)
    val extraLarge = mediaWidth(maxWidth = 1279)
}

object Font {
    val medium = 500
}
