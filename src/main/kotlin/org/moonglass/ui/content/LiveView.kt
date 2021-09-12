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

package org.moonglass.ui.content

import kotlinx.css.Align
import kotlinx.css.Display
import kotlinx.css.GridTemplateColumns
import kotlinx.css.JustifyContent
import kotlinx.css.alignItems
import kotlinx.css.display
import kotlinx.css.gridTemplateColumns
import kotlinx.css.height
import kotlinx.css.justifyContent
import kotlinx.css.padding
import kotlinx.css.paddingTop
import kotlinx.css.pct
import kotlinx.css.rem
import kotlinx.css.width
import kotlinx.css.zIndex
import org.moonglass.ui.Content
import org.moonglass.ui.ContentProps
import org.moonglass.ui.NavBar
import org.moonglass.ui.ResponsiveLayout
import org.moonglass.ui.ZIndex
import org.moonglass.ui.name
import org.moonglass.ui.video.Player
import react.RBuilder
import react.State
import react.dom.option
import styled.css
import styled.styledDiv
import styled.styledSelect


class LiveView(props: ContentProps) : Content<ContentProps, LiveViewState>(props) {
    override fun RBuilder.render() {
        child(NavBar::class) {
            attrs {
                api = props.api
                isSideBarShowing = props.isSideBarShowing
                renderWidget = {
                    styledSelect {
                        option(content = "4x4")
                    }
                }
            }
        }
        styledDiv {
            css {
                justifyContent = JustifyContent.center
                gridTemplateColumns = GridTemplateColumns("50% 50%")
                alignItems = Align.center
                padding(0.25.rem)
                display = Display.grid
                zIndex = ZIndex.Content()
                width = 100.pct
                height = 100.pct
                paddingTop = ResponsiveLayout.navBarEmHeight
            }
            name = "RecordingsContent"
            repeat(4) {
                child(Player::class) {}
            }
        }
    }
}

external interface LiveViewState : State
