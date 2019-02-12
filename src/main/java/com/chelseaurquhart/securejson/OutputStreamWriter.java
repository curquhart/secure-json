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

import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;

/**
 * @exclude
 */
class OutputStreamWriter implements ICharacterWriter {
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
}
