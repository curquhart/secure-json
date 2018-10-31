IFunction
=========

.. java:package:: com.chelseaurquhart.securejson
   :noindex:

.. java:type:: public interface IFunction<T, R>

   Functional interface that takes one parameter and returns a value.

   :param <T>: The parameter type.
   :param <R>: The return type.

Methods
-------
accept
^^^^^^

.. java:method::  R accept(T parInput) throws Exception
   :outertype: IFunction

   Performs this operation on the given argument and returns the result.

   :param parInput: The input argument.
   :throws Exception: On error.

