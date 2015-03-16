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
package gov.nist.appvet.gwt.server;

import gov.nist.appvet.gwt.client.GWTService;
import gov.nist.appvet.gwt.shared.AppInfoGwt;
import gov.nist.appvet.gwt.shared.ConfigInfoGwt;
import gov.nist.appvet.gwt.shared.ToolStatusGwt;
import gov.nist.appvet.gwt.shared.UserInfoGwt;
import gov.nist.appvet.properties.AppVetProperties;
import gov.nist.appvet.shared.Authenticate;
import gov.nist.appvet.shared.Database;
import gov.nist.appvet.shared.FileUtil;
import gov.nist.appvet.shared.Logger;
import gov.nist.appvet.shared.appvetparameters.AppVetParameter;
import gov.nist.appvet.shared.os.DeviceOS;
import gov.nist.appvet.shared.servletcommands.AppVetServletCommand;
import gov.nist.appvet.shared.status.ToolStatus;
import gov.nist.appvet.shared.status.ToolStatusManager;
import gov.nist.appvet.toolmgr.ToolServiceAdapter;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;


public class GWTServiceImpl extends RemoteServiceServlet implements GWTService {

	private static final long serialVersionUID = 1L;
	private static final Logger log = AppVetProperties.log;

	static {
		log.info("*** Starting GWT Service "
				+ AppVetProperties.VERSION + " on " + AppVetProperties.URL);
	}

	@Override
	public List<UserInfoGwt> adminSetUser(UserInfoGwt userInfo)
			throws IllegalArgumentException {
		if (userInfo.isNewUser()) {
			if (Database.adminAddNewUser(userInfo)) {
				log.debug("Added user " + userInfo.getFullName());
			} else {
				log.error("Could not add user " + userInfo.getFullName());
			}
		} else {
			if (Database.updateUser(userInfo)) {
				log.debug("Updated user " + userInfo.getFullName());
			} else {
				log.error("Could not update user " + userInfo.getFullName());
			}
		}
		return getUsersList();
	}

	@Override
	public ConfigInfoGwt authenticate(String username, String password)
			throws IllegalArgumentException {
		String user = "SELECT * FROM users " + "where username='" + username + "'";
		if (!Database.exists(user)) {
			log.warn("User " + username + " not found");
			return null;
		}
		final String clientIpAddress = getThreadLocalRequest().getRemoteAddr();
		if (Authenticate.isAuthenticated(username, password)) {
			Database.updateClientHost(username, clientIpAddress);
			log.debug(username + " logged into GWT from: "
					+ clientIpAddress);
			final String sessionId = Database.setSession(username,
					clientIpAddress);
			final long sessionExpiration = Database.getSessionExpiration(
					sessionId, clientIpAddress);
			return getConfigInfo(username, sessionId,
					sessionExpiration);
		} else {
			AppVetProperties.log.error("Could not authenticate user: " + username);
			return null;
		}
	}

	private static ConfigInfoGwt getConfigInfo(String username, String sessionId,
			long sessionExpiration) {
		final ConfigInfoGwt configInfo = new ConfigInfoGwt();
		final UserInfoGwt userInfo = Database.getUser(username);
		if (userInfo == null) {
			log.error("GWT DataProvider could not get user information");
			return null;
		}
		configInfo.setUserInfo(userInfo);
		configInfo.setAppVetHostUrl(AppVetProperties.HOST_URL);
		configInfo.setAppVetUrl(AppVetProperties.URL);
		configInfo.setAppVetServletUrl(AppVetProperties.SERVLET_URL);
		configInfo.setAppVetVersion(AppVetProperties.VERSION);
		configInfo.setSessionId(sessionId);
		configInfo.setMaxIdleTime(AppVetProperties.MAX_SESSION_IDLE_DURATION);
		configInfo.setGetUpdatesDelay(AppVetProperties.GET_UPDATES_DELAY);
		configInfo.setSessionExpirationLong(sessionExpiration);
		configInfo.setSystemMessage(AppVetProperties.STATUS_MESSAGE);
		
		// Get Android tools
		final ArrayList<ToolServiceAdapter> androidTools = AppVetProperties.androidTools;
		if ((androidTools != null) && !androidTools.isEmpty()) {
			final ArrayList<String> androidToolNames = new ArrayList<String>();
			final ArrayList<String> androidToolIDs = new ArrayList<String>();
			final ArrayList<String> androidToolReportFileTypes = new ArrayList<String>();
			for (int i = 0; i < androidTools.size(); i++) {
				final ToolServiceAdapter androidTool = androidTools.get(i);
				if ((androidTool != null)
						&& ((androidTool.id != null) & !androidTool.id.equals("registration"))
						&& !androidTool.id.equals("appinfo") && !androidTool.id.isEmpty()) {
					androidToolNames.add(androidTool.name);
					androidToolIDs.add(androidTool.id);
					androidToolReportFileTypes.add(androidTool.reportFileType.name());
				}
			}
			String[] toolNames = new String[androidToolNames.size()];
			toolNames = androidToolNames.toArray(toolNames);
			configInfo.setAndroidToolNames(toolNames);
			String[] toolIDs = new String[androidToolIDs.size()];
			toolIDs = androidToolIDs.toArray(toolIDs);
			configInfo.setAndroidToolIDs(toolIDs);
			String[] toolReportFileTypes = new String[androidToolReportFileTypes
			                                      .size()];
			toolReportFileTypes = androidToolReportFileTypes.toArray(toolReportFileTypes);
			configInfo.setAndroidToolType(toolReportFileTypes);
		} else {
			log.error("GWT DataProvider cannot read "
					+ "Android tool names from AppVetProperties. Tools must include "
					+ " at least 'appinfo'"
					+ " and 'registration' tools.");
			return null;
		}
		
		// Get iOS tools
		final ArrayList<ToolServiceAdapter> iosTools = AppVetProperties.iosTools;
		if ((iosTools != null) && !iosTools.isEmpty()) {
			final ArrayList<String> iosToolNames = new ArrayList<String>();
			final ArrayList<String> iosToolIDs = new ArrayList<String>();
			final ArrayList<String> iosToolReportFileTypes = new ArrayList<String>();
			for (int i = 0; i < iosTools.size(); i++) {
				final ToolServiceAdapter iosTool = iosTools.get(i);
				if ((iosTool != null)
						&& ((iosTool.id != null) & !iosTool.id.equals("registration"))
						&& !iosTool.id.equals("appinfo") && !iosTool.id.isEmpty()) {
					iosToolNames.add(iosTool.name);
					iosToolIDs.add(iosTool.id);
					iosToolReportFileTypes.add(iosTool.reportFileType.name());
				}
			}
			String[] toolNames = new String[iosToolNames.size()];
			toolNames = iosToolNames.toArray(toolNames);
			configInfo.setiOSToolNames(toolNames);
			String[] toolIDs = new String[iosToolIDs.size()];
			toolIDs = iosToolIDs.toArray(toolIDs);
			configInfo.setiOSToolIDs(toolIDs);
			String[] toolReportFileTypes = new String[iosToolReportFileTypes
			                                      .size()];
			toolReportFileTypes = iosToolReportFileTypes.toArray(toolReportFileTypes);
			configInfo.setiOSToolTypes(toolReportFileTypes);
		} else {
			log.error("GWT DataProvider cannot read "
					+ "iOS tool names from AppVetProperties. Tools must include "
					+ " at least 'appinfo'"
					+ " and 'registration' tools.");
			return null;
		}
		
		return configInfo;
	}

	@Override
	public Boolean deleteApp(DeviceOS os, String appid, String username)
			throws IllegalArgumentException {
		// TODO: Deleting an app will not be immediately reflected to other
		// users until a new AppVet session is started. A better approach is to
		// simply update the app's status to "DELETED" and update users'
		// display.
		final boolean deletedDbEntries = Database.deleteApp(os, appid);
		if (deletedDbEntries) {
			final String appIdPath = AppVetProperties.APPS_ROOT + "/" + appid;
			final File appDirectory = new File(appIdPath);
			FileUtil.deleteDirectory(appDirectory);
			final String iconPath = AppVetProperties.APP_IMAGES + "/" + appid
					+ ".png";
			File iconFile = new File(iconPath);
			if (iconFile.exists()) {
				iconFile.delete();
			}
			iconFile = null;
			log.debug(username + " invoked DELETE APP on app "
					+ appid);
			return true;
		} else {
			return false;
		}
	}

	@Override
	public Boolean deleteUser(String username) throws IllegalArgumentException {
		final boolean deletedUser = Database.deleteUser(username);
		if (deletedUser) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	public List<AppInfoGwt> getAllApps(String username)
			throws IllegalArgumentException {
		return Database.getAllApps(username);
	}

	@Override
	public List<ToolStatusGwt> getToolsResults(DeviceOS os, String sessionId, String appId)
			throws IllegalArgumentException {
		return getToolsStatuses(os, sessionId, appId);
	}

	@Override
	public List<AppInfoGwt> getUpdatedApps(long lastClientUpdate,
			String username) throws IllegalArgumentException {
		return Database.getUpdatedApps(username, lastClientUpdate);
	}

	@Override
	public List<UserInfoGwt> getUsersList() throws IllegalArgumentException {
		return Database.getUsers();
	}

	@Override
	public Boolean removeSession(String sessionId)
			throws IllegalArgumentException {
		final String clientIpAddress = getThreadLocalRequest().getRemoteAddr();
		final boolean removedSession = Database.removeSession(sessionId,
				clientIpAddress);
		Database.clearExpiredSessions();
		if (removedSession) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	public Boolean updateSessionTimeout(String sessionId, long sessionTimeout)
			throws IllegalArgumentException {
		final String clientIpAddress = getThreadLocalRequest().getRemoteAddr();
		final boolean sessionValid = Database.isValidSession(sessionId,
				clientIpAddress);
		if (!sessionValid) {
			Database.clearExpiredSessions();
			return false;
		} else {
			Database.updateSessionExpiration(sessionId, clientIpAddress,
					sessionTimeout);
			return true;
		}
	}

	public static List<ToolStatusGwt> getToolsStatuses(DeviceOS os, String sessionId,
			String appId) {
		
		
		final ArrayList<ToolStatusGwt> toolStatusList = new ArrayList<ToolStatusGwt>();
		ToolServiceAdapter tool = ToolServiceAdapter.getByToolId(os, "registration");
		ToolStatusGwt toolStatus = getToolStatusHtml(os, sessionId, appId, tool);
		if (toolStatus != null) {
			toolStatusList.add(toolStatus);
		}
		tool = ToolServiceAdapter.getByToolId(os, "appinfo");
		toolStatus = getToolStatusHtml(os, sessionId, appId, tool);
		if (toolStatus != null) {
			toolStatusList.add(toolStatus);
		}

		ArrayList<ToolServiceAdapter> tools = null;
		if (os == DeviceOS.ANDROID) {
			tools = AppVetProperties.androidTools;
		} else if (os == DeviceOS.IOS) {
			tools = AppVetProperties.iosTools;
		}
		
		for (int i = 0; i < tools.size(); i++) {
			tool = tools.get(i);
			if (!tool.id.equals("registration") && !tool.id.equals("appinfo")
					&& !tool.id.equals("override")) {
				toolStatus = getToolStatusHtml(os, sessionId, appId, tool);
				if (toolStatus != null) {
					toolStatusList.add(toolStatus);
				}
			}
		}
		return toolStatusList;
		
	}
	
	

	private static ToolStatusGwt getToolStatusHtml(DeviceOS os, 
			String sessionId, String appId, ToolServiceAdapter tool) {
		
		if (tool == null) {
			log.error("Tool is null");
			return null;
		}

		final ToolStatusGwt toolStatusGwt = new ToolStatusGwt();
		toolStatusGwt.setAppId(appId);

		String websiteHrefTag = "";
		if (tool.webSite != null) {
			websiteHrefTag = "<a href=\"" + tool.webSite
					+ "\" target=\"_blank\">" + tool.name + "</a>";
		} else {
			websiteHrefTag = tool.name;
		}
		toolStatusGwt.setTool(websiteHrefTag);
		boolean toolCompleted = false;
		final ToolStatus toolStatus = 
				ToolStatusManager.getToolStatus(os, appId, tool.id);
		if (toolStatus == null) {
			log.warn(appId + ", " + tool.id + "-status: null!");
		}
		//---------------------- Compute Tool Status ---------------------------
		if (toolStatus == null) {
			// Status for a tool may be null if it was recently installed
			// but not run for previously submitted apps. In such cases,
			// we return an NA status.
			ToolStatusManager.setToolStatus(os, appId, tool.id, ToolStatus.NA);
			toolCompleted = true;
			toolStatusGwt
			.setStatusDescription(
					"<div id=\"tabledim\" style='color: gray'>N/A</div>");
		} else if (toolStatus == ToolStatus.NA) {
			toolCompleted = true;
			toolStatusGwt
			.setStatusDescription(
					"<div id=\"tabledim\" style='color: gray'>N/A</div>");
		} else if (toolStatus == ToolStatus.FAIL) {
			toolCompleted = true;
			toolStatusGwt
			.setStatusDescription(
					"<div id=\"tableitembad\" style='color: red'>FAIL</div>");
		} else if (toolStatus == ToolStatus.ERROR) {
			toolCompleted = true;
			toolStatusGwt
			.setStatusDescription(
					"<div id=\"tableitembad\" style='color: red'>ERROR</div>");
		} else if (toolStatus == ToolStatus.WARNING) {
			toolCompleted = true;
			toolStatusGwt
			.setStatusDescription(
					"<div id=\"tableitembad\" style='color: orange'>WARNING</div>");
		} else if (toolStatus == ToolStatus.PASS) {
			toolCompleted = true;
			toolStatusGwt
			.setStatusDescription(
					"<div id=\"tableitemendorsed\" style='color: green'>PASS</div>");	
		} else if (toolStatus == ToolStatus.SUBMITTED) {
			toolStatusGwt
			.setStatusDescription(
					"<div id=\"tableitem\" style='color: black'>SUBMITTED</div>"); 
		} else {
			toolStatusGwt
			.setStatusDescription(
					"<div id=\"tableitem\" style='color: black'>"
							+ toolStatus.name() + "</div>");
		}

		//------------------------ Attach security report ----------------------
		if (toolStatus == null || toolStatus == ToolStatus.NA) {
			toolStatusGwt
			.setReport("<div id=\"tabledim\" style='color: gray'>N/A</div>");
		} else if (toolCompleted) {
			// Make sure we do not cache this report since any new reports will
			// override this URL with the same filename. Make sure to set
			// cachingAllowed="false" in Tomcat context.xml.
			final String dateString = "?nocache" + new Date().getTime();
			final String reportsPath = AppVetProperties.APPS_ROOT + "/"
					+ appId + "/reports";
			File reportFile = new File(reportsPath + "/" + tool.reportName);
			
			if (reportFile.exists()) {
				toolStatusGwt.setReport("<a href=\""
						+ AppVetProperties.SERVLET_URL + dateString + 
						"&" + AppVetParameter.COMMAND.value + "=" + AppVetServletCommand.GET_TOOL_REPORT.name() +
						"&" + AppVetParameter.APPID.value + "=" + appId +
						"&" + AppVetParameter.SESSIONID.value + "=" + sessionId + 
						"&" + AppVetParameter.TOOLID.value + "=" + tool.id 
						+ "\" target=\"_blank\">Results</a>");
			} 
		} else {
			toolStatusGwt
			.setReport("<div id=\"tabledim\" style='color: gray'>Unavailable</div>");
		}
		return toolStatusGwt;
	}

	@Override
	public boolean updateSelf(UserInfoGwt userInfo)
			throws IllegalArgumentException {
		return Database.updateUser(userInfo);
	}
}
