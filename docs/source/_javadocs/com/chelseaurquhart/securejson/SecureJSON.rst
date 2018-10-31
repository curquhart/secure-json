.. java:import:: java.io ByteArrayInputStream

.. java:import:: java.io InputStream

.. java:import:: java.io OutputStream

.. java:import:: java.nio.charset Charset

.. java:import:: java.nio.charset StandardCharsets

SecureJSON
==========

.. java:package:: com.chelseaurquhart.securejson
   :noindex:

.. java:type:: public final class SecureJSON

   SecureJSON is a JSON serializer and deserializer with strict security in mind. It does not create strings due to their inclusion in garbage collectible heap (which can make them potentially snoopable). See https://medium.com/@_west_on/protecting-strings-in-jvm-memory-84c365f8f01c for the motivations around this.

   .. DANGER:: Please note that this is not a substitution for secure coding practices or maintaining a secure environment. If an attacker has access to your JVM's memory, there isn't really anything you can do to guarantee that they can't see sensitive data, but the fleeting nature of data managed in this manner helps ensure that sensitive information is not kept in memory any longer than is necessary and as such helps to mitigate the risks.

Constructors
------------
SecureJSON
^^^^^^^^^^

.. java:constructor:: public SecureJSON()
   :outertype: SecureJSON

   Build a SecureJSON instance with default settings.

Methods
-------
fromJSON
^^^^^^^^

.. java:method:: public <T> void fromJSON(CharSequence parInput, IConsumer<T> parConsumer) throws JSONException
   :outertype: SecureJSON

   Convert a JSON character sequence to an object that consumer will accept. Throws JSONException on failure. After the consumer returns, all buffers we created while parsing the JSON character sequence will be destroyed.

   Example:

   ::

          import com.chelseaurquhart.securejson.SecureJSON;
          final SecureJSON secureJSON = new SecureJSON();
          try {
              secureJSON.fromJSON("{}", new IConsumer>() {
                  @Override
                  public void accept(final Map input) {
                      // do something with input
                  }
              });
          } catch (final JSONException e) {
          }
          // Warning: Any buffers we created for the Map above will be destroyed at this point, so they should
          // be either consumed in accept, or converted to strings (if they do not contain sensitive information.)

   :param parInput: The input character sequence to deserialize.
   :param parConsumer: The consumer to call with our unserialized JSON value.
   :param <T>: The type of object we expect. JSONDecodeException will be thrown if this is wrong. Note that Object (which will accept anything) is acceptable.
   :throws JSONException: On decode failure.

fromJSON
^^^^^^^^

.. java:method:: public <T> void fromJSON(CharSequence parInput, IConsumer<T> parConsumer, Class<T> parClass) throws JSONException
   :outertype: SecureJSON

   Convert a JSON character sequence to an object that consumer will accept. Throws JSONException on failure. After the consumer returns, all buffers we created while parsing the JSON character sequence will be destroyed. This is very similar to its counterpart that doesn't take a class argument, but this supports encoding into that class instead of into java types.

   Example:

   ::

          import com.chelseaurquhart.securejson.SecureJSON;
          final SecureJSON secureJSON = new SecureJSON();

          // This class will be read into.
          class MyCustomClass {
              private int myNumber;
              private CharSequence myString;
          }
          try {
              secureJSON.fromJSON("{}", new IConsumer() {
                  @Override
                  public void accept(final MyCustomClass input) {
                      // do something with input
                  }
              }, MyCustomClass.class);
          } catch (final JSONException e) {
          }
          // Warning: Any buffers we created for the MyCustomClass instance above will be destroyed at this point,
          // so they should be either consumed in accept, or converted to strings (if they do not contain sensitive
          // information.)

   :param parInput: The input character sequence to deserialize.
   :param parConsumer: The consumer to call with our unserialized JSON value.
   :param parClass: The class we will be building.
   :param <T>: The type of object we expect. JSONDecodeException will be thrown if this is wrong. Note that Object (which will accept anything) is acceptable.
   :throws JSONException: On decode failure.

fromJSON
^^^^^^^^

.. java:method:: public <T> void fromJSON(byte[] parInput, IConsumer<T> parConsumer) throws JSONException
   :outertype: SecureJSON

   Convert a JSON byte array to an object that consumer will accept. Throws JSONException on failure. After the consumer returns, all buffers we created while parsing the JSON character sequence will be destroyed.

   Example:

   ::

          import com.chelseaurquhart.securejson.SecureJSON;
          final SecureJSON secureJSON = new SecureJSON();
          try {
              secureJSON.fromJSON("{}".getBytes(), new IConsumer>() {
                  @Override
                  public void accept(final Map input) {
                      // do something with input
                  }
              });
          } catch (final JSONException e) {
          }
          // Warning: Any buffers we created for the Map above will be destroyed at this point, so they should
          // be either consumed in accept, or converted to strings (if they do not contain sensitive information.)

   :param parInput: The input character sequence to deserialize.
   :param parConsumer: The consumer to call with our unserialized JSON value.
   :param <T>: The type of object we expect. JSONDecodeException will be thrown if this is wrong. Note that Object (which will accept anything) is acceptable.
   :throws JSONException: On decode failure.

fromJSON
^^^^^^^^

.. java:method:: public <T> void fromJSON(byte[] parInput, IConsumer<T> parConsumer, Class<T> parClass) throws JSONException
   :outertype: SecureJSON

   Convert a JSON byte array to an object that consumer will accept. Throws JSONException on failure. After the consumer returns, all buffers we created while parsing the JSON character sequence will be destroyed. Of special note here, even though we can erase the byte[] array, we will not. That is up to the caller to do so.

   Example:

   ::

          import com.chelseaurquhart.securejson.SecureJSON;
          final SecureJSON secureJSON = new SecureJSON();

          // This class will be read into.
          class MyCustomClass {
              private int myNumber;
              private CharSequence myString;
          }
          try {
              secureJSON.fromJSON("{}", new IConsumer() {
                  @Override
                  public void accept(final MyCustomClass input) {
                      // do something with input
                  }
              }, MyCustomClass.class);
          } catch (final JSONException e) {
          }
          // Warning: Any buffers we created for the MyCustomClass instance above will be destroyed at this point,
          // so they should be either consumed in accept, or converted to strings (if they do not contain sensitive
          // information.)

   :param parInput: The input character sequence to deserialize.
   :param parConsumer: The consumer to call with our unserialized JSON value.
   :param parClass: The class we will be building.
   :param <T>: The type of object we expect. JSONDecodeException will be thrown if this is wrong. Note that Object (which will accept anything) is acceptable.
   :throws JSONException: On decode failure.

fromJSON
^^^^^^^^

.. java:method:: public <T> void fromJSON(InputStream parInput, IConsumer<T> parConsumer) throws JSONException
   :outertype: SecureJSON

   Read a JSON character sequence stream to an object that consumer will accept. Throws JSONException on failure. After the consumer returns, all buffers we created while parsing the JSON character sequence will be destroyed.

   Example:

   ::

          import com.chelseaurquhart.securejson.SecureJSON;
          final SecureJSON secureJSON = new SecureJSON();
          final InputStream inputStream = new ByteArrayInputStream("{}".getBytes());
          try {
              secureJSON.fromJSON(inputStream, new IConsumer>() {
                  @Override
                  public void accept(final Map input) {
                      // do something with input
                  }
              });
          } catch (final JSONException e) {
          }
          // Warning: Any buffers we created for the Map above will be destroyed at this point, so they should
          // be either consumed in accept, or converted to strings (if they do not contain sensitive information.)

   :param parInput: The input character stream to deserialize.
   :param parConsumer: The consumer to call with our unserialized JSON value.
   :param <T>: The type of object we expect. JSONDecodeException will be thrown if this is wrong. Note that Object (which will accept anything) is acceptable.
   :throws JSONException: On decode failure.

fromJSON
^^^^^^^^

.. java:method:: public <T> void fromJSON(InputStream parInput, IConsumer<T> parConsumer, Class<T> parClass) throws JSONException
   :outertype: SecureJSON

   Read a JSON character sequence stream to an object that consumer will accept. Throws JSONException on failure. After the consumer returns, all buffers we created while parsing the JSON character sequence will be destroyed.

   Example:

   ::

          import com.chelseaurquhart.securejson.SecureJSON;
          final SecureJSON secureJSON = new SecureJSON();
          final InputStream inputStream = new ByteArrayInputStream("{}".getBytes());

          // This class will be read into.
          class MyCustomClass {
              private int myNumber;
              private CharSequence myString;
          }
          try {
              secureJSON.fromJSON(inputStream, new IConsumer() {
                  @Override
                  public void accept(final MyCustomClass input) {
                      // do something with input
                  }
              }, MyCustomClass.class);
          } catch (final JSONException e) {
          }
          // Warning: Any buffers we created for the MyCustomClass instance above will be destroyed at this point,
          // so they should be either consumed in accept, or converted to strings (if they do not contain sensitive
          // information.)

   :param parInput: The input character stream to deserialize.
   :param parConsumer: The consumer to call with our unserialized JSON value.
   :param parClass: The class we will be building.
   :param <T>: The type of object we expect. JSONDecodeException will be thrown if this is wrong. Note that Object (which will accept anything) is acceptable.
   :throws JSONException: On decode failure.

toJSON
^^^^^^

.. java:method:: public void toJSON(Object parInput, IConsumer<CharSequence> parConsumer) throws JSONEncodeException
   :outertype: SecureJSON

   Convert an object to a JSON character sequence. If it cannot be converted, throws JSONEncodeException. After the consumer returns, the buffer will be destroyed so it MUST be fully consumed.

   Example:

   ::

          import com.chelseaurquhart.securejson.SecureJSON;
          import java.util.Arrays;
          final SecureJSON secureJSON = new SecureJSON();
          try {
              secureJSON.toJSON(Arrays.asList("1", 2, "three"), new IConsumer() {
                  @Override
                  public void accept(final CharSequence input) {
                      // do something with input
                  }
              });
          } catch (final JSONEncodeException e) {
          }
          // Warning: even if you copied input to a local variable above, it is destroyed before this
          // line and you will no longer be able to access it.

   :param parInput: The input object to toJSONAble to JSON.
   :param parConsumer: The consumer to provide the JSON character sequence to when completed.
   :throws JSONEncodeException: On encode failure.

toJSON
^^^^^^

.. java:method:: public void toJSON(Object parInput, OutputStream parOutputStream) throws JSONEncodeException
   :outertype: SecureJSON

   Convert an object to a JSON string, writing to the provided stream and specifying the character set.

   Example:

   ::

          import com.chelseaurquhart.securejson.SecureJSON;
          final SecureJSON secureJSON = new SecureJSON();
          try (final OutputStream outputStream = new FileOutputStream("file.json")) {
              secureJSON.toJSON(Arrays.asList("1", 2, "three"), outputStream);
          } catch (final JSONEncodeException e) {
          }

   :param parInput: The input object to toJSONAble to JSON.
   :param parOutputStream: The stream to write to.
   :throws JSONEncodeException: On encode failure.

toJSONBytes
^^^^^^^^^^^

.. java:method:: public void toJSONBytes(Object parInput, IConsumer<byte[]> parConsumer) throws JSONEncodeException
   :outertype: SecureJSON

   Convert an object to a JSON byte array. If it cannot be converted, throws JSONEncodeException. After the consumer returns, the buffer will be destroyed so it MUST be fully consumed.

   Example:

   ::

          import com.chelseaurquhart.securejson.SecureJSON;
          final SecureJSON secureJSON = new SecureJSON();
          try {
              secureJSON.toJSONBytes(Arrays.asList("1", 2, "three"), new IConsumer() {
                  @Override
                  public void accept(final byte[] input) {
                      // do something with input
                  }
              });
          } catch (final JSONEncodeException e) {
          }
          // Warning: even if you copied input to a local variable above, it is destroyed before this
          // line and you will no longer be able to access it.

   :param parInput: The input object to toJSONAble to JSON.
   :param parConsumer: The consumer to provide the JSON character sequence to when completed.
   :throws JSONEncodeException: On encode failure.

