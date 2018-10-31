package com.chelseaurquhart.securejson;

/**
 * Specifies the relativeTo state of a serialization target. Absolute means relative to the root of the JSON map. Note
 * that the absolute position whenever we are inside a collection-like entity (collection, array, or map.) Relative is
 * the default setting and usually makes the most sense.
 */
public enum Relativity {
    /**
     * Absolute relativeTo state means that all fields are serialized relative to the root JSON. In the case of fields
     * that are nested within arrays/sets, the root becomes that array/set.
     */
    ABSOLUTE,

    /**
     * Relative state means that all fields are serialized relative to their position in a Map. This is the default.
     */
    RELATIVE
}
