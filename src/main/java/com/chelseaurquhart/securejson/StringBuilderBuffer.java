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
 * StringBuilderBuffer is a buffer that is backed by a string builder. It is significantly more efficient than
 * ManagedSecureCharBuffer at the expense of strings not being cleaned from the JVM and not retaining references to the
 * original sequences (which means inputs won't be cleaned at the end!) This should not be used for sensitive
 * information, but is sensible for most cases.
 *
 */
public final class StringBuilderBuffer implements IWritableCharSequence, IStringable {
    private static final int INITIAL_CAPACITY = 32;
    @SuppressWarnings("PMD.AvoidStringBufferField")
    private final StringBuilder stringBuilder;
    private final boolean eraseOnClose;

    /**
     * Construct a StringBuilderBuffer instance with an initial capacity.
     * @param parInitialCapacity The initial capacity to use. If unspecified, the INITIAL_CAPACITY constant will be used
     * instead.
     */
    StringBuilderBuffer(final int parInitialCapacity) {
        this(parInitialCapacity, true);
    }

    /**
     * Construct a StringBuilderBuffer instance with an initial capacity.
     * @param parInitialCapacity The initial capacity to use. If unspecified, the INITIAL_CAPACITY constant will be used
     * instead.
     * @param parEraseOnClose If true, all buffers will be erased when we finish with it. Otherwise, close will be a
     * NOOP.
     */
    StringBuilderBuffer(final int parInitialCapacity, final boolean parEraseOnClose) {
        if (parInitialCapacity > 0) {
            stringBuilder = new StringBuilder(parInitialCapacity);
        } else {
            stringBuilder = new StringBuilder(INITIAL_CAPACITY);
        }
        eraseOnClose = parEraseOnClose;
    }

    /**
     * Is this restricted to the initial capacity? This is used when this is being managed by another class to determine
     * when more memory needs to be allocated.
     * @return True if this is restricted to the initial capacity.
     */
    @Override
    public boolean isRestrictedToCapacity() {
        return false;
    }

    /**
     * Get the current capacity.
     * @return The capacity.
     */
    @Override
    public int getCapacity() {
        return stringBuilder.capacity();
    }

    /**
     * Append a character.
     * @param parChar The character to append.
     */
    @Override
    public void append(final char parChar) {
        stringBuilder.append(parChar);
    }

    /**
     * Append a sequence of characters.
     * @param parChars The sequence to append.
     */
    @Override
    public void append(final CharSequence parChars) {
        stringBuilder.append(parChars);
    }

    /**
     * Reset the data in our buffer.
     */
    @Override
    public void close() {
        if (eraseOnClose) {
            stringBuilder.setLength(0);
        }
    }

    /**
     * Get our buffer length.
     * @return Our buffer length.
     */
    @Override
    public int length() {
        return stringBuilder.length();
    }

    /**
     * Get the character at the given index.
     * @param parIndex The index to lookup.
     * @return The character at the given index.
     */
    @Override
    public char charAt(final int parIndex) {
        return stringBuilder.charAt(parIndex);
    }

    /**
     * Get the subsequence between parStart and parEnd.
     * @param parStart The start index (inclusive)
     * @param parEnd The end index (exclusive)
     * @return The sequence between parStart and parEnd.
     */
    @Override
    public CharSequence subSequence(final int parStart, final int parEnd) {
        return stringBuilder.subSequence(parStart, parEnd);
    }

    @Override
    public String toString() {
        return stringBuilder.toString();
    }
}
