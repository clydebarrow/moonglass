package org.moonglass.ui.user

import org.moonglass.ui.MenuItemTemplate

class User {

    companion object {

        fun action(id: String) {
            console.log("Action: $id")
        }

        val menu = listOf(
            MenuItemTemplate("editProfile", "Edit profile", true, "profile.svg") {
                action(it)
            },
            MenuItemTemplate("logout", "Log out", true, "logout.svg") {
                action(it)
            }
        )
    }
}
