@file:JsModule("react-time-picker")
@file:JsNonModule

import react.Component
import react.Props
import react.ReactElement
import react.State

@JsName("default")
external class TimePicker : Component<TimePickerProps, State> {
    override fun render(): ReactElement?
}

external interface TimePickerProps : Props {
    var value: String
    var onChange: (String) -> Unit
}
