package com.chelseaurquhart.securejson;

import com.chelseaurquhart.securejson.JSONDecodeException.InvalidTokenException;
import com.chelseaurquhart.securejson.JSONDecodeException.MalformedJSONException;

import java.io.IOException;

abstract class EncodingAwareCharacterIterator implements ICharacterIterator {
    private static final int UTF16_BYTES = 2;
    private static final int UTF32_BYTES = 4;

    private Character nextChar;
    private int offset;
    private boolean initialized;
    private Encoding encoding;

    EncodingAwareCharacterIterator() {
        this(0);
    }

    EncodingAwareCharacterIterator(final int parOffset) {
        offset = parOffset;
    }

    @Override
    public final Character peek() throws IOException {
        cacheAndGetNextChar();

        return nextChar;
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
                if (findPartialUtf32Encoding()) {
                    return Encoding.UTF32BE;
                }
                return Encoding.UTF16BE;
            case myUtfLittleEndian:
                next();
                if (findPartialUtf32Encoding()) {
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
                if (findPartialUtf32Encoding()) {
                    return Encoding.UTF32LE;
                }
                readNullChars(1);
                return Encoding.UTF16LE;
            default:
                if (findPartialUtf32Encoding()) {
                    final Encoding myEncoding = findEncoding();
                    if (myEncoding == Encoding.UTF16BE) {
                        return Encoding.UTF32BE;
                    }
                    throw new MalformedJSONException(this);
                }
                return Encoding.UTF8;
        }
    }

    private boolean findPartialUtf32Encoding() throws IOException {
        final char myUtf32Char = '\u0000';

        if (hasNext() && peek() == myUtf32Char) {
            next();
            if (!hasNext() || next() != myUtf32Char) {
                throw new MalformedJSONException(this);
            }
            return true;
        }

        return false;
    }

    private Character cacheAndGetNextChar() throws IOException {
        if (!initialized) {
            initialized = true;
            encoding = findEncoding();
        }

        if (nextChar == null) {
            nextChar = readAndProcessNextChar();
        }

        return nextChar;
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
        if (nextChar != null) {
            try {
                offset++;
                return nextChar;
            } finally {
                nextChar = null;
            }
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
                readNullChars(UTF16_BYTES - 1);
                myChar = readNextChar();
                break;
            case UTF16LE:
                myChar = readNextChar();
                readNullChars(UTF16_BYTES - 1);
                break;
            case UTF32LE:
                myChar = readNextChar();
                readNullChars(UTF32_BYTES - 1);
                break;
            case UTF32BE:
                readNullChars(UTF32_BYTES - 1);
                myChar = readNextChar();
                break;
            default:
                throw new IOException("Invalid encoding");
        }

        return myChar;
    }

    private void readNullChars(final int parCount) throws IOException {
        for (int myIndex = 0; myIndex < parCount; myIndex++) {
            offset++;
            final Character myChar = readNextChar();
            if (myChar == null) {
                return;
            } else if (myChar != '\u0000') {
                throw new InvalidTokenException(this);
            }

        }
    }

    protected abstract Character readNextChar() throws IOException;

    private enum Encoding {
        UTF8,
        UTF16BE,
        UTF16LE,
        UTF32BE,
        UTF32LE
    }
}
