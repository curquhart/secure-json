package com.chelseaurquhart.securejson;

import io.github.novacrypto.SecureCharBuffer;

import java.io.Closeable;

final class ManagedSecureCharBuffer implements Closeable, AutoCloseable, CharSequence, ICharacterWriter {
    private static final int INITIAL_CAPACITY = 32;

    private SecureCharBuffer secureBuffer;
    private int capacity;

    ManagedSecureCharBuffer() {
        this(0);
    }

    ManagedSecureCharBuffer(final int parInitialCapacity) {
        if (parInitialCapacity > 0) {
            secureBuffer = SecureCharBuffer.withCapacity(parInitialCapacity);
            capacity = parInitialCapacity;
        } else {
            secureBuffer = SecureCharBuffer.withCapacity(INITIAL_CAPACITY);
            capacity = INITIAL_CAPACITY;
        }
    }

    @Override
    public void append(final char parChar) {
        // allocation is expensive so if we're going char-by-char, double the capacity if we run out of space.
        checkSizeAndReallocate(1, capacity / 2);

        secureBuffer.append(parChar);
    }

    @Override
    public void append(final CharSequence parChars) {
        // allocation is expensive so if we're going char-by-char, double the capacity if we run out of space.
        checkSizeAndReallocate(parChars.length(), capacity / 2);

        secureBuffer.append(parChars);
    }

    @Override
    public void close() {
        secureBuffer.close();
    }

    private void checkSizeAndReallocate(final int parExtraDataLength, final int parMinAdditionalAllocationSize) {
        if (length() + parExtraDataLength > capacity) {
            try (final SecureCharBuffer myOldSecureBuffer = secureBuffer) {
                final int myNewCapacity = capacity + parMinAdditionalAllocationSize;
                final SecureCharBuffer mySecureBuffer = SecureCharBuffer.withCapacity(myNewCapacity);
                mySecureBuffer.append(myOldSecureBuffer);
                capacity = myNewCapacity;
                secureBuffer = mySecureBuffer;
            }
        }
    }

    @Override
    public int length() {
        return secureBuffer.length();
    }

    @Override
    public char charAt(final int parIndex) {
        return secureBuffer.charAt(parIndex);
    }

    @Override
    public CharSequence subSequence(final int parStart, final int parEnd) {
        return secureBuffer.subSequence(parStart, parEnd);
    }

    @Override
    public String toString() {
        return secureBuffer.toString();
    }
}
