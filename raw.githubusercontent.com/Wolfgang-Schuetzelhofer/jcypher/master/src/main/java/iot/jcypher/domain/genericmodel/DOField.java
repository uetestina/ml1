/************************************************************************
 * Copyright (c) 2015 IoT-Solutions e.U.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *  http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ************************************************************************/

package iot.jcypher.domain.genericmodel;

import iot.jcypher.domain.genericmodel.internal.DomainModel;

import java.lang.reflect.Field;
import java.util.List;

public class DOField {
	
	public static String COMPONENTTYPE_Object = "java.lang.Object";
	public static String LIST_TYPE = "java.util.List";
	
	private String name;
	private String typeName;
	private String componentTypeName;
	private boolean buidInType;
	private DOType ownerType;
	private Field field;
	
	/**
	 * Create either a field i.e. an attribute defined in a domain object type or
	 * a list field i.e. a list or array attribute defined in a domain object type, depending on the value of the isList parameter.
	 * @param name
	 * @param typeName specifies the field's type or the list's component type depending on the value of the isList parameter.
	 * @param isList defines if this is a field or a list field
	 * @param ownerType
	 */
	DOField(String name, String typeName, boolean isList, DOType ownerType) {
		super();
		this.name = name;
		this.typeName = isList ? LIST_TYPE : typeName;
		this.buidInType = DomainModel.isBuildIn(this.typeName);
		this.ownerType = ownerType;
		if (isList)
			this.componentTypeName = typeName != null ? typeName : COMPONENTTYPE_Object;
	}

	public String getName() {
		return name;
	}

	public String getTypeName() {
		return typeName;
	}

	public String getComponentTypeName() {
		return componentTypeName;
	}

	public boolean isBuidInType() {
		return buidInType;
	}
	
	public boolean isListOrArray() {
		return field.getType().isArray() ||
				List.class.isAssignableFrom(field.getType());
	}
	
	void setComponentTypeName(String componentTypeName) {
		this.componentTypeName = componentTypeName;
	}

	void setValue(Object target, Object value) {
		try {
			getField().set(target, value);
		} catch (IllegalArgumentException | IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}
	
	Object getValue(Object target) {
		try {
			return getField().get(target);
		} catch (IllegalArgumentException | IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}
	
	public String asString(String indent) {
		StringBuilder sb = new StringBuilder();
		sb.append(indent);
		sb.append(this.name);
		sb.append(" : ");
		sb.append(this.typeName);
		sb.append(" (buildIn: ");
		sb.append(this.buidInType);
		sb.append(')');
		if (this.componentTypeName != null) {
			sb.append('[');
			sb.append(this.componentTypeName);
			sb.append(']');
		}
		return sb.toString();
	}
	
	private Field getField() {
		if (this.field == null) {
			Class<?> rawType;
			try {
				rawType = this.ownerType.getRawType();
				this.field = rawType.getDeclaredField(this.name);
				this.field.setAccessible(true);
			} catch (Throwable e) {
				throw new RuntimeException(e);
			}
		}
		return this.field;
	}

}
