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

import com.chelseaurquhart.securejson.JSONDecodeException.InvalidTokenException;
import com.chelseaurquhart.securejson.JSONDecodeException.EmptyJSONException;
import com.chelseaurquhart.securejson.JSONDecodeException.ExtraCharactersException;
import com.chelseaurquhart.securejson.JSONDecodeException.MalformedJSONException;
import com.chelseaurquhart.securejson.JSONDecodeException.MalformedListException;
import com.chelseaurquhart.securejson.JSONDecodeException.MalformedMapException;
import com.chelseaurquhart.securejson.JSONDecodeException.MalformedStringException;
import com.chelseaurquhart.securejson.JSONException.JSONRuntimeException;

import com.chelseaurquhart.securejson.util.StringUtil;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

@SuppressWarnings("PMD.CommentRequired")
public final class JSONReaderTest {
    @Test(dataProviderClass = NumberProvider.class, dataProvider = NumberProvider.DATA_PROVIDER_NAME)
    public void testReadNumberFromString(final NumberProvider.Parameters<?> parParameters) {
        final JSONReader myReader = new JSONReader.Builder(Settings.DEFAULTS)
            .numberReader(new NumberReader(parParameters.mathContext, Settings.DEFAULTS))
            .build();

        runTest(myReader, parParameters.number, parParameters.expected, parParameters.expectedException);
    }

    @Test(dataProviderClass = NumberProvider.class, dataProvider = NumberProvider.DATA_PROVIDER_NAME)
    public void testReadNumberFromStream(final NumberProvider.Parameters<?> parParameters) {
        final JSONReader myReader = new JSONReader.Builder(Settings.DEFAULTS)
            .numberReader(new NumberReader(parParameters.mathContext, Settings.DEFAULTS))
            .build();

        runTest(myReader, inputToStream(parParameters.number, null), parParameters.expected,
            parParameters.expectedException);
    }

    @Test(dataProviderClass = StringProvider.class, dataProvider = StringProvider.DATA_PROVIDER_NAME)
    public void testReadStringFromString(final StringProvider.Parameters parParameters) {
        final JSONReader myReader = new JSONReader.Builder(Settings.DEFAULTS)
            .stringReader(new StringReader(Settings.DEFAULTS))
            .build();

        runTest(myReader, parParameters.inputString, parParameters.expected, parParameters.expectedException);
    }

    @Test(dataProviderClass = StringProvider.class, dataProvider = StringProvider.DATA_PROVIDER_NAME)
    public void testReadStringFromStream(final StringProvider.Parameters parParameters) {
        final JSONReader myReader = new JSONReader.Builder(Settings.DEFAULTS)
            .stringReader(new StringReader(Settings.DEFAULTS))
            .build();

        runTest(myReader, inputToStream(parParameters.inputString, null), parParameters.expected,
            parParameters.expectedException);
    }

    static final String DATA_PROVIDER_NAME = "JSONReaderTest";

    @DataProvider(name = DATA_PROVIDER_NAME, parallel = true)
    static Object[] dataProvider(final Method parMethod) throws IOException {
        return new Object[]{
            new Parameters<Object>(
                "empty input",
                "",
                null,
                new EmptyJSONException(new PresetIterableCharSequence())
            ),
            new Parameters<Object>(
                "null",
                "null",
                null,
                null
            ),
            new Parameters<Object>(
                "padded null",
                " null   ",
                null,
                null
            ),
            new Parameters<Boolean>(
                "boolean true",
                "true",
                true,
                null
            ),
            new Parameters<Boolean>(
                "boolean padded true",
                "  true  ",
                true,
                null
            ),
            new Parameters<Boolean>(
                "boolean false",
                "false",
                false,
                null
            ),
            new Parameters<Boolean>(
                "boolean padded false",
                "   false   ",
                false,
                null
            ),
            new Parameters<Boolean>(
                "boolean true with suffix",
                "truemore",
                false,
                new InvalidTokenException(new PresetIterableCharSequence(4))
            ),
            new Parameters<Boolean>(
                "boolean true with extra token",
                "true]",
                false,
                new ExtraCharactersException(new PresetIterableCharSequence(4))
            ),
            new Parameters<Boolean>(
                "boolean false with suffix",
                "falsemore",
                false,
                new InvalidTokenException(new PresetIterableCharSequence(5))
            ),
            new Parameters<Boolean>(
                "boolean false with extra token",
                "false]",
                false,
                new ExtraCharactersException(new PresetIterableCharSequence(5))
            ),
            new Parameters<List<Object>>(
                "empty list",
                "[]",
                new ArrayList<Object>(),
                null
            ),
            new Parameters<List<Object>>(
                "empty list, UTF8 BOM",
                new byte[]{(byte) 0xef, (byte) 0xbb, (byte) 0xbf, '[', ']'},
                new ArrayList<Object>(),
                null
            ),
            new Parameters<List<Object>>(
                "empty list, UTF16 BOM big-endian",
                new byte[]{(byte) 0xfe, (byte) 0xff, 0, '[', 0, ']'},
                new ArrayList<Object>(),
                null
            ),
            new Parameters<List<Object>>(
                "empty list, UTF16 NO BOM big-endian",
                new byte[]{0, '[', 0, ']'},
                new ArrayList<Object>(),
                null
            ),
            new Parameters<List<Object>>(
                "empty list, UTF8 with BOM",
                new byte[]{0, '[', ']'},
                new ArrayList<Object>(),
                null
            ),
            new Parameters<List<Object>>(
                "empty list, UTF16 BOM little-endian",
                new byte[]{(byte) 0xff, (byte) 0xfe, '[', 0, ']', 0},
                new ArrayList<Object>(),
                null
            ),
            new Parameters<List<Object>>(
                "empty list, UTF16 NO BOM little-endian",
                new byte[]{'[', 0, ']', 0},
                new ArrayList<Object>(),
                null
            ),
            new Parameters<List<Object>>(
                "empty list, UTF32 BOM big-endian",
                new byte[]{0, 0, (byte) 0xfe, (byte) 0xff, 0, 0, 0, '[', 0, 0, 0, ']'},
                new ArrayList<Object>(),
                null
            ),
            new Parameters<List<Object>>(
                "empty list, UTF32 NO BOM big-endian",
                new byte[]{0, 0, 0, '[', 0, 0, 0, ']'},
                new ArrayList<Object>(),
                null
            ),
            new Parameters<List<Object>>(
                "empty list, UTF32 NO BOM big-endian, malformed",
                new byte[]{0, 0, 0, '[', 0, 0, ']'},
                new ArrayList<Object>(),
                new InvalidTokenException(new PresetIterableCharSequence(6))
            ),
            new Parameters<List<Object>>(
                "empty list, UTF32 BOM little-endian",
                new byte[]{(byte) 0xff, (byte) 0xfe, 0, 0, '[', 0, 0, 0, ']', 0, 0, 0},
                new ArrayList<Object>(),
                null
            ),
            new Parameters<List<Object>>(
                "empty list, UTF32 NO BOM little-endian",
                new byte[]{'[', 0, 0, 0, ']', 0, 0, 0},
                new ArrayList<Object>(),
                null
            ),
            new Parameters<List<Object>>(
                "empty list, UTF8 BOM, extra token",
                new byte[]{(byte) 0xef, (byte) 0xbb, (byte) 0xbf, '[', ']', ']'},
                new ArrayList<Object>(),
                new ExtraCharactersException(new PresetIterableCharSequence(5))
            ),
            new Parameters<List<Object>>(
                "empty list, UTF16 BOM big-endian, extra token",
                new byte[]{(byte) 0xfe, (byte) 0xff, 0, '[', 0, ']', 0, ']'},
                new ArrayList<Object>(),
                new ExtraCharactersException(new PresetIterableCharSequence(7))
            ),
            new Parameters<List<Object>>(
                "empty list, UTF16 BOM little-endian, extra token",
                new byte[]{(byte) 0xff, (byte) 0xfe, '[', 0, ']', 0, ']', 0},
                new ArrayList<Object>(),
                new ExtraCharactersException(new PresetIterableCharSequence(7))
            ),
            new Parameters<List<Object>>(
                "empty list, UTF32 BOM big-endian, extra token",
                new byte[]{0, 0, (byte) 0xfe, (byte) 0xff, 0, 0, 0, '[', 0, 0, 0, ']', 0, 0, 0, ']'},
                new ArrayList<Object>(),
                new ExtraCharactersException(new PresetIterableCharSequence(15))
            ),
            new Parameters<List<Object>>(
                "empty list, UTF32 BOM little-endian, extra token",
                new byte[]{(byte) 0xff, (byte) 0xfe, 0, 0, '[', 0, 0, 0, ']', 0, 0, 0, ']', 0, 0, 0},
                new ArrayList<Object>(),
                new ExtraCharactersException(new PresetIterableCharSequence(15))
            ),
            new Parameters<List<Object>>(
                "empty list padded",
                "  [   ]  ",
                new ArrayList<Object>(),
                null
            ),
            new Parameters<Boolean>(
                "empty list extra token",
                "[]]",
                false,
                new ExtraCharactersException(new PresetIterableCharSequence(2))
            ),
            new Parameters<List<Number>>(
                "list with numbers",
                "  [1,4 ,  -3,1.14159]  ",
                Arrays.asList(
                    1,
                    4,
                    -3,
                    (Number) 1.14159d
                ),
                null
            ),
            new Parameters<Object>(
                "list with trailing comma",
                "  [1,4 ,]  ",
                null,
                new InvalidTokenException(new PresetIterableCharSequence(8))
            ),
            new Parameters<Object>(
                "list with missing closing bracket",
                "  [1,4  ",
                null,
                new MalformedJSONException(new PresetIterableCharSequence(8))
            ),
            new Parameters<Object>(
                "empty list with missing closing bracket",
                "  [",
                null,
                new MalformedListException(new PresetIterableCharSequence(3))
            ),
            new Parameters<Object>(
                "empty padded list with missing closing bracket",
                "  [  ",
                null,
                new MalformedListException(new PresetIterableCharSequence(5))
            ),
            new Parameters<Object>(
                "empty map with missing closing brace",
                "  {",
                null,
                new MalformedMapException(new PresetIterableCharSequence(3))
            ),
            new Parameters<Object>(
                "empty padded map with missing closing brace",
                "  {  ",
                null,
                new MalformedMapException(new PresetIterableCharSequence(5))
            ),
            new Parameters<Object>(
                "nested lists",
                "  [[1,2],3]  ",
                new ArrayList<Object>() {
                    private static final long serialVersionUID = 1L;

                    {
                        add(new ArrayList<Integer>() {
                            private static final long serialVersionUID = 1L;

                            {
                                add(1);
                                add(2);
                            }
                        });
                        add(3);
                    }
                },
                null
            ),
            new Parameters<Object>(
                "nested lists malformed comma",
                "  [[1,2],  ,]  ",
                null,
                new InvalidTokenException(new PresetIterableCharSequence(11))
            ),
            new Parameters<Object>(
                "invalid token",
                "***",
                null,
                new InvalidTokenException(new PresetIterableCharSequence())
            ),
            new Parameters<Object>(
                "empty map",
                "{}",
                new HashMap<Object, Object>(),
                null
            ),
            new Parameters<Object>(
                "map with padding",
                "  {  }  ",
                new HashMap<Object, Object>(),
                null
            ),
            new Parameters<Object>(
                "map with numeric keys",
                "{1:\"test\"}",
                null,
                new MalformedStringException(new PresetIterableCharSequence(1))
            ),
            new Parameters<Object>(
                "map with list key",
                "{[\"x\"]:123}",
                null,
                new MalformedStringException(new PresetIterableCharSequence(1))
            ),
            new Parameters<Object>(
                "map with map key",
                "{[\"x\":1]:123}",
                null,
                new MalformedListException(new PresetIterableCharSequence(5))
            ),
            new Parameters<Object>(
                "map with list keys with immediate map separator in list",
                "{[: \"x\"]:123}",
                null,
                // NOTE: technically, we could detect the deformity at character 1 but because of the way we parse,
                // we will not see it until character 2.
                new MalformedJSONException(new PresetIterableCharSequence(2))
            ),
            new Parameters<Object>(
                "list with map separator",
                "[: \"x\"]",
                null,
                new MalformedJSONException(new PresetIterableCharSequence(1))
            ),
            new Parameters<Object>(
                "list with single map separator",
                "[:]",
                null,
                new MalformedJSONException(new PresetIterableCharSequence(1))
            ),
            new Parameters<Object>(
                "map with string:string",
                "{\"1\":\"test\"}",
                new HashMap<CharSequence, Object>() {
                    private static final long serialVersionUID = 1L;

                    {
                        put("1", "test");
                    }
                },
                null
            ),
            new Parameters<Object>(
                "map with string:int",
                "{\"1\":123}",
                new HashMap<CharSequence, Object>() {
                    private static final long serialVersionUID = 1L;

                    {
                        put("1", 123);
                    }
                },
                null
            ),
            new Parameters<Object>(
                "map with string:null",
                "{\"1\":null}",
                new HashMap<CharSequence, Object>() {
                    private static final long serialVersionUID = 1L;

                    {
                        put("1", null);
                    }
                },
                null
            ),
            new Parameters<Object>(
                "map with string:bool",
                "{\"1\":true}",
                new HashMap<CharSequence, Object>() {
                    private static final long serialVersionUID = 1L;

                    {
                        put("1", true);
                    }
                },
                null
            ),
            new Parameters<Object>(
                "map with string:bool padded",
                "{\"1\"   :    true}",
                new HashMap<CharSequence, Object>() {
                    private static final long serialVersionUID = 1L;

                    {
                        put("1", true);
                    }
                },
                null
            ),
            new Parameters<Object>(
                "much nesting",
                "{\"1\"   :    [1,2,3],\"2\":[false,{\"22\":\"456\"}]}",
                new HashMap<CharSequence, Object>() {
                    private static final long serialVersionUID = 1L;

                    {
                        put("1", Arrays.asList(1, 2, 3));
                        put("2", Arrays.asList(false, new HashMap<CharSequence, Object>() {
                            private static final long serialVersionUID = 1L;

                            {
                                put("22", "456");
                            }
                        }));
                    }
                },
                null
            ),
            new Parameters<Object>(
                "spaces around comma in map",
                "{\"asd\":\"sdf\"   ,  \"dfg\":\"fgh\"}",
                new HashMap<CharSequence, Object>() {
                    private static final long serialVersionUID = 1L;

                    {
                        put("asd", "sdf");
                        put("dfg", "fgh");
                    }
                },
                null
            ),
            new Parameters<Object>(
                "spaces around comma in list",
                "[\"abc\"  ,  \"def\"]",
                Arrays.asList("abc", "def"),
                null
            ),
            new Parameters<Deserializable>(
                "custom object",
                "[\"abc\"  ,  \"def\"]",
                new Deserializable() {
                    private static final long serialVersionUID = 1L;

                    {
                        input = new LinkedList<Object>() {
                            private static final long serialVersionUID = 1L;

                            {
                                add("abc");
                                add("def");
                            }
                        };
                    }
                },
                null
            ).clazz(Deserializable.class),
        };
    }

    @Test(dataProvider = DATA_PROVIDER_NAME)
    public void testReadGenericFromString(final Parameters<?> parParameters) {
        if (parParameters.inputBytes != null) {
            final char[] myChars = new char[parParameters.inputBytes.length];
            for (int myIndex = myChars.length - 1; myIndex >= 0; myIndex--) {
                myChars[myIndex] = (char) (0xff & parParameters.inputBytes[myIndex]);
            }
            parParameters.inputString = new String(myChars);
        }

        final JSONReader myReader = new JSONReader.Builder(Settings.DEFAULTS).build();
        runTest(myReader, parParameters.inputString, parParameters.expected,
            parParameters.expectedException);
    }

    @Test(dataProvider = DATA_PROVIDER_NAME)
    public void testReadGenericFromStream(final Parameters<?> parParameters) {
        final JSONReader myReader = new JSONReader.Builder(Settings.DEFAULTS).build();

        runTest(myReader, inputToStream(parParameters.inputString, parParameters.inputBytes), parParameters.expected,
            parParameters.expectedException);
    }

    private void runTest(final JSONReader parReader, final Object parInput, final Object parExpected,
                         final Exception parExpectedException) {
        try {
            final Object myActual;
            if (parInput instanceof CharSequence) {
                myActual = parReader.read((CharSequence) parInput);
            } else {
                myActual = parReader.read((InputStream) parInput);
            }
            Assert.assertNull(parExpectedException, "Expected exception was not thrown");
            Assert.assertEquals(StringUtil.deepCharSequenceToString(myActual),
                StringUtil.deepCharSequenceToString(parExpected));
        } catch (final IOException myException) {
            Assert.assertNotNull(parExpectedException);
            Assert.assertEquals(Util.unwrapException(myException).getMessage(),
                parExpectedException.getMessage());
            Assert.assertEquals(Util.unwrapException(myException).getClass(),
                parExpectedException.getClass());
        } catch (final JSONException myException) {
            Assert.assertNotNull(parExpectedException);
            Assert.assertEquals(Util.unwrapException(myException).getMessage(),
                parExpectedException.getMessage());
            Assert.assertEquals(Util.unwrapException(myException).getClass(),
                parExpectedException.getClass());
        } catch (final JSONRuntimeException myException) {
            Assert.assertNotNull(parExpectedException);
            Assert.assertEquals(Util.unwrapException(myException).getMessage(),
                parExpectedException.getMessage());
            Assert.assertEquals(Util.unwrapException(myException).getClass(),
                parExpectedException.getClass());
        }
    }

    static InputStream inputToStream(final CharSequence parInput, final byte[] parBytes) {
        if (parBytes == null) {
            return new ByteArrayInputStream(StringUtil.charSequenceToString(parInput).getBytes(StandardCharsets.UTF_8));
        } else {
            return new ByteArrayInputStream(parBytes);
        }
    }

    static class Parameters<T> {
        private String testName;
        private byte[] inputBytes;
        private CharSequence inputString;
        private T expected;
        private Class<T> expectedClass;
        private Exception expectedException;

        Parameters(final String parTestName, final byte[] parInputBytes, final T parExpected,
                   final Exception parExpectedException) {
            testName = parTestName;
            inputBytes = parInputBytes;
            expected = parExpected;
            expectedException = parExpectedException;
        }

        Parameters(final String parTestName, final CharSequence parInputString, final T parExpected,
                   final Exception parExpectedException) {
            testName = parTestName;
            final ManagedSecureCharBuffer mySecureBuffer = new ManagedSecureCharBuffer(parInputString.length());
            mySecureBuffer.append(parInputString);
            inputString = mySecureBuffer;
            expected = parExpected;
            expectedException = parExpectedException;
        }

        Parameters<T> clazz(final Class<T> parClass) {
            expectedClass = parClass;

            return this;
        }

        CharSequence getInputString() {
            if (inputBytes != null) {
                final char[] myChars = new char[inputBytes.length];
                for (int myIndex = myChars.length - 1; myIndex >= 0; myIndex--) {
                    myChars[myIndex] = (char) (0xff & inputBytes[myIndex]);
                }
                return new String(myChars);
            }

            return inputString;
        }

        byte[] getInputBytes() {
            return inputBytes;
        }

        public Object getExpected() {
            return expected;
        }

        Exception getExpectedException() {
            return expectedException;
        }

        @Override
        public String toString() {
            return testName;
        }

        Class<T> getExpectedClass() {
            return expectedClass;
        }
    }

    static class Deserializable implements IJSONAware {
        transient Object input;

        private Deserializable() {
        }

        @Override
        public void fromJSONable(final Object parInput) {
            input = parInput;
        }

        @Override
        public Object toJSONable() {
            return input;
        }
    }

    public static class ContainerStackTest {
        @Test
        public void testPopEmpty() {
            new JSONReader.ContainerStack().pop();
        }

        @Test
        public void testPushPopElements() {
            final JSONReader.ContainerStack myStack = new JSONReader.ContainerStack();
            final Container myContainerFirst = new Container("first");
            final Container myContainerSecond = new Container("second");
            myStack.push(myContainerFirst);
            myStack.push(myContainerSecond);
            Assert.assertSame(myStack.peek(), myContainerSecond);
            Assert.assertSame(myStack.peek(), myContainerSecond);
            myStack.pop();
            Assert.assertSame(myStack.peek(), myContainerFirst);
            myStack.pop();
            Assert.assertSame(myStack.peek(), null);
            myStack.pop();
            Assert.assertSame(myStack.peek(), null);
        }

        private static class Container implements JSONReader.IContainer<Object, IReader<?>> {
            private final String name;

            Container(final String parName) {
                name = parName;
            }

            @Override
            public Object resolve() {
                return null;
            }

            @Override
            public IReader<?> getReader() {
                return null;
            }

            @Override
            public String toString() {
                return name;
            }
        }
    }
}
