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
package gov.nist.appvet.toolmgr;

import gov.nist.appvet.properties.AppVetProperties;
import gov.nist.appvet.shared.Database;
import gov.nist.appvet.shared.FileUtil;
import gov.nist.appvet.shared.Logger;
import gov.nist.appvet.shared.MemoryUtil;
import gov.nist.appvet.shared.app.AppInfo;
import gov.nist.appvet.shared.status.AppStatus;
import gov.nist.appvet.shared.status.AppStatusManager;
import gov.nist.appvet.shared.status.ToolStatus;
import gov.nist.appvet.shared.status.ToolStatusManager;
import gov.nist.appvet.shared.validate.ValidateBase;
import gov.nist.appvet.tools.preprocessor.AndroidManifest;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;


public class ToolMgr implements Runnable {

	private static final Logger log = AppVetProperties.log;

	public static void staggerStart(int maxStagger) {
		final Random generator = new Random();
		final int i = maxStagger - generator.nextInt(maxStagger);
		try {
			Thread.sleep(i);
		} catch (final InterruptedException e) {
			log.error(e.getMessage());
		}
	}

	public ToolMgr() {
		log.info("*** Starting AppVet Tool Manager "
				+ AppVetProperties.VERSION + " on " + AppVetProperties.URL);
	}

	public void cleanUpFiles(AppInfo appInfo) {
		final String projectPath = appInfo.getIdPath() + "/"
				+ appInfo.getProjectName();
		final File projectDirectory = new File(projectPath);
		if (projectDirectory.exists()) {
			FileUtil.deleteDirectory(projectDirectory);
		} else {
			appInfo.log.warn("Directory " + projectPath
					+ " does not exist to be cleaned");
		}
		if (AppVetProperties.KEEP_APPS == false) {
			// Delete app file
			String appPath = appInfo.getIdPath() + "/" + appInfo.fileName;
			FileUtil.deleteFile(appPath);
		}
		appInfo.log.close();
		System.gc();
	}

	private synchronized boolean getAppMetaData(AppInfo appInfo) {
		log.debug("Start AndroidManifest preprocessing for appID="
				+ appInfo.appId);
		final ToolServiceAdapter appinfoTool = ToolServiceAdapter
				.getById("appinfo");
		final String reportsPath = appInfo.getReportsPath();
		final String appinfoReportPath = reportsPath + "/"
				+ appinfoTool.reportName;
		BufferedWriter appinfoReport = null;
		final String apkFilePath = appInfo.getIdPath() + "/" + appInfo.fileName;
		try {
			appinfoReport = new BufferedWriter(
					new FileWriter(appinfoReportPath));
			appinfoReport.write("<HTML>\n");
			appinfoReport.write("<head>\n");
			appinfoReport.write("<style type=\"text/css\">\n");
			appinfoReport.write("h3 {font-family:arial;}\n");
			appinfoReport.write("p {font-family:arial;}\n");
			appinfoReport.write("</style>\n");
			appinfoReport.write("</head>\n");
			appinfoReport.write("<body>\n");
			appinfoReport.write("<h3>AndroidManifest Pre-Processing Report</h3>\n");
			appinfoReport.write("<HR>\n");
			appinfoReport.write("<pre>\n");
			final Date date = new Date();
			final SimpleDateFormat format = new SimpleDateFormat(
					"yyyy-MM-dd' 'HH:mm:ss.SSSZ");
			final String currentDate = format.format(date);
			appinfoReport.write("Date: \t\t" + currentDate + "\n\n");
			appinfoReport.write("App ID: \t" + appInfo.appId + "\n");
			appinfoReport.write("File: \t\t" + appInfo.fileName + "\n");
			if (ValidateBase.hasValidAppFileExtension(appInfo.fileName)) {
				ToolStatusManager.setToolStatus(appInfo.appId, appinfoTool.id, ToolStatus.SUBMITTED);
				if (!AndroidManifest.decodeApk(appInfo, apkFilePath, appinfoReport)) {
					return false;
				}
				if (!AndroidManifest.getManifestInfo(appInfo, appinfoReport, true)) {
					return false;
				}
			} else {
				appInfo.log.error("File " + appInfo.fileName + " is invalid");
				ToolStatusManager.setToolStatus(appInfo.appId, appinfoTool.id, ToolStatus.ERROR);
				return false;
			}
			
/*			// Verify certificate
			if (!AndroidManifest.validCert(appInfo, appinfoReport)) {
				foundWarning = true;
			}

			// Verify SDK
			if (!AndroidManifest.validMinSDK(appInfo, appinfoReport)) {
				foundWarning = true;
				appinfoReport.write("\nWARNING: Invalid min SDK\n");
			}

			if (!AndroidManifest.validTargetSDK(appInfo, appinfoReport)) {
				foundWarning = true;
				appinfoReport.write("\nWARNING: Invalid target SDK\n");
			}

			// Final app status
			if (foundWarning) {
				ToolStatusManager.setToolStatus(appInfo.appId, appinfoTool.id, ToolStatus.WARNING);
			} else {
				ToolStatusManager.setToolStatus(appInfo.appId, appinfoTool.id, ToolStatus.PASS);
				appinfoReport.write("\n<hr>\n");
				appinfoReport
				.write("Status\t\t<font color=\"green\">PASS</font>\n");
			}*/
			
			ToolStatusManager.setToolStatus(appInfo.appId, appinfoTool.id, ToolStatus.PASS);
			appinfoReport.write("\n<hr>\n");
			appinfoReport
			.write("Status\t\t<font color=\"green\">PASS</font>\n");
			log.debug("End AndroidManifest preprocessing for appID="
					+ appInfo.appId);
			return true;
		} catch (final IOException e) {
			appInfo.log.error(e.getMessage());
			return false;
		} finally {
			try {
				if (appinfoReport != null) {
					appinfoReport.write("</pre>\n");
					appinfoReport.write("</body>\n");
					appinfoReport.write("</HTML>\n");
					appinfoReport.close();
					appinfoReport = null;
				}
			} catch (final IOException e) {
				appInfo.log.error(e.getMessage());
			}
		}
	}

	@Override
	public void run() {
		mainLoop: for (;;) {
			AppInfo appInfo = null;
			final String appid = Database.getNextApp(AppStatus.PENDING);
			if (appid != null) {
				final long startTime = new Date().getTime();
				log.debug(MemoryUtil.getFreeHeap("ToolMgr.run()"));
				appInfo = new AppInfo(appid);
				if (!getAppMetaData(appInfo)) {
					cleanUpFiles(appInfo);
					continue mainLoop;
				}
				delay();

				// Run available tools
				ArrayList<ToolServiceAdapter> availableTools = 
						AppVetProperties.availableTools;
				for (int i = 0; i < availableTools.size(); i++) {
					staggerStart(AppVetProperties.TOOL_MGR_STAGGER_INTERVAL);
					final ToolServiceAdapter tool = availableTools.get(i);
					if (!tool.id.equals("registration")
							&& !tool.id.equals("appinfo")) {
						log.debug("App " + appInfo.appId
								+ " starting " + tool.name);
						tool.setApp(appInfo);
						final Thread thread = tool.getThread();
						thread.start();
					}
				}

				// Wait for tools to complete
				for (int i = 0; i < availableTools.size(); i++) {
					final ToolServiceAdapter tool = availableTools.get(i);
					if (!tool.id.equals("registration")
							&& !tool.id.equals("appinfo")) {
						log.debug("App " + appInfo.appId
								+ " waiting for " + tool.name);
						wait(appInfo, tool);
					}
				}

				// Clean up all tools
				for (int i = 0; i < availableTools.size(); i++) {
					final ToolServiceAdapter tool = availableTools.get(i);
					if (!tool.id.equals("registration")
							&& !tool.id.equals("appinfo")) {
						log.debug("App " + appInfo.appId
								+ " closing " + tool.name);
						tool.cleanUp(appInfo, true);
					}
				}
				delay();

				// In rare cases, an app might still be in the PROCESSING
				// status at this point. If so, indicate this error by
				// changing app status to FAIL.
				AppStatus appStatus = AppStatusManager.getAppStatus(appInfo.appId);
				if (appStatus == AppStatus.PROCESSING) {
					log.error("App still PROCESSING after tools shut down."
							+ " Setting app status to ERROR");
					AppStatusManager.setAppStatus(appInfo.appId, AppStatus.ERROR);
				}

				// Post-process
				final long endTime = new Date().getTime();
				final long elapsedTime = endTime - startTime;
				appStatus = AppStatusManager.getAppStatus(appid);
				if (appStatus == AppStatus.ERROR) {
					appInfo.log.info("App status: ERROR");
				} else {
					appInfo.log.info("App status: " + appStatus.name());
				}
				appInfo.log.info("Total elapsed: "
						+ Logger.formatElapsed(elapsedTime));
				final String endTests = "END TESTS for app #"
						+ appInfo.appId;
				log.debug(endTests);
				appInfo.log.info(endTests);
				log.debug(MemoryUtil
						.getFreeHeap("End ToolMgr.run()"));
				availableTools = null;
				cleanUpFiles(appInfo);
				Database.setLastUpdate(appid);
			}
			delay();
		}
	}

	// Balance heavy loads
	public void delay() {
		try {
			Thread.sleep(AppVetProperties.TOOL_MGR_POLLING_INTERVAL);
		} catch (final InterruptedException e) {
			log.error(e.getMessage());
		}
	}

	public void wait(AppInfo appInfo, ToolServiceAdapter tool) {
		try {
			tool.thread.join(AppVetProperties.TOOL_TIMEOUT);
		} catch (final InterruptedException e) {
			appInfo.log.error("Tool timed out after "
					+ AppVetProperties.TOOL_TIMEOUT + "ms");
			ToolStatusManager.setToolStatus(appInfo.appId, tool.id, ToolStatus.ERROR);
		}
	}
}