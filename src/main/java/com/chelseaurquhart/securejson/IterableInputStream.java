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
import java.io.InputStream;

/**
 * @exclude
 */
class IterableInputStream extends EncodingAwareCharacterIterator implements ICharacterIterator {
    private static final int UNSIGNED_CONVERT_DIGIT = 0xff;

    private final InputStream inputStream;

    IterableInputStream(final InputStream parInputStream) {
        super();
        this.inputStream = parInputStream;
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
    protected Character readNextChar() throws IOException {
        final int myChar = inputStream.read();
        if (myChar == -1) {
            return null;
        }
        return (char) (UNSIGNED_CONVERT_DIGIT & myChar);
    }
}
