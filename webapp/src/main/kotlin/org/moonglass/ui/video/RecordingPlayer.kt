/*
 *
 *  * Copyright (c) 2021. Clyde Stubbs
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  * http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 *
 */

package org.moonglass.ui.video

import org.moonglass.ui.widgets.recordings.DateTimeSelector

external interface RecordingPlayerProps : PlayerProps {
    var dateTimeSelector: DateTimeSelector.SelectorState
    var offset: Int         // time offset in seconds
}

external interface RecordingPlayerState : PlayerState {
}

class RecordingPlayer(props: RecordingPlayerProps) : Player<RecordingPlayerProps, RecordingPlayerState>(props) {
    override val streamSource = RecordingBuffer()

    override fun componentDidUpdate(prevProps: RecordingPlayerProps, prevState: RecordingPlayerState, snapshot: Any) {
        if (props.source().isBlank())
            return
        if (
            state.currentSource != props.source() ||
            props.dateTimeSelector.startDate() != streamSource.startDate ||
            props.dateTimeSelector.startTime() != streamSource.startTime ||
            props.dateTimeSelector.endTime() != streamSource.endTime
        )
            updateSrcUrl()
    }

    override fun updateSrcUrl() {
        streamSource.startDate = props.dateTimeSelector.startDate()
        streamSource.startTime = props.dateTimeSelector.startTime()
        streamSource.endTime = props.dateTimeSelector.endTime()
        super.updateSrcUrl()
    }
}
