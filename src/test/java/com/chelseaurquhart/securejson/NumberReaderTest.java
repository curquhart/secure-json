package com.chelseaurquhart.securejson;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;
import java.math.MathContext;

public class NumberReaderTest {
    @Test(dataProviderClass = NumberProvider.class, dataProvider = NumberProvider.DATA_PROVIDER_NAME)
    public final void testRead(final NumberProvider.Parameters parParameters) {
        try {
            final Number myNumber = charSequenceToNumber(parParameters.number, parParameters.mathContext);
            Assert.assertNull(parParameters.expectedExceptionForParser);
            Assert.assertSame(myNumber.getClass(), parParameters.expectedNumberClass);
            Assert.assertEquals(myNumber, parParameters.expected);
        } catch (final Exception myException) {
            Assert.assertNotNull(parParameters.expectedExceptionForParser, myException.getMessage());
            Assert.assertEquals(myException.getMessage(), parParameters.expectedExceptionForParser.getMessage());
            Assert.assertEquals(myException.getClass(), parParameters.expectedExceptionForParser.getClass());
        }
    }

    @Test
    public final void testDefaults() throws IOException {
        Assert.assertEquals((short) 220, charSequenceToNumber("22e1", NumberReader.DEFAULT_MATH_CONTEXT));
    }

    private Number charSequenceToNumber(final CharSequence parNumber, final MathContext parMathContext)
            throws IOException {
        final boolean myHasDecimal = getIndex(parNumber, '.', 0) != -1;
        final int myExponentIndex = getIndex(parNumber, 'e', 0);
        final char myExponentSign;
        if (myExponentIndex > 0 && getIndex(parNumber, '-', myExponentIndex + 1) > -1) {
            myExponentSign = '-';
        } else {
            myExponentSign = '+';
        }
        return new NumberReader(parMathContext).charSequenceToNumber(parNumber, myHasDecimal, myExponentSign,
            new IterableCharSequence(""));
    }

    private int getIndex(final CharSequence parInput, final char parSearchFor, final int parStartIndex) {
        final char mySearchFor = Character.toLowerCase(parSearchFor);
        final int myInputLength = parInput.length();
        for (int myIndex = parStartIndex; myIndex < myInputLength; myIndex++) {
            if (Character.toLowerCase(parInput.charAt(myIndex)) == mySearchFor) {
                return myIndex;
            }
        }

        return -1;
    }
}
