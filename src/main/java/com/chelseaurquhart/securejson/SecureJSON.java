package com.chelseaurquhart.securejson;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * SecureJSON is a JSON serializer and deserializer with strict security in mind. It does not create strings due to
 * their inclusion in garbage collectible heap (which can make them potentially snoopable). See
 * https://medium.com/@_west_on/protecting-strings-in-jvm-memory-84c365f8f01c for the motivations around this.
 */
public final class SecureJSON {
    private static final boolean DEFAULT_STRICT_STRINGS = true;
    private static final boolean DEFAULT_STRICT_MAP_KEY_TYPES = true;

    private final boolean strictStrings;
    private final boolean strictMapKeyTypes;

    /**
     * Build a SecureJSON instance with default settings.
     */
    public SecureJSON() {
        strictStrings = DEFAULT_STRICT_STRINGS;
        strictMapKeyTypes = DEFAULT_STRICT_MAP_KEY_TYPES;
    }

    private SecureJSON(final Builder parBuilder) {
        strictStrings = parBuilder.strictStrings;
        strictMapKeyTypes = parBuilder.strictMapKeyTypes;
    }

    /**
     * Convert an object to a JSON character sequence. If it cannot be converted, throws JSONEncodeException. After the
     * consumer returns, the buffer will be destroyed so it MUST be fully consumed.
     *
     * @param parInput The input object to toJSONAble to JSON.
     * @param parConsumer The consumer to provide the JSON character sequence to when completed.
     * @throws JSONEncodeException On encode failure.
     */
    public void toJSON(final Object parInput, final IConsumer<CharSequence> parConsumer)
            throws JSONEncodeException {
        try (final JSONWriter myJsonWriter = new JSONWriter(new ObjectWriter())) {
            parConsumer.accept(myJsonWriter.write(parInput));
        } catch (final JSONEncodeException myException) {
            throw myException;
        } catch (final Exception myException) {
            throw new JSONEncodeException(myException);
        }
    }

    /**
     * Convert an object to a JSON byte array. If it cannot be converted, throws JSONEncodeException. After the consumer
     * returns, the buffer will be destroyed so it MUST be fully consumed.
     *
     * @param parInput The input object to toJSONAble to JSON.
     * @param parConsumer The consumer to provide the JSON character sequence to when completed.
     * @throws JSONEncodeException On encode failure.
     */
    public void toJSONBytes(final Object parInput, final IConsumer<byte[]> parConsumer)
            throws JSONEncodeException {
        try (final JSONWriter myJsonWriter = new JSONWriter(new ObjectWriter())) {
            parConsumer.accept(myJsonWriter.write(parInput).getBytes());
        } catch (final JSONEncodeException myException) {
            throw myException;
        } catch (final Exception myException) {
            throw new JSONEncodeException(myException);
        }
    }

    /**
     * Convert an object to a JSON string, writing to the provided stream.
     *
     * @param parInput The input object to toJSONAble to JSON.
     * @param parOutputStream The stream to write to.
     * @throws JSONEncodeException On encode failure.
     */
    public void toJSON(final Object parInput, final OutputStream parOutputStream) throws JSONEncodeException {
        toJSON(parInput, parOutputStream, StandardCharsets.UTF_8);
    }

    /**
     * Convert an object to a JSON string, writing to the provided stream.
     *
     * @param parInput The input object to toJSONAble to JSON.
     * @param parOutputStream The stream to write to.
     * @param parCharset The charset to use.
     * @throws JSONEncodeException On encode failure.
     */
    public void toJSON(final Object parInput, final OutputStream parOutputStream, final Charset parCharset)
            throws JSONEncodeException {
        try (final JSONWriter myJsonWriter = new JSONWriter(new ObjectWriter())) {
            myJsonWriter.write(parInput, new OutputStreamWriter(parOutputStream, parCharset));
        } catch (final JSONEncodeException myException) {
            throw myException;
        } catch (final Exception myException) {
            throw new JSONEncodeException(myException);
        }
    }

    /**
     * Convert a JSON character sequence to an object that consumer will accept. Throws JSONDecodeException on
     * failure. After the consumer returns, all buffers we created while parsing the JSON character sequence will be
     * destroyed.
     *
     * @param parInput The input character sequence to deserialize.
     * @param parConsumer The consumer to call with our unserialized JSON value.
     * @param <T> The type of object we expect. JSONDecodeException will be thrown if this is wrong. Note that Object
     *           (which will accept anything) is acceptable.
     * @throws JSONDecodeException On decode failure.
     */
    @SuppressWarnings("unchecked")
    public <T> void fromJSON(final CharSequence parInput, final IConsumer<T> parConsumer)
            throws JSONDecodeException {
        try (final JSONReader myJsonReader = new JSONReader()) {
            parConsumer.accept((T) myJsonReader.read(parInput));
        } catch (final JSONDecodeException myException) {
            throw myException;
        } catch (final Exception myException) {
            throw new JSONDecodeException(myException);
        }
    }

    /**
     * Convert a JSON character sequence to an object that consumer will accept. Throws JSONDecodeException on
     * failure. After the consumer returns, all buffers we created while parsing the JSON character sequence will be
     * destroyed.
     *
     * @param parInput The input character sequence to deserialize.
     * @param parConsumer The consumer to call with our unserialized JSON value.
     * @param parClass The class we will be building.
     * @param <T> The type of object we expect. JSONDecodeException will be thrown if this is wrong. Note that Object
     *           (which will accept anything) is acceptable.
     * @throws JSONException On decode failure.
     */
    @SuppressWarnings("unchecked")
    public <T> void fromJSON(final CharSequence parInput, final IConsumer<T> parConsumer,
                             final Class<T> parClass)
            throws JSONException {
        try {
            fromJSON(parInput, getConsumer(parConsumer, parClass));
        } catch (final JSONException.JSONRuntimeException myException) {
            throw myException.getCause();
        }
    }

    /**
     * Convert a JSON byte array to an object that consumer will accept. Throws JSONDecodeException on
     * failure. After the consumer returns, all buffers we created while parsing the JSON character sequence will be
     * destroyed.
     *
     * @param parInput The input character sequence to deserialize.
     * @param parConsumer The consumer to call with our unserialized JSON value.
     * @param <T> The type of object we expect. JSONDecodeException will be thrown if this is wrong. Note that Object
     *           (which will accept anything) is acceptable.
     * @throws JSONDecodeException On decode failure.
     */
    @SuppressWarnings("unchecked")
    public <T> void fromJSON(final byte[] parInput, final IConsumer<T> parConsumer)
            throws JSONDecodeException {
        try (final JSONReader myJsonReader = new JSONReader()) {
            parConsumer.accept((T) myJsonReader.read(new ByteArrayInputStream(parInput)));
        } catch (final JSONDecodeException myException) {
            throw myException;
        } catch (final Exception myException) {
            throw new JSONDecodeException(myException);
        }
    }

    /**
     * Convert a JSON byte array to an object that consumer will accept. Throws JSONDecodeException on
     * failure. After the consumer returns, all buffers we created while parsing the JSON character sequence will be
     * destroyed.
     *
     * @param parInput The input character sequence to deserialize.
     * @param parConsumer The consumer to call with our unserialized JSON value.
     * @param parClass The class we will be building.
     * @param <T> The type of object we expect. JSONDecodeException will be thrown if this is wrong. Note that Object
     *           (which will accept anything) is acceptable.
     * @throws JSONException On decode failure.
     */
    @SuppressWarnings("unchecked")
    public <T> void fromJSON(final byte[] parInput, final IConsumer<T> parConsumer, final Class<T> parClass)
            throws JSONException {
        try {
            fromJSON(parInput, getConsumer(parConsumer, parClass));
        } catch (final JSONException.JSONRuntimeException myException) {
            throw myException.getCause();
        }
    }

    /**
     * Read a JSON character sequence stream to an object that consumer will accept. Throws JSONDecodeException on
     * failure. After the consumer returns, all buffers we created while parsing the JSON character sequence will be
     * destroyed.
     *
     * @param parInput The input character stream to deserialize.
     * @param parConsumer The consumer to call with our unserialized JSON value.
     * @param <T> The type of object we expect. JSONDecodeException will be thrown if this is wrong. Note that Object
     *           (which will accept anything) is acceptable.
     * @throws JSONDecodeException On decode failure.
     */
    @SuppressWarnings("unchecked")
    public <T> void fromJSON(final InputStream parInput, final IConsumer<T> parConsumer) throws JSONDecodeException {
        try (final JSONReader myJsonReader = new JSONReader()) {
            parConsumer.accept((T) myJsonReader.read(parInput));
        } catch (final JSONDecodeException myException) {
            throw myException;
        } catch (final Exception myException) {
            throw new JSONDecodeException(myException);
        }
    }

    /**
     * Read a JSON character sequence stream to an object that consumer will accept. Throws JSONDecodeException on
     * failure. After the consumer returns, all buffers we created while parsing the JSON character sequence will be
     * destroyed.
     *
     * @param parInput The input character stream to deserialize.
     * @param parConsumer The consumer to call with our unserialized JSON value.
     * @param parClass The class we will be building.
     * @param <T> The type of object we expect. JSONDecodeException will be thrown if this is wrong. Note that Object
     *           (which will accept anything) is acceptable.
     * @throws JSONException On decode failure.
     */
    @SuppressWarnings("unchecked")
    public <T> void fromJSON(final InputStream parInput, final IConsumer<T> parConsumer, final Class<T> parClass)
            throws JSONException {
        try {
            fromJSON(parInput, getConsumer(parConsumer, parClass));
        } catch (final JSONException.JSONRuntimeException myException) {
            throw myException.getCause();
        }
    }

    private <T> IConsumer<Object> getConsumer(final IConsumer<T> parConsumer, final Class<T> parClass) {
        return new IConsumer<Object>() {
            @Override
            public void accept(final Object parOutput) {
                try {
                    parConsumer.accept(new ObjectReader<>(parClass, strictStrings, strictMapKeyTypes)
                        .accept(parOutput));
                } catch (final JSONException myException) {
                    throw new JSONException.JSONRuntimeException(myException);
                }
            }
        };
    }

    /**
     * Builder for SecureJSON, to allow specifying custom arguments.
     */
    public static final class Builder {
        private boolean strictStrings = DEFAULT_STRICT_STRINGS;
        private boolean strictMapKeyTypes = DEFAULT_STRICT_MAP_KEY_TYPES;

        /**
         * Set the strictStrings option. If strictStrings is true, we will never convert CharSequence to string. If it
         * is false, we will convert if we can't otherwise cast. Default is true.
         *
         * This should be used with caution, and certainly not for sensitive data as strings may stay in memory much
         * longer than desired.
         *
         * @param parStrictStrings The value to use for our strict strings setting.
         * @return A reference to this object.
         */
        public Builder strictStrings(final boolean parStrictStrings) {
            strictStrings = parStrictStrings;

            return this;
        }

        /**
         * Set the strictMapKeyTypes option. If strictMapKeyTypes is true, we will never convert CharSequence to string
         * when we are creating Maps (which require CharSequence-like keys). If false, we will try to convert the
         * object to a string.
         *
         * This should be used with caution, and certainly not for sensitive data as strings may stay in memory much
         * longer than desired.
         *
         * @param parStrictMapKeyTypes The value to use for our strict map keys setting.
         * @return A reference to this object.
         */
        public Builder strictMapKeyTypes(final boolean parStrictMapKeyTypes) {
            strictMapKeyTypes = parStrictMapKeyTypes;

            return this;
        }

        /**
         * Build a SecureJSON instance using our settings.
         *
         * @return A SecureJSON instance.
         */
        public SecureJSON build() {
            return new SecureJSON(this);
        }
    }
}
