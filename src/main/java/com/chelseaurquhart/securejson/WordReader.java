package com.chelseaurquhart.securejson;

import com.chelseaurquhart.securejson.JSONDecodeException.InvalidTokenException;

import java.io.IOException;

/**
 * @exclude
 */
class WordReader implements IReader {
    @Override
    public boolean isStart(final ICharacterIterator parIterator) throws IOException {
        final char myChar = parIterator.peek();

        return myChar == JSONSymbolCollection.Token.NULL.getShortSymbol()
            || myChar == JSONSymbolCollection.Token.FALSE.getShortSymbol()
            || myChar == JSONSymbolCollection.Token.TRUE.getShortSymbol();
    }

    @Override
    public SymbolType getSymbolType(final ICharacterIterator parIterator) {
        return SymbolType.UNKNOWN;
    }

    @Override
    public Object read(final ICharacterIterator parIterator)
            throws IOException {

        final char myChar = parIterator.peek();

        if (myChar == JSONSymbolCollection.Token.NULL.getShortSymbol()) {
            return readWord(parIterator, JSONSymbolCollection.Token.NULL);
        }
        if (myChar == JSONSymbolCollection.Token.FALSE.getShortSymbol()) {
            return readWord(parIterator, JSONSymbolCollection.Token.FALSE);
        }
        if (myChar == JSONSymbolCollection.Token.TRUE.getShortSymbol()) {
            return readWord(parIterator, JSONSymbolCollection.Token.TRUE);
        }

        throw new InvalidTokenException(parIterator);
    }

    @Override
    public void addValue(final ICharacterIterator parIterator, final Object parCollection, final Object parItem) {
    }

    @Override
    public Object normalizeCollection(final Object parValue) {
        return parValue;
    }

    private Object readWord(final ICharacterIterator parIterator, final JSONSymbolCollection.Token parToken)
            throws IOException {
        final CharSequence myWord = parToken.toString().toLowerCase();
        final int myCheckingLength = myWord.length();

        for (int myIndex = 0; myIndex < myCheckingLength; myIndex++) {
            if (!parIterator.hasNext()) {
                throw new InvalidTokenException(parIterator);
            }
            if (myWord.charAt(myIndex) != parIterator.next()) {
                throw new InvalidTokenException(parIterator);
            }
        }

        if (!parIterator.hasNext()) {
            return parToken.getValue();
        }

        final char myChar = parIterator.peek();
        if (JSONSymbolCollection.TOKENS.containsKey(myChar) || JSONSymbolCollection.WHITESPACES.containsKey(myChar)) {
            return parToken.getValue();
        }

        throw new InvalidTokenException(parIterator);
    }
}
