package com.chelseaurquhart.securejson;

import com.chelseaurquhart.securejson.JSONDecodeException.InvalidTokenException;
import com.chelseaurquhart.securejson.JSONDecodeException.ExtraCharactersException;
import com.chelseaurquhart.securejson.JSONDecodeException.MalformedListException;

import io.github.novacrypto.SecureCharBuffer;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;

public final class JSONReaderTest {
    @Test(dataProviderClass = NumberProvider.class, dataProvider = NumberProvider.DATA_PROVIDER_NAME)
    public void testReadNumberFromString(final NumberProvider.Parameters parParameters) throws IOException {
        final JSONReader myReader = new JSONReader(new NumberReader(parParameters.mathContext));

        runTest(myReader, parParameters.number, parParameters.expected, parParameters.expectedExceptionForReader);
    }

    @Test(dataProviderClass = NumberProvider.class, dataProvider = NumberProvider.DATA_PROVIDER_NAME)
    public void testReadNumberFromStream(final NumberProvider.Parameters parParameters) throws IOException {
        final JSONReader myReader = new JSONReader(new NumberReader(parParameters.mathContext));
        runTest(myReader, charSequenceToStream(parParameters.number), parParameters.expected,
            parParameters.expectedExceptionForReader);
    }

    @Test(dataProviderClass = StringProvider.class, dataProvider = StringProvider.DATA_PROVIDER_NAME)
    public void testReadStringFromString(final StringProvider.Parameters parParameters) throws IOException {
        final JSONReader myReader = new JSONReader(new StringReader());
        runTest(myReader, parParameters.inputString, parParameters.expected, parParameters.expectedException);
    }

    @Test(dataProviderClass = StringProvider.class, dataProvider = StringProvider.DATA_PROVIDER_NAME)
    public void testReadStringFromStream(final StringProvider.Parameters parParameters) throws IOException {
        final JSONReader myReader = new JSONReader(new StringReader());
        runTest(myReader, charSequenceToStream(parParameters.inputString), parParameters.expected,
            parParameters.expectedException);
    }

    private static final String DATA_PROVIDER_NAME = "JSONReaderTest";

    @DataProvider(name = DATA_PROVIDER_NAME, parallel = true)
    private static Object[] dataProvider(final Method parMethod) throws IOException {
        return new Object[]{
            new Parameters(
                "null",
                "null",
                null,
                null
            ),
            new Parameters(
                "padded null",
                " null   ",
                null,
                null
            ),
            new Parameters(
                "boolean true",
                "true",
                true,
                null
            ),
            new Parameters(
                "boolean padded true",
                "  true  ",
                true,
                null
            ),
            new Parameters(
                "boolean false",
                "false",
                false,
                null
            ),
            new Parameters(
                "boolean padded false",
                "   false   ",
                false,
                null
            ),
            new Parameters(
                "boolean true with suffix",
                "truemore",
                false,
                new InvalidTokenException(new PresetIterableCharSequence(4))
            ),
            new Parameters(
                "boolean true with extra token",
                "true]",
                false,
                new ExtraCharactersException(new PresetIterableCharSequence(4))
            ),
            new Parameters(
                "boolean false with suffix",
                "falsemore",
                false,
                new InvalidTokenException(new PresetIterableCharSequence(5))
            ),
            new Parameters(
                "boolean false with extra token",
                "false]",
                false,
                new ExtraCharactersException(new PresetIterableCharSequence(5))
            ),
            new Parameters(
                "empty list",
                "[]",
                new ArrayList<>(),
                null
            ),
            new Parameters(
                "empty list padded",
                "  [   ]  ",
                new ArrayList<>(),
                null
            ),
            new Parameters(
                "empty list extra token",
                "[]]",
                false,
                new ExtraCharactersException(new PresetIterableCharSequence(2))
            ),
            new Parameters(
                "list with numbers",
                "  [1,4 ,  -3,1.14159]  ",
                Arrays.asList(
                    (short) 1,
                    (short) 4,
                    (short) -3,
                    1.14159d
                ),
                null
            ),
            new Parameters(
                "list with trailing comma",
                "  [1,4 ,]  ",
                null,
                new InvalidTokenException(new PresetIterableCharSequence(8))
            ),
            new Parameters(
                "list with missing closing bracket",
                "  [1,4  ",
                null,
                new MalformedListException(new PresetIterableCharSequence(8))
            ),
            new Parameters(
                "empty list with missing closing bracket",
                "  [",
                null,
                new MalformedListException(new PresetIterableCharSequence(3))
            ),
            new Parameters(
                "empty padded list with missing closing bracket",
                "  [  ",
                null,
                new MalformedListException(new PresetIterableCharSequence(5))
            ),
            new Parameters(
                "invalid token",
                "***",
                null,
                new InvalidTokenException(new PresetIterableCharSequence())
            ),
        };
    }

    @Test(dataProvider = DATA_PROVIDER_NAME)
    public void testReadGenericFromString(final Parameters parParameters) throws IOException {
        final JSONReader myReader = new JSONReader();

        runTest(myReader, parParameters.inputString, parParameters.expected,
            parParameters.expectedException);
    }

    @Test(dataProvider = DATA_PROVIDER_NAME)
    public void testReadGenericFromStream(final Parameters parParameters) throws IOException {
        final JSONReader myReader = new JSONReader();

        runTest(myReader, charSequenceToStream(parParameters.inputString), parParameters.expected,
            parParameters.expectedException);
    }

    private void runTest(final JSONReader parReader, final Object parInput, final Object parExpected,
                         final Exception parExpectedException) throws IOException {
        try {
            final Object myActual;
            if (parInput instanceof CharSequence) {
                myActual = parReader.read((CharSequence) parInput);
            } else {
                myActual = parReader.read((InputStream) parInput);
            }
            Assert.assertNull(parExpectedException);
            if (parExpected == null) {
                Assert.assertNull(myActual);
            } else if (myActual instanceof CharSequence && parExpected instanceof CharSequence) {
                Assert.assertEquals(charSequenceToString((CharSequence) myActual),
                    charSequenceToString((CharSequence) parExpected));
            } else {
                Assert.assertEquals(myActual, parExpected);
            }
        } catch (final JSONDecodeException myException) {
            Assert.assertNotNull(parExpectedException);
            Assert.assertEquals(myException.getMessage(), parExpectedException.getMessage());
            Assert.assertEquals(myException.getClass(), parExpectedException.getClass());
        }
    }


    private InputStream charSequenceToStream(final CharSequence parInput) {
        return new ByteArrayInputStream(charSequenceToString(parInput).getBytes(StandardCharsets.UTF_8));
    }

    private String charSequenceToString(final CharSequence parInput) {
        final char[] myChars = new char[parInput.length()];
        CharBuffer.wrap(parInput).get(myChars);

        return new String(myChars);
    }

    private static class Parameters {
        private String testName;
        private CharSequence inputString;
        private Object expected;
        private Exception expectedException;

        Parameters(final String parTestName, final CharSequence parInputString, final Object parExpected,
                   final Exception parExpectedException) {
            testName = parTestName;
            final SecureCharBuffer mySecureBuffer = SecureCharBuffer.withCapacity(parInputString.length());
            mySecureBuffer.append(parInputString);
            inputString = mySecureBuffer;
            expected = parExpected;
            expectedException = parExpectedException;
        }

        @Override
        public String toString() {
            return testName;
        }
    }
}
