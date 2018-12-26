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

import com.chelseaurquhart.securejson.util.StringUtil;
import com.chelseaurquhart.securejson.JSONException.JSONRuntimeException;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;

@SuppressWarnings({"PMD.CommentRequired", "PMD.AvoidUsingShortType"})
public final class HugeDecimalTest {
    private HugeDecimalTest() {
    }

    private static final String DATA_PROVIDER_NAME = "HugeDecimalTest";

    @DataProvider(name = DATA_PROVIDER_NAME)
    Object[] dataProvider() {
        return new Object[]{
            new Parameters(
                "number that can be represented in all decimal types and simple types will convert",
                1.0
            )
                .expectedBigDecimal(BigDecimal.valueOf(1.0))
                .expectedBigInteger(BigInteger.ONE)
                .expectedDouble(1.0)
                .expectedFloat(1.0f)
                .expectedLong(1)
                .expectedInt(1)
                .expectedShort((short) 1)
                .expectedCharSequence("1.0"),
            new Parameters(
                "number that can be represented in all types",
                1
            )
                .expectedBigDecimal(BigDecimal.ONE)
                .expectedBigInteger(BigInteger.ONE)
                .expectedDouble(1.0)
                .expectedFloat(1.0f)
                .expectedLong(1)
                .expectedInt(1)
                .expectedShort((short) 1)
                .expectedCharSequence("1"),
            new Parameters(
                "negative number that can be represented in all types",
                -1
            )
                .expectedBigDecimal(BigDecimal.ONE.negate())
                .expectedBigInteger(BigInteger.ONE.negate())
                .expectedDouble(-1.0)
                .expectedFloat(-1.0f)
                .expectedLong(-1)
                .expectedInt(-1)
                .expectedShort((short) -1)
                .expectedCharSequence("-1"),
            new Parameters(
                "big integer value that can be represented in all types",
                BigInteger.valueOf(-1)
            )
                .expectedBigDecimal(BigDecimal.ONE.negate())
                .expectedBigInteger(BigInteger.ONE.negate())
                .expectedDouble(-1.0)
                .expectedFloat(-1.0f)
                .expectedLong(-1)
                .expectedInt(-1)
                .expectedShort((short) -1)
                .expectedCharSequence("-1"),
            new Parameters(
                "big decimal value that can be represented in all types",
                BigDecimal.valueOf(-1)
            )
                .expectedBigDecimal(BigDecimal.ONE.negate())
                .expectedBigInteger(BigInteger.ONE.negate())
                .expectedDouble(-1.0)
                .expectedFloat(-1.0f)
                .expectedLong(-1)
                .expectedInt(-1)
                .expectedShort((short) -1)
                .expectedCharSequence("-1"),
            new Parameters(
                "number that can be represented only in HugeDecimal",
                new HugeDecimal("1e100000000000", new NumberReader())
            )
                .expectedCharSequence("1e100000000000"),
            new Parameters(
                "error handling",
                new HugeDecimal("abc", new NumberReader())
            )
                .expectedCharSequence("abc")
            // all expected number types being null means exception expected for each.
        };
    }

    @Test(dataProvider = DATA_PROVIDER_NAME)
    public void testConvertToBigDecimal(final Parameters parParameters) {
        testConvert(parParameters, new IConsumer<HugeDecimal>() {
            @Override
            public void accept(final HugeDecimal parInput) {
                try {
                    Assert.assertEquals(parInput.bigDecimalValue(), parParameters.expectedBigDecimal);
                } catch (final NumberFormatException myException) {
                    Assert.assertNull(parParameters.expectedBigDecimal);
                } catch (final ArithmeticException myException) {
                    Assert.assertNull(parParameters.expectedBigDecimal);
                } catch (final JSONRuntimeException myException) {
                    Assert.assertNull(parParameters.expectedBigDecimal);
                } catch (final IOException myException) {
                    Assert.assertNull(parParameters.expectedBigDecimal);
                } catch (final JSONException myException) {
                    Assert.assertNull(parParameters.expectedBigDecimal);
                }
            }
        });
    }

    @Test(dataProvider = DATA_PROVIDER_NAME)
    public void testConvertToBigInteger(final Parameters parParameters) {
        testConvert(parParameters, new IConsumer<HugeDecimal>() {
            @Override
            public void accept(final HugeDecimal parInput) {
                try {
                    Assert.assertEquals(parInput.bigIntegerValue(), parParameters.expectedBigInteger);
                } catch (final NumberFormatException myException) {
                    Assert.assertNull(parParameters.expectedBigInteger);
                } catch (final ArithmeticException myException) {
                    Assert.assertNull(parParameters.expectedBigInteger);
                } catch (final JSONRuntimeException myException) {
                    Assert.assertNull(parParameters.expectedBigInteger);
                } catch (final IOException myException) {
                    Assert.assertNull(parParameters.expectedBigInteger);
                } catch (final JSONException myException) {
                    Assert.assertNull(parParameters.expectedBigInteger);
                }
            }
        });
    }

    @Test(dataProvider = DATA_PROVIDER_NAME)
    public void testConvertToDouble(final Parameters parParameters) {
        testConvert(parParameters, new IConsumer<HugeDecimal>() {
            @Override
            public void accept(final HugeDecimal parInput) {
                try {
                    Assert.assertEquals(parInput.doubleValue(), parParameters.expectedDouble);
                } catch (final NumberFormatException myException) {
                    Assert.assertNull(parParameters.expectedDouble);
                } catch (final ArithmeticException myException) {
                    Assert.assertNull(parParameters.expectedDouble);
                } catch (final JSONRuntimeException myException) {
                    Assert.assertNull(parParameters.expectedDouble);
                }
            }
        });
    }

    @Test(dataProvider = DATA_PROVIDER_NAME)
    public void testConvertToFloat(final Parameters parParameters) {
        testConvert(parParameters, new IConsumer<HugeDecimal>() {
            @Override
            public void accept(final HugeDecimal parInput) {
                try {
                    Assert.assertEquals(parInput.floatValue(), parParameters.expectedFloat);
                } catch (final NumberFormatException myException) {
                    Assert.assertNull(parParameters.expectedFloat);
                } catch (final ArithmeticException myException) {
                    Assert.assertNull(parParameters.expectedFloat);
                } catch (final JSONRuntimeException myException) {
                    Assert.assertNull(parParameters.expectedFloat);
                }
            }
        });
    }

    @Test(dataProvider = DATA_PROVIDER_NAME)
    public void testConvertToLong(final Parameters parParameters) {
        testConvert(parParameters, new IConsumer<HugeDecimal>() {
            @Override
            public void accept(final HugeDecimal parInput) {
                try {
                    Assert.assertEquals(Long.valueOf(parInput.longValue()), parParameters.expectedLong);
                } catch (final NumberFormatException myException) {
                    Assert.assertNull(parParameters.expectedLong);
                } catch (final ArithmeticException myException) {
                    Assert.assertNull(parParameters.expectedLong);
                } catch (final JSONRuntimeException myException) {
                    Assert.assertNull(parParameters.expectedLong);
                }
            }
        });
    }

    @Test(dataProvider = DATA_PROVIDER_NAME)
    public void testConvertToInt(final Parameters parParameters) {
        testConvert(parParameters, new IConsumer<HugeDecimal>() {
            @Override
            public void accept(final HugeDecimal parInput) {
                try {
                    Assert.assertEquals(Integer.valueOf(parInput.intValue()), parParameters.expectedInt);
                } catch (final NumberFormatException myException) {
                    Assert.assertNull(parParameters.expectedInt);
                } catch (final ArithmeticException myException) {
                    Assert.assertNull(parParameters.expectedInt);
                } catch (final JSONRuntimeException myException) {
                    Assert.assertNull(parParameters.expectedInt);
                }
            }
        });
    }

    @Test(dataProvider = DATA_PROVIDER_NAME)
    public void testConvertToShort(final Parameters parParameters) {
        testConvert(parParameters, new IConsumer<HugeDecimal>() {
            @Override
            public void accept(final HugeDecimal parInput) {
                try {
                    Assert.assertEquals(Short.valueOf(parInput.shortValue()), parParameters.expectedShort);
                } catch (final NumberFormatException myException) {
                    Assert.assertNull(parParameters.expectedShort);
                } catch (final ArithmeticException myException) {
                    Assert.assertNull(parParameters.expectedShort);
                } catch (final JSONRuntimeException myException) {
                    Assert.assertNull(parParameters.expectedShort);
                }
            }
        });
    }

    @Test(dataProvider = DATA_PROVIDER_NAME)
    public void testConvertToCharSequence(final Parameters parParameters) {
        testConvert(parParameters, new IConsumer<HugeDecimal>() {
            @Override
            public void accept(final HugeDecimal parInput) {
                Assert.assertEquals(StringUtil.charSequenceToString(parInput.charSequenceValue()),
                    parParameters.expectedCharSequence);
            }
        });
    }

    @Test
    public void testNumberSequencingAndSerializing() {
        final HugeDecimal myNumber = new HugeDecimal(123);
        Assert.assertEquals(myNumber.subSequence(0, 3), "123");
        Assert.assertEquals(myNumber.subSequence(0, 2), "12");

        final HugeDecimal mySequence = new HugeDecimal("123", new NumberReader());
        Assert.assertEquals(mySequence.subSequence(0, 3), "123");
        Assert.assertEquals(mySequence.subSequence(0, 2), "12");
    }

    @Test
    public void testCompareByBigDecimal() {
        final HugeDecimal myNumberLHS = new HugeDecimal("123.10e450", new NumberReader());
        final HugeDecimal myNumberRHS = new HugeDecimal("123.1e450", new NumberReader());
        final HugeDecimal myNumberRHSAlt = new HugeDecimal("123.11e450", new NumberReader());
        Assert.assertTrue(myNumberLHS.compareByBigDecimal(myNumberRHS));
        Assert.assertFalse(myNumberLHS.compareByBigDecimal(myNumberRHSAlt));
    }

    private void testConvert(final Parameters parParameters, final IConsumer<HugeDecimal> parConsumer) {
        // test from number
        final HugeDecimal myHugeDecimal = new HugeDecimal(parParameters.input);
        parConsumer.accept(myHugeDecimal);

        // test from string
        final CharSequence myCharSequenceValue;
        if (parParameters.input instanceof HugeDecimal) {
            myCharSequenceValue = ((HugeDecimal) parParameters.input).charSequenceValue();
        } else {
            myCharSequenceValue = parParameters.input.toString();
        }
        final HugeDecimal myHugeDecimalFromString = new HugeDecimal(myCharSequenceValue,
            new NumberReader());
        parConsumer.accept(myHugeDecimalFromString);
    }

    private static final class Parameters {
        private final String testName;
        private final Number input;

        private BigDecimal expectedBigDecimal;
        private BigInteger expectedBigInteger;
        private Double expectedDouble;
        private Float expectedFloat;
        private Long expectedLong;
        private Integer expectedInt;
        private Short expectedShort;
        private String expectedCharSequence;

        private Parameters(final String parTestName, final Number parInput) {
            testName = parTestName;
            input = parInput;
        }

        Parameters expectedBigDecimal(final BigDecimal parExpectedBigDecimal) {
            expectedBigDecimal = parExpectedBigDecimal;

            return this;
        }

        Parameters expectedBigInteger(final BigInteger parExpectedBigInteger) {
            expectedBigInteger = parExpectedBigInteger;

            return this;
        }

        Parameters expectedDouble(final double parExpectedDouble) {
            expectedDouble = parExpectedDouble;

            return this;
        }

        Parameters expectedFloat(final float parExpectedFloat) {
            expectedFloat = parExpectedFloat;

            return this;
        }

        Parameters expectedLong(final long parExpectedLong) {
            expectedLong = parExpectedLong;

            return this;
        }

        Parameters expectedInt(final int parExpectedInt) {
            expectedInt = parExpectedInt;

            return this;
        }

        Parameters expectedShort(final short parExpectedShort) {
            expectedShort = parExpectedShort;

            return this;
        }

        Parameters expectedCharSequence(final String parExpectedCharSequence) {
            expectedCharSequence = parExpectedCharSequence;

            return this;
        }

        @Override
        public String toString() {
            return testName;
        }
    }
}
