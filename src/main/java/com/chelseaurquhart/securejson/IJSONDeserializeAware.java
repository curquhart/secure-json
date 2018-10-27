package com.chelseaurquhart.securejson;

/**
 * Represents an object that can be deserialized.
 */
public interface IJSONDeserializeAware {
    /**
     * Import the data for this instance from the provided object.
     * @param parInput The input value to import.
     */
    void fromJSONable(Object parInput);
}
