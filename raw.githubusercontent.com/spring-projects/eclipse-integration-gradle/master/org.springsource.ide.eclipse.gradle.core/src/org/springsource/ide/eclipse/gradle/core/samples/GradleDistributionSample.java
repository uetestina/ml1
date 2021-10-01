/*******************************************************************************
 * Copyright (c) 2012 Pivotal Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.springsource.ide.eclipse.gradle.core.samples;

import java.io.File;
import java.net.URI;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.springsource.ide.eclipse.gradle.core.util.DownloadManager;
import org.springsource.ide.eclipse.gradle.core.util.DownloadManager.DownloadRequestor;
import org.springsource.ide.eclipse.gradle.core.util.ExceptionUtil;
import org.springsource.ide.eclipse.gradle.core.util.ZipFileUtil;


/**
 * A sample project from an official Gradle distribution. The project contents is generated by
 * copying contents from a gradle distribution zip.
 * 
 * @author Kris De Volder
 */
public class GradleDistributionSample extends SampleProject {

	private static final String DISTRO_SUFFIX = "-all.zip";
	private DownloadManager downloadManager;
	private String locationInZip;
	private URI distribution;
	private String name;

	/**
	 * @param distribution The URI of an official Gradle distribution.
	 * @param location The location of the sample inside the distribution zip.
	 */
	public GradleDistributionSample(DownloadManager downloadManager, URI distribution, String name, String location) {
		String zipName = new Path(distribution.getPath()).lastSegment();
		Assert.isLegal(zipName.endsWith(DISTRO_SUFFIX), "Distribution for sample project needs to be a '"+DISTRO_SUFFIX+"'");
		String distroName = zipName.substring(0, zipName.length()-DISTRO_SUFFIX.length());
		this.downloadManager = downloadManager;
		this.distribution = distribution;
		this.locationInZip = distroName+"/"+location;
		this.name = name;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void createAt(final File location) throws CoreException {
		try {
			downloadManager.doWithDownload(distribution, new DownloadRequestor() {
				public void exec(File downloadedFile) throws Exception {
					ZipFileUtil.unzip(downloadedFile.toURI().toURL(), location, locationInZip);
				}
			});
		} catch (Exception e) {
			throw ExceptionUtil.coreException(e);
		}
	}

}