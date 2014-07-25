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
package gov.nist.appvet.shared.status;

import gov.nist.appvet.properties.AppVetProperties;
import gov.nist.appvet.shared.Database;
import gov.nist.appvet.toolmgr.ToolServiceAdapter;

public class ToolStatusManager {

	private ToolStatusManager() {
	}

	private synchronized static String getToolStatusName (String appid, 
			String toolId) {
		return Database.getString("SELECT " + toolId + " FROM toolstatus " 
				+ "where appid='" + appid + "'");
	}

	public synchronized static ToolStatus getToolStatus(String appid, 
			String toolId) {
		String toolStatusName = getToolStatusName(appid, toolId);
		return ToolStatus.getStatus(toolStatusName);
	}

	// Update tool status and overall app status
	public synchronized static void setToolStatus(String appId, String toolId,
			ToolStatus toolStatus) {
		Database.update("UPDATE toolstatus SET " + toolId + "='"
				+ toolStatus.name() + "' where appid='" + appId + "'");
		Database.setLastUpdate(appId);

		computeAppStatus(appId);
	}

	private synchronized static void computeAppStatus(String appId) {
		// Registration
		ToolServiceAdapter tool = ToolServiceAdapter.getById("registration");
		ToolStatus toolStatus = getToolStatus(appId, tool.id);
		if (toolStatus == ToolStatus.ERROR) {
			AppStatusManager.setAppStatus(appId, AppStatus.ERROR);
			return;
		} else if (toolStatus == ToolStatus.PASS) {
			final AppStatus appStatus = AppStatusManager.getAppStatus(appId);
			if (appStatus == AppStatus.REGISTERING) {
				AppStatusManager.setAppStatus(appId, AppStatus.PENDING);
				return;
			}
		}
		// Android Manifest
		tool = ToolServiceAdapter.getById("appinfo");
		toolStatus = getToolStatus(appId, tool.id);
		if (toolStatus == ToolStatus.ERROR) {
			AppStatusManager.setAppStatus(appId, AppStatus.ERROR);
			return;
		} else if (toolStatus == ToolStatus.FAIL) {
			AppStatusManager.setAppStatus(appId, AppStatus.FAIL);
			return;
		} else if (toolStatus == ToolStatus.SUBMITTED) {
			AppStatusManager.setAppStatus(appId, AppStatus.PROCESSING);
			return;
		} else if (toolStatus == ToolStatus.WARNING) {
			AppStatusManager.setAppStatus(appId, AppStatus.WARNING);
		} else if (toolStatus == ToolStatus.PASS){
			// Do nothing
		}

		// Count number of each tool status types
		int numToolErrors = 0;
		int numToolFails = 0;
		int numToolWarnings = 0;
		int numToolPasses = 0;
		int numToolsSubmitted = 0;
		int numTools = AppVetProperties.availableTools.size();
		for (int i = 0; i < numTools; i++) {
			tool = AppVetProperties.availableTools.get(i);
			toolStatus = getToolStatus(appId, tool.id);
			if (toolStatus == ToolStatus.ERROR) {
				numToolErrors++;
			} else if (toolStatus == ToolStatus.FAIL) {
				numToolFails++;
			} else if (toolStatus == ToolStatus.WARNING) {
				numToolWarnings++;
			} else if (toolStatus == ToolStatus.PASS) {
				numToolPasses++;
			} else if (toolStatus == ToolStatus.NA) {
				//numToolsNa++;  // App status should already be pending
			} else if (toolStatus == ToolStatus.SUBMITTED) {
				numToolsSubmitted++;
			} else if (toolStatus == ToolStatus.PASS){
				numToolPasses++;
			}
		}
		if (numToolErrors > 0) {
			AppStatusManager.setAppStatus(appId, AppStatus.ERROR);
		} else if (numToolFails > 0) {
			AppStatusManager.setAppStatus(appId, AppStatus.FAIL);
		} else if (numToolWarnings > 0) {
			AppStatusManager.setAppStatus(appId, AppStatus.WARNING);
		} else if (numToolsSubmitted > 0) {
			AppStatusManager.setAppStatus(appId, AppStatus.PROCESSING);
		} else if (numToolPasses == numTools) {
			AppStatusManager.setAppStatus(appId, AppStatus.PASS);
		} else {
			AppVetProperties.log.debug("Unknown app status in ToolStatusManager");
			// Keep current app status
		}

	}

}
