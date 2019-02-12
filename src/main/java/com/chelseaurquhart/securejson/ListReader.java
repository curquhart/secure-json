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

import com.chelseaurquhart.securejson.JSONDecodeException.MalformedListException;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

/**
 * @exclude
 */
class ListReader implements IReader<ListReader.Container> {
    @Override
    public boolean isStart(final ICharacterIterator parIterator) throws IOException, JSONException {
        return JSONSymbolCollection.Token.forSymbolOrDefault(parIterator.peek(), null)
            == JSONSymbolCollection.Token.L_BRACE;
    }

    @Override
    public SymbolType getSymbolType(final ICharacterIterator parIterator) throws IOException, JSONException {
        final JSONSymbolCollection.Token myToken = JSONSymbolCollection.Token.forSymbolOrDefault(parIterator.peek(),
            JSONSymbolCollection.Token.UNKNOWN);

        switch (myToken) {
            case R_BRACE:
                return SymbolType.END;
            case COMMA:
                return SymbolType.SEPARATOR;
            default:
                return SymbolType.UNKNOWN;
        }
    }

    @Override
    public Container read(final ICharacterIterator parIterator, final JSONReader.IContainer<?, ?> parContainer)
            throws IOException, JSONException {
        parIterator.next();
        parIterator.skipWhitespace();

        if (!parIterator.hasNext()) {
            throw new MalformedListException(parIterator);
        }

        final SymbolType mySymbolType = getSymbolType(parIterator);
        if (mySymbolType != SymbolType.UNKNOWN && mySymbolType != SymbolType.END) {
            throw new MalformedListException(parIterator);
        }

        final Container myContainer;
        if (parContainer == null) {
            myContainer = new Container(this);
        } else {
            myContainer = (Container) parContainer;
        }

        return myContainer;
    }

    @Override
    public void addValue(final ICharacterIterator parIterator, final JSONReader.IContainer<?, ?> parCollection,
                         final Object parValue) throws IOException, JSONException {
        final Container myContainer = objectToContainer(parCollection);
        myContainer.add(parValue);

        parIterator.skipWhitespace();
        if (getSymbolType(parIterator) == SymbolType.UNKNOWN) {
            throw new MalformedListException(parIterator);
        }
    }

    @Override
    public void close() {
        // no resources to close.
    }

    private Container objectToContainer(final Object parValue) {
        return (Container) parValue;
    }

    /**
     * A container for our List.
     */
    static final class Container implements JSONReader.IContainer<List<Object>, ListReader> {
        private transient List<Object> list;
        private transient ListReader reader;

        private Container(final ListReader parReader) {
            reader = parReader;
        }

        private void add(final Object parValue) {
            resolve().add(parValue);
        }

        @Override
        public List<Object> resolve() {
            if (list == null) {
                list = new LinkedList<Object>();
            }

            return list;
        }

        @Override
        public ListReader getReader() {
            return reader;
        }
    }
}
