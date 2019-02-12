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


import org.testng.annotations.DataProvider;

import java.io.IOException;
import java.lang.reflect.Method;

/**
 * Provider of textual data, used by multiple test suites.
 */
@SuppressWarnings({"PMD.CommentRequired"})
public final class CharBufferProvider {
    static final String SUBSEQUENCE_DATA_PROVIDER_NAME = "CharBufferProviderTestCharSequence";
    static final String EQUALS_DATA_PROVIDER_NAME = "CharBufferProviderTestEquals";

    private CharBufferProvider() {
    }

    @DataProvider(name = SUBSEQUENCE_DATA_PROVIDER_NAME)
    static Object[] dataProvider(final Method parMethod)  {
        return new Object[]{
            new Parameters(
                "simple single byte buffer",
                4,
                new IConsumer<IWritableCharSequence>() {
                    @Override
                    public void accept(final IWritableCharSequence parInput) {
                        append(parInput, 'a', 'b');
                    }
                },
                "a",
                0,
                1,
                null
            ),
            new Parameters(
                "simple single string",
                4,
                new IConsumer<IWritableCharSequence>() {
                    @Override
                    public void accept(final IWritableCharSequence parInput) {
                        append(parInput, "ab");
                    }
                },
                "a",
                0,
                1,
                null
            ),
            new Parameters(
                "simple single bytes 1.5x capacity",
                2,
                new IConsumer<IWritableCharSequence>() {
                    @Override
                    public void accept(final IWritableCharSequence parInput) {
                        append(parInput, 'a', 'b', 'c');
                    }
                },
                "ab",
                0,
                2,
                null
            ),
            new Parameters(
                "simple single bytes 1.5x capacity expect full",
                2,
                new IConsumer<IWritableCharSequence>() {
                    @Override
                    public void accept(final IWritableCharSequence parInput) {
                        append(parInput, 'a', 'b', 'c');
                    }
                },
                "abc",
                0,
                3,
                null
            ),
            new Parameters(
                "simple single bytes 1.5x capacity expect full - first",
                2,
                new IConsumer<IWritableCharSequence>() {
                    @Override
                    public void accept(final IWritableCharSequence parInput) {
                        append(parInput, 'a', 'b', 'c');
                    }
                },
                "bc",
                1,
                3,
                null
            ),
            new Parameters(
                "starts in second buffer, consumes middle",
                3,
                new IConsumer<IWritableCharSequence>() {
                    @Override
                    public void accept(final IWritableCharSequence parInput) {
                        append(parInput, 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i');
                    }
                },
                "def",
                3,
                6,
                null
            ),
            new Parameters(
                "starts in second buffer, consumes part of middle",
                3,
                new IConsumer<IWritableCharSequence>() {
                    @Override
                    public void accept(final IWritableCharSequence parInput) {
                        append(parInput, 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i');
                    }
                },
                "de",
                3,
                5,
                null
            ),
            new Parameters(
                "starts in second buffer (last char), consumes next 1",
                3,
                new IConsumer<IWritableCharSequence>() {
                    @Override
                    public void accept(final IWritableCharSequence parInput) {
                        append(parInput, 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i');
                    }
                },
                "f",
                5,
                6,
                null
            ),
            new Parameters(
                "starts in second buffer (last char), consumes next 2",
                3,
                new IConsumer<IWritableCharSequence>() {
                    @Override
                    public void accept(final IWritableCharSequence parInput) {
                        append(parInput, 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i');
                    }
                },
                "fg",
                5,
                7,
                null
            ),
            new Parameters(
                "starts in third buffer (first char), consumes next 1",
                3,
                new IConsumer<IWritableCharSequence>() {
                    @Override
                    public void accept(final IWritableCharSequence parInput) {
                        append(parInput, 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i');
                    }
                },
                "g",
                6,
                7,
                null
            ),
            new Parameters(
                "starts in third buffer (first char), consumes next 2",
                3,
                new IConsumer<IWritableCharSequence>() {
                    @Override
                    public void accept(final IWritableCharSequence parInput) {
                        append(parInput, 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i');
                    }
                },
                "gh",
                6,
                8,
                null
            ),
            new Parameters(
                "charsequences, 3 buffers, empty middle, consume full",
                3,
                new IConsumer<IWritableCharSequence>() {
                    @Override
                    public void accept(final IWritableCharSequence parInput) {
                        append(parInput, "a", "", "c");
                    }
                },
                "ac",
                0,
                2,
                null
            ),
            new Parameters(
                "charsequences, 3 buffers, empty middle, consume first",
                3,
                new IConsumer<IWritableCharSequence>() {
                    @Override
                    public void accept(final IWritableCharSequence parInput) {
                        append(parInput, "a", "", "c");
                    }
                },
                "a",
                0,
                1,
                null
            ),
            new Parameters(
                "charsequences, 3 buffers, empty middle, consume last",
                3,
                new IConsumer<IWritableCharSequence>() {
                    @Override
                    public void accept(final IWritableCharSequence parInput) {
                        append(parInput, "a", "", "c");
                    }
                },
                "c",
                1,
                2,
                null
            ),
            new Parameters(
                "start == end",
                3,
                new IConsumer<IWritableCharSequence>() {
                    @Override
                    public void accept(final IWritableCharSequence parInput) {
                        append(parInput, "a", "", "c");
                    }
                },
                "",
                1,
                1,
                null
            ),
            new Parameters(
                "start < 0",
                3,
                new IConsumer<IWritableCharSequence>() {
                    @Override
                    public void accept(final IWritableCharSequence parInput) {
                        append(parInput, "a");
                    }
                },
                "",
                -1,
                1,
                "start must be >=0 and and end >= start"
            ),
            new Parameters(
                "end < start",
                3,
                new IConsumer<IWritableCharSequence>() {
                    @Override
                    public void accept(final IWritableCharSequence parInput) {
                        append(parInput, "a");
                    }
                },
                "",
                1,
                0,
                "start must be >=0 and and end >= start"
            ),
            new Parameters(
                "end > total length",
                3,
                new IConsumer<IWritableCharSequence>() {
                    @Override
                    public void accept(final IWritableCharSequence parInput) {
                        append(parInput, "a");
                    }
                },
                "",
                0,
                2,
                "buffer overflow detected"
            ),
        };
    }

    @DataProvider(name = EQUALS_DATA_PROVIDER_NAME)
    static Object[] equalsDataProvider() {
        return new Object[]{
            new Parameters(
                "empty string",
                new IConsumer<IWritableCharSequence>() {
                        @Override
                        public void accept(final IWritableCharSequence parInput) {
                            // NOOP
                        }
                    }, "", true),
            new Parameters(
                "simple string",
                new IConsumer<IWritableCharSequence>() {
                        @Override
                        public void accept(final IWritableCharSequence parInput) {
                            append(parInput, "123");
                        }
                    }, "123", true),
            new Parameters(
                "simple char string",
                new IConsumer<IWritableCharSequence>() {
                        @Override
                        public void accept(final IWritableCharSequence parInput) {
                            append(parInput, '1', '2', '3');
                        }
                    }, "123", true),
            new Parameters(
                "simple char string with an extra char",
                new IConsumer<IWritableCharSequence>() {
                        @Override
                        public void accept(final IWritableCharSequence parInput) {
                            append(parInput, '1', '2', '3');
                        }
                    }, "1234", false),
            new Parameters(
                "null string",
                new IConsumer<IWritableCharSequence>() {
                        @Override
                        public void accept(final IWritableCharSequence parInput) {
                            append(parInput, '1', '2', '3');
                        }
                    }, null, false),
            new Parameters(
                "null string to empty buffer",
                new IConsumer<IWritableCharSequence>() {
                        @Override
                        public void accept(final IWritableCharSequence parInput) {
                            // NOOP
                        }
                    }, null, false),
            new Parameters(
                "assortment of bytes, strings, and empty strings",
                new IConsumer<IWritableCharSequence>() {
                        @Override
                        public void accept(final IWritableCharSequence parInput) {
                            append(parInput, '1', '2', '3');
                            append(parInput, "123");
                            append(parInput, '1', '2', '3');
                            append(parInput, "", "", "", "");
                        }
                    }, "123123123", true),
        };
    }

    static void append(final IWritableCharSequence parInput, final char... parChars) {
        for (final char myChar : parChars) {
            try {
                parInput.append(myChar);
            } catch (final IOException myException) {
                throw new RuntimeException(myException);
            }
        }
    }

    static void append(final IWritableCharSequence parInput, final String... parStrings) {
        for (final String myString : parStrings) {
            try {
                parInput.append(myString);
            } catch (final IOException myException) {
                throw new RuntimeException(myException);
            }
        }
    }

    static boolean isEqual(final CharSequence parLhs, final CharSequence parRhs) {
        if (parLhs == null || parRhs == null) {
            return parLhs == parRhs;
        }

        final int myLength = parLhs.length();
        if (parRhs.length() != myLength) {
            return false;
        }

        for (int myIndex = 0; myIndex < myLength; myIndex++) {
            if (parLhs.charAt(myIndex) != parRhs.charAt(myIndex)) {
                return false;
            }
        }

        return true;
    }

    static final class Parameters {
        final String testName;
        final CharSequence expected;
        final int capacity;
        final IConsumer<IWritableCharSequence> consumer;
        int start;
        int end;
        String expectedException;
        final boolean expectedEquals;

        Parameters(final String parTestName, final int parCapacity,
                   final IConsumer<IWritableCharSequence> parWriterConsumer, final CharSequence parExpected,
                   final int parStart, final int parEnd, final String parExpectedException) {
            testName = parTestName;
            consumer = parWriterConsumer;
            capacity = parCapacity;
            expected = parExpected;
            start = parStart;
            end = parEnd;
            expectedException = parExpectedException;
            expectedEquals = false;
        }

        Parameters(final String parTestName, final IConsumer<IWritableCharSequence> parWriterConsumer,
                   final CharSequence parExpected, final boolean parExpectedEquals) {
            consumer = parWriterConsumer;
            testName = parTestName;
            capacity = 512;
            expected = parExpected;
            expectedEquals = parExpectedEquals;
        }

        @Override
        public String toString() {
            return testName;
        }
    }
}
