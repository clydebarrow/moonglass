package org.moonglass.ui.widgets

import kotlinx.browser.window
import kotlinx.coroutines.CoroutineScope
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
import org.moonglass.ui.applyState
import org.moonglass.ui.asBitRate
import org.moonglass.ui.asSize
import org.moonglass.ui.cardStyle
import org.moonglass.ui.imageSrc
import org.moonglass.ui.useColorSet
import org.moonglass.ui.utility.SavedState
import org.moonglass.ui.utility.SavedState.restore
import org.moonglass.ui.utility.StateVar
import org.moonglass.ui.video.LiveSource
import org.moonglass.ui.video.LiveSourceFactory
import org.w3c.dom.HTMLElement
import react.Props
import react.RBuilder
import react.RComponent
import react.State
import react.createRef
import react.dom.attrs
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

    private class Column(
        val heading: String,
        val minEms: Int,
        val align: TextAlign = TextAlign.right,
        val content: (LiveSource) -> String
    )

    companion object {
        private const val saveKey = "liveStatsKey"
        private val columns = listOf(
            Column("Source", 8, TextAlign.left) { it.caption },
            Column("Clients", 2) { it.clientCount.toString() },
            Column("Data rate", 8) { it.rate.asBitRate },
            Column("Total", 8) { it.totalBytes.asSize },
        )
    }

    private val elementRef = createRef<HTMLElement>()

    private lateinit var dragger: Dragger

    private lateinit var scope: CoroutineScope

    override fun LiveStatsState.init() {
        position = saveKey.restore { ScreenPosition(window.innerWidth - 400, window.innerHeight - 100) }
    }

    // this is the one that should be called???
    override fun LiveStatsState.init(props: LiveStatsProps) {
        init()
    }

    override fun componentDidMount() {
        scope = MainScope()
        elementRef.current?.let { element ->
            val pos = state.position.deNormalise(element)
            dragger = Dragger(element, pos, scope)
            applyState {
                position = pos
            }
            dragger.flow.onEach {
                elementRef.current?.let { element ->
                    applyState {
                        position = it.onScreen(element)
                    }
                }
            }.launchIn(scope)
        }
        scope.launch {
            LiveSourceFactory.flow.collect {
                forceUpdate()
            }
        }
        SavedState.addOnUnload(::saveMyStuff)
    }

    private fun saveMyStuff() {
        elementRef.current?.let { element ->
            SavedState.save(saveKey, state.position.normalise(element))
        }
    }

    override fun componentWillUnmount() {
        scope.cancel()      // also cancels the dragger
        saveMyStuff()
        SavedState.removeOnUnload(::saveMyStuff)
    }

    private fun StyledDOMBuilder<DIV>.cell(
        text: String,
        align: TextAlign = TextAlign.right,
        background: Color? = null
    ) {
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
        styledDiv {
            attrs {
                ref = elementRef
                onDoubleClickFunction = { App.showLiveStatus = false }
            }
            cardStyle()
            css {
                cursor = Cursor.grab
                display = if (props.isShowing.value)
                    Display.grid
                else
                    Display.none
                justifyContent = JustifyContent.center
                gridTemplateColumns =
                    GridTemplateColumns(columns.joinToString(" ") { "minmax(${it.minEms}rem, max-content)" })
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
                columns.forEach {
                    cell(it.heading, it.align, Theme().subHeader.backgroundColor)
                }
                LiveSourceFactory.sources.forEach { source ->
                    columns.forEach {
                        cell(it.content(source), it.align)
                    }
                }
            }
        }
    }
}
