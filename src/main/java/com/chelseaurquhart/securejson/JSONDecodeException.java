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

import java.io.IOException;

/**
 * Base class for JSON decode-specific errors.
 */
public class JSONDecodeException extends JSONException {
    private static final long serialVersionUID = 1L;

    /**
     * @exclude
     */
    JSONDecodeException(final ICharacterIterator parCharSequence, final Messages.Key parMessageKey)
            throws IOException {
        super(Messages.get(Messages.Key.ERROR_JSON_DECODE)
            .replace(":offset", String.valueOf(parCharSequence.getOffset()))
            .replace(":message", Messages.get(parMessageKey)));
    }

    /**
     * @exclude
     */
    JSONDecodeException(final Throwable parInput) {
        super(parInput);
    }

    /**
     * Exception indicating that the input JSON could not be parsed.
     */
    public static class MalformedJSONException extends JSONDecodeException {
        private static final long serialVersionUID = 1L;

        /**
         * @exclude
         */
        MalformedJSONException(final ICharacterIterator parCharSequence) throws IOException {
            super(parCharSequence, Messages.Key.ERROR_MALFORMED_JSON);
        }
    }

    /**
     * Exception indicating that the input JSON didn't contain any tokens we could understand.
     */
    public static class EmptyJSONException extends JSONDecodeException {
        private static final long serialVersionUID = 1L;

        /**
         * @exclude
         */
        EmptyJSONException(final ICharacterIterator parCharSequence) throws IOException {
            super(parCharSequence, Messages.Key.ERROR_EMPTY_JSON);
        }
    }

    /**
     * Exception indicating we could not parse a JSON string.
     */
    public static class MalformedStringException extends JSONDecodeException {
        private static final long serialVersionUID = 1L;

        /**
         * @exclude
         */
        MalformedStringException(final ICharacterIterator parCharSequence) throws IOException {
            super(parCharSequence, Messages.Key.ERROR_MALFORMED_STRING);
        }
    }

    /**
     * Exception indicating that a character in the input sequence is invalid.
     */
    public static class MalformedUnicodeValueException extends JSONDecodeException {
        private static final long serialVersionUID = 1L;

        /**
         * @exclude
         */
        MalformedUnicodeValueException(final ICharacterIterator parCharSequence) throws IOException {
            super(parCharSequence, Messages.Key.ERROR_MALFORMED_UNICODE_VALUE);
        }
    }

    /**
     * Exception indicating that the next token was either unexpected or unknown.
     */
    public static class InvalidTokenException extends JSONDecodeException {
        private static final long serialVersionUID = 1L;

        /**
         * @exclude
         */
        InvalidTokenException(final ICharacterIterator parCharSequence) throws IOException {
            super(parCharSequence, Messages.Key.ERROR_INVALID_TOKEN);
        }
    }

    /**
     * Exception indicating a malformed list.
     */
    public static class MalformedListException extends JSONDecodeException {
        private static final long serialVersionUID = 1L;

        /**
         * @exclude
         */
        MalformedListException(final ICharacterIterator parCharSequence) throws IOException {
            super(parCharSequence, Messages.Key.ERROR_MALFORMED_LIST);
        }
    }

    /**
     * Exception indicating a malformed map.
     */
    public static class MalformedMapException extends JSONDecodeException {
        private static final long serialVersionUID = 1L;

        /**
         * @exclude
         */
        MalformedMapException(final ICharacterIterator parCharSequence) throws IOException {
            super(parCharSequence, Messages.Key.ERROR_MALFORMED_MAP);
        }
    }

    /**
     * Exception indicating a malformed number.
     */
    public static class MalformedNumberException extends JSONDecodeException {
        private static final long serialVersionUID = 1L;

        /**
         * @exclude
         */
        MalformedNumberException(final ICharacterIterator parCharSequence) throws IOException {
            super(parCharSequence, Messages.Key.ERROR_MALFORMED_NUMBER);
        }
    }

    /**
     * Exception indicating that there were extra characters found after the closing token.
     */
    public static class ExtraCharactersException extends JSONDecodeException {
        private static final long serialVersionUID = 1L;

        /**
         * @exclude
         */
        ExtraCharactersException(final ICharacterIterator parCharSequence) throws IOException {
            super(parCharSequence, Messages.Key.ERROR_EXTRA_CHARACTERS);
        }
    }

    static JSONDecodeException fromException(final Exception parException) {
        final Throwable myException = Util.unwrapException(parException);

        if (myException instanceof JSONDecodeException) {
            return (JSONDecodeException) myException;
        } else {
            return new JSONDecodeException(myException);
        }
    }
}
