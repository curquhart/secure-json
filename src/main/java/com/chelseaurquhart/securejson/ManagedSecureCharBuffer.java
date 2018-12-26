/*
 * Copyright 2018 Chelsea Urquhart
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.chelseaurquhart.securejson;

import com.chelseaurquhart.securejson.JSONException.JSONRuntimeException;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.List;

/**
 * @exclude
 */
final class ManagedSecureCharBuffer implements IWritableCharSequence {
    /**
     * @exclude
     */
    static final int INITIAL_CAPACITY = 32;

    private final transient int initialCapacity;
    private final transient List<CharSequence> buffers;
    private final transient List<IWritableCharSequence> writeBuffers;
    private transient Capacity capacityRestriction;
    private transient IWritableCharSequence writeBuffer;

    ManagedSecureCharBuffer(final int parInitialCapacity) {
        if (parInitialCapacity > 0) {
            initialCapacity = parInitialCapacity;
        } else {
            initialCapacity = INITIAL_CAPACITY;
        }
        buffers = new LinkedList<CharSequence>();
        writeBuffers = new LinkedList<IWritableCharSequence>();
        capacityRestriction = Capacity.UNKNOWN;
    }

    private ManagedSecureCharBuffer(final int parInitialCapacity, final List<CharSequence> parBuffers,
                                    final List<IWritableCharSequence> parWriteBuffers) {
        initialCapacity = parInitialCapacity;
        buffers = parBuffers;
        writeBuffers = parWriteBuffers;
        capacityRestriction = Capacity.UNKNOWN;
    }

    @Override
    public void append(final char parChar) throws IOException {
        final int myMaxSize = initialCapacity + 1;

        if (writeBuffer == null || (capacityRestriction == Capacity.RESTRICTED
                && writeBuffer.length() + 1 >= writeBuffer.getCapacity())) {
            writeBuffer = new ObfuscatedByteBuffer(myMaxSize);
            if (writeBuffer.isRestrictedToCapacity()) {
                capacityRestriction = Capacity.RESTRICTED;
            } else {
                capacityRestriction = Capacity.UNRESTRICTED;
            }
            buffers.add(writeBuffer);
            writeBuffers.add(writeBuffer);
        }

        writeBuffer.append(parChar);
    }

    @Override
    public void append(final CharSequence parChars) {
        buffers.add(parChars);
        // we'll need a new buffer or we'll get out of order.
        writeBuffer = null;
    }

    @Override
    public void close() throws IOException {
        for (final IWritableCharSequence myBuffer : writeBuffers) {
            myBuffer.close();
        }
        buffers.clear();
        writeBuffers.clear();
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

        throw new StringIndexOutOfBoundsException();
    }

    @Override
    public CharSequence subSequence(final int parStart, final int parEnd) {
        validateBounds(parStart, parEnd);

        int myStart = parStart;
        int myOffset = 0;
        int myRemainingLength = Math.max(0, parEnd - parStart);
        final LinkedList<CharSequence> myBuffers = new LinkedList<CharSequence>();
        for (final CharSequence myBuffer : buffers) {
            final int myLength = myBuffer.length();
            if (myLength < 0) {
                throw new StringIndexOutOfBoundsException();
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
            if (myRemainingLength == 0) {
                break;
            }
        }

        if (myRemainingLength > 0) {
            final String myMessage;
            try {
                myMessage = Messages.get(Messages.Key.ERROR_BUFFER_OVERFLOW);
            } catch (final IOException myException) {
                throw new JSONRuntimeException(myException);
            }
            throw new StringIndexOutOfBoundsException(myMessage);
        }

        return new ManagedSecureCharBuffer(initialCapacity, myBuffers, writeBuffers);
    }

    @Override
    public boolean isRestrictedToCapacity() {
        return false;
    }

    @Override
    public int getCapacity() {
        return initialCapacity;
    }

    private void validateBounds(final int parStart, final int parEnd) {
        if (parStart < 0 || parEnd < 0 || parEnd < parStart) {
            final String myMessage;
            try {
                myMessage = Messages.get(Messages.Key.ERROR_BAD_SEQUENCE_ARGS);
            } catch (final IOException myException) {
                throw new JSONRuntimeException(myException);
            }
            throw new StringIndexOutOfBoundsException(myMessage);
        }

    }

    @Override
    public String toString() {
        throw new UnsupportedOperationException();
    }

    /**
     * @exclude
     */
    static class ObfuscatedByteBuffer implements CharSequence, IWritableCharSequence {
        private final transient int offset;
        private final transient int capacity;
        private final transient Integer fixedLength;
        private final transient ByteBuffer compositionFirst;
        private final transient ByteBuffer compositionSecond;

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
                throw new StringIndexOutOfBoundsException();
            }

            return (char) ((compositionFirst.get(myOffset) << JSONSymbolCollection.BITS_IN_BYTE)
                | (compositionSecond.get(myOffset) & JSONSymbolCollection.TWO_BYTE));
        }

        @Override
        public void append(final char parChar) throws IOException {
            if (fixedLength != null) {
                throw new UnsupportedOperationException(Messages.get(Messages.Key.ERROR_WRITE_TO_READONLY_BUFFER));
            }
            compositionFirst.put((byte) (parChar >> JSONSymbolCollection.BITS_IN_BYTE));
            compositionSecond.put((byte) (parChar & JSONSymbolCollection.TWO_BYTE));
        }

        @Override
        public CharSequence subSequence(final int parStart, final int parEnd) {
            final int myLength = offset + parEnd;
            if (myLength > length() || parStart < 0) {
                throw new StringIndexOutOfBoundsException();
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
        public void append(final CharSequence parCharSequence) throws IOException {
            final int myLength = parCharSequence.length();
            for (int myIndex = 0; myIndex < myLength; myIndex++) {
                append(parCharSequence.charAt(myIndex));
            }
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

    private enum Capacity {
        UNKNOWN,
        RESTRICTED,
        UNRESTRICTED
    }
}
