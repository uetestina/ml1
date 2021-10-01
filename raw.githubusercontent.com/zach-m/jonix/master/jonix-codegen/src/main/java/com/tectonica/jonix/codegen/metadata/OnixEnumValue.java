/*
 * Copyright (C) 2012-2020 Zach Melamed
 *
 * Latest version available online at https://github.com/zach-m/jonix
 * Contact me at zach@tectonica.co.il
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tectonica.jonix.codegen.metadata;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder( {"name", "value", "description", "comment"})
public class OnixEnumValue implements Comparable<OnixEnumValue> {
    public String name;
    public String value;
    public String description;
    public String comment; // additional comment for Javadocs

    public static OnixEnumValue create(String name, String value, String description) {
        OnixEnumValue oev = new OnixEnumValue();
        oev.name = name;
        oev.value = value;
        oev.description = description;
        return oev;
    }

    @Override
    public String toString() {
        return "{" + value + "=" + name + "}";
    }

    /**
     * comparator for unification processes; relevant when comparing values of one enum to values of another. in such
     * cases all that matters is the raw value.
     */
    @Override
    public int compareTo(OnixEnumValue other) {
        return value.compareTo(other.value); // NOTE: this is a value comparison, not name
    }
}