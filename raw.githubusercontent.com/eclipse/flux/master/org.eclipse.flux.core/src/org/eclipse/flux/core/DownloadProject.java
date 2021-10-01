/*******************************************************************************
 * Copyright (c) 2013, 2014 Pivotal Software, Inc. and others.
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License v1.0 
 * (http://www.eclipse.org/legal/epl-v10.html), and the Eclipse Distribution 
 * License v1.0 (http://www.eclipse.org/org/documents/edl-v10.html). 
 *
 * Contributors:
 *     Pivotal Software, Inc. - initial API and implementation
*******************************************************************************/
package org.eclipse.flux.core;

import java.io.ByteArrayInputStream;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.flux.client.CallbackIDAwareMessageHandler;
import org.eclipse.flux.client.MessageConnector;
import org.eclipse.m2e.core.MavenPlugin;
import org.eclipse.m2e.core.project.ResolverConfiguration;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author Martin Lippert
 */
public class DownloadProject {

	public interface CompletionCallback {
		public void downloadComplete(IProject project);
		public void downloadFailed();
	}

	private MessageConnector messagingConnector;

	private String projectName;
	private int callbackID;
	private CompletionCallback completionCallback;

	private String username;
	private IProject project;

	private Set<String> requestedProjectFiles = new ConcurrentSkipListSet<>();
	private Set<String> projectFiles = new ConcurrentSkipListSet<String>();

	private CallbackIDAwareMessageHandler projectResponseHandler;
	private CallbackIDAwareMessageHandler resourceResponseHandler;

	public DownloadProject(MessageConnector messagingConnector, String projectName, String username) {
		this.messagingConnector = messagingConnector;
		this.projectName = projectName;
		this.username = username;

		this.callbackID = this.hashCode();

		projectResponseHandler = new CallbackIDAwareMessageHandler("getProjectResponse", this.callbackID) {
			@Override
			public void handle(String messageType, JSONObject message) {
				getProjectResponse(message);
			}
		};
		resourceResponseHandler = new CallbackIDAwareMessageHandler("getResourceResponse", this.callbackID) {
			@Override
			public void handle(String messageType, JSONObject message) {
				getResourceResponse(message);
			}
		};
	}

	public void run(final CompletionCallback completionCallback) {
		this.messagingConnector.addMessageHandler(projectResponseHandler);
		this.messagingConnector.addMessageHandler(resourceResponseHandler);

		this.completionCallback = completionCallback;

		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		project = root.getProject(projectName);
		
		WorkspaceJob job = new WorkspaceJob("createProject") {
			@Override
			public IStatus runInWorkspace(IProgressMonitor monitor) throws CoreException {
				try {
					if (!project.exists()) {
						project.create(monitor);
					}
					if (!project.isOpen()) {
						project.open(monitor);
					}

					JSONObject message = new JSONObject();
					message.put("callback_id", callbackID);
					message.put("username", username);
					message.put("project", projectName);

					messagingConnector.send("getProjectRequest", message);
				} catch (Exception e) {
					e.printStackTrace();
					messagingConnector.removeMessageHandler(projectResponseHandler);
					messagingConnector.removeMessageHandler(resourceResponseHandler);
					completionCallback.downloadFailed();
				}

				return Status.OK_STATUS;
			}
		};
		
		job.setRule(ResourcesPlugin.getWorkspace().getRuleFactory().createRule(project));
		job.schedule();
	}

	public void getProjectResponse(JSONObject response) {
		try {
			final String responseProject = response.getString("project");
			final String responseUser = response.getString("username");
			final JSONArray files = response.getJSONArray("files");

			if (this.username.equals(responseUser)) {
				Set<String> newFiles = new HashSet<String>();
				
				for (int i = 0; i < files.length(); i++) {
					JSONObject resource = files.getJSONObject(i);

					String resourcePath = resource.getString("path");
					long timestamp = resource.getLong("timestamp");

					String type = resource.optString("type");

					if (type.equals("folder")) {
						if (!resourcePath.isEmpty()) {
							IFolder folder = project.getFolder(new Path(resourcePath));
							createFolder(folder);
							folder.setLocalTimeStamp(timestamp);
						}
					} else if (type.equals("file")) {
						boolean added = this.projectFiles.add(resourcePath);
						if (added) {
							newFiles.add(resourcePath);
						}
					}
				}

				for (Iterator<String> newFilesIterator = newFiles.iterator(); newFilesIterator.hasNext();) {
					String resourcePath = (String) newFilesIterator.next();

					this.requestedProjectFiles.add(resourcePath);

					JSONObject message = new JSONObject();
					message.put("callback_id", callbackID);
					message.put("username", this.username);
					message.put("project", responseProject);
					message.put("resource", resourcePath);

					messagingConnector.send("getResourceRequest", message);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			this.messagingConnector.removeMessageHandler(projectResponseHandler);
			this.messagingConnector.removeMessageHandler(resourceResponseHandler);
			this.completionCallback.downloadFailed();
		}
	}
	
	private void createFolder(IFolder folder) throws CoreException {
		if (!folder.exists()) {
	        IContainer parent = folder.getParent();
	        if (parent instanceof IFolder) {
	        		createFolder((IFolder) parent);
	        }
			folder.create(true, true, null);
		}
	}
	
	public void getResourceResponse(JSONObject response) {
		try {
			final String responseUser = response.getString("username");
			final String resourcePath = response.getString("resource");
			final long timestamp = response.getLong("timestamp");
			final String content = response.getString("content");

			if (this.username.equals(responseUser)) {
				IFile file = project.getFile(resourcePath);
				if (!file.exists()) {
					file.create(new ByteArrayInputStream(content.getBytes()), true, null);
				} else {
					file.setContents(new ByteArrayInputStream(content.getBytes()), true, false, null);
				}
				file.setLocalTimeStamp(timestamp);

				this.requestedProjectFiles.remove(resourcePath);
				if (this.requestedProjectFiles.isEmpty()) {
					this.messagingConnector.removeMessageHandler(projectResponseHandler);
					this.messagingConnector.removeMessageHandler(resourceResponseHandler);
					finish();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			this.messagingConnector.removeMessageHandler(projectResponseHandler);
			this.messagingConnector.removeMessageHandler(resourceResponseHandler);
			this.completionCallback.downloadFailed();
		}
	}
	
	public void finish() {
		if (projectFiles.contains("pom.xml") && !projectFiles.contains(".project")) {
			IFile pomFile = project.getFile("pom.xml");
			if (pomFile != null && pomFile.exists()) {
				importAsPureMavenProject(pomFile);
			}
		}
		// we need to do the same for Gradle projects
		
		this.completionCallback.downloadComplete(project);
	}

	private void importAsPureMavenProject(IFile pomFile) {
		WorkspaceJob job = new WorkspaceJob("importAsMaven") {
			
			@Override
			public IStatus runInWorkspace(IProgressMonitor monitor) throws CoreException {
				try {
					ResolverConfiguration resolverConfiguration = new ResolverConfiguration();
					String activeProfiles = "pom.xml";
					resolverConfiguration.setActiveProfiles(activeProfiles);
					
					MavenPlugin.getProjectConfigurationManager().enableMavenNature(project, resolverConfiguration, monitor);
					return Status.OK_STATUS;
				}
				catch (CoreException e) {
					e.printStackTrace();
					return Status.OK_STATUS; 
				}
			}
		};
		
		job.setRule(ResourcesPlugin.getWorkspace().getRoot());
		job.schedule();
	}

}
