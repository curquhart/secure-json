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
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;

@SuppressWarnings("PMD.CommentRequired")
public final class ManagedSecureCharBufferTest {
    @Test
    public void testFullBuffer() throws IOException {
        ManagedSecureCharBuffer myManagedSecureCharBuffer = null;
        try {
            myManagedSecureCharBuffer = new ManagedSecureCharBuffer(4);
            myManagedSecureCharBuffer.append('t');
            myManagedSecureCharBuffer.append('e');
            myManagedSecureCharBuffer.append('s');
            myManagedSecureCharBuffer.append('t');
            Assert.assertEquals("test", StringUtil.charSequenceToString(myManagedSecureCharBuffer));
        } finally {
            if (myManagedSecureCharBuffer != null) {
                myManagedSecureCharBuffer.close();
            }
        }
    }

    @Test
    public void testOverflowBuffer() throws IOException {
        ManagedSecureCharBuffer myManagedSecureCharBuffer = null;
        try {
            myManagedSecureCharBuffer = new ManagedSecureCharBuffer(4);
            myManagedSecureCharBuffer.append('t');
            myManagedSecureCharBuffer.append('e');
            myManagedSecureCharBuffer.append('s');
            myManagedSecureCharBuffer.append('t');
            myManagedSecureCharBuffer.append('2');
            Assert.assertEquals("test2", StringUtil.charSequenceToString(myManagedSecureCharBuffer));
        } finally {
            if (myManagedSecureCharBuffer != null) {
                myManagedSecureCharBuffer.close();
            }
        }
    }

    @Test
    public void testUnderflowBuffer() throws IOException {
        ManagedSecureCharBuffer myManagedSecureCharBuffer = null;
        try {
            myManagedSecureCharBuffer = new ManagedSecureCharBuffer(4);
            myManagedSecureCharBuffer.append('t');
            myManagedSecureCharBuffer.append('e');
            Assert.assertEquals("te", StringUtil.charSequenceToString(myManagedSecureCharBuffer));
        } finally {
            if (myManagedSecureCharBuffer != null) {
                myManagedSecureCharBuffer.close();
            }
        }
    }

    @Test
    public void testCharSequenceByReference() throws IOException {
        ManagedSecureCharBuffer myManagedSecureCharBuffer = null;
        try {
            myManagedSecureCharBuffer = new ManagedSecureCharBuffer(4);
            final MutatableString myCharSequence = new MutatableString();

            myCharSequence.string = "TEST";
            myManagedSecureCharBuffer.append(myCharSequence);

            Assert.assertEquals(StringUtil.charSequenceToString(myManagedSecureCharBuffer), "TEST");
            myCharSequence.string = "TEST2";
            Assert.assertEquals(StringUtil.charSequenceToString(myManagedSecureCharBuffer), "TEST2");
        } finally {
            if (myManagedSecureCharBuffer != null) {
                myManagedSecureCharBuffer.close();
            }
        }
    }

    @Test
    public void testCharSequenceAndBytesMixedClose() throws IOException {
        ManagedSecureCharBuffer myManagedSecureCharBuffer = null;
        try {
            myManagedSecureCharBuffer = new ManagedSecureCharBuffer(4);

            myManagedSecureCharBuffer.append('a');
            myManagedSecureCharBuffer.append('b');
            myManagedSecureCharBuffer.append("test");
            myManagedSecureCharBuffer.append('c');
            myManagedSecureCharBuffer.append("test2");
            myManagedSecureCharBuffer.append('d');

            Assert.assertEquals(StringUtil.charSequenceToString(myManagedSecureCharBuffer), "abtestctest2d");
        } finally {
            if (myManagedSecureCharBuffer != null) {
                myManagedSecureCharBuffer.close();
            }
        }

        // after closing all should be empty
        Assert.assertEquals(StringUtil.charSequenceToString(myManagedSecureCharBuffer), "");
    }

    @Test(expectedExceptions = UnsupportedOperationException.class)
    public void testToStringException() throws IOException {
        ManagedSecureCharBuffer myManagedSecureCharBuffer = null;
        try {
            myManagedSecureCharBuffer = new ManagedSecureCharBuffer(4);
            myManagedSecureCharBuffer.append('a');
            myManagedSecureCharBuffer.append('b');
            myManagedSecureCharBuffer.toString();
        } finally {
            if (myManagedSecureCharBuffer != null) {
                myManagedSecureCharBuffer.close();
            }
        }
    }

    @Test(dataProvider = CharBufferProvider.SUBSEQUENCE_DATA_PROVIDER_NAME,
            dataProviderClass = CharBufferProvider.class)
    public void testSubSequence(final CharBufferProvider.Parameters parParameters) throws IOException {
        final IWritableCharSequence myManagedSecureCharBuffer = new ManagedSecureCharBuffer(parParameters.capacity);
        parParameters.consumer.accept(myManagedSecureCharBuffer);
        try {
            final CharSequence mySequence = myManagedSecureCharBuffer.subSequence(
                parParameters.start, parParameters.end);
            Assert.assertEquals(StringUtil.charSequenceToString(mySequence), parParameters.expected);
            Assert.assertNull(parParameters.expectedException);
        } catch (final StringIndexOutOfBoundsException myException) {
            Assert.assertNotNull(parParameters.expectedException);
            Assert.assertEquals(myException.getMessage(), parParameters.expectedException);
        } finally {
            myManagedSecureCharBuffer.close();
        }
    }

    @Test(dataProvider = CharBufferProvider.EQUALS_DATA_PROVIDER_NAME, dataProviderClass = CharBufferProvider.class)
    public void testEqual(final CharBufferProvider.Parameters parParameters) throws IOException {
        final IWritableCharSequence myManagedSecureCharBuffer = new ManagedSecureCharBuffer(parParameters.capacity);
        parParameters.consumer.accept(myManagedSecureCharBuffer);
        try {
            if (parParameters.expectedEquals) {
                Assert.assertTrue(CharBufferProvider.isEqual(myManagedSecureCharBuffer, parParameters.expected));
            } else {
                Assert.assertFalse(CharBufferProvider.isEqual(myManagedSecureCharBuffer, parParameters.expected));
            }
        } finally {
            myManagedSecureCharBuffer.close();
        }
    }

    private static class MutatableString implements CharSequence {
        private String string;

        @Override
        public int length() {
            return string.length();
        }

        @Override
        public char charAt(final int parIndex) {
            return string.charAt(parIndex);
        }

        @Override
        public CharSequence subSequence(final int parStart, final int parEnd) {
            return string.subSequence(parStart, parEnd);
        }
    }

}
