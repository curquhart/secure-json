package com.chelseaurquhart.securejson;

import java.io.IOException;

class PresetIterableCharSequence extends IterableCharSequence {
    PresetIterableCharSequence() throws IOException {
        this(0);
    }

    PresetIterableCharSequence(final int parOffset) throws IOException {
        super("", parOffset);
    }
}
