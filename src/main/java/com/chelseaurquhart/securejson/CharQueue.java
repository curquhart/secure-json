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

import com.chelseaurquhart.securejson.JSONException.JSONRuntimeException;

/**
 * Circular array queue. Has optimizations to prevent math if size is 1.
 *
 * @exclude
 */
class CharQueue {
    private static final int SINGLE_CHAR_LEN = 1;

    private transient char[] chars;
    private transient int readIndex;
    private transient int writeIndex;

    CharQueue(final int parCapacity) {
        chars = new char[parCapacity];
    }

    void add(final char parChar) {
        if (chars.length == SINGLE_CHAR_LEN) {
            if (writeIndex != 0) {
                throw new JSONRuntimeException(new IllegalStateException());
            }
            chars[0] = parChar;
            writeIndex = SINGLE_CHAR_LEN;
            return;
        }

        if (readIndex == (writeIndex + 1) % chars.length) {
            throw new JSONRuntimeException(new IllegalStateException());
        }

        chars[writeIndex] = parChar;
        writeIndex = (writeIndex + SINGLE_CHAR_LEN) % chars.length;
    }

    char pop() {
        if (isEmpty()) {
            throw new JSONRuntimeException(new IllegalStateException());
        }

        final char myRes = chars[readIndex];
        chars[readIndex] = '\u0000';

        if (chars.length == SINGLE_CHAR_LEN) {
            writeIndex = 0;
        } else {
            readIndex = (readIndex + SINGLE_CHAR_LEN) % chars.length;
        }

        return myRes;
    }

    char peek() {
        if (isEmpty()) {
            throw new JSONRuntimeException(new IllegalStateException());
        }

        return chars[readIndex];
    }

    int size() {
        final int myRet;
        if (chars.length == SINGLE_CHAR_LEN) {
            myRet = writeIndex;
        } else {
            myRet = Math.abs(readIndex - writeIndex);
        }

        return myRet;
    }

    boolean isEmpty() {
        return readIndex == writeIndex;
    }

    int capacity() {
        return chars.length;
    }
}
