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

/**
 * Provide a structured way of managing z-indices. Most important stuff comes last.
 *
 * Gaps are left in the sequence to allow for fine-tuning
 */
enum class ZIndex(val index: Int) {
    Default(0),        // standard (0)
    NavBar(10),         // navbar here?
    Dismisser(20),      // overlay to dismiss a menu or dialog
    Modal(30),
    Menu(40),           // menu on top of standard stuff
    Input(50),          // modal input
    Spinner(60),        // busy indicator
    Tooltip(70),          // modal input
    Toast(80);          // transient feedback

    // allow simplified syntax
    operator fun invoke() = index
}
