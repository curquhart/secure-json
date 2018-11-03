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

import java.security.Permission;
import java.util.LinkedList;
import java.util.List;

/**
 * Helper class for marking specific security violations and tracking them. Tests that use this MUST NOT run in
 * parallel.
 */
final class SJSecurityManager extends SecurityManager {
    static final List<Permission> SECURITY_VIOLATIONS = new LinkedList<>();

    private static final SecurityManager ORIGINAL_SECURITY_MANAGER = System.getSecurityManager();

    static void setUp() {
        System.setSecurityManager(new SecurityManager() {
            @Override
            public void checkPermission(final Permission parPermission) {
                checkPermission(parPermission, null);
            }

            @Override
            public void checkPermission(final Permission parPermission, final Object parContext) {
                for (final Permission myPermission : SECURITY_VIOLATIONS) {
                    if (myPermission.getName().equals(parPermission.getName())
                            && myPermission.getActions().equals(parPermission.getActions())) {

                        SECURITY_VIOLATIONS.remove(myPermission);
                        throw new SecurityException();
                    }
                }
            }

            @Override
            public void checkExit(final int parStatus) {
            }
        });
    }

    static void tearDown() {
        SECURITY_VIOLATIONS.clear();
        System.setSecurityManager(ORIGINAL_SECURITY_MANAGER);
    }
}
