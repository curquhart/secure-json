package com.chelseaurquhart.securejson;

import com.chelseaurquhart.securejson.JSONDecodeException.EmptyJSONException;
import com.chelseaurquhart.securejson.JSONDecodeException.ExtraCharactersException;
import com.chelseaurquhart.securejson.JSONDecodeException.InvalidTokenException;
import com.chelseaurquhart.securejson.JSONDecodeException.MalformedJSONException;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.util.AbstractMap;
import java.util.Map;
import java.util.Objects;
import java.util.Stack;

/**
 * @exclude
 */
final class JSONReader implements Closeable, AutoCloseable {
    private final IReader[] readers;

    private JSONReader(final Builder parBuilder) {
        final IReader<CharSequence> myStringReader;
        final IReader<Number> myNumberReader;

        if (parBuilder.stringReader == null) {
            myStringReader = new StringReader(parBuilder.settings);
        } else {
            myStringReader = parBuilder.stringReader;
        }
        if (parBuilder.numberReader == null) {
            myNumberReader = new NumberReader(parBuilder.settings);
        } else {
            myNumberReader = parBuilder.numberReader;
        }

        readers = new IReader[] {
            myNumberReader,
            myStringReader,
            new WordReader(),
            new ListReader(this),
            new MapReader(this, myStringReader),
        };
    }

    Object read(final CharSequence parJson) throws IOException {
        return read(new IterableCharSequence(parJson));
    }

    Object read(final InputStream parInputStream) throws IOException {
        return read(new IterableInputStream(parInputStream));
    }

    Object read(final ICharacterIterator parIterator) throws IOException {
        final Stack<Map.Entry<IReader, Object>> myStack = new Stack<>();

        final Data myData = new Data();
        while (parIterator.hasNext()) {
            moveToNextToken(parIterator);

            myData.result = null;

            final IReader myReader = isStart(parIterator);
            if (myReader != null) {
                myData.result = myReader.read(parIterator);
                if (myReader.isContainerType()) {
                    myStack.push(new AbstractMap.SimpleImmutableEntry<>(myReader, myData.result));
                    continue;
                }

                myData.hasResult = true;
            } else if (myStack.empty()) {
                throw new InvalidTokenException(parIterator);
            } else {
                myData.hasResult = false;
            }
            moveToNextToken(parIterator);
            if (myData.hasResult) {
                myData.separatorForObject = null;
            }

            myData.isFinished = true;
            readStack(parIterator, myStack, myData);

            if (myData.isFinished) {
                if (parIterator.hasNext()) {
                    throw new ExtraCharactersException(parIterator);
                }
                return myData.result;
            }
        }

        throw new EmptyJSONException(parIterator);
    }

    private void readStack(final ICharacterIterator parIterator, final Stack<Map.Entry<IReader, Object>> parStack,
                           final Data parData) throws IOException {
        while (!parStack.empty()) {
            final Map.Entry<IReader, Object> myHead = parStack.peek();
            if (parData.separatorForObject != myHead) {
                parData.separatorForObject = null;
            }
            if (!parIterator.hasNext()) {
                throw new MalformedJSONException(parIterator);
            }
            final IReader.SymbolType mySymbolType = myHead.getKey().getSymbolType(parIterator);
            if (parData.hasResult) {
                myHead.getKey().addValue(parIterator, myHead.getValue(), parData.result);
            }
            if (mySymbolType == IReader.SymbolType.END) {
                if (parData.separatorForObject == myHead) {
                    throw new InvalidTokenException(parIterator);
                }
                parStack.pop();
                parIterator.next();
                moveToNextToken(parIterator);
                // feed it to its parent. Because map stores its data as a wrapper, we need to ask the reader
                // to provide a proper value (ex Map instead of Container)
                parData.result = myHead.getKey().normalizeCollection(myHead.getValue());
                parData.hasResult = true;
            } else if (mySymbolType == IReader.SymbolType.SEPARATOR) {
                // keep reading
                parIterator.next();
                moveToNextToken(parIterator);
                if (myHead.getKey().getSymbolType(parIterator) != IReader.SymbolType.UNKNOWN) {
                    throw new InvalidTokenException(parIterator);
                }
                parData.isFinished = false;
                parData.separatorForObject = myHead;
                break;
            } else if (isValidToken(parIterator.peek())) {
                parData.isFinished = false;
                break;
            } else {
                throw new MalformedJSONException(parIterator);
            }
        }
    }

    private IReader isStart(final ICharacterIterator parIterator) throws IOException {
        for (final IReader myReader : readers) {
            if (myReader.isStart(parIterator)) {
                return myReader;
            }
        }

        return null;
    }

    @Override
    public void close() throws IOException {
        IOException myException = null;
        for (final IReader myReader : readers) {
            try {
                myReader.close();
            } catch (final IOException myIoException) {
                myException = myIoException;
            }
        }

        if (myException != null) {
            throw myException;
        }
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

    static class Builder {
        private final Settings settings;
        private IReader<CharSequence> stringReader;
        private IReader<Number> numberReader;

        Builder(final Settings parSettings) {
            settings = Objects.requireNonNull(parSettings);
        }

        Builder stringReader(final IReader<CharSequence> parStringReader) {
            stringReader = Objects.requireNonNull(parStringReader);

            return this;
        }

        Builder numberReader(final IReader<Number> parNumberReader) {
            numberReader = Objects.requireNonNull(parNumberReader);

            return this;
        }

        JSONReader build() {
            return new JSONReader(this);
        }
    }

    private static class Data {
        private Object separatorForObject;
        private boolean isFinished;
        private Object result;
        private boolean hasResult;
    }
}
