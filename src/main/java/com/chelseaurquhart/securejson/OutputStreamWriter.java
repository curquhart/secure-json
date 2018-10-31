package com.chelseaurquhart.securejson;

import java.io.Closeable;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;

/**
 * @exclude
 */
class OutputStreamWriter implements ICharacterWriter, Closeable, AutoCloseable {
    private final PrintStream outputStream;

    OutputStreamWriter(final OutputStream parOutputStream, final Charset parCharset)
            throws UnsupportedEncodingException {
        outputStream = new PrintStream(parOutputStream, false, parCharset.name());
    }

    @Override
    public void append(final char parChar) {
        outputStream.append(parChar);
    }

    @Override
    public void append(final CharSequence parChars) {
        final int myCharsLength = parChars.length();
        // PrintStream.append(CharSequence) converts to a string so we need to go character-by-character ourselves.
        for (int myIndex = 0; myIndex < myCharsLength; myIndex++) {
            outputStream.append(parChars.charAt(myIndex));
        }
    }

    @Override
    public void close() {
        outputStream.close();
    }
}
