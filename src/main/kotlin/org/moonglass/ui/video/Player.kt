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

import kotlinx.css.Display
import kotlinx.css.display
import kotlinx.css.flexGrow
import kotlinx.css.height
import kotlinx.css.pct
import kotlinx.css.width
import react.Props
import react.RBuilder
import react.RComponent
import react.State
import react.dom.attrs
import react.dom.onPause
import react.dom.onPlay
import styled.css
import styled.styledVideo

external interface PlayerProps : Props {
    var source: VideoSource?
}

external interface PlayerState : State {
}

class Player(props: PlayerProps) : RComponent<PlayerProps, PlayerState>(props) {

    override fun RBuilder.render() {
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
                poster = "/images/placeholder.jpg"
                props.source?.also { videoSource ->
                    src = videoSource.srcUrl
                    onPlay = { videoSource.play() }
                    onPause = { videoSource.pause() }
                }
            }
        }
    }
}
