/*
 * Black Duck Software Suite SDK
 * Copyright (C) 2015  Black Duck Software, Inc.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */
package com.blackducksoftware.sdk.protex.client.examples;

import java.util.List;

import com.blackducksoftware.sdk.fault.SdkFault;
import com.blackducksoftware.sdk.protex.client.util.ProtexServerProxy;
import com.blackducksoftware.sdk.protex.user.User;
import com.blackducksoftware.sdk.protex.user.UserApi;
import com.blackducksoftware.sdk.protex.user.UserColumn;
import com.blackducksoftware.sdk.protex.user.UserPageFilter;
import com.blackducksoftware.sdk.protex.util.PageFilterFactory;

/**
 * This sample demonstrates how to list users in the order of their last name
 * 
 * It demonstrates:
 * - How to get a list users
 * - How to use PageFilter to sort the users
 */
public class SampleListUsersSortedByLastName extends BDProtexSample {

    private static UserApi userApi = null;

    /**
     * Output usage information for this sample
     */
    private static void usage() {
        String className = SampleListUsersSortedByLastName.class.getSimpleName();
        outputUsageDetails(className, getDefaultUsageParameters(), getDefaultUsageParameterDetails());
    }

    public static void main(String[] args) throws Exception {
        // check and save parameters
        if (args.length < 3) {
            System.err.println("Not enough parameters!");
            usage();
            System.exit(-1);
        }
        String serverUri = args[0];
        String username = args[1];
        String password = args[2];

        Long connectionTimeout = 120 * 1000L;

        ProtexServerProxy myProtexServer = null;

        try {
            try {
                myProtexServer = new ProtexServerProxy(serverUri, username, password, connectionTimeout);

                userApi = myProtexServer.getUserApi();
            } catch (RuntimeException e) {
                System.err.println("\nConnection to server '" + serverUri + "' failed: " + e.getMessage());
                throw e;
            }

            // Call the Api
            List<User> users = null;
            UserPageFilter pageFilter = PageFilterFactory.getAllRows(UserColumn.LAST_NAME);

            try {
                users = userApi.getUsers(pageFilter);
            } catch (SdkFault e) {
                System.err.println("getUsers() failed: " + e.getMessage());
                System.exit(-1);
            }

            if (users == null || users.isEmpty()) {
                System.out.println("No Users returned");
            } else {
                for (User user : users) {
                    System.out.println("User: " + user.getLastName() + ", " + user.getFirstName() + " ("
                            + user.getEmail() + ")");
                }
            }
        } catch (Exception e) {
            System.err.println("SampleListUsersSortedByLastName failed");
            e.printStackTrace(System.err);
            System.exit(-1);
        } finally {
            // This is optional - it causes the proxy to overwrite the stored password with null characters, increasing
            // security
            if (myProtexServer != null) {
                myProtexServer.close();
            }
        }
    }

}
