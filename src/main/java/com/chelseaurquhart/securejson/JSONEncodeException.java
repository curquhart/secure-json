package com.chelseaurquhart.securejson;

import java.io.IOException;

/**
 * Base class for JSON encode-specific errors.
 */
public class JSONEncodeException extends JSONException {
    /**
     * @exclude
     */
    JSONEncodeException(final Messages.Key parMessageKey)
            throws IOException {
        super(Messages.get(Messages.Key.ERROR_JSON_ENCODE)
            .replace(":message", Messages.get(parMessageKey)));
    }

    /**
     * @exclude
     */
    JSONEncodeException(final Throwable parInput) {
        super(Util.unwrapException(parInput));
    }

    /**
     * Exception representing an invalid data type.
     */
    public static class InvalidTypeException extends JSONEncodeException {
        /**
         * @exclude
         */
        InvalidTypeException() throws IOException {
            super(Messages.Key.ERROR_INVALID_TYPE);
        }
    }

    static JSONEncodeException fromException(final Exception parException) {
        final Throwable myException = Util.unwrapException(parException);

        if (myException instanceof JSONEncodeException) {
            return (JSONEncodeException) myException;
        } else {
            return new JSONEncodeException(myException);
        }
    }

}
