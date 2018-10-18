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
        } catch (final NumberFormatException myException) {
            throw new MalformedNumberException(parIterator);
        }
    }

    @Override
    public void addValue(final ICharacterIterator parIterator, final Object parCollection, final Object parItem) {
    }

    Number charSequenceToNumber(final CharSequence parNumber, final boolean parHasDecimal,
                                final char parExponentSign, final ICharacterIterator parIterator)
            throws IOException {
        final BigDecimal myDecimal;
        try {
            myDecimal = charSequenceToBigDecimal(parNumber, parIterator);
        } catch (final ArithmeticException myException) {
            // number is probably too big. We can still handle it, but our algorithm is very expensive and
            // a CharSequence may be okay.
            return new HugeDecimal(parNumber);
        }

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
        } catch (final ArithmeticException | NumberFormatException myException) {
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
        try {
            insertNumeric(myBuffer, parNumber, parIterator);
            return new BigDecimal(myBuffer, mathContext);
        } finally {
            // clear contents
            Arrays.fill(myBuffer, ' ');
        }
    }

    private void insertNumeric(final char[] parDestination, final CharSequence parSource,
                               final ICharacterIterator parIterator) throws IOException {
        int myDecimalPos = -1;
        int myExponentPos = -1;
        int myLength = parSource.length();

        // need to clean up this dumpster fire later.
        // need to move most of this logic into read so that the offset is correct

        // -01
        if (myLength > 2 && (parSource.charAt(0) == '-' && parSource.charAt(1) == '0' && parSource.charAt(2) != '.')) {
            Arrays.fill(parDestination, ' ');
            throw new MalformedNumberException(parIterator);
        }
        // 01.
        if (myLength > 2 && (parSource.charAt(0) == '0' && parSource.charAt(1) != '.' && parSource.charAt(1) != 'e'
                && parSource.charAt(1) != '0')) {
            Arrays.fill(parDestination, ' ');
            throw new MalformedNumberException(parIterator);
        }

        for (int myIndex = 0; myIndex < myLength; myIndex++) {
            final char myChar = parSource.charAt(myIndex);
            // java 10 correctly identifies this as invalid, but previous versions do not.
            if (myChar == JSONSymbolCollection.Token.DECIMAL.getShortSymbol()) {
                if (myIndex == 0 || myDecimalPos != -1 || myExponentPos != -1 || myIndex == myLength - 1
                        || (myIndex == 1 && parDestination[0] == '-')) {
                    Arrays.fill(parDestination, ' ');
                    throw new MalformedNumberException(parIterator);
                }
                myDecimalPos = myIndex;
            } else if (Character.toLowerCase(myChar) == JSONSymbolCollection.Token.EXPONENT.getShortSymbol()) {
                if (myIndex == myDecimalPos + 1) {
                    Arrays.fill(parDestination, ' ');
                    throw new MalformedNumberException(parIterator);
                }
                myExponentPos = myIndex;
            }
            parDestination[myIndex] = myChar;
        }
    }
}
