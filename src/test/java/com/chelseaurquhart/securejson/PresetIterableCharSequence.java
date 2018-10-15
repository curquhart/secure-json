package com.chelseaurquhart.securejson;

class PresetIterableCharSequence extends IterableCharSequence {
    private final int offset;

    PresetIterableCharSequence() {
        this(0);
    }

    PresetIterableCharSequence(final int parOffset) {
        super("");
        offset = parOffset;
    }

    @Override
    public int getOffset() {
        return offset;
    }
}
