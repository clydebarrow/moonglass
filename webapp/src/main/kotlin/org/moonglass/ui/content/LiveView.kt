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
import kotlinx.css.FlexDirection
import kotlinx.css.GridColumnEnd
import kotlinx.css.GridColumnStart
import kotlinx.css.GridRowEnd
import kotlinx.css.GridRowStart
import kotlinx.css.GridTemplateColumns
import kotlinx.css.GridTemplateRows
import kotlinx.css.JustifyContent
import kotlinx.css.TextAlign
import kotlinx.css.alignContent
import kotlinx.css.backgroundColor
import kotlinx.css.color
import kotlinx.css.display
import kotlinx.css.flexDirection
import kotlinx.css.gridColumnEnd
import kotlinx.css.gridColumnStart
import kotlinx.css.gridRowEnd
import kotlinx.css.gridRowStart
import kotlinx.css.gridTemplateColumns
import kotlinx.css.gridTemplateRows
import kotlinx.css.justifyContent
import kotlinx.css.margin
import kotlinx.css.maxHeight
import kotlinx.css.maxWidth
import kotlinx.css.padding
import kotlinx.css.paddingTop
import kotlinx.css.pct
import kotlinx.css.rem
import kotlinx.css.textAlign
import kotlinx.css.vh
import kotlinx.css.width
import kotlinx.css.zIndex
import kotlinx.html.js.onChangeFunction
import kotlinx.serialization.Serializable
import org.moonglass.ui.Content
import org.moonglass.ui.ContentProps
import org.moonglass.ui.ResponsiveLayout
import org.moonglass.ui.Theme
import org.moonglass.ui.ZIndex
import org.moonglass.ui.applyState
import org.moonglass.ui.name
import org.moonglass.ui.utility.SavedState
import org.moonglass.ui.video.LivePlayer
import org.moonglass.ui.video.LiveSourceFactory
import org.w3c.dom.HTMLSelectElement
import react.RBuilder
import react.State
import react.dom.attrs
import react.dom.option
import styled.css
import styled.styledDiv
import styled.styledSelect


/**
 * Display a tiled view of multiple live sources.
 */
class LiveView(props: ContentProps) : Content<ContentProps, LiveViewState>(props) {

    // a cache of sources to avoid recreating these on render.

    override fun RBuilder.renderNavBarWidget() {
        styledDiv {
            +"Layout:"
            css {
                margin(left = 1.rem, right = 1.rem)
            }
        }
        styledSelect {
            css {
                backgroundColor = Theme().content.backgroundColor
                color = Theme().content.textColor
            }
            attrs {
                value = state.layoutState.current
                onChangeFunction = {
                    val value = it.currentTarget.unsafeCast<HTMLSelectElement>().value
                    applyState {
                        layoutState.current = value
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

    override fun LiveViewState.init(props: ContentProps) {
        layoutState = SavedState.restore(dataKey) ?: LayoutState()
    }

    private fun saveMyStuff() {
        SavedState.save(dataKey, state.layoutState)
    }

    override fun componentDidMount() {
        SavedState.addOnUnload(::saveMyStuff)
    }

    override fun componentWillUnmount() {
        saveMyStuff()
        SavedState.removeOnUnload(::saveMyStuff)
    }

    override fun RBuilder.renderContent() {
        val allStreams = props.api.allStreams
        styledDiv {
            state.layoutState.layout.let { layout ->
                val arrangement = layout.arrangement
                val vFrac = (100.0 / arrangement.rows).pct
                val hFrac = (100.0 / arrangement.columns).pct
                val pHeight = (100.vh - ResponsiveLayout.navBarEmHeight) / arrangement.rows - 3.rem
                // height for player 0 in
                val pHeight0 = if (arrangement.feature)
                    pHeight * (arrangement.rows - 1)
                else
                    pHeight
                css {
                    justifyContent = JustifyContent.center
                    gridTemplateRows = GridTemplateRows((1..arrangement.rows).map { vFrac }.joinToString(" "))
                    gridTemplateColumns = GridTemplateColumns((1..arrangement.columns).map { hFrac }.joinToString(" "))
                    display = Display.grid
                    padding(0.25.rem)
                    zIndex = ZIndex.Content()
                    width = 100.pct
                    maxHeight = 100.pct
                    paddingTop = ResponsiveLayout.navBarEmHeight
                    backgroundColor = Theme().content.backgroundColor
                }
                name = "playersContent"
                repeat(arrangement.totalPlayers) { index ->
                    styledDiv {
                        css {
                            maxHeight = 100.pct
                            maxWidth = 100.pct
                            display = Display.flex
                            flexDirection = FlexDirection.column
                            alignContent = Align.center
                            padding(0.5.rem)
                            if (index == 0 && arrangement.feature) {
                                gridColumnStart = GridColumnStart("1")
                                gridColumnEnd = GridColumnEnd("${arrangement.columns}")
                                gridRowStart = GridRowStart("1")
                                gridRowEnd = GridRowEnd("${arrangement.rows}")
                            }
                        }
                        val sourceKey = layout.sources[index]
                        val stream = allStreams[sourceKey]
                        styledSelect {
                            css {
                                textAlign = TextAlign.center
                                backgroundColor = Theme().header.backgroundColor
                                color = Theme().header.textColor
                                padding(0.5.rem)
                            }
                            attrs {
                                value = sourceKey
                                onChangeFunction = {
                                    val value = it.currentTarget.unsafeCast<HTMLSelectElement>().value
                                    applyState {
                                        layoutState.layout.sources[index] = value
                                    }
                                }
                            }
                            option {
                                attrs {
                                    value = ""
                                }
                                +"Select stream"
                            }
                            allStreams.forEach {
                                option {
                                    attrs {
                                        value = it.key
                                    }
                                    +it.value.toString()
                                }
                            }
                        }

                        child(LivePlayer::class) {
                            attrs {
                                playerKey = "live-player-$index"
                                source = stream?.let { LiveSourceFactory.getSource(it) }
                                showControls = true
                                height = if (index == 0) pHeight0 else pHeight
                            }
                        }
                    }
                }
            }
        }
    }

    @Serializable
    enum class Arrangement(val title: String, val columns: Int, val rows: Int, val feature: Boolean = false) {
        Single("Single", 1, 1),
        L1x2("1x2", 1, 2),
        L2x2("2x2", 2, 2),
        L2x3("2x3", 2, 3),
        L1x4("1x4", 1, 4),
        L1p5("1+5", 3, 3, true);

        val layout: Layout get() = Layout(title, "_$name", this, MutableList(totalPlayers) { "" })
        val totalPlayers: Int get() = if (feature) rows + columns else rows * columns
    }

    @Serializable
    data class Layout(val name: String, val key: String, val arrangement: Arrangement, val sources: MutableList<String>)

    @Serializable
    data class LayoutState(
        val layouts: List<Layout> = Arrangement.values().map { it.layout },
        var current: String = layouts.first().key
    ) {
        val layout get() = layouts.first { it.key == current }
    }

    companion object {
        const val dataKey = "liveViewLayouts"


    }
}

external interface LiveViewState : State {
    var layoutState: LiveView.LayoutState
}
