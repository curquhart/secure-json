package com.chelseaurquhart.securejson;

import com.chelseaurquhart.securejson.util.StringUtil;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Verify interface for testing JSON strings.
 */
public final class Verify {
    private Verify() {
    }

    /**
     * Main entry point.
     *
     * @param parArgs The program arguments to use.
     */
    public static void main(final String[] parArgs) {
        if (parArgs.length == 0) {
            System.out.println("Usage: java TestJSONParsing file.json");
            System.exit(2);
        }

        try {
            final byte[] myValue = Files.readAllBytes(Paths.get(parArgs[0]));
            if (isValidJSON(myValue)) {
                System.out.println("valid");
                System.exit(0);
            }
            System.out.println("invalid");
            System.exit(1);
        } catch (final IOException myException) {
            System.out.println("not found");
            System.exit(2);
        }
    }

    private static boolean isValidJSON(final byte[] parValue) {
        try {
            SecureJSON.fromJSON(new ByteArrayInputStream(parValue), new IConsumer<Object>() {
                @Override
                public void accept(final Object parInput) {
                    System.out.println(StringUtil.deepCharSequenceToString(parInput));
                    if (parInput != null) {
                        System.out.println(parInput.getClass().getSimpleName());
                    }
                }
            });

            return true;
        } catch (final JSONDecodeException myException) {
            System.out.println(myException);
            return false;
        }
    }
}
