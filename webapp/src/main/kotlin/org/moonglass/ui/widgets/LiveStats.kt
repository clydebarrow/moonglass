package org.moonglass.ui.widgets

import kotlinx.browser.window
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.css.Color
import kotlinx.css.Cursor
import kotlinx.css.Display
import kotlinx.css.GridTemplateColumns
import kotlinx.css.JustifyContent
import kotlinx.css.Position
import kotlinx.css.TextAlign
import kotlinx.css.backgroundColor
import kotlinx.css.color
import kotlinx.css.cursor
import kotlinx.css.display
import kotlinx.css.gridTemplateColumns
import kotlinx.css.justifyContent
import kotlinx.css.left
import kotlinx.css.padding
import kotlinx.css.position
import kotlinx.css.px
import kotlinx.css.rem
import kotlinx.css.textAlign
import kotlinx.css.top
import kotlinx.css.zIndex
import kotlinx.html.DIV
import org.moonglass.ui.Theme
import org.moonglass.ui.ZIndex
import org.moonglass.ui.asBitRate
import org.moonglass.ui.asSize
import org.moonglass.ui.cardStyle
import org.moonglass.ui.utility.SavedState
import org.moonglass.ui.utility.SavedState.restore
import org.moonglass.ui.utility.StateVar
import org.moonglass.ui.video.LiveSourceFactory
import react.Props
import react.RBuilder
import react.RComponent
import react.State
import react.setState
import styled.StyledDOMBuilder
import styled.css
import styled.styledDiv


/**
 * This component displays live stats for the streaming video sources
 *
 */
external interface LiveStatsProps : Props {
    var isShowing: StateVar<Boolean>
}


external interface LiveStatsState : State {
    var position: ScreenPosition
}

class LiveStats : RComponent<LiveStatsProps, LiveStatsState>() {

    companion object {
        private const val saveKey = "liveStatsKey"
        val columns = listOf("Source", "Clients", "Data rate", "Total")
    }

    private var job: Job? = null

    private lateinit var dragger: Dragger

    private lateinit var scope: CoroutineScope

    override fun LiveStatsState.init() {
        position = saveKey.restore { ScreenPosition(window.innerWidth - 400, window.innerHeight - 100) }
        scope = MainScope()
        dragger = Dragger(position, scope)
    }

    // this is the one that should be called???
    override fun LiveStatsState.init(props: LiveStatsProps) {
        init()
    }

    private fun startStop(yes: Boolean) {
        if (yes) {
            if (job == null)
                job = scope.launch {
                    LiveSourceFactory.flow.collect {
                        forceUpdate()
                    }
                }
        } else
            job?.cancel()
    }

    override fun componentDidMount() {
        dragger.flow.onEach {
            setState {
                position = it
            }
        }.launchIn(scope)
        SavedState.addOnUnload(::saveMyStuff)
    }

    private fun saveMyStuff() {
        SavedState.save(saveKey, state.position)
    }
    override fun componentWillUnmount() {
        startStop(false)
        scope.cancel()      // also cancels the dragger
        saveMyStuff()
        SavedState.removeOnUnload(::saveMyStuff)
    }

    private fun StyledDOMBuilder<DIV>.cell(text: String, align: TextAlign = TextAlign.left, background: Color? = null) {
        styledDiv {
            +text
            css {
                textAlign = align
                background?.let { backgroundColor = it }
                padding(left = 0.5.rem, right = 0.5.rem)
            }
        }
    }

    private fun StyledDOMBuilder<DIV>.columnHeader(text: String) {
        cell(text, TextAlign.center, Theme().header.backgroundColor)
    }

    override fun RBuilder.render() {
        startStop(props.isShowing.value)
        styledDiv {
            dragger.attach(attrs)
            cardStyle()
            css {
                cursor = Cursor.grab
                display = if (props.isShowing.value)
                    Display.grid
                else
                    Display.none
                justifyContent = JustifyContent.center
                gridTemplateColumns = GridTemplateColumns(columns.joinToString(" ") { "minmax(8rem, max-content)" })
                position = Position.absolute
                top = state.position.y.px
                left = state.position.x.px
                backgroundColor = Theme().menu.backgroundColor
                color = Theme().menu.textColor
                zIndex = ZIndex.Stats.index
            }
            if (props.isShowing.value)
                columns.forEach {
                    columnHeader(it)
                }
            LiveSourceFactory.sources.forEach {
                cell(it.caption)
                cell(it.clientCount.toString(), TextAlign.right)
                cell(it.rate.asBitRate, TextAlign.right)
                cell(it.totalBytes.asSize, TextAlign.right)
            }
        }
    }
}
