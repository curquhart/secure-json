package com.chelseaurquhart.securejson;

import java.io.IOException;

class JSONEncodeException extends JSONException {
    JSONEncodeException(final Messages.Key parMessageKey)
            throws IOException {
        super(Messages.get(Messages.Key.ERROR_JSON_ENCODE)
            .replace(":message", Messages.get(parMessageKey)));
    }

    JSONEncodeException(final Throwable parInput) {
        super(Util.unwrapException(parInput));
    }

    static class JSONEncodeRuntimeException extends RuntimeException {
        private JSONEncodeException cause;

        JSONEncodeRuntimeException(final JSONEncodeException parInput) {
            super(parInput);
            cause = parInput;
        }

        JSONEncodeRuntimeException(final Exception parInput) {
            super(Util.unwrapException(parInput).getMessage());
        }

        @Override
        public JSONEncodeException getCause() {
            if (cause == null) {
                cause = new JSONEncodeException(super.getCause());
            }

            return cause;
        }
    }

    static class InvalidTypeException extends JSONEncodeException {
        InvalidTypeException() throws IOException {
            super(Messages.Key.ERROR_INVALID_TYPE);
        }
    }
}
