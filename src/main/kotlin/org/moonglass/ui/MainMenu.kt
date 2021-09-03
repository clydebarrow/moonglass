package org.moonglass.ui

import react.setState

object MainMenu {

    private fun onSelected(id: String) {
        items[id]?.let {
            App.instance?.setState {
                requestedContent = it
            }
        }
    }

    private val String.camelCase: String
        get() {
            return split(' ').joinToString("") { it.replaceFirstChar { it.uppercase() } }
                .replaceFirstChar { it.lowercase() }
        }

    class MainMenuItem(title: String) :
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
                MainMenuItem("Recordings"),
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
