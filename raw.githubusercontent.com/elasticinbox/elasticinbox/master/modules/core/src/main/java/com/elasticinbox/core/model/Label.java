/**
 * Copyright (c) 2011-2013 Optimax Software Ltd.
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 *  * Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *  * Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *  * Neither the name of Optimax Software, ElasticInbox, nor the names
 *    of its contributors may be used to endorse or promote products derived
 *    from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.elasticinbox.core.model;

import java.util.HashMap;
import java.util.Map;

import com.elasticinbox.common.utils.Assert;

public class Label
{
	private Integer id;
	private String name;
	private LabelCounters counters;
	private Map<String, String> attributes;

	public Label() {
		// required for JSON deserialization 
	}

	public Label(int id) {
		this.id = id;
	}

	public Label(int id, String name) {
		this.id = id;
		this.name = name;
	}

	public Integer getId() {
		return id;
	}

	public Label setId(int id) {
		this.id = id;
		return this;
	}

	public String getName() {
		return name;
	}

	public Label setName(String name) {
		this.name = name;
		return this;
	}

	public LabelCounters getCounters() {
		return counters;
	}

	public Label setCounters(LabelCounters counters) {
		this.counters = counters;
		return this;
	}

	public Map<String, String> getAttributes() {
		return attributes;
	}

	public Label setAttributes(Map<String, String> attributes) {
		this.attributes = attributes;
		return this;
	}

	/**
	 * Add attribute
	 * 
	 * @param name
	 * @param value
	 * @return
	 */
	public Label addAttribute(String name, String value)
	{
		if (this.attributes == null) {
			this.attributes = new HashMap<String, String>(1);
		}
		
		this.attributes.put(name, value);

		return this;
	}

	public void incrementCounters(LabelCounters diff)
	{
		Assert.notNull(counters, "Label counters are not initialized");
		counters.add(diff);
	}

	@Override
	public int hashCode() {
		return Integer.valueOf(id).hashCode();
	}
}