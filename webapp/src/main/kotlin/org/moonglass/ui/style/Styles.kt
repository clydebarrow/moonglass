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

package org.moonglass.ui.style

import kotlinx.css.Cursor
import kotlinx.css.Display
import kotlinx.css.JustifyContent
import kotlinx.css.LinearDimension
import kotlinx.css.Overflow
import kotlinx.css.cursor
import kotlinx.css.display
import kotlinx.css.flex
import kotlinx.css.justifyContent
import kotlinx.css.margin
import kotlinx.css.maxHeight
import kotlinx.css.opacity
import kotlinx.css.overflow
import kotlinx.css.padding
import kotlinx.css.properties.deg
import kotlinx.css.properties.ms
import kotlinx.css.properties.rotate
import kotlinx.css.properties.scaleY
import kotlinx.css.properties.transform
import kotlinx.css.properties.transition
import kotlinx.css.px
import kotlinx.css.rem
import kotlinx.html.InputType
import kotlinx.html.id
import kotlinx.html.js.onClickFunction
import org.moonglass.ui.imageSrc
import org.moonglass.ui.utility.StateVar
import org.w3c.dom.events.Event
import react.RBuilder
import react.dom.attrs
import react.dom.onChange
import styled.StyledDOMBuilder
import styled.css
import styled.styledDiv
import styled.styledImg
import styled.styledInput
import styled.styledLabel


/**
 * Apply css to collapse or expand an element
 * @param expanded True if it should be shown in expanded form
 * @param maxHeight Estimated maximum height. Should be as accurate as possible, but not less than required.
 */
fun StyledDOMBuilder<*>.shrinkable(expanded: Boolean, maxHeight: LinearDimension) {
    css {
        classes.add("shrinkable")
        // hide the dlement rows using maxHeight, with transition, so opening and closing is smooth.
        transform {
            //scaleY(if (expanded) 1.0 else 0.0)
        }
        if (expanded) {
            this.maxHeight = maxHeight
            opacity = 1.0
        } else {
            overflow = Overflow.hidden
            transform {
                scaleY(0.0)
            }
            this.maxHeight = 0.px
            opacity = 0.0
        }
        transition("all", 300.ms)
    }
}

/**
 * Place a button to provide affordance for the expansion of a view.
 * @param expanded If the button should be in the expanded state (pointing down)
 * @param onClick Called when the button is clicked. Optional.
 */
fun StyledDOMBuilder<*>.expandButton(expanded: Boolean, onClick: ((Event) -> Unit)? = null) {
    styledImg {
        imageSrc("play", 20.px)
        attrs {
            onClick?.also { onClickFunction = it }
        }
        css {
            cursor = Cursor.pointer
            transition("all", 300.ms)
            if (expanded)
                transform { rotate(90.deg) }
        }
    }
}

fun StyledDOMBuilder<*>.column(
    value: String,
    weight: Double,
    justify: JustifyContent = JustifyContent.end
) {
    styledDiv {
        css {
            display = Display.flex
            flex(weight, 0.0, 0.px)
            padding(left = 0.25.rem, right = 0.25.rem)
            justifyContent = justify
        }
        +value
    }
}

private val nonAlnumRegex = "[^A-Za-z0-9]".toRegex()

// String extensions

val String.alNum: String
    get() {
        return nonAlnumRegex.replace(this, "")
    }


/**
 * Add a checkbox
 */

fun RBuilder.checkBox(label: String, data: StateVar<Boolean>) {
    val inputId = label.alNum
    styledLabel {
        attrs.set("htmlFor", inputId)       // can't use htmlFor property since it doesn't work for react
        css {
            padding(left = 0.25.rem, right = 0.25.rem)
        }
        +label
    }
    styledInput(InputType.checkBox) {
        attrs["checked"] = data()           // another work-around for kotlinx react.
        attrs {
            id = inputId
            onChange = { data.value = !data() }
        }
        css {
            margin(left = 1.5.rem)
        }
    }

}
