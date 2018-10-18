package com.chelseaurquhart.securejson;

import com.chelseaurquhart.securejson.JSONDecodeException.InvalidTokenException;
import com.chelseaurquhart.securejson.JSONDecodeException.ExtraCharactersException;
import com.chelseaurquhart.securejson.JSONDecodeException.MalformedJSONException;
import com.chelseaurquhart.securejson.JSONDecodeException.MalformedListException;
import com.chelseaurquhart.securejson.JSONDecodeException.MalformedStringException;

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
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;

public final class JSONReaderTest {
    @Test(dataProviderClass = NumberProvider.class, dataProvider = NumberProvider.DATA_PROVIDER_NAME)
    public void testReadNumberFromString(final NumberProvider.Parameters parParameters) throws IOException {
        final JSONReader myReader = new JSONReader(new NumberReader(parParameters.mathContext));

        runTest(myReader, parParameters.number, parParameters.expected, parParameters.expectedException);
    }

    @Test(dataProviderClass = NumberProvider.class, dataProvider = NumberProvider.DATA_PROVIDER_NAME)
    public void testReadNumberFromStream(final NumberProvider.Parameters parParameters) throws IOException {
        final JSONReader myReader = new JSONReader(new NumberReader(parParameters.mathContext));
        runTest(myReader, inputToStream(parParameters.number, null), parParameters.expected,
            parParameters.expectedException);
    }

    @Test(dataProviderClass = StringProvider.class, dataProvider = StringProvider.DATA_PROVIDER_NAME)
    public void testReadStringFromString(final StringProvider.Parameters parParameters) throws IOException {
        final JSONReader myReader = new JSONReader(new StringReader());
        runTest(myReader, parParameters.inputString, parParameters.expected, parParameters.expectedException);
    }

    @Test(dataProviderClass = StringProvider.class, dataProvider = StringProvider.DATA_PROVIDER_NAME)
    public void testReadStringFromStream(final StringProvider.Parameters parParameters) throws IOException {
        final JSONReader myReader = new JSONReader(new StringReader());
        runTest(myReader, inputToStream(parParameters.inputString, null), parParameters.expected,
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
                "empty list, UTF8 BOM",
                new byte[]{(byte) 0xef, (byte) 0xbb, (byte) 0xbf, '[', ']'},
                new ArrayList<>(),
                null
            ),
            new Parameters(
                "empty list, UTF16 BOM big-endian",
                new byte[]{(byte) 0xfe, (byte) 0xff, '[', ']'},
                new ArrayList<>(),
                null
            ),
            new Parameters(
                "empty list, UTF16 BOM little-endian",
                new byte[]{(byte) 0xff, (byte) 0xfe, '[', ']'},
                new ArrayList<>(),
                null
            ),
            new Parameters(
                "empty list, UTF32 BOM big-endian",
                new byte[]{0, 0, (byte) 0xfe, (byte) 0xff, '[', ']'},
                new ArrayList<>(),
                null
            ),
            new Parameters(
                "empty list, UTF32 BOM little-endian",
                new byte[]{(byte) 0xff, (byte) 0xfe, 0, 0, '[', ']'},
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
                new MalformedJSONException(new PresetIterableCharSequence(8))
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
            new Parameters(
                "empty map",
                "{}",
                new HashMap<>(),
                null
            ),
            new Parameters(
                "map with padding",
                "  {  }  ",
                new HashMap<>(),
                null
            ),
            new Parameters(
                "map with numeric keys",
                "{1:\"test\"}",
                null,
                new MalformedStringException(new PresetIterableCharSequence(1))
            ),
            new Parameters(
                "map with string:string",
                "{\"1\":\"test\"}",
                new HashMap<CharSequence, Object>() {{
                    put("1", "test");
                }},
                null
            ),
            new Parameters(
                "map with string:int",
                "{\"1\":123}",
                new HashMap<CharSequence, Object>() {{
                    put("1", (short) 123);
                }},
                null
            ),
            new Parameters(
                "map with string:null",
                "{\"1\":null}",
                new HashMap<CharSequence, Object>() {{
                    put("1", null);
                }},
                null
            ),
            new Parameters(
                "map with string:bool",
                "{\"1\":true}",
                new HashMap<CharSequence, Object>() {{
                    put("1", true);
                }},
                null
            ),
            new Parameters(
                "map with string:bool padded",
                "{\"1\"   :    true}",
                new HashMap<CharSequence, Object>() {{
                    put("1", true);
                }},
                null
            ),
            new Parameters(
                "much nesting",
                "{\"1\"   :    [1,2,3],\"2\":[false,{\"22\":\"456\"}]}",
                new HashMap<CharSequence, Object>() {{
                    put("1", Arrays.asList((short) 1, (short) 2, (short) 3));
                    put("2", Arrays.asList(false, new HashMap<CharSequence, Object>() {{
                        put("22", "456");
                    }}));
                }},
                null
            ),
            new Parameters(
                "spaces around comma in map",
                "{\"asd\":\"sdf\"   ,  \"dfg\":\"fgh\"}",
                new HashMap<CharSequence, Object>() {{
                    put("asd", "sdf");
                    put("dfg", "fgh");
                }},
                null
            ),
            new Parameters(
                "spaces around comma in list",
                "[\"abc\"  ,  \"def\"]",
                Arrays.asList("abc", "def"),
                null
            ),
        };
    }

    @Test(dataProvider = DATA_PROVIDER_NAME)
    public void testReadGenericFromString(final Parameters parParameters) throws IOException {
        if (parParameters.inputBytes != null) {
            final char[] myChars = new char[parParameters.inputBytes.length];
            for (int myIndex = myChars.length - 1; myIndex >= 0; myIndex--) {
                myChars[myIndex] = (char) (0xff & parParameters.inputBytes[myIndex]);
            }
            parParameters.inputString = new String(myChars);
        }

        final JSONReader myReader = new JSONReader();
        runTest(myReader, parParameters.inputString, parParameters.expected,
            parParameters.expectedException);
    }

    @Test(dataProvider = DATA_PROVIDER_NAME)
    public void testReadGenericFromStream(final Parameters parParameters) throws IOException {
        final JSONReader myReader = new JSONReader();

        runTest(myReader, inputToStream(parParameters.inputString, parParameters.inputBytes), parParameters.expected,
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
            Assert.assertNull(parExpectedException, "Expected exception was not thrown");
            Assert.assertEquals(deepCharSequenceToString(myActual), deepCharSequenceToString(parExpected));
        } catch (final JSONDecodeException myException) {
            Assert.assertNotNull(parExpectedException);
            Assert.assertEquals(myException.getMessage(), parExpectedException.getMessage());
            Assert.assertEquals(myException.getClass(), parExpectedException.getClass());
        }
    }

    private Object deepCharSequenceToString(final Object parInput) {
        if (parInput == null) {
            return null;
        } else if (parInput instanceof CharSequence) {
            return charSequenceToString((CharSequence) parInput);
        } else if (parInput instanceof Map) {
            final Map myInputMap = (Map) parInput;

            final Map<String, Object> myInputMapCopy = new LinkedHashMap<>();
            for (final Object myObject : myInputMap.entrySet()) {
                final Map.Entry myEntry = (Map.Entry) myObject;
                myInputMapCopy.put(charSequenceToString((CharSequence) myEntry.getKey()),
                    deepCharSequenceToString(myEntry.getValue()));
            }

            return myInputMapCopy;
        } else if (parInput instanceof Collection) {
            final Collection myInputList = (Collection) parInput;
            final Collection<Object> myInputListCopy = new LinkedList<>();
            for (final Object myItem : myInputList) {
                myInputListCopy.add(deepCharSequenceToString(myItem));
            }

            return myInputListCopy;
        } else if (parInput.getClass().isArray()) {
            final Object[] myInputArray = (Object[]) parInput;
            for (int myIndex = 0; myIndex < myInputArray.length; myIndex++) {
                myInputArray[myIndex] = deepCharSequenceToString(myInputArray[myIndex]);
            }
        }

        return parInput;
    }

    private InputStream inputToStream(final CharSequence parInput, final byte[] parBytes) throws IOException {
        if (parBytes == null) {
            return new ByteArrayInputStream(charSequenceToString(parInput).getBytes(StandardCharsets.UTF_8));
        } else {
            return new ByteArrayInputStream(parBytes);
        }
    }

    private String charSequenceToString(final CharSequence parInput) {
        final char[] myChars = new char[parInput.length()];
        CharBuffer.wrap(parInput).get(myChars);

        return new String(myChars);
    }

    private static class Parameters {
        private String testName;
        private byte[] inputBytes;
        private CharSequence inputString;
        private Object expected;
        private Exception expectedException;

        Parameters(final String parTestName, final byte[] parInputBytes, final Object parExpected,
                   final Exception parExpectedException) {
            testName = parTestName;
            inputBytes = parInputBytes;
            expected = parExpected;
            expectedException = parExpectedException;
        }

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
