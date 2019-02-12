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

import com.chelseaurquhart.securejson.ObjectSerializer.SerializationSettings;

import net.jodah.typetools.TypeResolver;

import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.AbstractCollection;
import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Class for reading a map (from JSON) into an object.
 * @param <T> The Class of the object to be read.
 * @exclude
 */
@SuppressWarnings("unchecked")
final class ObjectReader<T> {
    private final ObjectSerializer objectSerializer = new ObjectSerializer();

    /**
     * A collection of types that we can ignore when we're recursively resolving targets.
     */
    private static final List<? extends Class<?>> IGNORE_RECURSION_TYPES = Collections.unmodifiableList(Arrays.asList(
        boolean.class,
        Boolean.class,
        byte.class,
        Byte.class,
        char.class,
        Character.class,
        short.class,
        Short.class,
        int.class,
        Integer.class,
        float.class,
        Float.class,
        double.class,
        Double.class,
        Number.class,
        CharSequence.class,
        Collection.class,
        List.class,
        Map.class
    ));

    private static final Map<Class<?>, IFunction<Number, Number>> CLASS_MAP;

    static {
        CLASS_MAP = new HashMap<Class<?>, IFunction<Number, Number>>();
        CLASS_MAP.put(byte.class, new IFunction<Number, Number>() {
            @Override
            public Number accept(final Number parInput) {
                return parInput.byteValue();
            }
        });
        CLASS_MAP.put(short.class, new IFunction<Number, Number>() {
            @Override
            public Number accept(final Number parInput) {
                return parInput.shortValue();
            }
        });
        CLASS_MAP.put(int.class, new IFunction<Number, Number>() {
            @Override
            public Number accept(final Number parInput) {
                return parInput.intValue();
            }
        });
        CLASS_MAP.put(float.class, new IFunction<Number, Number>() {
            @Override
            public Number accept(final Number parInput) {
                return parInput.floatValue();
            }
        });
        CLASS_MAP.put(double.class, new IFunction<Number, Number>() {
            @Override
            public Number accept(final Number parInput) {
                return parInput.doubleValue();
            }
        });
        CLASS_MAP.put(long.class, new IFunction<Number, Number>() {
            @Override
            public Number accept(final Number parInput) {
                return parInput.longValue();
            }
        });

        CLASS_MAP.put(Byte.class, CLASS_MAP.get(byte.class));
        CLASS_MAP.put(Short.class, CLASS_MAP.get(short.class));
        CLASS_MAP.put(Integer.class, CLASS_MAP.get(int.class));
        CLASS_MAP.put(Float.class, CLASS_MAP.get(float.class));
        CLASS_MAP.put(Double.class, CLASS_MAP.get(double.class));
        CLASS_MAP.put(Long.class, CLASS_MAP.get(long.class));
    }

    private final transient Class<T> clazz;
    private final transient Settings settings;

    ObjectReader(final Class<T> parClazz, final Settings parSettings) {
        super();
        clazz = parClazz;
        settings = parSettings;
    }

    T accept(final Object parInput) throws IOException, JSONException {
        return buildInstance(parInput, null);
    }

    private T buildInstance(final Object parInput, final Map<CharSequence, Object> parAbsMap) throws IOException,
            JSONException {
        final IFunction<Object, ?> myInitializer = settings.getClassInitializers().get(clazz);
        final T myInstance;
        if (myInitializer != null) {
            myInstance = (T) myInitializer.accept(parInput);
        } else {
            myInstance = objectSerializer.construct(clazz);
        }

        if (myInstance instanceof IJSONDeserializeAware) {
            ((IJSONDeserializeAware) myInstance).fromJSONable(parInput);
        } else if (objectSerializer.isMapType(parInput)) {
            final Map<CharSequence, Object> myMap = objectSerializer.castToMap(parInput);
            final Map<CharSequence, Object> myAbsMap;
            if (parAbsMap == null) {
                myAbsMap = myMap;
            } else {
                myAbsMap = parAbsMap;
            }
            accept(myInstance, myMap, myAbsMap);
        } else {
            throw new JSONException(Messages.get(Messages.Key.ERROR_READ_OBJECT_FROM_NON_MAP_TYPE));
        }

        return myInstance;
    }

    @SuppressWarnings("unchecked")
    private <U> Class<? extends U> getConcreteClass(final Class<? extends U> parClazz) throws IOException,
            JSONException {
        try {
            if (parClazz == Map.class || parClazz == AbstractMap.class) {
                return (Class<? extends U>) LinkedHashMap.class;
            }
            if (parClazz == Set.class || parClazz == AbstractSet.class) {
                return (Class<? extends U>) HashSet.class;
            }
            if (parClazz == Collection.class || parClazz == AbstractCollection.class || parClazz == List.class) {
                return (Class<? extends U>) LinkedList.class;
            }
        } catch (final ClassCastException myException) {
            throw new JSONException(myException);
        }

        if (parClazz.isInterface() || Modifier.isAbstract(parClazz.getModifiers())) {
            throw new JSONException(
                Messages.get(Messages.Key.ERROR_RESOLVE_IMPLEMENTATION).replace(":class", parClazz.getName()));
        }

        return parClazz;
    }

    private void accept(final Object parInstance, final Map<CharSequence, Object> parRelMap,
                        final Map<CharSequence, Object> parAbsMap) throws IOException, JSONException {
        for (final Field myField : objectSerializer.getFields(clazz)) {
            accept(parInstance, myField, parRelMap, parAbsMap);
        }
    }

    private void accept(final Object parInstance, final Field parField, final Map<CharSequence, Object> parRelMap,
                        final Map<CharSequence, Object> parAbsMap)
            throws IOException, JSONException {
        final SerializationSettings mySerializationSettings = objectSerializer.getSerializationSettings(parField);
        final Class<?> myType = parField.getType();
        final Object myValue;
        if (mySerializationSettings.getStrategy() == Relativity.ABSOLUTE) {
            myValue = extractFromMap(parAbsMap, mySerializationSettings.getTarget());
        } else {
            myValue = extractFromMap(parRelMap, mySerializationSettings.getTarget());
        }

        objectSerializer.setValueIfNotNull(parField, parInstance, buildValue(parField.getGenericType(), myType,
            myValue, parAbsMap));

        recursivelyAccept(parInstance, parField, myType, parAbsMap, null);
    }

    private <U> boolean recursivelyAccept(final Object parInstance, final Field parField, final Class<U> parType,
                                          final Map<CharSequence, Object> parAbsMap, final Set<Class<?>> parClassStack)
            throws IOException, JSONException {
        if (!canRecursivelyAccept(parType)) {
            return false;
        }
        Object myInstance = objectSerializer.getValue(parField, parInstance);
        if (myInstance == null) {
            myInstance = objectSerializer.construct(getConcreteClass(parType));
        }

        final ObjectData myData = new ObjectData();
        for (final Field myField : objectSerializer.getFields(parType)) {
            recursivelyAcceptWithData(myInstance, myField, parField, myData, parAbsMap, parClassStack);
        }

        if (myData.foundData) {
            objectSerializer.setValueIfNotNull(parField, parInstance, myInstance);
        }

        return myData.foundData;
    }

    private void recursivelyAcceptWithData(final Object parInstance, final Field parParentField, final Field parField,
                                           final ObjectData parData, final Map<CharSequence, Object> parAbsMap,
                                           final Set<Class<?>> parClassStack) throws JSONException, IOException {
        final SerializationSettings mySerializationSettings = objectSerializer.getSerializationSettings(
            parParentField);

        final Object myValue;
        if (mySerializationSettings.getStrategy() == Relativity.ABSOLUTE) {
            myValue = extractFromMap(parAbsMap, mySerializationSettings.getTarget());
        } else {
            myValue = null;
        }
        final Class<?> myFieldType = parParentField.getType();
        if (canRecursivelyAccept(myFieldType)) {
            recursivelyAcceptWithData(parInstance, mySerializationSettings, myFieldType, myValue, parField,
                    parParentField, parData, parAbsMap, parClassStack);
        } else if (myValue != null) {
            if (parInstance instanceof IJSONDeserializeAware) {
                ((IJSONDeserializeAware) parInstance).fromJSONable(myValue);
            } else {
                final Object myResolvedValue = buildValue(parParentField.getGenericType(), myFieldType, myValue,
                        parAbsMap);
                if (myResolvedValue != null) {
                    objectSerializer.setValueIfNotNull(parParentField, parInstance, myResolvedValue);
                    parData.foundData = true;
                }
            }
        }
    }

    private void recursivelyAcceptWithData(final Object parInstance,
                                           final SerializationSettings parSerializationSettings,
                                           final Class<?> parFieldType, final Object parValue,
                                           final Field parParentField, final Field parField, final ObjectData parData,
                                           final Map<CharSequence, Object> parAbsMap, final Set<Class<?>> parClassStack)
            throws IOException, JSONException {
        final Object mySubInstance = objectSerializer.construct(getConcreteClass(parFieldType));

        boolean myFoundSubData = false;
        if (parSerializationSettings.getStrategy() == Relativity.ABSOLUTE) {
            if (parValue != null) {
                final Object myAcceptedValue = buildObjectReader(parField.getType()).accept(parValue);
                objectSerializer.setValueIfNotNull(parParentField, mySubInstance, buildValue(parField.getGenericType(),
                        parField.getType(), myAcceptedValue, parAbsMap));
                myFoundSubData = myAcceptedValue != null;
            }
        } else {
            final Set<Class<?>> myClassStack;
            final boolean myCreatedStack;
            if (parClassStack == null) {
                myClassStack = buildIdentityHashSet();
                myCreatedStack = true;
            } else {
                myClassStack = parClassStack;
                myCreatedStack = false;
            }
            try {
                // stack (well technically set, backed by IdentityHashMap) exists to watch for recursion
                if (!myClassStack.contains(parFieldType)) {
                    myClassStack.add(parFieldType);
                    if (recursivelyAccept(parInstance, parField, parFieldType, parAbsMap, myClassStack)) {
                        myFoundSubData = true;
                    }
                }
            } finally {
                if (myCreatedStack) {
                    myClassStack.clear();
                }
            }
        }

        if (myFoundSubData) {
            parData.foundData = true;
        }
    }

    private Set<Class<?>> buildIdentityHashSet() {
        return new IdentityHashSet<Class<?>>();
    }

    private <U> ObjectReader<U> buildObjectReader(final Class<U> parType) {
        return new ObjectReader<U>(parType, settings);
    }

    private boolean canRecursivelyAccept(final Class<?> parFieldType) {
        if (parFieldType.isArray() || parFieldType.isEnum()) {
            return false;
        }
        for (final Class<?> myRecursionType : IGNORE_RECURSION_TYPES) {
            if (myRecursionType.isAssignableFrom(parFieldType)) {
                return false;
            }
        }

        return true;
    }

    @SuppressWarnings("unchecked")
    private <U> Object buildValue(final Type parGenericType, final Class<?> parType, final Object parValue,
                              final Map<CharSequence, Object> parAbsMap)
            throws IOException, JSONException {
        final Class<?> myType;
        if (parType == Object.class && parValue != null) {
            myType = deAnonymize(parValue.getClass());
        } else {
            myType = parType;
        }

        if (parValue == null) {
            return null;
        } else if (isMap(myType, parValue)) {
            return buildMapValue(parGenericType, myType, (Map) parValue);
        } else if (isCollection(myType, parValue)) {
            return buildCollectionValue(parGenericType, myType, (Collection) parValue);
        } else if (isArray(myType, parValue)) {
            return buildArrayValue(myType, parValue);
        } else if (isBoolean(myType, parValue)) {
            return parValue;
        } else if (isNumber(myType, parValue)) {
            return buildNumberValue(myType, (Number) parValue);
        } else if (CharSequence.class.isAssignableFrom(myType)) {
            return buildStringValue(myType, (CharSequence) parValue, settings.isStrictStrings());
        } else if (isEnum(myType, parValue)) {
            return buildEnumValue(myType, (CharSequence) parValue);
        } else {
            return new ObjectReader<U>((Class<U>) myType, settings).buildInstance(parValue, parAbsMap);
        }
    }

    private Object buildEnumValue(final Class<?> parType, final CharSequence parValue) throws IOException,
            JSONException {
        for (final Object myObject : parType.getEnumConstants()) {
            final String myValueString = myObject.toString();
            if (myValueString != null && myValueString.contentEquals(parValue)) {
                return myObject;
            }
        }

        throw new JSONException(Messages.get(Messages.Key.ERROR_INVALID_TOKEN));
    }

    private boolean isEnum(final Class<?> parType, final Object parValue) {
        return parType.isEnum() && parValue instanceof CharSequence;
    }

    private boolean isArray(final Class<?> parType, final Object parValue) {
        return parType.isArray() && parValue.getClass().isArray();
    }

    private boolean isCollection(final Class<?> parType, final Object parValue) {
        return Collection.class.isAssignableFrom(parType) && parValue instanceof Collection;
    }

    private boolean isBoolean(final Class<?> parType, final Object parValue) {
        return (Boolean.class.isAssignableFrom(parType) && parValue instanceof Boolean) || parType == boolean.class;
    }

    private boolean isMap(final Class<?> parType, final Object parValue) {
        return Map.class.isAssignableFrom(parType) && parValue instanceof Map;
    }

    private boolean isNumber(final Class<?> parType, final Object parValue) {
        return (Number.class.isAssignableFrom(parType) && parValue instanceof Number)
            || parType == short.class || parType == int.class || parType == long.class
            || parType == float.class || parType == double.class || parType == byte.class;
    }

    private Class<?> deAnonymize(final Class<?> parClass) {
        final Class<?> mySuperClass = parClass.getSuperclass();

        if (mySuperClass != null && parClass.isAnonymousClass()) {
            return deAnonymize(mySuperClass);
        }

        return parClass;
    }

    private Object buildStringValue(final Class<?> parType, final CharSequence parValue, final boolean parStrict) {
        if (parType == String.class && !(parValue instanceof String) && !parStrict) {
            // if we expect a String, and the value isn't a string, and we're not in strict mode, convert.
            final StringBuilder myStringBuilder = new StringBuilder();
            final int myValueLength = parValue.length();
            for (int myIndex = 0; myIndex < myValueLength; myIndex++) {
                myStringBuilder.append(parValue.charAt(myIndex));
            }

            return myStringBuilder.toString();
        }

        return parValue;
    }

    @SuppressWarnings("unchecked")
    private Object buildMapValue(final Type parGenericType, final Class<?> parType, final Map<?, ?> parValue)
            throws IOException, JSONException {
        final Type myType = TypeResolver.resolveGenericType(parType, parGenericType);
        final Class<?>[] myClasses = getGenericArgClasses(myType, 2, parType);

        if (!CharSequence.class.isAssignableFrom(myClasses[0]) && myClasses[0] != Object.class) {
            throw new JSONException(Messages.get(Messages.Key.ERROR_INVALID_MAP_KEY_TYPE));
        }

        final Map<Object, Object> myMap;
        try {
            myMap = objectSerializer.construct(getConcreteClass((Class<Map<Object, Object>>) parType));
        } catch (final ClassCastException myException) {
            throw new JSONException(myException);
        }
        final Type[] myArgs = getGenericTypes(myType, 2);
        for (final Map.Entry<?, ?> myEntry : parValue.entrySet()) {
            final Object myKey = buildStringValue(myClasses[0], (CharSequence) myEntry.getKey(),
                settings.isStrictMapKeyTypes());

            myMap.put(myKey, buildValue(myArgs[1], myClasses[1], myEntry.getValue(), null));
        }

        return myMap;
    }

    @SuppressWarnings("unchecked")
    private Object buildCollectionValue(final Type parGenericType, final Class<?> parType,
                                        final Collection<?> parValue) throws IOException, JSONException {
        final Collection<Object> myCollection;
        try {
            myCollection = objectSerializer.construct(getConcreteClass((Class<Collection<Object>>) parType));
        } catch (final ClassCastException myException) {
            throw new JSONException(myException);
        }
        final Type myType = TypeResolver.resolveGenericType(parType, parGenericType);
        final Type[] myArgs = getGenericTypes(myType, 1);
        final Class<?>[] myClasses = getGenericArgClasses(myType, 1, parType);
        for (final Object myEntry : parValue) {
            myCollection.add(buildValue(myArgs[0], myClasses[0], myEntry, null));
        }

        return myCollection;
    }

    @SuppressWarnings("unchecked")
    private Object buildArrayValue(final Class<?> parType, final Object parValue)
            throws IOException, JSONException {
        final Class<?> myClass = parType.getComponentType();

        final int myLength = Array.getLength(parValue);
        final Object myArray = Array.newInstance(myClass, myLength);
        for (int myIndex = 0; myIndex < myLength; myIndex++) {
            try {
                final Object myValue = Array.get(parValue, myIndex);
                Array.set(myArray, myIndex, buildValue(myClass, myClass, myValue, null));
            } catch (final IllegalArgumentException myException) {
                throw new JSONException(myException);
            }
        }

        return myArray;
    }

    private Type[] getGenericTypes(final Type parType, final int parCount) {
        final Type[] myTypes;
        if (parType instanceof ParameterizedType) {
            final ParameterizedType myParameterizedType = (ParameterizedType) parType;
            myTypes = myParameterizedType.getActualTypeArguments();
        } else {
            myTypes = new Type[parCount];
            Arrays.fill(myTypes, Object.class);
        }

        return myTypes;
    }

    private Class<?>[] getGenericArgClasses(final Type parGenericType, final int parCount,
                                            final Class<?> parInterfaceClass) {
        final Class<?>[] myClasses = TypeResolver.resolveRawArguments(parGenericType, parInterfaceClass);

        if (myClasses == null) {
            final Class<?>[] myDefaultClasses = new Class<?>[parCount];
            Arrays.fill(myDefaultClasses, Object.class);

            return myDefaultClasses;
        }

        for (int myIndex = 0; myIndex < myClasses.length; myIndex++) {
            if (myClasses[myIndex] == TypeResolver.Unknown.class) {
                myClasses[myIndex] = Object.class;
            }
        }

        return myClasses;
    }

    private Object buildNumberValue(final Class<?> parType, final Number parValue) throws JSONException {
        // Convert to the expected data type.
        try {
            final IFunction<Number, Number> myConverter = CLASS_MAP.get(parType);
            if (myConverter != null) {
                return myConverter.accept(parValue);
            }
            final Class<?> myValueClass = parValue.getClass();
            if (parType == BigInteger.class && myValueClass == BigDecimal.class) {
                return ((BigDecimal) parValue).toBigInteger();
            }
            if (parType == BigDecimal.class && myValueClass == BigInteger.class) {
                return new BigDecimal((BigInteger) parValue);
            }
            if (parType == BigInteger.class && myValueClass == HugeDecimal.class) {
                return ((HugeDecimal) parValue).bigIntegerValue();
            }
            if (parType == BigDecimal.class && myValueClass == HugeDecimal.class) {
                return ((HugeDecimal) parValue).bigDecimalValue();
            }
            if (parType == HugeDecimal.class && myValueClass != HugeDecimal.class) {
                return new HugeDecimal(parValue);
            }
        } catch (final NumberFormatException myException) {
            // conversion failure. This is specific to HugeDecimal in that it will disallow conversions that will
            // cause data loss, but there could be other custom implementations of Number.
            throw new JSONException(myException);
        } catch (final ArithmeticException myException) {
            // conversion failure. This is specific to HugeDecimal in that it will disallow conversions that will
            // cause data loss, but there could be other custom implementations of Number.
            throw new JSONException(myException);
        } catch (final IOException myException) {
            // conversion failure. This is specific to HugeDecimal in that it will disallow conversions that will
            // cause data loss, but there could be other custom implementations of Number.
            throw new JSONException(myException);
        }

        return parValue;
    }

    private Object extractFromMap(final Map<CharSequence, Object> parMap, final CharSequence[] parTarget)
            throws JSONException {
        Map<CharSequence, Object> myMap = parMap;
        Object myResult = null;

        for (final CharSequence myTarget : parTarget) {
            if (myMap == null) {
                return null;
            }
            myResult = myMap.get(myTarget);

            if (myResult == null) {
                return null;
            }

            if (myResult instanceof Map) {
                myMap = objectSerializer.castToMap(myResult);
            } else {
                myMap = null;
            }
        }

        return myResult;
    }

    /**
     * IdentityHashMap-backed set.
     *
     * @param <T> The element type.
     */
    static class IdentityHashSet<T> implements Set<T> {
        private final transient IdentityHashMap<T, Void> identityHashMap = new IdentityHashMap<T, Void>();

        @Override
        public int size() {
            throw new NotImplementedException(Messages.Key.ERROR_NOT_IMPLEMENTED, "size");
        }

        @Override
        public boolean isEmpty() {
            return identityHashMap.isEmpty();
        }

        @Override
        public boolean contains(final Object parKey) {
            return identityHashMap.containsKey(parKey);
        }

        @Override
        public Iterator<T> iterator() {
            throw new NotImplementedException(Messages.Key.ERROR_NOT_IMPLEMENTED, "iterator");
        }

        @Override
        public Object[] toArray() {
            throw new NotImplementedException(Messages.Key.ERROR_NOT_IMPLEMENTED, "toArray");
        }

        @Override
        public <T1> T1[] toArray(final T1[] parArrayClass) {
            throw new NotImplementedException(Messages.Key.ERROR_NOT_IMPLEMENTED, "toArray");
        }

        @Override
        public boolean add(final T parKey) {
            final boolean myHasKey = contains(parKey);
            if (!myHasKey) {
                identityHashMap.put(parKey, null);
            }
            return !myHasKey;
        }

        @Override
        public boolean remove(final Object parKey) {
            throw new NotImplementedException(Messages.Key.ERROR_NOT_IMPLEMENTED, "remove");
        }

        @Override
        public boolean containsAll(final Collection<?> parCollection) {
            throw new NotImplementedException(Messages.Key.ERROR_NOT_IMPLEMENTED, "containsAll");
        }

        @Override
        public boolean addAll(final Collection<? extends T> parCollection) {
            throw new NotImplementedException(Messages.Key.ERROR_NOT_IMPLEMENTED, "addAll");
        }

        @Override
        public boolean retainAll(final Collection<?> parCollection) {
            throw new NotImplementedException(Messages.Key.ERROR_NOT_IMPLEMENTED, "retainAll");
        }

        @Override
        public boolean removeAll(final Collection<?> parCollection) {
            throw new NotImplementedException(Messages.Key.ERROR_NOT_IMPLEMENTED, "removeAll");
        }

        @Override
        public void clear() {
            identityHashMap.clear();
        }
    }

    /**
     * Mutable data for the object reader to pass around.
     */
    private static class ObjectData {
        boolean foundData;
    }
}
