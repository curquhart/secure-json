package com.chelseaurquhart.securejson;

import com.chelseaurquhart.securejson.JSONDecodeException.MalformedListException;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

/**
 * @exclude
 */
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
    public Container read(final ICharacterIterator parIterator) throws IOException {
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

        return new Container();
    }

    @Override
    public void addValue(final ICharacterIterator parIterator, final Object parCollection, final Object parValue)
            throws IOException {
        final Container myContainer = objectToContainer(parCollection);
        myContainer.add(parValue);

        jsonReader.moveToNextToken(parIterator);
        if (getSymbolType(parIterator) == SymbolType.UNKNOWN) {
            throw new MalformedListException(parIterator);
        }
    }

    @Override
    public Object normalizeCollection(final Object parValue) {
        if (parValue instanceof Container) {
            return ((Container) parValue).getList();
        }

        return parValue;
    }

    private Container objectToContainer(final Object parValue) {
        return (Container) parValue;
    }

    private static final class Container {
        private List<Object> list;

        private Container() {
        }

        private void add(final Object parValue) {
            getList().add(parValue);
        }

        private List<Object> getList() {
            if (list == null) {
                list = new LinkedList<>();
            }

            return list;
        }
    }

}
