package com.chelseaurquhart.securejson;

import com.chelseaurquhart.securejson.JSONDecodeException.InvalidTokenException;

import java.io.IOException;

/**
 * @exclude
 */
class WordReader implements IReader<Object> {
    @Override
    public boolean isStart(final ICharacterIterator parIterator) throws IOException {
        return JSONSymbolCollection.WORD_TOKENS.get(parIterator.peek()) != null;
    }

    @Override
    public SymbolType getSymbolType(final ICharacterIterator parIterator) {
        return SymbolType.UNKNOWN;
    }

    @Override
    public Object read(final ICharacterIterator parIterator)
            throws IOException {

        final JSONSymbolCollection.Token myWordToken = JSONSymbolCollection.WORD_TOKENS.get(parIterator.peek());
        if (myWordToken != null) {
            readAndValidateWord(parIterator, myWordToken);
            return myWordToken.getValue();
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

    private void readAndValidateWord(final ICharacterIterator parIterator, final JSONSymbolCollection.Token parToken)
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
            return;
        }

        final char myChar = parIterator.peek();
        if (JSONSymbolCollection.TOKENS.containsKey(myChar) || JSONSymbolCollection.WHITESPACES.containsKey(myChar)) {
            return;
        }

        throw new InvalidTokenException(parIterator);
    }

    @Override
    public boolean isContainerType() {
        return false;
    }

    @Override
    public void close() {
    }
}
