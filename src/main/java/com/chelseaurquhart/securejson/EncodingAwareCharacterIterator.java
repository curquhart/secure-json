package com.chelseaurquhart.securejson;

import com.chelseaurquhart.securejson.JSONDecodeException.MalformedJSONException;

import java.io.IOException;

abstract class EncodingAwareCharacterIterator implements ICharacterIterator {
    private Character nextChar;
    private int offset;
    private boolean initialized;

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

        // UTF8
        final char myNextChar = peek();
        switch (myNextChar) {
            case myUtf8BomChar0:
                next();
                if (!hasNext() || next() != myUtf8BomChar1 || !hasNext() || next() != myUtf8BomChar2) {
                    throw new MalformedJSONException(this);
                }
                // UTF-8 but single byte characters
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
            findEncoding();
        }

        if (nextChar == null) {
            nextChar = readNextChar();
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
            return readNextChar();
        } catch (final IOException myException) {
            throw new RuntimeException(myException);
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
