package com.chelseaurquhart.securejson;

/**
 * Functional interface that consumes a value.
 *
 * @param <T> The type of value to consume.
 */
public interface IConsumer<T> {
    /**
     * Performs this operation on the given argument.
     * @param parInput The input argument.
     * @throws Exception on error.
     */
    void accept(T parInput) throws Exception;
}
