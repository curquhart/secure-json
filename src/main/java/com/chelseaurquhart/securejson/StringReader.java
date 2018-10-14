package com.chelseaurquhart.securejson;

import com.chelseaurquhart.securejson.JSONDecodeException.MalformedStringException;
import com.chelseaurquhart.securejson.JSONDecodeException.MalformedUnicodeValueException;

import java.io.IOException;

class StringReader implements IReader<CharSequence> {
    private static final short UNICODE_DIGITS = 4;
    private static final int TWO_DIGIT_MIN = 10;

    @Override
    public boolean isStart(final ICharacterIterator parIterator) {
        return parIterator.peek() == JSONSymbolCollection.Token.QUOTE.getShortSymbol();
    }

    @Override
    public CharSequence read(final ICharacterIterator parInput)
            throws IOException {
        if (parInput.next() != JSONSymbolCollection.Token.QUOTE.getShortSymbol()) {
            throw new MalformedStringException(parInput);
        }

        final ManagedSecureCharBuffer mySecureBuffer;
        if (parInput instanceof ISizeable) {
            // hack to prevent extra allocation when we're not working on a stream.
            mySecureBuffer = new ManagedSecureCharBuffer(((ISizeable) parInput).getSize());
        } else {
            mySecureBuffer = new ManagedSecureCharBuffer();
        }

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
                    } else if (myChar == 'n') {
                        myChar = '\n';
                    } else if (myChar == 'f') {
                        myChar = '\f';
                    }
                    mySecureBuffer.append(myChar);
                }
            } else {
                if (myChar == JSONSymbolCollection.Token.QUOTE.getShortSymbol()) {
                    return mySecureBuffer;
                }
                mySecureBuffer.append(myChar);
            }
        }

        // did not find leading quote
        throw new MalformedStringException(parInput);
    }

    private char readUnicode(final ICharacterIterator parInput) throws IOException {
        int myValue = 0;
        for (int myIndex = 0; myIndex < UNICODE_DIGITS; myIndex++) {
            char myChar = Character.toLowerCase(parInput.next());
            if (Character.isDigit(myChar)) {
                myValue = (myValue << UNICODE_DIGITS) + myChar - '0';
            } else if (myChar >= 'a' && myChar <= 'f') {
                myValue = (myValue << UNICODE_DIGITS) + TWO_DIGIT_MIN + myChar - 'a';
            } else {
                throw new MalformedUnicodeValueException(parInput);
            }
        }

        return (char) myValue;
    }
}
