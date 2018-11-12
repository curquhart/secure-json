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
 * @exclude
 */
class IterableCharSequence extends EncodingAwareCharacterIterator implements ICharacterIterator {
    private final transient CharSequence chars;
    private transient int offset;

    IterableCharSequence(final CharSequence parChars) {
        this(parChars, 0);
    }

    IterableCharSequence(final CharSequence parChars, final int parOffset) {
        super(parOffset);
        this.chars = parChars;
    }

    @Override
    public void remove() {
        throw new NotImplementedException(Messages.Key.ERROR_NOT_IMPLEMENTED, "remove");
    }

    @Override
    protected Character readNextChar() {
        if (offset < chars.length()) {
            return chars.charAt(offset++);
        }

        return null;
    }

    @Override
    public boolean canReadRange() {
        return true;
    }

    @Override
    public CharSequence range(final int parStart, final int parEnd) {
        return chars.subSequence(parStart, parEnd);
    }
}
