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
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.testng.Assert;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.io.IOException;
import java.lang.reflect.ReflectPermission;
import java.nio.charset.StandardCharsets;
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
@Test(singleThreaded = true)
public final class ObjectReaderTest {
    private static final Settings UNSTRICT_SETTINGS = new Settings(new SecureJSON.Builder().strictStrings(false));

    @BeforeTest
    static void setUp() {
        SJSecurityManager.setUp();
    }

    @AfterTest
    static void tearDown() {
        SJSecurityManager.tearDown();
    }

    private ObjectReaderTest() {
    }

    @Test
    public void testSimpleDeserialization() throws IOException {
        final SimpleDeserializationClass mySimpleDeserializationClass = new ObjectReader<>(
            SimpleDeserializationClass.class, UNSTRICT_SETTINGS).accept(new HashMap<CharSequence, Object>() {{
                    put("integerVal", 1);
                    put("shortVal", 2);
                    final ManagedSecureCharBuffer myStringBuffer = new ManagedSecureCharBuffer(Settings.DEFAULTS);
                    myStringBuffer.append("testingString");
                    put("stringVal", myStringBuffer);
                    final ManagedSecureCharBuffer myCSeqBuffer = new ManagedSecureCharBuffer(Settings.DEFAULTS);
                    myCSeqBuffer.append("testingCharSeq");
                    put("charSeqVal", myCSeqBuffer);
                    put("transientIntVal", 123);
                    put("ints", new int[]{1, 2, 3});
                    put("intList", Arrays.asList(4, 5, 6));
                    put("root1", true);
                }});

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

    public void testSimpleDeserializationSecurityViolation() throws IOException {
        try {
            SJSecurityManager.SECURITY_VIOLATIONS.add(new ReflectPermission("suppressAccessChecks"));
            testSimpleDeserialization();
            Assert.fail("Expected exception not thrown");
        } catch (final JSONException.JSONRuntimeException myException) {
            Assert.assertEquals(myException.getCause().getClass(), SecurityException.class);
        }
    }

    @Test
    public void testSimpleNesting() throws IOException {
        final SimpleNestingClass mySimpleNestingClass = new ObjectReader<>(
            SimpleNestingClass.class, UNSTRICT_SETTINGS).accept(new HashMap<CharSequence, Object>() {{
                    put("inner1", new HashMap<CharSequence, Object>() {{
                            put("integerVal", 11);
                            put("shortVal", 21);
                            final ManagedSecureCharBuffer myStringBuffer = new ManagedSecureCharBuffer(
                                Settings.DEFAULTS);
                            myStringBuffer.append("testingString1");
                            put("stringVal", myStringBuffer);
                            final ManagedSecureCharBuffer myCSeqBuffer = new ManagedSecureCharBuffer(Settings.DEFAULTS);
                            myCSeqBuffer.append("testingCharSeq1");
                            put("charSeqVal", myCSeqBuffer);
                            put("transientIntVal", 1234);
                        }});
                    put("inner2", new HashMap<CharSequence, Object>() {{
                            put("integerVal", 111);
                            put("shortVal", 211);
                            final ManagedSecureCharBuffer myStringBuffer = new ManagedSecureCharBuffer(
                                Settings.DEFAULTS);
                            myStringBuffer.append("testingString2");
                            put("stringVal", myStringBuffer);
                            final ManagedSecureCharBuffer myCSeqBuffer = new ManagedSecureCharBuffer(Settings.DEFAULTS);
                            myCSeqBuffer.append("testingCharSeq2");
                            put("charSeqVal", myCSeqBuffer);
                            put("transientIntVal", 12345);
                        }});
                }});

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
    public void testSubNesting() throws IOException {
        final SubNestingClass mySubNestingClass = new ObjectReader<>(
            SubNestingClass.class, UNSTRICT_SETTINGS).accept(new HashMap<CharSequence, Object>() {{
                    put("inner1", new HashMap<CharSequence, Object>() {{
                            put("data1", new HashMap<CharSequence, Object>() {{
                                    put("integerVal", 111);
                                    put("shortVal", 211);
                                    final ManagedSecureCharBuffer myStringBuffer = new ManagedSecureCharBuffer(
                                        Settings.DEFAULTS);
                                    myStringBuffer.append("testingString2");
                                    put("stringVal", myStringBuffer);
                                    final ManagedSecureCharBuffer myCSeqBuffer = new ManagedSecureCharBuffer(
                                        Settings.DEFAULTS);
                                    myCSeqBuffer.append("testingCharSeq2");
                                    put("charSeqVal", myCSeqBuffer);
                                    put("transientIntVal", 1234);
                                }});
                        }});
                }});

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
    public void testRecursiveNesting() throws IOException {
        final SubNestingClass mySubNestingClass = new ObjectReader<>(
                SubNestingClass.class, UNSTRICT_SETTINGS)
                    .accept(new HashMap<CharSequence, Object>() {{
                            put("inner1", new HashMap<CharSequence, Object>() {{
                                    put("inner1", new HashMap<CharSequence, Object>() {{
                                            put("data1", new HashMap<CharSequence, Object>() {{
                                                    put("integerVal", 1111);
                                                    put("shortVal", 2111);
                                                    final ManagedSecureCharBuffer myStringBuffer =
                                                        new ManagedSecureCharBuffer(Settings.DEFAULTS);
                                                    myStringBuffer.append("testingString3");
                                                    put("stringVal", myStringBuffer);
                                                    final ManagedSecureCharBuffer myCSeqBuffer =
                                                        new ManagedSecureCharBuffer(Settings.DEFAULTS);
                                                    myCSeqBuffer.append("testingCharSeq3");
                                                    put("charSeqVal", myCSeqBuffer);
                                                    put("transientIntVal", 12345);
                                                }});

                                            put("inner1", new HashMap<CharSequence, Object>() {{
                                                    put("data1", new HashMap<CharSequence, Object>() {{
                                                            put("integerVal", 111);
                                                            put("shortVal", 211);
                                                            final ManagedSecureCharBuffer myStringBuffer =
                                                                new ManagedSecureCharBuffer(Settings.DEFAULTS);
                                                            myStringBuffer.append("testingString2");
                                                            put("stringVal", myStringBuffer);
                                                            final ManagedSecureCharBuffer myCSeqBuffer =
                                                                new ManagedSecureCharBuffer(Settings.DEFAULTS);
                                                            myCSeqBuffer.append("testingCharSeq2");
                                                            put("charSeqVal", myCSeqBuffer);
                                                            put("transientIntVal", 1234);
                                                        }});
                                                }});
                                        }});
                                }});
                        }});

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
    public void testComplexType() throws IOException {
        final ComplexTypeClass myComplexTypeClass = new ObjectReader<>(
                ComplexTypeClass.class, Settings.DEFAULTS).accept(new HashMap<CharSequence, Object>() {{
                        final List<Map<CharSequence, Map<String, Integer>>> myList1 = new ArrayList<>();
                        myList1.add(new HashMap<CharSequence, Map<String, Integer>>() {{
                                put("1", new HashMap<String, Integer>() {{
                                        put("2", 3);
                                    }});
                            }});
                        final List<Map<CharSequence, Map<String, Integer>>> myList2 = new ArrayList<>();
                        myList2.add(new HashMap<CharSequence, Map<String, Integer>>() {{
                                put("4", new HashMap<String, Integer>() {{
                                        put("5", 6);
                                    }});
                            }});

                        put("data", new List[]{myList1, myList2});
                    }});

        Assert.assertEquals(myComplexTypeClass.data[0], new ArrayList<HashMap<CharSequence, Map<String, Integer>>>() {{
                add(new HashMap<CharSequence, Map<String, Integer>>() {{
                        put("1", new HashMap<String, Integer>() {{
                                put("2", 3);
                            }});
                    }});
            }});
        Assert.assertEquals(myComplexTypeClass.data[1], new ArrayList<HashMap<CharSequence, Map<String, Integer>>>() {{
                add(new HashMap<CharSequence, Map<String, Integer>>() {{
                        put("4", new HashMap<String, Integer>() {{
                                put("5", 6);
                            }});
                    }});
            }});
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testAbsoluteNesting() throws IOException {
        final NestingAbsClass myNestingAbsClass = new ObjectReader<>(
            NestingAbsClass.class, Settings.DEFAULTS).accept(new HashMap<CharSequence, Object>() {{
                    put("1", 11);
                    put("2", 21);
                    put("3", 31);
                    put("level2", new HashMap<CharSequence, Object>() {{
                            put("rel", 41);
                            put("l3", new HashMap<CharSequence, Object>() {{
                                    put("rel", 51);
                                    put("level4", new HashMap<CharSequence, Object>() {{
                                            put("level4", 61);
                                        }});
                                }});
                        }});
                }});

        Assert.assertEquals(myNestingAbsClass.level1, 11);
        Assert.assertEquals(myNestingAbsClass.level2.level2, 21);
        Assert.assertEquals(myNestingAbsClass.level2.rel, 41);
        Assert.assertEquals(myNestingAbsClass.level2.level3.level3, 31);
        Assert.assertEquals(myNestingAbsClass.level2.level3.rel, 51);
        Assert.assertEquals(myNestingAbsClass.level2.level3.level4.level4, 61);
    }

    @SuppressWarnings("unchecked")
    public void testConcreteConstruction() throws IOException {
        final NestingAbsClass.ConcreteTest myConcreteTest = new ObjectReader<>(
                NestingAbsClass.ConcreteTest.class, Settings.DEFAULTS).accept(new HashMap<CharSequence, Object>() {{
                        put("genericMap", new TreeMap<>() {{
                                put("a", "b");
                            }});
                        put("typedMap", new TreeMap<>() {{
                                put("c", "d");
                            }});
                        put("genericSet", new HashSet<>() {{
                                add("e");
                            }});
                        put("typedSet", new HashSet<>() {{
                                add("f");
                            }});
                        put("genericAbstractSet", new HashSet<>() {{
                                add("g");
                            }});
                        put("typedAbstractSet", new HashSet<>() {{
                                add("h");
                            }});
                        put("genericCollection", new ArrayList<>() {{
                                add("i");
                            }});
                        put("typedCollection", new LinkedList<>() {{
                                add("j");
                            }});
                        put("genericAbstractCollection", new HashSet<>() {{
                                add("k");
                            }});
                        put("genericList", new ArrayList<>() {{
                                add("l");
                            }});
                    }});

        Assert.assertEquals(myConcreteTest.genericMap, new LinkedHashMap() {{
                put("a", "b");
            }});
        Assert.assertEquals(myConcreteTest.typedMap, new LinkedHashMap() {{
                put("c", "d");
            }});
        Assert.assertEquals(myConcreteTest.genericSet, new HashSet() {{
                add("e");
            }});
        Assert.assertEquals(myConcreteTest.typedSet, new HashSet() {{
                add("f");
            }});
        Assert.assertEquals(myConcreteTest.genericAbstractSet, new HashSet() {{
                add("g");
            }});
        Assert.assertEquals(myConcreteTest.typedAbstractSet, new HashSet() {{
                add("h");
            }});
        Assert.assertEquals(myConcreteTest.genericCollection, new HashSet() {{
                add("i");
            }});
        Assert.assertEquals(myConcreteTest.typedCollection, new HashSet() {{
                add("j");
            }});
        Assert.assertEquals(myConcreteTest.genericAbstractCollection, new HashSet() {{
                add("k");
            }});
        Assert.assertEquals(myConcreteTest.genericList, new HashSet() {{
                add("l");
            }});
    }

    @SuppressWarnings("unchecked")
    @Test(expectedExceptions = JSONException.JSONRuntimeException.class)
    public void testUnknownType() throws IOException {
        final NestingAbsClass.InvalidClassTest myInvalidTest = new ObjectReader<>(
                NestingAbsClass.InvalidClassTest.class, Settings.DEFAULTS).accept(new HashMap<CharSequence, Object>() {{
                        put("calendar", new ArrayList<>() {{
                                add(Calendar.getInstance());
                            }});
                    }});
    }

    public static final class IdentityHashSetTest {
        @Test(expectedExceptions = NotImplementedException.class)
        public void testAddValue() {
            new ObjectReader.IdentityHashSet<>().size();
        }

        @Test(expectedExceptions = NotImplementedException.class)
        public void testIterator() {
            new ObjectReader.IdentityHashSet<>().iterator();
        }

        @Test
        public void testContains() {
            final ObjectReader.IdentityHashSet<String> myHashSet = new ObjectReader.IdentityHashSet<>();
            myHashSet.add("test");
            Assert.assertTrue(myHashSet.contains("test"));
            Assert.assertFalse(myHashSet.contains("testing"));
            Assert.assertFalse(myHashSet.contains(new String("test".getBytes(StandardCharsets.UTF_8))));
        }
    }

    private static final class SimpleDeserializationClass {
        private SimpleDeserializationClass() {
        }

        private int presetVal = 5;
        private int integerVal;
        private transient int transientIntVal;
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
            @SuppressFBWarnings(value = "UwF")
            private Level4 level4;
        }

        private static final class Level4 {
            private int level4;
        }

        private static final class ConcreteTest {
            private Map genericMap;
            private Map<String, Object> typedMap;
            private Set genericSet;
            private Set<CharSequence> typedSet;
            private AbstractSet genericAbstractSet;
            private AbstractSet<CharSequence> typedAbstractSet;
            private Collection genericCollection;
            private Collection<CharSequence> typedCollection;
            private AbstractCollection genericAbstractCollection;
            private List genericList;
        }

        private static final class InvalidClassTest {
            private Calendar calendar;
        }
    }
}
