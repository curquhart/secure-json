/*
 * Copyright 2019 Chelsea Urquhart
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
 * Base class for JSON encode-specific errors.
 */
public class JSONEncodeException extends JSONException {
    private static final long serialVersionUID = 1L;

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
        private static final long serialVersionUID = 1L;

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
