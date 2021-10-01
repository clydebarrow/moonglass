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

package org.moonglass.ui.user

import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.html.InputType
import org.moonglass.ui.App
import org.moonglass.ui.MenuGroup
import org.moonglass.ui.MenuItemTemplate
import org.moonglass.ui.ModalProps
import org.moonglass.ui.api.Api
import org.moonglass.ui.widgets.Dialog
import org.moonglass.ui.widgets.Toast

object User {

    val userLoggedIn get() = App.session?.let { "Logged in as ${it.username}" } ?: "Not logged in"
    fun action(id: String) {
        console.log("Action: $id")
    }

    fun showLoginDialog() {
        App.showDialog(LoginDialog::class)
    }

    val menu = listOf(
        MenuItemTemplate("editProfile", "Edit profile", true, "profile.svg") {
            action(it)
        },
        MenuItemTemplate("userPreferences", "Preferences", true, "settings.svg") {
            App.showDialog(UserPreferences.PreferencesDialog::class)
        },
        object : MenuItemTemplate("logout", "Log out", true, "logout.svg", {
            App.session?.csrf?.let {
                MainScope().launch {
                    Api.logout(it)
                }
            }
        }) {
            override val enabled: Boolean
                get() = App.session != null
        }
    )

    val group get() = MenuGroup(userLoggedIn, menu)

    class LoginDialog(props: ModalProps) : Dialog(props) {

        override val dismissOutside: Boolean = false        // only buttons will close

        override val title: String = "Login required"
        override val okText: String = "Login"

        override fun onSubmit() {
            MainScope().launch {
                if (Api.login(username.value, password.value)) {
                    Toast.toast("Login successful")
                    props.doDismiss()
                    App.refreshAll()
                }
            }
        }

        override fun validate(): Boolean = username.value.isNotBlank() && password.value.isNotBlank()

        private val username = Entry("Username", InputType.text)
        private val password = Entry("Password", InputType.password)
        override val items: List<Entry> = listOf(
            username,
            password
        )

    }
}
