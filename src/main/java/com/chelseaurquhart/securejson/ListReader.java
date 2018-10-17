package com.chelseaurquhart.securejson;

import com.chelseaurquhart.securejson.JSONDecodeException.MalformedListException;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

class ListReader implements IReader {
    private final JSONReader jsonReader;

    ListReader(final JSONReader parJsonReader) {
        jsonReader = parJsonReader;
    }

    @Override
    public boolean isStart(final ICharacterIterator parIterator) throws IOException {
        return parIterator.peek() == JSONSymbolCollection.Token.L_BRACE.getShortSymbol();
    }

    @Override
    public SymbolType getSymbolType(final ICharacterIterator parIterator) throws IOException {
        final char myChar = parIterator.peek();

        if (myChar == JSONSymbolCollection.Token.R_BRACE.getShortSymbol()) {
            return SymbolType.END;
        } else if (myChar == JSONSymbolCollection.Token.COMMA.getShortSymbol()) {
            return SymbolType.SEPARATOR;
        }

        return SymbolType.UNKNOWN;
    }

    @Override
    public List<Object> read(final ICharacterIterator parIterator) throws IOException {
        final List<Object> myList = new LinkedList<>();

        if (parIterator.peek() != JSONSymbolCollection.Token.L_BRACE.getShortSymbol()) {
            throw new MalformedListException(parIterator);
        }

        parIterator.next();
        jsonReader.moveToNextToken(parIterator);

        if (!parIterator.hasNext()) {
            throw new MalformedListException(parIterator);
        }

        final SymbolType mySymbolType = getSymbolType(parIterator);
        if (mySymbolType != SymbolType.UNKNOWN && mySymbolType != SymbolType.END) {
            throw new MalformedListException(parIterator);
        }

        return myList;
    }

    @Override
    public void addValue(final ICharacterIterator parIterator, final Object parCollection, final Object parValue)
            throws IOException {
        final List<Object> myList = objectToList(parCollection);

        myList.add(parValue);
        jsonReader.moveToNextToken(parIterator);
        if (getSymbolType(parIterator) == SymbolType.UNKNOWN) {
            throw new MalformedListException(parIterator);
        }
    }

    @Override
    public Object normalizeCollection(final Object parValue) {
        return parValue;
    }

    @SuppressWarnings("unchecked")
    private List<Object> objectToList(final Object parValue) {
        return (List<Object>) parValue;
    }
}
