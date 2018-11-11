/*
 * Copyright 2018 Chelsea Urquhart
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.chelseaurquhart.securejson;

import java.io.IOException;
import java.util.Iterator;

/**
 * @exclude
 */
interface ICharacterIterator extends Iterator<Character> {
    /**
     * Get the next available character without increasing the internal position.
     *
     * @return The next available character or null if there isn't one.
     * @throws IOException On read failure.
     * @throws JSONException On decode failure.
     */
    Character peek() throws IOException, JSONException;

    /**
     * Get the current offset.
     *
     * @return The current offset.
     */
    int getOffset();
}
