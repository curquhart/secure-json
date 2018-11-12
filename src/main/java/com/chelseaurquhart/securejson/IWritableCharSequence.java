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
 * Interface representing a CharSequence that can be written to. It must implement close even if it is a NOOP
 * function.
 */
public interface IWritableCharSequence extends CharSequence, Closeable, IAutoCloseable {
    /**
     * Append a character to the sequence of characters.
     *
     * @param parChar The character to append.
     * @throws IOException On write failure.
     */
    void append(char parChar) throws IOException;

    /**
     * Check if this buffer is restricted to the initial capacity. If it is, we will create more buffers before going
     * over the initial capacity. If it is not, we will expect it to handle resizing itself.
     *
     * Note that the result of this will be cached. Changing values at runtime is not supported.
     *
     * @return True if this buffer is restricted to the initial capacity. False otherwise.
     */
    boolean isRestrictedToCapacity();

    /**
     * Return the max capacity of this buffer.
     * @return The max capacity.
     */
    int getCapacity();
}
