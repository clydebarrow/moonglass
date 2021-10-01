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

package org.moonglass.ui.video

import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.moonglass.ui.widgets.recordings.Stream

/**
 * A central place to create LiveSources. A single LiveSource can stream the same data to multiple
 * MediaSources so we use a cache to avoid creating new objects.
 */
object LiveSourceFactory {

    private val sourceCache = mutableMapOf<String, LiveSource>()

    private val scope = MainScope()

    fun getSource(stream: Stream): LiveSource {
        return sourceCache.getOrPut(stream.key) {
            LiveSource(stream)
        }.also {
            updateFlow()
        }
    }

    private var timerJob: Job? = null

    /**
     * Update the sources state
     */
    fun updateFlow() {
        if (timerJob == null) {
            timerJob = (1..Int.MAX_VALUE).asSequence().asFlow().onEach {
                delay(2000)
                updateFlow()
            }.launchIn(scope)
        }
        scope.launch {
            iFlow.emit(sources)
        }
    }

    /**
     * Get a list of sources currently in the cache
     */
    val sources: List<LiveSource> get() = sourceCache.values.sortedBy { it.caption }

    private val iFlow = MutableSharedFlow<List<LiveSource>>(1, 1, BufferOverflow.DROP_OLDEST)

    val flow: SharedFlow<List<LiveSource>> get() = iFlow
}
