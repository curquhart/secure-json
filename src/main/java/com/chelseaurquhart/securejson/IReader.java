package com.chelseaurquhart.securejson;

import java.io.IOException;

/**
 * @exclude
 */
interface IReader {
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

    Object read(ICharacterIterator parIterator) throws IOException;

    void addValue(ICharacterIterator parIterator, Object parCollection, Object parValue) throws IOException;

    boolean isStart(ICharacterIterator parIterator) throws IOException;

    SymbolType getSymbolType(ICharacterIterator parIterator) throws IOException;
}
