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

package org.moonglass.ui.video

import kotlinx.css.Color
import kotlinx.css.Display
import kotlinx.css.FlexDirection
import kotlinx.css.JustifyItems
import kotlinx.css.TextAlign
import kotlinx.css.backgroundColor
import kotlinx.css.display
import kotlinx.css.flexDirection
import kotlinx.css.flexGrow
import kotlinx.css.fontSize
import kotlinx.css.height
import kotlinx.css.justifyItems
import kotlinx.css.pct
import kotlinx.css.rem
import kotlinx.css.textAlign
import kotlinx.css.width
import react.Props
import react.RBuilder
import react.RComponent
import react.State
import react.dom.attrs
import styled.css
import styled.styledDiv
import styled.styledVideo

external interface PlayerProps : Props {
    var source: VideoSource?
    var availableHeight: Int
    var availableWidth: Int
}

external interface PlayerState : State {
}

class Player(props: PlayerProps) : RComponent<PlayerProps, PlayerState>(props) {

    override fun RBuilder.render() {
        styledDiv {
            css {
                display = Display.flex
                flexDirection = FlexDirection.column
                justifyItems = JustifyItems.center
                width = 100.pct
                height = 100.pct
            }

            styledDiv {
                css {
                    fontSize = 1.2.rem
                    textAlign = TextAlign.center
                    backgroundColor = Color.lightGray
                }
                +(props.source?.caption ?: "---")
            }

            styledVideo {
                css {
                    display = Display.flex
                    flexGrow = 1.0
                    /*
                    width = state.width.px - 1.5.rem        // allow for padding
                    height = state.height.px - 1.5.rem        // allow for padding

                     */
                    width = 100.pct
                    height = 100.pct
                }
                attrs {
                    //width = "${state.width}px"
                    //height = "${state.height}px"
                    autoPlay = true
                    autoBuffer = true
                    controls = true
                    props.source.also {
                        if (it != null)
                            src = it.srcUrl
                        else
                            poster = "/images/placeholder.jpg"
                    }
                }
            }
        }
    }
}
