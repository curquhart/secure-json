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

import com.chelseaurquhart.securejson.JSONDecodeException.EmptyJSONException;
import com.chelseaurquhart.securejson.JSONDecodeException.ExtraCharactersException;
import com.chelseaurquhart.securejson.JSONDecodeException.InvalidTokenException;
import com.chelseaurquhart.securejson.JSONDecodeException.MalformedJSONException;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayDeque;
import java.util.Deque;

/**
 * @exclude
 */
final class JSONReader implements Closeable, IAutoCloseable {
    @SuppressWarnings("rawtypes")
    private final transient IReader<?>[] readers;

    private JSONReader(final Builder parBuilder) {
        final IReader<CharSequence> myStringReader;
        final IReader<Number> myNumberReader;

        if (parBuilder.stringReaderImpl == null) {
            myStringReader = new StringReader(parBuilder.settingsImpl);
        } else {
            myStringReader = parBuilder.stringReaderImpl;
        }
        if (parBuilder.numberReaderImpl == null) {
            myNumberReader = new NumberReader(parBuilder.settingsImpl);
        } else {
            myNumberReader = parBuilder.numberReaderImpl;
        }

        readers = new IReader<?>[] {
            myNumberReader,
            myStringReader,
            new WordReader(),
            new ListReader(this),
            new MapReader(this),
        };
    }

    Object read(final CharSequence parJson) throws IOException, JSONException {
        return read(new IterableCharSequence(parJson));
    }

    Object read(final InputStream parInputStream) throws IOException, JSONException {
        return read(new IterableInputStream(parInputStream));
    }

    Object read(final ICharacterIterator parIterator) throws IOException, JSONException {
        final ContainerStack myStack = new ContainerStack();

        final ReaderData myReaderData = new ReaderData();
        while (parIterator.hasNext()) {
            moveToNextToken(parIterator);

            myReaderData.result = null;

            final IReader<?> myReader = getReaderStartingNextChar(parIterator);
            if (myReader != null) {
                myReaderData.result = myReader.read(parIterator, null);
                if (myReaderData.result instanceof IContainer) {
                    myStack.push((IContainer) myReaderData.result);
                    continue;
                }

                myReaderData.hasResult = true;
            } else if (myStack.isEmpty()) {
                throw new InvalidTokenException(parIterator);
            } else {
                myReaderData.hasResult = false;
            }
            moveToNextToken(parIterator);

            myReaderData.isFinished = true;
            readStack(parIterator, myStack, myReaderData);

            if (myReaderData.isFinished) {
                if (parIterator.hasNext()) {
                    throw new ExtraCharactersException(parIterator);
                }
                return myReaderData.result;
            }
        }

        throw new EmptyJSONException(parIterator);
    }

    private void readStack(final ICharacterIterator parIterator, final ContainerStack parStack,
                           final ReaderData parReaderData) throws IOException, JSONException {
        while (!parStack.isEmpty()) {
            readStackEntry(parIterator, parStack, parReaderData);

            if (!parReaderData.isFinished) {
                break;
            }
        }
    }

    private void readStackEntry(final ICharacterIterator parIterator,
                                final ContainerStack parStack, final ReaderData parReaderData)
            throws IOException, JSONException {
        final IContainer<?, ?> myHead = parStack.peek();
        if (!parIterator.hasNext() || myHead == null) {
            throw new MalformedJSONException(parIterator);
        }

        // unwrap - Pair will be repurposed so we must retain certainty that we don't depend on it!
        final IReader<?> myReader = myHead.getReader();

        final IReader.SymbolType mySymbolType = myReader.getSymbolType(parIterator);
        if (parReaderData.hasResult) {
            myReader.addValue(parIterator, myHead, parReaderData.result);
        }
        if (mySymbolType == IReader.SymbolType.END) {
            readStackEnd(parIterator, myHead, parReaderData, parStack);
        } else if (mySymbolType == IReader.SymbolType.SEPARATOR) {
            readStackPart(parIterator, myReader, parReaderData);
        } else if (parIterator.hasNext() && isValidToken(parIterator.peek())) {
            parReaderData.isFinished = false;
        } else {
            throw new MalformedJSONException(parIterator);
        }
    }

    private void readStackPart(final ICharacterIterator parIterator, final IReader<?> parReader,
                               final ReaderData parReaderData) throws IOException, JSONException {
        parIterator.next();
        moveToNextToken(parIterator);
        if (parReader.getSymbolType(parIterator) != IReader.SymbolType.UNKNOWN) {
            throw new InvalidTokenException(parIterator);
        }
        // keep reading
        parReaderData.isFinished = false;
    }

    private void readStackEnd(final ICharacterIterator parIterator,
                              final IContainer<?, ?> parValue, final ReaderData parReaderData,
                              final ContainerStack parStack)
            throws IOException, JSONException {
        parStack.pop();
        parIterator.next();
        moveToNextToken(parIterator);
        // feed it to its parent. Because map stores its data as a wrapper, we need to ask the reader
        // to provide a proper value (ex Map instead of Container)
        parReaderData.result = parValue.resolve();
        // keep reading
        parReaderData.hasResult = true;
    }

    private IReader<?> getReaderStartingNextChar(final ICharacterIterator parIterator) throws IOException,
            JSONException {
        for (final IReader<?> myReader : readers) {
            if (myReader.isStart(parIterator)) {
                return myReader;
            }
        }

        return null;
    }

    @Override
    public void close() throws IOException {
        IOException myException = null;
        for (final IReader<?> myReader : readers) {
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

    void moveToNextToken(final ICharacterIterator parIterator) throws IOException, JSONException {
        while (parIterator.hasNext()) {
            final char myChar = parIterator.peek();
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

    /**
     * Builder for JSONReader.
     */
    static class Builder {
        private final transient Settings settingsImpl;
        private transient IReader<CharSequence> stringReaderImpl;
        private transient IReader<Number> numberReaderImpl;

        Builder(final Settings parSettings) {
            settingsImpl = Objects.requireNonNull(parSettings);
        }

        Builder stringReader(final IReader<CharSequence> parStringReader) {
            stringReaderImpl = Objects.requireNonNull(parStringReader);

            return this;
        }

        Builder numberReader(final IReader<Number> parNumberReader) {
            numberReaderImpl = Objects.requireNonNull(parNumberReader);

            return this;
        }

        JSONReader build() {
            return new JSONReader(this);
        }
    }

    /**
     * Data for use by the JSON Reader.
     */
    private static class ReaderData {
        private transient boolean isFinished;
        private transient Object result;
        private transient boolean hasResult;
    }

    /**
     * Container interface for collections. All collection types must return an implementation of this.
     *
     * @param <T> The type this container is wrapping.
     * @param <U> The reader this container is associated with.
     */
    interface IContainer<T, U extends IReader<?>> {
        /**
         * Resolve the underlying collection.
         *
         * @return The underlying collection.
         */
        T resolve();

        /**
         * Get the reader associated with this container.
         *
         * @return The reader associated with this container.
         */
        U getReader();
    }

    /**
     * Stack of Containers. Optimized for the use of most operations happening on head.
     */
    static class ContainerStack {
        private IContainer<?, ?> head;
        private Deque<IContainer<?, ?>> stack;

        void push(final IContainer<?, ?> parValue) {
            if (head == null) {
                head = parValue;
            } else {
                if (stack == null) {
                    stack = new ArrayDeque<IContainer<?, ?>>();
                }

                stack.push(head);
                head = parValue;
            }
        }

        boolean isEmpty() {
            return head == null;
        }

        IContainer<?, ?> peek() {
            return head;
        }

        void pop() {
            if (head == null || stack == null || stack.isEmpty()) {
                head = null;
            } else {
                head = stack.pop();
            }
        }
    }
}
