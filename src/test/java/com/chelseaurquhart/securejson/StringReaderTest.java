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

import com.chelseaurquhart.securejson.JSONException.JSONRuntimeException;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.CharBuffer;
import java.util.Calendar;

@SuppressWarnings("PMD.CommentRequired")
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
        } catch (final IOException myException) {
            Assert.assertNotNull(parParameters.expectedException, myException.getMessage());
            Assert.assertEquals(myException.getMessage(), parParameters.expectedException.getMessage());
            Assert.assertEquals(myException.getClass(), parParameters.expectedException.getClass());
        } catch (final JSONException myException) {
            Assert.assertNotNull(parParameters.expectedException, myException.getMessage());
            Assert.assertEquals(myException.getMessage(), parParameters.expectedException.getMessage());
            Assert.assertEquals(myException.getClass(), parParameters.expectedException.getClass());
        } catch (final JSONRuntimeException myException) {
            Assert.assertNotNull(parParameters.expectedException, myException.getMessage());
            Assert.assertEquals(myException.getMessage(), parParameters.expectedException.getMessage());
            Assert.assertEquals(myException.getClass(), parParameters.expectedException.getClass());
        }
    }

    @Test
    public void testGetSymbolType() {
        Assert.assertEquals(IReader.SymbolType.UNKNOWN, new StringReader(Settings.DEFAULTS).getSymbolType(null));
    }

    @Test
    public void testNormalizeCollectionAlwaysReturnsInput() {
        final StringReader myReader = new StringReader(Settings.DEFAULTS);
        Assert.assertNull(myReader.normalizeCollection(null));
        Assert.assertEquals(123, myReader.normalizeCollection(123));
        final Calendar myCalendar = Calendar.getInstance();
        Assert.assertSame(myCalendar, myReader.normalizeCollection(myCalendar));
    }

    @Test(expectedExceptions = NotImplementedException.class)
    public void testAddValue() throws IOException, JSONException {
        final IReader<?> myStringReader = new StringReader(Settings.DEFAULTS);
        myStringReader.addValue(null, null, null);
    }
}
