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

import kotlinx.serialization.Serializable
import org.moonglass.ui.ModalProps
import org.moonglass.ui.Theme
import org.moonglass.ui.data.DateFormat
import org.moonglass.ui.data.TimeFormat
import org.moonglass.ui.utility.SavedState
import org.moonglass.ui.utility.SavedState.restore
import org.moonglass.ui.widgets.Dialog

@Serializable
class UserPreferences {
    var theme: Theme.Mode = Theme.Mode.values().first()
    var dateFormat = DateFormat.values().first()
    var timeFormat = TimeFormat.values().first()

    companion object {
        const val saveKey = "userPreferences"
        var current = saveKey.restore { UserPreferences() }

        operator fun invoke() = current

        fun save() {
            SavedState.save(saveKey, current)
        }
    }

    class PreferencesDialog(props: ModalProps) : Dialog(props) {
        override val title: String = "User preferences"
        override val okText: String = "Save"

        private val themeSelect = SelectEntry("Display mode", Theme.Mode.values().toList(), current.theme)
        private val dateSelect =
            SelectEntry("Date Format", DateFormat.values().toList(), defaultValue = current.dateFormat)
        private val timeSelect = SelectEntry("Time Format", TimeFormat.values().toList(), current.timeFormat)

        override val items: List<Entry> = listOf(themeSelect, dateSelect, timeSelect)

        override fun onSubmit() {
            current.theme = themeSelect.enumValue
            current.dateFormat = dateSelect.enumValue
            current.timeFormat = timeSelect.enumValue
            save()
            super.onSubmit()
        }
    }
}
