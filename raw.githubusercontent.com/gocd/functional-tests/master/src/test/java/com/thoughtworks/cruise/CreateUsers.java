/*************************GO-LICENSE-START*********************************
 * Copyright 2015 ThoughtWorks, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *************************GO-LICENSE-END***********************************/

package com.thoughtworks.cruise;

import com.thoughtworks.cruise.page.OnAnyPage;
import com.thoughtworks.cruise.page.OnLoginPage;
import com.thoughtworks.cruise.page.OnPreferencesPage;
import com.thoughtworks.cruise.state.ScenarioState;
import com.thoughtworks.cruise.utils.ScenarioHelper;
import net.sf.sahi.client.Browser;

public class CreateUsers {   

    private String email;

    private final ScenarioState scenarioState;

	private final ScenarioHelper scenarioHelper;

	private final Browser browser;    
    
    public CreateUsers(ScenarioState scenarioState, ScenarioHelper scenarioHelper, Browser browser) {
        this.scenarioState = scenarioState;
		this.scenarioHelper = scenarioHelper;
		this.browser = browser;
    }

    public void setAlias(String alias) throws Exception {
        System.err.println("setting alias " + alias);
        new OnPreferencesPage(scenarioState, browser).changeEmailToAndAliasTo(this.email, alias);
    }

    public void setEmail(String email) throws Exception {        
        this.email = email;
    }
    
    public void setUsername(String username) throws Exception {
        System.err.println("logging in as" + username);
        new OnLoginPage(scenarioState, scenarioHelper, browser).loginAs(username);
        System.err.println("logged in");
    }
    
    public void setUp() {        
        new OnAnyPage(scenarioState, scenarioHelper, browser).logout();        
    }

    public void tearDown() throws Exception {        
        new OnAnyPage(scenarioState, scenarioHelper, browser).logout();        
    }

	@com.thoughtworks.gauge.Step("CreateUsers <table>")
	public void brtMethod(com.thoughtworks.gauge.Table table) throws Throwable {
		com.thoughtworks.twist.migration.brt.BRTMigrator brtMigrator = new com.thoughtworks.twist.migration.brt.BRTMigrator();
		try {
			brtMigrator.BRTExecutor(table, this);
		} catch (Exception e) {
			throw e.getCause();
		}
	}

}