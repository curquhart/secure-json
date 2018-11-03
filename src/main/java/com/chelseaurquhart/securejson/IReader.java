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

import java.io.Closeable;
import java.io.IOException;

/**
 * @exclude
 */
interface IReader<T> extends Closeable {
    Object normalizeCollection(Object parValue);

    /**
     * @exclude
     */
    enum SymbolType {
        END,
        SEPARATOR,
        UNKNOWN,
        RESERVED
    }

    T read(ICharacterIterator parIterator) throws IOException;

    void addValue(ICharacterIterator parIterator, Object parCollection, Object parValue) throws IOException;

    boolean isStart(ICharacterIterator parIterator) throws IOException;

    boolean isContainerType();

    SymbolType getSymbolType(ICharacterIterator parIterator) throws IOException;
}
