package com.chelseaurquhart.securejson;

import com.chelseaurquhart.securejson.JSONEncodeException.InvalidTypeException;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

class JSONWriter implements Closeable, AutoCloseable {
    private static final int INITIAL_CAPACITY = 512;
    private final List<ManagedSecureCharBuffer> secureBuffers;

    JSONWriter() {
        secureBuffers = new ArrayList<>();
    }

    CharSequence write(final Object parInput) throws IOException {
        final ManagedSecureCharBuffer mySecureBuffer = new ManagedSecureCharBuffer(INITIAL_CAPACITY);
        secureBuffers.add(mySecureBuffer);

        write(parInput, mySecureBuffer);
        return mySecureBuffer;
    }

    void write(final Object parInput, final ICharacterWriter parSecureBuffer) throws IOException {
        if (parInput instanceof Collection) {
            parSecureBuffer.append(JSONSymbolCollection.Token.L_BRACE.getShortSymbol());
            for (final Object myElement : ((Collection) parInput)) {
                write(myElement, parSecureBuffer);
            }
            parSecureBuffer.append(JSONSymbolCollection.Token.R_BRACE.getShortSymbol());
        } else if (parInput instanceof Map) {
            parSecureBuffer.append(JSONSymbolCollection.Token.L_CURLY.getShortSymbol());
            boolean myIsFirst = true;
            for (final Object myObject : ((Map) parInput).entrySet()) {
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
        } else if (parInput instanceof Number && parInput instanceof CharSequence) {
                parSecureBuffer.append((CharSequence) parInput);
        } else if (parInput instanceof Number) {
            // nothing we can do here, need to convert to string
            parSecureBuffer.append(parInput.toString());
        } else if (parInput == null) {
            parSecureBuffer.append(JSONSymbolCollection.Token.NULL.getSymbol().toString());
        } else if (parInput instanceof Boolean && (boolean) parInput) {
            parSecureBuffer.append(JSONSymbolCollection.Token.TRUE.getSymbol().toString());
        } else if (parInput instanceof Boolean && !(boolean) parInput) {
            parSecureBuffer.append(JSONSymbolCollection.Token.FALSE.getSymbol().toString());
        } else if (parInput instanceof CharSequence) {
            parSecureBuffer.append(JSONSymbolCollection.Token.QUOTE.getShortSymbol());
            writeQuoted((CharSequence) parInput, parSecureBuffer);
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
