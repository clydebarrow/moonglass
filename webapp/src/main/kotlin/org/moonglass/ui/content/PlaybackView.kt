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

import kotlinx.css.Display
import kotlinx.css.GridTemplateRows
import kotlinx.css.Position
import kotlinx.css.display
import kotlinx.css.gridTemplateRows
import kotlinx.css.height
import kotlinx.css.margin
import kotlinx.css.marginLeft
import kotlinx.css.pct
import kotlinx.css.position
import kotlinx.css.rem
import kotlinx.css.width
import kotlinx.html.DIV
import kotlinx.html.js.onChangeFunction
import org.moonglass.ui.ContentProps
import org.moonglass.ui.ResponsiveLayout
import org.moonglass.ui.Theme
import org.moonglass.ui.applyState
import org.moonglass.ui.useColorSet
import org.moonglass.ui.utility.SavedState
import org.moonglass.ui.utility.SavedState.restore
import org.moonglass.ui.utility.StateValue
import org.moonglass.ui.video.RecordingPlayer
import org.moonglass.ui.widgets.ScrubBar
import org.moonglass.ui.widgets.recordings.DateTimeSelector
import org.w3c.dom.HTMLSelectElement
import react.RBuilder
import react.dom.attrs
import react.dom.option
import styled.StyledDOMBuilder
import styled.css
import styled.styledDiv
import styled.styledSelect
import kotlin.js.Date


/**
 * Display a tiled view of multiple live sources.
 */
class PlaybackView(props: ContentProps) : TiledView<PlaybackViewState>(props) {
    override val title: String = "Playback"

    private lateinit var timerHandle: Any
    private var lastStartTime: Double = Date().getTime()

    override fun restoreMyStuff(state: PlaybackViewState) {
        super.restoreMyStuff(state)
        state.selectorState = saveKey.restore { DateTimeSelector.Saved() }.getState(this@PlaybackView)
        state.selectorState.listener = ::onStateChange
    }

    private fun onStateChange(any: Any) {
        console.log("onStateChange: $any")
        lastStartTime = Date().getTime()
    }

    override fun saveMyStuff() {
        super.saveMyStuff()
        SavedState.save(saveKey, DateTimeSelector.Saved.from(state.selectorState))
    }

    override fun RBuilder.render() {
        renderNavBar()
        styledDiv {
            css {
                height = 100.pct
                width = 100.pct
                marginLeft = ResponsiveLayout.sideBarReserve
                position = Position.relative
                display = Display.grid
                gridTemplateRows = GridTemplateRows("minmax(0, 1fr) max-content")
            }
            renderTiles()
            child(ScrubBar::class) {
                attrs {
                    dateTimeSelector = state.selectorState
                }
            }
        }
    }

    override fun RBuilder.renderNavBarWidget() {
        styledDiv {
            css {
                display = Display.flex
            }
            child(DateTimeSelector::class) {
                attrs {
                    data = state.selectorState
                    showDuration = false
                }
            }
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
                            sources = layoutState.layout.getStates(this@PlaybackView)
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
    }

    // Don't convert this to a static assignment as the `init()` function gets called before the constructor
    // is complete. Making this effectively a function rather than a property works.
    private val saveKey get() = "playbackViewKey"

    override fun StyledDOMBuilder<DIV>.addPlayer(
        key: String,
        stateValue: StateValue<String>
    ) {
        child(RecordingPlayer::class) {
            attrs {
                dateTimeSelector = state.selectorState
                playerKey = key
                source = stateValue
                overlay = true
                offsetSecs = (Date().getTime() - lastStartTime) / 1000
            }
        }
    }
}

external interface PlaybackViewState : TiledViewState {
    var selectorState: DateTimeSelector.SelectorState
}
