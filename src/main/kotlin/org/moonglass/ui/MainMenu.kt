package org.moonglass.ui

import org.moonglass.ui.content.Recordings
import org.moonglass.ui.widgets.recordings.DateTimeSelector
import react.Props
import react.RComponent
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

    class MainMenuItem(
        title: String,
        val contentComponent: KClass<out RComponent<Props, *>>? = null,
        val headerComponent: KClass<out RComponent<Props, *>>? = null
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
                MainMenuItem("Recordings", Recordings::class, DateTimeSelector::class),
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
