com.chelseaurquhart.securejson
==============================

Classes to securely translate to and from JSON by avoiding the use of strings which are too long-lived in memory, and ensuring that the buffers that are potentially storing sensitive information are immediately erased after consumption.

:author: Chelsea Urquhart

.. java:package:: com.chelseaurquhart.securejson

.. toctree::
   :maxdepth: 1

   HugeDecimal
   IConsumer
   IFunction
   IJSONAware
   IJSONDeserializeAware
   IJSONSerializeAware
   IWritableCharSequence
   JSONDecodeException
   JSONDecodeException-EmptyJSONException
   JSONDecodeException-ExtraCharactersException
   JSONDecodeException-InvalidTokenException
   JSONDecodeException-MalformedJSONException
   JSONDecodeException-MalformedListException
   JSONDecodeException-MalformedMapException
   JSONDecodeException-MalformedNumberException
   JSONDecodeException-MalformedStringException
   JSONDecodeException-MalformedUnicodeValueException
   JSONEncodeException
   JSONEncodeException-InvalidTypeException
   JSONException
   Relativity
   SecureJSON
   SecureJSON-Builder
   Serialize

