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

package org.moonglass.ui.utility

import org.moonglass.ui.applyState
import react.Props
import react.RComponent
import react.State

/**
 * This class is used to store values that can be passed to a child wrapped in a way that allows it to update the value.
 */
open class StateVar<T : Any>(value: T, val component: RComponent<out Props, out State>) {

    operator fun invoke() = value

    open var value: T = value
        set(value) {
            component.applyState({
                listener?.invoke(value)
            }) {
                field = value
            }
        }

    var listener: ((T) -> Unit)? = null

    /**
     * Convenience function to create a StateVar inside a component, typically used inside init()
     */
    companion object {
        fun <T : Any> RComponent<out Props, out State>.createValue(value: T): StateVar<T> {
            return StateVar(value, this)
        }
    }
}
