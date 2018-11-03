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

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.lang.reflect.ReflectPermission;
import java.util.HashMap;
import java.util.LinkedList;

// We have to run single-threaded to prevent our security manager from buggering up.
@Test(singleThreaded = true)
public final class ObjectWriterTest {
    private ObjectWriterTest() {
    }

    @Test
    void testSimpleObject() {
        final ObjectWriter myObjectWriter = new ObjectWriter();
        Assert.assertEquals(myObjectWriter.accept(new Object() {
            @SuppressFBWarnings(value = "UrF")
            private CharSequence a = "b";
            @SuppressFBWarnings(value = "UrF")
            private transient CharSequence b = "c";
        }), new HashMap<CharSequence, Object>() {{
                put("a", "b");
            }});
    }


    public void testSimpleObjectSecurityViolation() {
        try {
            SJSecurityManager.SECURITY_VIOLATIONS.add(new ReflectPermission("suppressAccessChecks"));
            testSimpleObject();
            Assert.fail("Expected exception not thrown");
        } catch (final JSONException.JSONRuntimeException myException) {
            Assert.assertEquals(myException.getCause().getClass(), SecurityException.class);
        }
    }

    @Test
    void testJSONAwareList() {
        final ObjectWriter myObjectWriter = new ObjectWriter();
        Assert.assertEquals(myObjectWriter.accept(new IJSONSerializeAware() {
            @SuppressFBWarnings(value = "Se")

            @Override
            public Object toJSONable() {
                return new LinkedList<Object>() {{
                        add("a");
                        add("b");
                    }};
            }
        }), new LinkedList<Object>() {{
                add("a");
                add("b");
            }});
    }
}
