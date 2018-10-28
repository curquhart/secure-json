package com.chelseaurquhart.securejson;

import com.chelseaurquhart.securejson.JSONEncodeException.InvalidTypeException;

import java.io.Closeable;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

class JSONWriter implements Closeable, AutoCloseable {
    private static final int INITIAL_CAPACITY = 512;
    private final List<ManagedSecureCharBuffer> secureBuffers;
    private final IObjectMutator objectMutator;

    JSONWriter() {
        this(null);
    }

    JSONWriter(final IObjectMutator parObjectMutator) {
        secureBuffers = new ArrayList<>();
        objectMutator = parObjectMutator;
    }

    ManagedSecureCharBuffer write(final Object parInput) throws IOException {
        final ManagedSecureCharBuffer mySecureBuffer = new ManagedSecureCharBuffer(INITIAL_CAPACITY);
        secureBuffers.add(mySecureBuffer);

        write(parInput, mySecureBuffer);
        return mySecureBuffer;
    }

    void write(final Object parInput, final ICharacterWriter parSecureBuffer) throws IOException {
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
            parSecureBuffer.append(JSONSymbolCollection.Token.L_BRACE.getShortSymbol());
            for (final Object myElement : ((Collection) myInput)) {
                write(myElement, parSecureBuffer);
            }
            parSecureBuffer.append(JSONSymbolCollection.Token.R_BRACE.getShortSymbol());
        } else if (myInput.getClass().isArray()) {
            parSecureBuffer.append(JSONSymbolCollection.Token.L_BRACE.getShortSymbol());
            final int myLength = Array.getLength(myInput);
            for (int myIndex = 0; myIndex < myLength; myIndex++) {
                write(Array.get(myInput, myIndex), parSecureBuffer);
            }
            parSecureBuffer.append(JSONSymbolCollection.Token.R_BRACE.getShortSymbol());
        } else if (myInput instanceof Map) {
            parSecureBuffer.append(JSONSymbolCollection.Token.L_CURLY.getShortSymbol());
            boolean myIsFirst = true;
            for (final Object myObject : ((Map) myInput).entrySet()) {
                final Map.Entry myEntry = (Map.Entry) myObject;
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

    private void writeQuoted(final CharSequence parInput, final ICharacterWriter parSecureBuffer) {
        final int myInputLength = parInput.length();
        for (int myIndex = 0; myIndex < myInputLength; myIndex++) {
            final char myNextChar = parInput.charAt(myIndex);
            JSONSymbolCollection.Token myToken = JSONSymbolCollection.Token.NULL;
            try {
                myToken = JSONSymbolCollection.Token.forSymbol(myNextChar);
            } catch (IllegalArgumentException myException) {
            }

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
                    if (((myNextChar < JSONSymbolCollection.MIN_ALLOWED_ASCII_CODE)
                            || (myNextChar > JSONSymbolCollection.MAX_ALLOWED_ASCII_CODE))) {
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
        for (final ICharacterWriter mySecureBuffer : secureBuffers) {
            mySecureBuffer.close();
        }
    }
}
