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

import org.moonglass.ui.widgets.recordings.Stream

/**
 * Represents something that will deliver a streaming url for a given stream.
 * Repeated calls to getSrcUrl should return the same value for the same argument, i.e. caching is assumed.
 */
interface StreamSource {
    /**
     * Return a url suitable for use as the `src` attribute of a video player element
     */
    fun getSrcUrl(source: Stream): String?

    /**
     * Stop the dataflow and release any resources
     */
    fun close()
}
