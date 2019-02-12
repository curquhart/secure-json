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

import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;
import java.math.MathContext;

@SuppressWarnings("PMD.CommentRequired")
public final class NumberReaderTest {
    @Test(dataProviderClass = NumberProvider.class, dataProvider = NumberProvider.DATA_PROVIDER_NAME)
    public void testRead(final NumberProvider.Parameters<?> parParameters) {
        try {
            final Number myNumber = charSequenceToNumber(parParameters.number, parParameters.mathContext);
            Assert.assertNull(parParameters.expectedException);
            Assert.assertSame(myNumber.getClass(), parParameters.expectedNumberClass);
            Assert.assertEquals(myNumber, parParameters.expected);
        } catch (final IOException myException) {
            Assert.assertNotNull(parParameters.expectedException, myException.getMessage());
            Assert.assertEquals(myException.getMessage(), parParameters.expectedException.getMessage());
            Assert.assertEquals(myException.getClass(), parParameters.expectedException.getClass());
        } catch (final JSONException myException) {
            Assert.assertNotNull(parParameters.expectedException, myException.getMessage());
            Assert.assertEquals(myException.getMessage(), parParameters.expectedException.getMessage());
            Assert.assertEquals(myException.getClass(), parParameters.expectedException.getClass());
        }
    }

    @Test
    public void testDefaults() throws IOException, JSONException {
        Assert.assertEquals(220, charSequenceToNumber("22e1", NumberReader.DEFAULT_MATH_CONTEXT));
    }

    private Number charSequenceToNumber(final CharSequence parNumber, final MathContext parMathContext)
            throws IOException, JSONException {
        return new NumberReader(parMathContext, Settings.DEFAULTS).charSequenceToNumber(parNumber, 0);
    }

    @Test(expectedExceptions = NotImplementedException.class)
    public void testAddValue() throws IOException, JSONException {
        final IReader<?> myNumberReader = new NumberReader(Settings.DEFAULTS);
        myNumberReader.addValue(null, null, null);
    }
}
