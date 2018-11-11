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

import com.chelseaurquhart.securejson.JSONDecodeException.MalformedListException;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

/**
 * @exclude
 */
class ListReader implements IReader<ListReader.Container> {
    private final transient JSONReader jsonReader;

    ListReader(final JSONReader parJsonReader) {
        jsonReader = parJsonReader;
    }

    @Override
    public boolean isStart(final ICharacterIterator parIterator) throws IOException, JSONException {
        return JSONSymbolCollection.Token.forSymbolOrDefault(parIterator.peek(), null)
            == JSONSymbolCollection.Token.L_BRACE;
    }

    @Override
    public SymbolType getSymbolType(final ICharacterIterator parIterator) throws IOException, JSONException {
        if (!parIterator.hasNext()) {
            throw new MalformedListException(parIterator);
        }

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
    public Container read(final ICharacterIterator parIterator) throws IOException, JSONException {
        parIterator.next();
        jsonReader.moveToNextToken(parIterator);

        if (!parIterator.hasNext()) {
            throw new MalformedListException(parIterator);
        }

        final SymbolType mySymbolType = getSymbolType(parIterator);
        if (mySymbolType != SymbolType.UNKNOWN && mySymbolType != SymbolType.END) {
            throw new MalformedListException(parIterator);
        }

        return new Container();
    }

    @Override
    public void addValue(final ICharacterIterator parIterator, final Object parCollection, final Object parValue)
            throws IOException, JSONException {
        final Container myContainer = objectToContainer(parCollection);
        myContainer.add(parValue);

        jsonReader.moveToNextToken(parIterator);
        if (getSymbolType(parIterator) == SymbolType.UNKNOWN) {
            throw new MalformedListException(parIterator);
        }
    }

    @Override
    public Object normalizeCollection(final Object parValue) {
        if (parValue instanceof Container) {
            return ((Container) parValue).getList();
        }

        return parValue;
    }

    @Override
    public boolean isContainerType() {
        return true;
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
    static final class Container {
        private transient List<Object> list;

        private Container() {
        }

        private void add(final Object parValue) {
            getList().add(parValue);
        }

        private List<Object> getList() {
            if (list == null) {
                list = new LinkedList<>();
            }

            return list;
        }
    }
}
