package com.chelseaurquhart.securejson;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Objects;

/**
 * HugeDecimal is for numbers that are too big to parse with BigDecimal through normal means.
 */
public class HugeDecimal extends Number {
    private final CharSequence chars;

    HugeDecimal(final CharSequence parChars) {
        chars = parChars;
    }

    @Override
    public final int intValue() {
        return 0;
    }

    @Override
    public final long longValue() {
        return 0;
    }

    @Override
    public final float floatValue() {
        return 0;
    }

    @Override
    public final double doubleValue() {
        return 0;
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
    public final BigInteger bigIntegerValue() {
        return BigInteger.valueOf(0L);
    }

    /**
     * Get our value converted to a BigDecimal.
     *
     * @return a BigDecimal representation of our character sequence.
     */
    public final BigDecimal bigDecimalValue() {
        return new BigDecimal(0);
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
        if (chars.length() != myThat.chars.length()) {
            return false;
        }

        for (int myIndex = chars.length() - 1; myIndex >= 0; myIndex--) {
            if (chars.charAt(myIndex) != myThat.chars.charAt(myIndex)) {
                return false;
            }
        }

        return true;
    }

    @Override
    public final int hashCode() {
        return Objects.hash(chars);
    }
}
