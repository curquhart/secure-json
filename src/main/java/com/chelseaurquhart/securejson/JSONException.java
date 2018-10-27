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
}
