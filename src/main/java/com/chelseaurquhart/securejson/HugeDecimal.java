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

import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Objects;

/**
 * HugeDecimal is for numbers that are too big to parse with BigDecimal through normal means.
 * Note that we will frequently convert to strings during conversion, so sensitive data should either
 *   a) not be stored in numeric types or
 *   b) should be retrieved from HugeDecimal.charSequenceValue()
 */
public final class HugeDecimal extends Number implements CharSequence {
    private static final long serialVersionUID = 1L;

    private transient CharSequence chars;
    private final transient NumberReader numberReader;
    private final transient Number number;

    /**
     * @exclude
     */
    HugeDecimal(final CharSequence parChars, final NumberReader parNumberReader) {
        super();
        chars = parChars;
        numberReader = parNumberReader;
        number = null;
    }

    /**
     * @exclude
     */
    HugeDecimal(final Number parValue) {
        super();
        if (parValue instanceof HugeDecimal) {
            final HugeDecimal myValueHugeDecimal = (HugeDecimal) parValue;
            chars = myValueHugeDecimal.chars;
            numberReader = myValueHugeDecimal.numberReader;
            number = myValueHugeDecimal.number;
        } else {
            chars = null;
            numberReader = null;
            number = parValue;
        }
    }

    /**
     * Get our integer value.
     *
     * @return An integer representation of our value.
     * @throws JSONRuntimeException On error.
     */
    @Override
    public int intValue() {
        if (number != null) {
            return number.intValue();
        }

        try {
            return numberReader.charSequenceToBigDecimal(chars, 0).getKey().intValue();
        } catch (final IOException | JSONException myException) {
            throw new JSONRuntimeException(myException);
        }
    }

    /**
     * Get our long value.
     *
     * @return A long representation of our value.
     * @throws JSONRuntimeException On error.
     */
    @Override
    public long longValue() {
        if (number != null) {
            return number.longValue();
        }

        try {
            return numberReader.charSequenceToBigDecimal(chars, 0).getKey().longValue();
        } catch (final IOException | JSONException myException) {
            throw new JSONRuntimeException(myException);
        }
    }

    /**
     * Get our float value.
     *
     * @return A float representation of our value.
     * @throws JSONRuntimeException On error.
     */
    @Override
    public float floatValue() {
        if (number != null) {
            return number.floatValue();
        }

        try {
            return numberReader.charSequenceToBigDecimal(chars, 0).getKey().floatValue();
        } catch (final IOException | JSONException myException) {
            throw new JSONRuntimeException(myException);
        }
    }

    /**
     * Get our double value.
     *
     * @return A double representation of our value.
     * @throws JSONRuntimeException On error.
     */
    @Override
    public double doubleValue() {
        if (number != null) {
            return number.doubleValue();
        }

        try {
            return numberReader.charSequenceToBigDecimal(chars, 0).getKey().doubleValue();
        } catch (final IOException | JSONException myException) {
            throw new JSONRuntimeException(myException);
        }
    }

    /**
     * Get our raw character sequence value.
     *
     * @return Our raw character sequence value.
     */
    public CharSequence charSequenceValue() {
        if (number != null) {
            return number.toString();
        }

        return chars;
    }

    /**
     * Get our value converted to a BigInteger.
     *
     * @return a BigInteger representation of our character sequence.
     * @throws IOException On IO error with resource file.
     * @throws JSONException On sequence read failure.
     */
    public BigInteger bigIntegerValue() throws IOException, JSONException {
        if (number != null) {
            if (number instanceof BigInteger) {
                return (BigInteger) number;
            }
            if (number instanceof BigDecimal) {
                return ((BigDecimal) number).toBigInteger();
            }

            // decimal first and then big integer, since this will allow us to strip .0000 without hacks.
            return new BigDecimal(number.toString()).toBigIntegerExact();
        }

        return numberReader.charSequenceToBigDecimal(chars, 0).getKey().toBigIntegerExact();
    }

    /**
     * Get our value converted to a BigDecimal.
     *
     * @return a BigDecimal representation of our character sequence.
     * @throws IOException On IO error with resource file.
     * @throws JSONException On sequence read failure.
     */
    public BigDecimal bigDecimalValue() throws IOException, JSONException {
        if (number != null) {
            if (number instanceof BigDecimal) {
                return (BigDecimal) number;
            }
            if (number instanceof BigInteger) {
                return new BigDecimal((BigInteger) number);
            }

            return new BigDecimal(number.toString());
        }

        return numberReader.charSequenceToBigDecimal(chars, 0).getKey();
    }

    @Override
    public boolean equals(final Object parObject) {
        if (this == parObject) {
            return true;
        }
        if (parObject == null || getClass() != parObject.getClass()) {
            return false;
        }
        final HugeDecimal myThat = (HugeDecimal) parObject;
        if (chars != null && myThat.chars != null) {
            if (chars.length() != myThat.chars.length()) {
                return false;
            }

            for (int myIndex = chars.length() - 1; myIndex >= 0; myIndex--) {
                if (chars.charAt(myIndex) != myThat.chars.charAt(myIndex)) {
                    return false;
                }
            }

            return true;
        } else {
            try {
                return bigDecimalValue().equals(myThat.bigDecimalValue());
            } catch (final IOException | JSONException myException) {
                throw new JSONRuntimeException(myException);
            }
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(chars);
    }

    @Override
    public int length() {
        if (number != null && chars == null) {
            chars = number.toString();
        }
        return chars.length();
    }

    @Override
    public char charAt(final int parIndex) {
        if (number != null && chars == null) {
            chars = number.toString();
        }
        return chars.charAt(parIndex);
    }

    @Override
    public CharSequence subSequence(final int parStart, final int parEnd) {
        if (number != null && chars == null) {
            chars = number.toString();
        }
        return chars.subSequence(parStart, parEnd);
    }

    private void writeObject(final ObjectOutputStream parObjectOutputStream) throws IOException {
        throw new NotSerializableException();
    }

    private void readObject(final ObjectInputStream parObjectInputStream) throws ClassNotFoundException, IOException {
        throw new NotSerializableException();
    }
}
