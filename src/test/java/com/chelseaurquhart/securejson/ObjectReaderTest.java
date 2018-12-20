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

import com.chelseaurquhart.securejson.util.StringUtil;
import com.chelseaurquhart.securejson.JSONException.JSONRuntimeException;

import org.testng.Assert;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.io.IOException;
import java.lang.reflect.ReflectPermission;
import java.util.AbstractCollection;
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

// We have to run single-threaded to prevent our security manager from buggering up.
@SuppressWarnings("PMD.CommentRequired")
@Test(singleThreaded = true)
public final class ObjectReaderTest {
    private static final Settings DEFAULT_SETTINGS = new Settings(
        new SecureJSON.Builder()
            .registerClassInitializer(NestingAbsClass.Level2.class, new IFunction<Object, NestingAbsClass.Level2>() {
                @Override
                public NestingAbsClass.Level2 accept(final Object parInput) {
                    return new NestingAbsClass.Level2("test");
                }
            }));
    private static final Settings UNSTRICT_SETTINGS = new Settings(new SecureJSON.Builder().strictStrings(false));

    @BeforeTest
    @SuppressWarnings("PMD.JUnit4TestShouldUseBeforeAnnotation")
    static void setUp() {
        SJSecurityManager.setUp();
    }

    @AfterTest
    @SuppressWarnings("PMD.JUnit4TestShouldUseAfterAnnotation")
    static void tearDown() {
        SJSecurityManager.tearDown();
    }

    private ObjectReaderTest() {
    }

    @Test
    public void testSimpleDeserialization() throws IOException, JSONException {
        final SimpleDeserializationClass mySimpleDeserializationClass = new ObjectReader<SimpleDeserializationClass>(
            SimpleDeserializationClass.class, UNSTRICT_SETTINGS).accept(new HashMap<CharSequence, Object>() {
                private static final long serialVersionUID = 1L;

                {
                    put("integerVal", 1);
                    put("shortVal", 2);
                    final ManagedSecureCharBuffer myStringBuffer = new ManagedSecureCharBuffer(DEFAULT_SETTINGS);
                    myStringBuffer.append("testingString");
                    put("stringVal", myStringBuffer);
                    final ManagedSecureCharBuffer myCSeqBuffer = new ManagedSecureCharBuffer(DEFAULT_SETTINGS);
                    myCSeqBuffer.append("testingCharSeq");
                    put("charSeqVal", myCSeqBuffer);
                    put("transientIntVal", 123);
                    put("ints", new int[]{1, 2, 3});
                    put("intList", Arrays.asList(4, 5, 6));
                    put("root1", true);
                }
            });

        Assert.assertEquals(mySimpleDeserializationClass.presetVal, 5);
        Assert.assertEquals(mySimpleDeserializationClass.integerVal, 1);
        Assert.assertEquals(mySimpleDeserializationClass.shortVal, 2);
        Assert.assertEquals(mySimpleDeserializationClass.transientIntVal, 0);
        Assert.assertEquals(mySimpleDeserializationClass.stringVal, "testingString");
        Assert.assertEquals(mySimpleDeserializationClass.charSeqVal.getClass(), ManagedSecureCharBuffer.class);
        Assert.assertEquals(StringUtil.charSequenceToString(mySimpleDeserializationClass.charSeqVal),
            "testingCharSeq");
        Assert.assertEquals(mySimpleDeserializationClass.ints, new int[]{1, 2, 3});
        Assert.assertEquals(mySimpleDeserializationClass.intList, Arrays.asList(4, 5, 6));
        Assert.assertTrue(mySimpleDeserializationClass.absPosition);
    }

    @Test
    public void testSimpleDeserializationSecurityViolation() throws IOException {
        try {
            SJSecurityManager.SECURITY_VIOLATIONS.add(new ReflectPermission("suppressAccessChecks"));
            testSimpleDeserialization();
            Assert.fail("Expected exception not thrown");
        } catch (final JSONException myException) {
            Assert.assertEquals(myException.getCause().getClass(), SecurityException.class);
        } catch (final JSONRuntimeException myException) {
            Assert.assertEquals(myException.getCause().getClass(), SecurityException.class);
        }
    }

    @Test
    public void testSimpleNesting() throws IOException, JSONException {
        final SimpleNestingClass mySimpleNestingClass = new ObjectReader<SimpleNestingClass>(
            SimpleNestingClass.class, UNSTRICT_SETTINGS).accept(new HashMap<CharSequence, Object>() {
                private static final long serialVersionUID = 1L;

                {
                    put("inner1", new HashMap<CharSequence, Object>() {
                        private static final long serialVersionUID = 1L;

                        {
                            put("integerVal", 11);
                            put("shortVal", 21);
                            final ManagedSecureCharBuffer myStringBuffer = new ManagedSecureCharBuffer(
                                DEFAULT_SETTINGS);
                            myStringBuffer.append("testingString1");
                            put("stringVal", myStringBuffer);
                            final ManagedSecureCharBuffer myCSeqBuffer = new ManagedSecureCharBuffer(DEFAULT_SETTINGS);
                            myCSeqBuffer.append("testingCharSeq1");
                            put("charSeqVal", myCSeqBuffer);
                            put("transientIntVal", 1234);
                        }
                    });
                    put("inner2", new HashMap<CharSequence, Object>() {
                        private static final long serialVersionUID = 1L;

                        {
                            put("integerVal", 111);
                            put("shortVal", 211);
                            final ManagedSecureCharBuffer myStringBuffer = new ManagedSecureCharBuffer(
                                DEFAULT_SETTINGS);
                            myStringBuffer.append("testingString2");
                            put("stringVal", myStringBuffer);
                            final ManagedSecureCharBuffer myCSeqBuffer =
                                new ManagedSecureCharBuffer(DEFAULT_SETTINGS);
                            myCSeqBuffer.append("testingCharSeq2");
                            put("charSeqVal", myCSeqBuffer);
                            put("transientIntVal", 12345);
                        }
                    });
                }
            });

        Assert.assertNull(mySimpleNestingClass.inner1);
        Assert.assertEquals(mySimpleNestingClass.inner2.presetVal, 5);
        Assert.assertEquals(mySimpleNestingClass.inner2.integerVal, 111);
        Assert.assertEquals(mySimpleNestingClass.inner2.shortVal, 211);
        Assert.assertEquals(mySimpleNestingClass.inner2.transientIntVal, 0);
        Assert.assertEquals(mySimpleNestingClass.inner2.stringVal, "testingString2");
        Assert.assertEquals(mySimpleNestingClass.inner2.charSeqVal.getClass(), ManagedSecureCharBuffer.class);
        Assert.assertEquals(StringUtil.charSequenceToString(mySimpleNestingClass.inner2.charSeqVal),
            "testingCharSeq2");
    }

    @Test
    public void testSubNesting() throws IOException, JSONException {
        final SubNestingClass mySubNestingClass = new ObjectReader<SubNestingClass>(
            SubNestingClass.class, UNSTRICT_SETTINGS).accept(new HashMap<CharSequence, Object>() {
                private static final long serialVersionUID = 1L;

                {
                    put("inner1", new HashMap<CharSequence, Object>() {
                        private static final long serialVersionUID = 1L;

                        {
                            put("data1", new HashMap<CharSequence, Object>() {
                                private static final long serialVersionUID = 1L;

                                {
                                    put("integerVal", 111);
                                    put("shortVal", 211);
                                    final ManagedSecureCharBuffer myStringBuffer = new ManagedSecureCharBuffer(
                                        DEFAULT_SETTINGS);
                                    myStringBuffer.append("testingString2");
                                    put("stringVal", myStringBuffer);
                                    final ManagedSecureCharBuffer myCSeqBuffer = new ManagedSecureCharBuffer(
                                        DEFAULT_SETTINGS);
                                    myCSeqBuffer.append("testingCharSeq2");
                                    put("charSeqVal", myCSeqBuffer);
                                    put("transientIntVal", 1234);
                                }
                            });
                        }
                    });
                }
            });

        Assert.assertNull(mySubNestingClass.data1);
        Assert.assertEquals(mySubNestingClass.inner1.data1.presetVal, 5);
        Assert.assertEquals(mySubNestingClass.inner1.data1.integerVal, 111);
        Assert.assertEquals(mySubNestingClass.inner1.data1.shortVal, 211);
        Assert.assertEquals(mySubNestingClass.inner1.data1.transientIntVal, 0);
        Assert.assertEquals(mySubNestingClass.inner1.data1.stringVal, "testingString2");
        Assert.assertEquals(mySubNestingClass.inner1.data1.charSeqVal.getClass(), ManagedSecureCharBuffer.class);
        Assert.assertEquals(StringUtil.charSequenceToString(mySubNestingClass.inner1.data1.charSeqVal),
            "testingCharSeq2");
    }

    @Test
    public void testRecursiveNesting() throws IOException, JSONException {
        final SubNestingClass mySubNestingClass = new ObjectReader<SubNestingClass>(
            SubNestingClass.class, UNSTRICT_SETTINGS)
            .accept(new HashMap<CharSequence, Object>() {
                private static final long serialVersionUID = 1L;

                {
                    put("inner1", new HashMap<CharSequence, Object>() {
                        private static final long serialVersionUID = 1L;

                        {
                            put("inner1", new HashMap<CharSequence, Object>() {
                                private static final long serialVersionUID = 1L;

                                {
                                    put("data1", new HashMap<CharSequence, Object>() {
                                        private static final long serialVersionUID = 1L;

                                        {
                                            put("integerVal", 1111);
                                            put("shortVal", 2111);
                                            final ManagedSecureCharBuffer myStringBuffer =
                                                new ManagedSecureCharBuffer(DEFAULT_SETTINGS);
                                            myStringBuffer.append("testingString3");
                                            put("stringVal", myStringBuffer);
                                            final ManagedSecureCharBuffer myCSeqBuffer =
                                                new ManagedSecureCharBuffer(DEFAULT_SETTINGS);
                                            myCSeqBuffer.append("testingCharSeq3");
                                            put("charSeqVal", myCSeqBuffer);
                                            put("transientIntVal", 12345);
                                        }
                                    });

                                    put("inner1", new HashMap<CharSequence, Object>() {
                                        private static final long serialVersionUID = 1L;

                                        {
                                            put("data1", new HashMap<CharSequence, Object>() {
                                                private static final long serialVersionUID = 1L;

                                                {
                                                    put("integerVal", 111);
                                                    put("shortVal", 211);
                                                    final ManagedSecureCharBuffer myStringBuffer =
                                                        new ManagedSecureCharBuffer(DEFAULT_SETTINGS);
                                                    myStringBuffer.append("testingString2");
                                                    put("stringVal", myStringBuffer);
                                                    final ManagedSecureCharBuffer myCSeqBuffer =
                                                        new ManagedSecureCharBuffer(DEFAULT_SETTINGS);
                                                    myCSeqBuffer.append("testingCharSeq2");
                                                    put("charSeqVal", myCSeqBuffer);
                                                    put("transientIntVal", 1234);
                                                }
                                            });
                                        }
                                    });
                                }
                            });
                        }
                    });
                }
            });

        Assert.assertNull(mySubNestingClass.data1);
        Assert.assertNull(mySubNestingClass.inner1.data1);
        Assert.assertEquals(mySubNestingClass.inner1.inner1.data1.presetVal, 5);
        Assert.assertEquals(mySubNestingClass.inner1.inner1.data1.integerVal, 1111);
        Assert.assertEquals(mySubNestingClass.inner1.inner1.data1.shortVal, 2111);
        Assert.assertEquals(mySubNestingClass.inner1.inner1.data1.transientIntVal, 0);
        Assert.assertEquals(mySubNestingClass.inner1.inner1.data1.stringVal, "testingString3");
        Assert.assertEquals(mySubNestingClass.inner1.inner1.data1.charSeqVal.getClass(),
            ManagedSecureCharBuffer.class);
        Assert.assertEquals(StringUtil.charSequenceToString(mySubNestingClass.inner1.inner1.data1.charSeqVal),
            "testingCharSeq3");

        Assert.assertEquals(mySubNestingClass.inner1.inner1.inner1.data1.presetVal, 5);
        Assert.assertEquals(mySubNestingClass.inner1.inner1.inner1.data1.integerVal, 111);
        Assert.assertEquals(mySubNestingClass.inner1.inner1.inner1.data1.shortVal, 211);
        Assert.assertEquals(mySubNestingClass.inner1.inner1.inner1.data1.transientIntVal, 0);
        Assert.assertEquals(mySubNestingClass.inner1.inner1.inner1.data1.stringVal, "testingString2");
        Assert.assertEquals(mySubNestingClass.inner1.inner1.inner1.data1.charSeqVal.getClass(),
            ManagedSecureCharBuffer.class);
        Assert.assertEquals(StringUtil.charSequenceToString(mySubNestingClass.inner1.inner1.inner1.data1.charSeqVal),
            "testingCharSeq2");
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testComplexType() throws IOException, JSONException {
        final ComplexTypeClass myComplexTypeClass = new ObjectReader<ComplexTypeClass>(
            ComplexTypeClass.class, DEFAULT_SETTINGS).accept(new HashMap<CharSequence, Object>() {
                private static final long serialVersionUID = 1L;

                {
                    final List<Map<CharSequence, Map<String, Integer>>> myList1 =
                            new ArrayList<Map<CharSequence, Map<String, Integer>>>();
                    myList1.add(new HashMap<CharSequence, Map<String, Integer>>() {
                        private static final long serialVersionUID = 1L;

                        {
                            put("1", new HashMap<String, Integer>() {
                                private static final long serialVersionUID = 1L;

                                {
                                    put("2", 3);
                                }
                            });
                        }
                    });
                    final List<Map<CharSequence, Map<String, Integer>>> myList2 =
                            new ArrayList<Map<CharSequence, Map<String, Integer>>>();
                    myList2.add(new HashMap<CharSequence, Map<String, Integer>>() {
                        private static final long serialVersionUID = 1L;

                        {
                            put("4", new HashMap<String, Integer>() {
                                private static final long serialVersionUID = 1L;

                                {
                                    put("5", 6);
                                }
                            });
                        }
                    });

                    put("data", new Object[]{myList1, myList2});
                }
            });

        Assert.assertEquals(myComplexTypeClass.data[0], new ArrayList<HashMap<CharSequence, Map<String, Integer>>>() {
            private static final long serialVersionUID = 1L;

            {
                add(new HashMap<CharSequence, Map<String, Integer>>() {
                    private static final long serialVersionUID = 1L;

                    {
                        put("1", new HashMap<String, Integer>() {
                            private static final long serialVersionUID = 1L;

                            {
                                put("2", 3);
                            }
                        });
                    }
                });
            }
        });
        Assert.assertEquals(myComplexTypeClass.data[1], new ArrayList<HashMap<CharSequence, Map<String, Integer>>>() {
            private static final long serialVersionUID = 1L;

            {
                add(new HashMap<CharSequence, Map<String, Integer>>() {
                    private static final long serialVersionUID = 1L;

                    {
                        put("4", new HashMap<String, Integer>() {
                            private static final long serialVersionUID = 1L;

                            {
                                put("5", 6);
                            }
                        });
                    }
                });
            }
        });
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testAbsoluteNesting() throws IOException, JSONException {
        final NestingAbsClass myNestingAbsClass = new ObjectReader<NestingAbsClass>(
                NestingAbsClass.class, DEFAULT_SETTINGS).accept(new HashMap<CharSequence, Object>() {
                    private static final long serialVersionUID = 1L;

                    {
                        put("1", 11);
                        put("2", 21);
                        put("3", 31);
                        put("level2", new HashMap<CharSequence, Object>() {
                            private static final long serialVersionUID = 1L;

                            {
                                put("rel", 41);
                                put("l3", new HashMap<CharSequence, Object>() {
                                    private static final long serialVersionUID = 1L;

                                    {
                                        put("rel", 51);
                                        put("level4", new HashMap<CharSequence, Object>() {
                                            private static final long serialVersionUID = 1L;

                                            {
                                                put("level4", 61);
                                            }
                                        });
                                    }
                                });
                            }
                        });
                    }
                });

        Assert.assertEquals(myNestingAbsClass.level1, 11);
        Assert.assertEquals(myNestingAbsClass.level2.level2, 21);
        Assert.assertEquals(myNestingAbsClass.level2.arg, "test");
        Assert.assertEquals(myNestingAbsClass.level2.rel, 41);
        Assert.assertEquals(myNestingAbsClass.level2.level3.level3, 31);
        Assert.assertEquals(myNestingAbsClass.level2.level3.rel, 51);
        Assert.assertEquals(myNestingAbsClass.level2.level3.level4.level4, 61);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testConcreteConstruction() throws IOException, JSONException {
        final NestingAbsClass.ConcreteTest myConcreteTest = new ObjectReader<NestingAbsClass.ConcreteTest>(
                NestingAbsClass.ConcreteTest.class, DEFAULT_SETTINGS).accept(new HashMap<CharSequence, Object>() {
                    private static final long serialVersionUID = 1L;

                    {
                        put("genericMap", new TreeMap<CharSequence, Object>() {
                            private static final long serialVersionUID = 1L;

                            {
                                put("a", "b");
                            }
                        });
                        put("typedMap", new TreeMap<CharSequence, Object>() {
                            private static final long serialVersionUID = 1L;

                            {
                                put("c", "d");
                            }
                        });
                        put("genericSet", new HashSet<Object>() {
                            private static final long serialVersionUID = 1L;

                            {
                                add("e");
                            }
                        });
                        put("typedSet", new HashSet<Object>() {
                            private static final long serialVersionUID = 1L;

                            {
                                add("f");
                            }
                        });
                        put("genericAbstractSet", new HashSet<Object>() {
                            private static final long serialVersionUID = 1L;

                            {
                                add("g");
                            }
                        });
                        put("typedAbstractSet", new HashSet<Object>() {
                            private static final long serialVersionUID = 1L;

                            {
                                add("h");
                            }
                        });
                        put("genericCollection", new ArrayList<Object>() {
                            private static final long serialVersionUID = 1L;

                            {
                                add("i");
                            }
                        });
                        put("typedCollection", new LinkedList<Object>() {
                            private static final long serialVersionUID = 1L;

                            {
                                add("j");
                            }
                        });
                        put("genericAbstractCollection", new HashSet<Object>() {
                            private static final long serialVersionUID = 1L;

                            {
                                add("k");
                            }
                        });
                        put("genericList", new ArrayList<Object>() {
                            private static final long serialVersionUID = 1L;

                            {
                                add("l");
                            }
                        });
                        put("testEnum", "ONE");
                    }
                });

        Assert.assertEquals(myConcreteTest.genericMap, new LinkedHashMap<Object, Object>() {
            private static final long serialVersionUID = 1L;

            {
                put("a", "b");
            }
        });
        Assert.assertEquals(myConcreteTest.typedMap, new LinkedHashMap<String, Object>() {
            private static final long serialVersionUID = 1L;

            {
                put("c", "d");
            }
        });
        Assert.assertEquals(myConcreteTest.genericSet, new HashSet<Object>() {
            private static final long serialVersionUID = 1L;

            {
                add("e");
            }
        });
        Assert.assertEquals(myConcreteTest.typedSet, new HashSet<Object>() {
            private static final long serialVersionUID = 1L;

            {
                add("f");
            }
        });
        Assert.assertEquals(myConcreteTest.genericAbstractSet, new HashSet<Object>() {
            private static final long serialVersionUID = 1L;

            {
                add("g");
            }
        });
        Assert.assertEquals(myConcreteTest.typedAbstractSet, new HashSet<Object>() {
            private static final long serialVersionUID = 1L;

            {
                add("h");
            }
        });
        Assert.assertEquals(myConcreteTest.genericCollection, new HashSet<Object>() {
            private static final long serialVersionUID = 1L;

            {
                add("i");
            }
        });
        Assert.assertEquals(myConcreteTest.typedCollection, new HashSet<Object>() {
            private static final long serialVersionUID = 1L;

            {
                add("j");
            }
        });
        Assert.assertEquals(myConcreteTest.genericAbstractCollection, new HashSet<Object>() {
            private static final long serialVersionUID = 1L;

            {
                add("k");
            }
        });
        Assert.assertEquals(myConcreteTest.genericList, new HashSet<Object>() {
            private static final long serialVersionUID = 1L;

            {
                add("l");
            }
        });
        Assert.assertEquals(myConcreteTest.testEnum, ObjectWriterTest.TestEnum.ONE);
    }

    @SuppressWarnings("unchecked")
    @Test(expectedExceptions = JSONException.JSONRuntimeException.class)
    public void testUnknownType() throws IOException, JSONException {
        new ObjectReader<NestingAbsClass.InvalidClassTest>(
            NestingAbsClass.InvalidClassTest.class, DEFAULT_SETTINGS).accept(new HashMap<CharSequence, Object>() {
                private static final long serialVersionUID = 1L;

                {
                    put("calendar", new ArrayList<Object>() {
                        private static final long serialVersionUID = 1L;

                        {
                            add(Calendar.getInstance());
                        }
                    });
                }
            });
    }

    public static final class IdentityHashSetTest {
        @Test(expectedExceptions = NotImplementedException.class)
        public void testAddValue() {
            new ObjectReader.IdentityHashSet<Object>().size();
        }

        @Test(expectedExceptions = NotImplementedException.class)
        public void testIterator() {
            new ObjectReader.IdentityHashSet<Object>().iterator();
        }

        @Test
        public void testContains() {
            final ObjectReader.IdentityHashSet<String> myHashSet = new ObjectReader.IdentityHashSet<String>();
            myHashSet.add("test");
            Assert.assertTrue(myHashSet.contains("test"));
            Assert.assertFalse(myHashSet.contains("testing"));
            Assert.assertFalse(myHashSet.contains(new String("test".getBytes(StandardCharsets.UTF_8),
                StandardCharsets.UTF_8)));
        }
    }

    private static final class SimpleDeserializationClass {
        private SimpleDeserializationClass() {
        }

        private int presetVal = 5;
        private int integerVal;
        private transient int transientIntVal;
        @SuppressWarnings("PMD.AvoidUsingShortType")
        private short shortVal;
        private String stringVal;
        private CharSequence charSeqVal;
        private int[] ints;
        private List<Integer> intList;
        @Serialize(name = "root1", relativeTo = Relativity.ABSOLUTE)
        private boolean absPosition;
    }

    private static final class SimpleNestingClass {
        private SimpleNestingClass() {
        }

        private transient SimpleDeserializationClass inner1;
        private SimpleDeserializationClass inner2;
    }

    private static final class SubNestingClass {
        private SubNestingClass() {
        }

        private SubNestingClass inner1;
        private SimpleDeserializationClass data1;
    }

    private static final class ComplexTypeClass {
        private ComplexTypeClass() {
        }

        private List<HashMap<CharSequence, Map<String, Integer>>>[] data;
    }

    private static final class NestingAbsClass {
        private NestingAbsClass() {
        }

        @Serialize(name = "1", relativeTo = Relativity.ABSOLUTE)
        private int level1;
        private Level2 level2;

        private static final class Level2 {
            final transient String arg;
            private Level2(final String parArg) {
                arg = parArg;
            }

            @Serialize(name = "2", relativeTo = Relativity.ABSOLUTE)
            private int level2;
            @Serialize(name = {"l3"})
            private Level3 level3;
            private int rel;

        }

        private static final class Level3 {
            @Serialize(name = "3", relativeTo = Relativity.ABSOLUTE)
            private int level3;
            private int rel;
            private Level4 level4;
        }

        private static final class Level4 {
            private int level4;
        }

        private static final class ConcreteTest {
            private Map<?, ?> genericMap;
            private Map<String, Object> typedMap;
            private Set<?> genericSet;
            private Set<CharSequence> typedSet;
            private AbstractSet<?> genericAbstractSet;
            private AbstractSet<CharSequence> typedAbstractSet;
            private Collection<?> genericCollection;
            private Collection<CharSequence> typedCollection;
            private AbstractCollection<?> genericAbstractCollection;
            private List<?> genericList;
            private ObjectWriterTest.TestEnum testEnum;
        }

        private static final class InvalidClassTest {
            @SuppressWarnings("PMD.UnusedPrivateField")
            private Calendar calendar;
        }
    }
}
