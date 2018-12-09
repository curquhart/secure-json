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

import java.lang.reflect.ReflectPermission;
import java.util.HashMap;
import java.util.LinkedList;

// We have to run single-threaded to prevent our security manager from buggering up.
@SuppressWarnings("PMD.CommentRequired")
@Test(singleThreaded = true)
public final class ObjectWriterTest {
    enum TestEnum {
        ONE,
        TWO,
        THREE,
        FOUR
    }

    private ObjectWriterTest() {
    }

    @Test
    void testSimpleObject() {
        final ObjectWriter myObjectWriter = new ObjectWriter();
        final Object myOutput = myObjectWriter.accept(new Object() {
            private CharSequence a = "b";
            private transient CharSequence b = "c";
            private TestEnum testEnum = TestEnum.ONE;
            private TestEnum testEnum2 = TestEnum.TWO;
            private Inner inner = new Inner();
        });
        Assert.assertEquals(myOutput, new HashMap<CharSequence, Object>() {
            private static final long serialVersionUID = 1L;

            {
                put("a", "b");
                put("testEnum", "ONE");
                put("testEnum2", "TWO");
                put("inner", new HashMap<CharSequence, Object>() {{
                    put("testEnum", "THREE");
                    put("inner2", new HashMap<CharSequence, Object>() {{
                        put("test", new HashMap<CharSequence, Object>() {{
                            put("enum", "FOUR");
                        }});
                    }});
                }});
            }
        });
    }

    @Test
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
            @Override
            public Object toJSONable() {
                return new LinkedList<Object>() {
                    private static final long serialVersionUID = 1L;

                    {
                        add("a");
                        add("b");
                    }
                };
            }
        }), new LinkedList<Object>() {
            private static final long serialVersionUID = 1L;

            {
                add("a");
                add("b");
            }
        });
    }

    @SuppressWarnings("PMD.UnusedPrivateField")
    private static class Inner {
        private TestEnum testEnum = TestEnum.THREE;
        private Inner2 inner2 = new Inner2();
    }

    @SuppressWarnings("PMD.UnusedPrivateField")
    private static class Inner2 {
        @Serialize(name={"test", "enum"})
        private TestEnum testEnum = TestEnum.FOUR;
    }
}
