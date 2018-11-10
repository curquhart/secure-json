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

import com.chelseaurquhart.securejson.JSONEncodeException.InvalidTypeException;

import java.io.Closeable;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @exclude
 */
class JSONWriter implements Closeable, AutoCloseable {
    private static final int INITIAL_CAPACITY = 512;
    private final transient List<ManagedSecureCharBuffer> secureBuffers;
    private final transient IObjectMutator objectMutator;
    private final transient Settings settings;

    JSONWriter(final Settings parSettings) {
        this(null, parSettings);
    }

    JSONWriter(final IObjectMutator parObjectMutator, final Settings parSettings) {
        secureBuffers = new ArrayList<>();
        objectMutator = parObjectMutator;
        settings = parSettings;
    }

    ManagedSecureCharBuffer write(final Object parInput) throws IOException, InvalidTypeException {
        final ManagedSecureCharBuffer mySecureBuffer = new ManagedSecureCharBuffer(INITIAL_CAPACITY, settings);
        secureBuffers.add(mySecureBuffer);

        write(parInput, mySecureBuffer);
        return mySecureBuffer;
    }

    void write(final Object parInput, final ICharacterWriter parSecureBuffer) throws IOException, InvalidTypeException {
        final Object myInput;
        if (objectMutator != null) {
            myInput = objectMutator.accept(parInput);
        } else {
            myInput = parInput;
        }

        if (myInput instanceof IJSONAware) {
            write(((IJSONAware) myInput).toJSONable(), parSecureBuffer);
            return;
        }

        if (myInput == null) {
            parSecureBuffer.append(JSONSymbolCollection.Token.NULL.getSymbol().toString());
        } else if (myInput instanceof Collection) {
            writeCollection(parSecureBuffer, (Collection) myInput);
        } else if (myInput.getClass().isArray()) {
            writeArray(parSecureBuffer, myInput);
        } else if (myInput instanceof Map) {
            writeMap(parSecureBuffer, (Map) myInput);
        } else if (myInput instanceof Number && myInput instanceof CharSequence) {
            parSecureBuffer.append((CharSequence) myInput);
        } else if (myInput instanceof Number) {
            // nothing we can do here, need to convert to string
            parSecureBuffer.append(myInput.toString());
        } else if (myInput instanceof Boolean && (boolean) myInput) {
            parSecureBuffer.append(JSONSymbolCollection.Token.TRUE.getSymbol().toString());
        } else if (myInput instanceof Boolean && !(boolean) myInput) {
            parSecureBuffer.append(JSONSymbolCollection.Token.FALSE.getSymbol().toString());
        } else if (myInput instanceof CharSequence) {
            parSecureBuffer.append(JSONSymbolCollection.Token.QUOTE.getShortSymbol());
            writeQuoted((CharSequence) myInput, parSecureBuffer);
            parSecureBuffer.append(JSONSymbolCollection.Token.QUOTE.getShortSymbol());
        } else {
            throw new InvalidTypeException();
        }
    }

    private void writeArray(final ICharacterWriter parSecureBuffer, final Object parInput) throws IOException,
            InvalidTypeException {
        parSecureBuffer.append(JSONSymbolCollection.Token.L_BRACE.getShortSymbol());
        final int myLength = Array.getLength(parInput);
        boolean myIsFirst = true;
        for (int myIndex = 0; myIndex < myLength; myIndex++) {
            if (!myIsFirst) {
                parSecureBuffer.append(JSONSymbolCollection.Token.COMMA.getShortSymbol());
            }
            myIsFirst = false;
            write(Array.get(parInput, myIndex), parSecureBuffer);
        }
        parSecureBuffer.append(JSONSymbolCollection.Token.R_BRACE.getShortSymbol());
    }

    private void writeMap(final ICharacterWriter parSecureBuffer, final Map<?, ?> parInput) throws IOException,
            InvalidTypeException {
        parSecureBuffer.append(JSONSymbolCollection.Token.L_CURLY.getShortSymbol());
        boolean myIsFirst = true;
        for (final Map.Entry<?, ?> myEntry : parInput.entrySet()) {
            final Object myKey = myEntry.getKey();
            if (!(myKey instanceof CharSequence)) {
                throw new InvalidTypeException();
            }

            if (!myIsFirst) {
                parSecureBuffer.append(JSONSymbolCollection.Token.COMMA.getShortSymbol());
            }
            myIsFirst = false;
            parSecureBuffer.append(JSONSymbolCollection.Token.QUOTE.getShortSymbol());
            writeQuoted((CharSequence) myKey, parSecureBuffer);
            parSecureBuffer.append(JSONSymbolCollection.Token.QUOTE.getShortSymbol());
            parSecureBuffer.append(JSONSymbolCollection.Token.COLON.getShortSymbol());

            write(myEntry.getValue(), parSecureBuffer);
        }
        parSecureBuffer.append(JSONSymbolCollection.Token.R_CURLY.getShortSymbol());
    }

    private void writeCollection(final ICharacterWriter parSecureBuffer, final Collection<?> parInput)
            throws IOException, InvalidTypeException {
        parSecureBuffer.append(JSONSymbolCollection.Token.L_BRACE.getShortSymbol());
        boolean myIsFirst = true;
        for (final Object myElement : parInput) {
            if (!myIsFirst) {
                parSecureBuffer.append(JSONSymbolCollection.Token.COMMA.getShortSymbol());
            }
            myIsFirst = false;
            write(myElement, parSecureBuffer);
        }
        parSecureBuffer.append(JSONSymbolCollection.Token.R_BRACE.getShortSymbol());
    }

    private void writeQuoted(final CharSequence parInput, final ICharacterWriter parSecureBuffer) throws IOException {
        final int myInputLength = parInput.length();
        for (int myIndex = 0; myIndex < myInputLength; myIndex++) {
            final char myNextChar = parInput.charAt(myIndex);
            final JSONSymbolCollection.Token myToken = JSONSymbolCollection.Token.forSymbolOrDefault(
                myNextChar, JSONSymbolCollection.Token.NULL);

            switch (myToken) {
                case QUOTE:
                case ESCAPE:
                case SLASH:
                case CARRIAGE_RETURN:
                case BACKSPACE:
                case LINE_FEED:
                case FORM_FEED:
                    parSecureBuffer.append(myToken.getValue().toString());
                    break;
                default:
                    if (myNextChar < JSONSymbolCollection.MIN_ALLOWED_ASCII_CODE
                            || myNextChar > JSONSymbolCollection.MAX_ALLOWED_ASCII_CODE) {
                        parSecureBuffer.append(JSONSymbolCollection.Token.ESCAPE.getShortSymbol());
                        parSecureBuffer.append(JSONSymbolCollection.Token.UNICODE.getShortSymbol());
                        parSecureBuffer.append(
                            intToHex((myNextChar >> JSONSymbolCollection.UNICODE_DIGIT_FIRST)
                                & JSONSymbolCollection.HEX_MAX));
                        parSecureBuffer.append(
                            intToHex((myNextChar >> JSONSymbolCollection.UNICODE_DIGIT_SECOND)
                                & JSONSymbolCollection.HEX_MAX));
                        parSecureBuffer.append(
                            intToHex((myNextChar >> JSONSymbolCollection.UNICODE_DIGIT_THIRD)
                                & JSONSymbolCollection.HEX_MAX));
                        parSecureBuffer.append(intToHex(myNextChar & JSONSymbolCollection.HEX_MAX));
                    } else {
                        parSecureBuffer.append(myNextChar);
                    }
                    break;
            }
        }
    }

    private char intToHex(final int parInput) {
        final int myDigit = parInput & JSONSymbolCollection.HEX_MAX;

        if (myDigit >= JSONSymbolCollection.HEX_MIN_ALPHA) {
            return (char) ('a' + myDigit - JSONSymbolCollection.HEX_MIN_ALPHA);
        } else {
            return (char) ('0' + myDigit);
        }
    }

    @Override
    public void close() throws IOException {
        for (final ManagedSecureCharBuffer mySecureBuffer : secureBuffers) {
            mySecureBuffer.close();
        }
    }
}
