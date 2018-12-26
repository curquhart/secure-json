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
 * IStringable is used as an optimization when building strings. If a ICharacterWriter implements IStringable,
 * we will return it (once it is finished and is no longer mutable) as a String instead of a CharSequence.
 */
public interface IStringable {
    /**
     * Convert this object to a string.
     *
     * @return A string representation of the object.
     */
    @Override
    String toString();
}
