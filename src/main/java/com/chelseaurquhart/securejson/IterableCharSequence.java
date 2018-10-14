package com.chelseaurquhart.securejson;

import java.io.IOException;
import java.util.Iterator;

class IterableCharSequence implements CharSequence, Iterator<Character> {
    private final CharSequence chars;
    private int offset;

    IterableCharSequence(final CharSequence parChars) {
        this.chars = parChars;
    }

    Character peek() {
        return chars.charAt(offset);
    }

    int getOffset() {
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
    public int length() {
        return chars.length();
    }

    @Override
    public char charAt(final int parOffset) {
        return chars.charAt(parOffset);
    }

    @Override
    public CharSequence subSequence(final int parStart, final int parEnd) {
        return chars.subSequence(parStart, parEnd);
    }

    @Override
    public void remove() {
        try {
            throw new UnsupportedOperationException(Messages.get(Messages.Key.ERROR_ITERATOR_REMOVE_NOT_ALLOWED));
        } catch (final IOException myException) {
            throw new RuntimeException(myException);
        }
    }
}
