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

import com.chelseaurquhart.securejson.JSONDecodeException.InvalidTokenException;

import java.io.IOException;

/**
 * @exclude
 */
class WordReader implements IReader<Object> {
    @Override
    public boolean isStart(final ICharacterIterator parIterator) throws IOException, JSONException {
        return JSONSymbolCollection.WORD_TOKENS.get(parIterator.peek()) != null;
    }

    @Override
    public SymbolType getSymbolType(final ICharacterIterator parIterator) {
        return SymbolType.UNKNOWN;
    }

    @Override
    public Object read(final ICharacterIterator parIterator, final JSONReader.IContainer<?, ?> parCollection)
            throws IOException, JSONException {

        final JSONSymbolCollection.Token myWordToken = JSONSymbolCollection.WORD_TOKENS.get(parIterator.peek());
        if (myWordToken != null) {
            readAndValidateWord(parIterator, myWordToken);
            return myWordToken.getValue();
        }

        throw new InvalidTokenException(parIterator);
    }

    @Override
    public void addValue(final ICharacterIterator parIterator, final JSONReader.IContainer<?, ?> parCollection,
                         final Object parItem) {
        // only for collections
        throw new NotImplementedException(Messages.Key.ERROR_NOT_IMPLEMENTED, "addValue");
    }

    private void readAndValidateWord(final ICharacterIterator parIterator, final JSONSymbolCollection.Token parToken)
            throws IOException, JSONException {
        final CharSequence myWord = parToken.toString().toLowerCase();
        final int myCheckingLength = myWord.length();

        for (int myIndex = 0; myIndex < myCheckingLength; myIndex++) {
            if (!parIterator.hasNext()) {
                throw new InvalidTokenException(parIterator);
            }
            if (myWord.charAt(myIndex) != parIterator.peek()) {
                throw new InvalidTokenException(parIterator);
            }

            parIterator.next();
        }

        if (!parIterator.hasNext()) {
            return;
        }

        final char myChar = parIterator.peek();
        if (JSONSymbolCollection.TOKENS.containsKey(myChar) || JSONSymbolCollection.WHITESPACES.containsKey(myChar)) {
            return;
        }

        throw new InvalidTokenException(parIterator);
    }

    @Override
    public void close() {
        // This reader does not open any resources.
    }
}
