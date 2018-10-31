.. java:import:: java.io Closeable

.. java:import:: java.io IOException

IWritableCharSequence
=====================

.. java:package:: com.chelseaurquhart.securejson
   :noindex:

.. java:type:: public interface IWritableCharSequence extends CharSequence, Closeable, AutoCloseable

   Interface representing a CharSequence that can be written to. It must implement close even if it is a NOOP function.

Methods
-------
append
^^^^^^

.. java:method::  void append(char parChar) throws IOException
   :outertype: IWritableCharSequence

   Append a character to the sequence of characters.

   :param parChar: The character to append.
   :throws IOException: On write failure.

getCapacity
^^^^^^^^^^^

.. java:method::  int getCapacity()
   :outertype: IWritableCharSequence

   Return the max capacity of this buffer.

   :return: The max capacity.

isRestrictedToCapacity
^^^^^^^^^^^^^^^^^^^^^^

.. java:method::  boolean isRestrictedToCapacity()
   :outertype: IWritableCharSequence

   Check if this buffer is restricted to the initial capacity. If it is, we will create more buffers before going over the initial capacity. If it is not, we will expect it to handle resizing itself.

   :return: True if this buffer is restricted to the initial capacity. False otherwise.

