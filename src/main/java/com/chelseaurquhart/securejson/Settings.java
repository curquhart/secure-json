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

import java.util.HashMap;
import java.util.Map;

/**
 * @exclude
 */
class Settings {
    static final boolean DEFAULT_STRICT_STRINGS = true;
    static final boolean DEFAULT_STRICT_MAP_KEY_TYPES = true;
    static final IFunction<Integer, IWritableCharSequence> DEFAULT_WRITABLE_CHAR_BUFFER_FACTORY =
            new IFunction<Integer, IWritableCharSequence>() {
        @Override
        public IWritableCharSequence accept(final Integer parCapacity) {
            return new ManagedSecureCharBuffer.ObfuscatedByteBuffer(parCapacity);
        }
    };
    static final Settings DEFAULTS = new Settings();

    private final boolean strictStrings;
    private final boolean strictMapKeyTypes;
    private final IFunction<Integer, IWritableCharSequence> writableCharBufferFactory;
    private final Map<Class<?>, IFunction<Object, ?>> classInitializers;

    Settings() {
        strictStrings = DEFAULT_STRICT_STRINGS;
        strictMapKeyTypes = DEFAULT_STRICT_MAP_KEY_TYPES;
        writableCharBufferFactory = DEFAULT_WRITABLE_CHAR_BUFFER_FACTORY;
        classInitializers = new HashMap<Class<?>, IFunction<Object, ?>>();
    }

    Settings(final SecureJSON.Builder parBuilder) {
        strictStrings = parBuilder.isStrictStrings();
        strictMapKeyTypes = parBuilder.isStrictMapKeyTypes();
        writableCharBufferFactory = parBuilder.getWritableCharBufferFactory();
        classInitializers = parBuilder.getClassInitializers();
    }

    boolean isStrictStrings() {
        return strictStrings;
    }

    boolean isStrictMapKeyTypes() {
        return strictMapKeyTypes;
    }

    IFunction<Integer, IWritableCharSequence> getWritableCharBufferFactory() {
        return writableCharBufferFactory;
    }

    Map<Class<?>, IFunction<Object, ?>> getClassInitializers() {
        return classInitializers;
    }
}
