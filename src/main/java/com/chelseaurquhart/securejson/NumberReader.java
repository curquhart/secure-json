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

import com.chelseaurquhart.securejson.JSONDecodeException.MalformedNumberException;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.Map;

/**
 * @exclude
 */
class NumberReader extends ManagedSecureBufferList implements IReader<Number> {
    static final MathContext DEFAULT_MATH_CONTEXT = new MathContext(MathContext.DECIMAL64.getPrecision(),
        RoundingMode.UNNECESSARY);

    // min and max values that we will allow to convert to double. Outside of this range, we'll use a bigger type.
    private static final BigDecimal MIN_VALUE = new BigDecimal("-1.0e300", MathContext.UNLIMITED);
    private static final BigDecimal MAX_VALUE = new BigDecimal("1.0e300", MathContext.UNLIMITED);

    private final MathContext mathContext;
    private final Settings settings;

    NumberReader(final Settings parSettings) {
        this(DEFAULT_MATH_CONTEXT, parSettings);
    }

    NumberReader(final MathContext parMathContext, final Settings parSettings) {
        this.mathContext = parMathContext;
        settings = parSettings;
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
        final ManagedSecureCharBuffer mySecureBuffer = new ManagedSecureCharBuffer(settings);
        addSecureBuffer(mySecureBuffer);

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
        // only for collections
        throw new NotImplementedException(Messages.Key.ERROR_NOT_IMPLEMENTED, "addValue");
    }

    Number charSequenceToNumber(final CharSequence parNumber, final int parOffset)
            throws IOException {
        final Map.Entry<BigDecimal, Boolean> myDecimalAndForceDouble;
        try {
            myDecimalAndForceDouble = charSequenceToBigDecimal(parNumber, parOffset);
        } catch (final NumberFormatException | ArithmeticException myException) {
            // number is probably too big. We can still handle it, but our algorithm is very expensive and
            // a CharSequence may be okay so we want to lazy convert it, if requested.
            return new HugeDecimal(parNumber, this);
        }

        final BigDecimal myDecimal = myDecimalAndForceDouble.getKey();
        final boolean myForceDouble = myDecimalAndForceDouble.getValue();

        // return the smallest representation we can.
        // if not decimal or negative exponent, always return a double.
        if (!myForceDouble) {
            try {
                return myDecimal.shortValueExact();
            } catch (ArithmeticException e) {
                // Allowed empty block: trying multiple conversions.
            }
            try {
                return myDecimal.intValueExact();
            } catch (ArithmeticException e) {
                // Allowed empty block: trying multiple conversions.
            }
            try {
                return myDecimal.longValueExact();
            } catch (ArithmeticException e) {
                // Allowed empty block: trying multiple conversions.
            }
        }

        try {
            final double myDoubleValue = myDecimal.doubleValue();
            if (!verifyNumber(myDoubleValue, myDecimal)) {
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

    @Override
    public boolean isContainerType() {
        return false;
    }

    Map.Entry<BigDecimal, Boolean> charSequenceToBigDecimal(final CharSequence parSource, final int parOffset)
            throws IOException {
        final char[] myBuffer = new char[parSource.length()];
        try {
            return charSequenceToBigDecimal(parSource, parOffset, myBuffer);
        } finally {
            Arrays.fill(myBuffer, '\u0000');
        }
    }

    // allow new BigDecimal as we only use it for verification.
    private boolean verifyNumber(final double parDoubleValue, final BigDecimal parDecimalValue) {
        if (Double.isInfinite(parDoubleValue) || parDecimalValue.compareTo(MIN_VALUE) <= 0
                || parDecimalValue.compareTo(MAX_VALUE) >= 0 || parDecimalValue.scale() > Double.MAX_EXPONENT
                || parDecimalValue.scale() < Double.MIN_EXPONENT) {
            return false;
        }

        return true;
    }

    private Map.Entry<BigDecimal, Boolean> charSequenceToBigDecimal(final CharSequence parSource, final int parOffset,
                                                                    final char[] parBuffer)
            throws IOException {
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
            parBuffer[myIndex + myLengthOffset] = myChar;
            final char myNextChar;
            if (myLength > myIndex + 1) {
                myNextChar = parSource.charAt(myIndex + 1);
            } else {
                myNextChar = 0;
            }

            switch (myToken) {
                case ZERO:
                    if (myLength != 1 && !myFoundNonZeroDigit && !myFoundDecimal) {
                        if (myLength <= myIndex + 1) {
                            if (myLastChar != JSONSymbolCollection.Token.MINUS.getShortSymbol()) {
                                throw buildException(parSource, myIndex + parOffset);
                            }
                        } else if (myNextChar != JSONSymbolCollection.Token.DECIMAL.getShortSymbol()
                                && myNextChar != JSONSymbolCollection.Token.EXPONENT.getShortSymbol()) {
                            throw buildException(parSource, myIndex + parOffset + 1);
                        }
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
                            || myIndex == myLength - 1 || myIndex == 0) {
                        throw buildException(parSource, myIndex + parOffset);
                    }
                    myFoundExponent = true;
                    if (myNextChar == JSONSymbolCollection.Token.MINUS.getShortSymbol()) {
                        myForceDouble = true;
                        parBuffer[++myIndex + myLengthOffset] = JSONSymbolCollection.Token.MINUS.getShortSymbol();
                        if (myIndex == myLength - 1) {
                            throw buildException(parSource, myIndex + parOffset);
                        }
                    } else if (myNextChar == JSONSymbolCollection.Token.PLUS.getShortSymbol()) {
                        myIndex++;
                        if (myIndex == myLength - 1) {
                            throw buildException(parSource, myIndex + parOffset);
                        }
                        myLengthOffset--;
                    }
                    break;
                case MINUS:
                    if (myIndex == myLength - 1) {
                        throw buildException(parSource, myIndex + parOffset);
                    }

                    if (myNextChar == JSONSymbolCollection.Token.DECIMAL.getShortSymbol()
                            || myNextChar == JSONSymbolCollection.Token.EXPONENT.getShortSymbol()) {
                        throw buildException(parSource, myIndex + parOffset + 1);
                    } else if (myIndex == 0) {
                        // 0 is ok, anything else is not.
                        break;
                    }
                default:
                    throw buildException(parSource, myIndex + parOffset);
            }

            myLastChar = myChar;
        }

        return new AbstractMap.SimpleImmutableEntry<>(
            new BigDecimal(parBuffer, 0, myLength + myLengthOffset, mathContext), myForceDouble);
    }

    private MalformedNumberException buildException(final CharSequence parSource, final int parOffset)
            throws IOException {
        return new MalformedNumberException(new IterableCharSequence(parSource, parOffset));
    }
}
