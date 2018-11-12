/*
 * Copyright 2018 Chelsea Urquhart
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.chelseaurquhart.securejson;

import com.chelseaurquhart.securejson.JSONException.JSONRuntimeException;

import com.chelseaurquhart.securejson.util.StringUtil;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.Map;

@SuppressWarnings("PMD.CommentRequired")
public final class SecureJSONTest {
    @Test(dataProviderClass = JSONReaderTest.class, dataProvider = JSONReaderTest.DATA_PROVIDER_NAME)
    public void testReadString(final JSONReaderTest.Parameters<Object> parParameters) {
        try {
            new SecureJSON().fromJSON(parParameters.getInputString(), new IConsumer<Object>() {
                @Override
                public void accept(final Object parInput) {
                    Assert.assertEquals(StringUtil.deepCharSequenceToString(parParameters.getExpected()),
                        StringUtil.deepCharSequenceToString(parInput));
                }
            }, parParameters.getExpectedClass());
            Assert.assertNull(parParameters.getExpectedException(), "Expected exception was not thrown");
        } catch (final JSONDecodeException myException) {
            checkException(parParameters, myException);
        } catch (final JSONRuntimeException myException) {
            checkException(parParameters, myException);
        }
    }

    @Test(dataProviderClass = JSONReaderTest.class, dataProvider = JSONReaderTest.DATA_PROVIDER_NAME)
    public void testReadStream(final JSONReaderTest.Parameters<Object> parParameters) {
        final InputStream myInputStream = JSONReaderTest.inputToStream(
            parParameters.getInputString(), parParameters.getInputBytes());

        try {
            new SecureJSON().fromJSON(myInputStream, new IConsumer<Object>() {
                @Override
                public void accept(final Object parInput) {
                    Assert.assertEquals(StringUtil.deepCharSequenceToString(parParameters.getExpected()),
                        StringUtil.deepCharSequenceToString(parInput));
                }
            }, parParameters.getExpectedClass());
            Assert.assertNull(parParameters.getExpectedException(), "Expected exception was not thrown");
        } catch (final JSONDecodeException myException) {
            checkException(parParameters, myException);
        } catch (final JSONRuntimeException myException) {
            checkException(parParameters, myException);
        }
    }

    @Test(dataProviderClass = JSONReaderTest.class, dataProvider = JSONReaderTest.DATA_PROVIDER_NAME)
    public void testReadBytes(final JSONReaderTest.Parameters<Object> parParameters) {
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
            }, parParameters.getExpectedClass());
            Assert.assertNull(parParameters.getExpectedException(), "Expected exception was not thrown");
        } catch (final JSONDecodeException myException) {
            checkException(parParameters, myException);
        } catch (final JSONRuntimeException myException) {
            checkException(parParameters, myException);
        }
    }

    private void checkException(final JSONReaderTest.Parameters<?> parParameters, final Exception parException) {
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
                    StringUtil.charSequenceToString(parParameters.getSecureJSONExpected()));
            }
        });
    }

    @Test(dataProviderClass = JSONWriterTest.class, dataProvider = JSONWriterTest.DATA_PROVIDER_NAME)
    public void testWriteStream(final JSONWriterTest.Parameters parParameters)
            throws JSONEncodeException, UnsupportedEncodingException {
        final ByteArrayOutputStream myOutputStream = new ByteArrayOutputStream();
        new SecureJSON().toJSON(parParameters.getInputObject(), myOutputStream);
        Assert.assertEquals(StringUtil.charSequenceToString(myOutputStream.toString(StandardCharsets.UTF_8.name())),
            StringUtil.charSequenceToString(parParameters.getSecureJSONExpected()));
    }

    @Test(dataProviderClass = JSONWriterTest.class, dataProvider = JSONWriterTest.DATA_PROVIDER_NAME)
    public void testWriteBytes(final JSONWriterTest.Parameters parParameters)
            throws JSONEncodeException {
        new SecureJSON().toJSONBytes(parParameters.getInputObject(), new IConsumer<byte[]>() {
            @Override
            public void accept(final byte[] parInput) {
                Assert.assertEquals(StandardCharsets.UTF_8.decode(ByteBuffer.wrap(parInput)).toString(),
                    StringUtil.charSequenceToString(parParameters.getSecureJSONExpected()));
            }
        });
    }

    @Test(expectedExceptions = JSONDecodeException.class)
    public void testReadIncorrectType() throws JSONException {
        final CharSequence myInput = "\"test\"";
        // read correct type to test for general functionality
        // use the builder just so there is something executing it
        new SecureJSON.Builder().build().fromJSON(myInput, new IConsumer<CharSequence>() {
            @Override
            public void accept(final CharSequence parInput) {
                Assert.assertEquals(StringUtil.charSequenceToString(parInput), "test");
            }
        });

        // Bad cast
        new SecureJSON().fromJSON(myInput, new IConsumer<Map<?, ?>>() {
            @Override
            public void accept(final Map<?, ?> parInput) {
                Assert.fail("invalid type");
            }
        });
    }
}
