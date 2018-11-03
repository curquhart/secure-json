package com.chelseaurquhart.securejson;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;

public final class MessagesTest {
    private static final String DATA_PROVIDER_NAME = "MessagesTest";

    @DataProvider(name = DATA_PROVIDER_NAME)
    private static Object[] dataProvider() {
        return Messages.Key.values();
    }

    private MessagesTest() {
    }

    @Test(dataProvider = DATA_PROVIDER_NAME)
    public void testMessages(final Messages.Key parMessageKey) {
        try {
            Assert.assertNotEquals(Messages.get(parMessageKey), "");
        } catch (final IOException myException) {
            Assert.fail(String.format("Could not read message %s", parMessageKey), myException);
        }
    }
}
