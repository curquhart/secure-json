package com.chelseaurquhart.securejson;

import java.io.IOException;

abstract class JSONDecodeException extends RuntimeException {
    JSONDecodeException(final ICharacterIterator parCharSequence, final Messages.Key parMessageKey)
            throws IOException {
        super(Messages.get(Messages.Key.ERROR_JSON_DECODE)
            .replace(":offset", "" + parCharSequence.getOffset())
            .replace(":message", Messages.get(parMessageKey)));
    }

    static class MalformedStringException extends JSONDecodeException {
        MalformedStringException(final ICharacterIterator parCharSequence) throws IOException {
            super(parCharSequence, Messages.Key.ERROR_MALFORMED_STRING);
        }
    }

    static class MalformedUnicodeValueException extends JSONDecodeException {
        MalformedUnicodeValueException(final ICharacterIterator parCharSequence) throws IOException {
            super(parCharSequence, Messages.Key.ERROR_MALFORMED_UNICODE_VALUE);
        }
    }

    static class InvalidTokenException extends JSONDecodeException {
        InvalidTokenException(final ICharacterIterator parCharSequence) throws IOException {
            super(parCharSequence, Messages.Key.ERROR_INVALID_TOKEN);
        }
    }

    static class MalformedListException extends JSONDecodeException {
        MalformedListException(final ICharacterIterator parCharSequence) throws IOException {
            super(parCharSequence, Messages.Key.ERROR_MALFORMED_LIST);
        }
    }

    static class MalformedMapException extends JSONDecodeException {
        MalformedMapException(final ICharacterIterator parCharSequence) throws IOException {
            super(parCharSequence, Messages.Key.ERROR_MALFORMED_MAP);
        }
    }

    static class MalformedNumberException extends JSONDecodeException {
        MalformedNumberException(final ICharacterIterator parCharSequence) throws IOException {
            super(parCharSequence, Messages.Key.ERROR_MALFORMED_NUMBER);
        }
    }

    static class ExtraCharactersException extends JSONDecodeException {
        ExtraCharactersException(final ICharacterIterator parCharSequence) throws IOException {
            super(parCharSequence, Messages.Key.ERROR_EXTRA_CHARACTERS);
        }
    }
}
