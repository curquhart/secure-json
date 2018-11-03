package com.chelseaurquhart.securejson;

import java.io.IOException;

/**
 * @exclude
 */
interface IObjectMutator extends IFunction<Object, Object> {
    /**
     * Mutates a given object to a simple type that we can process.
     * @param parInput The input argument.
     * @return The processed object.
     */
    Object accept(Object parInput) throws IOException;
}
