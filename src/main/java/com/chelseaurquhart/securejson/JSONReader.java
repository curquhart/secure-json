package com.chelseaurquhart.securejson;

import com.chelseaurquhart.securejson.JSONDecodeException.ExtraCharactersException;
import com.chelseaurquhart.securejson.JSONDecodeException.InvalidTokenException;

import java.io.IOException;
import java.io.InputStream;

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
        return read(new IterableCharSequence(parJson), true);
    }

    Object read(final InputStream parInputStream) throws IOException {
        return read(new IterableInputStream(parInputStream), true);
    }

    Object read(final ICharacterIterator parIterator, final boolean parIsRoot) throws IOException {
        moveToNextToken(parIterator);

        final Object myResult;
        if (wordReader.isStart(parIterator)) {
            myResult = wordReader.read(parIterator);
        } else if (listReader.isStart(parIterator)) {
            myResult = listReader.read(parIterator);
        } else if (mapReader.isStart(parIterator)) {
            myResult = mapReader.read(parIterator);
        } else if (numberReader.isStart(parIterator)) {
            myResult = numberReader.read(parIterator);
        } else if (stringReader.isStart(parIterator)) {
            myResult = stringReader.read(parIterator);
        } else {
            throw new InvalidTokenException(parIterator);
        }

        moveToNextToken(parIterator);
        if (parIsRoot && parIterator.hasNext()) {
            throw new ExtraCharactersException(parIterator);
        }

        return myResult;
    }

    void moveToNextToken(final ICharacterIterator parIterator) throws IOException {
        while (parIterator.hasNext()) {
            final char myChar = Character.toLowerCase(parIterator.peek());
            if (JSONSymbolCollection.WHITESPACES.containsKey(myChar)) {
                parIterator.next();
            } else if (JSONSymbolCollection.TOKENS.containsKey(myChar)
                    || JSONSymbolCollection.NUMBERS.containsKey(myChar)) {
                break;
            } else {
                throw new InvalidTokenException(parIterator);
            }
        }
    }
}
