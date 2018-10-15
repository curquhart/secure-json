package com.chelseaurquhart.securejson;

import com.chelseaurquhart.securejson.JSONDecodeException.MalformedMapException;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

class MapReader implements IReader<Map<CharSequence, Object>> {
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
    public Map<CharSequence, Object> read(final ICharacterIterator parIterator) throws IOException {
        final Map<CharSequence, Object> myMap = new LinkedHashMap<>();

        if (parIterator.peek() != JSONSymbolCollection.Token.L_CURLY.getShortSymbol()) {
            throw new MalformedMapException(parIterator);
        }

        parIterator.next();
        jsonReader.moveToNextToken(parIterator);

        if (!parIterator.hasNext()) {
            throw new MalformedMapException(parIterator);
        }

        char myNextChar;
        if (parIterator.peek() != JSONSymbolCollection.Token.R_CURLY.getShortSymbol()) {
            boolean myIsMapEnd;

            do {
                final CharSequence myKey = stringReader.read(parIterator);
                jsonReader.moveToNextToken(parIterator);
                if (parIterator.peek() != JSONSymbolCollection.Token.COLON.getShortSymbol()) {
                    throw new MalformedMapException(parIterator);
                }
                parIterator.next();
                final Object myValue = jsonReader.read(parIterator, false);
                myMap.put(myKey, myValue);
                jsonReader.moveToNextToken(parIterator);
                if (!parIterator.hasNext()) {
                    throw new MalformedMapException(parIterator);
                }
                myNextChar = parIterator.peek();
                myIsMapEnd = myNextChar == JSONSymbolCollection.Token.R_CURLY.getShortSymbol();
                if (myNextChar == JSONSymbolCollection.Token.COMMA.getShortSymbol()) {
                    parIterator.next();
                } else if (!myIsMapEnd) {
                    throw new MalformedMapException(parIterator);
                }
            } while (parIterator.hasNext() && !myIsMapEnd);

            if (!myIsMapEnd) {
                throw new MalformedMapException(parIterator);
            }
        }

        parIterator.next();
        return myMap;
    }
}
