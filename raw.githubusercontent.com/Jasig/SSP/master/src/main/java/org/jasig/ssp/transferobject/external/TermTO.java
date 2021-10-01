/**
 * Licensed to Apereo under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Apereo licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License.  You may obtain a
 * copy of the License at the following location:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.jasig.ssp.transferobject.external;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.jasig.ssp.model.external.Term;
import org.jasig.ssp.transferobject.jsonserializer.DateOnlyDeserializer;
import org.jasig.ssp.transferobject.jsonserializer.DateOnlySerializer;
import com.google.common.collect.Lists;


public class TermTO implements ExternalDataTO<Term> {

	private String code;

	private String name;

	@JsonSerialize(using = DateOnlySerializer.class)
	@JsonDeserialize(using = DateOnlyDeserializer.class)
	private Date startDate;

	@JsonSerialize(using = DateOnlySerializer.class)
	@JsonDeserialize(using = DateOnlyDeserializer.class) 
	private Date endDate;

	private int reportYear;

	public TermTO() {
		super();
	}

	public TermTO(final Term model) {
		super();
		from(model);
	}

	@Override
	public final void from(final Term model) {
		code = model.getCode();
		name = model.getName();
		setStartDate(model.getStartDate());
		setEndDate(model.getEndDate());
		reportYear = model.getReportYear();
	}

	public static List<TermTO> toTOList(
			final Collection<Term> models) {
		final List<TermTO> tObjects = Lists.newArrayList();
		for (final Term model : models) {
			tObjects.add(new TermTO(model)); // NOPMD by jon.adams
		}

		return tObjects;
	}

	public String getName() {
		return name;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public String getCode() {
		return code;
	}

	public void setCode(final String code) {
		this.code = code;
	}

	public Date getStartDate() {
		return startDate == null ? null : new Date(startDate.getTime());
	}

	public final void setStartDate(final Date startDate) {
		this.startDate = startDate == null ? null : new Date(
				startDate.getTime());
	}

	public Date getEndDate() {
		return endDate == null ? null : new Date(endDate.getTime());
	}

	public final void setEndDate(final Date endDate) {
		this.endDate = endDate == null ? null : new Date(endDate.getTime());
	}

	public int getReportYear() {
		return reportYear;
	}

	public void setReportYear(final int reportYear) {
		this.reportYear = reportYear;
	}
}