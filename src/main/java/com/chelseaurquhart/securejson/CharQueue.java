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

/**
 * Circular array queue.
 *
 * @exclude
 */
class CharQueue {
    private char[] chars;
    private int readIndex = 0;
    private int writeIndex = 0;

    CharQueue(final int parCapacity) {
        chars = new char[parCapacity + 1];
    }

    void add(final char parChar) {
        if (readIndex == (writeIndex + 1) % chars.length) {
            throw new IllegalStateException();
        }

        chars[writeIndex] = parChar;
        writeIndex = (writeIndex + 1) % chars.length;
    }

    char pop() {
        if (isEmpty()) {
            throw new IllegalStateException();
        }

        final char myRes = chars[readIndex];
        chars[readIndex] = '\u0000';
        readIndex = (readIndex + 1) % chars.length;

        return myRes;
    }

    char peek() {
        if (isEmpty()) {
            throw new IllegalStateException();
        }

        return chars[readIndex];
    }

    int size() {
        return Math.abs(readIndex - writeIndex);
    }

    boolean isEmpty() {
        return readIndex == writeIndex;
    }
}