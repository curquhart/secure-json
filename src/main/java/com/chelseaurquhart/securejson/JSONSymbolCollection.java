package com.chelseaurquhart.securejson;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

final class JSONSymbolCollection {
    private static final Map<Character, Token> SHORT_TOKEN_MAP = new HashMap<>();

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
        Token.PLUS.getShortSymbol()
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
        QUOTE('"'),
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
        EXPONENT('e');

        static Token forSymbol(final char parSymbol) {
            final char mySymbol = Character.toLowerCase(parSymbol);
            if (SHORT_TOKEN_MAP.containsKey(mySymbol)) {
                return SHORT_TOKEN_MAP.get(mySymbol);
            }

            throw new IllegalArgumentException(String.format("%c is an invalid symbol.", parSymbol));
        }

        private final Object symbol;
        private final Object value;

        Token(final Object parSymbol) {
            this(parSymbol, parSymbol);
        }

        Token(final Object parSymbol, final Object parValue) {
            this.symbol = parSymbol;
            this.value = parValue;
            SHORT_TOKEN_MAP.put(getShortSymbol(), this);
        }

        Object getValue() {
            return value;
        }

        Object getSymbol() {
            return symbol;
        }

        char getShortSymbol() {
            if (symbol instanceof Character) {
                return (char) symbol;
            }

            return symbol.toString().charAt(0);
        }
    }
}
