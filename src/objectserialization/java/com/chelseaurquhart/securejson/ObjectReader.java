package com.chelseaurquhart.securejson;

import com.chelseaurquhart.securejson.util.StringUtil;
import net.jodah.typetools.TypeResolver;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
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
 */
public class ObjectReader<T> extends ObjectSerializer {
    /**
     * A collection of types that we can ignore when we're recursively resolving targets.
     */
    private static List<? extends Class<?>> IGNORE_RECURSION_TYPES = Collections.unmodifiableList(Arrays.asList(
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

    private final Class<T> clazz;

    ObjectReader(final Class<T> parClazz) {
        clazz = parClazz;
    }

    @SuppressWarnings("unchecked")
    final T accept(final Object parInput) throws JSONException {
        return buildInstance(parInput, null);
    }

    private T buildInstance(final Object parInput, final Map<CharSequence, Object> parAbsMap) throws JSONException {
        final T myInstance = construct(clazz);

        if (myInstance instanceof IJSONDeserializeAware) {
            ((IJSONDeserializeAware) myInstance).fromJSONable(parInput);
        } else if (isMapType(parInput)) {
            final Map<CharSequence, Object> myMap = castToMap(parInput);
            final Map<CharSequence, Object> myAbsMap;
            if (parAbsMap == null) {
                myAbsMap = myMap;
            } else {
                myAbsMap = parAbsMap;
            }
            accept(myInstance, myMap, myAbsMap);
        } else {
            throw new JSONDecodeException(new Exception("cannot read object from non-map"));
        }

        return myInstance;
    }

    private <U> U construct(final Class<U> parClazz) throws JSONException {
        final Class<? extends U> myClazz = getConcreteClass(parClazz);

        try {
            final Constructor<? extends U> myConstructor = myClazz.getDeclaredConstructor();
            myConstructor.setAccessible(true);
            return myConstructor.newInstance();
        } catch (NoSuchMethodException | IllegalAccessException | InstantiationException
                | InvocationTargetException | ClassCastException myException) {
            throw new JSONDecodeException(myException);
        }
    }

    @SuppressWarnings("unchecked")
    private <U> Class<? extends U> getConcreteClass(final Class<? extends U> parClazz) throws JSONException {
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
            throw new JSONException(String.format("Could not resolve concrete implementation for %s",
                parClazz.getName()));
        }

        return parClazz;
    }

    private void accept(final Object parInstance, final Map<CharSequence, Object> parRelMap,
                        final Map<CharSequence, Object> parAbsMap) throws JSONException {
        for (final Field myField : getFields(clazz)) {
            accept(parInstance, myField, parRelMap, parAbsMap);
        }
    }

    private void accept(final Object parInstance, final Field parField, final Map<CharSequence, Object> parRelMap,
                        final Map<CharSequence, Object> parAbsMap)
            throws JSONException {
        final SerializationSettings mySerializationSettings = getSerializationSettings(parField);
        final Class<?> myType = parField.getType();
        final Object myValue;
        if (mySerializationSettings.getStrategy() == Relativity.ABSOLUTE) {
            myValue = extractFromMap(parAbsMap, mySerializationSettings.getTarget());
        } else {
            myValue = extractFromMap(parRelMap, mySerializationSettings.getTarget());
        }

        setValueIfNotNull(parField, parInstance, buildValue(parField.getGenericType(), myType, myValue, parAbsMap));

        recursivelyAccept(parInstance, parField, myType, parAbsMap, null);
    }

    private <U> boolean recursivelyAccept(final Object parInstance, final Field parField, final Class<U> parType,
                                       final Map<CharSequence, Object> parAbsMap, final Set<Class<?>> parClassStack)
            throws JSONException {
        if (!canRecursivelyAccept(parType)) {
            return false;
        }
        Object myInstance = getValue(parField, parInstance);
        if (myInstance == null) {
            myInstance = construct(parType);
        }

        boolean myFoundData = false;
        for (final Field myField : getFields(parType)) {
            final SerializationSettings mySerializationSettings = getSerializationSettings(myField);

            final Object myValue;
            if (mySerializationSettings.getStrategy() == Relativity.ABSOLUTE) {
                myValue = extractFromMap(parAbsMap, mySerializationSettings.getTarget());
            } else {
                myValue = null;
            }
            final Class<?> myFieldType = myField.getType();
            if (canRecursivelyAccept(myFieldType)) {
                final Object mySubInstance = construct(myFieldType);

                boolean myFoundSubData = false;
                if (mySerializationSettings.getStrategy() == Relativity.ABSOLUTE) {
                    if (myValue != null) {
                        final Object myAcceptedValue = new ObjectReader<>(myField.getType()).accept(myValue);
                        setValueIfNotNull(parField, mySubInstance, buildValue(myField.getGenericType(),
                            myField.getType(), myAcceptedValue, parAbsMap));
                        myFoundSubData = myAcceptedValue != null;
                    }
                } else {
                    final Set<Class<?>> myClassStack;
                    final boolean myCreatedStack;
                    if (parClassStack == null) {
                        myClassStack = new IdentityHashSet<>();
                        myCreatedStack = true;
                    } else {
                        myClassStack = parClassStack;
                        myCreatedStack = false;
                    }
                    try {
                        // stack (well technically set, backed by IdentityHashMap) exists to watch for recursion
                        if (!myClassStack.contains(myFieldType)) {
                            myClassStack.add(myFieldType);
                            if (recursivelyAccept(myInstance, myField, myFieldType, parAbsMap, myClassStack)) {
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
                    myFoundData = true;
                }
            } else if (myValue != null) {
                if (myInstance instanceof IJSONDeserializeAware) {
                    ((IJSONDeserializeAware) myInstance).fromJSONable(myValue);
                } else {
                    final Object myResolvedValue = buildValue(myField.getGenericType(), myFieldType, myValue,
                        parAbsMap);
                    if (myResolvedValue != null) {
                        setValueIfNotNull(myField, myInstance, myResolvedValue);
                        myFoundData = true;
                    }
                }
            }
        }

        if (myFoundData) {
            setValueIfNotNull(parField, parInstance, myInstance);
        }

        return myFoundData;
    }

    private boolean canRecursivelyAccept(final Class<?> parFieldType) {
        if (parFieldType.isArray()) {
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
    private Object buildValue(final Type parGenericType, final Class<?> parType, final Object parValue,
                              final Map<CharSequence, Object> parAbsMap)
            throws JSONException {
        final Class<?> myType;
        if (parType == Object.class && parValue != null) {
            myType = deAnonymize(parValue.getClass());
        } else {
            myType = parType;
        }

        if (parValue == null) {
            return null;
        } else if (Map.class.isAssignableFrom(myType) && parValue instanceof Map) {
            return buildMapValue(parGenericType, myType, (Map) parValue);
        } else if (Collection.class.isAssignableFrom(myType) && parValue instanceof Collection) {
            return buildCollectionValue(parGenericType, myType, (Collection) parValue);
        } else if (myType.isArray() && parValue.getClass().isArray()) {
            return buildArrayValue(myType, parValue);
        } else if ((Boolean.class.isAssignableFrom(myType) && parValue instanceof Boolean)
                || myType == boolean.class) {
            return parValue;
        } else if ((Number.class.isAssignableFrom(myType) && parValue instanceof Number)
                || myType == short.class || myType == int.class || myType == long.class
                || myType == float.class || myType == double.class) {
            return buildNumberValue(myType, (Number) parValue);
        } else if (CharSequence.class.isAssignableFrom(myType)) {
            return buildStringValue(myType, (CharSequence) parValue);
        } else {
            return new ObjectReader<>(myType).buildInstance(parValue, parAbsMap);
        }
    }

    private Class<?> deAnonymize(final Class<?> parClass) {
        final Class<?> mySuperClass = parClass.getSuperclass();

        if (mySuperClass != null && parClass.isAnonymousClass()) {
            return deAnonymize(mySuperClass);
        }

        return parClass;
    }

    private Object buildStringValue(final Class<?> parType, final CharSequence parValue) {
        if (parType == String.class) {
            return StringUtil.charSequenceToString(parValue);
        }

        return parValue;
    }

    @SuppressWarnings("unchecked")
    private Object buildMapValue(final Type parGenericType, final Class<?> parType, final Map<?, ?> parValue)
            throws JSONException {
        final Type myType = TypeResolver.resolveGenericType(parType, parGenericType);
        final Type[] myArgs = getGenericTypes(myType, 2);
        final Class[] myClasses = getGenericArgClasses(myType, 2, parType);

        if (!CharSequence.class.isAssignableFrom(myClasses[0]) && myClasses[0] != Object.class) {
            throw new JSONException("Map keys must implement CharSequence or be generic Objects");
        }

        final Map<Object, Object> myMap;
        try {
            myMap = construct((Class<Map<Object, Object>>) parType);
        } catch (final ClassCastException myException) {
            throw new JSONException(myException);
        }
        for (final Map.Entry<?, ?> myEntry : parValue.entrySet()) {
            final Object myKey = buildStringValue(myClasses[0], (CharSequence) myEntry.getKey());

            myMap.put(myKey, buildValue(myArgs[1], myClasses[1], myEntry.getValue(), null));
        }

        return myMap;
    }

    @SuppressWarnings("unchecked")
    private Object buildCollectionValue(final Type parGenericType, final Class<?> parType,
                                        final Collection<?> parValue) throws JSONException {
        final Type myType = TypeResolver.resolveGenericType(parType, parGenericType);
        final Type[] myArgs = getGenericTypes(myType, 1);
        final Class[] myClasses = getGenericArgClasses(myType, 1, parType);

        final Collection<Object> myCollection;
        try {
            myCollection = construct((Class<Collection<Object>>) parType);
        } catch (final ClassCastException myException) {
            throw new JSONException(myException);
        }
        for (final Object myEntry : parValue) {
            myCollection.add(buildValue(myArgs[0], myClasses[0], myEntry, null));
        }

        return myCollection;
    }

    @SuppressWarnings("unchecked")
    private Object buildArrayValue(final Class<?> parType, final Object parValue)
            throws JSONException {
        final Class myClass = parType.getComponentType();

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

    private Class[] getGenericArgClasses(final Type parGenericType, final int parCount,
                                         final Class<?> parInterfaceClass) {
        final Class[] myClasses = TypeResolver.resolveRawArguments(parGenericType, parInterfaceClass);

        if (myClasses == null) {
            final Class[] myDefaultClasses = new Class[parCount];
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
            if (parType == short.class || parType == Short.class) {
                return parValue.shortValue();
            }
            if (parType == int.class || parType == Integer.class) {
                return parValue.intValue();
            }
            if (parType == long.class || parType == Long.class) {
                return parValue.longValue();
            }
            if (parType == float.class || parType == Float.class) {
                return parValue.floatValue();
            }
            if (parType == double.class || parType == Double.class) {
                return parValue.doubleValue();
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
        } catch (final Throwable myException) {
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
                myMap = castToMap(myResult);
            } else {
                myMap = null;
            }
        }

        return myResult;
    }

    private static class IdentityHashSet<T> implements Set<T> {
        private final IdentityHashMap<T, Void> identityHashMap = new IdentityHashMap<>();

        @Override
        public int size() {
            return identityHashMap.size();
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
            return identityHashMap.keySet().iterator();
        }

        @Override
        public Object[] toArray() {
            return identityHashMap.keySet().toArray();
        }

        @Override
        public <T1> T1[] toArray(final T1[] parArrayClass) {
            return identityHashMap.keySet().toArray(parArrayClass);
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
            final boolean myHasKey = contains(parKey);
            if (myHasKey) {
                identityHashMap.remove(parKey);
            }

            return myHasKey;
        }

        @Override
        public boolean containsAll(final Collection<?> parCollection) {
            return identityHashMap.keySet().containsAll(parCollection);
        }

        @Override
        public boolean addAll(final Collection<? extends T> parCollection) {
            boolean myAdded = false;
            for (final T myObject : parCollection) {
                if (add(myObject)) {
                    myAdded = true;
                }
            }

            return myAdded;
        }

        @Override
        public boolean retainAll(final Collection<?> parCollection) {
            throw new UnsupportedOperationException("retainAll is not implemented");
        }

        @Override
        public boolean removeAll(final Collection<?> parCollection) {
            throw new UnsupportedOperationException("removeAll is not implemented");
        }

        @Override
        public void clear() {
            identityHashMap.clear();
        }
    }
}
