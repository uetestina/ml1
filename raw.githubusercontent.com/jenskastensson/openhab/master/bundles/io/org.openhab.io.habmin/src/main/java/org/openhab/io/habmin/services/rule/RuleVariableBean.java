/**
 * Copyright (c) 2010-2013, openHAB.org and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.io.habmin.services.rule;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * This is a java bean that is used with JAXB to serialize items
 * to XML or JSON.
 *
 * @author Chris Jackson
 * @since 1.4.0
 *
 */
@XmlRootElement(name="variable")
public class RuleVariableBean {

	public String name;
	public String type;
	public String label;
	
	public String itemtype;

	public String value;
	public String description;
	public String scope;

	public RuleVariableBean() {}
}
