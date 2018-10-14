package com.chelseaurquhart.securejson;

import java.io.IOException;

interface IReader<T> {
    T read(IterableCharSequence parIterator) throws IOException;

    boolean isStart(IterableCharSequence parIterator);
}
