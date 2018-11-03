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

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;

public final class WordReaderTest {
    private WordReaderTest() {
    }

    private static final String DATA_PROVIDER_NAME = "WordReaderTest";

    @DataProvider(name = DATA_PROVIDER_NAME)
    Object[] dataProvider() throws IOException {
        return new Object[]{
            new Parameters(
                "null string",
                "null",
                null,
                null
            ),
            new Parameters(
                "null string capital first",
                "NULL",
                null,
                new JSONDecodeException.InvalidTokenException(new PresetIterableCharSequence(0))
            ),
            new Parameters(
                "null string capital second",
                "nULL",
                null,
                new JSONDecodeException.InvalidTokenException(new PresetIterableCharSequence(1))
            ),
            new Parameters(
                "false string",
                "false",
                false,
                null
            ),
            new Parameters(
                "true string",
                "true",
                true,
                null
            ),
            new Parameters(
                "bad token at start",
                "Btrue",
                null,
                new JSONDecodeException.InvalidTokenException(new PresetIterableCharSequence(0))
            ),
            new Parameters(
                "bad token in middle",
                "trAe",
                null,
                new JSONDecodeException.InvalidTokenException(new PresetIterableCharSequence(2))
            ),
            new Parameters(
                "bad token at end",
                "trub",
                null,
                new JSONDecodeException.InvalidTokenException(new PresetIterableCharSequence(3))
            ),
            new Parameters(
                "bad token after",
                "truer",
                null,
                new JSONDecodeException.InvalidTokenException(new PresetIterableCharSequence(4))
            ),
        };
    }

    @Test(dataProvider = DATA_PROVIDER_NAME)
    public void testRead(final Parameters parParameters) {
        final WordReader myWordReader = new WordReader();
        try {
            Assert.assertEquals(myWordReader.read(new IterableCharSequence(parParameters.input)),
                parParameters.expected);
            Assert.assertNull(parParameters.expectedException);
        } catch (final IOException myException) {
            Assert.assertNotNull(parParameters.expectedException, myException.getMessage());
            Assert.assertEquals(myException.getMessage(), parParameters.expectedException.getMessage());
            Assert.assertEquals(myException.getClass(), parParameters.expectedException.getClass());
        }
    }

    private static final class Parameters {
        private final String testName;
        private final CharSequence input;
        private final Object expected;
        private final Exception expectedException;

        private Parameters(final String parTestName, final CharSequence parInput, final Object parExpected,
                           final Exception parExceptedException) {
            testName = parTestName;
            input = parInput;
            expected = parExpected;
            expectedException = parExceptedException;
        }

        @Override
        public String toString() {
            return testName;
        }
    }
}
