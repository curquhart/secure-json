package com.chelseaurquhart.securejson;

import java.io.IOException;

abstract class JSONDecodeException extends RuntimeException {
    JSONDecodeException(final IterableCharSequence parCharSequence, final Messages.Key parMessageKey)
            throws IOException {
        super(Messages.get(Messages.Key.ERROR_JSON_DECODE)
            .replace(":offset", "" + parCharSequence.getOffset())
            .replace(":message", Messages.get(parMessageKey)));
    }

    static class MalformedStringException extends JSONDecodeException {
        MalformedStringException(final IterableCharSequence parCharSequence) throws IOException {
            super(parCharSequence, Messages.Key.ERROR_MALFORMED_STRING);
        }
    }

    static class MalformedUnicodeValueException extends JSONDecodeException {
        MalformedUnicodeValueException(final IterableCharSequence parCharSequence) throws IOException {
            super(parCharSequence, Messages.Key.ERROR_MALFORMED_UNICODE_VALUE);
        }
    }

    static class InvalidTokenException extends JSONDecodeException {
        InvalidTokenException(final IterableCharSequence parCharSequence) throws IOException {
            super(parCharSequence, Messages.Key.ERROR_INVALID_TOKEN);
        }
    }

    static class MalformedListException extends JSONDecodeException {
        MalformedListException(final IterableCharSequence parCharSequence) throws IOException {
            super(parCharSequence, Messages.Key.ERROR_MALFORMED_LIST);
        }
    }

    static class MalformedNumberException extends JSONDecodeException {
        MalformedNumberException(final IterableCharSequence parCharSequence) throws IOException {
            super(parCharSequence, Messages.Key.ERROR_MALFORMED_NUMBER);
        }
    }

    static class ExtraCharactersException extends JSONDecodeException {
        ExtraCharactersException(final IterableCharSequence parCharSequence) throws IOException {
            super(parCharSequence, Messages.Key.ERROR_EXTRA_CHARACTERS);
        }
    }
}
