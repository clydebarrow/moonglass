package org.moonglass.ui.video

import kotlinx.browser.window
import kotlinx.css.Color
import kotlinx.css.Display
import kotlinx.css.FlexDirection
import kotlinx.css.JustifyItems
import kotlinx.css.LinearDimension
import kotlinx.css.TextAlign
import kotlinx.css.backgroundColor
import kotlinx.css.display
import kotlinx.css.flexDirection
import kotlinx.css.flexGrow
import kotlinx.css.fontSize
import kotlinx.css.height
import kotlinx.css.justifyItems
import kotlinx.css.maxWidth
import kotlinx.css.pct
import kotlinx.css.px
import kotlinx.css.rem
import kotlinx.css.textAlign
import kotlinx.css.width
import org.moonglass.ui.ResponsiveLayout
import react.Props
import react.RBuilder
import react.RComponent
import react.State
import react.dom.attrs
import react.setState
import styled.css
import styled.styledDiv
import styled.styledVideo

external interface PlayerProps : Props {
    var source: VideoSource?
    var availableHeight: Int
    var availableWidth: Int
}

external interface PlayerState : State {
    var width: Int
    var height: Int
    var aspectRatio: Double
}

class Player(props: PlayerProps) : RComponent<PlayerProps, PlayerState>(props) {

    private fun PlayerState.calculateWidthAndHeight(ratio: Double) {
        val padding = (1.5 * ResponsiveLayout.emPixels).toInt()
        aspectRatio = ratio
        if (props.availableWidth / aspectRatio > props.availableHeight) {
            height = props.availableHeight - padding
            width = (props.availableHeight * aspectRatio).toInt() - padding
        } else {
            height = (props.availableWidth / aspectRatio).toInt() - padding
            width = props.availableWidth - padding
        }
        console.log("Aspectratio = $aspectRatio, availableWidth=${props.availableWidth}, availableHeight=${props.availableHeight}, width=$width, height=$height")
    }

    override fun PlayerState.init(props: PlayerProps) {
        calculateWidthAndHeight(1200.0 / 800)
    }

    override fun componentWillReceiveProps(nextProps: PlayerProps) {
        if (nextProps.source != props.source)
            nextProps.source?.setAspectCallback {
                setState {
                    calculateWidthAndHeight(it)
                }
            }
    }

    override fun RBuilder.render() {
        styledDiv {
            css {
                display = Display.flex
                flexDirection = FlexDirection.column
                justifyItems = JustifyItems.center
            }

            styledDiv {
                css {
                    fontSize = 1.2.rem
                    textAlign = TextAlign.center
                    backgroundColor = Color.lightGray
                }
                +(props.source?.caption ?: "---")
            }

            styledVideo {
                css {
                    width = state.width.px - 1.5.rem        // allow for padding
                    height = state.height.px - 1.5.rem        // allow for padding
                    display = Display.flex
                    flexGrow = 1.0
                }
                attrs {
                    width = "${state.width}px"
                    height = "${state.height}px"
                    autoPlay = true
                    autoBuffer = true
                    controls = true
                    poster = "/images/placeholder.png"
                    props.source?.also {
                        src = it.srcUrl
                    }
                }
            }
        }
    }
}
