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
import com.chelseaurquhart.securejson.JSONException.JSONRuntimeException;

/**
 * Exception indicating something is not implemented.
 */
public class NotImplementedException extends UnsupportedOperationException {
    private static final long serialVersionUID = 1L;

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
