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

package org.moonglass.ui.widgets

import kotlinx.css.BorderStyle
import kotlinx.css.Color
import kotlinx.css.Outline
import kotlinx.css.Position
import kotlinx.css.backgroundColor
import kotlinx.css.borderColor
import kotlinx.css.borderRadius
import kotlinx.css.borderStyle
import kotlinx.css.borderWidth
import kotlinx.css.color
import kotlinx.css.fontSize
import kotlinx.css.left
import kotlinx.css.outline
import kotlinx.css.padding
import kotlinx.css.pct
import kotlinx.css.position
import kotlinx.css.properties.ms
import kotlinx.css.properties.transform
import kotlinx.css.properties.transition
import kotlinx.css.properties.translate
import kotlinx.css.px
import kotlinx.css.top
import kotlinx.html.InputType
import kotlinx.html.id
import kotlinx.html.js.onChangeFunction
import kotlinx.html.js.onFocusOutFunction
import react.Props
import react.RBuilder
import react.RComponent
import react.State
import react.dom.attrs
import styled.css
import styled.styledInput
import styled.styledLabel

external interface InputProps : Props {
    var id: String
    var value: String
    var label: String
    var type: InputType
    var attrs: Map<String, Any>?
    var onChange: (String) -> Unit
}

class InputGroup(props: InputProps) : RComponent<InputProps, State>(props) {
    override fun RBuilder.render() {
        styledInput {

            props.attrs?.forEach {
                attrs[it.key] = it.toString()
            }
            attrs {
                id = props.id
                type = props.type
                    onFocusOutFunction = { props.onChange(value) }
                value = props.value
            }
            css {
                outline = Outline.none
                padding(16.px, 22.px)
                borderWidth = 1.px
                borderStyle = BorderStyle.solid
                borderColor = Color("#dadce0")
                fontSize = 18.px
                borderRadius = 5.px
                focus {
                    borderColor = Color.royalBlue
                    borderWidth = 2.px
                }
                valid {
                    adjacentSibling("label") {
                        top = (-1).px
                        padding(0.px, 3.px)
                        fontSize = 14.px
                        color = Color("#8d8d8d")
                    }
                }
                focus {
                    adjacentSibling("label") {
                        top = (-1).px
                        padding(0.px, 3.px)
                        fontSize = 14.px
                        color = Color.royalBlue
                        transition(duration = 300.ms)
                    }
                }
            }
        }
        styledLabel {
            attrs {
                htmlFor = props.id
            }
            css {
                color = Color("#8d8d8d")
                position = Position.absolute
                top = 27.px
                left = 55.px
                backgroundColor = Color.white
                transition(duration = 300.ms)
                transform {
                    translate((-50).pct, (-50).pct)
                }
            }
            +props.label
        }
    }
}
