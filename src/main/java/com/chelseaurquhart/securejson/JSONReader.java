package com.chelseaurquhart.securejson;

import com.chelseaurquhart.securejson.JSONDecodeException.EmptyJSONException;
import com.chelseaurquhart.securejson.JSONDecodeException.ExtraCharactersException;
import com.chelseaurquhart.securejson.JSONDecodeException.InvalidTokenException;
import com.chelseaurquhart.securejson.JSONDecodeException.MalformedJSONException;

import java.io.IOException;
import java.io.InputStream;
import java.util.AbstractMap;
import java.util.Map;
import java.util.Stack;

class JSONReader {
    private final NumberReader numberReader;
    private final StringReader stringReader;
    private final ListReader listReader;
    private final WordReader wordReader;
    private final MapReader mapReader;

    JSONReader() {
        this(null, null, null, null, null);
    }

    JSONReader(final NumberReader parNumberReader) {
        this(parNumberReader, null, null, null, null);
    }

    JSONReader(final StringReader parStringReader) {
        this(null, parStringReader, null, null, null);
    }

    JSONReader(final ListReader parListReader) {
        this(null, null, parListReader, null, null);
    }

    JSONReader(final WordReader parWordReader) {
        this(null, null, null, parWordReader, null);
    }

    JSONReader(final MapReader parMapReader) {
        this(null, null, null, null, parMapReader);
    }

    private JSONReader(final NumberReader parNumberReader, final StringReader parStringReader,
                       final ListReader parListReader, final WordReader parWordReader, final MapReader parMapReader) {
        if (parNumberReader == null) {
            numberReader = new NumberReader();
        } else {
            numberReader = parNumberReader;
        }
        if (parStringReader == null) {
            stringReader = new StringReader();
        } else {
            stringReader = parStringReader;
        }
        if (parListReader == null) {
            listReader = new ListReader(this);
        } else {
            listReader = parListReader;
        }
        if (parWordReader == null) {
            wordReader = new WordReader();
        } else {
            wordReader = parWordReader;
        }
        if (parMapReader == null) {
            mapReader = new MapReader(this, stringReader);
        } else {
            mapReader = parMapReader;
        }
    }

    Object read(final CharSequence parJson) throws IOException {
        return read(new IterableCharSequence(parJson));
    }

    Object read(final InputStream parInputStream) throws IOException {
        return read(new IterableInputStream(parInputStream));
    }

    Object read(final ICharacterIterator parIterator) throws IOException {
        final Stack<Map.Entry<IReader, Object>> myStack = new Stack<>();

        findEncoding(parIterator);

        Object mySeparatorForObject = null;
        while (parIterator.hasNext()) {
            moveToNextToken(parIterator);

            Object myResult = null;
            boolean myHasResult = true;
            if (wordReader.isStart(parIterator)) {
                myResult = wordReader.read(parIterator);
            } else if (listReader.isStart(parIterator)) {
                myStack.push(new AbstractMap.SimpleImmutableEntry<IReader, Object>(
                    listReader, listReader.read(parIterator)));
                continue;
            } else if (mapReader.isStart(parIterator)) {
                myStack.push(new AbstractMap.SimpleImmutableEntry<IReader, Object>(
                    mapReader, mapReader.read(parIterator)));
                continue;
            } else if (numberReader.isStart(parIterator)) {
                myResult = numberReader.read(parIterator);
            } else if (stringReader.isStart(parIterator)) {
                myResult = stringReader.read(parIterator);
            } else if (myStack.empty()) {
                throw new InvalidTokenException(parIterator);
            } else {
                myHasResult = false;
            }
            moveToNextToken(parIterator);
            if (myHasResult) {
                mySeparatorForObject = null;
            }

            boolean myIsFinished = true;
            while (!myStack.empty()) {
                Map.Entry<IReader, Object> myHead = myStack.peek();
                if (mySeparatorForObject != myHead) {
                    mySeparatorForObject = null;
                }
                if (!parIterator.hasNext()) {
                    throw new MalformedJSONException(parIterator);
                }
                final IReader.SymbolType mySymbolType = myHead.getKey().getSymbolType(parIterator);
                if (myHasResult) {
                    myHead.getKey().addValue(parIterator, myHead.getValue(), myResult);
                }
                if (mySymbolType == IReader.SymbolType.END) {
                    if (mySeparatorForObject == myHead) {
                        throw new InvalidTokenException(parIterator);
                    }
                    myStack.pop();
                    parIterator.next();
                    moveToNextToken(parIterator);
                    // feed it to its parent. Because map stores its data as a wrapper, we need to ask the reader
                    // to provide a proper value (ex Map instead of Container)
                    myResult = myHead.getKey().normalizeCollection(myHead.getValue());
                    myHasResult = true;
                } else if (mySymbolType == IReader.SymbolType.SEPARATOR) {
                    // keep reading
                    parIterator.next();
                    moveToNextToken(parIterator);
                    if (myHead.getKey().getSymbolType(parIterator) != IReader.SymbolType.UNKNOWN) {
                        throw new InvalidTokenException(parIterator);
                    }
                    myIsFinished = false;
                    mySeparatorForObject = myHead;
                    break;
                } else if (isValidToken(parIterator.peek())) {
                    myIsFinished = false;
                    break;
                } else {
                    throw new MalformedJSONException(parIterator);
                }
            }

            if (myIsFinished) {
                if (parIterator.hasNext()) {
                    throw new ExtraCharactersException(parIterator);
                }
                return myResult;
            }
        }

        throw new EmptyJSONException(parIterator);
    }

    private Encoding findEncoding(final ICharacterIterator parIterator) throws IOException {
        // We can accept either encoding. UTF-8 characters, other than the BOM, are not allowed in JSON, so these are
        // the only special characters we need to handle.

        final char myUtf8BomChar0 = '\u00ef';
        final char myUtf8BomChar1 = '\u00bb';
        final char myUtf8BomChar2 = '\u00bf';

        final char myUtf16BomChar0 = '\u00fe';
        final char myUtf16BomChar1 = '\u00ff';

        final char myUtfBigEndian = '\ufeff';
        final char myUtfLittleEndian = '\ufffe';
        final char myUtf32Char = '\u0000';

        // UTF8
        final char myNextChar = parIterator.peek();
        switch (myNextChar) {
            case myUtf8BomChar0:
                parIterator.next();
                if (!parIterator.hasNext() || parIterator.next() != myUtf8BomChar1 || !parIterator.hasNext()
                        || parIterator.next() != myUtf8BomChar2) {
                    throw new MalformedJSONException(parIterator);
                }
                // UTF-8 but single byte characters
                return Encoding.UTF8;
            case myUtfBigEndian:
                parIterator.next();
                if (findPartialUtf32Encoding(parIterator)) {
                    return Encoding.UTF32BE;
                }
                return Encoding.UTF16BE;
            case myUtfLittleEndian:
                parIterator.next();
                if (findPartialUtf32Encoding(parIterator)) {
                    return Encoding.UTF32LE;
                }
                return Encoding.UTF16LE;
            case myUtf32Char:
                parIterator.next();
                if (parIterator.hasNext() && parIterator.next() != myUtf32Char) {
                    throw new MalformedJSONException(parIterator);
                }
                final Encoding myEncoding = findEncoding(parIterator);
                if (myEncoding == Encoding.UTF16BE) {
                    return Encoding.UTF32BE;
                }
                throw new MalformedJSONException(parIterator);
            case myUtf16BomChar0:
                parIterator.next();
                // big-endian
                if (!parIterator.hasNext() || parIterator.next() != myUtf16BomChar1) {
                    throw new MalformedJSONException(parIterator);
                }
                return Encoding.UTF16BE;
            case myUtf16BomChar1:
                parIterator.next();
                // little-endian
                if (!parIterator.hasNext() || parIterator.next() != myUtf16BomChar0) {
                    throw new MalformedJSONException(parIterator);
                }
                if (findPartialUtf32Encoding(parIterator)) {
                    return Encoding.UTF32LE;
                }
                return Encoding.UTF16LE;
            default:
                return Encoding.UTF8;
        }
    }

    private boolean findPartialUtf32Encoding(final ICharacterIterator parIterator) throws IOException {
        final char myUtf32Char = '\u0000';

        if (parIterator.hasNext() && parIterator.peek() == myUtf32Char) {
            parIterator.next();
            if (!parIterator.hasNext() || parIterator.next() != myUtf32Char) {
                throw new MalformedJSONException(parIterator);
            }
            return true;
        }

        return false;
    }

    void moveToNextToken(final ICharacterIterator parIterator) throws IOException {
        while (parIterator.hasNext()) {
            final char myChar = Character.toLowerCase(parIterator.peek());
            if (JSONSymbolCollection.WHITESPACES.containsKey(myChar)) {
                parIterator.next();
            } else if (isValidToken(myChar)) {
                break;
            } else {
                throw new InvalidTokenException(parIterator);
            }
        }
    }

    private boolean isValidToken(final char parChar) {
        return JSONSymbolCollection.TOKENS.containsKey(parChar) || JSONSymbolCollection.NUMBERS.containsKey(parChar);
    }

    private enum Encoding {
        UTF8,
        UTF16BE,
        UTF16LE,
        UTF32BE,
        UTF32LE
    }
}
