/*
 *
 *  * Copyright (c) 2021. Clyde Stubbs
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  * http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 *
 */

package org.moonglass.ui.content

import csstype.HtmlAttributes
import kotlinx.css.margin
import kotlinx.css.pct
import kotlinx.css.rem
import kotlinx.html.DIV
import kotlinx.html.js.onChangeFunction
import org.moonglass.ui.ContentProps
import org.moonglass.ui.Theme
import org.moonglass.ui.applyState
import org.moonglass.ui.useColorSet
import org.moonglass.ui.utility.StateValue
import org.moonglass.ui.video.StreamPlayer
import org.w3c.dom.HTMLSelectElement
import react.RBuilder
import react.dom.attrs
import react.dom.option
import styled.StyledDOMBuilder
import styled.css
import styled.styledDiv
import styled.styledSelect


/**
 * Display a tiled view of multiple live sources.
 */
class LiveView(props: ContentProps) : TiledView<TiledViewState>(props) {
    override val title: String = "Live"

    override fun RBuilder.renderNavBarWidget() {
        styledDiv {
            +"Layout:"
            css {
                margin(left = 1.rem, right = 1.rem)
            }
        }
        styledSelect {
            css {
                useColorSet(Theme().content)
            }
            attrs {
                value = state.layoutState.current
                onChangeFunction = {
                    val value = it.currentTarget.unsafeCast<HTMLSelectElement>().value
                    applyState {
                        layoutState.current = value
                        sources = layoutState.layout.getStates(this@LiveView)
                    }
                }
            }
            state.layoutState.layouts.sortedBy { it.name }.forEach {
                option {
                    attrs {
                        value = it.key
                    }
                    +it.name
                }
            }
        }
    }

    override fun StyledDOMBuilder<DIV>.addPlayer(
        key: String,
        stateValue: StateValue<String>
    ) {
        child(StreamPlayer::class) {
            attrs {
                playerKey = key
                source = stateValue
                overlay = true
                offsetSecs = 0.0
            }
        }
    }
}
