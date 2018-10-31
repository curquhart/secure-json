package com.chelseaurquhart.securejson;

import com.chelseaurquhart.securejson.util.StringUtil;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Verify interface for testing JSON strings.
 */
public final class Verify {
    private static final String PROPERTY_MODE_KEY = "verifyRunMode";
    private static final String MODE_BATCH = "batch";
    private static final Pattern FILE_TYPE_PATTERN = Pattern.compile(".*/([niy])_.*\\.json$");

    private Verify() {
    }

    /**
     * Main entry point.
     *
     * @param parArgs The program arguments to use.
     */
    public static void main(final String[] parArgs) {
        if (parArgs.length == 0) {
            System.out.println("Usage: java Verify ...file.json");
            System.exit(2);
        }

        int myFinalExitCode = 0;
        final boolean myIsBatch = System.getProperty(PROPERTY_MODE_KEY, "").equals(MODE_BATCH);
        int myFailCount = 0;
        for (final String myFilename : parArgs) {
            Status myStatus;

            try {
                final byte[] myValue = Files.readAllBytes(Paths.get(myFilename));
                if (isValidJSON(myValue, !myIsBatch)) {
                    myStatus = Status.OK;
                } else {
                    myStatus = Status.FAIL;
                }
            } catch (final IOException myException) {
                myStatus = Status.NOT_FOUND;
            }

            int myExitCode = 0;

            if (myIsBatch) {
                final Matcher myMatch = FILE_TYPE_PATTERN.matcher(myFilename);
                if (!myMatch.matches()) {
                    System.out.printf("Bad filename: %s%n", myFilename);
                    System.exit(-1);
                }
                switch (myMatch.group(1)) {
                    case "y":
                        if (myStatus != Status.OK) {
                            myExitCode = 1;
                            myFailCount++;
                        }
                        break;
                    case "n":
                        if (myStatus == Status.OK) {
                            myExitCode = 1;
                            myFailCount++;
                        }
                        break;
                    case "i":
                        // technically these are optional, but we support all of the cases.
                        if (myStatus != Status.OK) {
                            myExitCode = 1;
                            myFailCount++;
                        }
                        break;
                    default:
                        System.out.printf("Invalid type: %s%n", myMatch.group(1));
                        System.exit(-1);
                }
                System.out.printf("%s: ", myFilename);
            }

            switch (myStatus) {
                case OK:
                    System.out.print("valid");
                    break;
                case FAIL:
                    System.out.print("invalid");
                    break;
                case NOT_FOUND:
                    System.out.print("not found");
                    break;
                default:
                    System.out.printf("Invalid status: %s%n", myStatus);
                    System.exit(-1);
            }

            if (myIsBatch) {
                if (myExitCode == 0) {
                    System.out.println(" \u2705");
                } else {
                    System.out.println(" \u274c");
                }
            } else {
                switch (myStatus) {
                    case OK:
                        break;
                    case FAIL:
                        myExitCode = 1;
                        break;
                    case NOT_FOUND:
                        myExitCode = 2;
                        break;
                    default:
                        System.out.printf("Invalid status: %s%n", myStatus);
                        System.exit(-1);
                }
            }

            myFinalExitCode = Math.max(myFinalExitCode, myExitCode);
        }

        if (myIsBatch) {
            System.out.printf("Total: %d. Failed: %d. Succeeded: %d%n", parArgs.length, myFailCount,
                parArgs.length - myFailCount);
        }
        System.exit(myFinalExitCode);
    }

    private static boolean isValidJSON(final byte[] parValue, final boolean parEnableConsole) {
        try {
            new SecureJSON().fromJSON(new ByteArrayInputStream(parValue), new IConsumer<Object>() {
                @Override
                public void accept(final Object parInput) {
                    if (parEnableConsole) {
                        System.out.println(StringUtil.deepCharSequenceToString(parInput));
                    }
                    if (parInput != null && parEnableConsole) {
                        System.out.println(parInput.getClass().getSimpleName());
                    }
                }
            });

            return true;
        } catch (final JSONException myException) {
            return false;
        }
    }

    private enum Status {
        OK,
        FAIL,
        NOT_FOUND
    }
}
