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
import gov.nist.appvet.shared.Logger;
import gov.nist.appvet.shared.analysis.AnalysisType;
import gov.nist.appvet.shared.os.DeviceOS;
import gov.nist.appvet.toolmgr.ToolServiceAdapter;

public class ToolStatusManager {
	
	private static final Logger log = AppVetProperties.log;

	private ToolStatusManager() {
	}

	// TODO: Change method call to remove os. We can compute os in this method
	// via Database.getAppOS(appId).
	public synchronized static ToolStatus getToolStatus(DeviceOS os, String appid, 
			String toolId) {
		String toolStatusName = getToolStatusName(os, appid, toolId);
		return ToolStatus.getStatus(toolStatusName);
	}
	
	private synchronized static String getToolStatusName (DeviceOS os, String appid, 
			String toolId) {
		if (os == DeviceOS.ANDROID) {
			// Android
			return Database.getString("SELECT " + toolId + " FROM androidtoolstatus " 
					+ "where appid='" + appid + "'");
		} else if (os == DeviceOS.IOS) {
			// iOS
			return Database.getString("SELECT " + toolId + " FROM iostoolstatus " 
					+ "where appid='" + appid + "'");
		} else {
			return null;
		}
	}

	// Update tool status and overall app status
	public synchronized static void setToolStatus(DeviceOS os, String appId, String toolId,
			ToolStatus toolStatus) {
		if (os == DeviceOS.ANDROID) {
			Database.update("UPDATE androidtoolstatus SET " + toolId + "='"
					+ toolStatus.name() + "' where appid='" + appId + "'");
		} else if (os == DeviceOS.IOS) {
			Database.update("UPDATE iostoolstatus SET " + toolId + "='"
					+ toolStatus.name() + "' where appid='" + appId + "'");
		}
		Database.setLastUpdate(appId);
		computeAppStatus(os, appId);
	}

	private synchronized static void computeAppStatus(DeviceOS os, String appId) {
		// If audit report is not null, use this status as the app's status
		ToolServiceAdapter tool = ToolServiceAdapter.getByToolId(os, "audit");
		log.debug("### TOOL AUDIT: " + tool.id);

		if (tool.analysisType != null) {
			ToolStatus toolStatus = getToolStatus(os, appId, tool.id);
			log.debug("### TOOL STATUS FOR AUDIT: " + toolStatus.name());
			if (toolStatus == ToolStatus.PASS) {
				AppStatusManager.setAppStatus(appId, AppStatus.PASS);
				return;
			} else if (toolStatus == ToolStatus.WARNING) {
				AppStatusManager.setAppStatus(appId, AppStatus.WARNING);
				return;
			} else if (toolStatus == ToolStatus.FAIL) {
				AppStatusManager.setAppStatus(appId, AppStatus.FAIL);
				return;
			} 
		} else {
			log.warn("Tool status for audit is null");
		}

		// Audit report has not been set, so compute app status from 
		// preprocessing and analysis tools.
		
		// Registration
		tool = ToolServiceAdapter.getByToolId(os, "registration");
		ToolStatus toolStatus = getToolStatus(os, appId, tool.id);
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
		// Manifest
		tool = ToolServiceAdapter.getByToolId(os, "appinfo");
		toolStatus = getToolStatus(os, appId, tool.id);
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
		int numTools = 0;
		if (os == DeviceOS.ANDROID) {
			// Subtract 1 since we don't count the audit report
			numTools = AppVetProperties.androidTools.size();
		} else if (os == DeviceOS.IOS) {
			// Subtract 1 since we don't count the audit report
			numTools = AppVetProperties.iosTools.size();
		}
		
		for (int i = 0; i < numTools; i++) {
			tool = null;
			if (os == DeviceOS.ANDROID) {
				tool = AppVetProperties.androidTools.get(i);
			} else if (os == DeviceOS.IOS) {
				tool = AppVetProperties.iosTools.get(i);
			}
			if (tool.analysisType != AnalysisType.AUDIT) {
				toolStatus = getToolStatus(os, appId, tool.id);
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
		}
		
		
		log.debug("@@@ numTools: " + numTools);
		log.debug("@@@ numToolErrors: " + numToolErrors);
		log.debug("@@@ numToolFails: " + numToolFails);
		log.debug("@@@ numToolWarnings: " + numToolWarnings);
		log.debug("@@@ numToolsSubmitted: " + numToolsSubmitted);
		log.debug("@@@ numToolPasses: " + numToolPasses);
		log.debug("@@@ numTools: " + numTools);
		if (numToolErrors > 0) {
			AppStatusManager.setAppStatus(appId, AppStatus.ERROR);
		} else if (numToolFails > 0) {
			AppStatusManager.setAppStatus(appId, AppStatus.FAIL);
		} else if (numToolWarnings > 0) {
			AppStatusManager.setAppStatus(appId, AppStatus.WARNING);
		} else if (numToolsSubmitted > 0) {
			AppStatusManager.setAppStatus(appId, AppStatus.PROCESSING);
		} else if (numToolPasses == numTools-1) { // Don't count audit tool
			AppStatusManager.setAppStatus(appId, AppStatus.PASS);
		} else {
			
			AppVetProperties.log.debug("Unknown app status in ToolStatusManager");
			// Keep current app status
		}

	}

}
