package com.chelseaurquhart.securejson;

/**
 * Represents an object that can be serialized.
 */
public interface IJSONSerializeAware {
    /**
     * Return a representation that can be JSON-serialized.
     *
     * @return A JSON-serializable representation.
     */
    Object toJSONable();
}
