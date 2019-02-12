/*
 * Copyright 2019 Chelsea Urquhart
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
class JSONWriter implements Closeable, IAutoCloseable {
    private static final int INITIAL_CAPACITY = 512;
    private final transient List<IWritableCharSequence> secureBuffers;
    private final transient IObjectMutator objectMutator;
    private final transient Settings settings;

    JSONWriter(final Settings parSettings) {
        this(null, parSettings);
    }

    JSONWriter(final IObjectMutator parObjectMutator, final Settings parSettings) {
        secureBuffers = new ArrayList<IWritableCharSequence>();
        objectMutator = parObjectMutator;
        settings = parSettings;
    }

    IWritableCharSequence write(final Object parInput) throws IOException, InvalidTypeException {
        final IWritableCharSequence mySecureBuffer = settings.getWritableCharBufferFactory().accept(INITIAL_CAPACITY);
        secureBuffers.add(mySecureBuffer);

        write(parInput, mySecureBuffer);
        return mySecureBuffer;
    }

    void write(final Object parInput, final ICharacterWriter parSecureBuffer) throws IOException, InvalidTypeException {
        if (parInput instanceof IJSONSerializeAware) {
            final Object myJsonable = ((IJSONSerializeAware) parInput).toJSONable();
            if (myJsonable == parInput) {
                throw new JSONException.JSONRuntimeException(
                        new JSONEncodeException(Messages.Key.ERROR_RECURSION_DETECTED));
            }
            write(myJsonable, parSecureBuffer);
            return;
        }

        if (parInput == null) {
            parSecureBuffer.append(JSONSymbolCollection.Token.NULL.getSymbol().toString());
        } else if (parInput instanceof IJSONValue) {
            parSecureBuffer.append(Objects.requireNonNull((IJSONValue) parInput).getValue());
        } else if (parInput instanceof Collection) {
            writeCollection(parSecureBuffer, (Collection) parInput);
        } else if (parInput.getClass().isArray()) {
            writeArray(parSecureBuffer, parInput);
        } else if (parInput instanceof Map) {
            writeMap(parSecureBuffer, (Map) parInput);
        } else if (parInput instanceof Number && parInput instanceof CharSequence) {
            parSecureBuffer.append((CharSequence) parInput);
        } else if (parInput instanceof Number) {
            // nothing we can do here, need to convert to string
            parSecureBuffer.append(parInput.toString());
        } else if (parInput instanceof Boolean && (Boolean) parInput) {
            parSecureBuffer.append(JSONSymbolCollection.Token.TRUE.getSymbol().toString());
        } else if (parInput instanceof Boolean) {
            parSecureBuffer.append(JSONSymbolCollection.Token.FALSE.getSymbol().toString());
        } else if (parInput instanceof CharSequence) {
            parSecureBuffer.append(JSONSymbolCollection.Token.QUOTE.getShortSymbol());
            writeQuoted((CharSequence) parInput, parSecureBuffer);
            parSecureBuffer.append(JSONSymbolCollection.Token.QUOTE.getShortSymbol());
        } else {
            final Object myInput = mutateInput(parInput);
            if (myInput == parInput) {
                throw new InvalidTypeException();
            } else {
                write(myInput, parSecureBuffer);
            }
        }
    }

    private Object mutateInput(final Object parInput) {
        final Object myOutput;
        if (objectMutator != null) {
            myOutput = objectMutator.accept(parInput);
        } else {
            myOutput = parInput;
        }

        return myOutput;
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
        for (final IWritableCharSequence mySecureBuffer : secureBuffers) {
            mySecureBuffer.close();
        }
    }
}
