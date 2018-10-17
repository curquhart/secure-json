package com.chelseaurquhart.securejson;

import io.github.novacrypto.SecureCharBuffer;

final class ManagedSecureCharBuffer implements CharSequence {
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

    void append(final char parChar) {
        // allocation is expensive so if we're going char-by-char, double the capacity if we run out of space.
        checkSizeAndReallocate(1, capacity / 2);

        secureBuffer.append(parChar);
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
