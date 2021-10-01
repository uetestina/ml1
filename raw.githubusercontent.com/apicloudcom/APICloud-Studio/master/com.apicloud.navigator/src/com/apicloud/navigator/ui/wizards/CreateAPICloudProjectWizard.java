/**
 * APICloud Studio
 * Copyright (c) 2014-2015 by APICloud, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3.
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */

package com.apicloud.navigator.ui.wizards;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.widgets.Display;
import org.eclipse.team.core.TeamException;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.eclipse.ui.wizards.newresource.BasicNewProjectResourceWizard;
import org.json.JSONException;
import org.json.JSONObject;

import com.apicloud.authentication.AuthenticActivator;
import com.apicloud.commons.util.MD5Util;
import com.apicloud.commons.util.UtilActivator;
import com.apicloud.commons.model.Config;
import com.apicloud.navigator.Activator;
import com.apicloud.navigator.dialogs.Messages;
import com.apicloud.navigator.ui.pages.NewUZProjectWizardPage;
import com.apicloud.networkservice.RC4Util;

import org.tigris.subversion.subclipse.core.util.SVNUtil;

public class CreateAPICloudProjectWizard extends Wizard implements INewWizard{
	private NewUZProjectWizardPage page;
	private IProject project;
	private IWorkbench mworkbench;
	private String userName;
	private String appId;
	private String svnUrl;
	private String ip;
	private String userPassWord;
	private String uname;
	private String cookie;
	/**
	 * Constructor
	 */
	public CreateAPICloudProjectWizard() {
		super();
	}

	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		mworkbench = workbench;
		initinfo();
		setNeedsProgressMonitor(true);
	}

	private void initinfo() {
		Properties p =AuthenticActivator.getProperties();
		userName = p.getProperty("username");
		userPassWord = RC4Util
				.HloveyRC4(p.getProperty("password"), RC4Util.key);
		setIp(p.getProperty("ip"));
		cookie = p.getProperty("cookie");
	}

	@Override
	public void addPages() {
		setWindowTitle(Messages.CREATEAPPWIZARD);
		page = new NewUZProjectWizardPage(
				Messages.CREATEPROJECTWIZARD);
		addPage(page);
	}

	public boolean canFinish() {
		if (page.isCanFinish()) {
			return true;
		}
		return false;
	}

	@Override
	public boolean performFinish() {
		if (project != null) {
			return true;
		}
		final IProject projectHandle = page.getProjectHandle();
		if (projectHandle == null) {
			return false;
		}

		if (!validate(projectHandle.getName())) {
			return false;
		}
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		final IProjectDescription desc = workspace
				.newProjectDescription(projectHandle.getName());
		WorkspaceModifyOperation op = new WorkspaceModifyOperation() {
			protected void execute(IProgressMonitor monitor)
					throws CoreException {
				try {
					createProject1(desc, projectHandle, monitor);
				} catch (OperationCanceledException e) {
					e.printStackTrace();
				}
			}
		};
		try {
			getContainer().run(true, true, op);
		} catch (Exception e) {
			e.printStackTrace();
		}
		project = projectHandle;
		if (project == null) {
			UtilActivator.logger.info("end project is null ");
			return false;
		}

		IWorkbenchWindow ww = mworkbench.getActiveWorkbenchWindow();
		BasicNewProjectResourceWizard.selectAndReveal(project, ww);

		this.page.dispose();
		return true;
	}

	String javahlLibZipPackagePath;

	void createProject1(IProjectDescription description, final IProject proj,
			IProgressMonitor monitor) throws CoreException,
			OperationCanceledException {

		try {
			monitor.beginTask("create...", 1000);
			proj.create(description, monitor);
			monitor.worked(1);
			if (monitor.isCanceled()) {
				throw new OperationCanceledException();
			}
			proj.open(IResource.BACKGROUND_REFRESH, new SubProgressMonitor(
					monitor, 300));
			IContainer container = (IContainer) proj;
			String projectPath = container.getLocation().toOSString();
			try {
				copyProject(projectPath);
				createConfigFile(container, monitor);
				Job svnJob = new Job("create....") {
					@Override
					protected IStatus run(IProgressMonitor monitor) {
						try {
							SVNUtil.syncProjectToSVN(proj, userName,
									MD5Util.String2MD5(userPassWord), svnUrl);
							TimeUnit.MILLISECONDS.sleep(50L);
						} catch (TeamException e) {
							e.printStackTrace();
							return Status.CANCEL_STATUS;
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						monitor.done();
						return Status.OK_STATUS;
					}
				};
				svnJob.schedule();
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} finally {
			monitor.done();
		}
	}

	private void copyProject(final String targetPath) {
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				try {
					String projectName = page.getAPICloudProjectName();
					String sourcePath = new File(FileLocator.toFileURL(
							Platform.getBundle(Activator.PLUGIN_ID)
									.getResource("resource/" + projectName))
							.getFile()).getAbsolutePath();

					File[] files = (new File(sourcePath)).listFiles();
					for (int i = 0; i < files.length; i++) {
						if (files[i].isFile()) {
							copyFile(files[i], new File(targetPath
									+ File.separator + files[i].getName()));
						}
						if (files[i].isDirectory()
								&& !files[i].getName().startsWith(".")) {
							String sorceDir = sourcePath + File.separator
									+ files[i].getName();
							String targetDir = targetPath + File.separator
									+ files[i].getName();
							copyDirectiory(sorceDir, targetDir);
						}
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});
	}

	void createProject(IProjectDescription description, final IProject proj,
			IProgressMonitor monitor) throws CoreException,
			OperationCanceledException {
		try {
			monitor.beginTask("", 2000);
			proj.create(description, new SubProgressMonitor(monitor, 1000));
			if (monitor.isCanceled()) {
				throw new OperationCanceledException();
			}
			proj.open(IResource.BACKGROUND_REFRESH, new SubProgressMonitor(
					monitor, 1000));
			IContainer container = (IContainer) proj;
			IFolder Folder = container.getFolder(new Path("icon"));
			Folder.create(true, true, monitor);
			Folder = container.getFolder(new Path("css"));
			Folder.create(true, true, monitor);
			final IFile common_css = container.getFile(new Path(
					"/css/common.css"));
			common_css
					.create(this.getClass().getResourceAsStream(
							"/resource/common.css"), true, monitor);
			final IFile uz_css = container.getFile(new Path("/css/api.css"));
			uz_css.create(
					this.getClass().getResourceAsStream("/resource/api.css"),
					true, monitor);
			Folder = container.getFolder(new Path("html"));
			Folder.create(true, true, monitor);

			final IFile main_html = container.getFile(new Path(
					"/html/main.html"));
			main_html.create(
					this.getClass().getResourceAsStream("/resource/main.html"),
					true, monitor);

			Folder = container.getFolder(new Path("script"));
			Folder.create(true, true, monitor);
			final IFile uz_js = container.getFile(new Path("/script/api.js"));
			uz_js.create(this.getClass()
					.getResourceAsStream("/resource/api.js"), true, monitor);

			Folder = container.getFolder(new Path("launch"));
			Folder.create(true, true, monitor);

			Folder = container.getFolder(new Path("image"));
			Folder.create(true, true, monitor);
			final IFile image = container.getFile(new Path(
					"/image/loading_more.gif"));
			image.create(
					this.getClass().getResourceAsStream(
							"/icons/loading_more.gif"), true, monitor);

			Folder = container.getFolder(new Path("feature"));
			Folder.create(true, true, monitor);

			Folder = container.getFolder(new Path("res"));
			Folder.create(true, true, monitor);

			Folder = container.getFolder(new Path("wgt"));
			Folder.create(true, true, monitor);
			try {
				createPictureResource(container, monitor);
				createConfigFile(container, monitor);
				final IFile indexIFile = container.getFile(new Path(
						"/index.html"));
				indexIFile.create(
						this.getClass().getResourceAsStream(
								"/resource/index.html"), true, monitor);
				Job svnJob = new Job("") {

					@Override
					protected IStatus run(IProgressMonitor monitor) {
						try {
							SVNUtil.syncProjectToSVN(proj, userName,
									MD5Util.String2MD5(userPassWord), svnUrl);
						} catch (TeamException e) {
							e.printStackTrace();
							return Status.CANCEL_STATUS;
						}
						return Status.OK_STATUS;
					}
				};
				svnJob.schedule(300L);


			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} finally {
			monitor.done();
		}
	}

	private void createPictureResource(IContainer container,
			IProgressMonitor monitor) throws CoreException {
		final IFile icon57 = container
				.getFile(new Path("/icon/icon150x150.png"));
		icon57.create(
				this.getClass().getResourceAsStream("/icons/icon150x150.png"),
				true, monitor);

		final IFile launch640x960 = container.getFile(new Path(
				"/launch/launch1080x1920.png"));
		launch640x960.create(
				this.getClass().getResourceAsStream(
						"/icons/launch1080x1920.png"), true, monitor);
	}

	private void createConfigFile(final IContainer container,
			final IProgressMonitor monitor) throws CoreException, IOException {
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				try {
					final IFile configIFile = container.getFile(new Path(
							"/config.xml"));
					Config config;
					config = Config.loadXml(new FileInputStream(configIFile
							.getLocation().toOSString()));
					if (page.getNameText() != null) {
						config.setDesc(page.getNameText());
					}
					config.setName(page.getProjectName());
					config.setAuthorEmail(userName == null ? "test" : userName);
					config.setAuthorName(uname == null ? "Author" : uname);
					config.setId(appId == null ? "A"
							+ System.currentTimeMillis() : appId);
					Config.saveXml(config, new File(configIFile.getLocation()
							.toOSString()));
					container.refreshLocal(IContainer.DEPTH_INFINITE, monitor);
				} catch (IOException e) {
					e.printStackTrace();
				} catch (CoreException e) {
					e.printStackTrace();
				}
			}
		});

	}

	private boolean validate(String name) {
		try {
			String message=com.apicloud.navigator.Activator.network_instance.validateUser(name, page.getNameText(), page.isSafed(), userName, userPassWord, cookie,ip);
			
			JSONObject json;
			json = new JSONObject(message);
			String status = json.getString("status");
			if (status.equals("0")) {
				String errorStr = json.getString("msg");
				MessageDialog.openError(this.getShell(), "fail", errorStr);
				return false;

			} else {
				String body = json.getString("result");
				JSONObject result = new JSONObject(body);
				appId = result.getString("appId");
				svnUrl = result.getString("svn");
				System.err.println(svnUrl);
				uname = result.getString("uname");
				if (svnUrl.equals("") || svnUrl == null) {
					MessageDialog.openError(this.getShell(), "fail",
							"svn create fail");
					return false;
				}
				SVNUtil.addSVNToView(svnUrl);
				return true;
			}
		} catch (JSONException e1) {
			UtilActivator.logger.info("JSONException");
			e1.printStackTrace();
			return false;
		}
	}

	private void copyFile(File sourcefile, File targetFile) throws IOException {
		FileInputStream input = new FileInputStream(sourcefile);
		BufferedInputStream inbuff = new BufferedInputStream(input);
		FileOutputStream out = new FileOutputStream(targetFile);
		BufferedOutputStream outbuff = new BufferedOutputStream(out);
		byte[] b = new byte[1024];
		int len = 0;
		while ((len = inbuff.read(b)) != -1) {
			outbuff.write(b, 0, len);
		}
		outbuff.flush();
		inbuff.close();
		outbuff.close();
		out.close();
		input.close();
	}

	private void copyDirectiory(String sourceDir, String targetDir)
			throws IOException {
		(new File(targetDir)).mkdirs();
		File[] file = (new File(sourceDir)).listFiles();
		if (file == null) {
		}
		for (int i = 0; i < file.length; i++) {
			if (file[i].isFile()) {
				File sourceFile = file[i];
				File targetFile = new File(
						new File(targetDir).getAbsolutePath() + File.separator
								+ file[i].getName());
				copyFile(sourceFile, targetFile);
			}
			if (file[i].isDirectory() && !file[i].getName().startsWith(".")) {
				String sourcePath = sourceDir + "/" + file[i].getName();
				String targetPath = targetDir + "/" + file[i].getName();
				copyDirectiory(sourcePath, targetPath);
			}
		}

	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}
}
