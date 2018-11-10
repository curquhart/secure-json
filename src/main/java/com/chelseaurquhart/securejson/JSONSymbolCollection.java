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

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @exclude
 */
final class JSONSymbolCollection {
    static final int MIN_ALLOWED_ASCII_CODE = 32;
    static final int MAX_ALLOWED_ASCII_CODE = 126;
    static final short UNICODE_DIGITS = 4;
    static final short UNICODE_DIGIT_FIRST = UNICODE_DIGITS * 3;
    static final short UNICODE_DIGIT_SECOND = UNICODE_DIGITS * 2;
    static final short UNICODE_DIGIT_THIRD = UNICODE_DIGITS;
    static final short HEX_MIN_ALPHA = 10;
    static final short HEX_MAX = 15;
    static final short BITS_IN_BYTE = 8;
    static final short TWO_BYTE = 255;

    static final Map<Character, Token> END_TOKENS = listToMap(
        Token.R_BRACE,
        Token.R_CURLY,
        Token.QUOTE,
        Token.COMMA
    );

    static final Map<Character, Token> WORD_TOKENS = listToMap(
        Token.NULL,
        Token.TRUE,
        Token.FALSE
    );

    static final Map<Character, Character> WHITESPACES = listToMap(
        ' ',
        '\t',
        '\r',
        '\n',
        '\u0009'
    );

    static final Map<Character, Token> TOKENS = listToMap(
        Token.L_BRACE,
        Token.R_BRACE,
        Token.L_CURLY,
        Token.R_CURLY,
        Token.COLON,
        Token.QUOTE,
        Token.COMMA,
        Token.NULL,
        Token.FALSE,
        Token.TRUE
    );

    static final Map<Character, Token> NUMBERS = listToMap(
        Token.ZERO,
        Token.ONE,
        Token.TWO,
        Token.THREE,
        Token.FOUR,
        Token.FIVE,
        Token.SIX,
        Token.SEVEN,
        Token.EIGHT,
        Token.NINE,
        Token.DECIMAL,
        Token.MINUS,
        Token.PLUS,
        Token.EXPONENT
    );

    private static Map<Character, Token> listToMap(final Token... parElements) {
        final Map<Character, Token> myMap = new HashMap<>();
        for (final Token myElement : parElements) {
            myMap.put(myElement.getShortSymbol(), myElement);
        }

        return Collections.unmodifiableMap(myMap);
    }

    private static Map<Character, Character> listToMap(final Character... parElements) {
        final Map<Character, Character> myMap = new HashMap<>();
        for (final Character myElement : parElements) {
            myMap.put(myElement, myElement);
        }

        return Collections.unmodifiableMap(myMap);
    }

    private JSONSymbolCollection() {
    }

    /**
     * @exclude
     */
    enum Token {
        NULL("null", null),
        FALSE("false", false),
        TRUE("true", true),
        L_BRACE('['),
        R_BRACE(']'),
        L_CURLY('{'),
        R_CURLY('}'),
        COLON(':'),
        QUOTE('"', "\\\""),
        COMMA(','),
        ZERO('0'),
        ONE('1'),
        TWO('2'),
        THREE('3'),
        FOUR('4'),
        FIVE('5'),
        SIX('6'),
        SEVEN('7'),
        EIGHT('8'),
        NINE('9'),
        DECIMAL('.'),
        MINUS('-'),
        PLUS('+'),
        EXPONENT('e'),
        SLASH('/', "\\/"),
        CARRIAGE_RETURN('\r', "\\r"),
        LINE_FEED('\n', "\\n"),
        FORM_FEED('\f', "\\f"),
        BACKSPACE('\b', "\\b"),
        UNICODE('u'),
        ESCAPE('\\', "\\\\"),
        UNKNOWN(null, null);

        private static final Map<Character, Token> SHORT_TOKEN_MAP = new HashMap<>();

        static {
            for (final Token myValue : values()) {
                if (myValue != UNKNOWN) {
                    SHORT_TOKEN_MAP.put(myValue.getShortSymbol(), myValue);
                }
            }
        }

        static Token forSymbol(final char parSymbol) throws IOException, JSONException {
            final Token myToken = forSymbolOrDefault(parSymbol, null);

            if (myToken == null) {
                throw new JSONException(
                    Messages.get(Messages.Key.ERROR_INVALID_SYMBOL).replace(":symbol", String.valueOf(parSymbol)));
            }

            return myToken;
        }

        static Token forSymbolOrDefault(final char parSymbol, final Token parDefault) {
            final char mySymbol = Character.toLowerCase(parSymbol);
            if (SHORT_TOKEN_MAP.containsKey(mySymbol)) {
                return SHORT_TOKEN_MAP.get(mySymbol);
            }

            return parDefault;
        }

        private final Object symbol;
        private final Object value;
        private final char shortSymbol;

        Token(final Object parSymbol) {
            this(parSymbol, parSymbol);
        }

        Token(final Object parSymbol, final Object parValue) {
            this.symbol = parSymbol;
            this.value = parValue;
            if (symbol instanceof Character) {
                this.shortSymbol = (char) symbol;
            } else if (symbol != null) {
                this.shortSymbol = symbol.toString().charAt(0);
            } else {
                this.shortSymbol = '\u0000';
            }
        }

        Object getValue() {
            return value;
        }

        Object getSymbol() {
            return symbol;
        }

        char getShortSymbol() {
            return shortSymbol;
        }
    }
}
