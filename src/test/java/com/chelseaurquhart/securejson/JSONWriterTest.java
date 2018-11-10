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
import com.chelseaurquhart.securejson.JSONEncodeException.InvalidTypeException;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;

public final class JSONWriterTest {
    static final String DATA_PROVIDER_NAME = "JSONWriterTest";

    @DataProvider(name = DATA_PROVIDER_NAME, parallel = true)
    private static Object[] dataProvider(final Method parMethod) {
        return new Object[]{
            new Parameters(
                "null",
                null,
                "null"
            ),
            new Parameters(
                "true",
                true,
                "true"
            ),
            new Parameters(
                "false",
                false,
                "false"
            ),
            new Parameters(
                "empty array",
                new ArrayList<>(),
                "[]"
            ),
            new Parameters(
                "empty set",
                new HashSet<>(),
                "[]"
            ),
            new Parameters(
                "empty array",
                new Object[0],
                "[]"
            ),
            new Parameters(
                "array with numbers",
                new Object[]{0, 1, 2},
                "[0,1,2]"
            ),
            new Parameters(
                "empty map",
                new LinkedHashMap<>(),
                "{}"
            ),
            new Parameters(
                "nesting",
                new LinkedHashMap<String, Object>() {{
                        put("a", new ArrayList<Object>() {{
                                add(new HashMap<String, Object>() {{
                                        put("x", 12);
                                    }});
                            }});
                    }},
                "{\"a\":[{\"x\":12}]}"
            ),
            new Parameters(
                "emoji",
                "\uD83D\uDE0D",
                "\"\\ud83d\\ude0d\""
            ),
            new Parameters(
                "escaped emoji",
                "\\uD83D\\uDE0D",
                "\"\\\\uD83D\\\\uDE0D\""
            ),
            new Parameters(
                "special escapes",
                "\"\\/\r\b\n\f",
                "\"\\\"\\\\\\/\\r\\b\\n\\f\""
            ),
            new Parameters(
                "scientific notation",
                1.0e43,
                "1.0E43"
            ),
        };
    }

    @Test(dataProvider = DATA_PROVIDER_NAME)
    public void testWrite(final Parameters parParameters) throws IOException, InvalidTypeException {
        try (final JSONWriter myWriter = new JSONWriter(Settings.DEFAULTS)) {
            Assert.assertEquals(StringUtil.deepCharSequenceToString(myWriter.write(parParameters.inputObject)),
                StringUtil.deepCharSequenceToString(parParameters.expected));
        }
    }

    static class Parameters {
        private String testName;
        private CharSequence expected;
        private Object inputObject;

        Parameters(final String parTestName, final Object parObject, final CharSequence parExpected) {
            testName = parTestName;
            inputObject = parObject;
            expected = parExpected;
        }

        public String getTestName() {
            return testName;
        }

        public CharSequence getExpected() {
            return expected;
        }

        public Object getInputObject() {
            return inputObject;
        }

        @Override
        public String toString() {
            return testName;
        }
    }
}
