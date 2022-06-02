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

import kotlinx.css.Display
import kotlinx.css.Position
import kotlinx.css.display
import kotlinx.css.height
import kotlinx.css.marginLeft
import kotlinx.css.pct
import kotlinx.css.position
import kotlinx.css.width
import org.moonglass.ui.api.Api
import org.moonglass.ui.utility.StateVar
import react.Props
import react.RBuilder
import react.RComponent
import react.State
import styled.css
import styled.styledDiv

external interface ContentProps : Props {
    var api: Api
    var isSideBarShowing: StateVar<Boolean>
}

abstract class Content<P : ContentProps, S : State>(props: P) : RComponent<P, S>(props) {

    abstract fun RBuilder.renderNavBarWidget()

    abstract val title: String

    open fun RBuilder.renderNavBar() {
        child(NavBar::class) {
            attrs {
                api = props.api
                isSideBarShowing = props.isSideBarShowing
                renderWidget = { it.renderNavBarWidget() }
            }
        }
    }

    /**
     * Subclasses should override, always calling renderNavBar first.
     */
    override fun RBuilder.render() {
        renderNavBar()
        styledDiv {
            css {
                marginLeft = ResponsiveLayout.sideBarReserve
                height = 100.pct
                width = 100.pct
                display = Display.block
                position = Position.relative
            }
        }
    }
}

