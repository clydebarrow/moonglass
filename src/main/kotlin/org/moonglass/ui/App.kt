package org.moonglass.ui

import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.css.Display
import kotlinx.css.FlexDirection
import kotlinx.css.display
import kotlinx.css.flexDirection
import kotlinx.css.flexGrow
import kotlinx.css.height
import kotlinx.css.marginTop
import kotlinx.css.pct
import kotlinx.css.vh
import kotlinx.css.width
import kotlinx.datetime.Clock
import org.moonglass.ui.utility.Timer
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
    var size: ResponsiveLayout.Size
    var toastMessage: String
    var toastUntil: Long
    var toastUrgency: Toast.Urgency
    var toastState: Toast.State
    var requestedContent: MainMenu.MainMenuItem
    var refreshing: MutableSet<String>
}

external interface AppProps : Props {
    var size: ResponsiveLayout.Size
}

@JsExport
class App(props: AppProps) : RComponent<AppProps, AppState>(props) {

    private val toastTimer = Timer()

    override fun AppState.init(props: AppProps) {
        refreshing = mutableSetOf()
        toastMessage = ""
        toastUntil = 0
        toastUrgency = Toast.Urgency.Normal
        size = props.size
        toastState = Toast.State.Hidden
        requestedContent = MainMenu.menu.first().items.first()
    }

    override fun componentDidMount() {
        window.addEventListener("resize", {
            setState {
                size = ResponsiveLayout.current
            }
        })
        instance = this
    }

    override fun componentWillUnmount() {
        instance = null
    }

    override fun RBuilder.render() {
        ResponsiveLayout.context.Provider(state.size) {
            styledDiv {
                css {
                    display = Display.flex
                    flexDirection = FlexDirection.column
                    width = 100.pct
                    height = 100.vh
                }
                child(NavBar::class) {}
                styledDiv {
                    css {
                        flexGrow = 1.0
                        display = Display.flex
                        flexDirection = FlexDirection.row
                        marginTop = NavBar.barHeight
                        width = 100.pct
                    }
                    if (!state.size.mobile)
                        child(SideBar::class) {}
                    child(Content::class) {
                        attrs {
                            requested = state.requestedContent
                        }
                    }
                }
            }
            child(Toast::class) {
                attrs {
                    message = state.toastMessage
                    urgency = state.toastUrgency
                    displayState = state.toastState
                    if (state.toastState != Toast.State.Hidden) {
                        val delay = (state.toastUntil - Clock.System.now().toEpochMilliseconds()).toInt()
                        if (delay > Toast.fadeoutDuration)
                            toastTimer.start(delay - Toast.fadeoutDuration) {
                                setState { toastState = Toast.State.Fading }
                            } else
                            toastTimer.start(delay) {
                                setState { toastState = Toast.State.Hidden }
                            }
                    } else
                        toastTimer.cancel()
                }
            }
            // show a spinner if we are refreshing
            if (state.refreshing.isNotEmpty())
                child(Spinner::class) {}
        }
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
                if(active)
                    refreshing.add(key)
                else
                    refreshing.remove(key)
            }
        }

        fun render() {
            react.dom.render(document.getElementById("root")) {
                child(App::class) {
                    attrs.size = ResponsiveLayout.current
                }
            }
        }
    }
}

