package com.chelseaurquhart.securejson;

import com.chelseaurquhart.securejson.JSONDecodeException.MalformedNumberException;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Arrays;

class NumberReader implements IReader<Number> {
    static final MathContext DEFAULT_MATH_CONTEXT = new MathContext(MathContext.DECIMAL64.getPrecision(),
        RoundingMode.UNNECESSARY);

    private static final BigDecimal MIN_VALUE = new BigDecimal(Double.MIN_VALUE);
    private static final BigDecimal MAX_VALUE = new BigDecimal(Double.MAX_VALUE);
    private final MathContext mathContext;

    NumberReader() {
        this(DEFAULT_MATH_CONTEXT);
    }

    NumberReader(final MathContext parMathContext) {
        this.mathContext = parMathContext;
    }

    @Override
    public boolean isStart(final IterableCharSequence parIterator) {
        final char myChar = parIterator.peek();
        return myChar == JSONSymbolCollection.Token.MINUS.getShortSymbol() || Character.isDigit(myChar);
    }

    @Override
    public Number read(final IterableCharSequence parIterator) throws IOException {
        final int myIteratorLength = parIterator.length();

        int myDecimalPosition = 0;
        int myExponentPosition = myIteratorLength + 1;
        char myExponentSign = JSONSymbolCollection.Token.PLUS.getShortSymbol();

        final ManagedSecureCharBuffer mySecureBuffer = new ManagedSecureCharBuffer();

        if (parIterator.peek() == JSONSymbolCollection.Token.MINUS.getShortSymbol()) {
            mySecureBuffer.append(parIterator.next());
        }

        while (parIterator.hasNext()) {
            final char myNextChar = parIterator.peek();
            final int myIteratorOffset = parIterator.getOffset();
            if (myNextChar == JSONSymbolCollection.Token.DECIMAL.getShortSymbol()) {
                if (myDecimalPosition != 0) {
                    throw new MalformedNumberException(parIterator);
                }
                myDecimalPosition = myIteratorOffset;
            } else if (Character.toLowerCase(myNextChar) == JSONSymbolCollection.Token.EXPONENT.getShortSymbol()) {
                if (myExponentPosition != myIteratorLength + 1) {
                    throw new MalformedNumberException(parIterator);
                }
                myExponentPosition = myIteratorOffset;
                final char myFirstExpDigit = parIterator.charAt(myExponentPosition + 1);
                if (myFirstExpDigit == JSONSymbolCollection.Token.PLUS.getShortSymbol()
                        || myFirstExpDigit == JSONSymbolCollection.Token.MINUS.getShortSymbol()) {
                    myExponentSign = myFirstExpDigit;
                    myExponentPosition++;
                    mySecureBuffer.append(parIterator.next());
                } else if (!Character.isDigit(myFirstExpDigit)) {
                    throw new MalformedNumberException(parIterator);
                }
            } else if (JSONSymbolCollection.WHITESPACES.containsKey(myNextChar)
                    || JSONSymbolCollection.END_TOKENS.containsKey(myNextChar)) {
                break;
            } else if (!Character.isDigit(myNextChar)) {
                throw new MalformedNumberException(parIterator);
            }

            mySecureBuffer.append(parIterator.next());
        }

        try {
            return charSequenceToNumber(mySecureBuffer, myDecimalPosition > 0, myExponentSign);
        } catch (ArithmeticException | NumberFormatException e) {
            throw new MalformedNumberException(parIterator);
        }
    }

    Number charSequenceToNumber(final CharSequence parNumber, final boolean parHasDecimal,
                                        final char parExponentSign) {
        final BigDecimal myDecimal = charSequenceToBigDecimal(parNumber);

        if (!parHasDecimal && parExponentSign == JSONSymbolCollection.Token.PLUS.getShortSymbol()) {
            // return the smallest representation we can.
            // if not decimal or negative exponent, always return a double.
            try {
                return myDecimal.shortValueExact();
            } catch (ArithmeticException e) {
            }
            try {
                return myDecimal.intValueExact();
            } catch (ArithmeticException e) {
            }
            try {
                return myDecimal.longValueExact();
            } catch (ArithmeticException e) {
            }
        }

        try {
            final double myDoubleValue = myDecimal.doubleValue();
            if ((myDecimal.compareTo(MIN_VALUE) < 0 || myDecimal.compareTo(MAX_VALUE) > 0)
                && !new BigDecimal(myDoubleValue)
                    .setScale(myDecimal.scale(), RoundingMode.UNNECESSARY).equals(myDecimal)) {
                return myDecimal;
            }

            return myDoubleValue;
        } catch (ArithmeticException | NumberFormatException e) {
            return myDecimal;
        }
    }

    private BigDecimal charSequenceToBigDecimal(final CharSequence parNumber) {
        final char[] myBuffer = new char[parNumber.length()];
        insert(myBuffer, parNumber);
        final BigDecimal myResult = new BigDecimal(myBuffer, mathContext);

        // clear contents
        Arrays.fill(myBuffer, ' ');

        return myResult;
    }

    private void insert(final char[] parDestination, final CharSequence parSource) {
        for (int myIndex = 0; myIndex < parSource.length(); myIndex++) {
            parDestination[myIndex] = parSource.charAt(myIndex);
        }
    }
}
