package com.chelseaurquhart.securejson;

import  com.chelseaurquhart.securejson.JSONDecodeException.MalformedNumberException;
import  com.chelseaurquhart.securejson.JSONDecodeException.InvalidTokenException;

import io.github.novacrypto.SecureCharBuffer;
import org.testng.annotations.DataProvider;

import java.io.IOException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.math.RoundingMode;

public final class NumberProvider {
    private static final MathContext HUGE_PRECISION_MATH_CONTEXT = new MathContext((int) Math.pow(2, 17),
        RoundingMode.UNNECESSARY);
    static final String DATA_PROVIDER_NAME = "NumberProvider";

    private NumberProvider() {
    }

    /**
     * This provider is consumed by the number parser as well as the json reader. The json reader tests are much less
     * strict than those of the number parser, but it confirms that numbers read (and fail to read) appropriately.
     *
     * @param parMethod The method being executed.
     * @return A collection of parameters.
     * @throws IOException If message loading failed.
     */
    @DataProvider(name = DATA_PROVIDER_NAME, parallel = true)
    public static Object[] dataProvider(final Method parMethod) throws IOException {
        return new Object[]{
            // whole numbers at edges of type ranges
            buildParameters(
                "short min - 1",
                "" + (Short.MIN_VALUE - 1),
                Short.MIN_VALUE - 1,
                Integer.class,
                NumberReader.DEFAULT_MATH_CONTEXT
            ),
            buildParameters(
                "short min",
                "" + Short.MIN_VALUE,
                Short.MIN_VALUE,
                Short.class,
                NumberReader.DEFAULT_MATH_CONTEXT
            ),
            buildParameters(
                "short min + 1",
                "" + (Short.MIN_VALUE + 1),
                (short) (Short.MIN_VALUE + 1),
                Short.class,
                NumberReader.DEFAULT_MATH_CONTEXT
            ),
            buildParameters(
                "int min - 1",
                "" + ((long) Integer.MIN_VALUE - 1),
                (long) Integer.MIN_VALUE - 1,
                Long.class,
                NumberReader.DEFAULT_MATH_CONTEXT
            ),
            buildParameters(
                "int min",
                "" + Integer.MIN_VALUE,
                Integer.MIN_VALUE,
                Integer.class,
                NumberReader.DEFAULT_MATH_CONTEXT
            ),
            buildParameters(
                "int min + 1",
                "" + (Integer.MIN_VALUE + 1),
                Integer.MIN_VALUE + 1,
                Integer.class,
                NumberReader.DEFAULT_MATH_CONTEXT
            ),
            buildParameters(
                "long min - 1",
                new BigDecimal(BigInteger.valueOf(Long.MIN_VALUE)).subtract(BigDecimal.ONE).toString(),
                new BigDecimal(BigInteger.valueOf(Long.MIN_VALUE)).subtract(BigDecimal.ONE),
                BigDecimal.class,
                HUGE_PRECISION_MATH_CONTEXT
            ),
            buildParameters(
                "long min",
                "" + Long.MIN_VALUE,
                Long.MIN_VALUE,
                Long.class,
                HUGE_PRECISION_MATH_CONTEXT
            ),
            buildParameters(
                "long min + 1",
                "" + (Long.MIN_VALUE + 1),
                Long.MIN_VALUE + 1,
                Long.class,
                HUGE_PRECISION_MATH_CONTEXT
            ),
            buildParameters(
                "short max - 1",
                "" + (Short.MAX_VALUE - 1),
                (short) (Short.MAX_VALUE - 1),
                Short.class,
                NumberReader.DEFAULT_MATH_CONTEXT
            ),
            buildParameters(
                "short max",
                "" + Short.MAX_VALUE,
                Short.MAX_VALUE,
                Short.class,
                NumberReader.DEFAULT_MATH_CONTEXT
            ),
            buildParameters(
                "short max + 1",
                "" + (Short.MAX_VALUE + 1),
                Short.MAX_VALUE + 1,
                Integer.class,
                NumberReader.DEFAULT_MATH_CONTEXT
            ),
            buildParameters(
                "int max - 1",
                "" + (Integer.MAX_VALUE - 1),
                Integer.MAX_VALUE - 1,
                Integer.class,
                NumberReader.DEFAULT_MATH_CONTEXT
            ),
            buildParameters(
                "int max",
                "" + Integer.MAX_VALUE,
                Integer.MAX_VALUE,
                Integer.class,
                NumberReader.DEFAULT_MATH_CONTEXT
            ),
            buildParameters(
                "int max + 1",
                "" + ((long) Integer.MAX_VALUE + 1),
                (long) Integer.MAX_VALUE + 1,
                Long.class,
                NumberReader.DEFAULT_MATH_CONTEXT
            ),
            buildParameters(
                "long max - 1",
                "" + (Long.MAX_VALUE - 1),
                Long.MAX_VALUE - 1,
                Long.class,
                HUGE_PRECISION_MATH_CONTEXT
            ),
            buildParameters(
                "long max",
                "" + Long.MAX_VALUE,
                Long.MAX_VALUE,
                Long.class,
                HUGE_PRECISION_MATH_CONTEXT
            ),
            buildParameters(
                "long max + 1",
                new BigDecimal(BigInteger.valueOf(Long.MAX_VALUE))
                    .add(BigDecimal.ONE, HUGE_PRECISION_MATH_CONTEXT).toString(),
                new BigDecimal(BigInteger.valueOf(Long.MAX_VALUE))
                    .add(BigDecimal.ONE, HUGE_PRECISION_MATH_CONTEXT).doubleValue(),
                Double.class,
                HUGE_PRECISION_MATH_CONTEXT
            ),
            buildParameters(
                "double min - 1",
                new BigDecimal(Double.MIN_VALUE).subtract(BigDecimal.ONE, HUGE_PRECISION_MATH_CONTEXT).toString(),
                new BigDecimal(Double.MIN_VALUE).subtract(BigDecimal.ONE, HUGE_PRECISION_MATH_CONTEXT),
                BigDecimal.class,
                HUGE_PRECISION_MATH_CONTEXT
            ),
            buildParameters(
                "double min",
                new BigDecimal(Double.MIN_VALUE).toString(),
                Double.MIN_VALUE,
                Double.class,
                HUGE_PRECISION_MATH_CONTEXT
            ),
            buildParameters(
                "double min + 1",
                "" + (Double.MIN_VALUE + 1),
                Double.MIN_VALUE + 1,
                Double.class,
                NumberReader.DEFAULT_MATH_CONTEXT
            ),
            buildParameters(
                "double max - 1",
                "" + (Double.MAX_VALUE - 1),
                Double.MAX_VALUE - 1,
                Double.class,
                HUGE_PRECISION_MATH_CONTEXT
            ),
            buildParameters(
                "double max",
                new BigDecimal(Double.MAX_VALUE).toString(),
                Double.MAX_VALUE,
                Double.class,
                HUGE_PRECISION_MATH_CONTEXT
            ),
            buildParameters(
                "double max + 1",
                new BigDecimal(Double.MAX_VALUE).add(BigDecimal.ONE, HUGE_PRECISION_MATH_CONTEXT).toString(),
                new BigDecimal(Double.MAX_VALUE).add(BigDecimal.ONE, HUGE_PRECISION_MATH_CONTEXT),
                BigDecimal.class,
                HUGE_PRECISION_MATH_CONTEXT
            ),
            buildParameters(
                "simple standard notation with decimal padded",
                "1.0000000",
                1d,
                Double.class,
                NumberReader.DEFAULT_MATH_CONTEXT
            ),
            buildParameters(
                "simple standard notation with plus prefix",
                "+0001.000000",
                1d,
                Double.class,
                NumberReader.DEFAULT_MATH_CONTEXT
            ).readerException(new InvalidTokenException(new PresetIterableCharSequence())),
            buildParameters(
                "lots of decimals",
                "1.000000123",
                1.000000123d,
                Double.class,
                NumberReader.DEFAULT_MATH_CONTEXT
            ),
            buildParameters(
                "1.0 padded, negative",
                "-1.00000000",
                -1d,
                Double.class,
                NumberReader.DEFAULT_MATH_CONTEXT
            ),
            buildParameters(
                "1.0 unpadded, negative",
                "-1.0",
                -1d,
                Double.class,
                NumberReader.DEFAULT_MATH_CONTEXT
            ),
            buildParameters(
                "1.00001 negative",
                "-1.00001",
                new BigDecimal("-1.00001"),
                BigDecimal.class,
                NumberReader.DEFAULT_MATH_CONTEXT
            ),
            buildParameters(
                "1.00001 positive",
                "1.00001",
                1.00001d,
                Double.class,
                NumberReader.DEFAULT_MATH_CONTEXT
            ),
            buildParameters(
                "1.0 padded, no sign",
                "00000001.00000000",
                1d,
                Double.class,
                NumberReader.DEFAULT_MATH_CONTEXT
            ),
            buildParameters(
                "1.0 padded, positive sign",
                "+00000001.00000000",
                1d,
                Double.class,
                NumberReader.DEFAULT_MATH_CONTEXT
            ).readerException(new InvalidTokenException(new PresetIterableCharSequence())),
            buildParameters(
                "1.0 unpadded, no sign",
                "1.0",
                1d,
                Double.class,
                NumberReader.DEFAULT_MATH_CONTEXT
            ),
            buildParameters(
                "1.0 unpadded, positive sign",
                "+1.0",
                1d,
                Double.class,
                NumberReader.DEFAULT_MATH_CONTEXT
            ).readerException(new InvalidTokenException(new PresetIterableCharSequence())),
            buildParameters(
                "simple standard notation with no decimal, negative",
                "-1",
                (short) -1,
                Short.class,
                NumberReader.DEFAULT_MATH_CONTEXT
            ),
            buildParameters(
                "simple standard notation with no decimal, positive",
                "1",
                (short) 1,
                Short.class,
                NumberReader.DEFAULT_MATH_CONTEXT
            ),
            buildParameters(
                "simple scientific notation lowercase no sign",
                "1.0e2",
                100d,
                Double.class,
                NumberReader.DEFAULT_MATH_CONTEXT
            ),
            buildParameters(
                "simple scientific notation uppercase no sign",
                "1.0E2",
                100d,
                Double.class,
                NumberReader.DEFAULT_MATH_CONTEXT
            ),
            buildParameters(
                "simple scientific notation positive sign",
                "1.0e+2",
                100d,
                Double.class,
                NumberReader.DEFAULT_MATH_CONTEXT
            ),
            buildParameters(
                "simple scientific notation negative sign",
                "1.0e-2",
                0.01d,
                Double.class,
                NumberReader.DEFAULT_MATH_CONTEXT
            ),
            buildParameters(
                "simple scientific notation no decimal negative sign",
                "1e-2",
                0.01d,
                Double.class,
                NumberReader.DEFAULT_MATH_CONTEXT
            ),
            buildParameters(
                "simple scientific notation no decimal no sign",
                "1e2",
                (short) 100,
                Short.class,
                NumberReader.DEFAULT_MATH_CONTEXT
            ),
            buildParameters(
                "simple scientific notation 2 digits no decimal no sign",
                "22e1",
                (short) 220,
                Short.class,
                NumberReader.DEFAULT_MATH_CONTEXT
            ),
            buildParameters(
                "0.0",
                "0.0",
                0.0d,
                Double.class,
                NumberReader.DEFAULT_MATH_CONTEXT
            ),
            buildParameters(
                "enormous number",
                new BigDecimal(1234567890).pow(1234, HUGE_PRECISION_MATH_CONTEXT).toString(),
                new BigDecimal(1234567890).pow(1234, HUGE_PRECISION_MATH_CONTEXT),
                BigDecimal.class,
                HUGE_PRECISION_MATH_CONTEXT
            ),
            buildParameters(
                "enormous number (negative)",
                new BigDecimal(-1234567890).pow(1234, HUGE_PRECISION_MATH_CONTEXT).toString(),
                new BigDecimal(-1234567890).pow(1234, HUGE_PRECISION_MATH_CONTEXT),
                BigDecimal.class,
                HUGE_PRECISION_MATH_CONTEXT
            ),
            buildParameters(
                "decimal in exponent",
                "1e2.0",
                null,
                null,
                NumberReader.DEFAULT_MATH_CONTEXT
            ).readerException(new MalformedNumberException(new PresetIterableCharSequence(5)))
                .parserException(new MalformedNumberException(new PresetIterableCharSequence())),
            buildParameters(
                "overflow",
                new BigDecimal(Double.MAX_VALUE).add(BigDecimal.ONE, HUGE_PRECISION_MATH_CONTEXT).toString(),
                null,
                null,
                NumberReader.DEFAULT_MATH_CONTEXT
            ).readerException(new MalformedNumberException(new PresetIterableCharSequence(309)))
                .parserException(new ArithmeticException("Rounding necessary")),
            buildParameters(
                "multiple decimals",
                "1.2.3",
                null,
                null,
                NumberReader.DEFAULT_MATH_CONTEXT
            ).readerException(new MalformedNumberException(new PresetIterableCharSequence(3)))
                .parserException(new MalformedNumberException(new PresetIterableCharSequence()))
        };
    }

    private static Parameters buildParameters(final String parTestName, final String parInputNumber,
                                              final Number parExpectedNumber, final Class parExpectedClass,
                                              final MathContext parMathContext) {
        return new Parameters(
            parTestName,
            newSecureCharBuffer(parInputNumber),
            parExpectedNumber,
            parExpectedClass,
            parMathContext
        );
    }

    private static String concat(final String parDelimiter, final String[] parInput) {
        final StringBuilder myStringBuilder = new StringBuilder();
        for (int myIndex = 0; myIndex < parInput.length - 1; myIndex++) {
            myStringBuilder.append(parInput[myIndex]).append(parDelimiter);
        }
        if (parInput.length > 0) {
            myStringBuilder.append(parInput[parInput.length - 1]);
        }

        return myStringBuilder.toString();
    }

    private static CharSequence newSecureCharBuffer(final CharSequence parInput) {
        if (parInput == null || parInput.length() == 0) {
            return null;
        }

        final SecureCharBuffer mySecureBuffer = SecureCharBuffer.withCapacity(parInput.length());
        mySecureBuffer.append(parInput);

        return mySecureBuffer;
    }

    static class Parameters {
        final String testName;
        final CharSequence number;
        final Number expected;
        final Class expectedNumberClass;
        final MathContext mathContext;
        Exception expectedExceptionForReader;
        Exception expectedExceptionForParser;

        Parameters(final String parTestName, final CharSequence parNumber, final Number parExpected,
                   final Class parExpectedNumberClass, final MathContext parMathContext) {
            this.testName = parTestName;
            this.number = parNumber;
            this.expected = parExpected;
            this.expectedNumberClass = parExpectedNumberClass;
            this.mathContext = parMathContext;
        }

        Parameters exception(final Exception parException) {
            parserException(parException);
            readerException(parException);
            return this;
        }

        Parameters parserException(final Exception parException) {
            this.expectedExceptionForParser = parException;
            return this;
        }

        Parameters readerException(final Exception parException) {
            this.expectedExceptionForReader = parException;
            return this;
        }

        @Override
        public String toString() {
            return testName;
        }
    }
}
