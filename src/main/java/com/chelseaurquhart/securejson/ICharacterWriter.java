package com.chelseaurquhart.securejson;

import java.io.Closeable;

interface ICharacterWriter extends Closeable {
    void append(final char parChar);

    void append(final CharSequence parChars);
}
