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

import com.chelseaurquhart.securejson.JSONDecodeException.MalformedMapException;
import com.chelseaurquhart.securejson.JSONDecodeException.MalformedStringException;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @exclude
 */
class MapReader implements IReader<MapReader.Container> {
    @Override
    public boolean isStart(final ICharacterIterator parIterator) throws IOException, JSONException {
        return JSONSymbolCollection.Token.forSymbolOrDefault(parIterator.peek(), null)
            == JSONSymbolCollection.Token.L_CURLY;
    }

    @Override
    public SymbolType getSymbolType(final ICharacterIterator parIterator) throws IOException, JSONException {
        if (!parIterator.hasNext()) {
            throw new MalformedMapException(parIterator);
        }

        final JSONSymbolCollection.Token myToken = JSONSymbolCollection.Token.forSymbolOrDefault(
            parIterator.peek(), JSONSymbolCollection.Token.UNKNOWN);

        switch (myToken) {
            case R_CURLY:
                return SymbolType.END;
            case COLON:
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
            throw new MalformedMapException(parIterator);
        }

        final Container myContainer;
        if (parContainer == null) {
            myContainer = new Container(this, parIterator.getOffset());
        } else {
            myContainer = (Container) parContainer;
        }

        return myContainer;
    }

    @Override
    public void addValue(final ICharacterIterator parIterator, final JSONReader.IContainer<?, ?> parContainer,
                         final Object parValue) throws IOException, JSONException {
        final Container myContainer = (Container) parContainer;
        parIterator.skipWhitespace();
        final JSONSymbolCollection.Token myToken = JSONSymbolCollection.Token.forSymbolOrDefault(parIterator.peek(),
            null);
        if (myContainer.key == null) {
            if (myToken != JSONSymbolCollection.Token.COLON) {
                throw new MalformedMapException(parIterator);
            }
            if (!(parValue instanceof CharSequence)) {
                throw new MalformedStringException(myContainer.keyStartIndex);
            }

            myContainer.key = (CharSequence) parValue;
        } else {
            if (myToken != JSONSymbolCollection.Token.R_CURLY && myToken != JSONSymbolCollection.Token.COMMA) {
                throw new MalformedMapException(parIterator);
            }

            myContainer.put(myContainer.key, parValue);
            myContainer.key = null;
            myContainer.keyStartIndex = parIterator.getOffset();
        }
    }

    @Override
    public void close() {
        // NOOP
    }

    /**
     * Container for Map data.
     */
    static final class Container implements JSONReader.IContainer<Map<CharSequence, Object>, MapReader> {
        private transient Map<CharSequence, Object> map;
        private transient CharSequence key;
        private transient int keyStartIndex;
        private transient MapReader reader;

        private Container(final MapReader parReader, final int parKeyStartIndex) {
            reader = parReader;
            keyStartIndex = parKeyStartIndex;
        }

        private void put(final CharSequence parKey, final Object parValue) {
            resolve().put(parKey, parValue);
        }

        @Override
        public Map<CharSequence, Object> resolve() {
            if (map == null) {
                map = new LinkedHashMap<CharSequence, Object>();
            }

            return map;
        }

        @Override
        public MapReader getReader() {
            return reader;
        }
    }
}
