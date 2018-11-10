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

import com.chelseaurquhart.securejson.JSONDecodeException.MalformedMapException;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @exclude
 */
class MapReader implements IReader<MapReader.Container> {
    private final transient JSONReader jsonReader;
    private final transient IReader<CharSequence> stringReader;

    MapReader(final JSONReader parJsonReader, final IReader<CharSequence> parStringReader) {
        jsonReader = parJsonReader;
        stringReader = parStringReader;
    }

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
                return SymbolType.SEPARATOR;
            case COMMA:
                return SymbolType.RESERVED;
            default:
                return SymbolType.UNKNOWN;
        }
    }

    @Override
    public Container read(final ICharacterIterator parIterator) throws IOException, JSONException {
        parIterator.next();
        jsonReader.moveToNextToken(parIterator);

        if (!parIterator.hasNext()) {
            throw new MalformedMapException(parIterator);
        }

        if (JSONSymbolCollection.Token.forSymbolOrDefault(parIterator.peek(), null)
                == JSONSymbolCollection.Token.R_CURLY) {
            return new Container(null);
        } else {
            return new Container(readKey(parIterator));
        }
    }

    @Override
    public void addValue(final ICharacterIterator parIterator, final Object parCollection, final Object parValue)
            throws IOException, JSONException {
        final Container myContainer = objectToContainer(parCollection);
        myContainer.put(myContainer.key, parValue);
        jsonReader.moveToNextToken(parIterator);
        final SymbolType mySymbolType = getSymbolType(parIterator);
        if (mySymbolType == SymbolType.RESERVED) {
            parIterator.next();
            jsonReader.moveToNextToken(parIterator);
            myContainer.key = readKey(parIterator);
        } else if (mySymbolType == SymbolType.UNKNOWN) {
            throw new MalformedMapException(parIterator);
        }
    }

    @Override
    public Object normalizeCollection(final Object parValue) {
        if (parValue instanceof Container) {
            return ((Container) parValue).getMap();
        }

        return parValue;
    }

    @Override
    public boolean isContainerType() {
        return true;
    }

    @Override
    public void close() throws IOException {
        stringReader.close();
    }

    private CharSequence readKey(final ICharacterIterator parIterator) throws IOException, JSONException {
        final CharSequence myKey = stringReader.read(parIterator);
        jsonReader.moveToNextToken(parIterator);
        if (JSONSymbolCollection.Token.forSymbolOrDefault(parIterator.peek(), null)
                != JSONSymbolCollection.Token.COLON) {
            throw new MalformedMapException(parIterator);
        }
        parIterator.next();
        jsonReader.moveToNextToken(parIterator);
        final SymbolType mySymbolType = getSymbolType(parIterator);
        if (mySymbolType != SymbolType.UNKNOWN && mySymbolType != SymbolType.END) {
            throw new MalformedMapException(parIterator);
        }

        return myKey;
    }

    private Container objectToContainer(final Object parValue) {
        return (Container) parValue;
    }

    /**
     * Container for Map data.
     */
    static final class Container {
        private transient Map<CharSequence, Object> map;
        private transient CharSequence key;

        private Container(final CharSequence parKey) {
            key = parKey;
        }

        private void put(final CharSequence parKey, final Object parValue) {
            getMap().put(parKey, parValue);
        }

        private Map<CharSequence, Object> getMap() {
            if (map == null) {
                map = new LinkedHashMap<>();
            }

            return map;
        }
    }
}
