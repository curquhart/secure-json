package com.chelseaurquhart.securejson;

import com.chelseaurquhart.securejson.JSONDecodeException.MalformedMapException;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

class MapReader implements IReader {
    private final JSONReader jsonReader;
    private final StringReader stringReader;

    MapReader(final JSONReader parJsonReader, final StringReader parStringReader) {
        jsonReader = parJsonReader;
        stringReader = parStringReader;
    }

    @Override
    public boolean isStart(final ICharacterIterator parIterator) throws IOException {
        return parIterator.peek() == JSONSymbolCollection.Token.L_CURLY.getShortSymbol();
    }

    @Override
    public SymbolType getSymbolType(final ICharacterIterator parIterator) throws IOException {
        final char myChar = parIterator.peek();

        if (myChar == JSONSymbolCollection.Token.R_CURLY.getShortSymbol()) {
            return SymbolType.END;
        } else if (myChar == JSONSymbolCollection.Token.COLON.getShortSymbol()) {
            return SymbolType.SEPARATOR;
        } else if (myChar == JSONSymbolCollection.Token.COMMA.getShortSymbol()) {
            return SymbolType.RESERVED;
        }

        return SymbolType.UNKNOWN;
    }

    @Override
    public MapReader.Container read(final ICharacterIterator parIterator) throws IOException {
        final Map<CharSequence, Object> myMap = new LinkedHashMap<>();

        if (parIterator.peek() != JSONSymbolCollection.Token.L_CURLY.getShortSymbol()) {
            throw new MalformedMapException(parIterator);
        }

        parIterator.next();
        jsonReader.moveToNextToken(parIterator);

        if (!parIterator.hasNext()) {
            throw new MalformedMapException(parIterator);
        }

        if (parIterator.peek() == JSONSymbolCollection.Token.R_CURLY.getShortSymbol()) {
            return new Container(myMap, null);
        } else {
            return new Container(myMap, readKey(parIterator));
        }
    }

    @Override
    public void addValue(final ICharacterIterator parIterator, final Object parCollection, final Object parValue)
            throws IOException {
        final Container myContainer = objectToContainer(parCollection);
        myContainer.map.put(myContainer.key, parValue);
        jsonReader.moveToNextToken(parIterator);
        final SymbolType mySymbolType = getSymbolType(parIterator);
        if (mySymbolType == SymbolType.RESERVED) {
            parIterator.next();
            jsonReader.moveToNextToken(parIterator);
            myContainer.key = readKey(parIterator);
        } else if (mySymbolType == SymbolType.UNKNOWN) {
            throw new MalformedMapException(parIterator);
        }
    }

    @Override
    public Object normalizeCollection(final Object parValue) {
        if (parValue instanceof Container) {
            return ((Container) parValue).map;
        }

        return parValue;
    }

    private CharSequence readKey(final ICharacterIterator parIterator) throws IOException {
        final CharSequence myKey = stringReader.read(parIterator);
        jsonReader.moveToNextToken(parIterator);
        if (parIterator.peek() != JSONSymbolCollection.Token.COLON.getShortSymbol()) {
            throw new MalformedMapException(parIterator);
        }
        parIterator.next();
        jsonReader.moveToNextToken(parIterator);
        final SymbolType mySymbolType = getSymbolType(parIterator);
        if (mySymbolType != SymbolType.UNKNOWN && mySymbolType != SymbolType.END) {
            throw new MalformedMapException(parIterator);
        }

        return myKey;
    }

    private Container objectToContainer(final Object parValue) {
        return (Container) parValue;
    }

    private static final class Container {
        private final Map<CharSequence, Object> map;
        private CharSequence key;

        private Container(final Map<CharSequence, Object> parMap, final CharSequence parKey) {
            map = parMap;
            key = parKey;
        }
    }
}
