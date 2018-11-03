package com.chelseaurquhart.securejson;

import java.io.IOException;

/**
 * Base class for all SecureJSON exceptions.
 */
public class JSONException extends IOException {
    /**
     * @exclude
     */
    JSONException(final String parMessage) {
        super(parMessage);
    }

    /**
     * @exclude
     */
    JSONException(final Throwable parException) {
        super(parException.getMessage(), parException);
    }

    /**
     * @exclude
     */
    static class JSONRuntimeException extends RuntimeException {
        JSONRuntimeException(final Exception parInput) {
            super(parInput);
        }

        JSONRuntimeException(final String parMessage) {
            super(parMessage);
        }
    }
}
