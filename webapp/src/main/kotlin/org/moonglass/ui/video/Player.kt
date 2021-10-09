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

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
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

external interface PlayerProps : Props {
    var height: LinearDimension
    var playerKey: String
    var source: StateValue<String>          // the stream key, updatable
    var overlay: Boolean                    // should the selector overlay the video?
}

external interface PlayerState : State {
    var isSelectorShowing: Boolean
    var wasTouched: Boolean         // set if we saw a touch event
    var srcUrl: String?
    var currentSource: String?      // updated to equal props.source after srcUrl updated
}

abstract class Player<P : PlayerProps, S: PlayerState>(props: P) : RComponent<P, S>(props) {

    private var job: Job? = null

    abstract val streamSource: StreamSource     // where we get our video from

    protected lateinit var scope: CoroutineScope

    /**
     * A hook for the video element.
     */

    private val videoRef = createRef<HTMLVideoElement>()
    private val selectorRef = createRef<HTMLElement>()
    override fun componentWillUnmount() {
        console.log("Player: willUnmount: source = ${props.source()}")
        streamSource.close()
        scope.cancel()
    }


    override fun S.init(props: P) {
        scope = MainScope()
        isSelectorShowing = !props.overlay
        wasTouched = false
        srcUrl = null
        currentSource = null
        // defer updating until after init complete
        scope.launch {
            updateSrcUrl()      // must call here, and in afterUpdate
        }
    }

    override fun componentDidMount() {
        // in case of remount.
        console.log("Player: didMount source = ${props.source()}")
        if (state.srcUrl != null)
            videoRef.current?.play()?.catch { }        // start play after state update
    }


    // asynchronously get the source url and update if required.
    override fun componentDidUpdate(prevProps: P, prevState: S, snapshot: Any) {
        if (props.source().isNotBlank() && state.currentSource != props.source())
            updateSrcUrl()
    }

    protected open fun updateSrcUrl() {
        if(job?.isActive == true) {
            console.log("job still running")
            return
        }
        val newSource = props.source()
        console.log("Player: updateSrcIrl called, source = $newSource")
        Api.allStreams[newSource]?.let { source ->
            job = scope.launch {
                val url = streamSource.getSrcUrl(source)
                console.log("srcUrl ${state.srcUrl} becomes $url")
                if (state.srcUrl != url) {
                    applyState({
                        console.log("Starting play for $url")
                        videoRef.current?.play()?.catch { }        // start play after state update
                    }) {
                        currentSource = newSource
                        srcUrl = url
                    }
                }
            }
        }
    }

    override fun RBuilder.render() {
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
                            console.log("source value changes to $value")
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
                key = props.playerKey
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
                    autoPlay = true
                    autoBuffer = true
                    controls = false
                    poster = "/images/placeholder.jpg"
                    state.srcUrl?.let { src = it }
                }
            }
        }
    }
}
