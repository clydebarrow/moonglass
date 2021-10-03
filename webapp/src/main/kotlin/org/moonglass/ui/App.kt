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

import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.css.Display
import kotlinx.css.FlexDirection
import kotlinx.css.Position
import kotlinx.css.display
import kotlinx.css.flexDirection
import kotlinx.css.flexGrow
import kotlinx.css.height
import kotlinx.css.position
import kotlinx.css.vh
import kotlinx.css.vw
import kotlinx.css.width
import kotlinx.css.zIndex
import org.moonglass.ui.api.Api
import org.moonglass.ui.utility.SavedState
import org.moonglass.ui.utility.StateVar
import org.moonglass.ui.widgets.Dialog
import org.moonglass.ui.widgets.LiveStats
import org.moonglass.ui.widgets.Spinner
import org.moonglass.ui.widgets.Toast
import react.Props
import react.RBuilder
import react.RComponent
import react.State
import react.setState
import styled.css
import styled.styledDiv
import kotlin.reflect.KClass


external interface AppState : State {
    var refreshing: MutableSet<String>
    var contentShowing: MainMenu.MainMenuItem
    var dialogShowing: KClass<out Dialog>?
    var dismissable: Boolean
    var isSideBarShowing: StateVar<Boolean>
    var isContextMenuShowing: StateVar<Boolean>
    var isLiveStatsShowing: StateVar<Boolean>
    var contextMenuData: ContextMenuData
    var api: Api
}

@JsExport
class App() : RComponent<Props, AppState>() {

    private fun restoreShowing(): MainMenu.MainMenuItem {
        val old: String? = SavedState.restore(appComponentKey)
        return MainMenu.getItem(old)
    }

    override fun AppState.init() {
        instance = this@App
        refreshing = mutableSetOf()
        contentShowing = restoreShowing()
        isSideBarShowing = StateVar(false, this@App)
        isLiveStatsShowing = StateVar(false, this@App)
        isContextMenuShowing = StateVar(false, this@App)
        contextMenuData = ContextMenuData()
        api = Api()
        refreshList()
    }

    override fun componentDidMount() {
        SavedState.addOnUnload(::saveMyStuff)
        window.addEventListener("resize", {
            forceUpdate()
        })
    }

    override fun componentWillUnmount() {
        saveMyStuff()
        SavedState.removeOnUnload(::saveMyStuff)
    }

    private fun saveMyStuff() {
        SavedState.save(appComponentKey, state.contentShowing.menuId)
    }

    private fun refreshList() {
        MainScope().launch {
            Api.fetchApi()?.let { list ->
                applyState {
                    api = list
                }
            }
        }
    }

    override fun RBuilder.render() {
        styledDiv {
            css {
                if (Theme().isDark)
                    put("color-scheme", "dark")
                position = Position.relative
                flexGrow = 1.0
                display = Display.flex
                flexDirection = FlexDirection.row
                width = 100.vw
                height = 100.vh
                zIndex = ZIndex.Content()
            }
            child(SideBar::class) {
                attrs {
                    isSideBarShowing = state.isSideBarShowing
                }
            }
            state.contentShowing.apply {
                contentComponent?.also {
                    child(it) {
                        attrs {
                            api = state.api
                            isSideBarShowing = state.isSideBarShowing
                        }
                    }
                } ?: +title
            }
        }
        // Context menu
        child(Menu::class) {
            attrs {
                groups = state.contextMenuData.groups
                style = state.contextMenuData.style
                isShowing = state.isContextMenuShowing
            }
        }
        child(Toast::class) { attrs { } }
        child(LiveStats::class) {
            attrs {
                isShowing = state.isLiveStatsShowing
            }
        }
        state.dialogShowing?.let {
            child(it) {
                attrs {
                    doDismiss = { applyState { dialogShowing = null } }
                }
            }
        }
        // show a spinner if we are refreshing
        if (state.refreshing.isNotEmpty())
            child(Spinner::class) {}
    }

    companion object {

        // The app
        var instance: App? = null


        const val appComponentKey = "appComponentKey"
        // get the current camera list

        val session: Api.Session?
            get() = instance?.state?.api?.session


        fun refreshAll() {
            instance?.refreshList()
        }

        fun clearApiData() {
            instance?.apply {
                setState { api = Api() }
            }
        }

        val selectedItemId: String? get() = instance?.state?.contentShowing?.menuId

        fun showContextMenu(data: ContextMenuData) {
            instance?.let {
                it.state.isContextMenuShowing.value = false
                it.setState {
                    contextMenuData = data
                }
                it.state.isContextMenuShowing.value = true
            }
        }

        fun showContent(item: MainMenu.MainMenuItem) {
            console.log("showContent ${item.title}")
            instance?.apply {
                // if we are already showing  this content, just refresh it.
                if (state.contentShowing == item) {
                    state.isSideBarShowing.value = false
                    item.refresher?.invoke()
                } else {
                    state.isSideBarShowing.value = false
                    setState {
                        contentShowing = item
                    }
                    item.refresher?.invoke()
                }
            }
        }

        var showSideBar: Boolean
            get() = instance?.state?.isSideBarShowing?.value == true
            set(value) {
                instance?.state?.isSideBarShowing?.value = value
            }

        /**
         * Control showing of the status overlay
         */

        var showLiveStatus: Boolean
            get() = instance?.state?.isLiveStatsShowing?.value == true
            set(value) {
                instance?.state?.isLiveStatsShowing?.value = value
            }

        /**
         * Show a modal dialog
         */

        fun showDialog(dialog: KClass<out Dialog>, dismissable: Boolean = true) {
            instance?.setState {
                dialogShowing = dialog
                this.dismissable = dismissable
            }
        }

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


        fun render() {
            react.dom.render(document.getElementById("root")) {
                child(App::class) { }
            }
        }
    }
}

