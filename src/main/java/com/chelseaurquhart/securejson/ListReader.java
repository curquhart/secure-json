package com.chelseaurquhart.securejson;

import com.chelseaurquhart.securejson.JSONDecodeException.MalformedListException;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

class ListReader implements IReader<List<Object>> {
    private final JSONReader jsonReader;

    ListReader(final JSONReader parJsonReader) {
        jsonReader = parJsonReader;
    }

    @Override
    public boolean isStart(final ICharacterIterator parIterator) throws IOException {
        return parIterator.peek() == JSONSymbolCollection.Token.L_BRACE.getShortSymbol();
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

        char myNextChar;
        if (parIterator.peek() != JSONSymbolCollection.Token.R_BRACE.getShortSymbol()) {
            boolean myIsListEnd;

            do {
                myList.add(jsonReader.read(parIterator, false));
                jsonReader.moveToNextToken(parIterator);
                if (!parIterator.hasNext()) {
                    throw new MalformedListException(parIterator);
                }
                myNextChar = parIterator.peek();
                myIsListEnd = myNextChar == JSONSymbolCollection.Token.R_BRACE.getShortSymbol();
                if (myNextChar == JSONSymbolCollection.Token.COMMA.getShortSymbol()) {
                    parIterator.next();
                } else if (!myIsListEnd) {
                    throw new MalformedListException(parIterator);
                }
            } while (parIterator.hasNext() && !myIsListEnd);

            if (!myIsListEnd) {
                throw new MalformedListException(parIterator);
            }
        }

        parIterator.next();
        return myList;
    }
}
