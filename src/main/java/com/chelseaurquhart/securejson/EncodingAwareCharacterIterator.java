package com.chelseaurquhart.securejson;

import com.chelseaurquhart.securejson.JSONDecodeException.InvalidTokenException;
import com.chelseaurquhart.securejson.JSONDecodeException.MalformedJSONException;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;

/**
 * @exclude
 */
abstract class EncodingAwareCharacterIterator implements ICharacterIterator {
    private static final int UTF16_BYTES = 2;
    private static final int UTF32_BYTES = 4;

    private final Deque<Character> charQueue;
    private int offset;
    private boolean initialized;
    private Encoding encoding;

    EncodingAwareCharacterIterator() {
        this(0);
    }

    EncodingAwareCharacterIterator(final int parOffset) {
        offset = parOffset;
        charQueue = new ArrayDeque<>();
    }

    @Override
    public final Character peek() throws IOException {
        cacheAndGetNextChar();

        return charQueue.peek();
    }

    private Encoding findEncoding() throws IOException {
        // We can accept either encoding. UTF-8 characters, other than the BOM, are not allowed in JSON, so these are
        // the only special characters we need to handle.

        final char myUtf8BomChar0 = '\u00ef';
        final char myUtf8BomChar1 = '\u00bb';
        final char myUtf8BomChar2 = '\u00bf';

        final char myUtf16BomChar0 = '\u00fe';
        final char myUtf16BomChar1 = '\u00ff';

        final char myUtfBigEndian = '\ufeff';
        final char myUtfLittleEndian = '\ufffe';

        final char myNextChar = peek();
        switch (myNextChar) {
            case myUtf8BomChar0:
                next();
                if (!hasNext() || next() != myUtf8BomChar1 || !hasNext() || next() != myUtf8BomChar2) {
                    throw new MalformedJSONException(this);
                }
                return Encoding.UTF8;
            case myUtfBigEndian:
                next();
                if (findPartialUtf32Encoding() == Encoding.UTF32) {
                    return Encoding.UTF32BE;
                }
                return Encoding.UTF16BE;
            case myUtfLittleEndian:
                next();
                if (findPartialUtf32Encoding() == Encoding.UTF32) {
                    return Encoding.UTF32LE;
                }
                return Encoding.UTF16LE;
            case myUtf16BomChar0:
                next();
                // big-endian
                if (!hasNext() || next() != myUtf16BomChar1) {
                    throw new MalformedJSONException(this);
                }
                return Encoding.UTF16BE;
            case myUtf16BomChar1:
                next();
                // little-endian
                if (!hasNext() || next() != myUtf16BomChar0) {
                    throw new MalformedJSONException(this);
                }
                if (findPartialUtf32Encoding() == Encoding.UTF32) {
                    return Encoding.UTF32LE;
                }
                if (forceReadNullChars(1) == EOFMarker.EOF) {
                    throw new MalformedJSONException(this);
                }
                return Encoding.UTF16LE;
            default:
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
    }

    private Encoding findPartialUtf32Encoding() throws IOException {
        final char myUtf32Char = '\u0000';

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

    private Character cacheAndGetNextChar() throws IOException {
        if (!initialized) {
            initialized = true;
            encoding = findEncoding();
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
        } catch (final IOException myException) {
            throw new RuntimeException(myException);
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

        try {
            offset++;
            return readAndProcessNextChar();
        } catch (final IOException myException) {
            throw new RuntimeException(myException);
        }
    }

    private Character readAndProcessNextChar() throws IOException {
        if (encoding == null || encoding == Encoding.UTF8) {
            return readNextChar();
        }

        final Character myChar;
        switch (encoding) {
            case UTF16BE:
                // support UTF8 with UTF16 BOM. >.<
                readNullChars(UTF16_BYTES - 1);
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

    private EOFMarker forceReadNullChars(final int parCount) throws IOException {
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
            } else if (myChar != '\u0000') {
                charQueue.add(myChar);
                return myReadNulls;
            }
            myReadNulls++;
            offset++;
        }

        return myReadNulls;
    }

    protected abstract Character readNextChar() throws IOException;

    private enum Encoding {
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
}
