package ntut.csie.ezScrum.restful.mobilel.service;

import static org.junit.Assert.assertEquals;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import ntut.csie.ezScrum.issue.sql.service.core.Configuration;
import ntut.csie.ezScrum.refactoring.manager.ProjectManager;
import ntut.csie.ezScrum.restful.mobile.service.ReleasePlanWebService;
import ntut.csie.ezScrum.test.CreateData.CreateProject;
import ntut.csie.ezScrum.test.CreateData.CreateRelease;
import ntut.csie.ezScrum.test.CreateData.CreateSprint;
import ntut.csie.ezScrum.test.CreateData.InitialSQL;
import ntut.csie.ezScrum.web.dataObject.ProjectObject;
import ntut.csie.ezScrum.web.dataObject.ReleaseObject;
import ntut.csie.ezScrum.web.dataObject.SprintObject;
import ntut.csie.ezScrum.web.databaseEnum.ReleaseEnum;
import ntut.csie.ezScrum.web.databaseEnum.SprintEnum;
import ntut.csie.ezScrum.web.helper.ReleasePlanHelper;
import ntut.csie.jcis.account.core.LogonException;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ReleasePlanWebServiceTest {
	private int mProjectCount = 1;
	private int mReleaseCount = 3;
	private int mSprintCount = 3;
	private CreateProject mCP;
	private CreateRelease mCR;
	private ProjectObject mProject;
	private ReleasePlanHelper mReleasePlanHelper;
	private ReleasePlanWebService mReleasePlanWebService;
	private Configuration mConfig;

	@Before
	public void setUp() throws Exception {
		mConfig = new Configuration();
		mConfig.setTestMode(true);
		mConfig.save();
		
		// 初始化 SQL
		InitialSQL ini = new InitialSQL(mConfig);
		ini.exe();

		// 新增一個 Project
		mCP = new CreateProject(mProjectCount);
		mCP.exeCreate();

		mCR = new CreateRelease(mReleaseCount, mCP);
		mCR.exe();
		
		mProject = mCP.getAllProjects().get(0);
		mReleasePlanHelper = new ReleasePlanHelper(mProject);
	}

	@After
	public void tearDown() throws Exception {
		// 初始化 SQL
		InitialSQL ini = new InitialSQL(mConfig);
		ini.exe();

		// 刪除外部檔案
		ProjectManager projectManager = new ProjectManager();
		projectManager.deleteAllProject();
		
		mConfig.setTestMode(false);
		mConfig.save();
		// release
		mCP = null;
		mCR = null;
		mProject = null;
		mReleasePlanHelper = null;
		mReleasePlanWebService = null;
		mConfig = null;
	}

	@Test
	public void testGetAllReleasePlan() throws LogonException, JSONException {
		String username = "admin";
		String userpwd = "admin";
		String projectID = mProject.getName();
		mReleasePlanWebService = new ReleasePlanWebService(username, userpwd, projectID);

		// create sprint
		CreateSprint CS = new CreateSprint(mSprintCount, mCP);
		CS.exe();

		// 從ReleasePlanHelper拿出release做assert
		List<ReleaseObject> releasePlanDescs = mReleasePlanHelper.getReleases();
		JSONArray releasesJSONArray = new JSONArray(mReleasePlanWebService.getAllReleasePlan());
		
		for (int i = 0; i < mReleaseCount; i++) {
			JSONObject releaseJson = (JSONObject) releasesJSONArray.get(i);
			assertEquals(releasePlanDescs.get(i).getId(), releaseJson.get(ReleaseEnum.ID));
			assertEquals(releasePlanDescs.get(i).getName(), releaseJson.get(ReleaseEnum.NAME));
			assertEquals(releasePlanDescs.get(i).getDescription(), releaseJson.get(ReleaseEnum.DESCRIPTION));
			JSONArray sprintJsonArray = new JSONArray(releaseJson.get("sprints").toString());
			ArrayList<SprintObject> sprints = releasePlanDescs.get(i).getSprints();
			// assert ReleasePlan中的SprintPlan
			for(int j = 0; j < sprintJsonArray.length(); j++) {
				JSONObject sprintJson = (JSONObject) sprintJsonArray.get(j);
				assertEquals(sprints.get(j).getId(), sprintJson.getLong(SprintEnum.ID));
				assertEquals(sprints.get(j).getGoal(), sprintJson.get(SprintEnum.GOAL));
				assertEquals(sprints.get(j).getInterval(), sprintJson.get(SprintEnum.INTERVAL));
				assertEquals(sprints.get(j).getTeamSize(), sprintJson.get(SprintEnum.TEAM_SIZE));
				assertEquals(sprints.get(j).getFocusFactor(), sprintJson.get(SprintEnum.FOCUS_FACTOR));
				assertEquals(sprints.get(j).getAvailableHours(), sprintJson.get(SprintEnum.AVAILABLE_HOURS));
				assertEquals(sprints.get(j).getDemoPlace(), sprintJson.get(SprintEnum.DEMO_PLACE));
				assertEquals(sprints.get(j).getDailyInfo(), sprintJson.get(SprintEnum.DAILY_INFO));
			}
		}
	}
	
	@Test
	public void testGetReleasePlan() throws LogonException, JSONException, SQLException {
		String username = "admin";
		String userpwd = "admin";
		String projectID = mProject.getName();
		mReleasePlanWebService = new ReleasePlanWebService(username, userpwd, projectID);

		// create sprint
		CreateSprint CS = new CreateSprint(mSprintCount, mCP);
		CS.exe();

		// 從ReleasePlanHelper拿出release做assert
		ArrayList<ReleaseObject> releases = mReleasePlanHelper.getReleases();
		
		for(int i = 0; i < mReleaseCount ; i++){
			JSONObject releaseJson = new JSONObject(mReleasePlanWebService.getReleasePlan(releases.get(i).getId()));
			assertEquals(releases.get(i).getId(), releaseJson.get(ReleaseEnum.ID));
			assertEquals(releases.get(i).getName(), releaseJson.get(ReleaseEnum.NAME));
			assertEquals(releases.get(i).getStartDateString(), releaseJson.get(ReleaseEnum.START_DATE));
			assertEquals(releases.get(i).getDueDateString(), releaseJson.get(ReleaseEnum.DUE_DATE));
			assertEquals(releases.get(i).getDescription(), releaseJson.get(ReleaseEnum.DESCRIPTION));
		}
	}
}
