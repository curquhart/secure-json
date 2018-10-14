package com.chelseaurquhart.securejson;

import java.io.IOException;

class IterableCharSequence implements ISizeable, ICharacterIterator {
    private final CharSequence chars;
    private int offset;

    IterableCharSequence(final CharSequence parChars) {
        this.chars = parChars;
    }

    @Override
    public Character peek() {
        return chars.charAt(offset);
    }

    @Override
    public int getOffset() {
        return offset;
    }

    @Override
    public boolean hasNext() {
        return offset < chars.length();
    }

    @Override
    public Character next() {
        return chars.charAt(offset++);
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
    public int getSize() {
        return chars.length();
    }
}
