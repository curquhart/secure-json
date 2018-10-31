package com.chelseaurquhart.securejson;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;

/**
 * @exclude
 */
class IterableInputStream extends EncodingAwareCharacterIterator implements ICharacterIterator, Closeable {
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
            throw new RuntimeException(myException);
        }
    }

    @Override
    public void close() throws IOException {
        inputStream.close();
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
