package com.chelseaurquhart.securejson;

import org.testng.annotations.DataProvider;

import java.lang.reflect.Method;

public final class StringProvider {
    static final String DATA_PROVIDER_NAME = "StringProvider";

    private StringProvider() {
    }

    /**
     * This provider is consumed by the number parser as well as the json reader. The json reader tests are much less
     * strict than those of the number parser, but it confirms that numbers read (and fail to read) appropriately.
     *
     * @param parMethod The method being executed.
     * @return A collection of parameters.
     */
    @DataProvider(name = DATA_PROVIDER_NAME, parallel = true)
    public static Object[] dataProvider(final Method parMethod) {
        return new Object[]{
            buildParameters(
                "simple string",
                "\"simple string\"",
                "simple string"
            ),
            buildParameters(
                "string with escaped backslash",
                "\"simple \\\\string\"",
                "simple \\string"
            ),
            buildParameters(
                "string with quotes",
                "\"simple \\\"string\"",
                "simple \"string"
            ),
        };
    }

    private static Parameters buildParameters(final String parTestName, final CharSequence parInputString,
                                              final String parExpected) {

        return new Parameters(
            parTestName,
            newSecureCharBuffer(parInputString),
            parExpected
        );
    }

    private static CharSequence newSecureCharBuffer(final CharSequence parInput) {
        if (parInput == null || parInput.length() == 0) {
            return null;
        }

        final ManagedSecureCharBuffer mySecureBuffer = new ManagedSecureCharBuffer(parInput.length(),
            Settings.DEFAULTS);
        mySecureBuffer.append(parInput);

        return mySecureBuffer;
    }

    static class Parameters {
        final String testName;
        final CharSequence inputString;
        final CharSequence expected;
        Exception expectedException;

        Parameters(final String parTestName, final CharSequence parInputString, final CharSequence parExpected) {
            this.testName = parTestName;
            this.inputString = parInputString;
            this.expected = parExpected;
        }

        Parameters exception(final Exception parException) {
            this.expectedException = parException;
            return this;
        }

        @Override
        public String toString() {
            return testName;
        }
    }
}
