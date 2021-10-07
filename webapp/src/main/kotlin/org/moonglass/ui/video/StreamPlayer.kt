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

package org.moonglass.ui.video

import kotlinx.css.LinearDimension
import kotlinx.css.PointerEvents
import kotlinx.css.Position
import kotlinx.css.TextAlign
import kotlinx.css.backgroundColor
import kotlinx.css.color
import kotlinx.css.height
import kotlinx.css.left
import kotlinx.css.opacity
import kotlinx.css.padding
import kotlinx.css.pct
import kotlinx.css.pointerEvents
import kotlinx.css.position
import kotlinx.css.properties.transition
import kotlinx.css.px
import kotlinx.css.rem
import kotlinx.css.right
import kotlinx.css.textAlign
import kotlinx.css.top
import kotlinx.css.width
import kotlinx.css.zIndex
import kotlinx.html.js.onChangeFunction
import kotlinx.html.js.onMouseOverFunction
import kotlinx.html.js.onTouchStartFunction
import org.moonglass.ui.ResponsiveLayout
import org.moonglass.ui.Theme
import org.moonglass.ui.ZIndex
import org.moonglass.ui.api.Api
import org.moonglass.ui.applyState
import org.moonglass.ui.useColorSet
import org.moonglass.ui.utility.StateValue
import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLSelectElement
import org.w3c.dom.HTMLVideoElement
import react.Props
import react.RBuilder
import react.RComponent
import react.State
import react.createRef
import react.dom.attrs
import react.dom.onMouseLeave
import react.dom.option
import styled.css
import styled.styledDiv
import styled.styledSelect
import styled.styledVideo

external interface StreamPlayerProps : Props {
    var height: LinearDimension
    var playerKey: String
    var source: StateValue<String>
    var overlay: Boolean                    // should the selector overlay the video?
    var streamSource: StreamSource
}

external interface StreamPlayerState : State {
    var isSelectorShowing: Boolean
    var wasTouched: Boolean         // set if we saw a touch event
}

class StreamPlayer(props: StreamPlayerProps) : RComponent<StreamPlayerProps, StreamPlayerState>(props) {


    /**
     * A hook for the video element.
     */

    private val videoRef = createRef<HTMLVideoElement>()


    override fun componentWillUnmount() {
        props.streamSource.close()
    }

    override fun StreamPlayerState.init(props: StreamPlayerProps) {
        isSelectorShowing = !props.overlay
        wasTouched = false
    }

    private val selectorRef = createRef<HTMLElement>()

    // force play after update
    override fun componentDidUpdate(prevProps: StreamPlayerProps, prevState: StreamPlayerState, snapshot: Any) {
        videoRef.current?.play()
    }

    override fun RBuilder.render() {
        val stream = Api.allStreams[props.source.value]
        val videoHeight: LinearDimension
        val selectorPosition: Position
        val selectorOpacity: Double
        if (props.overlay) {
            videoHeight = props.height - 3.rem
            selectorPosition = Position.absolute
            selectorOpacity = if (state.isSelectorShowing) 1.0 else 0.0
        } else {
            videoHeight = props.height
            selectorPosition = Position.relative
            selectorOpacity = 1.0
        }

        styledDiv {
            css {
                width = 100.pct
                height = props.height
                position = Position.relative
            }
            attrs {
                // these events are used to show and hide the selector overlay
                onMouseOverFunction = { applyState { isSelectorShowing = true } }
                onMouseLeave = { applyState { isSelectorShowing = !props.overlay } }
                if (props.overlay)
                    onTouchStartFunction = {
                        applyState { wasTouched = true; isSelectorShowing = !isSelectorShowing }
                    }
            }
            styledSelect {
                ref = selectorRef
                css {
                    position = selectorPosition
                    opacity = selectorOpacity
                    width = 100.pct
                    height = 3.rem
                    left = 0.px
                    top = 0.px
                    right = 0.px
                    textAlign = TextAlign.center
                    backgroundColor = Theme().header.backgroundColor
                    color = Theme().header.textColor
                    padding(0.5.rem)
                    zIndex = ZIndex.Content.index + 2
                    transition("all", ResponsiveLayout.menuTransitionTime)
                    // disable hover effects on touch devices because hover gets applied stickily.
                    if (!state.wasTouched) {
                        hover {
                            opacity = 1.0
                        }
                    }
                }
                attrs {
                    value = props.source.value
                    onChangeFunction = {
                        val value = it.currentTarget.unsafeCast<HTMLSelectElement>().value
                        applyState {
                            props.source.value = value
                        }
                    }

                }
                option {
                    attrs {
                        value = ""
                    }
                    +"Select stream"
                }
                Api.allStreams.forEach {
                    option {
                        attrs {
                            value = it.key
                        }
                        +it.value.toString()
                    }
                }
            }

            styledVideo {
                ref = videoRef
                css {
                    useColorSet(Theme().content)
                    width = 100.pct
                    height = videoHeight
                    position = Position.relative
                    zIndex = ZIndex.Content.index
                    pointerEvents = PointerEvents.none
                }
                attrs {
                    set("muted", true)
                    key = props.playerKey
                    autoPlay = true
                    autoBuffer = true
                    controls = false
                    poster = "/images/placeholder.jpg"
                    stream?.let {
                        props.streamSource.getSrcUrl(it)
                    }?.let {
                        src = it
                    }
                }
            }
        }
    }

    companion object {
        const val MAX_BUFFER = 10
    }
}
