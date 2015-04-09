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
import gov.nist.appvet.shared.os.DeviceOS;
import gov.nist.appvet.shared.status.AppStatus;
import gov.nist.appvet.shared.status.AppStatusManager;
import gov.nist.appvet.shared.status.ToolStatus;
import gov.nist.appvet.shared.status.ToolStatusManager;
import gov.nist.appvet.tools.preprocessor.AndroidMetadata;
import gov.nist.appvet.tools.preprocessor.IOSMetadata;

import java.io.File;
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
		final String projectPath = appInfo.getProjectPath();
		final File projectDirectory = new File(projectPath);
		if (projectDirectory.exists()) {
			FileUtil.deleteDirectory(projectDirectory);
		} else {
			appInfo.log.warn("Directory " + projectPath
					+ " does not exist to be cleaned");
		}
		if (AppVetProperties.KEEP_APPS == false) {
			// Delete app file
			String appFilePath = appInfo.getAppFilePath();
			FileUtil.deleteFile(appFilePath);
		}
		appInfo.log.close();
		System.gc();
	}
	

	private synchronized boolean getAppMetaData(AppInfo appInfo) {
		if (appInfo.os == DeviceOS.ANDROID) {
			return AndroidMetadata.getAndroidMetaData(appInfo, log);
		} else if (appInfo.os == DeviceOS.IOS) {
			return IOSMetadata.getIosMetaData(appInfo, log);
		} else {
			log.error("Unknown OS");
			return false;
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
				ArrayList<ToolServiceAdapter> availableTools = null;
				if (appInfo.os == DeviceOS.ANDROID) {
					availableTools = AppVetProperties.androidTools;
				} else if (appInfo.os == DeviceOS.IOS) {
					availableTools = AppVetProperties.iosTools;
				}
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

				// Note here that the app may still have a PROCESSING
				// status if it is still waiting for a result from an
				// asynchronous service. However, all the tools for this app
				// have completed at this point. The PROCESSING status will
				// change once the report for the asynchronous service(s)
				// are received.

				// Post-process
				final long endTime = new Date().getTime();
				final long elapsedTime = endTime - startTime;
				AppStatus appStatus = AppStatusManager.getAppStatus(appid);
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
			ToolStatusManager.setToolStatus(appInfo.os, appInfo.appId, tool.id, ToolStatus.ERROR);
		}
	}
}