package com.chelseaurquhart.securejson;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.nio.CharBuffer;

public final class StringReaderTest {
    private StringReaderTest() {
    }

    @Test(dataProviderClass = StringProvider.class, dataProvider = StringProvider.DATA_PROVIDER_NAME)
    public void testConvert(final StringProvider.Parameters parParameters) {
        try {
            final CharSequence myResult = new StringReader(Settings.DEFAULTS)
                .read(new IterableCharSequence(parParameters.inputString));
            Assert.assertNull(parParameters.expectedException);
            // need to convert in sort of a roundabout way (or loop) as SecureCharBuffer cannot be string-converted.
            final char[] myActualChars = new char[myResult.length()];
            final char[] myExpectedChars = new char[parParameters.expected.length()];
            CharBuffer.wrap(myResult).get(myActualChars);
            CharBuffer.wrap(parParameters.expected).get(myExpectedChars);
            Assert.assertEquals(new String(myActualChars), new String(myExpectedChars));
        } catch (final Exception myException) {
            Assert.assertNotNull(parParameters.expectedException, myException.getMessage());
            Assert.assertEquals(myException.getMessage(), parParameters.expectedException.getMessage());
            Assert.assertEquals(myException.getClass(), parParameters.expectedException.getClass());
        }
    }
}
