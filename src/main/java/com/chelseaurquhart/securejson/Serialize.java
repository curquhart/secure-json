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

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The Serialize annotation provides settings for JSON serialization.
 *
 * <p>
 *     name can take a String or Array of Strings. It defines where the value is read/written from. Using an array will
 *     use nested values.
 * </p>
 *
 * <p>
 *     relativeTo takes a <a href="Relativity.html">Relativity</a> value. It can be either RELATIVE or ABSOLUTE. Note
 *     that the specific meaning of ABSOLUTE changes when working with collection-like entities (Array, Collection, Map)
 *     in that the root becomes that collection when fields are contained within.
 * </p>
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Serialize {
    /**
     * The target to serialize to. To allow nesting, this supports an array.
     * @return An array of Strings.
     */
    String[] name() default {};

    /**
     * The relativeTo setting. This changes the name that fields are serialized relative to their siblings.
     * @return A Relativity name.
     */
    Relativity relativeTo() default Relativity.RELATIVE;
}
