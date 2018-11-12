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

import com.chelseaurquhart.securejson.JSONException.JSONRuntimeException;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Serializer to/from Objects.
 * @exclude
 */
class ObjectSerializer extends ObjectReflector {
    final SerializationSettings getSerializationSettings(final Field parField) {
        final Serialize myAnnotation = parField.getAnnotation(Serialize.class);
        String[] mySerializationTarget = null;
        Relativity mySerializationTargetStrategy = Relativity.RELATIVE;

        if (myAnnotation != null) {
            final String[] myValue = myAnnotation.name();
            if (myValue.length > 0) {
                mySerializationTarget = myValue;
            }
            mySerializationTargetStrategy = myAnnotation.relativeTo();
        }

        if (mySerializationTarget == null) {
            mySerializationTarget = new String[]{parField.getName()};
        }

        return new SerializationSettings(mySerializationTarget, mySerializationTargetStrategy);
    }

    final boolean isSimpleType(final Object parInput) {
        return parInput == null || parInput instanceof Number || parInput instanceof CharSequence
            || parInput instanceof Boolean;
    }

    final boolean isCollectionType(final Object parInput) {
        return parInput instanceof Collection;
    }

    final boolean isMapType(final Object parInput) {
        return parInput instanceof Map;
    }

    final boolean isArrayType(final Object parInput) {
        return parInput != null && isArrayType(parInput.getClass());
    }

    final boolean isArrayType(final Class<?> parInput) {
        return parInput.isArray();
    }

    final Object resolve(final Object parInput) {
        if (parInput instanceof IJSONSerializeAware) {
            try {
                return resolve(((IJSONSerializeAware) parInput).toJSONable());
            } catch (final RuntimeException myException) {
                throw new JSONRuntimeException(myException);
            }
        }

        return parInput;
    }

    final Collection<Field> getFields(final Class<?> parClass) {
        final List<Field> myCollection = new LinkedList<Field>();
        for (final Field myField : parClass.getDeclaredFields()) {
            if (Modifier.isTransient(myField.getModifiers()) || myField.isSynthetic()) {
                // ignore transient and synthetic fields.
                continue;
            }
            myCollection.add(myField);
        }
        final Class<?> mySuperClass = parClass.getSuperclass();
        if (mySuperClass != null) {
            myCollection.addAll(getFields(mySuperClass));
        }

        return Collections.unmodifiableCollection(myCollection);
    }

    final Object getValue(final Field parField, final Object parInstance) {
        return AccessController.doPrivileged(new PrivilegedAction<Object>() {
            @Override
            public Object run() {
                final boolean myOriginalValue = isAccessible(parField, parInstance);
                try {
                    parField.setAccessible(true);
                    return parField.get(parInstance);
                    // RuntimeException because we need to catch InaccessibleObjectException
                    // but also compile pre-java 9.
                } catch (final IllegalAccessException myException) {
                    throw new JSONRuntimeException(myException);
                } catch (final RuntimeException myException) {
                    throw new JSONRuntimeException(myException);
                } finally {
                    parField.setAccessible(myOriginalValue);
                }
            }
        });
    }

    final void setValueIfNotNull(final Field parField, final Object parInstance, final Object parValue) {
        if (parValue == null) {
            return;
        }

        AccessController.doPrivileged(new PrivilegedAction<Field>() {
            @Override
            public Field run() {
                final boolean myOriginalValue = isAccessible(parField, parInstance);
                try {
                    parField.setAccessible(true);
                    parField.set(parInstance, parValue);
                    // RuntimeException because we need to catch InaccessibleObjectException
                    // but also compile pre-java 9.
                } catch (final IllegalAccessException myException) {
                    throw new JSONRuntimeException(myException);
                } catch (final RuntimeException myException) {
                    throw new JSONRuntimeException(myException);
                } finally {
                    parField.setAccessible(myOriginalValue);
                }

                return null;
            }
        });
    }

    <U> U construct(final Class<U> parClazz) throws JSONDecodeException {
        final Constructor<? extends U> myConstructor;
        try {
            myConstructor = parClazz.getDeclaredConstructor();
        } catch (final NoSuchMethodException myException) {
            throw new JSONDecodeException(myException);
        }

        return AccessController.doPrivileged(new PrivilegedAction<U>() {
                @Override
                public U run() {
                    // RuntimeException because we need to catch InaccessibleObjectException
                    // but also compile pre-java 9.
                    final boolean myOriginalValue = isAccessible(myConstructor, null);
                    try {
                        myConstructor.setAccessible(true);
                    } catch (final RuntimeException myException) {
                        throw new JSONRuntimeException(myException);
                    }

                    try {
                        return myConstructor.newInstance();
                    } catch (final InstantiationException myException) {
                        throw new JSONRuntimeException(myException);
                    } catch (final IllegalAccessException myException) {
                        throw new JSONRuntimeException(myException);
                    } catch (final InvocationTargetException myException) {
                        throw new JSONRuntimeException(myException);
                    } catch (final SecurityException myException) {
                        throw new JSONRuntimeException(myException);
                    } finally {
                        myConstructor.setAccessible(myOriginalValue);
                    }
                }
            });
    }

    @SuppressWarnings("unchecked")
    final Map<CharSequence, Object> castToMap(final Object parValue) throws JSONException {
        try {
            return (Map) parValue;
        } catch (final ClassCastException myException) {
            throw new JSONException(myException);
        }
    }

    /**
     * @exclude
     */
    static final class SerializationSettings {
        private final CharSequence[] target;
        private final Relativity strategy;

        private SerializationSettings(final CharSequence[] parTarget, final Relativity parStrategy) {
            target = parTarget;
            strategy = parStrategy;
        }

        CharSequence[] getTarget() {
            return target;
        }

        Relativity getStrategy() {
            return strategy;
        }

        @Override
        public boolean equals(final Object parObject) {
            if (this == parObject) {
                return true;
            }
            if (parObject == null || getClass() != parObject.getClass()) {
                return false;
            }
            final SerializationSettings myThat = (SerializationSettings) parObject;
            return Arrays.equals(target, myThat.target);
        }

        @Override
        public int hashCode() {
            return Arrays.hashCode(target);
        }
    }
}
