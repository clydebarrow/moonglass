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

package org.moonglass.ui

import kotlinx.css.Color
import kotlinx.css.Position
import kotlinx.css.backgroundColor
import kotlinx.css.left
import kotlinx.css.pct
import kotlinx.css.position
import kotlinx.css.properties.transform
import kotlinx.css.properties.translate
import kotlinx.css.top
import kotlinx.css.zIndex
import kotlinx.html.DIV
import kotlinx.html.js.onClickFunction
import react.Props
import react.RBuilder
import react.RComponent
import react.State
import react.dom.attrs
import styled.StyledDOMBuilder
import styled.css
import styled.styledDiv

external interface ModalProps : Props {
    var doDismiss: (() -> Unit)                        // Function to call ondismiss
}

/**
 * Show a component as a modal dialog, centered on a translucent dismisser.
 */
abstract class Modal<P : ModalProps, S : State> : RComponent<P, S>() {

    open val dismissOutside: Boolean = true

    abstract fun StyledDOMBuilder<DIV>.renderInner()

    override fun RBuilder.render() {
        dismisser({ if (dismissOutside) props.doDismiss() }) {
            css {
                backgroundColor = Color("#00000030")    // darkish overlay
            }
            styledDiv {
                // ensure clicking inside the dialog does not dismiss it.
                attrs {
                    onClickFunction = { it.stopPropagation() }
                }
                css {
                    zIndex = ZIndex.Modal()
                    position = Position.absolute
                    left = 50.pct
                    top = 30.pct
                    transform {
                        translate((-50).pct, (-30).pct)
                    }
                }
                renderInner()
            }
        }
    }
}
