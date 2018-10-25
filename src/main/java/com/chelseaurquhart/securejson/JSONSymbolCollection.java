package com.chelseaurquhart.securejson;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

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

    static final Map<Character, Character> END_TOKENS = listToMap(
        Token.R_BRACE.getShortSymbol(),
        Token.R_CURLY.getShortSymbol(),
        Token.QUOTE.getShortSymbol(),
        Token.COMMA.getShortSymbol()
    );

    static final Map<Character, Character> WHITESPACES = listToMap(
        ' ',
        '\t',
        '\r',
        '\n',
        '\u0009'
    );

    static final Map<Character, Character> TOKENS = listToMap(
        Token.L_BRACE.getShortSymbol(),
        Token.R_BRACE.getShortSymbol(),
        Token.L_CURLY.getShortSymbol(),
        Token.R_CURLY.getShortSymbol(),
        Token.COLON.getShortSymbol(),
        Token.QUOTE.getShortSymbol(),
        Token.COMMA.getShortSymbol(),
        Token.NULL.getShortSymbol(),
        Token.FALSE.getShortSymbol(),
        Token.TRUE.getShortSymbol()
    );

    static final Map<Character, Character> NUMBERS = listToMap(
        Token.ZERO.getShortSymbol(),
        Token.ONE.getShortSymbol(),
        Token.TWO.getShortSymbol(),
        Token.THREE.getShortSymbol(),
        Token.FOUR.getShortSymbol(),
        Token.FIVE.getShortSymbol(),
        Token.SIX.getShortSymbol(),
        Token.SEVEN.getShortSymbol(),
        Token.EIGHT.getShortSymbol(),
        Token.NINE.getShortSymbol(),
        Token.DECIMAL.getShortSymbol(),
        Token.MINUS.getShortSymbol(),
        Token.PLUS.getShortSymbol(),
        Token.EXPONENT.getShortSymbol()
    );

    private static Map<Character, Character> listToMap(final Character... parElements) {
        final Map<Character, Character> myMap = new HashMap<>();
        for (final Character myElement : parElements) {
            myMap.put(myElement, myElement);
        }

        return Collections.unmodifiableMap(myMap);
    }

    private JSONSymbolCollection() {
    }

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
        ESCAPE('\\', "\\\\");

        private static final Map<Character, Token> SHORT_TOKEN_MAP = new HashMap<>();

        static {
            for (final Token myValue : values()) {
                SHORT_TOKEN_MAP.put(myValue.getShortSymbol(), myValue);
            }
        }

        static Token forSymbol(final char parSymbol) {
            final char mySymbol = Character.toLowerCase(parSymbol);
            if (SHORT_TOKEN_MAP.containsKey(mySymbol)) {
                return SHORT_TOKEN_MAP.get(mySymbol);
            }

            throw new IllegalArgumentException(String.format("%c is an invalid symbol.", parSymbol));
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
            } else {
                this.shortSymbol = symbol.toString().charAt(0);
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
