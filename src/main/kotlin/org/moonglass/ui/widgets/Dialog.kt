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

import kotlinx.css.Align
import kotlinx.css.Color
import kotlinx.css.Display
import kotlinx.css.FlexDirection
import kotlinx.css.FontWeight
import kotlinx.css.GridTemplateColumns
import kotlinx.css.JustifyContent
import kotlinx.css.LinearDimension
import kotlinx.css.TextAlign
import kotlinx.css.alignContent
import kotlinx.css.backgroundColor
import kotlinx.css.borderColor
import kotlinx.css.borderRadius
import kotlinx.css.borderWidth
import kotlinx.css.display
import kotlinx.css.flexDirection
import kotlinx.css.flexGrow
import kotlinx.css.fontSize
import kotlinx.css.fontWeight
import kotlinx.css.gridTemplateColumns
import kotlinx.css.height
import kotlinx.css.justifyContent
import kotlinx.css.margin
import kotlinx.css.padding
import kotlinx.css.pct
import kotlinx.css.properties.boxShadow
import kotlinx.css.px
import kotlinx.css.rem
import kotlinx.css.rgba
import kotlinx.css.textAlign
import kotlinx.css.width
import kotlinx.css.zIndex
import kotlinx.html.ButtonType
import kotlinx.html.DIV
import kotlinx.html.InputType
import kotlinx.html.js.onChangeFunction
import kotlinx.html.js.onClickFunction
import kotlinx.serialization.Serializable
import org.moonglass.ui.Modal
import org.moonglass.ui.ModalProps
import org.moonglass.ui.ZIndex
import org.moonglass.ui.cardStyle
import org.moonglass.ui.name
import org.moonglass.ui.utility.SavedState
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.events.Event
import react.State
import react.dom.KeyboardEvent
import react.dom.attrs
import react.dom.defaultValue
import react.dom.onKeyPress
import react.setState
import styled.StyledDOMBuilder
import styled.css
import styled.styledButton
import styled.styledDiv
import styled.styledForm
import styled.styledInput


abstract class Dialog(props: ModalProps) : Modal<DialogState>(props) {

    /**
     * An entry in the dialog.
     */

    class Entry(val text: String, val type: InputType, val defaultValue: String = "") {
        val key = text.replace(" ", "").lowercase()
    }

    abstract val title: String

    open fun onSubmit() {
        props.doDismiss()
    }

    open fun validate() = true

    open val okText = "OK"
    open val cancelText = "Cancel"

    abstract val items: List<Entry>

    val saveKey get() = "Dialog-$title"

    val inputData by lazy {
        items.associate { it.key to it.defaultValue }.toMutableMap().also { data ->
            try {
                SavedState.restore<SavedData>(saveKey)?.let { savedData ->
                    console.log("Restored data $savedData")
                    data.filter { it.value.isBlank() }.map { it.key }.forEach { key ->
                        savedData.data[key]?.let { data[key] = it }
                    }
                }
            } catch (ex: Exception) {
                console.log(ex.toString())
            }
        }
    }


    var Entry.value: String
        get() = inputData[key] ?: ""
        set(value) {
            inputData[key] = value
        }

    override fun DialogState.init(props: ModalProps) {
        console.log("Dialog init")
        // restore any previously entered data that does not have a default value
    }

    override fun componentWillUnmount() {
        console.log("Saving data $inputData")
        SavedState.save(saveKey, cleanData())
    }

    private fun Entry.onValueChange(event: Event) {
        val element = (event.target as HTMLInputElement)
        value = element.value
        setState {
            isValid = validate()
        }
    }

    private fun keyDown(event: KeyboardEvent<*>) {
        event.apply {
            when (key) {
                "Escape" -> props.doDismiss()
                "Enter" -> {
                    if (validate()) onSubmit()
                }
                else -> return
            }
            event.stopPropagation()
            event.preventDefault()
        }
    }

    override fun StyledDOMBuilder<DIV>.renderInner() {
        // use a form so we can legally enclose input fields
        styledForm {
            attrs {
                onKeyPress = ::keyDown
            }
            cardStyle()
            css {
                display = Display.flex
                flexDirection = FlexDirection.column
            }
            // prevent submit on enter
            styledButton(type = ButtonType.submit) {
                attrs {
                    disabled = true
                }
                css {
                    display = Display.none
                }
            }

            // header
            styledDiv {
                css {
                    backgroundColor = headerBackground
                    fontWeight = FontWeight.bold
                    justifyContent = JustifyContent.center
                    textAlign = TextAlign.center
                    fontSize = 1.3.rem
                    padding(0.5.rem)
                    margin(0.5.rem)
                }
                +title
            }
            // The dialog items
            styledDiv {
                css {
                    display = Display.grid
                    gridTemplateColumns = GridTemplateColumns("max-content max-content")
                    width = LinearDimension.maxContent
                    height = LinearDimension.auto
                    zIndex = ZIndex.Input()
                }

                items.forEach { item ->
                    name = item.key
                    styledDiv {
                        css {
                            margin(0.2.rem)
                            justifyContent = JustifyContent.end
                            padding(0.5.rem)
                        }
                        +item.text
                    }
                    styledInput(type = item.type) {
                        css {
                            justifyContent = JustifyContent.start
                            width = LinearDimension.fillAvailable
                            padding(0.5.rem)
                            borderWidth = 1.px
                            borderColor = Color.lightGray
                            borderRadius = 0.1.rem
                            margin(bottom = 0.2.rem, right = 0.5.rem)
                        }
                        attrs {
                            defaultValue = item.value
                            onChangeFunction = { item.onValueChange(it) }
                        }
                    }
                }
            }
            // buttons
            styledDiv {
                css {
                    display = Display.flex
                    flexDirection = FlexDirection.row
                    justifyContent = JustifyContent.spaceEvenly
                }
                // cancel button
                if (cancelText.isNotBlank()) {
                    button(cancelText) { dismiss() }
                }
                button(okText, state.isValid) { onSubmit() }
            }
        }
    }


    private fun StyledDOMBuilder<*>.button(text: String, enabled: Boolean = true, onClick: (Event) -> Unit) {
        styledButton {
            attrs {
                onClickFunction = {
                    it.stopPropagation()        // ensure form is not submitted
                    it.preventDefault()
                    onClick(it)
                }
                if (!enabled)
                    disabled = true
            }
            css {
                display = Display.flex
                flexGrow = 1.0
                backgroundColor = if (text == cancelText) cancelButtonBackground else buttonBackground
                disabled {
                    backgroundColor = disabledBackground
                }
                borderRadius = 8.px
                borderWidth = 1.px
                borderColor = Color.gray
                boxShadow(rgba(0, 0, 0, 0.1), 0.px, 8.px, 15.px)
                justifyContent = JustifyContent.center
                alignContent = Align.center
                textAlign = TextAlign.center
                width = 50.pct
                padding(0.5.rem)
                margin(top = 0.5.rem, bottom = 0.5.rem, left = 1.rem, right = 1.rem)
            }
            +text
        }
    }

    private fun cleanData(): SavedData {
        val passwords = items.filter { it.type != InputType.password }.map { it.key }.toSet()
        return SavedData(inputData.filterKeys { (it in passwords) })
    }

    companion object {
        val buttonBackground = Color("#e0e0ff")
        val cancelButtonBackground = Color("#ffe0e0")
        val disabledBackground = Color("#f0f0f0")
        val headerBackground = Color("#f0f0f0")
    }

    @Serializable
    private data class SavedData(val data: Map<String, String>) {
        companion object {
        }
    }

}

external interface DialogState : State {
    var isValid: Boolean
}
