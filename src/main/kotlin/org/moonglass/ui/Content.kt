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

import kotlinx.html.DIV
import org.moonglass.ui.api.Api
import org.moonglass.ui.utility.StateVar
import react.Props
import react.RBuilder
import react.RComponent
import react.State
import react.dom.option
import styled.StyledDOMBuilder
import styled.styledSelect

external interface ContentProps : Props {
    var api: Api
    var isSideBarShowing: StateVar<Boolean>
}

abstract class Content<P : ContentProps, S : State>(props: P) : RComponent<P, S>(props) {

    abstract fun RBuilder.renderNavBarWidget(): Unit

    abstract fun RBuilder.renderContent(): Unit

    override fun RBuilder.render() {
        child(NavBar::class) {
            attrs {
                api = props.api
                isSideBarShowing = props.isSideBarShowing
                renderWidget = { it.renderNavBarWidget() }
            }
        }
        renderContent()
    }
}

