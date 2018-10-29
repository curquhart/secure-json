package com.chelseaurquhart.securejson;

import java.io.Closeable;
import java.io.IOException;

interface ICharacterWriter extends Closeable {
    void append(final char parChar) throws IOException;

    void append(final CharSequence parChars) throws IOException;
}
