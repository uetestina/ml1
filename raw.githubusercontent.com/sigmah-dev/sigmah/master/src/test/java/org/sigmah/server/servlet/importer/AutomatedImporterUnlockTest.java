package org.sigmah.server.servlet.importer;

/*
 * #%L
 * Sigmah
 * %%
 * Copyright (C) 2010 - 2016 URD
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.extjs.gxt.ui.client.data.BaseModelData;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

import javax.persistence.EntityTransaction;

import org.sigmah.server.dao.AbstractDaoTest;
import org.sigmah.server.dao.UserDAO;
import org.sigmah.server.dispatch.impl.UserDispatch;
import org.sigmah.server.domain.*;
import org.sigmah.server.domain.base.Entity;
import org.sigmah.server.domain.element.DefaultFlexibleElement;
import org.sigmah.server.domain.element.TextAreaElement;
import org.sigmah.server.domain.importation.ImportationScheme;
import org.sigmah.server.domain.importation.ImportationSchemeModel;
import org.sigmah.server.domain.importation.Variable;
import org.sigmah.server.domain.importation.VariableFlexibleElement;
import org.sigmah.server.domain.layout.Layout;
import org.sigmah.server.domain.profile.GlobalPermission;
import org.sigmah.server.domain.profile.OrgUnitProfile;
import org.sigmah.server.domain.profile.Profile;
import org.sigmah.server.domain.value.Value;
import org.sigmah.server.mapper.Mapper;
import org.sigmah.server.security.Authenticator;
import org.sigmah.shared.Language;
import org.sigmah.shared.command.AutomatedImport;
import org.sigmah.shared.dispatch.CommandException;
import org.sigmah.shared.dto.ProjectDTO;
import org.sigmah.shared.dto.importation.ImportationSchemeDTO;
import org.sigmah.shared.dto.referential.AmendmentState;
import org.sigmah.shared.dto.referential.AutomatedImportStatus;
import org.sigmah.shared.dto.referential.DefaultFlexibleElementType;
import org.sigmah.shared.dto.referential.GlobalPermissionEnum;
import org.sigmah.shared.dto.referential.ImportationSchemeFileFormat;
import org.sigmah.shared.dto.referential.ImportationSchemeImportType;
import org.sigmah.shared.dto.referential.ProjectModelStatus;
import org.sigmah.shared.dto.referential.TextAreaType;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Test class for <code>AutomatedImporter</code>.
 * 
 * @author Raphaël Calabro (raphael.calabro@netapsys.fr)
 */
public class AutomatedImporterUnlockTest extends AbstractDaoTest {
	
	private static final String EMAIL_ADDRESS = "urd-sigmah+test@ideia.fr";
	
	@Inject
	private Injector injector;
	
	@Inject
	private Mapper mapper;
	
	@Inject
	private Authenticator authenticator;
	
	@Inject
	private UserDAO userDAO;
	
	@Inject
	private UserDispatch dispatch;
	
	private Entity[] entities = new Entity[0];
	
	private int projectId;
	private int schemeId;
	private int introductionElementId;
	private int phaseModelId;
	
	@Before
	public void before() {
		persistEntities();
	}
	
	@After
	public void after() {
		removeEntities();
	}
	
	/**
	 * Test of importCorrespondances method, of class AutomatedImporter.
	 */
	@Test
	public void testImportCorrespondances() throws CommandException, IOException {
		final CsvImporter importer = new CsvImporter();
		importer.setInjector(injector);
		importer.setScheme(getImportationScheme());
		importer.setExecutionContext(getExecutionContext());
		importer.initialize();

		importer.setInputStream(getClass().getResourceAsStream("import.csv"));

		final AutomatedImport configuration = new AutomatedImport("1234", "import.csv", getImportationScheme(), true, true, false);
		
		final AutomatedImporter instance = new AutomatedImporter(importer);
		final List<BaseModelData> result = instance.importCorrespondances(configuration);
		
		Assert.assertEquals(2, result.size());
		Assert.assertEquals(projectId, result.get(0).get(ProjectDTO.ID));
		Assert.assertEquals("I1", result.get(0).get(ProjectDTO.NAME));
		Assert.assertEquals("TestProject", result.get(0).get(ProjectDTO.FULL_NAME));
		Assert.assertEquals(AutomatedImportStatus.UNLOCKED_AND_UPDATED, result.get(0).get("status"));
		
		Assert.assertNotEquals(0, result.get(1).get(ProjectDTO.ID));
		Assert.assertEquals("I8", result.get(1).get(ProjectDTO.NAME));
		Assert.assertEquals("Mon projet qui n'existe pas", result.get(1).get(ProjectDTO.FULL_NAME));
		Assert.assertEquals(AutomatedImportStatus.CREATED_AND_UPDATED, result.get(1).get("status"));

		final Project project = em().find(Project.class, projectId);
		Assert.assertEquals("I1", project.getName());
		Assert.assertEquals("Mon projet d’import", project.getFullName());
		Assert.assertEquals("Ce projet incroyable, efficace et plein d'avenir devrait sauver beaucoup de personnes", em().createQuery(
				"SELECT v.value from Value AS v WHERE v.containerId = :projectId AND v.element.id = :elementId", String.class)
				.setParameter("projectId", projectId)
				.setParameter("elementId", introductionElementId)
				.getSingleResult());

		final Project project2 = em().find(Project.class, result.get(1).get(ProjectDTO.ID));
		Assert.assertEquals("I8", project2.getName());
		Assert.assertEquals("Mon projet qui n'existe pas", project2.getFullName());
		Assert.assertEquals("Rien", em().createQuery(
				"SELECT v.value from Value AS v WHERE v.containerId = :projectId AND v.element.id = :elementId", String.class)
				.setParameter("projectId", project2.getId())
				.setParameter("elementId", introductionElementId)
				.getSingleResult());
	}
	
	private ImportationSchemeDTO getImportationScheme() {
		return mapper.map(em().find(ImportationScheme.class, schemeId), new ImportationSchemeDTO());
	}
	
	private UserDispatch.UserExecutionContext getExecutionContext() {
		return dispatch.createContext(getUser(), null, null);
	}
	
	private User getUser() {
		return userDAO.findUserByEmail(EMAIL_ADDRESS);
	}
	
	private void persistEntities() {
		final EntityTransaction transaction = em().getTransaction();
		transaction.begin();
		
		// Project Model
		final DefaultFlexibleElement codeElement = new DefaultFlexibleElement();
		codeElement.setType(DefaultFlexibleElementType.CODE);
		codeElement.setAmendable(true);
		em().persist(codeElement);
		
		final DefaultFlexibleElement titleElement = new DefaultFlexibleElement();
		titleElement.setAmendable(true);
		titleElement.setType(DefaultFlexibleElementType.TITLE);
		em().persist(titleElement);
		
		final TextAreaElement introductionElement = new TextAreaElement();
		introductionElement.setType(TextAreaType.TEXT.getCode());
		em().persist(introductionElement);
		
		introductionElementId = introductionElement.getId();
		
		final Layout detailsLayout = new Layout(3, 1);
		detailsLayout.addConstraint(0, 0, codeElement, 0);
		detailsLayout.addConstraint(1, 0, titleElement, 0);
		detailsLayout.addConstraint(2, 0, introductionElement, 0);
		
		final Layout bannerLayout = new Layout(0, 0);
		
		final Layout phaseModelLayout = new Layout(0, 0);
		
		final ProjectModel model = new ProjectModel();
		model.setName("TestModel");
		model.setStatus(ProjectModelStatus.READY);
		model.setPhaseModels(new ArrayList<PhaseModel>());
		model.setVisibilities(new ArrayList<ProjectModelVisibility>());
		
		final PhaseModel phaseModel = new PhaseModel();
		phaseModel.setName("TestPhaseModel");
		phaseModel.setDisplayOrder(0);
		phaseModel.setParentProjectModel(model);
		phaseModel.setLayout(phaseModelLayout);
		model.getPhaseModels().add(phaseModel);
		
		final ProjectDetails details = new ProjectDetails();
		details.setLayout(detailsLayout);
		details.setProjectModel(model);
		
		em().persist(details);
		
		final ProjectBanner banner = new ProjectBanner();
		banner.setProjectModel(model);
		banner.setLayout(bannerLayout);
		
		em().persist(banner);
		
		model.setProjectDetails(details);
		model.setProjectBanner(banner);
		
		em().persist(model);
		
		phaseModelId = model.getPhaseModels().get(0).getId();
		
		// User
		final User user = new User();
		user.setActive(Boolean.TRUE);
		user.setEmail(EMAIL_ADDRESS);
		user.setName("TestLastName");
		user.setFirstName("TestFirstName");
		user.setHashedPassword(authenticator.hashPassword("sigmah"));
		user.setLocale(Language.FR.getLocale());
		em().persist(user);
		
		// OrgUnit
		final OrgUnit orgUnit = new OrgUnit();
		orgUnit.setName("TOU");
		orgUnit.setFullName("Test Org Unit");
		orgUnit.setCanContainProjects(true);
		em().persist(orgUnit);
		
		// Profile
		final Profile profile = new Profile();
		profile.setName("Test Profile");
		profile.setGlobalPermissions(new ArrayList<GlobalPermission>());
		profile.getGlobalPermissions().add(globalPermission(GlobalPermissionEnum.LOCK_PROJECT, profile));
		em().persist(profile);

		final OrgUnitProfile orgUnitProfile = new OrgUnitProfile();
		orgUnitProfile.setType(OrgUnitProfile.OrgUnitProfileType.MAIN);
		orgUnitProfile.setUser(user);
		orgUnitProfile.setOrgUnit(orgUnit);
		orgUnitProfile.setProfiles(new ArrayList<Profile>());
		orgUnitProfile.getProfiles().add(profile);
		em().persist(orgUnitProfile);
		List<OrgUnitProfile> orgUnitsProfiles = new ArrayList<>();
		orgUnitsProfiles.add(orgUnitProfile);
		user.setOrgUnitsWithProfiles(orgUnitsProfiles);
		em().merge(user);
		
		// Importation Scheme
		final ArrayList<Variable> variables = new ArrayList<>();
		
		final ImportationScheme scheme = new ImportationScheme();
		scheme.setName("Test scheme");
		scheme.setFileFormat(ImportationSchemeFileFormat.CSV);
		scheme.setFirstRow(2);
		scheme.setImportType(ImportationSchemeImportType.ROW);
		scheme.setVariables(variables);
		
		final Variable codeVariable = new Variable();
		codeVariable.setImportationScheme(scheme);
		codeVariable.setName("Code");
		codeVariable.setReference("0");
		
		final Variable titleVariable = new Variable();
		titleVariable.setImportationScheme(scheme);
		titleVariable.setName("Title");
		titleVariable.setReference("1");
		
		final Variable introductionVariable = new Variable();
		introductionVariable.setImportationScheme(scheme);
		introductionVariable.setName("Introduction");
		introductionVariable.setReference("5");
		
		variables.add(codeVariable);
		variables.add(titleVariable);
		variables.add(introductionVariable);
		
		em().persist(scheme);
		
		schemeId = scheme.getId();
		
		// Importation Scheme Model
		final ArrayList<VariableFlexibleElement> variableFlexibleElements = new ArrayList<>();
		
		final ImportationSchemeModel importationSchemeModel = new ImportationSchemeModel();
		importationSchemeModel.setImportationScheme(scheme);
		importationSchemeModel.setProjectModel(model);
		importationSchemeModel.setVariableFlexibleElements(variableFlexibleElements);
		
		final VariableFlexibleElement codeVariableFlexibleElement = new VariableFlexibleElement();
		codeVariableFlexibleElement.setImportationSchemeModel(importationSchemeModel);
		codeVariableFlexibleElement.setIsKey(Boolean.TRUE);
		codeVariableFlexibleElement.setFlexibleElement(codeElement);
		codeVariableFlexibleElement.setVariable(codeVariable);
		
		final VariableFlexibleElement titleVariableFlexibleElement = new VariableFlexibleElement();
		titleVariableFlexibleElement.setImportationSchemeModel(importationSchemeModel);
		titleVariableFlexibleElement.setFlexibleElement(titleElement);
		titleVariableFlexibleElement.setVariable(titleVariable);
		
		final VariableFlexibleElement introductionVariableFlexibleElement = new VariableFlexibleElement();
		introductionVariableFlexibleElement.setImportationSchemeModel(importationSchemeModel);
		introductionVariableFlexibleElement.setFlexibleElement(introductionElement);
		introductionVariableFlexibleElement.setVariable(introductionVariable);
		
		variableFlexibleElements.add(codeVariableFlexibleElement);
		variableFlexibleElements.add(titleVariableFlexibleElement);
		variableFlexibleElements.add(introductionVariableFlexibleElement);
		
		em().persist(importationSchemeModel);
		
		
		// Project, country and values
		final Country country = new Country();
		country.setName("Testry");
		country.setCodeISO("_t");
		country.setBounds(new Bounds());
		em().persist(country);
		
		final Project project = new Project();
		project.setProjectModel(model);
		project.setName("I1");
		project.setFullName("TestProject");
		project.setAmendmentState(AmendmentState.LOCKED);
		project.setPhases(new ArrayList<Phase>());
		project.setStartDate(new Date());
		project.setOwner(user);
		project.setLastSchemaUpdate(new Date());
		project.setCountry(country);
		project.setPartners(new HashSet<OrgUnit>());
		project.getPartners().add(orgUnit);
		em().persist(project);
		
		this.projectId = project.getId();
		
		final Value introductionValue = new Value();
		introductionValue.setValue("Pas d'introduction");
		introductionValue.setContainerId(project.getId());
		introductionValue.setElement(introductionElement);
		introductionValue.setLastModificationAction('C');
		introductionValue.setLastModificationDate(new Date());
		introductionValue.setLastModificationUser(user);
		em().persist(introductionValue);
		
		transaction.commit();
		
		this.entities = new Entity[] {
			banner,
			details,
			codeVariableFlexibleElement,
			titleVariableFlexibleElement,
			introductionVariableFlexibleElement,
			importationSchemeModel,
			codeVariable,
			titleVariable,
			introductionVariable,
			scheme,
			model,
			bannerLayout,
			detailsLayout,
			phaseModelLayout,
			introductionElement,
			titleElement,
			codeElement,
			profile,
			orgUnit,
			country
		};
	}
	
	private void removeEntities() {
		final EntityTransaction transaction = em().getTransaction();
		transaction.begin();

		User user = getUser();
		em().createQuery("DELETE FROM HistoryToken AS ht WHERE ht.user = :user")
				.setParameter("user", user)
				.executeUpdate();

		em().createQuery("DELETE FROM Value AS v WHERE v.lastModificationUser = :user")
				.setParameter("user", user)
				.executeUpdate();
		
		for (final Project project : em().createQuery("SELECT p FROM Project AS p WHERE p.owner = :user", Project.class)
				.setParameter("user", user)
				.getResultList()) {
			project.setPartners(new HashSet<OrgUnit>());
			em().persist(project);
			em().remove(project);
		}

		for (OrgUnitProfile orgUnitProfile : em().createQuery("SELECT oup FROM OrgUnitProfile AS oup WHERE oup.user = :user", OrgUnitProfile.class)
				.setParameter("user", user)
				.getResultList()) {
			em().remove(orgUnitProfile);
		}
		user.setOrgUnitsWithProfiles(new ArrayList<OrgUnitProfile>());
		em().persist(user);

		for (final Entity entity : entities) {
			em().remove(entity);
		}
		em().remove(user);

		this.entities = new Entity[0];
		transaction.commit();
	}
	
	private GlobalPermission globalPermission(GlobalPermissionEnum permission, Profile profile) {
		final GlobalPermission globalPermission = new GlobalPermission();
		globalPermission.setProfile(profile);
		globalPermission.setPermission(permission);
		return globalPermission;
	}
	
}
