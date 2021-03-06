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

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
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
     * @throws IOException If we failed to close an input stream.
     */
    @SuppressWarnings({"PMD.DoNotCallSystemExit", "PMD.DataflowAnomalyAnalysis", "PMD.SystemPrintln"})
    public static void main(final String[] parArgs) throws IOException {
        if (parArgs.length == 0) {
            System.out.println("Usage: java Verify ...file.json");

            System.exit(2);
        }

        int myFinalExitCode = 0;
        final boolean myIsBatch = System.getProperty(PROPERTY_MODE_KEY, "").equals(MODE_BATCH);
        int myFailCount = 0;
        for (final String myFilename : parArgs) {
            Status myStatus;

            System.out.printf("%s: ", myFilename);
            System.out.flush();

            InputStream myStream = null;
            try {
                myStream = new FileInputStream(myFilename);

                if (isValidJSON(myStream, !myIsBatch)) {
                    myStatus = Status.OK;
                } else {
                    myStatus = Status.FAIL;
                }
            } catch (final IOException myException) {
                if (myStream != null) {
                    myStream.close();
                }
                myStatus = Status.NOT_FOUND;
            }

            int myExitCode = 0;

            if (myIsBatch) {
                final Matcher myMatch = FILE_TYPE_PATTERN.matcher(myFilename);
                if (!myMatch.matches()) {
                    System.out.printf("Bad filename: %s%n", myFilename);
                    System.exit(-1);
                }
                switch (myMatch.group(1).charAt(0)) {
                    case 'y':
                        if (myStatus != Status.OK) {
                            myExitCode = 1;
                            myFailCount++;
                        }
                        break;
                    case 'n':
                        if (myStatus == Status.OK) {
                            myExitCode = 1;
                            myFailCount++;
                        }
                        break;
                    case 'i':
                        // technically these are optional, but we support all of the cases.
                        if (myStatus != Status.OK) {
                            myExitCode = 1;
                            myFailCount++;
                        }
                        break;
                    default:
                        System.out.printf("Invalid type: %s%n", myMatch.group(1));
                        System.exit(-1);
                        break;
                }
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
                    break;
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
                        break;
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

    @SuppressWarnings("PMD.SystemPrintln")
    private static boolean isValidJSON(final InputStream parValue, final boolean parEnableConsole) {
        try {
            new SecureJSON().fromJSON(parValue, new IConsumer<Object>() {
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
