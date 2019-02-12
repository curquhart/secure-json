/*
 * Copyright 2019 Chelsea Urquhart
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
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;

@SuppressWarnings("PMD.CommentRequired")
public final class MessagesTest {
    private static final String DATA_PROVIDER_NAME = "MessagesTest";

    @DataProvider(name = DATA_PROVIDER_NAME)
    static Object[] dataProvider() {
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
