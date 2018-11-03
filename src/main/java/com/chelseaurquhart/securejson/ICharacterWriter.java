package com.chelseaurquhart.securejson;

import java.io.IOException;

/**
 * @exclude
 */
interface ICharacterWriter {
    void append(final char parChar) throws IOException;

    void append(final CharSequence parChars) throws IOException;
}
