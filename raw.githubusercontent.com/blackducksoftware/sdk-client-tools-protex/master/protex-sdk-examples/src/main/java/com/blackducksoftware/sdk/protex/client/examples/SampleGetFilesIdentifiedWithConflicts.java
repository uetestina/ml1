/*
 * Copyright (C) 2009, 2010 Black Duck Software Inc.
 * http://www.blackducksoftware.com/
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Black Duck Software ("Confidential Information"). You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Black Duck Software.
 */
package com.blackducksoftware.sdk.protex.client.examples;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.blackducksoftware.sdk.fault.SdkFault;
import com.blackducksoftware.sdk.protex.client.util.ProtexServerProxy;
import com.blackducksoftware.sdk.protex.project.codetree.CodeTreeApi;
import com.blackducksoftware.sdk.protex.project.codetree.CodeTreeNode;
import com.blackducksoftware.sdk.protex.project.codetree.CodeTreeNodeRequest;
import com.blackducksoftware.sdk.protex.project.codetree.CodeTreeNodeType;
import com.blackducksoftware.sdk.protex.project.codetree.NodeCountType;
import com.blackducksoftware.sdk.protex.util.CodeTreeUtilities;

/**
 * This sample demonstrates how to get the code tree for a project
 *
 * It demonstrates:
 * - How to generate a CodeTree starting at the ROOT = "/"
 * - How to generate a CodeTree with indefinite depth
 */
public class SampleGetFilesIdentifiedWithConflicts extends BDProtexSample {

    private static CodeTreeApi codetreeApi = null;

    /**
     * Output usage information for this sample
     */
    private static void usage() {
        String className = SampleGetFilesIdentifiedWithConflicts.class.getSimpleName();

        List<String> parameters = new ArrayList<String>(getDefaultUsageParameters());
        parameters.add("<project ID>");

        List<String> paramDescriptions = new ArrayList<String>(getDefaultUsageParameterDetails());
        paramDescriptions.add(formatUsageDetail("project ID",
                "The ID of the project to get the files with conflicts for, i.e. \"c_newsampleproject\""));

        outputUsageDetails(className, parameters, paramDescriptions);
    }

    public static void main(String[] args) throws Exception {
        // check and save parameters
        if (args.length < 4) {
            System.err.println("Not enough parameters!");
            usage();
            System.exit(-1);
        }

        String serverUri = args[0];
        String username = args[1];
        String password = args[2];
        String projectId = args[3];

        Long connectionTimeout = 120 * 1000L;

        ProtexServerProxy myProtexServer = null;

        try {
            try {
                myProtexServer = new ProtexServerProxy(serverUri, username, password, connectionTimeout);

                codetreeApi = myProtexServer.getCodeTreeApi();
            } catch (RuntimeException e) {
                System.err.println("Connection to server '" + serverUri + "' failed: " + e.getMessage());
                throw e;
            }

            String root = "/";

            List<CodeTreeNode> codeTree = null;

            try {
                // request code tree for all nodes, including the parentPath node
                CodeTreeNodeRequest nodeRequest = new CodeTreeNodeRequest();
                nodeRequest.setDepth(CodeTreeUtilities.INFINITE_DEPTH);
                nodeRequest.setIncludeParentNode(false);
                nodeRequest.getIncludedNodeTypes().add(CodeTreeNodeType.FILE);
                nodeRequest.getCounts().add(NodeCountType.LICENSE_CONFLICTS);

                codeTree = codetreeApi.getCodeTreeNodes(projectId, root, nodeRequest);
            } catch (SdkFault e) {
                System.err.println("getCodeTree failed: " + e.getMessage());
                throw new RuntimeException(e);
            }

            System.out.println("Code Tree requested for " + root + " with indefinite depth");

            List<String> pathsWithConflict = new ArrayList<String>();
            if (codeTree != null) {
                for (CodeTreeNode node : codeTree) {
                    Map<NodeCountType, Long> counts = CodeTreeUtilities.getNodeCountMap(node);

                    if (counts.get(NodeCountType.LICENSE_CONFLICTS) > 0) {
                        pathsWithConflict.add(node.getName());
                    }
                }
            } else {
                System.out.println("No code tree nodes found in project " + projectId);
            }

            System.out.println("Project with ID " + projectId + " has identifications with license conflicts on these files:");

            for (String filePath : pathsWithConflict) {
                System.out.println("\t" + filePath);
            }
        } catch (Exception e) {
            System.err.println("SampleGetFileIdentifiedWithConflicts failed");
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
