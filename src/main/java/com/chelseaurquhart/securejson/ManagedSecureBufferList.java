package com.chelseaurquhart.securejson;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

class ManagedSecureBufferList implements Closeable, AutoCloseable {
    private final List<ManagedSecureCharBuffer> secureBuffers;

    ManagedSecureBufferList() {
        secureBuffers = new ArrayList<>();
    }

    @Override
    public void close() throws IOException {
        for (final ManagedSecureCharBuffer myBuffer : secureBuffers) {
            myBuffer.close();
        }

        secureBuffers.clear();
    }

    void addSecureBuffer(final ManagedSecureCharBuffer parSecureBuffer) {
        secureBuffers.add(parSecureBuffer);
    }
}
