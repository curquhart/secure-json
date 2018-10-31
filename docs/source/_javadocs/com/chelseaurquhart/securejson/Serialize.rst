.. java:import:: java.lang.annotation ElementType

.. java:import:: java.lang.annotation Retention

.. java:import:: java.lang.annotation RetentionPolicy

.. java:import:: java.lang.annotation Target

Serialize
=========

.. java:package:: com.chelseaurquhart.securejson
   :noindex:

.. java:type:: @Retention @Target public @interface Serialize

   The Serialize annotation provides settings for JSON serialization.

   name can take a String or Array of Strings. It defines where the value is read/written from. Using an array will use nested values.

   relativeTo takes a \ `Relativity <Relativity.html>`_\  value. It can be either RELATIVE or ABSOLUTE. Note that the specific meaning of ABSOLUTE changes when working with collection-like entities (Array, Collection, Map) in that the root becomes that collection when fields are contained within.

