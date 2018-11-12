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

/**
 * Objects class to provide simple helpers (functionality from Java 7 Objects)
 */
final class Objects {
    private Objects() {
    }

    static <T> T requireNonNull(final T parObj) {
        if (parObj == null) {
            throw new NullPointerException();
        }
        return parObj;
    }

    static int hash(final Object parObj) {
        if (parObj == null) {
            return 0;
        }

        return parObj.hashCode();
    }
}
