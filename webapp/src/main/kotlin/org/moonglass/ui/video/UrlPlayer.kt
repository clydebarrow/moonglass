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

import kotlinx.css.LinearDimension
import kotlinx.css.height
import kotlinx.css.pct
import kotlinx.css.rem
import kotlinx.css.width
import org.moonglass.ui.ResponsiveLayout
import org.moonglass.ui.Theme
import org.moonglass.ui.useColorSet
import react.Props
import react.RBuilder
import react.RComponent
import react.State
import react.dom.attrs
import styled.css
import styled.styledVideo

external interface UrlPlayerProps : Props {
    var height: LinearDimension?
    var playerKey: String
    var showControls: Boolean
    var source: RecordingSource?
}


class UrlPlayer(props: UrlPlayerProps) : RComponent<UrlPlayerProps, State>(props) {
    override fun RBuilder.render() {
        styledVideo {
            css {
                useColorSet(Theme().content)
                width = 100.pct
                height = props.height ?: (ResponsiveLayout.playerHeight - 3.rem)
            }
            attrs {
                key = props.playerKey
                //width = "${state.width}px"
                //height = "${state.height}px"
                autoPlay = true
                autoBuffer = true
                controls = props.showControls
                poster = "/images/placeholder.jpg"
                props.source?.also { videoSource ->
                    src = videoSource.srcUrl
                }
            }
        }
    }
}
