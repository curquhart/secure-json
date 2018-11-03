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

import java.io.IOException;

/**
 * @exclude
 */
class IterableCharSequence extends EncodingAwareCharacterIterator implements ICharacterIterator {
    private final CharSequence chars;
    private int offset;

    IterableCharSequence(final CharSequence parChars) {
        this(parChars, 0);
    }

    IterableCharSequence(final CharSequence parChars, final int parOffset) {
        super(parOffset);
        this.chars = parChars;
    }

    @Override
    public void remove() {
        try {
            throw new UnsupportedOperationException(Messages.get(Messages.Key.ERROR_ITERATOR_REMOVE_NOT_ALLOWED));
        } catch (final IOException myException) {
            throw new JSONRuntimeException(myException);
        }
    }

    @Override
    protected Character readNextChar() {
        if (offset < chars.length()) {
            return chars.charAt(offset++);
        }

        return null;
    }
}
