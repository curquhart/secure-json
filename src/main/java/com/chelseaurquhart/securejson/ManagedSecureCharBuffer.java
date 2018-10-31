package com.chelseaurquhart.securejson;

import java.io.Closeable;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.LinkedList;

/**
 * @exclude
 */
final class ManagedSecureCharBuffer implements Closeable, AutoCloseable, CharSequence, ICharacterWriter {
    private static final int INITIAL_CAPACITY = 32;
    private static final int HASH_PRIME = 31;

    private final int initialCapacity;
    private final LinkedList<CharSequence> buffers;
    private byte[] bytes;
    private final Settings settings;

    ManagedSecureCharBuffer(final Settings parSettings) {
        this(0, parSettings);
    }

    ManagedSecureCharBuffer(final int parInitialCapacity, final Settings parSettings) {
        if (parInitialCapacity > 0) {
            initialCapacity = parInitialCapacity;
        } else {
            initialCapacity = INITIAL_CAPACITY;
        }
        buffers = new LinkedList<>();
        settings = parSettings;
    }

    private ManagedSecureCharBuffer(final int parInitialCapacity, final LinkedList<CharSequence> parBuffers,
                                    final Settings parSettings) {
        initialCapacity = parInitialCapacity;
        buffers = parBuffers;
        settings = parSettings;
    }

    @Override
    public void append(final char parChar) throws IOException {
        final int myMaxSize = initialCapacity + 1;

        final CharSequence myHeadBuffer;
        if (buffers.isEmpty()) {
            myHeadBuffer = null;
        } else {
            myHeadBuffer = buffers.getLast();
        }

        final CharSequence myWriteBuffer;
        if (!(myHeadBuffer instanceof IWritableCharSequence)
                || (myHeadBuffer.length() + 1 >= ((IWritableCharSequence) myHeadBuffer).getCapacity()
                && ((IWritableCharSequence) myHeadBuffer).isRestrictedToCapacity())) {
            try {
                myWriteBuffer = settings.getWritableCharBufferFactory().accept(myMaxSize);
            } catch (final Exception myException) {
                if (myException instanceof IOException) {
                    throw (IOException) myException;
                }

                throw new JSONException(myException);
            }
            buffers.add(myWriteBuffer);
        } else {
            myWriteBuffer = myHeadBuffer;
        }
        ((ObfuscatedByteBuffer) myWriteBuffer).append(parChar);
    }

    @Override
    public void append(final CharSequence parChars) {
        buffers.add(parChars);
    }

    @Override
    public void close() throws IOException {
        for (final CharSequence myBuffer : buffers) {
            if (myBuffer instanceof Closeable) {
                ((Closeable) myBuffer).close();
            }
        }
        buffers.clear();
        closeBytes();
    }

    byte[] getBytes() {
        closeBytes();

        CharBuffer myCharBuffer = null;
        ByteBuffer myByteBuffer = null;
        final int myLength = length();
        try {
            myCharBuffer = CharBuffer.allocate(myLength);
            for (final CharSequence myBuffer : buffers) {
                final int mySubLength = myBuffer.length();
                for (int myIndex = 0; myIndex < mySubLength; myIndex++) {
                    myCharBuffer.append(myBuffer.charAt(myIndex));
                }
            }
            myCharBuffer.position(0);
            myByteBuffer = StandardCharsets.UTF_8.encode(myCharBuffer);
            if (myByteBuffer.limit() == myByteBuffer.capacity() && myByteBuffer.hasArray()) {
                // it is more efficient to take the underlying array as-is, but we must check limit vs capacity because
                // it may have some extra characters.
                bytes = myByteBuffer.array();
                // do not reset the buffer below because we're using the internal buffer!
                myByteBuffer = null;
            } else {
                final int myLimit = myByteBuffer.limit();
                bytes = new byte[myLimit];
                myByteBuffer.get(bytes, 0, myByteBuffer.limit());
            }
        } finally {
            if (myCharBuffer != null) {
                final char[] myNulls = new char[myLength];
                myCharBuffer.position(0);
                myCharBuffer.put(myNulls);
            }
            if (myByteBuffer != null) {
                final byte[] myNulls = new byte[myByteBuffer.limit()];
                myByteBuffer.position(0);
                myByteBuffer.put(myNulls);
            }
        }

        return bytes;
    }

    @Override
    public int length() {
        int myLength = 0;

        for (final CharSequence myBuffer : buffers) {
            myLength += myBuffer.length();
        }

        return myLength;
    }

    @Override
    public char charAt(final int parIndex) {
        int myOffset = 0;
        for (final CharSequence myBuffer : buffers) {
            final int myLength = myBuffer.length();
            if (parIndex < myOffset + myLength) {
                return myBuffer.charAt(parIndex - myOffset);
            }
            myOffset += myLength;
        }

        throw new ArrayIndexOutOfBoundsException();
    }

    @Override
    public CharSequence subSequence(final int parStart, final int parEnd) {
        int myOffset = 0;
        int myStart = parStart;
        int myRemainingLength = parEnd - parStart;
        final LinkedList<CharSequence> myBuffers = new LinkedList<>();
        if (parStart < 0 || parEnd < 0 || parEnd < parStart) {
            final String myMessage;
            try {
                myMessage = Messages.get(Messages.Key.ERROR_BAD_SEQUENCE_ARGS);
            } catch (final IOException myException) {
                throw new RuntimeException(myException);
            }
            throw new ArrayIndexOutOfBoundsException(myMessage);
        }

        for (final CharSequence myBuffer : buffers) {
            int myLength = myBuffer.length();
            if (myLength < 0) {
                throw new ArrayIndexOutOfBoundsException();
            }
            final boolean myHasBuffers = !myBuffers.isEmpty();
            final int myEnd = Math.min(myStart + myRemainingLength, myLength);
            if (myHasBuffers || myOffset + myLength > parStart) {
                myBuffers.add(myBuffer.subSequence(myStart, myEnd));
                myRemainingLength -= myEnd - myStart;
                myStart = 0;
            }
            myOffset += myLength;
            if (myStart > 0) {
                myStart -= myLength;
            }
            if (myRemainingLength < 1) {
                break;
            }
        }

        if (myRemainingLength > 0) {
            final String myMessage;
            try {
                myMessage = Messages.get(Messages.Key.ERROR_BUFFER_OVERFLOW);
            } catch (final IOException myException) {
                throw new RuntimeException(myException);
            }
            throw new ArrayIndexOutOfBoundsException(myMessage);
        }

        return new ManagedSecureCharBuffer(initialCapacity, myBuffers, settings);
    }

    @Override
    public String toString() {
        throw new UnsupportedOperationException();
    }

    private void closeBytes() {
        if (bytes != null) {
            Arrays.fill(bytes, (byte) 0);
            bytes = null;
        }
    }

    @Override
    public boolean equals(final Object parObject) {
        if (this == parObject) {
            return true;
        }
        if (!(parObject instanceof CharSequence)) {
            return false;
        }

        return isEqual(subSequence(0, length()), (CharSequence) parObject);
    }

    @Override
    public int hashCode() {
        return hashCode(subSequence(0, length()));
    }

    private static boolean isEqual(final CharSequence parLhs, final CharSequence parRhs) {
        if (parLhs == null || parRhs == null) {
            return parLhs == parRhs;
        }

        final int myLength = parLhs.length();
        if (parRhs.length() != myLength) {
            return false;
        }

        for (int myIndex = 0; myIndex < myLength; myIndex++) {
            if (parLhs.charAt(myIndex) != parRhs.charAt(myIndex)) {
                return false;
            }
        }

        return true;
    }

    private static int hashCode(final CharSequence parSubSequence) {
        int myHashCode = 0;
        for (int myIndex = parSubSequence.length() - 1; myIndex > 0; myIndex--) {
            myHashCode += HASH_PRIME * (int) parSubSequence.charAt(myIndex);
        }

        return myHashCode;
    }

    /**
     * @exclude
     */
    static class ObfuscatedByteBuffer implements CharSequence, IWritableCharSequence {
        private final int offset;
        private final int capacity;
        private final Integer fixedLength;
        private final ByteBuffer compositionFirst;
        private final ByteBuffer compositionSecond;

        ObfuscatedByteBuffer(final int parCapacity) {
            offset = 0;
            capacity = parCapacity;
            compositionFirst = ByteBuffer.allocateDirect(parCapacity);
            compositionSecond = ByteBuffer.allocateDirect(parCapacity);
            fixedLength = null;
        }

        private ObfuscatedByteBuffer(final int parOffset, final int parCapacity, final int parLength,
                                     final ByteBuffer parCompositionFirst, final ByteBuffer parCompositionSecond) {
            offset = parOffset;
            capacity = parCapacity;
            compositionFirst = parCompositionFirst;
            compositionSecond = parCompositionSecond;
            fixedLength = parLength;
        }

        @Override
        public int length() {
            if (fixedLength == null) {
                return compositionFirst.position() - offset;
            } else {
                return fixedLength - offset;
            }
        }

        @Override
        public char charAt(final int parIndex) {
            final int myOffset = offset + parIndex;
            if (myOffset > capacity) {
                throw new ArrayIndexOutOfBoundsException();
            }

            return (char) ((compositionFirst.get(myOffset) << JSONSymbolCollection.BITS_IN_BYTE)
                | (compositionSecond.get(myOffset) & JSONSymbolCollection.TWO_BYTE));
        }

        public void append(final char parChar) {
            if (fixedLength != null) {
                throw new UnsupportedOperationException("attempt to append to a readonly buffer");
            }
            compositionFirst.put((byte) (parChar >> JSONSymbolCollection.BITS_IN_BYTE));
            compositionSecond.put((byte) ((parChar & JSONSymbolCollection.TWO_BYTE)));
        }

        @Override
        public CharSequence subSequence(final int parStart, final int parEnd) {
            final int myLength = offset + parEnd;
            if (myLength > length() || parStart < 0) {
                throw new ArrayIndexOutOfBoundsException();
            }
            return new ObfuscatedByteBuffer(offset + parStart, capacity, myLength, compositionFirst,
                compositionSecond);
        }

        @Override
        public void close() {
            for (int myIndex = length() - 1; myIndex > 0; myIndex--) {
                compositionFirst.put((byte) 0);
                compositionSecond.put((byte) 0);
                // reset position in case we want to re-use it.
                compositionFirst.position(0);
                compositionSecond.position(0);
            }
        }

        @Override
        public boolean equals(final Object parObject) {
            if (this == parObject) {
                return true;
            }
            if (!(parObject instanceof CharSequence)) {
                return false;
            }

            return isEqual(subSequence(0, length()), (CharSequence) parObject);
        }

        @Override
        public int hashCode() {
            return ManagedSecureCharBuffer.hashCode(subSequence(0, length()));
        }

        @Override
        public String toString() {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean isRestrictedToCapacity() {
            return true;
        }

        @Override
        public int getCapacity() {
            return capacity;
        }
    }
}
