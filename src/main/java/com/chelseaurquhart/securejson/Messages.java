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
import java.io.InputStream;
import java.util.Properties;

/**
 * @exclude
 */
final class Messages {
    private static final String RESOURCE_NAME = "com/chelseaurquhart/securejson/messages.properties";

    private static Messages INSTANCE;

    private final transient Properties properties;

    /**
     * @exclude
     */
    enum Key {
        ERROR_JSON_DECODE,
        ERROR_JSON_ENCODE,
        ERROR_MALFORMED_STRING,
        ERROR_EMPTY_JSON,
        ERROR_MALFORMED_JSON,
        ERROR_MALFORMED_UNICODE_VALUE,
        ERROR_INVALID_TOKEN,
        ERROR_MALFORMED_LIST,
        ERROR_MALFORMED_MAP,
        ERROR_MALFORMED_NUMBER,
        ERROR_EXTRA_CHARACTERS,
        ERROR_ITERATOR_REMOVE_NOT_ALLOWED,
        ERROR_INVALID_TYPE,
        ERROR_BAD_SEQUENCE_ARGS,
        ERROR_BUFFER_OVERFLOW,
        ERROR_INVALID_ENCODING,
        ERROR_WRITE_TO_READONLY_BUFFER,
        ERROR_READ_OBJECT_FROM_NON_MAP_TYPE,
        ERROR_RESOLVE_IMPLEMENTATION,
        ERROR_INVALID_MAP_KEY_TYPE,
        ERROR_INVALID_MAP_KEY_TYPE_STRICT,
        ERROR_NOT_IMPLEMENTED,
        ERROR_ATTEMPT_TO_ADD_MAP_ENTRY_TO_NON_MAP,
        ERROR_INVALID_SERIALIZATION_CONFIG,
        ERROR_INVALID_SYMBOL
    }

    static String get(final Key parKey) throws IOException {
        return getInstance().properties.getProperty(parKey.toString());
    }

    private static Messages getInstance() throws IOException {
        if (INSTANCE == null) {
            loadOrReload();
        }

        return INSTANCE;
    }

    private static void loadOrReload() throws IOException {
        synchronized (Messages.class) {
            // another thread could have started initialization.
            if (INSTANCE == null) {
                INSTANCE = new Messages();
            }
        }
    }

    private Messages() throws IOException {
        properties = new Properties();

        final ClassLoader myLoader = Thread.currentThread().getContextClassLoader();
        try (InputStream myResourceStream = myLoader.getResourceAsStream(RESOURCE_NAME)) {
            properties.load(myResourceStream);
        }
    }
}
