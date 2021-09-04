package org.moonglass.ui

import org.moonglass.ui.content.Recordings
import react.Props
import react.RComponent
import react.State
import kotlin.reflect.KClass

object MainMenu {

    private fun onSelected(id: String) {
        items[id]?.let {
            Content.requestContent(it)
        }
    }

    private val String.camelCase: String
        get() {
            return split(' ').joinToString("") { it.replaceFirstChar { it.uppercase() } }
                .replaceFirstChar { it.lowercase() }
        }

    class MainMenuItem(title: String, val clazz: KClass<out RComponent<in Props, out State>>? = null) :
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
                MainMenuItem("Recordings", Recordings::class),
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
