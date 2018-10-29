package com.chelseaurquhart.securejson;

/**
 * Specifies the relativeTo state of a serialization target.
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
