package org.moonglass.ui.widgets

import kotlinx.css.BorderStyle
import kotlinx.css.Color
import kotlinx.css.LinearDimension
import kotlinx.css.Position
import kotlinx.css.borderColor
import kotlinx.css.borderRadius
import kotlinx.css.borderRightColor
import kotlinx.css.borderStyle
import kotlinx.css.borderWidth
import kotlinx.css.height
import kotlinx.css.left
import kotlinx.css.pct
import kotlinx.css.position
import kotlinx.css.properties.IterationCount
import kotlinx.css.properties.Timing
import kotlinx.css.properties.deg
import kotlinx.css.properties.ms
import kotlinx.css.properties.rotate
import kotlinx.css.properties.s
import kotlinx.css.properties.transform
import kotlinx.css.properties.translate
import kotlinx.css.rem
import kotlinx.css.top
import kotlinx.css.width
import kotlinx.css.zIndex
import kotlinx.html.role
import org.moonglass.ui.ZIndex
import react.Props
import react.RBuilder
import react.RComponent
import react.State
import react.dom.attrs
import styled.animation
import styled.css
import styled.styledDiv

class Spinner : RComponent<Props, State>() {
    override fun RBuilder.render() {
        styledDiv {
            css {
                zIndex = ZIndex.Spinner()
                position = Position.fixed;
                top = 50.pct
                left = 50.pct
                transform { translate(50.pct, 50.pct) }
            }
            styledDiv {
                attrs { role = "status" }
                css {
                    width = 7.rem
                    height = 7.rem
                    borderWidth = 0.25.rem
                    borderStyle = BorderStyle.solid
                    borderColor = Color.teal
                    borderRightColor = Color.transparent
                    borderRadius = 50.pct
                    animation(duration = 0.75.s, timing = Timing.linear, iterationCount = IterationCount.infinite) {
                        to {
                            transform { rotate(360.deg) }
                        }
                    }
                }
            }
        }
    }
}
