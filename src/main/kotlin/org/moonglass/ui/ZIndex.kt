package org.moonglass.ui

/**
 * Provide a structured way of managing z-indices. Most important stuff comes last.
 *
 * Gaps are left in the sequence to allow for fine-tuning
 */
enum class ZIndex(val index: Int) {
    Default(0),        // standard (0)
    NavBar(10),         // navbar here?
    Dismisser(20),      // overlay to dismiss a menu or dialog
    Menu(30),           // menu on top of standard stuff
    Input(40),          // modal input
    Spinner(50),        // busy indicator
    Toast(60);          // transient feedback

    // allow simplified syntax
    operator fun invoke() = index
}
