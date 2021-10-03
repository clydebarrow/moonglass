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
import kotlinx.css.GridColumn
import kotlinx.css.GridTemplateColumns
import kotlinx.css.JustifyContent
import kotlinx.css.Position
import kotlinx.css.TextAlign
import kotlinx.css.backgroundColor
import kotlinx.css.bottom
import kotlinx.css.cursor
import kotlinx.css.display
import kotlinx.css.gridColumn
import kotlinx.css.gridTemplateColumns
import kotlinx.css.justifyContent
import kotlinx.css.left
import kotlinx.css.padding
import kotlinx.css.position
import kotlinx.css.px
import kotlinx.css.rem
import kotlinx.css.right
import kotlinx.css.textAlign
import kotlinx.css.top
import kotlinx.css.zIndex
import kotlinx.html.DIV
import kotlinx.html.js.onClickFunction
import kotlinx.html.js.onDoubleClickFunction
import org.moonglass.ui.App
import org.moonglass.ui.Theme
import org.moonglass.ui.ZIndex
import org.moonglass.ui.asBitRate
import org.moonglass.ui.asSize
import org.moonglass.ui.cardStyle
import org.moonglass.ui.imageSrc
import org.moonglass.ui.useColorSet
import org.moonglass.ui.utility.SavedState
import org.moonglass.ui.utility.SavedState.restore
import org.moonglass.ui.utility.StateVar
import org.moonglass.ui.video.LiveSourceFactory
import react.Props
import react.RBuilder
import react.RComponent
import react.State
import react.dom.attrs
import react.setState
import styled.StyledDOMBuilder
import styled.css
import styled.styledDiv
import styled.styledImg


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
        val columns = listOf("Source" to 8, "Clients" to 2, "Data rate" to 8, "Total" to 8)
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
            if (job == null) {
                job = scope.launch {
                    LiveSourceFactory.flow.collect {
                        forceUpdate()
                    }
                }
            }
        } else {
            job?.cancel()
            job = null
        }
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

    override fun RBuilder.render() {
        startStop(props.isShowing.value)
        styledDiv {
            attrs {
                onDoubleClickFunction = { App.showLiveStatus = false }
            }
            dragger.attach(attrs)
            cardStyle()
            css {
                cursor = Cursor.grab
                display = if (props.isShowing.value)
                    Display.grid
                else
                    Display.none
                justifyContent = JustifyContent.center
                gridTemplateColumns =
                    GridTemplateColumns(columns.joinToString(" ") { "minmax(${it.second}rem, max-content)" })
                position = Position.absolute
                top = state.position.y.px
                left = state.position.x.px
                useColorSet(Theme().menu)
                zIndex = ZIndex.Stats.index
            }
            if (props.isShowing.value) {
                styledDiv {
                    css {
                        gridColumn = GridColumn("1 / span ${columns.size}")
                        textAlign = TextAlign.center
                        useColorSet(Theme().header)
                        position = Position.relative
                    }
                    +"Live stream statistics"
                    // overlay a closing button
                    styledImg {
                        imageSrc("close", 1.5.rem)
                        css {
                            position = Position.absolute
                            right = 0.px
                            backgroundColor = Color.transparent
                            top = 0.px
                            bottom = 0.px
                            cursor = Cursor.pointer
                        }
                        attrs {
                            onClickFunction = { App.showLiveStatus = false }
                        }
                    }
                }
                columns.forEachIndexed { index, it ->
                    cell(
                        it.first,
                        if (index == 0) TextAlign.left else TextAlign.right,
                        Theme().subHeader.backgroundColor
                    )
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
}
