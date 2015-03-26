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
package gov.nist.appvet.servlet;

import gov.nist.appvet.properties.AppVetProperties;
import gov.nist.appvet.shared.Authenticate;
import gov.nist.appvet.shared.Database;
import gov.nist.appvet.shared.ErrorMessage;
import gov.nist.appvet.shared.FileUtil;
import gov.nist.appvet.shared.Logger;
import gov.nist.appvet.shared.ReportFileType;
import gov.nist.appvet.shared.Zip;
import gov.nist.appvet.shared.app.AppInfo;
import gov.nist.appvet.shared.appvetparameters.AppVetParameter;
import gov.nist.appvet.shared.os.DeviceOS;
import gov.nist.appvet.shared.servletcommands.AppVetServletCommand;
import gov.nist.appvet.shared.status.AppStatus;
import gov.nist.appvet.shared.status.AppStatusManager;
import gov.nist.appvet.shared.status.ToolStatus;
import gov.nist.appvet.shared.status.ToolStatusManager;
import gov.nist.appvet.shared.validate.ValidateBase;
import gov.nist.appvet.toolmgr.ToolServiceAdapter;
import gov.nist.appvet.toolmgr.ToolMgr;
import gov.nist.appvet.tools.preprocessor.Registration;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

public class AppVetServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private static final Logger log = AppVetProperties.log;
	private ToolMgr toolMgr = null;
	private Thread toolMgrThread = null;

	@Override
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) {
		/* Used for all GET commands. */
		String userName = request.getParameter(AppVetParameter.USERNAME.value);
		/* Used for all GET commands. */
		String password = request.getParameter(AppVetParameter.PASSWORD.value);
		/* Used for all GET commands. */
		String sessionId = request
				.getParameter(AppVetParameter.SESSIONID.value);
		/* Used for all GET commands. */
		String commandStr = request.getParameter(AppVetParameter.COMMAND.value);
		/* Used for all GET commands except GET_APPVET_LOG. */
		String appId = request.getParameter(AppVetParameter.APPID.value);
		/* Used only for GET_TOOL_REPORT command. */
		String toolId = request.getParameter(AppVetParameter.TOOLID.value);
		String clientIpAddress = request.getRemoteAddr();
		
		// GIT test from appsec

		try {
			// ------------------------- Authenticate --------------------------
			if (isAuthenticated(sessionId, userName, password, clientIpAddress,
					commandStr)) {
				if (userName == null && sessionId != null) {
					userName = Database.getSessionUser(sessionId);
				}
				log.debug("User '" + userName + "' is authenticated.");
			} else {
				log.debug("User '" + userName + "' could not be authenticated.");
				sendHttpResponse(userName, appId, commandStr, clientIpAddress,
						ErrorMessage.AUTHENTICATION_ERROR.getDescription(),
						response, HttpServletResponse.SC_BAD_REQUEST, true);
				return;
			}

			// ----------------------- Verify App ID -------------------------
			log.debug("commandStr: " + commandStr);
			
			final AppVetServletCommand command = AppVetServletCommand
					.getCommand(commandStr);

			if (appId != null && command != AppVetServletCommand.GET_APPVET_LOG) {
				/* GET_APPVET_LOG does not require an app ID. */
				boolean appExists = Database.appExists(appId);
				if (!appExists) {
					sendHttpResponse(userName, appId, commandStr,
							clientIpAddress,
							ErrorMessage.INVALID_APPID.getDescription(),
							response, HttpServletResponse.SC_BAD_REQUEST, true);
				}
			}

			// ----------------------- Handle command -------------------------

			switch (command) {

			case GET_STATUS:
				/*
				 * Get the current processing status of the app. Used only by
				 * non-GUI clients. GUI clients get status via GWT RPC.
				 */
				log.debug(userName + " invoked " + command.name() + " on app "
						+ appId);
				final AppStatus currentStatus = AppStatusManager
						.getAppStatus(appId);
				sendHttpResponse(userName, appId, command.name(),
						clientIpAddress,
						"CURRENT_STATUS=" + currentStatus.name(), response,
						HttpServletResponse.SC_OK, false);
				break;
			case GET_ALL_TOOL_IDS:
				/* Get a list of tools associated with an app. */
				log.debug(userName + " invoked " + command.name());
				returnAllToolIDs(response, clientIpAddress);
				break;
			case GET_APP_TOOL_IDS:
				/* Get a list of tools associated with an app. */
				log.debug(userName + " invoked " + command.name() + " on app "
						+ appId);
				returnAppToolIDs(response, appId, clientIpAddress);
				break;
			case GET_TOOL_REPORT:
				/*
				 * Get the tool report of the selected app. Used by GUI and
				 * non-GUI clients.
				 */
				log.debug(userName + " invoked " + command.name() + " of "
						+ toolId + " report on app " + appId);
				returnReport(response, appId, toolId, clientIpAddress);
				break;
			case GET_APP_LOG:
				/*
				 * Get the log of the selected app. Used by GUI and non-GUI
				 * clients.
				 */
				log.debug(userName + " invoked " + command.name() + " on app "
						+ appId);
				returnAppLog(response, appId, clientIpAddress);
				break;
			case GET_APPVET_LOG:
				/* Get the main AppVet log. Used by GUI and non-GUI clients. */
				log.debug(userName + " invoked " + command.name());
				returnAppVetLog(response, clientIpAddress);
				break;
			case DOWNLOAD_REPORTS:
				/*
				 * Download reports for the selected app. Reports can only be
				 * downloaded if the app has completed processing. Used by GUI
				 * and non-GUI clients.
				 */
				log.debug(userName + " invoked " + command.name() + " on "
						+ "app " + appId);
				final AppStatus appStatus = AppStatusManager
						.getAppStatus(appId);
				if (appStatus != null) {
					if (appStatus == AppStatus.ERROR
							|| appStatus == AppStatus.FAIL
							|| appStatus == AppStatus.WARNING
							|| appStatus == AppStatus.PASS) {
						downloadReports(response, appId, sessionId,
								clientIpAddress);
					} else {
						sendHttpResponse(userName, appId, command.name(),
								clientIpAddress, "App " + appId
										+ " has not finished processing",
								response, HttpServletResponse.SC_BAD_REQUEST,
								true);
					}
				} else {
					log.warn("Null appstatus in doGet()");
				}
				break;
			default:
				log.warn("Received unknown command: " + commandStr
						+ " from IP: " + clientIpAddress);
			}
		} finally {
			userName = null;
			password = null;
			sessionId = null;
			commandStr = null;
			appId = null;
			clientIpAddress = null;
			System.gc();
		}
	}

	@Override
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) {

		AppVetServletCommand command = null;
		String commandStr = null;
		String userName = null;
		String password = null;
		String sessionId = null;
		String toolId = null;
		String toolRisk = null;
		String appId = null;
		FileItem fileItem = null;
		String clientIpAddress = request.getRemoteAddr();
		FileItemFactory factory = null;
		ServletFileUpload upload = null;
		List<FileItem> items = null;
		Iterator<FileItem> iter = null;
		FileItem item = null;

		try {
			factory = new DiskFileItemFactory();
			upload = new ServletFileUpload(factory);
			items = upload.parseRequest(request);
			iter = items.iterator();
			String incomingParameter = null;
			String incomingValue = null;
			while (iter.hasNext()) {
				item = iter.next();
				if (item.isFormField()) {
					incomingParameter = item.getFieldName();
					incomingValue = item.getString();
					if (incomingParameter.equals(AppVetParameter.COMMAND.value)) {
						/* Used for all POST commands. */
						commandStr = incomingValue;
						log.debug("commandStr: " + commandStr);
					} else if (incomingParameter
							.equals(AppVetParameter.USERNAME.value)) {
						/* Used for all POST commands. */
						userName = incomingValue;
						log.debug("userName: " + userName);
					} else if (incomingParameter
							.equals(AppVetParameter.PASSWORD.value)) {
						/* Used for all POST commands. */
						password = incomingValue;
						log.debug("password: " + password);
					} else if (incomingParameter
							.equals(AppVetParameter.SESSIONID.value)) {
						/* Used for all POST commands. */
						sessionId = incomingValue;
						log.debug("sessionId: " + sessionId);
					} else if (incomingParameter
							.equals(AppVetParameter.TOOLID.value)) {
						/* Used only for submit report command. */
						toolId = incomingValue;
						log.debug("toolid: " + toolId);
					} else if (incomingParameter
							.equals(AppVetParameter.TOOLRISK.value)) {
						/* Used only for submit report command. */
						toolRisk = incomingValue;
						log.debug("toolrisk: " + toolRisk);
					} else if (incomingParameter
							.equals(AppVetParameter.APPID.value)) {
						/* Used only for submit report command. */
						appId = incomingValue;
						log.debug("appId: " + appId);
					} else {
						log.warn("Received unknown parameter: " + incomingValue
								+ " from IP: " + clientIpAddress);
					}
				} else {
					// item should now hold the received file
					fileItem = item;
				}
			}
			incomingParameter = null;
			incomingValue = null;

			// ----------------------- Authenticate --------------------------
			if (isAuthenticated(sessionId, userName, password, clientIpAddress,
					commandStr)) {
				if (sessionId != null) {
					userName = Database.getSessionUser(sessionId);
				}
				log.debug("User '" + userName + "' is authenticated.");
			} else {
				log.debug("User '" + userName + "' could not be authenticated.");
				sendHttpResponse(userName, appId, commandStr, clientIpAddress,
						ErrorMessage.AUTHENTICATION_ERROR.getDescription(),
						response, HttpServletResponse.SC_BAD_REQUEST, true);
				return;
			}

			// ------------------- Verify file attachment ---------------------
			if (fileItem == null) {
				sendHttpResponse(userName, appId, commandStr, clientIpAddress,
						ErrorMessage.MISSING_FILE.getDescription(), response,
						HttpServletResponse.SC_BAD_REQUEST, true);
				return;
			}
			if (!ValidateBase.isPrintable(fileItem.getName())) {
				sendHttpResponse(userName, appId, commandStr, clientIpAddress,
						ErrorMessage.ILLEGAL_CHAR_IN_UPLOADED_FILENAME_ERROR
								.getDescription(), response,
						HttpServletResponse.SC_BAD_REQUEST, true);
				return;
			}

			// ----------------------- Handle command -------------------------
			AppInfo appInfo = null;
			command = AppVetServletCommand.valueOf(commandStr);
			switch (command) {
			case SUBMIT_APP:
				/*
				 * Submit a new app for processing. Used by GUI and non-GUI
				 * clients.
				 */
				log.debug(userName + " invoked " + command.name()
						+ " with file " + fileItem.getName());
				if (!ValidateBase.hasValidAppFileExtension(fileItem.getName())) {
					sendHttpResponse(userName, appId, commandStr,
							clientIpAddress,
							ErrorMessage.INVALID_APP_FILE_EXTENSION
									.getDescription(), response,
							HttpServletResponse.SC_BAD_REQUEST, true);
					return;
				} else {
					appInfo = createAppInfo(userName, sessionId, fileItem,
							clientIpAddress, request);
					if (appInfo == null)
						return;
					else {
						sendHttpResponse(userName, appInfo.appId, commandStr,
								clientIpAddress, "appid=" + appInfo.appId,
								response, HttpServletResponse.SC_ACCEPTED,
								false);
						Registration registration = new Registration(appInfo);
						registration.registerApp();
					}
				}
				break;
			case SUBMIT_REPORT:
				/* Submit a tool report. Used by GUI and non-GUI clients. */
				log.debug(userName + " invoked " + command.name() + " on app "
						+ appId + " with report " + fileItem.getName());
				if (!ValidateBase.hasValidReportFileExtension(fileItem
						.getName())) {
					sendHttpResponse(userName, appId, commandStr,
							clientIpAddress,
							ErrorMessage.INVALID_REPORT_FILE_EXTENSION
									.getDescription(), response,
							HttpServletResponse.SC_BAD_REQUEST, true);
					return;
				} else {
					boolean appExists = Database.appExists(appId);
					if (!appExists) {
						sendHttpResponse(userName, appId, commandStr,
								clientIpAddress,
								ErrorMessage.INVALID_APPID.getDescription(),
								response, HttpServletResponse.SC_BAD_REQUEST,
								true);
					} else {
						sendHttpResponse(userName, appId, commandStr,
								clientIpAddress, "HTTP/1.1 202 Accepted",
								response, HttpServletResponse.SC_ACCEPTED,
								false);
						appInfo = createAppInfo(appId, userName, commandStr,
								toolId, toolRisk, fileItem, clientIpAddress,
								response);
						if (appInfo == null)
							return;
						else
							submitReport(userName, appInfo, response);
					}
				}
				break;
			default:
				log.warn("Received unknown command: " + commandStr
						+ " from IP: " + clientIpAddress);
			}
		} catch (final FileUploadException e) {
			sendHttpResponse(userName, appId, commandStr, clientIpAddress,
					ErrorMessage.FILE_UPLOAD_ERROR.getDescription(), response,
					HttpServletResponse.SC_BAD_REQUEST, true);
			return;
		} finally {
			command = null;
			commandStr = null;
			userName = null;
			password = null;
			sessionId = null;
			toolId = null;
			toolRisk = null;
			appId = null;
			fileItem = null;
			clientIpAddress = null;
			factory = null;
			upload = null;
			items = null;
			iter = null;
			item = null;
			System.gc();
		}
	}

	/**
	 * This method creates an app information object for a tool report
	 * submission.
	 * 
	 * @param appId
	 * @param userName
	 * @param commandStr
	 * @param toolId
	 * @param toolRisk
	 * @param fileItem
	 * @param clientIpAddress
	 * @param response
	 * @return
	 */
	private AppInfo createAppInfo(String appId, String userName,
			String commandStr, String toolId, String toolRisk,
			FileItem fileItem, String clientIpAddress,
			HttpServletResponse response) {
		if (Database.appExists(appId)) {
			AppInfo appInfo = new AppInfo(appId);
			appInfo.toolId = toolId;
			appInfo.toolRisk = toolRisk;
			appInfo.fileItem = fileItem;
			return appInfo;
		} else {
			// App ID received but does not exist in database.
			sendHttpResponse(userName, appId, commandStr, clientIpAddress,
					ErrorMessage.INVALID_APPID.getDescription(), response,
					HttpServletResponse.SC_BAD_REQUEST, true);
			return null;
		}
	}

	/**
	 * This method creates an app information object for an app submission.
	 * 
	 * @param userName
	 * @param sessionId
	 * @param fileItem
	 * @param clientIpAddress
	 * @param request
	 * @return
	 */
	private AppInfo createAppInfo(String userName, String sessionId,
			FileItem fileItem, String clientIpAddress,
			HttpServletRequest request) {

		String appId = generateAppid();
		AppInfo appInfo = new AppInfo(appId, true);
		appInfo.userName = userName;
		appInfo.sessionId = sessionId;
		appInfo.fileItem = fileItem;
		String origFileName = FileUtil
				.getNormalizedFileName(fileItem.getName());
		// Note that if the original fileItem name contained whitespace then
		// appInfo.fileName will replace those spaces with underscore
		// characters.
		appInfo.appFileName = FileUtil.replaceSpaceWithUnderscore(origFileName);

		String fileNameUpperCase = appInfo.appFileName.toUpperCase();
		if (fileNameUpperCase.endsWith(".APK")) {
			appInfo.os = DeviceOS.ANDROID;
		} else if (fileNameUpperCase.endsWith(".IPA")) {
			appInfo.os = DeviceOS.IOS;
		}

		int extensionIndex = -1;
		if (appInfo.os == DeviceOS.ANDROID) {
			extensionIndex = appInfo.appFileName.toUpperCase().indexOf(".APK");
		} else if (appInfo.os == DeviceOS.IOS) {
			extensionIndex = appInfo.appFileName.toUpperCase().indexOf(".IPA");
		}
		appInfo.appName = appInfo.appFileName.substring(0, extensionIndex);

		InetAddress addr;
		try {
			addr = InetAddress.getByName(request.getRemoteHost());
		} catch (UnknownHostException e) {
			log.error(e.getMessage());
			return null;
		}
		appInfo.clientHost = addr.getCanonicalHostName();

		// Set temporary icon until icon is extracted from app
		final String sourceIconPath = AppVetProperties.APP_IMAGES
				+ "/default.png";
		appInfo.log.debug("Source icon: " + sourceIconPath);
		final String destIconPath = AppVetProperties.APP_IMAGES + "/"
				+ appInfo.appId + ".png";
		FileUtil.copyFile(sourceIconPath, destIconPath);
		return appInfo;
	}

	private void downloadReports(HttpServletResponse response, String appid,
			String sessionId, String clientIpAddress) {
		boolean zipped = false;
		String destinationZipPath = null;
		String contentDisposition = "";

		if (appid != null) {
			String sourceDirPath = AppVetProperties.APPS_ROOT + "/" + appid
					+ "/reports";
			destinationZipPath = AppVetProperties.APPS_ROOT + "/" + appid
					+ "/AppVet_Reports_App-" + appid + "_SID-" + sessionId
					+ ".zip";
			Zip zip = new Zip();
			zipped = zip.zipDir(sourceDirPath, destinationZipPath);
		} else {
			sendHttpResponse(null, null, null, clientIpAddress,
					"appid is null.", response,
					HttpServletResponse.SC_BAD_REQUEST, true);
			return;
		}
		if (zipped) {
			try {
				File file = new File(destinationZipPath);
				if (!file.exists()) {
					sendHttpResponse(null, null, null, clientIpAddress,
							"Could not locate zipped report", response,
							HttpServletResponse.SC_INTERNAL_SERVER_ERROR, true);
					return;
				}
				contentDisposition = "attachment;filename=AppVet_Reports_App-"
						+ appid + ".zip";
				response.setContentType("application/zip");
				response.setHeader("Content-Disposition", contentDisposition);
				response.setHeader("Cache-Control", "max-age=0");
				response.setContentLength((int) file.length());
				returnFile(response, file);
				file = null;
				if (appid != null) {
					FileUtil.deleteFile(destinationZipPath);
				}
			} catch (final Exception e) {
				log.error(e.getMessage());
				e.printStackTrace();
			}
		}
	}

	private String generateAppid() {
		for (;;) {
			Random r = new Random();
			final int randInt = r.nextInt(9999999);
			String appId = new Integer(randInt).toString();
			if (!Database.appExists(appId)) {
				return appId;
			}
		}
	}

	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		log.info("*** Starting AppVet service " + AppVetProperties.VERSION
				+ " on " + AppVetProperties.SERVLET_URL);
		toolMgr = new ToolMgr();
		toolMgrThread = new Thread(toolMgr);
		toolMgrThread.start();
	}

	// TODO: Remove commandStr and clientIpAddress if not needed!
	private boolean isAuthenticated(String sessionId, String userName,
			String password, String clientIpAddress, String commandStr) {

		// Session IDs are only used by AppVet GUI users. All other clients
		// authenticate via username and password and do not use session IDs.
		log.debug("Validating session...");
		if (sessionId != null) {
			if (Database.isValidSession(sessionId, clientIpAddress)) {
				return true;
			}
		}

		log.debug("Session non-existent or invalid. Authenticate using"
				+ " username and password...");

		if (userName != null && password != null) {
			if (Authenticate.isAuthenticated(userName, password)) {
				return true;
			} else {
				return false;
			}
		} else {
			return false;
		}

		/*
		 * log.debug(
		 * "User authenticated. Checking login command for other clients...");
		 * 
		 * // Username and password authenticated, so check if request is for //
		 * login authentication. Note that all AppVet users except //
		 * third-party clients will authenticate via RPC with GWTServiceImpl.
		 * final Role role = Database.getRole(userName); final
		 * AppVetServletCommand command =
		 * AppVetServletCommand.getCommand(commandStr); if ((command ==
		 * AppVetServletCommand.AUTHENTICATE) && (role == Role.OTHER_CLIENT)) {
		 * return true; }
		 * 
		 * log.debug("Verifying role for login authentication... ");
		 * 
		 * // If request is not for login authentication, check if user is an //
		 * app store or third-party analysis provider if (role ==
		 * Role.TOOL_SERVICE_PROVIDER || role == Role.ANALYST) { // This is for
		 * Kryptowire return true; } else { return false; }
		 */
	}

	private void returnAppLog(HttpServletResponse response, String appid,
			String clientIpAddress) {
		if (appid == null) {
			sendHttpResponse(null, null, null, clientIpAddress,
					"Invalid app ID", response,
					HttpServletResponse.SC_BAD_REQUEST, true);
			return;
		}
		try {
			String filePath = AppVetProperties.APPS_ROOT + "/" + appid
					+ "/reports/" + AppVetProperties.APP_LOG_NAME;
			File file = new File(filePath);
			try {
				if (!file.exists()) {
					sendHttpResponse(null, null, null, clientIpAddress,
							"Could not locate app log at: " + filePath,
							response,
							HttpServletResponse.SC_INTERNAL_SERVER_ERROR, true);
					return;
				}
				response.setContentType("text/plain");
				response.setHeader("Content-Disposition",
						"inline;filename=appvet_log.txt");
				response.setHeader("Cache-Control", "max-age=0");
				response.setContentLength((int) file.length());
				returnFile(response, file);
			} finally {
				file = null;
				filePath = null;
			}
		} catch (final Exception e) {
			log.error(e.getMessage());
			e.printStackTrace();
		}
	}

	private void returnAppVetLog(HttpServletResponse response,
			String clientIpAddress) {
		try {
			String appVetLogPath = AppVetProperties.LOG_PATH;
			File logFile = new File(appVetLogPath);
			try {
				if (!logFile.exists()) {
					sendHttpResponse(null, null, null, clientIpAddress,
							"Could not locate AppVet log", response,
							HttpServletResponse.SC_INTERNAL_SERVER_ERROR, true);
					return;
				}
				response.setContentType("text/plain");
				response.setHeader("Content-Disposition",
						"inline;filename=appvet_log.txt");
				response.setHeader("Cache-Control", "max-age=0");
				response.setContentLength((int) logFile.length());
				returnFile(response, logFile);
			} finally {
				logFile = null;
				appVetLogPath = null;
			}
		} catch (final Exception e) {
			log.error(e.getMessage());
			e.printStackTrace();
		}
	}

	public boolean returnFile(HttpServletResponse response, File file) {
		try {
			FileInputStream fis = new FileInputStream(file);
			byte[] bytes = null;
			OutputStream os = null;
			try {
				int read = 0;
				bytes = new byte[1024];
				os = response.getOutputStream();
				while ((read = fis.read(bytes)) != -1) {
					os.write(bytes, 0, read);
				}
				fis.close();
				return true;
			} finally {
				if (os != null) {
					os.flush();
					os.close();
					os = null;
				}
				bytes = null;

				if (fis != null) {
					fis.close();
					fis = null;
				}
			}
		} catch (final IOException e) {
			log.error(e.getMessage());
			return false;
		}
	}

	/** Return all tool IDs for a specific OS */
	public void returnAllToolIDs(HttpServletResponse response,
			String clientIpAddress) {

		StringBuffer payload = new StringBuffer(
				"AppVet Android and iOS tool IDs\n\n");

		payload.append("Android tool IDs:\n");
		ArrayList<ToolServiceAdapter> androidTools = AppVetProperties.androidTools;
		for (int i = 0; i < androidTools.size(); i++) {
			ToolServiceAdapter androidTool = androidTools.get(i);
			payload.append(androidTool.id + "\n");
		}

		payload.append("iOS tool IDs:\n");
		ArrayList<ToolServiceAdapter> iosTools = AppVetProperties.iosTools;
		for (int i = 0; i < iosTools.size(); i++) {
			ToolServiceAdapter iosTool = iosTools.get(i);
			payload.append(iosTool.id + "\n");
		}

		sendHttpResponse(null, null, null, clientIpAddress, payload.toString(),
				response, HttpServletResponse.SC_OK, true);
	}

	/** Return tool IDs associated with a specific app */
	public void returnAppToolIDs(HttpServletResponse response, String appid,
			String clientIpAddress) {
		if (appid == null) {
			sendHttpResponse(null, appid, null, clientIpAddress,
					"Invalid app ID", response,
					HttpServletResponse.SC_BAD_REQUEST, true);
			return;
		}

		StringBuffer payload = new StringBuffer();
		DeviceOS appOS = Database.getAppOS(appid);

		if (appOS == DeviceOS.ANDROID) {
			//payload.append("Android tool IDs for app: " + appid + "\n");
			ArrayList<ToolServiceAdapter> androidTools = AppVetProperties.androidTools;
			for (int i = 0; i < androidTools.size(); i++) {
				ToolServiceAdapter androidTool = androidTools.get(i);
				payload.append(androidTool.id + "\n");
			}
		} else if (appOS == DeviceOS.IOS) {
			//payload.append("iOS tool IDs for app: " + appid + "\n");
			ArrayList<ToolServiceAdapter> iosTools = AppVetProperties.iosTools;
			for (int i = 0; i < iosTools.size(); i++) {
				ToolServiceAdapter iosTool = iosTools.get(i);
				payload.append(iosTool.id + "\n");
			}
		} else {
			log.error("Unknown device OS");
		}

		sendHttpResponse(null, appid, null, clientIpAddress,
				payload.toString(), response, HttpServletResponse.SC_OK, true);
	}

	public void returnReport(HttpServletResponse response, String appid,
			String toolId, String clientIpAddress) {
		if (appid == null) {
			sendHttpResponse(null, appid, null, clientIpAddress,
					"Invalid app ID", response,
					HttpServletResponse.SC_BAD_REQUEST, true);
			return;
		} else if (toolId == null) {
			sendHttpResponse(null, appid, null, clientIpAddress,
					"Invalid tool ID", response,
					HttpServletResponse.SC_BAD_REQUEST, true);
			return;
		}
		try {
			DeviceOS appOS = Database.getAppOS(appid);
			ToolServiceAdapter tool = ToolServiceAdapter.getByToolId(appOS,
					toolId);
			if (tool == null) {
				sendHttpResponse(null, appid, null, clientIpAddress,
						"Invalid tool ID", response,
						HttpServletResponse.SC_BAD_REQUEST, true);
				return;
			} else {
				if (tool.reportName == null) {
					sendHttpResponse(null, appid, null, clientIpAddress,
							"Tool report unavailable", response,
							HttpServletResponse.SC_INTERNAL_SERVER_ERROR, true);
					return;
				} else {
					String filePath = AppVetProperties.APPS_ROOT + "/" + appid
							+ "/reports/" + tool.reportName;
					File file = new File(filePath);
					if (!file.exists()) {
						sendHttpResponse(null, appid, null, clientIpAddress,
								"Report not available", response,
								HttpServletResponse.SC_BAD_REQUEST, true);
						return;
					}
					if (tool.reportFileType == ReportFileType.PDF) {
						response.setContentType("application/pdf");
					} else if (tool.reportFileType == ReportFileType.HTML) {
						response.setContentType("text/html");
					} else if (tool.reportFileType == ReportFileType.TXT) {
						response.setContentType("text/plain");
					} else if (tool.reportFileType == ReportFileType.RTF) {
						response.setContentType("application/rtf");
					}
					response.setHeader("Content-Disposition", "inline;filename="
							+ tool.reportName);
					response.setHeader("Cache-Control", "max-age=0");
					response.setContentLength((int) file.length());
					returnFile(response, file);
				}
			}
		} catch (final Exception e) {
			log.error(e.getMessage());
			e.printStackTrace();
		}
	}

	private boolean sendHttpResponse(String userName, String appId,
			String commandStr, String clientIpAddress, String message,
			HttpServletResponse response, int httpResponseCode,
			boolean errorMessage) {
		PrintWriter out = null;
		try {
			response.setStatus(httpResponseCode);
			response.setContentType("text/html");
			// Return app ID for the new app submission
			response.setHeader("appid", appId);
			out = response.getWriter();
			out.println(message);

			out.flush();
			if (errorMessage) {
				log.error("Returned HTTP " + httpResponseCode + "\n"
						+ "message = " + message + "\n" + "username = "
						+ userName + "\n" + "appid = " + appId + "\n"
						+ "command = " + commandStr + "\n"
						+ "clientIpAddress = '" + clientIpAddress + "'");
			} else {
				log.debug("Returned HTTP " + httpResponseCode + " to "
						+ clientIpAddress);
			}
			return true;
		} catch (final IOException e) {
			log.error(e.getMessage().toString());
			log.error("No HTTP response code sent to " + clientIpAddress);
			return false;
		} finally {
			if (out != null) {
				out.close();
				out = null;
			}
		}
	}

	private void submitReport(String submitterUserName, AppInfo appInfo,
			HttpServletResponse response) {
		final ToolServiceAdapter tool = ToolServiceAdapter.getByToolId(
				appInfo.os, appInfo.toolId);
		String reportName = null;
		if (appInfo.toolId == null) {
			appInfo.log.error("appInfo.reportType is null");
			return;
		}
		if (tool == null) {
			appInfo.log.error("adapter is null");
			return;
		}
		reportName = tool.reportName;
		final boolean reportSaved = FileUtil.saveReportUpload(appInfo.appId,
				reportName, appInfo.fileItem);
		if (reportSaved) {
			// Override reports override the final PASS/FAIL decision.
			if (appInfo.toolRisk.equals("FAIL")) {
				ToolStatusManager.setToolStatus(appInfo.os, appInfo.appId,
						tool.id, ToolStatus.FAIL);
			} else if (appInfo.toolRisk.equals("WARNING")) {
				ToolStatusManager.setToolStatus(appInfo.os, appInfo.appId,
						tool.id, ToolStatus.WARNING);
			} else if (appInfo.toolRisk.equals("PASS")) {
				ToolStatusManager.setToolStatus(appInfo.os, appInfo.appId,
						tool.id, ToolStatus.PASS);
			} else {
				appInfo.log.error("Unknown report type '" + appInfo.toolRisk
						+ "' received from " + appInfo.userName);
			}

			appInfo.log.info(submitterUserName + " invoked SUBMIT_REPORT for "
					+ tool.name + " on " + "app " + appInfo.appId + " with "
					+ appInfo.fileItem.getName() + " setting tool status to "
					+ appInfo.toolRisk);
		} else {
			appInfo.log.error("Could not save report!");
		}
	}

}
