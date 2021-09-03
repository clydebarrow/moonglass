@file:JsModule("react-calendar")
@file:JsNonModule

import react.*
import kotlin.js.Date

@JsName("Calendar")
external val calendar: ComponentClass<CalendarProps>

external interface CalendarProps : Props {
    var defaultView: String
    var defaultActiveStartDate: Date
    var onChange: (Date) -> Unit
}

