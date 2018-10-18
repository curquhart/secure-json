package com.chelseaurquhart.securejson;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.MathContext;
import java.math.RoundingMode;
import java.nio.CharBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * CLI interface for testing JSON strings.
 */
public final class CLI {
    private static final MathContext MATH_CONTEXT = new MathContext((int) Math.pow(2, 17), RoundingMode.HALF_UP);

    private CLI() {
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
        } catch (IOException e) {
            System.out.println("not found");
            System.exit(2);
        }
    }

    private static boolean isValidJSON(final byte[] parValue) throws IOException {
        try {
            JSONReader myReader = new JSONReader(new NumberReader(MATH_CONTEXT));
            Object myObj = deepCharSequenceToString(myReader.read(new ByteArrayInputStream(parValue)));
            System.out.println(myObj);
            if (myObj != null) {
                System.out.println(myObj.getClass().getSimpleName());
            }
            return true;
        } catch (JSONDecodeException e) {
            System.out.println(e);
            return false;
        }
    }

    private static String charSequenceToString(final CharSequence parInput) {
        final char[] myChars = new char[parInput.length()];
        CharBuffer.wrap(parInput).get(myChars);

        return new String(myChars);
    }

    private static Object deepCharSequenceToString(final Object parInput) {
        if (parInput == null) {
            return null;
        } else if (parInput instanceof CharSequence) {
            return charSequenceToString((CharSequence) parInput);
        } else if (parInput instanceof Map) {
            final Map myInputMap = (Map) parInput;

            final Map<String, Object> myInputMapCopy = new LinkedHashMap<>();
            for (final Object myObject : myInputMap.entrySet()) {
                final Map.Entry myEntry = (Map.Entry) myObject;
                myInputMapCopy.put(charSequenceToString((CharSequence) myEntry.getKey()),
                        deepCharSequenceToString(myEntry.getValue()));
            }

            return myInputMapCopy;
        } else if (parInput instanceof Collection) {
            final Collection myInputList = (Collection) parInput;
            final Collection<Object> myInputListCopy = new LinkedList<>();
            for (final Object myItem : myInputList) {
                myInputListCopy.add(deepCharSequenceToString(myItem));
            }

            return myInputListCopy;
        } else if (parInput.getClass().isArray()) {
            final Object[] myInputArray = (Object[]) parInput;
            for (int myIndex = 0; myIndex < myInputArray.length; myIndex++) {
                myInputArray[myIndex] = deepCharSequenceToString(myInputArray[myIndex]);
            }
        }

        return parInput;
    }
}
