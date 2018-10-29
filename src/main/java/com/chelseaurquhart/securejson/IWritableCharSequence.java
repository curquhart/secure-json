package com.chelseaurquhart.securejson;

import java.io.Closeable;
import java.io.IOException;

/**
 * Interface representing a CharSequence that can be written to. It must implement close even if it is a NOOP
 * function.
 */
public interface IWritableCharSequence extends CharSequence, Closeable, AutoCloseable {
    /**
     * Append a character to the sequence of characters.
     *
     * @param parChar The character to append.
     * @throws IOException On write failure.
     */
    void append(char parChar) throws IOException;

    /**
     * Check if this buffer is restricted to the initial capacity. If it is, we will create more buffers before going
     * over the initial capacity. If it is not, we will expect it to handle resizing itself.
     *
     * @return True if this buffer is restricted to the initial capacity. False otherwise.
     */
    boolean isRestrictedToCapacity();

    /**
     * Return the max capacity of this buffer.
     * @return The max capacity.
     */
    int getCapacity();
}
