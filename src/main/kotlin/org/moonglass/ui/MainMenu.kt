package org.moonglass.ui

import org.moonglass.ui.content.Recordings
import react.State
import kotlin.reflect.KClass

object MainMenu {

    private fun onSelected(id: String) {
        items[id]?.let {
            App.showContent(it)
        }
    }

    private val String.camelCase: String
        get() {
            return split(' ').joinToString("") { it.replaceFirstChar { it.uppercase() } }
                .replaceFirstChar { it.lowercase() }
        }

    /**
     * Create a main menu entry
     * @param title The title of the menu content
     * @param contentComponent The React component to render the content
     * @param headerComponent An optional react component to render in the Navbar
     * @param refresher A function to refresh an already showing content component
     */

    class MainMenuItem(
        title: String,
        val contentComponent: KClass<out Content<in ContentProps, out State>>? = null,
        val refresher: (() -> Unit)? = null
    ) :
        MenuItemTemplate(
            title.camelCase,
            title,
            true,
            title.camelCase + ".svg",
            { onSelected(it) }
        )

    val menu = listOf(
        MenuGroup(
            "Video",
            listOf(
                MainMenuItem("Recordings", Recordings::class) { App.refreshAll() },
                MainMenuItem("Live view"),
            )
        ),
        MenuGroup(
            "Configuration",
            listOf(
                MainMenuItem("Cameras"),
                MainMenuItem("Storage"),
                MainMenuItem("Users"),
            )
        )
    )

    private val items = menu.map { it.items }.flatten().map { it.menuId to it }.toMap()
}
