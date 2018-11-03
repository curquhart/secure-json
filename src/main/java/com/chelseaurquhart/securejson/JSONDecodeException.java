package com.chelseaurquhart.securejson;

import java.io.IOException;

/**
 * Base class for JSON decode-specific errors.
 */
public class JSONDecodeException extends JSONException {
    /**
     * @exclude
     */
    JSONDecodeException(final ICharacterIterator parCharSequence, final Messages.Key parMessageKey)
            throws IOException {
        super(Messages.get(Messages.Key.ERROR_JSON_DECODE)
            .replace(":offset", "" + parCharSequence.getOffset())
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
