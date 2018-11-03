/*
 * Copyright 2018 Chelsea Urquhart
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
        return JSONSymbolCollection.Token.QUOTE.getShortSymbol().equals(parIterator.peek());
    }

    @Override
    public SymbolType getSymbolType(final ICharacterIterator parIterator) {
        return SymbolType.UNKNOWN;
    }

    @Override
    public CharSequence read(final ICharacterIterator parInput)
            throws IOException {
        if (!JSONSymbolCollection.Token.QUOTE.getShortSymbol().equals(parInput.peek())) {
            throw new MalformedStringException(parInput);
        }
        parInput.next();

        final ManagedSecureCharBuffer mySecureBuffer = new ManagedSecureCharBuffer(settings);
        addSecureBuffer(mySecureBuffer);

        while (parInput.hasNext()) {
            char myChar = parInput.next();
            if (myChar == '\\') {
                if (!parInput.hasNext()) {
                    throw new MalformedStringException(parInput);
                }
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
            final char myChar = Character.toLowerCase(parInput.next());
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
