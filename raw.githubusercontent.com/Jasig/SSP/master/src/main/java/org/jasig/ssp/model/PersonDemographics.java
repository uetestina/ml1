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
package org.jasig.ssp.model; // NOPMD by jon.adams on 5/24/12 1:34 PM

import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.validation.constraints.Size;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.jasig.ssp.model.reference.ChildCareArrangement;
import org.jasig.ssp.model.reference.Citizenship;
import org.jasig.ssp.model.reference.EmploymentShifts;
import org.jasig.ssp.model.reference.Ethnicity;
import org.jasig.ssp.model.reference.Race;
import org.jasig.ssp.model.reference.Genders;
import org.jasig.ssp.model.reference.MaritalStatus;
import org.jasig.ssp.model.reference.MilitaryAffiliation;
import org.jasig.ssp.model.reference.VeteranStatus;

/**
 * Students should have some demographic information stored for use in
 * notifications to appropriate users, and for reporting purposes.
 * 
 * Students may have one associated demographic instance (one-to-one mapping).
 * Non-student users should never have any demographic information associated to
 * them.
 * 
 * @author jon.adams
 */
@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public class PersonDemographics // NOPMD by jon.adams on 5/24/12 1:34 PM
		extends AbstractAuditable implements Auditable {

	private static final long serialVersionUID = 3252611289245443664L;

	private Boolean local;

	private BigDecimal balanceOwed;

	@Column(length = 50)
	@Size(max = 50)
	private String countryOfResidence;

	@Column(length = 25)
	@Size(max = 25)
	private String paymentStatus;

	@ManyToOne(fetch = FetchType.LAZY)
	@Cascade({ CascadeType.PERSIST, CascadeType.MERGE, CascadeType.SAVE_UPDATE })
	@JoinColumn(name = "marital_status_id", nullable = true)
	private MaritalStatus maritalStatus;

	@ManyToOne(fetch = FetchType.LAZY)
	@Cascade({ CascadeType.PERSIST, CascadeType.MERGE, CascadeType.SAVE_UPDATE })
	@JoinColumn(name = "military_affiliation_id", nullable = true)
	private MilitaryAffiliation militaryAffiliation;	
	
	@ManyToOne(fetch = FetchType.LAZY)
	@Cascade({ CascadeType.PERSIST, CascadeType.MERGE, CascadeType.SAVE_UPDATE })
	@JoinColumn(name = "ethnicity_id", nullable = true)
	private Ethnicity ethnicity;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@Cascade({ CascadeType.PERSIST, CascadeType.MERGE, CascadeType.SAVE_UPDATE })
	@JoinColumn(name = "race_id", nullable = true)
	private Race race;

	@Enumerated(EnumType.STRING)
	private Genders gender;

	@ManyToOne(fetch = FetchType.LAZY)
	@Cascade({ CascadeType.PERSIST, CascadeType.MERGE, CascadeType.SAVE_UPDATE })
	@JoinColumn(name = "citizenship_id", nullable = true)
	private Citizenship citizenship;

	@Column(length = 50)
	@Size(max = 50)
	private String countryOfCitizenship;

	@ManyToOne(fetch = FetchType.LAZY)
	@Cascade({ CascadeType.PERSIST, CascadeType.MERGE, CascadeType.SAVE_UPDATE })
	@JoinColumn(name = "veteran_status_id", nullable = true)
	private VeteranStatus veteranStatus;

	private Boolean primaryCaregiver, childCareNeeded;

	private Integer numberOfChildren;

	@ManyToOne(fetch = FetchType.LAZY)
	@Cascade({ CascadeType.PERSIST, CascadeType.MERGE, CascadeType.SAVE_UPDATE })
	@JoinColumn(name = "child_care_arrangement_id", nullable = true)
	private ChildCareArrangement childCareArrangement;

	@Column(length = 50)
	@Size(max = 50)
	private String childAges;

	private Boolean employed;

	@Column(length = 50)
	@Size(max = 50)
	private String placeOfEmployment;

	@Enumerated(EnumType.STRING)
	private EmploymentShifts shift;

	@Column(length = 50)
	@Size(max = 50)
	private String wage;

	@Column(length = 20)
	@Size(max = 20)
	private String totalHoursWorkedPerWeek;

	public BigDecimal getBalanceOwed() {
		return balanceOwed;
	}

	public void setBalanceOwed(final BigDecimal balanceOwed) {
		this.balanceOwed = balanceOwed;
	}

	public Boolean getLocal() {
		return local;
	}

	public void setLocal(final Boolean local) {
		this.local = local;
	}

	public String getCountryOfResidence() {
		return countryOfResidence;
	}

	public void setCountryOfResidence(final String countryOfResidence) {
		this.countryOfResidence = countryOfResidence;
	}

	public String getPaymentStatus() {
		return paymentStatus;
	}

	public void setPaymentStatus(final String paymentStatus) {
		this.paymentStatus = paymentStatus;
	}

	public MaritalStatus getMaritalStatus() {
		return maritalStatus;
	}

	public void setMaritalStatus(final MaritalStatus maritalStatus) {
		this.maritalStatus = maritalStatus;
	}

	public MilitaryAffiliation getMilitaryAffiliation() {
		return militaryAffiliation;
	}

	public void setMilitaryAffiliation(final MilitaryAffiliation militaryAffiliation) {
		this.militaryAffiliation = militaryAffiliation;
	}
	
	public Ethnicity getEthnicity() {
		return ethnicity;
	}

	public void setEthnicity(final Ethnicity ethnicity) {
		this.ethnicity = ethnicity;
	}
	
	public Race getRace() {
		return race;
	}
	
	public void setRace(final Race race) {
		this.race = race;
	}

	public Genders getGender() {
		return gender;
	}

	public void setGender(final Genders gender) {
		this.gender = gender;
	}

	public Citizenship getCitizenship() {
		return citizenship;
	}

	public void setCitizenship(final Citizenship citizenship) {
		this.citizenship = citizenship;
	}

	public String getCountryOfCitizenship() {
		return countryOfCitizenship;
	}

	public void setCountryOfCitizenship(final String countryOfCitizenship) {
		this.countryOfCitizenship = countryOfCitizenship;
	}

	public VeteranStatus getVeteranStatus() {
		return veteranStatus;
	}

	public void setVeteranStatus(final VeteranStatus veteranStatus) {
		this.veteranStatus = veteranStatus;
	}

	public Boolean getPrimaryCaregiver() {
		return primaryCaregiver;
	}

	public void setPrimaryCaregiver(final Boolean primaryCaregiver) {
		this.primaryCaregiver = primaryCaregiver;
	}

	public Integer getNumberOfChildren() {
		return numberOfChildren;
	}

	public void setNumberOfChildren(final Integer numberOfChildren) {
		this.numberOfChildren = numberOfChildren;
	}

	public ChildCareArrangement getChildCareArrangement() {
		return childCareArrangement;
	}

	public void setChildCareArrangement(
			final ChildCareArrangement childCareArrangement) {
		this.childCareArrangement = childCareArrangement;
	}

	public String getChildAges() {
		return childAges;
	}

	public void setChildAges(final String childAges) {
		this.childAges = childAges;
	}

	public Boolean getChildCareNeeded() {
		return childCareNeeded;
	}

	public void setChildCareNeeded(final Boolean childCareNeeded) {
		this.childCareNeeded = childCareNeeded;
	}

	public Boolean getEmployed() {
		return employed;
	}

	public void setEmployed(final Boolean employed) {
		this.employed = employed;
	}

	public String getPlaceOfEmployment() {
		return placeOfEmployment;
	}

	public void setPlaceOfEmployment(final String placeOfEmployment) {
		this.placeOfEmployment = placeOfEmployment;
	}

	public EmploymentShifts getShift() {
		return shift;
	}

	public void setShift(final EmploymentShifts shift) {
		this.shift = shift;
	}

	public String getWage() {
		return wage;
	}

	public void setWage(final String wage) {
		this.wage = wage;
	}

	public String getTotalHoursWorkedPerWeek() {
		return totalHoursWorkedPerWeek;
	}

	public void setTotalHoursWorkedPerWeek(final String totalHoursWorkedPerWeek) {
		this.totalHoursWorkedPerWeek = totalHoursWorkedPerWeek;
	}

	@Override
	protected int hashPrime() {
		return 11;
	}

	@Override
	final public int hashCode() { // NOPMD by jon.adams on 5/9/12 7:13 PM
		int result = hashPrime();

		// AbstractAuditable properties
		result *= hashField("id", getId());
		result *= hashField("objectStatus", getObjectStatus());

		// PersonDemographics
		result *= hashField("balanceOwed", balanceOwed);
		result *= local == null ? "local".hashCode()
				: (local ? 3 : 5);
		result *= hashField("countryOfResidence", countryOfResidence);
		result *= hashField("paymentStatus", paymentStatus);
		result *= hashField("maritalStatus", maritalStatus);
		result *= hashField("militaryAffiliation", militaryAffiliation);
		result *= hashField("ethnicity", ethnicity);
		result *= hashField("race", race);
		result *= gender == null ? "gender".hashCode() : gender.hashCode();
		result *= hashField("citizenship", citizenship);
		result *= hashField("countryOfCitizenship", countryOfCitizenship);
		result *= hashField("veteranStatus", veteranStatus);
		result *= primaryCaregiver == null ? "primaryCaregiver".hashCode()
				: (primaryCaregiver ? 13 : 17);
		result *= hashField("numberOfChildren", numberOfChildren);
		result *= hashField("childCareArrangement", childCareArrangement);
		result *= hashField("childAges", childAges);
		result *= childCareNeeded == null ? "childCareNeeded".hashCode()
				: (childCareNeeded ? 19 : 23);
		result *= employed == null ? "employed".hashCode()
				: (employed ? 29 : 31);
		result *= hashField("placeOfEmployment", placeOfEmployment);
		result *= shift == null ? "shift".hashCode() : shift.hashCode();
		result *= hashField("wage", wage);
		result *= hashField("totalHoursWorkedPerWeek", totalHoursWorkedPerWeek);

		return result;
	}
}