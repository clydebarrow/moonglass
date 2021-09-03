package org.moonglass.ui.video

import kotlinx.css.Align
import kotlinx.css.Color
import kotlinx.css.Display
import kotlinx.css.FlexDirection
import kotlinx.css.JustifyItems
import kotlinx.css.TextAlign
import kotlinx.css.alignItems
import kotlinx.css.backgroundColor
import kotlinx.css.display
import kotlinx.css.flexDirection
import kotlinx.css.flexGrow
import kotlinx.css.fontSize
import kotlinx.css.height
import kotlinx.css.justifyItems
import kotlinx.css.maxWidth
import kotlinx.css.px
import kotlinx.css.rem
import kotlinx.css.textAlign
import kotlinx.css.width
import react.Props
import react.RBuilder
import react.RComponent
import react.State
import react.dom.attrs
import styled.css
import styled.styledDiv
import styled.styledVideo

external interface PlayerProps : Props {
    var src: String?
    var width: Int
    var height: Int
    var title: String
}

class Player(props: PlayerProps) : RComponent<PlayerProps, State>(props) {
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
                +props.title
            }

            styledVideo {
                css {
                    maxWidth = 1024.px
                    width = props.width.px
                    height = props.height.px
                    display = Display.flex
                    flexGrow = 1.0
                }
                attrs {
                    autoPlay = true
                    autoBuffer = true
                    controls = true
                    poster = "/images/placeholder.png"
                    props.src?.let {
                        if (it.isNotBlank()) {
                            src = it
                            width = props.width.px.toString()
                            height = props.height.px.toString()
                        }
                    }
                }
            }
        }
    }
}
