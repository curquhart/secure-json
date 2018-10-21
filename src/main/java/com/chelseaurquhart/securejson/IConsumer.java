package com.chelseaurquhart.securejson;

interface IConsumer<T> {
    /**
     * Performs this operation on the given argument.
     * @param parInput The input argument.
     */
    void accept(T parInput);
}
