package com.chelseaurquhart.securejson;

import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Objects;

/**
 * HugeDecimal is for numbers that are too big to parse with BigDecimal through normal means.
 */
public class HugeDecimal extends Number {
    private static final long serialVersionUID = 1L;

    private final CharSequence chars;
    private final transient NumberReader numberReader;
    private final Number number;

    HugeDecimal(final CharSequence parChars, final NumberReader parNumberReader) {
        chars = parChars;
        numberReader = parNumberReader;
        number = null;
    }

    HugeDecimal(final Number parValue) {
        chars = null;
        numberReader = null;
        number = parValue;
    }

    /**
     * Get our integer value.
     *
     * @return An integer representation of our value.
     */
    @Override
    public final int intValue() {
        if (number != null) {
            return number.intValue();
        }

        try {
            return numberReader.charSequenceToBigDecimal(chars, 0).getKey().intValue();
        } catch (final IOException myException) {
            throw new RuntimeException(myException);
        }
    }

    /**
     * Get our long value.
     *
     * @return A long representation of our value.
     */
    @Override
    public final long longValue() {
        if (number != null) {
            return number.longValue();
        }

        try {
            return numberReader.charSequenceToBigDecimal(chars, 0).getKey().longValue();
        } catch (final IOException myException) {
            throw new RuntimeException(myException);
        }
    }

    /**
     * Get our float value.
     *
     * @return A float representation of our value.
     */
    @Override
    public final float floatValue() {
        if (number != null) {
            return number.floatValue();
        }

        try {
            return numberReader.charSequenceToBigDecimal(chars, 0).getKey().floatValue();
        } catch (final IOException myException) {
            throw new RuntimeException(myException);
        }
    }

    /**
     * Get our double value.
     *
     * @return A double representation of our value.
     */
    @Override
    public final double doubleValue() {
        if (number != null) {
            return number.doubleValue();
        }

        try {
            return numberReader.charSequenceToBigDecimal(chars, 0).getKey().doubleValue();
        } catch (final IOException myException) {
            throw new RuntimeException(myException);
        }
    }

    /**
     * Get our raw character sequence value.
     *
     * @return Our raw character sequence value.
     */
    public final CharSequence charSequenceValue() {
        return chars;
    }

    /**
     * Get our value converted to a BigInteger.
     *
     * @return a BigInteger representation of our character sequence.
     */
    public final BigInteger bigIntegerValue() throws IOException {
        if (number != null) {
            if (number instanceof BigInteger) {
                return (BigInteger) number;
            }
            if (number instanceof BigDecimal) {
                return ((BigDecimal) number).toBigInteger();
            }
        }

        return numberReader.charSequenceToBigDecimal(chars, 0).getKey().toBigIntegerExact();
    }

    /**
     * Get our value converted to a BigDecimal.
     *
     * @return a BigDecimal representation of our character sequence.
     */
    public final BigDecimal bigDecimalValue() throws IOException {
        if (number != null) {
            if (number instanceof BigDecimal) {
                return (BigDecimal) number;
            }
            if (number instanceof BigInteger) {
                return new BigDecimal((BigInteger) number);
            }
        }

        return numberReader.charSequenceToBigDecimal(chars, 0).getKey();
    }

    @Override
    public final boolean equals(final Object parObject) {
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
            } catch (final IOException myException) {
                throw new RuntimeException(myException);
            }
        }
    }

    @Override
    public final int hashCode() {
        return Objects.hash(chars);
    }

    private void writeObject(final ObjectOutputStream parObjectOutputStream) throws IOException {
        throw new NotSerializableException();
    }

    private void readObject(final ObjectInputStream parObjectInputStream) throws ClassNotFoundException, IOException {
        throw new NotSerializableException();
    }
}
