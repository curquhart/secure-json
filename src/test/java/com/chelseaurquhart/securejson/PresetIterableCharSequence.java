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
 * Helper "IterableCharSequence" for helping to compare error messages when we need to inject an offset.
 */
class PresetIterableCharSequence extends IterableCharSequence {
    PresetIterableCharSequence() {
        this(0);
    }

    PresetIterableCharSequence(final int parOffset) {
        super("", parOffset);
    }
}
