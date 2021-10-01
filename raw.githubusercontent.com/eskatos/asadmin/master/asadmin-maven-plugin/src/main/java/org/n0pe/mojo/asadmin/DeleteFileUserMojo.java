/*
 * Copyright (c) 2010, Paul Merlin.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package org.n0pe.mojo.asadmin;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.n0pe.asadmin.AsAdminCmdList;
import org.n0pe.asadmin.commands.DeleteFileUser;

/**
 * @goal delete-file-user
 */
public class DeleteFileUserMojo
    extends AbstractAsadminMojo
{

    /**
     * @parameter
     * @required
     */
    private String userName;

    @Override
    protected AsAdminCmdList getAsCommandList()
        throws MojoExecutionException, MojoFailureException
    {
        getLog().info( "Delete file user: " + userName );
        AsAdminCmdList cmdList = new AsAdminCmdList();
        cmdList.add( new DeleteFileUser( userName ) );
        return cmdList;
    }

}
