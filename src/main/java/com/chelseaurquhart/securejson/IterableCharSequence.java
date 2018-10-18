package com.chelseaurquhart.securejson;

import java.io.IOException;

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
            throw new RuntimeException(myException);
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
