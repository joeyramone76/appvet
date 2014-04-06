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
import gov.nist.appvet.shared.status.ToolStatus;
import gov.nist.appvet.shared.status.ToolStatusManager;
import gov.nist.appvet.toolmgr.ToolServiceAdapter;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

/**
 * $$Id: GWTServiceImpl.java 38554 2014-03-30 16:06:12Z steveq $$
 */
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
				log.info("Added user " + userInfo.getFullName());
			} else {
				log.error("Could not add user " + userInfo.getFullName());
			}
		} else {
			if (Database.updateUser(userInfo)) {
				log.info("Updated user " + userInfo.getFullName());
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
			log.info(username + " logged into GWT from: "
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
		configInfo.setAppVetBuild(AppVetProperties.BUILD);
		configInfo.setLastUpdated(AppVetProperties.LAST_UPDATED);
		configInfo.setSessionId(sessionId);
		configInfo.setMaxIdleTime(AppVetProperties.MAX_SESSION_IDLE_DURATION);
		configInfo.setGetUpdatesDelay(AppVetProperties.GET_UPDATES_DELAY);
		configInfo.setSessionExpirationLong(sessionExpiration);
		configInfo.setSystemMessage(AppVetProperties.STATUS_MESSAGE);
		final ArrayList<ToolServiceAdapter> availableTools = AppVetProperties.availableTools;
		if ((availableTools != null) && !availableTools.isEmpty()) {
			final ArrayList<String> availableToolNames = new ArrayList<String>();
			final ArrayList<String> availableToolIDs = new ArrayList<String>();
			final ArrayList<String> availableToolReportFileTypes = new ArrayList<String>();
			for (int i = 0; i < availableTools.size(); i++) {
				final ToolServiceAdapter tool = availableTools.get(i);
				if ((tool != null)
						&& ((tool.id != null) & !tool.id.equals("registration"))
						&& !tool.id.equals("appinfo") && !tool.id.isEmpty()) {
					availableToolNames.add(tool.name);
					availableToolIDs.add(tool.id);
					availableToolReportFileTypes.add(tool.reportFileType.name());
				}
			}
			String[] toolNames = new String[availableToolNames.size()];
			toolNames = availableToolNames.toArray(toolNames);
			configInfo.setAvailableToolNames(toolNames);
			String[] toolIDs = new String[availableToolIDs.size()];
			toolIDs = availableToolIDs.toArray(toolIDs);
			configInfo.setAvailableToolIDs(toolIDs);
			String[] toolReportFileTypes = new String[availableToolReportFileTypes
			                                      .size()];
			toolReportFileTypes = availableToolReportFileTypes.toArray(toolReportFileTypes);
			configInfo.setAvailableToolsType(toolReportFileTypes);
		} else {
			log.error("GWT DataProvider cannot read "
					+ "tool names from AppVetProperties");
			return null;
		}
		return configInfo;
	}

	@Override
	public Boolean deleteApp(String appid, String username)
			throws IllegalArgumentException {
		// TODO: Deleting an app will not be immediately reflected to other
		// users until a new AppVet session is started. A better approach is to
		// simply update the app's status to "DELETED" and update users'
		// display.
		final boolean deletedDbEntries = Database.deleteApp(appid);
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
			log.info(username + " invoked DELETE APP on app "
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
	public List<ToolStatusGwt> getToolResults(String sessionId, String appId)
			throws IllegalArgumentException {
		return getToolsStatuses(sessionId, appId);
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

	public static List<ToolStatusGwt> getToolsStatuses(String sessionId,
			String appId) {
		final ArrayList<ToolStatusGwt> toolStatusList = new ArrayList<ToolStatusGwt>();
		ToolServiceAdapter tool = ToolServiceAdapter.getById("registration");
		ToolStatusGwt toolStatus = getToolStatusHtml(sessionId, appId, tool);
		if (toolStatus != null) {
			toolStatusList.add(toolStatus);
		}
		tool = ToolServiceAdapter.getById("appinfo");
		toolStatus = getToolStatusHtml(sessionId, appId, tool);
		if (toolStatus != null) {
			toolStatusList.add(toolStatus);
		}

		final ArrayList<ToolServiceAdapter> tools = AppVetProperties.availableTools;
		for (int i = 0; i < tools.size(); i++) {
			tool = tools.get(i);
			if (!tool.id.equals("registration") && !tool.id.equals("appinfo")
					&& !tool.id.equals("override")) {
				toolStatus = getToolStatusHtml(sessionId, appId, tool);
				if (toolStatus != null) {
					toolStatusList.add(toolStatus);
				}
			}
		}
		return toolStatusList;
	}

	private static ToolStatusGwt getToolStatusHtml(String sessionId, String appId,
			ToolServiceAdapter tool) {
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
				ToolStatusManager.getToolStatus(appId, tool.id);
		if (toolStatus == null) {
			log.warn(appId + ", " + tool.id + "-status: null!");
		}
		//---------------------- Compute Tool Status ---------------------------
		if (toolStatus == null) {
			// Status for a tool may be null if it was recently installed
			// but not run for previously submitted apps. In such cases,
			// we return an NA status.
			ToolStatusManager.setToolStatus(appId, tool.id, ToolStatus.NA);
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
						+ AppVetProperties.SERVLET_URL + dateString
						+ "&command=GET_TOOL_REPORT&appid=" + appId
						+ "&sessionid=" + sessionId + "&report="
						+ tool.reportName + "\" target=\"_blank\">Results</a>");
			} else {
				String otherReportName;
				if (tool.reportName.indexOf("override_security_report") > -1) {
					otherReportName = "override_security_report.rtf";
				} else {
					// See if an old TXT version of the report exists
					otherReportName = tool.id + "_security_report" + ".txt";
				}
				reportFile = new File(reportsPath + "/" + otherReportName);
				if (reportFile.exists()) {
					toolStatusGwt.setReport("<a href=\""
							+ AppVetProperties.SERVLET_URL + dateString
							+ "&command=GET_TOOL_REPORT&appid=" + appId
							+ "&sessionid=" + sessionId + "&report="
							+ otherReportName
							+ "\" target=\"_blank\">Results</a>");
				} else {
					toolStatusGwt
					.setReport("<div id=\"tabledim\" style='color: gray'>Unavailable</div>");
				}
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
