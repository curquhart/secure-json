package com.chelseaurquhart.securejson;

interface IFunction<T, R> {
    /**
     * Performs this operation on the given argument and returns the result.
     * @param parInput The input argument.
     */
    R accept(T parInput) throws Exception;
}
