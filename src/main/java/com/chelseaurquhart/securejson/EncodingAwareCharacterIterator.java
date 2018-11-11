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

import com.chelseaurquhart.securejson.JSONDecodeException.InvalidTokenException;
import com.chelseaurquhart.securejson.JSONDecodeException.MalformedJSONException;
import com.chelseaurquhart.securejson.JSONException.JSONRuntimeException;

import java.io.IOException;
import java.util.NoSuchElementException;

/**
 * @exclude
 */
abstract class EncodingAwareCharacterIterator implements ICharacterIterator {
    private static final int UTF16_BYTES = 2;
    private static final int UTF32_BYTES = 4;

    // only enough space to store the next character if we read too far.
    private static final int INITIAL_QUEUE_CAPACITY = 3;
    // after encoding is detected, reduce to a single character. This allows for optimization within the queue.
    private static final int RUNTIME_QUEUE_CAPACITY = 1;

    private static final char UTF8_BOM_CHAR0 = '\u00ef';
    private static final char UTF8_BOM_CHAR1 = '\u00bb';
    private static final char UTF8_BOM_CHAR2 = '\u00bf';

    private static final char UTF16_BOM_CHAR0 = '\u00fe';
    private static final char UTF16_BOM_CHAR1 = '\u00ff';

    private static final char UTF_BIG_ENDIAN = '\ufeff';
    private static final char UTF_LITTLE_ENDIAN = '\ufffe';
    private static final char NULL = '\u0000';

    private transient CharQueue charQueue;
    private transient int offset;
    private transient State state = State.UNINITIALIZED;
    private transient Encoding encoding;

    EncodingAwareCharacterIterator() {
        this(0);
    }

    EncodingAwareCharacterIterator(final int parOffset) {
        offset = parOffset;
        charQueue = new CharQueue(INITIAL_QUEUE_CAPACITY);
    }

    @Override
    public final Character peek() throws IOException, JSONException {
        cacheAndGetNextChar();

        return charQueue.peek();
    }

    Encoding getEncoding() {
        return encoding;
    }

    private Encoding findEncoding() throws IOException, JSONException {
        // We can accept either encoding. UTF-8 characters, other than the BOM, are not allowed in JSON, so these are
        // the only special characters we need to handle.

        if (!hasNext()) {
            return Encoding.UTF8;
        }
        final char myNextChar = peek();
        switch (myNextChar) {
            case UTF8_BOM_CHAR0:
                next();
                if (!hasNext() || peek() != UTF8_BOM_CHAR1) {
                    throw new MalformedJSONException(this);
                }
                next();
                if (!hasNext() || peek() != UTF8_BOM_CHAR2) {
                    throw new MalformedJSONException(this);
                }
                next();
                return Encoding.UTF8;
            case UTF_BIG_ENDIAN:
                next();
                if (findPartialUtf32Encoding() == Encoding.UTF32) {
                    return Encoding.UTF32BE;
                }
                return Encoding.UTF16BE;
            case UTF_LITTLE_ENDIAN:
                next();
                if (findPartialUtf32Encoding() == Encoding.UTF32) {
                    return Encoding.UTF32LE;
                }
                return Encoding.UTF16LE;
            case UTF16_BOM_CHAR0:
                next();
                // big-endian
                if (!hasNext() || peek() != UTF16_BOM_CHAR1) {
                    throw new MalformedJSONException(this);
                }
                next();
                return Encoding.UTF16BE;
            case UTF16_BOM_CHAR1:
                return findUtf16Or32LittleEndianEncoding(UTF16_BOM_CHAR0);
            default:
                return findEncodingWithoutBOM();
        }
    }

    private Encoding findUtf16Or32LittleEndianEncoding(final char parUtf16BomChar0) throws IOException, JSONException {
        next();
        // little-endian
        if (!hasNext() || peek() != parUtf16BomChar0) {
            throw new MalformedJSONException(this);
        }
        next();
        if (findPartialUtf32Encoding() == Encoding.UTF32) {
            return Encoding.UTF32LE;
        }
        if (forceReadNullChars(1) == EOFMarker.EOF) {
            throw new MalformedJSONException(this);
        }
        return Encoding.UTF16LE;
    }

    private Encoding findEncodingWithoutBOM() throws IOException, JSONException {
        final Encoding myPartialEncoding = findPartialUtf32Encoding();
        if (myPartialEncoding == Encoding.UTF32) {
            final Encoding myEncoding = findEncoding();
            if (myEncoding == Encoding.UTF16BE) {
                return Encoding.UTF32BE;
            }
            throw new MalformedJSONException(this);
        } else if (myPartialEncoding == Encoding.UTF16) {
            return Encoding.UTF16BE;
        }

        final int myQueueSize = charQueue.size();
        final int myNullCount = readNullChars(UTF32_BYTES - 1);

        if (myNullCount == UTF32_BYTES - 1) {
            return Encoding.UTF32LE;
        } else if (myNullCount == UTF16_BYTES - 1) {
            // For UTF16BE, the last character is always the next non-NULL character except where EOF. We can
            // determine if we should force the next null marker by checking our queue size before and after the
            // above read.
            if (myQueueSize != charQueue.size() && forceReadNullChars(UTF16_BYTES - 1) == EOFMarker.EOF) {
                throw new MalformedJSONException(this);
            }
            return Encoding.UTF16LE;
        } else if (myNullCount != 0 && myNullCount != -1) {
            throw new MalformedJSONException(this);
        } else {
            return Encoding.UTF8;
        }
    }

    private Encoding findPartialUtf32Encoding() throws IOException, JSONException {
        final char myUtf32Char = NULL;

        if (hasNext() && peek() == myUtf32Char) {
            next();
            if (!hasNext() || peek() != myUtf32Char) {
                return Encoding.UTF16;
            }
            next();
            return Encoding.UTF32;
        }

        return Encoding.UTF8_IMPLICIT;
    }

    private Character cacheAndGetNextChar() throws IOException, JSONException {
        if (state == State.UNINITIALIZED) {
            state = State.CHECKING_CHARSET;
            encoding = findEncoding();
            state = State.CHECKED_CHARSET;
        }

        if (charQueue.isEmpty()) {
            final Character myNextChar = readAndProcessNextChar();
            if (null != myNextChar) {
                charQueue.add(myNextChar);
            }

            return myNextChar;
        }

        return charQueue.peek();
    }

    @Override
    public final boolean hasNext() {
        try {
            return cacheAndGetNextChar() != null;
        } catch (final IOException | JSONException myException) {
            throw new JSONRuntimeException(myException);
        }
    }


    @Override
    public final int getOffset() {
        return offset;
    }

    @Override
    public final Character next() {
        if (!charQueue.isEmpty()) {
            offset++;
            return charQueue.pop();
        }

        // once our queue is empty, change capacity. There is some optimization built around a single char queue.
        if (state == State.CHECKED_CHARSET && charQueue.capacity() == INITIAL_QUEUE_CAPACITY) {
            charQueue = new CharQueue(RUNTIME_QUEUE_CAPACITY);
            state = State.INITIALIZED;
        }

        offset++;
        final Character myChar;

        try {
            myChar = readAndProcessNextChar();
        } catch (final IOException | JSONDecodeException myException) {
            throw new JSONRuntimeException(myException);
        }

        if (myChar == null) {
            throw new JSONRuntimeException(new NoSuchElementException());
        }
        return myChar;
    }

    private Character readAndProcessNextChar() throws IOException, JSONDecodeException {
        if (encoding == null || encoding == Encoding.UTF8) {
            return readNextChar();
        }

        final Character myChar;
        switch (encoding) {
            case UTF16BE:
                // support UTF8 with UTF16 BOM. >.<
                if (readNullChars(UTF16_BYTES - 1) == 0) {
                    // We read a BOM, but we do not have nulls in the data. This means it is actually UTF8.
                    encoding = Encoding.UTF8;
                }
                if (charQueue.isEmpty()) {
                    myChar = readNextChar();
                } else {
                    myChar = charQueue.pop();
                }
                break;
            case UTF16LE:
                myChar = readNextChar();
                forceReadNullChars(UTF16_BYTES - 1);
                break;
            case UTF32LE:
                myChar = readNextChar();
                forceReadNullChars(UTF32_BYTES - 1);
                break;
            case UTF32BE:
                forceReadNullChars(UTF32_BYTES - 1);
                myChar = readNextChar();
                break;
            default:
                throw new JSONDecodeException(this, Messages.Key.ERROR_INVALID_ENCODING);
        }

        return myChar;
    }

    private EOFMarker forceReadNullChars(final int parCount) throws IOException, InvalidTokenException {
        final int myReadChars = readNullChars(parCount);
        if (myReadChars == -1) {
            return EOFMarker.EOF;
        }
        if (myReadChars != parCount) {
            throw new InvalidTokenException(this);
        }

        return EOFMarker.UNKNOWN;
    }

    private int readNullChars(final int parCount) throws IOException {
        int myReadNulls = 0;
        for (int myIndex = 0; myIndex < parCount; myIndex++) {
            final Character myChar = readNextChar();
            if (myChar == null) {
                // EOF
                return -1;
            } else if (myChar != NULL) {
                charQueue.add(myChar);
                return myReadNulls;
            }
            myReadNulls++;
            offset++;
        }

        return myReadNulls;
    }

    /**
     * Reads the next available character.
     *
     * @return The next available character or NULL if there are no more.
     * @throws IOException On read failure.
     */
    protected abstract Character readNextChar() throws IOException;

    enum Encoding {
        UTF8,
        UTF16BE,
        UTF16LE,
        UTF32BE,
        UTF32LE,
        UTF32,
        UTF16,
        UTF8_IMPLICIT
    }

    private enum EOFMarker {
        EOF,
        UNKNOWN
    }

    private enum State {
        UNINITIALIZED,
        CHECKING_CHARSET,
        CHECKED_CHARSET,
        INITIALIZED
    }
}
