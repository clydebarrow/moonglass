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

package org.moonglass.ui.user

import org.moonglass.ui.ModalProps
import org.moonglass.ui.Theme
import org.moonglass.ui.data.DateFormat
import org.moonglass.ui.data.TimeFormat
import org.moonglass.ui.widgets.Dialog

object UserPreferences {

    var mode: Theme.Mode = Theme.Mode.values().first()
    var dateFormat = DateFormat.values().first()
    var timeFormat = TimeFormat.values().first()

    class PreferencesDialog(props: ModalProps) : Dialog(props) {
        override val title: String = "User preferences"
        override val okText: String = "Close"

        private val display = SelectEntry("Display mode", Theme.Mode.values().toList(), mode)
        private val date = SelectEntry("Date Format", DateFormat.values().toList(), defaultValue = dateFormat)
        private val time = SelectEntry("Time Format", TimeFormat.values().toList(), timeFormat)

        override val items: List<Entry> = listOf(display, date, time)
    }
}
