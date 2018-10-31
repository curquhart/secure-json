.. java:import:: java.io ByteArrayInputStream

.. java:import:: java.io InputStream

.. java:import:: java.io OutputStream

.. java:import:: java.nio.charset StandardCharsets

SecureJSON.Builder
==================

.. java:package:: com.chelseaurquhart.securejson
   :noindex:

.. java:type:: public static final class Builder
   :outertype: SecureJSON

   Builder for SecureJSON. This allows specifying options for the encoder and decoder to use.

Methods
-------
build
^^^^^

.. java:method:: public SecureJSON build()
   :outertype: SecureJSON.Builder

   Build a SecureJSON instance using our settings.

   Example:

   ::

          import com.chelseaurquhart.securejson.SecureJSON;
          final SecureJSON secureJSON = new SecureJSON.Builder().build();

   The above example is functionally equivalent to:

   ::

          import com.chelseaurquhart.securejson.SecureJSON;
          final SecureJSON secureJSON = new SecureJSON();

   :return: A SecureJSON instance.

strictMapKeyTypes
^^^^^^^^^^^^^^^^^

.. java:method:: public Builder strictMapKeyTypes(boolean parStrictMapKeyTypes)
   :outertype: SecureJSON.Builder

   Set the strictMapKeyTypes option. If strictMapKeyTypes is true, we will never convert CharSequence to string when we are creating Maps (which require CharSequence-like keys). If false, we will try to convert the object to a string. This should be used with caution, and certainly not for sensitive data as strings may stay in memory much longer than desired.

   **Default**: true

   Example (disable strict map key types in order to allow Strings to be populated for Map keys):

   ::

          import com.chelseaurquhart.securejson.SecureJSON;
          final SecureJSON secureJSON = new SecureJSON.Builder()
              .strictMapKeyTypes(false)
              .build();

   :param parStrictMapKeyTypes: The value to use for our strict map keys setting.
   :return: A reference to this object.

strictStrings
^^^^^^^^^^^^^

.. java:method:: public Builder strictStrings(boolean parStrictStrings)
   :outertype: SecureJSON.Builder

   Set the strictStrings option. If strictStrings is true, we will never convert CharSequence to string. If it is false, we will convert if we can't otherwise cast. Default is true. This should be used with caution, and certainly not for sensitive data as strings may stay in memory much longer than desired.

   **Default**: true

   Example (disable strict strings in order to allow Strings to be populated for non-sensitive fields):

   ::

          import com.chelseaurquhart.securejson.SecureJSON;
          final SecureJSON secureJSON = new SecureJSON.Builder()
              .strictStrings(false)
              .build();

   :param parStrictStrings: The value to use for our strict strings setting.
   :return: A reference to this object.

writableCharBufferFactory
^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: public Builder writableCharBufferFactory(IFunction<Integer, IWritableCharSequence> parWritableCharBufferFactory)
   :outertype: SecureJSON.Builder

   Set the factory to use for building secure buffers. By default we will use our own implementation, but this can be used to provide a custom one.

   **Default**: <internally managed factory>

   Example (use a custom IWritableCharSequence factory):

   .. DANGER:: This sample implementation is by no means secure! The default, however, is.

   ::

          import com.chelseaurquhart.securejson.SecureJSON;
          final SecureJSON secureJSON = new SecureJSON.Builder()
              .writableCharBufferFactory(new IFunction() {
                  @Override
                  public IWritableCharSequence accept(final Integer parCapacity) {
                      return new IWritableCharSequence() {
                          private final StringBuilder builder = new StringBuilder(parCapacity);

                          @Override
                          public void append(char parChar) {
                              builder.append(parCapacity);
                          }

                          @Override
                          public boolean isRestrictedToCapacity() {
                              return false;
                          }

                          @Override
                          public int getCapacity() {
                              return builder.capacity();
                          }

                          @Override
                          public void close() {
                              builder.setLength(0);
                          }

                          @Override
                          public int length() {
                              return builder.length();
                          }

                          @Override
                          public char charAt(final int parIndex) {
                              return builder.charAt(parIndex);
                          }

                          @Override
                          public CharSequence subSequence(final int parStart, final int parEnd) {
                              return builder.subSequence(parStart, parEnd);
                          }
                      };
                  }
              })
              .build();

   :param parWritableCharBufferFactory: The factory to use for building secure buffers.
   :return: A reference to this object.

