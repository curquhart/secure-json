package com.chelseaurquhart.securejson;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * SecureJSON is a JSON serializer and deserializer with strict security in mind. It does not create strings due to
 * their inclusion in garbage collectible heap (which can make them potentially snoopable). See
 * https://medium.com/@_west_on/protecting-strings-in-jvm-memory-84c365f8f01c for the motivations around this. It
 * uses the excellent SecureString library (https://github.com/NovaCrypto/SecureString) for the secure data structures.
 */
public final class SecureJSON {
    private SecureJSON() {
    }

    /**
     * Convert an object to a JSON string. If it cannot be converted, throws JSONEncodeException. After the consumer
     * returns, the buffer will be destroyed so it MUST be fully consumed.
     *
     * @param parInput The input object to serialize to JSON.
     * @param parConsumer The consumer to provide the JSON character sequence to when completed.
     * @throws JSONEncodeException On encode failure.
     */
    public static void toJSON(final Object parInput, final IConsumer<CharSequence> parConsumer)
            throws JSONEncodeException {
        try (final JSONWriter myJsonWriter = new JSONWriter()) {
            parConsumer.accept(myJsonWriter.write(parInput));
        } catch (final JSONEncodeException myException) {
            throw myException;
        } catch (final Exception myException) {
            throw new JSONEncodeException(myException);
        }
    }

    /**
     * Convert an object to a JSON string, writing to the provided stream.
     *
     * @param parInput The input object to serialize to JSON.
     * @param parOutputStream The stream to write to.
     * @throws JSONEncodeException On encode failure.
     */
    public static void toJSON(final Object parInput, final OutputStream parOutputStream) throws JSONEncodeException {
        toJSON(parInput, parOutputStream, StandardCharsets.UTF_8);
    }

    /**
     * Convert an object to a JSON string, writing to the provided stream.
     *
     * @param parInput The input object to serialize to JSON.
     * @param parOutputStream The stream to write to.
     * @param parCharset The charset to use.
     * @throws JSONEncodeException On encode failure.
     */
    public static void toJSON(final Object parInput, final OutputStream parOutputStream, final Charset parCharset)
            throws JSONEncodeException {
        try (final JSONWriter myJsonWriter = new JSONWriter()) {
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
     * @param parCustomer The consumer to call with our unserialized JSON value.
     * @param <T> The type of object we expect. JSONDecodeException will be thrown if this is wrong. Note that Object
     *           (which will accept anything) is acceptable.
     * @throws JSONDecodeException On decode failure.
     */
    @SuppressWarnings("unchecked")
    public static <T> void fromJSON(final CharSequence parInput, final IConsumer<T> parCustomer)
            throws JSONDecodeException {
        try (final JSONReader myJsonReader = new JSONReader()) {
            parCustomer.accept((T) myJsonReader.read(parInput));
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
     * @param parCustomer The consumer to call with our unserialized JSON value.
     * @param <T> The type of object we expect. JSONDecodeException will be thrown if this is wrong. Note that Object
     *           (which will accept anything) is acceptable.
     * @throws JSONDecodeException On decode failure.
     */
    @SuppressWarnings("unchecked")
    public static <T> void fromJSON(final InputStream parInput, final IConsumer<T> parCustomer)
            throws JSONDecodeException {
        try (final JSONReader myJsonReader = new JSONReader()) {
            parCustomer.accept((T) myJsonReader.read(parInput));
        } catch (final JSONDecodeException myException) {
            throw myException;
        } catch (final Exception myException) {
            throw new JSONDecodeException(myException);
        }
    }
}
