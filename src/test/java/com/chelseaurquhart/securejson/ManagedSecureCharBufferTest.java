package com.chelseaurquhart.securejson;

import com.chelseaurquhart.securejson.util.StringUtil;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;
import java.lang.reflect.Method;

public final class ManagedSecureCharBufferTest {
    private static final String SUBSEQUENCE_DATA_PROVIDER_NAME = "ManagedSecureCharBufferTestCharSequence";
    @DataProvider(name = SUBSEQUENCE_DATA_PROVIDER_NAME)
    private static Object[] dataProvider(final Method parMethod)  {
        return new Object[]{
            new Parameters(
                "simple single byte buffer",
                4,
                new IConsumer<ManagedSecureCharBuffer>() {
                    @Override
                    public void accept(final ManagedSecureCharBuffer parInput) {
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
                new IConsumer<ManagedSecureCharBuffer>() {
                    @Override
                    public void accept(final ManagedSecureCharBuffer parInput) {
                        parInput.append("ab");
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
                new IConsumer<ManagedSecureCharBuffer>() {
                    @Override
                    public void accept(final ManagedSecureCharBuffer parInput) {
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
                new IConsumer<ManagedSecureCharBuffer>() {
                    @Override
                    public void accept(final ManagedSecureCharBuffer parInput) {
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
                new IConsumer<ManagedSecureCharBuffer>() {
                    @Override
                    public void accept(final ManagedSecureCharBuffer parInput) {
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
                new IConsumer<ManagedSecureCharBuffer>() {
                    @Override
                    public void accept(final ManagedSecureCharBuffer parInput) {
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
                new IConsumer<ManagedSecureCharBuffer>() {
                    @Override
                    public void accept(final ManagedSecureCharBuffer parInput) {
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
                new IConsumer<ManagedSecureCharBuffer>() {
                    @Override
                    public void accept(final ManagedSecureCharBuffer parInput) {
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
                new IConsumer<ManagedSecureCharBuffer>() {
                    @Override
                    public void accept(final ManagedSecureCharBuffer parInput) {
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
                new IConsumer<ManagedSecureCharBuffer>() {
                    @Override
                    public void accept(final ManagedSecureCharBuffer parInput) {
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
                new IConsumer<ManagedSecureCharBuffer>() {
                    @Override
                    public void accept(final ManagedSecureCharBuffer parInput) {
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
                new IConsumer<ManagedSecureCharBuffer>() {
                    @Override
                    public void accept(final ManagedSecureCharBuffer parInput) {
                        parInput.append("a");
                        parInput.append("");
                        parInput.append("c");
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
                new IConsumer<ManagedSecureCharBuffer>() {
                    @Override
                    public void accept(final ManagedSecureCharBuffer parInput) {
                        parInput.append("a");
                        parInput.append("");
                        parInput.append("c");
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
                new IConsumer<ManagedSecureCharBuffer>() {
                    @Override
                    public void accept(final ManagedSecureCharBuffer parInput) {
                        parInput.append("a");
                        parInput.append("");
                        parInput.append("c");
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
                new IConsumer<ManagedSecureCharBuffer>() {
                    @Override
                    public void accept(final ManagedSecureCharBuffer parInput) {
                        parInput.append("a");
                        parInput.append("");
                        parInput.append("c");
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
                new IConsumer<ManagedSecureCharBuffer>() {
                    @Override
                    public void accept(final ManagedSecureCharBuffer parInput) {
                        parInput.append("a");
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
                new IConsumer<ManagedSecureCharBuffer>() {
                    @Override
                    public void accept(final ManagedSecureCharBuffer parInput) {
                        parInput.append("a");
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
                new IConsumer<ManagedSecureCharBuffer>() {
                    @Override
                    public void accept(final ManagedSecureCharBuffer parInput) {
                        parInput.append("a");
                    }
                },
                "",
                0,
                2,
                "buffer overflow detected"
            ),
        };
    }

    @Test
    public void testFullBuffer() throws IOException {
        try (final ManagedSecureCharBuffer myManagedSecureCharBuffer = new ManagedSecureCharBuffer(4,
                Settings.DEFAULTS)) {
            myManagedSecureCharBuffer.append('t');
            myManagedSecureCharBuffer.append('e');
            myManagedSecureCharBuffer.append('s');
            myManagedSecureCharBuffer.append('t');
            Assert.assertEquals("test", StringUtil.charSequenceToString(myManagedSecureCharBuffer));
        }
    }

    @Test
    public void testOverflowBuffer() throws IOException {
        try (final ManagedSecureCharBuffer myManagedSecureCharBuffer = new ManagedSecureCharBuffer(4,
                Settings.DEFAULTS)) {
            myManagedSecureCharBuffer.append('t');
            myManagedSecureCharBuffer.append('e');
            myManagedSecureCharBuffer.append('s');
            myManagedSecureCharBuffer.append('t');
            myManagedSecureCharBuffer.append('2');
            Assert.assertEquals("test2", StringUtil.charSequenceToString(myManagedSecureCharBuffer));
        }
    }

    @Test
    public void testUnderflowBuffer() throws IOException {
        try (final ManagedSecureCharBuffer myManagedSecureCharBuffer = new ManagedSecureCharBuffer(4,
                Settings.DEFAULTS)) {
            myManagedSecureCharBuffer.append('t');
            myManagedSecureCharBuffer.append('e');
            Assert.assertEquals("te", StringUtil.charSequenceToString(myManagedSecureCharBuffer));
        }
    }

    @Test
    public void testCharSequenceByReference() throws IOException {
        try (final ManagedSecureCharBuffer myManagedSecureCharBuffer = new ManagedSecureCharBuffer(4,
                Settings.DEFAULTS)) {
            final MutatableString myCharSequence = new MutatableString();

            myCharSequence.string = "TEST";
            myManagedSecureCharBuffer.append(myCharSequence);

            Assert.assertEquals(StringUtil.charSequenceToString(myManagedSecureCharBuffer), "TEST");
            myCharSequence.string = "TEST2";
            Assert.assertEquals(StringUtil.charSequenceToString(myManagedSecureCharBuffer), "TEST2");
        }
    }

    @Test
    public void testCharSequenceAndBytesMixedClose() throws IOException {
        final ManagedSecureCharBuffer myManagedSecureCharBuffer = new ManagedSecureCharBuffer(4,
                Settings.DEFAULTS);

        myManagedSecureCharBuffer.append('a');
        myManagedSecureCharBuffer.append('b');
        myManagedSecureCharBuffer.append("test");
        myManagedSecureCharBuffer.append('c');
        myManagedSecureCharBuffer.append("test2");
        myManagedSecureCharBuffer.append('d');

        Assert.assertEquals(StringUtil.charSequenceToString(myManagedSecureCharBuffer), "abtestctest2d");
        myManagedSecureCharBuffer.close();
        // after closing all should be empty
        Assert.assertEquals(StringUtil.charSequenceToString(myManagedSecureCharBuffer), "");
    }

    @Test(expectedExceptions = UnsupportedOperationException.class)
    public void testToStringException() throws IOException {
        try (final ManagedSecureCharBuffer myManagedSecureCharBuffer = new ManagedSecureCharBuffer(4,
                Settings.DEFAULTS)) {
            myManagedSecureCharBuffer.append('a');
            myManagedSecureCharBuffer.append('b');
            myManagedSecureCharBuffer.toString();
        }
    }

    @Test(dataProvider = SUBSEQUENCE_DATA_PROVIDER_NAME)
    public void testSubSequence(final Parameters parParameters) throws IOException {
        try (final ManagedSecureCharBuffer myManagedSecureCharBuffer = parParameters.managedSecureCharBuffer) {
            try {
                final CharSequence mySequence = myManagedSecureCharBuffer.subSequence(
                    parParameters.start, parParameters.end);
                Assert.assertEquals(StringUtil.charSequenceToString(mySequence), parParameters.expected);
                Assert.assertNull(parParameters.expectedException);
            } catch (final ArrayIndexOutOfBoundsException myException) {
                Assert.assertNotNull(parParameters.expectedException);
                Assert.assertEquals(myException.getMessage(), parParameters.expectedException);
            }
        }
    }

    private static void append(final ManagedSecureCharBuffer parInput, final char... parChars) {
        for (final char myChar : parChars) {
            try {
                parInput.append(myChar);
            } catch (final IOException myException) {
                throw new RuntimeException(myException);
            }
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

    private static final class Parameters {
        private String testName;
        private ManagedSecureCharBuffer managedSecureCharBuffer;
        private CharSequence expected;
        private int start;
        private int end;
        private String expectedException;

        private Parameters(final String parTestName, final int parCapacity,
                           final IConsumer<ManagedSecureCharBuffer> parManagedSecureCharBufferConsumer,
                           final CharSequence parExpected, final int parStart, final int parEnd,
                           final String parExpectedException) {
            testName = parTestName;
            managedSecureCharBuffer = new ManagedSecureCharBuffer(parCapacity, Settings.DEFAULTS);
            parManagedSecureCharBufferConsumer.accept(managedSecureCharBuffer);
            expected = parExpected;
            start = parStart;
            end = parEnd;
            expectedException = parExpectedException;
        }

        @Override
        public String toString() {
            return testName;
        }
    }
}
