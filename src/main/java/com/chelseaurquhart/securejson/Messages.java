package com.chelseaurquhart.securejson;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * @exclude
 */
final class Messages {
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
        ERROR_BUFFER_OVERFLOW
    }

    static String get(final Key parKey) throws IOException {
        return getInstance().properties.getProperty(parKey.toString());
    }

    private static final String RESOURCE_NAME = "com/chelseaurquhart/securejson/messages.properties";

    private static Messages INSTANCE;

    private final Properties properties;

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
        try (InputStream resourceStream = myLoader.getResourceAsStream(RESOURCE_NAME)) {
            properties.load(resourceStream);
        }
    }
}
