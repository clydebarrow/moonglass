package org.moonglass.ui

import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.css.Display
import kotlinx.css.FlexDirection
import kotlinx.css.display
import kotlinx.css.flexDirection
import kotlinx.css.flexGrow
import kotlinx.css.height
import kotlinx.css.pct
import kotlinx.css.vh
import kotlinx.css.width
import org.moonglass.ui.widgets.Spinner
import org.moonglass.ui.widgets.Toast
import react.Props
import react.RBuilder
import react.RComponent
import react.State
import react.setState
import styled.css
import styled.styledDiv


external interface AppState : State {
    var toastMessage: String
    var toastUrgency: Toast.Urgency
    var toastState: Toast.State
    var refreshing: MutableSet<String>
    var contentShowing: MainMenu.MainMenuItem
}

@JsExport
class App() : RComponent<Props, AppState>() {

    override fun AppState.init() {
        refreshing = mutableSetOf()
        toastMessage = ""
        toastUrgency = Toast.Urgency.Normal
        toastState = Toast.State.Hidden
        contentShowing = MainMenu.menu.first().items.first()
    }

    override fun componentDidMount() {
        instance = this
        window.addEventListener("resize", {
            forceUpdate()
        })
    }

    override fun componentWillUnmount() {
        instance = null
    }

    override fun RBuilder.render() {
        styledDiv {
            name = "App"
            css {
                display = Display.flex
                flexDirection = FlexDirection.column
                width = 100.pct
                height = 100.vh
            }
            child(NavBar::class) {
                attrs {
                    headerComponent = state.contentShowing.headerComponent
                }
            }
            styledDiv {
                css {
                    flexGrow = 1.0
                    display = Display.flex
                    flexDirection = FlexDirection.row
                    //marginTop = ResponsiveLayout.navBarEmHeight.rem
                    width = 100.pct
                }
                if (ResponsiveLayout.showSideMenu)
                    child(SideBar::class) {}
                child(Content::class) {
                    attrs {
                        contentShowing = state.contentShowing
                    }
                }
            }
        }
        child(Toast::class) { attrs { } }
        // show a spinner if we are refreshing
        if (state.refreshing.isNotEmpty())
            child(Spinner::class) {}
    }

    companion object {

        var instance: App? = null

        /**
         * Tell the app that a long-running operation is going on, or has completed.
         * @param key A key to identify the operation
         * @param active True to show the spinner, false to hide it.
         */
        fun setRefresh(key: String, active: Boolean) {
            instance?.setState {
                if (active)
                    refreshing.add(key)
                else
                    refreshing.remove(key)
            }
        }

        val selectedItemId: String? get() = instance?.state?.contentShowing?.menuId

        fun showContent(item: MainMenu.MainMenuItem) {
            instance?.apply {
                if (item.contentComponent == null)
                    Toast.toast("No content implemented for ${item.title}")
                setState {
                    contentShowing = item
                }
            }
        }
        fun render() {
            react.dom.render(document.getElementById("root")) {
                child(App::class) { }
            }
        }
    }
}

