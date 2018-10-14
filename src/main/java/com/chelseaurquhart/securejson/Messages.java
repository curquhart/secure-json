package com.chelseaurquhart.securejson;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

final class Messages {
    enum Key {
        ERROR_JSON_DECODE,
        ERROR_MALFORMED_STRING,
        ERROR_MALFORMED_UNICODE_VALUE,
        ERROR_INVALID_TOKEN,
        ERROR_MALFORMED_LIST,
        ERROR_MALFORMED_NUMBER,
        ERROR_EXTRA_CHARACTERS,
        ERROR_ITERATOR_REMOVE_NOT_ALLOWED
    }

    static String get(final Key parKey) throws IOException {
        return getInstance().properties.getProperty(parKey.toString());
    }

    private static final String RESOURCE_NAME = "messages.properties";

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
