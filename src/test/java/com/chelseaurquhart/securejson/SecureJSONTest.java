package com.chelseaurquhart.securejson;

import com.chelseaurquhart.securejson.util.StringUtil;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public final class SecureJSONTest {
    @Test(dataProviderClass = JSONReaderTest.class, dataProvider = JSONReaderTest.DATA_PROVIDER_NAME)
    public void testReadString(final JSONReaderTest.Parameters parParameters) {
        try {
            new SecureJSON().fromJSON(parParameters.getInputString(), new IConsumer<Object>() {
                @Override
                public void accept(final Object parInput) {
                    Assert.assertEquals(StringUtil.deepCharSequenceToString(parParameters.getExpected()),
                        StringUtil.deepCharSequenceToString(parInput));
                }
            });
            Assert.assertNull(parParameters.getExpectedException(), "Expected exception was not thrown");
        } catch (final Exception myException) {
            checkException(parParameters, myException);
        }
    }

    @Test(dataProviderClass = JSONReaderTest.class, dataProvider = JSONReaderTest.DATA_PROVIDER_NAME)
    public void testReadStream(final JSONReaderTest.Parameters parParameters) {
        final InputStream myInputStream = JSONReaderTest.inputToStream(
            parParameters.getInputString(), parParameters.getInputBytes());

        try {
            new SecureJSON().fromJSON(myInputStream, new IConsumer<Object>() {
                @Override
                public void accept(final Object parInput) {
                    Assert.assertEquals(StringUtil.deepCharSequenceToString(parParameters.getExpected()),
                        StringUtil.deepCharSequenceToString(parInput));
                }
            });
            Assert.assertNull(parParameters.getExpectedException(), "Expected exception was not thrown");
        } catch (final Exception myException) {
            checkException(parParameters, myException);
        }
    }

    @Test(dataProviderClass = JSONReaderTest.class, dataProvider = JSONReaderTest.DATA_PROVIDER_NAME)
    public void testReadBytes(final JSONReaderTest.Parameters parParameters) {
        try {
            final byte[] myBytes;
            if (parParameters.getInputBytes() == null) {
                myBytes = StringUtil.charSequenceToString(parParameters.getInputString()).getBytes(
                    StandardCharsets.UTF_8);
            } else {
                myBytes = parParameters.getInputBytes();
            }
            new SecureJSON().fromJSON(myBytes, new IConsumer<Object>() {
                @Override
                public void accept(final Object parInput) {
                    Assert.assertEquals(StringUtil.deepCharSequenceToString(parParameters.getExpected()),
                        StringUtil.deepCharSequenceToString(parInput));
                }
            });
            Assert.assertNull(parParameters.getExpectedException(), "Expected exception was not thrown");
        } catch (final Exception myException) {
            checkException(parParameters, myException);
        }
    }

    private void checkException(final JSONReaderTest.Parameters parParameters, final Exception parException) {
        Assert.assertNotNull(parParameters.getExpectedException());
        Assert.assertEquals(Util.unwrapException(parException).getMessage(),
            parParameters.getExpectedException().getMessage());
        Assert.assertEquals(Util.unwrapException(parException).getClass(),
            parParameters.getExpectedException().getClass());
    }

    @Test(dataProviderClass = JSONWriterTest.class, dataProvider = JSONWriterTest.DATA_PROVIDER_NAME)
    public void testWriteString(final JSONWriterTest.Parameters parParameters) throws JSONEncodeException {
        new SecureJSON().toJSON(parParameters.getInputObject(), new IConsumer<CharSequence>() {
            @Override
            public void accept(final CharSequence parInput) {
                Assert.assertEquals(StringUtil.charSequenceToString(parInput),
                    StringUtil.charSequenceToString(parParameters.getExpected()));
            }
        });
    }

    @Test(dataProviderClass = JSONWriterTest.class, dataProvider = JSONWriterTest.DATA_PROVIDER_NAME)
    public void testWriteStream(final JSONWriterTest.Parameters parParameters)
            throws JSONEncodeException, UnsupportedEncodingException {
        final ByteArrayOutputStream myOutputStream = new ByteArrayOutputStream();
        new SecureJSON().toJSON(parParameters.getInputObject(), myOutputStream);
        Assert.assertEquals(StringUtil.charSequenceToString(myOutputStream.toString(StandardCharsets.UTF_8.name())),
            StringUtil.charSequenceToString(parParameters.getExpected()));
    }

    @Test(dataProviderClass = JSONWriterTest.class, dataProvider = JSONWriterTest.DATA_PROVIDER_NAME)
    public void testWriteBytes(final JSONWriterTest.Parameters parParameters)
            throws JSONEncodeException {
        new SecureJSON().toJSONBytes(parParameters.getInputObject(), new IConsumer<byte[]>() {
            @Override
            public void accept(final byte[] parInput) {
                Assert.assertEquals(StandardCharsets.UTF_8.decode(ByteBuffer.wrap(parInput)).toString(),
                    StringUtil.charSequenceToString(parParameters.getExpected()));
            }
        });
    }

    @Test(expectedExceptions = JSONDecodeException.class)
    public void testReadIncorrectType() throws JSONException {
        final CharSequence myInput = "\"test\"";
        // read correct type to test for general functionality
        new SecureJSON().fromJSON(myInput, new IConsumer<CharSequence>() {
            @Override
            public void accept(final CharSequence parInput) {
                Assert.assertEquals(StringUtil.charSequenceToString(parInput), "test");
            }
        });

        // Bad cast
        new SecureJSON().fromJSON(myInput, new IConsumer<Map>() {
            @Override
            public void accept(final Map parInput) {
                Assert.fail("invalid type");
            }
        });
    }
}
