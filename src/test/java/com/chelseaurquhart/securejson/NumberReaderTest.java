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
            Assert.assertNull(parParameters.expectedException);
            Assert.assertSame(myNumber.getClass(), parParameters.expectedNumberClass);
            Assert.assertEquals(myNumber, parParameters.expected);
        } catch (final IOException | JSONException.JSONRuntimeException myException) {
            Assert.assertNotNull(parParameters.expectedException, myException.getMessage());
            Assert.assertEquals(myException.getMessage(), parParameters.expectedException.getMessage());
            Assert.assertEquals(myException.getClass(), parParameters.expectedException.getClass());
        }
    }

    @Test
    public final void testDefaults() throws IOException {
        Assert.assertEquals((short) 220, charSequenceToNumber("22e1", NumberReader.DEFAULT_MATH_CONTEXT));
    }

    private Number charSequenceToNumber(final CharSequence parNumber, final MathContext parMathContext)
            throws IOException {
        return new NumberReader(parMathContext, Settings.DEFAULTS).charSequenceToNumber(parNumber, 0);
    }
}
