package com.chelseaurquhart.securejson;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.CharBuffer;
import java.util.List;

public final class JSONReaderTest {
    @Test(dataProviderClass = NumberProvider.class, dataProvider = NumberProvider.DATA_PROVIDER_NAME)
    public void testReadNumber(final NumberProvider.Parameters parParameters) throws IOException {
        final JSONReader myReader = new JSONReader(new NumberReader(parParameters.mathContext));

        try {
            Assert.assertEquals(myReader.read(parParameters.number), parParameters.expected);
            Assert.assertNull(parParameters.expectedExceptionForReader);
        } catch (final JSONDecodeException myException) {
            Assert.assertNotNull(parParameters.expectedExceptionForReader);
            Assert.assertEquals(myException.getMessage(), parParameters.expectedExceptionForReader.getMessage());
            Assert.assertEquals(myException.getClass(), parParameters.expectedExceptionForReader.getClass());
        }
    }

    @Test(dataProviderClass = StringProvider.class, dataProvider = StringProvider.DATA_PROVIDER_NAME)
    public void testReadString(final StringProvider.Parameters parParameters) throws IOException {
        final JSONReader myReader = new JSONReader(new StringReader());

        try {
            final Object myResult = myReader.read(parParameters.inputString);
            // need to convert in sort of a roundabout way (or loop) as SecureCharBuffer cannot be string-converted.
            final char[] myActualChars = new char[((CharSequence) myResult).length()];
            final char[] myExpectedChars = new char[parParameters.expected.length()];
            CharBuffer.wrap((CharSequence) myResult).get(myActualChars);
            CharBuffer.wrap(parParameters.expected).get(myExpectedChars);
            Assert.assertEquals(new String(myActualChars), new String(myExpectedChars));
            Assert.assertNull(parParameters.expectedException);
        } catch (JSONDecodeException e) {
            Assert.assertNotNull(parParameters.expectedException);
        }
    }

    @Test
    public void testReadNull() throws IOException {
        Assert.assertNull(new JSONReader().read("null"));
        Assert.assertNull(new JSONReader().read(" null   "));
    }

    @Test
    public void testReadBoolean() throws IOException {
        Assert.assertTrue((boolean) new JSONReader().read("true"));
        Assert.assertTrue((boolean) new JSONReader().read("   true   "));
        Assert.assertFalse((boolean) new JSONReader().read("   false   "));
    }

    @Test(expectedExceptions = JSONDecodeException.class)
    public void testReadBooleanTrueSuffix() throws IOException {
        new JSONReader().read("truemore");
    }

    @Test(expectedExceptions = JSONDecodeException.class)
    public void testReadBooleanTrueExtraToken() throws IOException {
        new JSONReader().read("true]");
    }

    @Test(expectedExceptions = JSONDecodeException.class)
    public void testReadBooleanFalseSuffix() throws IOException {
        new JSONReader().read("falsemore");
    }

    @Test(expectedExceptions = JSONDecodeException.class)
    public void testReadBooleanFalseExtraToken() throws IOException {
        new JSONReader().read("false]");
    }

    @Test
    public void testReadEmptyList() throws IOException {
        Assert.assertTrue(new JSONReader().read("[]") instanceof List);
    }

    @Test
    public void testReadEmptyListPadded() throws IOException {
        final Object myList = new JSONReader().read("  [   ]  ");
        Assert.assertTrue(myList instanceof List);
        Assert.assertEquals(((List) myList).size(), 0);
    }

    @Test(expectedExceptions = JSONDecodeException.class)
    public void testReadEmptyListExtraToken() throws IOException {
        new JSONReader().read("[]]");
    }

    @Test
    public void testReadListWithNumbers() throws IOException {
        final Object myObject = new JSONReader().read("  [1,4 ,  -3,1.14159]  ");
        Assert.assertTrue(myObject instanceof List);
        final List myList = (List) myObject;
        Assert.assertEquals(myList.size(), 4);
        Assert.assertEquals(myList.get(0), (short) 1);
        Assert.assertEquals(myList.get(1), (short) 4);
        Assert.assertEquals(myList.get(2), (short) -3);
        Assert.assertEquals(myList.get(3), 1.14159d);
    }

    @Test(expectedExceptions = JSONDecodeException.class)
    public void testReadListWithTrailingComma() throws IOException {
        new JSONReader().read("  [1,4 ,]  ");
    }

    @Test(expectedExceptions = JSONDecodeException.class)
    public void testReadListWithMissingClosingBracket() throws IOException {
        new JSONReader().read("  [1,4  ");
    }

    @Test(expectedExceptions = JSONDecodeException.class)
    public void testReadEmptyListWithMissingClosingBracket() throws IOException {
        new JSONReader().read("  [");
    }

    @Test(expectedExceptions = JSONDecodeException.class)
    public void testReadEmptyListWithMissingClosingBracketPadded() throws IOException {
        new JSONReader().read("  [  ");
    }

    @Test(expectedExceptions = JSONDecodeException.class)
    public void testReadInvalidToken() throws IOException {
        new JSONReader().read("***");
    }
}
