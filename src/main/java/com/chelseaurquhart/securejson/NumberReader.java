package com.chelseaurquhart.securejson;

import com.chelseaurquhart.securejson.JSONDecodeException.MalformedNumberException;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Arrays;

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
                if (myExponentPosition != 0) {
                    throw new MalformedNumberException(parIterator);
                }
                myExponentPosition = myIteratorOffset;
                mySecureBuffer.append(parIterator.next());
                final char myFirstExpDigit = parIterator.peek();
                if (myFirstExpDigit == JSONSymbolCollection.Token.PLUS.getShortSymbol()
                        || myFirstExpDigit == JSONSymbolCollection.Token.MINUS.getShortSymbol()) {
                    myExponentSign = myFirstExpDigit;
                    myExponentPosition++;
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
            return charSequenceToNumber(mySecureBuffer, myDecimalPosition > 0, myExponentSign, parIterator);
        } catch (ArithmeticException | NumberFormatException e) {
            throw new MalformedNumberException(parIterator);
        }
    }

    @Override
    public void addValue(final ICharacterIterator parIterator, final Object parCollection, final Object parItem) {
    }

    Number charSequenceToNumber(final CharSequence parNumber, final boolean parHasDecimal,
                                final char parExponentSign, final ICharacterIterator parIterator)
            throws IOException {
        final BigDecimal myDecimal = charSequenceToBigDecimal(parNumber, parIterator);

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

    @Override
    public Object normalizeCollection(final Object parValue) {
        return parValue;
    }

    private BigDecimal charSequenceToBigDecimal(final CharSequence parNumber, final ICharacterIterator parIterator)
            throws IOException {
        final char[] myBuffer = new char[parNumber.length()];
        insertNumeric(myBuffer, parNumber, parIterator);
        final BigDecimal myResult = new BigDecimal(myBuffer, mathContext);

        // clear contents
        Arrays.fill(myBuffer, ' ');

        return myResult;
    }

    private void insertNumeric(final char[] parDestination, final CharSequence parSource,
                               final ICharacterIterator parIterator) throws IOException {
        boolean myFoundDecimal = false;
        boolean myFoundExponent = false;
        for (int myIndex = 0; myIndex < parSource.length(); myIndex++) {
            final char myChar = parSource.charAt(myIndex);
            // java 10 correctly identifies this as invalid, but previous versions do not.
            if (myChar == JSONSymbolCollection.Token.DECIMAL.getShortSymbol()) {
                if (myFoundDecimal || myFoundExponent) {
                    throw new MalformedNumberException(parIterator);
                }
                myFoundDecimal = true;
            } else if (Character.toLowerCase(myChar) == JSONSymbolCollection.Token.EXPONENT.getShortSymbol()) {
                myFoundExponent = true;
            }
            parDestination[myIndex] = myChar;
        }
    }
}
