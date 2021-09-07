package org.moonglass.ui.user

import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.html.InputType
import org.moonglass.ui.App
import org.moonglass.ui.MenuGroup
import org.moonglass.ui.MenuItemTemplate
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

    class LoginDialog : Dialog() {


        override val title: String = "Login required"
        override val okText: String = "Submit"

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
