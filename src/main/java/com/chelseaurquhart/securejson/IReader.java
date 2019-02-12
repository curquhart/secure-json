/*
 * Copyright 2019 Chelsea Urquhart
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

import java.io.Closeable;
import java.io.IOException;

/**
 * @exclude
 */
interface IReader<T> extends Closeable {
    /**
     * @exclude
     */
    enum SymbolType {
        END,
        SEPARATOR,
        UNKNOWN
    }

    /**
     * Read the next T that this represents.
     *
     * @param parIterator The iterator to read from.
     * @param parContainer A container for the collection we are managing.
     * @return An instanceof T
     * @throws IOException On read failure.
     * @throws JSONException On process failure.
     */
    T read(ICharacterIterator parIterator, JSONReader.IContainer<?, ?> parContainer) throws IOException, JSONException;

    /**
     * Add a value to the collection we are managing.
     *
     * @param parIterator The iterator to read from.
     * @param parCollection The collection we are managing.
     * @param parValue The value to add to the collection.
     * @throws IOException On read failure.
     * @throws JSONException On process failure.
     */
    void addValue(ICharacterIterator parIterator, JSONReader.IContainer<?, ?> parCollection, Object parValue)
            throws IOException,
        JSONException;

    /**
     * Check if the next character in the iterator is the start token for this reader. This should not increment the
     * input.
     * @param parIterator The iterator to read.
     * @return True if the next character in the iterator is the start of a token we manage. Otherwise false.
     * @throws IOException On read failure.
     * @throws JSONException On process failure.
     */
    boolean isStart(ICharacterIterator parIterator) throws IOException, JSONException;

    /**
     * Get the symbol type from the provided iterator. This is used for determining when we hit a separator, end of a
     * sequence, etc.
     * @param parIterator The iterator to read.
     * @return The symbol type relative to this reader. Return UNKNOWN if it is not applicable.
     * @throws IOException On read failure.
     * @throws JSONException On process failure.
     */
    SymbolType getSymbolType(ICharacterIterator parIterator) throws IOException, JSONException;
}
