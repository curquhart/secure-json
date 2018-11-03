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

package com.chelseaurquhart.securejson.util;

import java.nio.CharBuffer;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * Utility class for converting character sequences into strings recursively.
 */
public final class StringUtil {
    private StringUtil() {
    }

    /**
     * Recursively deep-copy all char sequences from parInput into Strings.
     * @param parInput The object to process.
     * @return An object with no secure strings.
     */
    public static Object deepCharSequenceToString(final Object parInput) {
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

    /**
     * Converts a character sequence to a string.
     *
     * @param parInput The input to convert.
     * @return A character sequence with no strings.
     */
    public static String charSequenceToString(final CharSequence parInput) {
        if (parInput instanceof String) {
            return (String) parInput;
        }

        final char[] myChars = new char[parInput.length()];
        CharBuffer.wrap(parInput).get(myChars);

        return new String(myChars);
    }
}
