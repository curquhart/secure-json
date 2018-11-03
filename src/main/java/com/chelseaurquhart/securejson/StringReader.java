package com.chelseaurquhart.securejson;

import com.chelseaurquhart.securejson.JSONDecodeException.MalformedStringException;
import com.chelseaurquhart.securejson.JSONDecodeException.MalformedUnicodeValueException;

import java.io.IOException;

/**
 * @exclude
 */
class StringReader extends ManagedSecureBufferList implements IReader<CharSequence> {
    private static final int TWO_DIGIT_MIN = 10;

    private final Settings settings;

    StringReader(final Settings parSettings) {
        settings = parSettings;
    }

    @Override
    public boolean isStart(final ICharacterIterator parIterator) throws IOException {
        return parIterator.peek() == JSONSymbolCollection.Token.QUOTE.getShortSymbol();
    }

    @Override
    public SymbolType getSymbolType(final ICharacterIterator parIterator) {
        return SymbolType.UNKNOWN;
    }

    @Override
    public CharSequence read(final ICharacterIterator parInput)
            throws IOException {
        if (parInput.peek() != JSONSymbolCollection.Token.QUOTE.getShortSymbol()) {
            throw new MalformedStringException(parInput);
        }
        parInput.next();

        final ManagedSecureCharBuffer mySecureBuffer = new ManagedSecureCharBuffer(settings);
        addSecureBuffer(mySecureBuffer);

        while (parInput.hasNext()) {
            char myChar = parInput.next();
            if (myChar == '\\') {
                myChar = parInput.next();

                if (myChar == 'u') {
                    mySecureBuffer.append(readUnicode(parInput));
                } else {
                    if (myChar == 't') {
                        myChar = '\t';
                    } else if (myChar == 'r') {
                        myChar = '\r';
                    } else if (myChar == 'b') {
                        myChar = '\b';
                    } else if (myChar == 'n') {
                        myChar = '\n';
                    } else if (myChar == 'f') {
                        myChar = '\f';
                    } else if (myChar != '\\' && myChar != '"' && myChar != '/') {
                        throw new MalformedUnicodeValueException(parInput);
                    }
                    mySecureBuffer.append(myChar);
                }
            } else if (myChar < JSONSymbolCollection.MIN_ALLOWED_ASCII_CODE) {
                throw new MalformedUnicodeValueException(parInput);
            } else {
                if (myChar == JSONSymbolCollection.Token.QUOTE.getShortSymbol()) {
                    return mySecureBuffer;
                }
                mySecureBuffer.append(myChar);
            }
        }

        // did not find trailing quote
        throw new MalformedStringException(parInput);
    }

    @Override
    public void addValue(final ICharacterIterator parIterator, final Object parCollection, final Object parItem) {
    }

    @Override
    public Object normalizeCollection(final Object parValue) {
        return parValue;
    }

    @Override
    public boolean isContainerType() {
        return false;
    }

    private char readUnicode(final ICharacterIterator parInput) throws IOException {
        int myValue = 0;
        for (int myIndex = 0; myIndex < JSONSymbolCollection.UNICODE_DIGITS; myIndex++) {
            char myChar = Character.toLowerCase(parInput.next());
            if (Character.isDigit(myChar)) {
                myValue = (myValue << JSONSymbolCollection.UNICODE_DIGITS) + myChar - '0';
            } else if (myChar >= 'a' && myChar <= 'f') {
                myValue = (myValue << JSONSymbolCollection.UNICODE_DIGITS) + TWO_DIGIT_MIN + myChar - 'a';
            } else {
                throw new MalformedUnicodeValueException(parInput);
            }
        }

        return (char) myValue;
    }
}
