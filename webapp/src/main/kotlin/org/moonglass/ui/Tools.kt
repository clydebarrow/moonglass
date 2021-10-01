package org.moonglass.ui

import org.moonglass.ui.user.User

object Tools {
    val menu = listOf(
        MenuItemTemplate("liveStats", "Live stats", true, "livestats.svg") {
            App.showLiveStatus = !App.showLiveStatus
        }
    )

}
