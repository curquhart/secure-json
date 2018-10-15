package com.chelseaurquhart.securejson;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;

class IterableInputStream implements ISizeable, ICharacterIterator, Closeable {
    private final InputStream inputStream;
    private Integer nextChar;
    private int offset;

    IterableInputStream(final InputStream parInputStream) {
        this.inputStream = parInputStream;
    }

    @Override
    public Character peek() throws IOException {
        cacheNextChar();

        return (char) nextChar.intValue();
    }

    @Override
    public int getOffset() {
        return offset;
    }

    @Override
    public boolean hasNext() {
        cacheNextChar();
        return nextChar != -1;
    }

    @Override
    public Character next() {
        final char myNextChar = (char) readNextChar();
        offset++;
        return myNextChar;
    }

    @Override
    public void remove() {
        try {
            throw new UnsupportedOperationException(Messages.get(Messages.Key.ERROR_ITERATOR_REMOVE_NOT_ALLOWED));
        } catch (final IOException myException) {
            throw new RuntimeException(myException);
        }
    }

    @Override
    public int getSize() throws IOException {
        return inputStream.available() + offset;
    }

    @Override
    public void close() throws IOException {
        inputStream.close();
    }

    private void cacheNextChar() {
        if (nextChar == null) {
            nextChar = readNextChar();
        }
    }

    private int readNextChar() {
        if (nextChar != null) {
            final int myChar = nextChar;
            nextChar = null;
            return myChar;
        }

        try {
            return inputStream.read();
        } catch (final IOException myException) {
            throw new RuntimeException(myException);
        }
    }
}
