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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder( {"name", "primitiveType", "simpleTypeName", "enumName"})
public class OnixAttribute implements Comparable<OnixAttribute> {
    public String name;
    public Primitive primitiveType;
    @JsonIgnore
    public OnixSimpleType simpleType; // may be null if this attribute is based on a primitive type

    private OnixAttribute() {
    }

    public String getSimpleTypeName() {
        return (simpleType == null) ? null : simpleType.name;
    }

    public String getEnumName() {
        return (simpleType == null) ? null : simpleType.enumName;
    }

    public static OnixAttribute create(String name, Primitive primitiveType) {
        OnixAttribute ova = new OnixAttribute();
        ova.name = name;
        ova.primitiveType = primitiveType;
        ova.simpleType = null;
        return ova;
    }

    public static OnixAttribute create(String name, OnixSimpleType simpleType) {
        OnixAttribute ova = new OnixAttribute();
        ova.name = name;
        ova.primitiveType = simpleType.primitiveType;
        ova.simpleType = simpleType;
        return ova;
    }

    @Override
    public String toString() {
        return name + "(" + primitiveType.name() + " / " + getSimpleTypeName() + ")";
    }

    @Override
    public int compareTo(OnixAttribute other) {
        return name.compareTo(other.name);
    }
}