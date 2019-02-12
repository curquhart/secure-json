/*
 * Copyright 2019 Chelsea Urquhart
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

import com.chelseaurquhart.securejson.EncodingAwareCharacterIterator.Encoding;
import com.chelseaurquhart.securejson.JSONException.JSONRuntimeException;
import com.chelseaurquhart.securejson.JSONDecodeException.InvalidTokenException;
import com.chelseaurquhart.securejson.JSONDecodeException.MalformedJSONException;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;

@SuppressWarnings("PMD.CommentRequired")
public final class EncodingAwareCharacterIteratorTest {
    static final String DATA_PROVIDER_NAME = "EncodingAwareCharacterIteratorTest";

    @DataProvider(name = DATA_PROVIDER_NAME, parallel = true)
    static Object[] dataProvider() throws IOException {
        return new Object[]{
            new Parameters(
                "empty string",
                "",
                new char[0],
                null,
                Encoding.UTF8
            ),
            new Parameters(
                "single character",
                "a",
                new char[]{'a'},
                null,
                Encoding.UTF8
            ),
            new Parameters(
                "single character but iterated to end",
                new EACIIterator("a", new IConsumer<EACIIterator>() {
                    @Override
                    public void accept(final EACIIterator parInput) {
                        parInput.next();
                    }
                }),
                new char[0],
                null,
                Encoding.UTF8
            ),
            new Parameters(
                "UTF8 BOM",
                new String(new char[]{0xef, 0xbb, 0xbf, 'a', 'b'}),
                new char[]{'a', 'b'},
                null,
                Encoding.UTF8
            ),
            new Parameters(
                "UTF8 malformed BOM - missing last char",
                new String(new char[]{0xef,  0xbb, 'a', 'b'}),
                null,
                new MalformedJSONException(new PresetIterableCharSequence(2)),
                null
            ),
            new Parameters(
                "UTF8 malformed BOM - missing second char",
                new String(new char[]{0xef, 'a', 'b'}),
                null,
                new MalformedJSONException(new PresetIterableCharSequence(1)),
                null
            ),
            new Parameters(
                "UTF16 BOM big-endian",
                new String(new char[]{0xfe, 0xff, 0, 'a', 0, 'b'}),
                new char[]{'a', 'b'},
                null,
                Encoding.UTF16BE
            ),
            new Parameters(
                "UTF16 NO BOM big-endian",
                new String(new char[]{0, 'a', 0, 'b'}),
                new char[]{'a', 'b'},
                null,
                Encoding.UTF16BE
            ),
            new Parameters(
                "UTF8 with BOM",
                new String(new char[]{0, 'a', 'b'}),
                new char[]{'a', 'b'},
                null,
                Encoding.UTF8
            ),
            new Parameters(
                "UTF16 BOM little-endian",
                new String(new char[]{0xff, 0xfe, 'a', 0, 'b', 0}),
                new char[]{'a', 'b'},
                null,
                Encoding.UTF16LE
            ),
            new Parameters(
                "UTF16 BOM little-endian, missing second marker byte",
                new String(new char[]{0xff, 'a', 0, 'b', 0}),
                new char[]{'a', 'b'},
                new MalformedJSONException(new PresetIterableCharSequence(1)),
                null
            ),
            new Parameters(
                "UTF16 NO BOM little-endian",
                new String(new char[]{'a', 0, 'b', 0}),
                new char[]{'a', 'b'},
                null,
                Encoding.UTF16LE
            ),
            new Parameters(
                "UTF32 BOM big-endian",
                new String(new char[]{0, 0, (char) 0xfe, (char) 0xff, 0, 0, 0, 'a', 0, 0, 0, 'b'}),
                new char[]{'a', 'b'},
                null,
                Encoding.UTF32BE
            ),
            new Parameters(
                "UTF32 BOM big-endian, missing second marker byte",
                new String(new char[]{0, 0, (char) 0xfe, 0, 0, 0, 'a', 0, 0, 0, 'b'}),
                new char[]{'a', 'b'},
                new MalformedJSONException(new PresetIterableCharSequence(3)),
                null
            ),
            new Parameters(
                "UTF32 NO BOM big-endian",
                new String(new char[]{0, 0, 0, 'a', 0, 0, 0, 'b'}),
                new char[]{'a', 'b'},
                null,
                Encoding.UTF32BE
            ),
            new Parameters(
                "UTF32 NO BOM big-endian, malformed",
                new String(new char[]{0, 0, 0, 'a', 0, 0, 'b'}),
                new char[]{'a'},
                new InvalidTokenException(new PresetIterableCharSequence(6)),
                Encoding.UTF32BE
            ),
            new Parameters(
                "UTF32 BOM little-endian",
                new String(new char[]{0xff, 0xfe, 0, 0, 'a', 0, 0, 0, 'b', 0, 0, 0}),
                new char[]{'a', 'b'},
                null,
                Encoding.UTF32LE
            ),
            new Parameters(
                "UTF32 NO BOM little-endian",
                new String(new char[]{'a', 0, 0, 0, 'b', 0, 0, 0}),
                new char[]{'a', 'b'},
                null,
                Encoding.UTF32LE
            ),
        };
    }

    @Test(dataProvider = DATA_PROVIDER_NAME)
    public void testIterate(final Parameters parParameters) {
        try {
            if (parParameters.expected == null || parParameters.expected.length == 0) {
                Assert.assertFalse(parParameters.input.hasNext());
            } else {
                for (int myIndex = 0; myIndex < parParameters.expected.length; myIndex++) {
                    final Character myChar = parParameters.expected[myIndex];
                    Assert.assertTrue(parParameters.input.hasNext());
                    Assert.assertEquals(parParameters.input.peek(), myChar);
                    Assert.assertEquals(parParameters.input.next(), myChar);
                }
                Assert.assertFalse(parParameters.input.hasNext());
            }
            Assert.assertNull(parParameters.expectedException);
        } catch (final JSONException myException) {
            Assert.assertNotNull(parParameters.expectedException, "exception \"" + myException.getMessage() + "\"");
            Assert.assertEquals(Util.unwrapException(myException).getMessage(),
                parParameters.expectedException.getMessage());
        } catch (final JSONRuntimeException myException) {
            Assert.assertNotNull(parParameters.expectedException, "exception \"" + myException.getMessage() + "\"");
            Assert.assertEquals(Util.unwrapException(myException).getMessage(),
                parParameters.expectedException.getMessage());
        } catch (final IOException myException) {
            Assert.assertNotNull(parParameters.expectedException, "exception \"" + myException.getMessage() + "\"");
            Assert.assertEquals(Util.unwrapException(myException).getMessage(),
                parParameters.expectedException.getMessage());
        } finally {
            // we must check encoding after because we do not know the encoding until we start reading.
            // if encoding is malformed we could know the encoding but still get an error. Due to this, check the
            // encoding, even on error.
            Assert.assertEquals(parParameters.input.getEncoding(), parParameters.expectedEncoding);
        }
    }

    static class EACIIterator extends EncodingAwareCharacterIterator {
        private final CharSequence input;
        private int index;

        EACIIterator(final CharSequence parInput, final IConsumer<EACIIterator> parInitializer) {
            this(parInput);
            parInitializer.accept(this);
        }

        EACIIterator(final CharSequence parInput) {
            final ManagedSecureCharBuffer mySecureBuffer = new ManagedSecureCharBuffer(parInput.length());
            mySecureBuffer.append(parInput);
            input = mySecureBuffer;
        }

        @Override
        protected Character readNextChar() {
            if (index == input.length()) {
                return null;
            }

            return input.charAt(index++);
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean canReadRange() {
            return true;
        }

        @Override
        public CharSequence range(final int parStart, final int parEnd) {
            return input.subSequence(parStart, parEnd);
        }
    }

    static class Parameters {
        private String testName;
        private EACIIterator input;
        private char[] expected;
        private Exception expectedException;
        private EncodingAwareCharacterIterator.Encoding expectedEncoding;

        Parameters(final String parTestName, final CharSequence parInput, final char[] parExpected,
                   final Exception parExpectedException, final Encoding parExpectedEncoding) {
            this(parTestName, new EACIIterator(parInput), parExpected, parExpectedException, parExpectedEncoding);
        }

        Parameters(final String parTestName, final EACIIterator parInput, final char[] parExpected,
                   final Exception parExpectedException, final Encoding parExpectedEncoding) {
            testName = parTestName;
            input = parInput;
            expected = parExpected;
            expectedException = parExpectedException;
            expectedEncoding = parExpectedEncoding;
        }

        @Override
        public String toString() {
            return testName;
        }
    }

}
