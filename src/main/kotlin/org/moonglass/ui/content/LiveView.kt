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

import io.ktor.http.ContentType
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
import kotlinx.css.JustifyItems
import kotlinx.css.alignItems
import kotlinx.css.display
import kotlinx.css.flexDirection
import kotlinx.css.gridColumnEnd
import kotlinx.css.gridColumnStart
import kotlinx.css.gridRowEnd
import kotlinx.css.gridRowStart
import kotlinx.css.gridTemplateColumns
import kotlinx.css.gridTemplateRows
import kotlinx.css.height
import kotlinx.css.justifyContent
import kotlinx.css.justifyItems
import kotlinx.css.margin
import kotlinx.css.padding
import kotlinx.css.paddingTop
import kotlinx.css.pct
import kotlinx.css.rem
import kotlinx.css.width
import kotlinx.css.zIndex
import kotlinx.serialization.Serializable
import org.moonglass.ui.Content
import org.moonglass.ui.ContentProps
import org.moonglass.ui.ResponsiveLayout
import org.moonglass.ui.ZIndex
import org.moonglass.ui.name
import org.moonglass.ui.utility.SavedState
import org.moonglass.ui.video.LiveSource
import org.moonglass.ui.video.Player
import org.moonglass.ui.video.VideoSource
import org.moonglass.ui.widgets.recordings.Stream
import org.moonglass.ui.widgets.recordings.streamFor
import org.w3c.dom.HTMLSelectElement
import react.RBuilder
import react.State
import react.dom.attrs
import react.dom.onChange
import react.dom.option
import styled.css
import styled.styledDiv
import styled.styledOption
import styled.styledSelect


class LiveView(props: ContentProps) : Content<ContentProps, LiveViewState>(props) {
    override fun RBuilder.renderNavBarWidget() {
        styledDiv {
            +"Layout:"
            css {
                margin(left = 1.rem, right = 1.rem)
            }
        }
        styledSelect {
            attrs {
                value = state.layoutState.current
                onChange = { state.layoutState.current = (it.currentTarget as HTMLSelectElement).value }
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
        styledDiv {
            state.layoutState.layout.arrangement.let { arrangement ->
                css {
                    justifyContent = JustifyContent.center
                    gridTemplateColumns = GridTemplateColumns((1..arrangement.columns).joinToString(" ") { "1fr" })
                    gridTemplateRows = GridTemplateRows((1..arrangement.rows).joinToString(" ") { "1fr" })
                    alignItems = Align.center
                    padding(0.25.rem)
                    display = Display.grid
                    zIndex = ZIndex.Content()
                    width = 100.pct
                    height = 100.pct
                    paddingTop = ResponsiveLayout.navBarEmHeight
                }
                name = "RecordingsContent"
                repeat(arrangement.totalPlayers) { index ->
                    styledDiv {
                        css {
                            display = Display.flex
                            flexDirection = FlexDirection.column
                            justifyItems = JustifyItems.center
                            width = 100.pct
                            height = 100.pct
                            if (index == 0 && arrangement.feature) {
                                gridColumnStart = GridColumnStart("1")
                                gridColumnEnd = GridColumnEnd("${arrangement.columns - 1}")
                                gridRowStart = GridRowStart("1")
                                gridRowEnd = GridRowEnd("${arrangement.rows - 1}")
                            }
                        }
                        styledSelect {
                            

                        }
                        val sourceKey = state.layoutState.layouts[index].key
                        val stream = props.api.streamFor(sourceKey)

                        child(Player::class) {
                            attrs {
                                if(stream != null)
                                    source = LiveSource(stream.wsUrl, stream.toString())
                            }
                        }
                    }
                }
            }
        }
    }

    @Serializable
    enum class Arrangement(val title: String, val columns: Int, val rows: Int, val feature: Boolean = false) {
        Single("Single", 1, 2),
        L1x2("1x2", 1, 2),
        L2x2("2x2", 2, 2),
        L2x3("2x3", 2, 3),
        L1x4("1x4", 1, 4),
        L1p5("1+5", 3, 3, true);

        val layout: Layout get() = Layout(title, "_$name", this, listOf())
        val totalPlayers: Int get() = if (feature) rows + columns else rows * columns
    }

    @Serializable
    data class Layout(val name: String, val key: String, val arrangement: Arrangement, val sources: List<String>)

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
