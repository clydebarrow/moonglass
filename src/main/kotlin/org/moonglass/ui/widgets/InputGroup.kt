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

/*

const InputGroup = styled.div`
  position: relative;
`;

const InputLabel = styled.label`
  color: #8d8d8d;
  position: absolute;
  top: 27px;
  left: 55px;
  background: #ffffff;
  transition: 300ms;
  transform: translate(-50%, -50%);
`;

const InputField = styled.input`
  outline: none;
  padding: 16px 22px;
  border: 1px solid #dadce0;
  font-size: 18px;
  border-radius: 5px;

  &:focus
  {
    border: 2px solid royalblue;
  }

  &:valid + ${InputLabel}
  {
    top: -1px;
    padding: 0 3px;
    font-size:14px;
    color: #8d8d8d;
  }

  &:focus + ${InputLabel}
  {
    top: -1px;
    padding: 0 3px;
    font-size:14px;
    color: royalblue;
    transition: 300ms;
  }
`;

const Input: React.FC<InputProps> = ({ id, label, ...rest }) => {
  return (
    <InputGroup>
      <InputField id={id} {...rest} />
      <InputLabel htmlFor={id} >{label}</InputLabel>
    </InputGroup>
  );
}

export default Input;
 */
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
