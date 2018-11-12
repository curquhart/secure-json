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

import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

@SuppressWarnings("PMD.CommentRequired")
public final class IterableInputStreamTest {
    private IterableInputStreamTest() {
    }

    @Test(expectedExceptions = UnsupportedOperationException.class)
    public void testRemove() {
        new IterableInputStream(new ByteArrayInputStream(new byte[0])).remove();
    }

    @Test
    public void testReadNextChar() throws IOException {
        InputStream myBuffer = null;
        try {
            myBuffer = new ByteArrayInputStream(new byte[]{'a', 'b'});
            final IterableInputStream myStream = new IterableInputStream(myBuffer);
            Assert.assertEquals(myStream.readNextChar(), (Character) 'a');
            Assert.assertEquals(myStream.readNextChar(), (Character) 'b');
            Assert.assertNull(myStream.readNextChar());
            Assert.assertNull(myStream.readNextChar());
        } finally {
            if (myBuffer != null) {
                myBuffer.close();
            }
        }
    }
}
