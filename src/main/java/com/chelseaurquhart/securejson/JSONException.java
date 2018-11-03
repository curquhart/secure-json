/*
 * Copyright 2018 Chelsea Urquhart
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
