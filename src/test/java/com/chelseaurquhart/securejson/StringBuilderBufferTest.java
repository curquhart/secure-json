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

import com.chelseaurquhart.securejson.util.StringUtil;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;

@SuppressWarnings("PMD.CommentRequired")
public final class StringBuilderBufferTest {
    @Test(dataProvider = CharBufferProvider.SUBSEQUENCE_DATA_PROVIDER_NAME,
            dataProviderClass = CharBufferProvider.class)
    public void testSubSequence(final CharBufferProvider.Parameters parParameters) throws IOException {
        final IWritableCharSequence myManagedSecureCharBuffer = new StringBuilderBuffer(parParameters.capacity);
        parParameters.consumer.accept(myManagedSecureCharBuffer);
        try {
            final CharSequence mySequence = myManagedSecureCharBuffer.subSequence(
                parParameters.start, parParameters.end);
            Assert.assertEquals(StringUtil.charSequenceToString(mySequence), parParameters.expected);
            Assert.assertNull(parParameters.expectedException);
        } catch (final StringIndexOutOfBoundsException myException) {
            Assert.assertNotNull(parParameters.expectedException);
        } finally {
            myManagedSecureCharBuffer.close();
        }
    }

    @Test(dataProvider = CharBufferProvider.EQUALS_DATA_PROVIDER_NAME, dataProviderClass = CharBufferProvider.class)
    public void testEqual(final CharBufferProvider.Parameters parParameters) throws IOException {
        final IWritableCharSequence myManagedSecureCharBuffer = new StringBuilderBuffer(parParameters.capacity);
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
}
