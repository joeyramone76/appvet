/* This software was developed by employees of the National Institute of
 * Standards and Technology (NIST), an agency of the Federal Government.
 * Pursuant to title 15 United States Code Section 105, works of NIST
 * employees are not subject to copyright protection in the United States
 * and are considered to be in the public domain.  As a result, a formal
 * license is not needed to use the software.
 * 
 * This software is provided by NIST as a service and is expressly
 * provided "AS IS".  NIST MAKES NO WARRANTY OF ANY KIND, EXPRESS, IMPLIED
 * OR STATUTORY, INCLUDING, WITHOUT LIMITATION, THE IMPLIED WARRANTY OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE, NON-INFRINGEMENT
 * AND DATA ACCURACY.  NIST does not warrant or make any representations
 * regarding the use of the software or the results thereof including, but
 * not limited to, the correctness, accuracy, reliability or usefulness of
 * the software.
 * 
 * Permission to use this software is contingent upon your acceptance
 * of the terms of this agreement.
 */
package gov.nist.appvet.shared.app;

import gov.nist.appvet.properties.AppVetProperties;
import gov.nist.appvet.shared.Database;
import gov.nist.appvet.shared.Logger;

import java.io.File;

import org.apache.commons.fileupload.FileItem;


public class AppInfo extends AppInfoBase {

	public static final long serialVersionUID = 1L;
	private static final Logger appvetLog = AppVetProperties.log;
	public String sessionId = null;
	public FileItem fileItem = null;
	public String toolId = null;	// Used only for submitting tool report
	public String toolRisk = null;  // Used only for submitting tool risk
	public String projectName = null;
	public String idPath = null;
	public Logger log = null;

	// Use only for retrieving existing apps.
	public AppInfo(String appId) {
		this.appId = appId;
		log = new Logger(getLogPath(appId));
		userName = Database.getOwner(appId);
		fileName = Database.getAppFileName(appId);
		clientHost = Database.getClientIPAddress(appId);
		projectName = Database.getAppName(appId);
		String fileNameUpperCase = fileName.toUpperCase();
		if (fileNameUpperCase.endsWith("APK")) {
			final int extensionIndex = fileNameUpperCase.indexOf(".APK");
			if (extensionIndex > 0) {
				projectName = fileName.substring(0, extensionIndex);
			}
		}
	}

	//Use only for registering new apps and reports.
	public AppInfo(String appId, boolean createAppDirectories) {
		this.appId = appId;
		final String reportsDir = AppVetProperties.APPS_ROOT + "/" + appId
				+ "/reports";
		if (createAppDirectories) {
			if (!new File(reportsDir).mkdirs()) {
				appvetLog.error("Could not create reports directory "
						+ reportsDir);
				return;
			}
			final File imagesDir = new File(AppVetProperties.APP_IMAGES);
			if (!imagesDir.exists()) {
				if (!imagesDir.mkdirs()) {
					appvetLog.error("Could not create images directory "
							+ imagesDir);
				}
			}
		}
		log = new Logger(getLogPath(appId));
	}

	private String getLogPath(String appid) {
		return AppVetProperties.APPS_ROOT + "/" + appid
				+ "/reports/" + AppVetProperties.APP_LOG_NAME;
	}

	public String getIdPath() {
		return AppVetProperties.APPS_ROOT + "/" + appId;
	}

	public String getProjectName() {
		return projectName;
	}

	public String getProjectPath() {
		return AppVetProperties.APPS_ROOT + "/" + appId + "/" + projectName;
	}

	public String getReportsPath() {
		return AppVetProperties.APPS_ROOT + "/" + appId + "/reports";
	}

	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}
}
