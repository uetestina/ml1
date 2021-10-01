/**
 * <a href="http://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, http://www.frentix.com
 * <p>
 */
package org.olat.repository;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.id.Identity;
import org.olat.core.id.Roles;
import org.olat.core.id.UserConstants;
import org.olat.repository.model.SearchRepositoryEntryParameters;
import org.olat.resource.OLATResource;
import org.olat.resource.OLATResourceManager;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Description:<br>
 * 
 * <P>
 * Initial Date:  18 oct. 2011 <br>
 *
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class RepositoryManagerQueryTest extends OlatTestCase {
	
	private static final String TEST_RES_NAME = "TestManagerQuery";
	
	@Autowired
	private RepositoryManager rm;
	@Autowired
	private RepositoryService repositoryService;
	@Autowired
	private UserManager userManager;
	
	private static Identity admin;
	
	private static final String author = "RepositoryManagerQueryAuthor";
	private static boolean initialized = false;
	
	public void testManagers() {
		assertNotNull(rm);
	}
	
	@Before
	public void setup() {
		if(initialized) return;
		
		DB db = DBFactory.getInstance();
		
		admin = JunitTestHelper.createAndPersistIdentityAsAdmin("administrator");
		
		Identity id1 = JunitTestHelper.createAndPersistIdentityAsAuthor(author + "1");
		id1.getUser().setProperty(UserConstants.FIRSTNAME, author + "1");
		id1.getUser().setProperty(UserConstants.LASTNAME, author + "1");
		userManager.updateUserFromIdentity(id1);

		Identity id2 = JunitTestHelper.createAndPersistIdentityAsAuthor(author + "2");
		id2.getUser().setProperty(UserConstants.FIRSTNAME, author + "2");
		id2.getUser().setProperty(UserConstants.LASTNAME, author + "2");
		userManager.updateUserFromIdentity(id2);

		Identity institut1 = JunitTestHelper.createAndPersistIdentityAsAuthor("kanu");
		institut1.getUser().setProperty(UserConstants.INSTITUTIONALNAME, "Volks");
		institut1.getUser().setProperty(UserConstants.FIRSTNAME, "Kanu");
		institut1.getUser().setProperty(UserConstants.LASTNAME, "Unchou");
		userManager.updateUserFromIdentity(institut1);

		Identity institut2 = JunitTestHelper.createAndPersistIdentityAsAuthor("rei");
		institut2.getUser().setProperty(UserConstants.INSTITUTIONALNAME, "Nerv");
		institut2.getUser().setProperty(UserConstants.FIRSTNAME, "Rei");
		institut2.getUser().setProperty(UserConstants.LASTNAME, "Ayanami");
		userManager.updateUserFromIdentity(institut2);

		db.commitAndCloseSession();

		// generate some repo entries
		int numbRes = 500;
		for (int i = 0; i < numbRes; i++) {
			
			
			int rest = i%4;
			int access = 0;
			Identity owner = null;
			switch(rest) {
				case 0: {
					access = RepositoryEntry.ACC_USERS_GUESTS;
					owner = id1;
					break;
				}
				case 1: {
					access = RepositoryEntry.ACC_USERS;
					owner = id2;
					break;
				}
				case 2: {
					access = RepositoryEntry.ACC_OWNERS;
					owner = institut1;
					break;
				}
				case 3: {
					access = RepositoryEntry.ACC_OWNERS;
					owner = institut2;
					break;
				}
			}
			
			// create course and persist as OLATResourceImpl
			RepositoryEntry re = createCourseRepositoryEntry(db, owner, i);
			re.setAccess(access);

			repositoryService.update(re);
			if (i % 20 == 0) {
				db.commitAndCloseSession();
			}
		}

		db.commitAndCloseSession();
		
		initialized = true;
	}
	
	@Test
	public void testOneShootQueryWithRoles() {
		List<String> types = Collections.singletonList(TEST_RES_NAME);
		
		//roles: author + institution manager
		Roles role2 = new Roles(false, false, false, true, false, true, false);
		SearchRepositoryEntryParameters params = new SearchRepositoryEntryParameters(null, null, null, types, admin, role2, "Volks");
		List<RepositoryEntry> resultOneShootInstitut = rm.genericANDQueryWithRolesRestriction(params, 0, -1, true);
		assertNotNull(resultOneShootInstitut);
		assertFalse(resultOneShootInstitut.isEmpty());
	}
	
	@Test
	public void testOneShootQueryWithAuthorRole() {
		List<String> types = Collections.singletonList(TEST_RES_NAME);
		
		//roles: author + institution manager
		Roles role2 = new Roles(false, false, false, true, false, false, false);

		SearchRepositoryEntryParameters params = new SearchRepositoryEntryParameters(null, null, null, types, null, role2, null);
		List<RepositoryEntry> resultOneShoot = rm.genericANDQueryWithRolesRestriction(params, 0, -1, true);
		assertNotNull(resultOneShoot);
		assertFalse(resultOneShoot.isEmpty());
	}
	
	@Test
	public void testOneShootWithInstitution() {
		List<String> types = Collections.singletonList(TEST_RES_NAME);
		
		//roles: institution manager
		Roles role3 = new Roles(false, false, false, true, false, true, false);

		SearchRepositoryEntryParameters params = new SearchRepositoryEntryParameters(null, null, null, types, null, role3, "Volks");
		List<RepositoryEntry> resultOneShootInstitut3 = rm.genericANDQueryWithRolesRestriction(params, 0, -1, true);
		assertNotNull(resultOneShootInstitut3);
		assertFalse(resultOneShootInstitut3.isEmpty());
	}
	
	@Test
	public void testOneShootWithInstitutionAndSearchByAuthorName() {
		List<String> types = Collections.singletonList(TEST_RES_NAME);
		
		//roles: institution manager search: authorname
		Roles role4 = new Roles(false, false, false, false, false, true, false);

		SearchRepositoryEntryParameters params = new SearchRepositoryEntryParameters(null, "kan", null, types, null, role4, "Volks");
		List<RepositoryEntry> resultOneShootInstitut4 = rm.genericANDQueryWithRolesRestriction(params, 0, -1, true);
		assertNotNull(resultOneShootInstitut4);
		assertFalse(resultOneShootInstitut4.isEmpty());
	}
	
	@Test
	public void testOneShootQueryPaging() {
		List<String> types = Collections.singletonList(TEST_RES_NAME);
		
		//roles: institution manager search: authorname
		Roles role4 = new Roles(false, false, false, false, false, true, false);

		
		//test paging
		SearchRepositoryEntryParameters params = new SearchRepositoryEntryParameters(null, "kan", null, types, null, role4, "Volks");
		List<RepositoryEntry> resultOneShootInstitut6 = rm.genericANDQueryWithRolesRestriction(params, 0, 50, true);
		rm.countGenericANDQueryWithRolesRestriction(params);
		assertNotNull(resultOneShootInstitut6);
		assertEquals(50, resultOneShootInstitut6.size());
	}
	
	private RepositoryEntry createCourseRepositoryEntry(DB db, Identity owner, final int i) {
		OLATResource r =  OLATResourceManager.getInstance().createOLATResourceInstance(TEST_RES_NAME);
		db.saveObject(r);
		// now make a repository entry for this course
		RepositoryEntry re = repositoryService.create(owner, null,
				"Lernen mit OLAT " + i, "JunitTest_RepositoryEntry_" + i, "Description of learning by OLAT " + i, r, RepositoryEntry.ACC_OWNERS);
		//db.commit();
		return re;
	}
}