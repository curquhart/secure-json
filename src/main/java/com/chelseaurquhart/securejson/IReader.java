package com.chelseaurquhart.securejson;

import java.io.IOException;

interface IReader<T> {
    T read(ICharacterIterator parIterator) throws IOException;

    boolean isStart(ICharacterIterator parIterator) throws IOException;
}
