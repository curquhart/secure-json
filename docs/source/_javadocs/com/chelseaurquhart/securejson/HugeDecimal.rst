.. java:import:: java.io IOException

.. java:import:: java.io NotSerializableException

.. java:import:: java.io ObjectInputStream

.. java:import:: java.io ObjectOutputStream

.. java:import:: java.math BigDecimal

.. java:import:: java.math BigInteger

.. java:import:: java.util Objects

HugeDecimal
===========

.. java:package:: com.chelseaurquhart.securejson
   :noindex:

.. java:type:: public class HugeDecimal extends Number implements CharSequence

   HugeDecimal is for numbers that are too big to parse with BigDecimal through normal means.

Methods
-------
bigDecimalValue
^^^^^^^^^^^^^^^

.. java:method:: public final BigDecimal bigDecimalValue() throws IOException
   :outertype: HugeDecimal

   Get our value converted to a BigDecimal.

   :throws IOException: On error.
   :return: a BigDecimal representation of our character sequence.

bigIntegerValue
^^^^^^^^^^^^^^^

.. java:method:: public final BigInteger bigIntegerValue() throws IOException
   :outertype: HugeDecimal

   Get our value converted to a BigInteger.

   :throws IOException: On error.
   :return: a BigInteger representation of our character sequence.

charAt
^^^^^^

.. java:method:: @Override public final char charAt(int parIndex)
   :outertype: HugeDecimal

charSequenceValue
^^^^^^^^^^^^^^^^^

.. java:method:: public final CharSequence charSequenceValue()
   :outertype: HugeDecimal

   Get our raw character sequence value.

   :return: Our raw character sequence value.

doubleValue
^^^^^^^^^^^

.. java:method:: @Override public final double doubleValue()
   :outertype: HugeDecimal

   Get our double value.

   :return: A double representation of our value.

equals
^^^^^^

.. java:method:: @Override public final boolean equals(Object parObject)
   :outertype: HugeDecimal

floatValue
^^^^^^^^^^

.. java:method:: @Override public final float floatValue()
   :outertype: HugeDecimal

   Get our float value.

   :return: A float representation of our value.

hashCode
^^^^^^^^

.. java:method:: @Override public final int hashCode()
   :outertype: HugeDecimal

intValue
^^^^^^^^

.. java:method:: @Override public final int intValue()
   :outertype: HugeDecimal

   Get our integer value.

   :return: An integer representation of our value.

length
^^^^^^

.. java:method:: @Override public final int length()
   :outertype: HugeDecimal

longValue
^^^^^^^^^

.. java:method:: @Override public final long longValue()
   :outertype: HugeDecimal

   Get our long value.

   :return: A long representation of our value.

subSequence
^^^^^^^^^^^

.. java:method:: @Override public final CharSequence subSequence(int parStart, int parEnd)
   :outertype: HugeDecimal

