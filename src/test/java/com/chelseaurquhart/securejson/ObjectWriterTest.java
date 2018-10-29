package com.chelseaurquhart.securejson;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.LinkedList;

public final class ObjectWriterTest {
    private ObjectWriterTest() {
    }

    @Test
    void testSimpleObject() throws JSONException {
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

    @Test
    void testJSONAwareList() throws JSONException {
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
