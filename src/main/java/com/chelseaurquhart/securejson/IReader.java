package com.chelseaurquhart.securejson;

import java.io.Closeable;
import java.io.IOException;

/**
 * @exclude
 */
interface IReader<T> extends Closeable {
    Object normalizeCollection(Object parValue);

    /**
     * @exclude
     */
    enum SymbolType {
        END,
        SEPARATOR,
        UNKNOWN,
        RESERVED
    }

    T read(ICharacterIterator parIterator) throws IOException;

    void addValue(ICharacterIterator parIterator, Object parCollection, Object parValue) throws IOException;

    boolean isStart(ICharacterIterator parIterator) throws IOException;

    boolean isContainerType();

    SymbolType getSymbolType(ICharacterIterator parIterator) throws IOException;
}
