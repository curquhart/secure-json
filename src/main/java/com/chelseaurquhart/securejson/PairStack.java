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

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Stack that takes a pair of elements. Specialized to never instantiate or require object instantiation
 * except during construction.
 *
 * @exclude
 */
class PairStack<T, U> {
    private transient Deque<T> part1;
    private transient Deque<U> part2;
    private final transient Pair<T, U> pair;

    PairStack() {
        part1 = new ArrayDeque<>();
        part2 = new ArrayDeque<>();
        pair = new Pair<>();
    }

    /**
     * Add a pair to the queue.
     *
     * @param parFirst The first component of the pair.
     * @param parSecond The second component of the pair.
     */
    void push(final T parFirst, final U parSecond) {
        part1.push(parFirst);
        part2.push(parSecond);
    }

    /**
     * Get a pair out of the queue. WARNING: This will return the same instance every time to reduce object
     * constructions so do not depend on it persisting!
     *
     * @return A populated Pair instance.
     */
    Pair<T, U> pop() {
        pair.first = part1.pop();
        pair.second = part2.pop();

        return pair;
    }

    /**
     * Get a pair out of the queue without removing it. WARNING: This will return the same instance every time to reduce
     * object constructions so do not depend on it persisting!
     *
     * @return A populated Pair instance.
     */
    Pair<T, U> peek() {
        pair.first = part1.peek();
        pair.second = part2.peek();

        return pair;
    }

    int size() {
        return part1.size();
    }

    boolean isEmpty() {
        return part1.isEmpty();
    }

    /**
     * A pair of elements for the stack.
     *
     * @param <T> The type of the first element.
     * @param <U> The type of the second element.
     */
    static class Pair<T, U> {
        T first;
        U second;
    }
}
