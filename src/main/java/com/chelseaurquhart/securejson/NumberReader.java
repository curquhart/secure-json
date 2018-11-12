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

    private final transient MathContext mathContext;
    private final transient Settings settings;

    NumberReader(final Settings parSettings) {
        this(DEFAULT_MATH_CONTEXT, parSettings);
    }

    NumberReader(final MathContext parMathContext, final Settings parSettings) {
        super();
        this.mathContext = parMathContext;
        settings = parSettings;
    }

    @Override
    public boolean isStart(final ICharacterIterator parIterator) throws IOException, JSONException {
        return JSONSymbolCollection.NUMBERS.containsKey(parIterator.peek());
    }

    @Override
    public SymbolType getSymbolType(final ICharacterIterator parIterator) {
        return SymbolType.UNKNOWN;
    }

    @Override
    public Number read(final ICharacterIterator parIterator) throws IOException, JSONException {
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
            throws IOException, JSONException {
        final Map.Entry<BigDecimal, Boolean> myDecimalAndForceDouble;
        try {
            myDecimalAndForceDouble = charSequenceToBigDecimal(parNumber, parOffset);
        } catch (final NumberFormatException myException) {
            // number is probably too big. We can still handle it, but our algorithm is very expensive and
            // a CharSequence may be okay so we want to lazy convert it, if requested.
            return new HugeDecimal(parNumber, this);
        } catch (final ArithmeticException myException) {
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
                return myDecimal.intValueExact();
            } catch (final ArithmeticException myException) {
                // Allowed empty block: trying multiple conversions.
            }
            try {
                return myDecimal.longValueExact();
            } catch (final ArithmeticException myException) {
                // Allowed empty block: trying multiple conversions.
            }
        }

        try {
            final double myDoubleValue = myDecimal.doubleValue();
            if (!verifyNumber(myDoubleValue, myDecimal)) {
                return myDecimal;
            }

            return myDoubleValue;
        } catch (final ArithmeticException myException) {
            return myDecimal;
        } catch (final NumberFormatException myException) {
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
            throws IOException, JSONException {
        final char[] myBuffer = new char[parSource.length()];
        try {
            return charSequenceToBigDecimal(parSource, parOffset, myBuffer);
        } finally {
            Arrays.fill(myBuffer, '\u0000');
        }
    }

    // allow new BigDecimal as we only use it for verification.
    private boolean verifyNumber(final double parDoubleValue, final BigDecimal parDecimalValue) {
        return !(Double.isInfinite(parDoubleValue) || parDecimalValue.compareTo(MIN_VALUE) <= 0
            || parDecimalValue.compareTo(MAX_VALUE) >= 0 || parDecimalValue.scale() > Double.MAX_EXPONENT
            || parDecimalValue.scale() < Double.MIN_EXPONENT);
    }

    private Map.Entry<BigDecimal, Boolean> charSequenceToBigDecimal(final CharSequence parSource, final int parOffset,
                                                                    final char[] parBuffer)
            throws IOException, JSONException {
        boolean myFoundExponent = false;
        boolean myFoundDecimal = false;
        boolean myFoundNonZeroDigit = false;

        char myLastChar = 0;
        final int myLength = parSource.length();
        final NumberData myData = new NumberData();

        while (myData.index < myLength) {
            final char myChar = parSource.charAt(myData.index);
            final JSONSymbolCollection.Token myToken;
            try {
                myToken = JSONSymbolCollection.Token.forSymbol(myChar);
            } catch (final IllegalArgumentException myException) {
                throw buildException(parSource, myData.index + parOffset);
            }
            parBuffer[myData.index + myData.lengthOffset] = myChar;
            final char myNextChar;
            if (myLength > myData.index + 1) {
                myNextChar = parSource.charAt(myData.index + 1);
            } else {
                myNextChar = 0;
            }
            final JSONSymbolCollection.Token myNextToken = JSONSymbolCollection.Token.forSymbolOrDefault(
                myNextChar, JSONSymbolCollection.Token.UNKNOWN);
            final JSONSymbolCollection.Token myLastToken = JSONSymbolCollection.Token.forSymbolOrDefault(
                myLastChar, JSONSymbolCollection.Token.UNKNOWN);

            switch (myToken) {
                case ZERO:
                    validateZeroPosition(myLength, myData.index, myLastToken, myNextToken, myFoundNonZeroDigit,
                        myFoundDecimal, parSource, parOffset);
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
                    validateDecimalPositionWithData(myFoundDecimal, myFoundExponent, myData, myLength, parSource,
                            parOffset);
                    myFoundDecimal = true;
                    break;
                case EXPONENT:
                    validateExponentPositionWithData(myFoundExponent, myLastToken, myNextToken, myNextChar, myLength,
                        parBuffer, myData, parOffset, parSource);
                    myFoundExponent = true;
                    break;
                case MINUS:
                    validateMinusPosition(myData.index, myLength, myNextToken, parOffset, parSource);
                    break;
                default:
                    throw buildException(parSource, myData.index + parOffset);
            }

            myLastChar = myChar;
            myData.index++;
        }

        return new AbstractMap.SimpleImmutableEntry<BigDecimal, Boolean>(
            new BigDecimal(parBuffer, 0, myLength + myData.lengthOffset, mathContext), myData.forceDouble);
    }

    private void validateMinusPosition(final int parIndex, final int parLength,
                                       final JSONSymbolCollection.Token parNextToken, final int parOffset,
                                       final CharSequence parSource) throws IOException, MalformedNumberException {
        if (parIndex == parLength - 1) {
            throw buildException(parSource, parIndex + parOffset);
        }

        if (parNextToken == JSONSymbolCollection.Token.DECIMAL
                || parNextToken == JSONSymbolCollection.Token.EXPONENT) {
            throw buildException(parSource, parIndex + parOffset + 1);
        } else if (parIndex != 0) {
            throw buildException(parSource, parIndex + parOffset);
        }
    }

    private void validateExponentPositionWithData(final boolean parFoundExponent,
                                                  final JSONSymbolCollection.Token parLastToken,
                                                  final JSONSymbolCollection.Token parNextToken, final char parNextChar,
                                                  final int parLength, final char[] parBuffer, final NumberData parData,
                                                  final int parOffset, final CharSequence parSource)
            throws IOException, MalformedNumberException {
        if (parFoundExponent || parLastToken == JSONSymbolCollection.Token.DECIMAL || parData.index == parLength - 1
                || parData.index == 0) {
            throw buildException(parSource, parData.index + parOffset);
        }

        if (parNextToken == JSONSymbolCollection.Token.MINUS) {
            parData.forceDouble = true;
            parBuffer[++parData.index + parData.lengthOffset] = parNextChar;
            if (parData.index == parLength - 1) {
                throw buildException(parSource, parData.index + parOffset);
            }
        } else if (parNextToken == JSONSymbolCollection.Token.PLUS) {
            parData.index++;
            if (parData.index == parLength - 1) {
                throw buildException(parSource, parData.index + parOffset);
            }
            parData.lengthOffset--;
        }
    }

    private void validateDecimalPositionWithData(final boolean parFoundDecimal, final boolean parFoundExponent,
                                                 final NumberData parData, final int parLength,
                                                 final CharSequence parSource, final int parOffset) throws IOException,
            MalformedNumberException {
        if (parFoundDecimal || parFoundExponent || parData.index == 0 || parData.index == parLength - 1) {
            throw buildException(parSource, parData.index + parOffset);
        }
        parData.forceDouble = true;
    }

    private void validateZeroPosition(final int parLength, final int parIndex,
                                      final JSONSymbolCollection.Token parLastToken,
                                      final JSONSymbolCollection.Token parNextToken,
                                      final boolean parFoundNonZeroDigit, final boolean parFoundDecimal,
                                      final CharSequence parSource, final int parOffset) throws IOException,
            MalformedNumberException {
        if (parLength != 1 && !parFoundNonZeroDigit && !parFoundDecimal) {
            if (parLength <= parIndex + 1) {
                if (parLastToken != JSONSymbolCollection.Token.MINUS) {
                    throw buildException(parSource, parIndex + parOffset);
                }
            } else if (parNextToken != JSONSymbolCollection.Token.DECIMAL
                    && parNextToken != JSONSymbolCollection.Token.EXPONENT) {
                throw buildException(parSource, parIndex + parOffset + 1);
            }
        }
    }

    private MalformedNumberException buildException(final CharSequence parSource, final int parOffset)
            throws IOException {
        return new MalformedNumberException(new IterableCharSequence(parSource, parOffset));
    }

    /**
     * Mutable data container for number reads.
     */
    private class NumberData {
        private boolean forceDouble;
        private int lengthOffset;
        private int index;
    }
}
