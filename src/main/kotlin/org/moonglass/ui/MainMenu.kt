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

import org.moonglass.ui.content.LiveView
import org.moonglass.ui.content.Recordings
import react.State
import kotlin.reflect.KClass

object MainMenu {

    private fun onSelected(id: String) {
        getItem(id)?.let {
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
                MainMenuItem("Live view", LiveView::class),
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

    val default get() = menu.first().items.first()

    fun getItem(id: String): MainMenuItem? {
        return items[id]
    }
}
