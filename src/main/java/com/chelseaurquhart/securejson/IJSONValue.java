package com.chelseaurquhart.securejson;

/**
 * Interface used to inject already serialized JSON into JSON output.
 */
public interface IJSONValue {
    /**
     * Gets the JSON representation of this object.
     *
     * @return The serialized JSON value.
     */
    CharSequence getValue();
}
