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

import com.chelseaurquhart.securejson.JSONException.JSONRuntimeException;

import java.io.ByteArrayInputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * SecureJSON is a JSON serializer and deserializer with strict security in mind. It does not create strings due to
 * their inclusion in garbage collectible heap (which can make them potentially snoopable). See
 * https://medium.com/@_west_on/protecting-strings-in-jvm-memory-84c365f8f01c for the motivations around this.
 *
 * <p>
 * .. DANGER::
 *     Please note that this is not a substitution for secure coding practices or maintaining a secure environment. If
 *     an attacker has access to your JVM's memory, there isn't really anything you can do to guarantee that they can't
 *     see sensitive data, but the fleeting nature of data managed in this manner helps ensure that sensitive
 *     information is not kept in memory any longer than is necessary and as such helps to mitigate the risks.
 * </p>
 */
public final class SecureJSON {
    private final Settings settings;

    /**
     * Build a SecureJSON instance with default settings.
     */
    public SecureJSON() {
        settings = new Settings();
    }

    private SecureJSON(final Builder parBuilder) {
        settings = new Settings(parBuilder);
    }

    /**
     * Convert an object to a JSON character sequence. If it cannot be converted, throws JSONEncodeException. After the
     * consumer returns, the buffer will be destroyed so it MUST be fully consumed.
     *
     * <p>Example:</p>::
     *     <code>
     *
     *        import com.chelseaurquhart.securejson.SecureJSON;
     *        import java.util.Arrays;
     *        final SecureJSON secureJSON = new SecureJSON();
     *        try {
     *            secureJSON.toJSON(Arrays.asList("1", 2, "three"), new IConsumer&lt;CharSequence&gt;() {
     *                &#64;Override
     *                public void accept(final CharSequence input) {
     *                    // do something with input
     *                }
     *            });
     *        } catch (final JSONEncodeException e) {
     *        }
     *        // Warning: even if you copied input to a local variable above, it is destroyed before this
     *        // line and you will no longer be able to access it.
     *     </code>
     *
     * @param parInput The input object to toJSONAble to JSON.
     * @param parConsumer The consumer to provide the JSON character sequence to when completed.
     * @throws JSONEncodeException On encode failure.
     */
    public void toJSON(final Object parInput, final IConsumer<CharSequence> parConsumer)
            throws JSONEncodeException {
        Objects.requireNonNull(parConsumer);

        writeJSON(new IThrowableConsumer<JSONWriter>() {
            @Override
            public void accept(final JSONWriter parWriter) throws IOException, JSONException {
                parConsumer.accept(parWriter.write(parInput));
            }
        });
    }

    /**
     * Convert an object to a JSON byte array. If it cannot be converted, throws JSONEncodeException. After the consumer
     * returns, the buffer will be destroyed so it MUST be fully consumed.
     *
     * <p>Example:</p>::
     *     <code>
     *
     *        import com.chelseaurquhart.securejson.SecureJSON;
     *        final SecureJSON secureJSON = new SecureJSON();
     *        try {
     *            secureJSON.toJSONBytes(Arrays.asList("1", 2, "three"), new IConsumer&lt;byte[]&gt;() {
     *                &#64;Override
     *                public void accept(final byte[] input) {
     *                    // do something with input
     *                }
     *            });
     *        } catch (final JSONEncodeException e) {
     *        }
     *        // Warning: even if you copied input to a local variable above, it is destroyed before this
     *        // line and you will no longer be able to access it.
     *     </code>
     *
     * @param parInput The input object to toJSONAble to JSON.
     * @param parConsumer The consumer to provide the JSON character sequence to when completed.
     * @throws JSONEncodeException On encode failure.
     */
    public void toJSONBytes(final Object parInput, final IConsumer<byte[]> parConsumer)
            throws JSONEncodeException {
        Objects.requireNonNull(parConsumer);

        writeJSON(new IThrowableConsumer<JSONWriter>() {
            @Override
            public void accept(final JSONWriter parWriter) throws IOException, JSONException {
                parConsumer.accept(parWriter.write(parInput).getBytes());
            }
        });
    }

    /**
     * Convert an object to a JSON string, writing to the provided stream and specifying the character set.
     *
     * <p>Example:</p>::
     *     <code>
     *
     *        import com.chelseaurquhart.securejson.SecureJSON;
     *        final SecureJSON secureJSON = new SecureJSON();
     *        try (final OutputStream outputStream = new FileOutputStream("file.json")) {
     *            secureJSON.toJSON(Arrays.asList("1", 2, "three"), outputStream);
     *        } catch (final JSONEncodeException e) {
     *        }
     *     </code>
     *
     * @param parInput The input object to toJSONAble to JSON.
     * @param parOutputStream The stream to write to.
     * @throws JSONEncodeException On encode failure.
     */
    public void toJSON(final Object parInput, final OutputStream parOutputStream) throws JSONEncodeException {
        Objects.requireNonNull(parOutputStream);

        writeJSON(new IThrowableConsumer<JSONWriter>() {
            @Override
            public void accept(final JSONWriter parWriter) throws IOException, JSONException {
                parWriter.write(parInput, new OutputStreamWriter(parOutputStream, StandardCharsets.UTF_8));
            }
        });
    }

    /**
     * Convert a JSON character sequence to an object that consumer will accept. Throws JSONDecodeException on
     * failure. After the consumer returns, all buffers we created while parsing the JSON character sequence will be
     * destroyed.
     *
     * <p>Example:</p>::
     *     <code>
     *
     *        import com.chelseaurquhart.securejson.SecureJSON;
     *        final SecureJSON secureJSON = new SecureJSON();
     *        try {
     *            secureJSON.fromJSON("{}", new IConsumer&lt;Map&lt;CharSequence, Object&gt;&gt;() {
     *                &#64;Override
     *                public void accept(final Map&lt;CharSequence, Object&gt; input) {
     *                    // do something with input
     *                }
     *            });
     *        } catch (final JSONDecodeException e) {
     *        }
     *        // Warning: Any buffers we created for the Map above will be destroyed at this point, so they should
     *        // be either consumed in accept, or converted to strings (if they do not contain sensitive information.)
     *     </code>
     *
     * @param parInput The input character sequence to deserialize.
     * @param parConsumer The consumer to call with our unserialized JSON value.
     * @param <T> The type of object we expect. JSONDecodeException will be thrown if this is wrong. Note that Object
     *           (which will accept anything) is acceptable.
     * @throws JSONDecodeException On decode failure.
     */
    public <T> void fromJSON(final CharSequence parInput, final IConsumer<T> parConsumer) throws JSONDecodeException {
        Objects.requireNonNull(parInput);
        Objects.requireNonNull(parConsumer);

        readJSON(new IThrowableFunction<JSONReader, T>() {
            @Override
            @SuppressWarnings("unchecked")
            public T accept(final JSONReader parReader) throws IOException, JSONException {
                return (T) parReader.read(parInput);
            }
        }, parConsumer);
    }

    /**
     * Convert a JSON character sequence to an object that consumer will accept. Throws JSONDecodeException on
     * failure. After the consumer returns, all buffers we created while parsing the JSON character sequence will be
     * destroyed. This is very similar to its counterpart that doesn't take a class argument, but this supports
     * encoding into that class instead of into java types.
     *
     * <p>Example:</p>::
     *     <code>
     *
     *        import com.chelseaurquhart.securejson.SecureJSON;
     *        final SecureJSON secureJSON = new SecureJSON();
     *
     *        // This class will be read into.
     *        class MyCustomClass {
     *            private int myNumber;
     *            private CharSequence myString;
     *        }
     *        try {
     *            secureJSON.fromJSON("{}", new IConsumer&lt;MyCustomClass&gt;() {
     *                &#64;Override
     *                public void accept(final MyCustomClass input) {
     *                    // do something with input
     *                }
     *            }, MyCustomClass.class);
     *        } catch (final JSONDecodeException e) {
     *        }
     *        // Warning: Any buffers we created for the MyCustomClass instance above will be destroyed at this point,
     *        // so they should be either consumed in accept, or converted to strings (if they do not contain sensitive
     *        // information.)
     *     </code>
     *
     * @param parInput The input character sequence to deserialize.
     * @param parConsumer The consumer to call with our unserialized JSON value.
     * @param parClass The class we will be building.
     * @param <T> The type of object we expect. JSONDecodeException will be thrown if this is wrong. Note that Object
     *           (which will accept anything) is acceptable.
     * @throws JSONDecodeException On decode failure.
     */
    public <T> void fromJSON(final CharSequence parInput, final IConsumer<T> parConsumer, final Class<T> parClass)
            throws JSONDecodeException {
        fromJSON(parInput, getConsumer(parConsumer, parClass));
    }

    /**
     * Convert a JSON byte array to an object that consumer will accept. Throws JSONDecodeException on
     * failure. After the consumer returns, all buffers we created while parsing the JSON character sequence will be
     * destroyed.
     *
     * <p>Example:</p>::
     *     <code>
     *
     *        import com.chelseaurquhart.securejson.SecureJSON;
     *        final SecureJSON secureJSON = new SecureJSON();
     *        try {
     *            secureJSON.fromJSON("{}".getBytes(), new IConsumer&lt;Map&lt;CharSequence, Object&gt;&gt;() {
     *                &#64;Override
     *                public void accept(final Map&lt;CharSequence, Object&gt; input) {
     *                    // do something with input
     *                }
     *            });
     *        } catch (final JSONDecodeException e) {
     *        }
     *        // Warning: Any buffers we created for the Map above will be destroyed at this point, so they should
     *        // be either consumed in accept, or converted to strings (if they do not contain sensitive information.)
     *     </code>
     *
     * @param parInput The input character sequence to deserialize.
     * @param parConsumer The consumer to call with our unserialized JSON value.
     * @param <T> The type of object we expect. JSONDecodeException will be thrown if this is wrong. Note that Object
     *           (which will accept anything) is acceptable.
     * @throws JSONDecodeException On decode failure.
     */
    public <T> void fromJSON(final byte[] parInput, final IConsumer<T> parConsumer) throws JSONDecodeException {
        Objects.requireNonNull(parInput);
        Objects.requireNonNull(parConsumer);

        readJSON(new IThrowableFunction<JSONReader, T>() {
            @Override
            @SuppressWarnings("unchecked")
            public T accept(final JSONReader parReader) throws IOException, JSONException {
                return (T) parReader.read(new ByteArrayInputStream(parInput));
            }
        }, parConsumer);
    }

    /**
     * Convert a JSON byte array to an object that consumer will accept. Throws JSONDecodeException on
     * failure. After the consumer returns, all buffers we created while parsing the JSON character sequence will be
     * destroyed. Of special note here, even though we can erase the byte[] array, we will not. That is up to the caller
     * to do so.
     *
     * <p>Example:</p>::
     *     <code>
     *
     *        import com.chelseaurquhart.securejson.SecureJSON;
     *        final SecureJSON secureJSON = new SecureJSON();
     *
     *        // This class will be read into.
     *        class MyCustomClass {
     *            private int myNumber;
     *            private CharSequence myString;
     *        }
     *        try {
     *            secureJSON.fromJSON("{}", new IConsumer&lt;MyCustomClass&gt;() {
     *                &#64;Override
     *                public void accept(final MyCustomClass input) {
     *                    // do something with input
     *                }
     *            }, MyCustomClass.class);
     *        } catch (final JSONDecodeException e) {
     *        }
     *        // Warning: Any buffers we created for the MyCustomClass instance above will be destroyed at this point,
     *        // so they should be either consumed in accept, or converted to strings (if they do not contain sensitive
     *        // information.)
     *     </code>
     *
     * @param parInput The input character sequence to deserialize.
     * @param parConsumer The consumer to call with our unserialized JSON value.
     * @param parClass The class we will be building.
     * @param <T> The type of object we expect. JSONDecodeException will be thrown if this is wrong. Note that Object
     *           (which will accept anything) is acceptable.
     * @throws JSONDecodeException On decode failure.
     */
    public <T> void fromJSON(final byte[] parInput, final IConsumer<T> parConsumer, final Class<T> parClass)
            throws JSONDecodeException {
        Objects.requireNonNull(parInput);
        Objects.requireNonNull(parConsumer);

        fromJSON(parInput, getConsumer(parConsumer, parClass));
    }

    /**
     * Read a JSON character sequence stream to an object that consumer will accept. Throws JSONDecodeException on
     * failure. After the consumer returns, all buffers we created while parsing the JSON character sequence will be
     * destroyed.
     *
     * <p>Example:</p>::
     *     <code>
     *
     *        import com.chelseaurquhart.securejson.SecureJSON;
     *        final SecureJSON secureJSON = new SecureJSON();
     *        final InputStream inputStream = new ByteArrayInputStream("{}".getBytes());
     *        try {
     *            secureJSON.fromJSON(inputStream, new IConsumer&lt;Map&lt;CharSequence, Object&gt;&gt;() {
     *                &#64;Override
     *                public void accept(final Map&lt;CharSequence, Object&gt; input) {
     *                    // do something with input
     *                }
     *            });
     *        } catch (final JSONDecodeException e) {
     *        }
     *        // Warning: Any buffers we created for the Map above will be destroyed at this point, so they should
     *        // be either consumed in accept, or converted to strings (if they do not contain sensitive information.)
     *     </code>
     *
     * @param parInput The input character stream to deserialize.
     * @param parConsumer The consumer to call with our unserialized JSON value.
     * @param <T> The type of object we expect. JSONDecodeException will be thrown if this is wrong. Note that Object
     *           (which will accept anything) is acceptable.
     * @throws JSONDecodeException On decode failure.
     */
    public <T> void fromJSON(final InputStream parInput, final IConsumer<T> parConsumer) throws JSONDecodeException {
        Objects.requireNonNull(parInput);
        Objects.requireNonNull(parConsumer);

        readJSON(new IThrowableFunction<JSONReader, T>() {
            @Override
            @SuppressWarnings("unchecked")
            public T accept(final JSONReader parReader) throws IOException, JSONException {
                return (T) parReader.read(parInput);
            }
        }, parConsumer);
    }

    /**
     * Read a JSON character sequence stream to an object that consumer will accept. Throws JSONDecodeException on
     * failure. After the consumer returns, all buffers we created while parsing the JSON character sequence will be
     * destroyed.
     *
     * <p>Example:</p>::
     *     <code>
     *
     *        import com.chelseaurquhart.securejson.SecureJSON;
     *        final SecureJSON secureJSON = new SecureJSON();
     *        final InputStream inputStream = new ByteArrayInputStream("{}".getBytes());
     *
     *        // This class will be read into.
     *        class MyCustomClass {
     *            private int myNumber;
     *            private CharSequence myString;
     *        }
     *        try {
     *            secureJSON.fromJSON(inputStream, new IConsumer&lt;MyCustomClass&gt;() {
     *                &#64;Override
     *                public void accept(final MyCustomClass input) {
     *                    // do something with input
     *                }
     *            }, MyCustomClass.class);
     *        } catch (final JSONDecodeException e) {
     *        }
     *        // Warning: Any buffers we created for the MyCustomClass instance above will be destroyed at this point,
     *        // so they should be either consumed in accept, or converted to strings (if they do not contain sensitive
     *        // information.)
     *     </code>
     *
     * @param parInput The input character stream to deserialize.
     * @param parConsumer The consumer to call with our unserialized JSON value.
     * @param parClass The class we will be building.
     * @param <T> The type of object we expect. JSONDecodeException will be thrown if this is wrong. Note that Object
     *           (which will accept anything) is acceptable.
     * @throws JSONDecodeException On decode failure.
     */
    public <T> void fromJSON(final InputStream parInput, final IConsumer<T> parConsumer, final Class<T> parClass)
            throws JSONDecodeException {
        Objects.requireNonNull(parInput);
        Objects.requireNonNull(parConsumer);

        fromJSON(parInput, getConsumer(parConsumer, parClass));
    }

    private <T> void readJSON(final IThrowableFunction<JSONReader, T> parReadFunc, final IConsumer<T> parConsumer)
            throws JSONDecodeException {
        final JSONReader myJsonReader = new JSONReader.Builder(settings).build();
        try {
            parConsumer.accept(parReadFunc.accept(myJsonReader));
        } catch (final JSONException myException) {
            throw JSONDecodeException.fromException(myException);
        } catch (final JSONRuntimeException myException) {
            throw JSONDecodeException.fromException(myException);
        } catch (final IOException myException) {
            throw JSONDecodeException.fromException(myException);
        } catch (final ClassCastException myException) {
            throw JSONDecodeException.fromException(myException);
        } finally {
            closeDecodeResource(myJsonReader);
        }
    }

    private void writeJSON(final IThrowableConsumer<JSONWriter> parWriteFunc) throws JSONEncodeException {
        final JSONWriter myJsonWriter = new JSONWriter(new ObjectWriter(), settings);
        try {
            parWriteFunc.accept(myJsonWriter);
        } catch (final JSONRuntimeException myException) {
            throw JSONEncodeException.fromException(myException);
        } catch (final IOException myException) {
            throw JSONEncodeException.fromException(myException);
        } catch (final JSONException myException) {
            throw JSONEncodeException.fromException(myException);
        } finally {
            closeEncodeResource(myJsonWriter);
        }
    }

    private void closeEncodeResource(final Closeable parResource) throws JSONEncodeException {
        if (parResource != null) {
            try {
                parResource.close();
            } catch (final IOException myException) {
                throw JSONEncodeException.fromException(myException);
            }
        }
    }

    private void closeDecodeResource(final Closeable parResource) throws JSONDecodeException {
        if (parResource != null) {
            try {
                parResource.close();
            } catch (final IOException myException) {
                throw JSONDecodeException.fromException(myException);
            }
        }
    }

    private <T> IConsumer<?> getConsumer(final IConsumer<T> parConsumer, final Class<T> parClass) {
        Objects.requireNonNull(parConsumer);

        if (parClass == null) {
            return parConsumer;
        }

        return new IConsumer<Object>() {
            @Override
            public void accept(final Object parOutput) {
                try {
                    parConsumer.accept(new ObjectReader<T>(parClass, settings)
                        .accept(parOutput));
                } catch (final IOException myException) {
                    throw new JSONRuntimeException(myException);
                } catch (final JSONException myException) {
                    throw new JSONRuntimeException(myException);
                }
            }
        };
    }

    /**
     * Builder for SecureJSON. This allows specifying options for the encoder and decoder to use.
     */
    public static final class Builder {
        private boolean strictStrings = Settings.DEFAULT_STRICT_STRINGS;
        private boolean strictMapKeyTypes = Settings.DEFAULT_STRICT_MAP_KEY_TYPES;
        private IFunction<Integer, IWritableCharSequence> writableCharBufferFactory =
            Settings.DEFAULT_WRITABLE_CHAR_BUFFER_FACTORY;
        private final Map<Class<?>, IFunction<Object, ?>> classInitializers
            = new HashMap<Class<?>, IFunction<Object, ?>>();

        /**
         * Set the strictStrings option. If strictStrings is true, we will never convert CharSequence to string. If it
         * is false, we will convert if we can't otherwise cast. Default is true.
         *
         * This should be used with caution, and certainly not for sensitive data as strings may stay in memory much
         * longer than desired.
         *
         * <p>
         *     **Default**: true
         * </p>
         *
         * <p>Example (disable strict strings in order to allow Strings to be populated for non-sensitive fields):</p>::
         *     <code>
         *
         *        import com.chelseaurquhart.securejson.SecureJSON;
         *        final SecureJSON secureJSON = new SecureJSON.Builder()
         *            .strictStrings(false)
         *            .build();
         *     </code>
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
         * <p>
         *     **Default**: true
         * </p>
         *
         * <p>Example (disable strict map key types in order to allow Strings to be populated for Map keys):</p>::
         *     <code>
         *
         *        import com.chelseaurquhart.securejson.SecureJSON;
         *        final SecureJSON secureJSON = new SecureJSON.Builder()
         *            .strictMapKeyTypes(false)
         *            .build();
         *     </code>
         *
         * @param parStrictMapKeyTypes The value to use for our strict map keys setting.
         * @return A reference to this object.
         */
        public Builder strictMapKeyTypes(final boolean parStrictMapKeyTypes) {
            strictMapKeyTypes = parStrictMapKeyTypes;

            return this;
        }

        /**
         * Set the factory to use for building secure buffers. By default we will use our own implementation, but this
         * can be used to provide a custom one.
         *
         * <p>
         *     **Default**: &lt;internally managed factory&gt;
         * </p>
         *
         * <p>Example (use a custom IWritableCharSequence factory):</p>
         * <p>
         * .. DANGER::
         *   This sample implementation is by no means secure! The default, however, is.
         *
         * </p>::
         *    <code>
         *
         *        import com.chelseaurquhart.securejson.SecureJSON;
         *        final SecureJSON secureJSON = new SecureJSON.Builder()
         *            .writableCharBufferFactory(new IFunction&lt;Integer, IWritableCharSequence&gt;() {
         *                &#64;Override
         *                public IWritableCharSequence accept(final Integer parCapacity) {
         *                    return new IWritableCharSequence() {
         *                        private final StringBuilder builder = new StringBuilder(parCapacity);
         *
         *                        &#64;Override
         *                        public void append(char parChar) {
         *                            builder.append(parCapacity);
         *                        }
         *
         *                        &#64;Override
         *                        public boolean isRestrictedToCapacity() {
         *                            return false;
         *                        }
         *
         *                        &#64;Override
         *                        public int getCapacity() {
         *                            return builder.capacity();
         *                        }
         *
         *                        &#64;Override
         *                        public void close() {
         *                            builder.setLength(0);
         *                        }
         *
         *                        &#64;Override
         *                        public int length() {
         *                            return builder.length();
         *                        }
         *
         *                        &#64;Override
         *                        public char charAt(final int parIndex) {
         *                            return builder.charAt(parIndex);
         *                        }
         *
         *                        &#64;Override
         *                        public CharSequence subSequence(final int parStart, final int parEnd) {
         *                            return builder.subSequence(parStart, parEnd);
         *                        }
         *                    };
         *                }
         *            })
         *            .build();
         *     </code>
         *
         * @param parWritableCharBufferFactory The factory to use for building secure buffers.
         * @return A reference to this object.
         */
        public Builder writableCharBufferFactory(
                final IFunction<Integer, IWritableCharSequence> parWritableCharBufferFactory) {
            writableCharBufferFactory = Objects.requireNonNull(parWritableCharBufferFactory);

            return this;
        }

        /**
         * Register a class initializer. This will allow classes to be constructed without using reflection, assuming
         * that they inherit from IJSONDeserializeAware. Note that while the full input will be provided to extract
         * any construction parameters, etc, the object should still implement IJSONDeserializeAware unless the
         * intention is to allow reflection to do the rest of the property initializations.
         *
         * <p>Example:</p>::
         *     <code>
         *
         *        import com.chelseaurquhart.securejson.SecureJSON;
         *        final SecureJSON secureJSON = new SecureJSON.Builder()
         *            .registerClassInitializer(SomeClass.class, () -> new SomeClass("required arg"))
         *            .build();
         *     </code>
         *
         * @param parClazz The class to register.
         * @param parInitializer The initializer to register.
         * @param <T> The type of class to register.
         * @return A reference to this object.
         */
        public <T> Builder registerClassInitializer(final Class<T> parClazz,
                                                    final IFunction<Object, T> parInitializer) {
            classInitializers.put(parClazz, parInitializer);

            return this;
        }

        /**
         * Build a SecureJSON instance using our settings.
         *
         * <p>Example:</p>::
         *     <code>
         *
         *        import com.chelseaurquhart.securejson.SecureJSON;
         *        final SecureJSON secureJSON = new SecureJSON.Builder().build();
         *     </code>
         *
         * <p>The above example is functionally equivalent to:</p>::
         *     <code>
         *
         *        import com.chelseaurquhart.securejson.SecureJSON;
         *        final SecureJSON secureJSON = new SecureJSON();
         *     </code>
         *
         * @return A SecureJSON instance.
         */
        public SecureJSON build() {
            return new SecureJSON(this);
        }

        /**
         * @exclude
         */
        boolean isStrictStrings() {
            return strictStrings;
        }

        /**
         * @exclude
         */
        boolean isStrictMapKeyTypes() {
            return strictMapKeyTypes;
        }

        /**
         * @exclude
         */
        IFunction<Integer, IWritableCharSequence> getWritableCharBufferFactory() {
            return writableCharBufferFactory;
        }

        /**
         * @exclude
         */
        Map<Class<?>, IFunction<Object, ?>> getClassInitializers() {
            return classInitializers;
        }
    }

}
