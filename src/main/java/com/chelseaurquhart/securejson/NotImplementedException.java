package com.chelseaurquhart.securejson;

import java.io.IOException;
import com.chelseaurquhart.securejson.JSONException.JSONRuntimeException;

/**
 * Base class for all SecureJSON exceptions.
 */
public class NotImplementedException extends UnsupportedOperationException {
    /**
     * @exclude
     */
    NotImplementedException(final Messages.Key parMessageKey, final String parMethodName) {
        super(buildMessage(parMessageKey).replace(":method", parMethodName));
    }

    private static String buildMessage(final Messages.Key parMessageKey) {
        try {
            return Messages.get(parMessageKey);
        } catch (final IOException myException) {
            throw new JSONRuntimeException(myException);
        }
    }
}
