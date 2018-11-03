package com.chelseaurquhart.securejson;

/**
 * Functional interface that takes one parameter and returns a value.
 *
 * @param <T> The parameter type.
 * @param <R> The return type.
 */
public interface IFunction<T, R> {
    /**
     * Performs this operation on the given argument and returns the result.
     * @param parInput The input argument.
     * @return The processed value.
     */
    R accept(T parInput);
}
