package com.chelseaurquhart.securejson;

import com.chelseaurquhart.securejson.JSONDecodeException.MalformedNumberException;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.Map;

class NumberReader implements IReader {
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
    public boolean isStart(final ICharacterIterator parIterator) throws IOException {
        return JSONSymbolCollection.NUMBERS.containsKey(parIterator.peek());
    }

    @Override
    public SymbolType getSymbolType(final ICharacterIterator parIterator) {
        return SymbolType.UNKNOWN;
    }

    @Override
    public Number read(final ICharacterIterator parIterator) throws IOException {
        int myDecimalPosition = 0;
        int myExponentPosition = 0;
        char myExponentSign = JSONSymbolCollection.Token.PLUS.getShortSymbol();

        final ManagedSecureCharBuffer mySecureBuffer = new ManagedSecureCharBuffer();

        final int myOffset = parIterator.getOffset();
        while (parIterator.hasNext()) {
            final char myNextChar = parIterator.peek();

            if (JSONSymbolCollection.WHITESPACES.containsKey(myNextChar)
                    || JSONSymbolCollection.END_TOKENS.containsKey(myNextChar)) {
                break;
            } else {
                mySecureBuffer.append(parIterator.next());
            }
        }

        try {
            return charSequenceToNumber(mySecureBuffer, myOffset);
        } catch (final ArithmeticException myException) {
            throw new MalformedNumberException(parIterator);
        }
    }

    @Override
    public void addValue(final ICharacterIterator parIterator, final Object parCollection, final Object parItem) {
    }

    Number charSequenceToNumber(final CharSequence parNumber, final int parOffset)
            throws IOException {
        final Map.Entry<BigDecimal, Boolean> myDecimalAndForceDouble;
        try {
            myDecimalAndForceDouble = charSequenceToBigDecimal(parNumber, parOffset);
        } catch (final NumberFormatException | ArithmeticException myException) {
            // number is probably too big. We can still handle it, but our algorithm is very expensive and
            // a CharSequence may be okay so we want to lazy convert it, if requested.
            return new HugeDecimal(parNumber);
        }

        final BigDecimal myDecimal = myDecimalAndForceDouble.getKey();
        final boolean myForceDouble = myDecimalAndForceDouble.getValue();

        // return the smallest representation we can.
        // if not decimal or negative exponent, always return a double.
        if (!myForceDouble) {
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
        } catch (final ArithmeticException | NumberFormatException myException) {
            return myDecimal;
        }
    }

    @Override
    public Object normalizeCollection(final Object parValue) {
        return parValue;
    }

    private Map.Entry<BigDecimal, Boolean> charSequenceToBigDecimal(final CharSequence parSource, final int parOffset)
            throws IOException {
        final char[] myBuffer = new char[parSource.length()];
        try {
            boolean myFoundExponent = false;
            boolean myFoundDecimal = false;
            boolean myFoundNonZeroDigit = false;
            boolean myForceDouble = false;

            char myLastChar = 0;
            final int myLength = parSource.length();
            int myLengthOffset = 0;
            for (int myIndex = 0; myIndex < myLength; myIndex++) {
                final char myChar = parSource.charAt(myIndex);
                final JSONSymbolCollection.Token myToken;
                try {
                    myToken = JSONSymbolCollection.Token.forSymbol(myChar);
                } catch (final IllegalArgumentException myException) {
                    throw buildException(parSource, myIndex + parOffset);
                }
                myBuffer[myIndex + myLengthOffset] = myChar;
                switch (myToken) {
                    case ZERO:
                        if (!myFoundNonZeroDigit && !myFoundDecimal && (myLength < myIndex + 1
                                || parSource.charAt(myIndex + 1)
                                != JSONSymbolCollection.Token.DECIMAL.getShortSymbol())) {
                            throw buildException(parSource, myIndex + parOffset);
                        }
                        break;
                    case ONE:
                    case TWO:
                    case THREE:
                    case FOUR:
                    case FIVE:
                    case SIX:
                    case SEVEN:
                    case EIGHT:
                    case NINE:
                        myFoundNonZeroDigit = true;
                        break;
                    case DECIMAL:
                        if (myFoundDecimal || myFoundExponent || myIndex == 0 || myIndex == myLength - 1) {
                            throw buildException(parSource, myIndex + parOffset);
                        }
                        myFoundDecimal = true;
                        myForceDouble = true;
                        break;
                    case EXPONENT:
                        if (myFoundExponent || myLastChar == JSONSymbolCollection.Token.DECIMAL.getShortSymbol()
                                || myIndex == myLength - 1) {
                            throw buildException(parSource, myIndex + parOffset);
                        }
                        myFoundExponent = true;
                        final char myNextChar = parSource.charAt(myIndex + 1);
                        if (myNextChar == JSONSymbolCollection.Token.MINUS.getShortSymbol()) {
                            myForceDouble = true;
                            myBuffer[++myIndex + myLengthOffset] = JSONSymbolCollection.Token.MINUS.getShortSymbol();
                        } else if (myNextChar == JSONSymbolCollection.Token.PLUS.getShortSymbol()) {
                            myIndex++;
                            myLengthOffset--;
                        }
                        break;
                    case MINUS:
                        if (myIndex == 0) {
                            // 0 is ok, anything else is not.
                            break;
                        }
                    default:
                        throw buildException(parSource, myIndex + parOffset);
                }

                myLastChar = myChar;
            }
            return new AbstractMap.SimpleImmutableEntry<>(
                new BigDecimal(myBuffer, 0, myLength + myLengthOffset, mathContext), myForceDouble);
        } finally {
            // clear contents
            Arrays.fill(myBuffer, ' ');
        }
    }

    private MalformedNumberException buildException(final CharSequence parSource, final int parOffset)
            throws IOException {
        return new MalformedNumberException(new IterableCharSequence(parSource, parOffset));
    }
}
