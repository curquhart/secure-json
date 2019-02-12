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

/**
 * Functional interface that takes one parameter and returns a value.
 *
 * @param <T> The parameter type.
 * @param <R> The return type.
 */
public interface IFunction<T, R> {
    /**
     * Performs this operation on the given argument and returns the result.
     * @param parInput The input argument.
     * @return The processed value.
     */
    R accept(T parInput);
}
