package com.chelseaurquhart.securejson;

import java.io.IOException;

/**
 * Base class for all SecureJSON exceptions.
 */
public class JSONException extends IOException {
    JSONException(final String parMessage) {
        super(parMessage);
    }

    JSONException(final Throwable parException) {
        super(parException);
    }

    static class JSONRuntimeException extends RuntimeException {
        private JSONException cause;

        JSONRuntimeException(final JSONException parInput) {
            super(parInput);
            cause = parInput;
        }

        JSONRuntimeException(final Exception parInput) {
            super(Util.unwrapException(parInput).getMessage());
        }

        @Override
        public JSONException getCause() {
            if (cause == null) {
                cause = new JSONException(super.getCause());
            }

            return cause;
        }
    }
}
