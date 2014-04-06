package gov.nist.appvet.tools.preprocessor;

import gov.nist.appvet.properties.AppVetProperties;
import gov.nist.appvet.shared.Database;
import gov.nist.appvet.shared.ErrorMessage;
import gov.nist.appvet.shared.FileUtil;
import gov.nist.appvet.shared.Logger;
import gov.nist.appvet.shared.app.AppInfo;
import gov.nist.appvet.shared.status.AppStatus;
import gov.nist.appvet.shared.status.AppStatusManager;
import gov.nist.appvet.shared.status.ToolStatus;
import gov.nist.appvet.shared.status.ToolStatusManager;
import gov.nist.appvet.toolmgr.ToolServiceAdapter;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class Registration {
	
	private static final Logger log = AppVetProperties.log;
	private AppInfo appInfo = null;
	
	public Registration(AppInfo appInfo) {
		this.appInfo = appInfo;
	}

	public boolean registerApp() {
		log.debug("Start registration for appID=" + appInfo.appId);
		Connection connection = null;
		PreparedStatement preparedStatement = null;
		BufferedWriter regReportWriter = null;
		ToolServiceAdapter registrationTool = ToolServiceAdapter.getById("registration");
		String reportsPath = appInfo.getReportsPath();
		String registrationReportPath = reportsPath + "/"
				+ registrationTool.reportName;
		try {
			regReportWriter = new BufferedWriter(new FileWriter(
					registrationReportPath));
			regReportWriter.write("<HTML>\n");
			regReportWriter.write("<head>\n");
			regReportWriter.write("<style type=\"text/css\">\n");
			regReportWriter.write("h3 {font-family:arial;}\n");
			regReportWriter.write("p {font-family:arial;}\n");
			regReportWriter.write("</style>\n");
			regReportWriter.write("</head>\n");
			regReportWriter.write("<body>\n");
			regReportWriter.write("<h3>Registration Report</h3>\n");
			regReportWriter.write("<HR>\n");
			regReportWriter.write("<pre>\n");
			final Date date = new Date();
			final SimpleDateFormat format = new SimpleDateFormat(
					"yyyy-MM-dd' 'HH:mm:ss.SSSZ");
			final String currentDate = format.format(date);
			regReportWriter.write("Date: \t\t" + currentDate + "\n\n");
			connection = Database.getConnection();
			AppStatus appStatus = AppStatusManager.getAppStatus(appInfo.appId);
			if (appStatus == null) {
				
				//--------------------------------------------------------------
				// Add entry to apps table
				//--------------------------------------------------------------
				preparedStatement = connection
						.prepareStatement("REPLACE INTO apps (appid, appname, packagename, "
								+ "versioncode, versionname, filename, "
								+ "submittime, appstatus, "
								+ "statustime, username, clienthost"
								+ ") "
								+ "values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
				// Set app ID
				preparedStatement.setString(1, appInfo.appId);
				// Set app name
				preparedStatement.setString(2, "Unknown");
				// Set package name
				preparedStatement.setString(3, null);
				// Set version code
				preparedStatement.setString(4, null);
				// Set version name
				preparedStatement.setString(5, null);
				// Set file name
				preparedStatement.setString(6, appInfo.fileName);
				final java.sql.Timestamp timeStamp = new java.sql.Timestamp(
						new java.util.Date().getTime());
				// Set submission timestamp
				preparedStatement.setTimestamp(7, timeStamp);
				// Set app status
				preparedStatement.setString(8, AppStatus.REGISTERING.name());
				// Set received timestamp
				preparedStatement.setTimestamp(9, timeStamp);
				// Set username
				preparedStatement.setString(10, appInfo.userName);
				// Set client hostname
				preparedStatement.setString(11, appInfo.clientHost);

				preparedStatement.executeUpdate();
				preparedStatement.close();
				
				//--------------------------------------------------------------
				// Add entry to toolstatus table
				//--------------------------------------------------------------
				preparedStatement = connection
						.prepareStatement("REPLACE INTO toolstatus (appid) "
								+ "values (?)");
				preparedStatement.setString(1, appInfo.appId);
				preparedStatement.executeUpdate();
				preparedStatement.close();

				final ArrayList<ToolServiceAdapter> availableTools = AppVetProperties.availableTools;
				for (int i = 0; i < availableTools.size(); i++) {
					final ToolServiceAdapter tool = availableTools.get(i);
					setToolStartStatus(appInfo, tool, connection,
							preparedStatement);
				}
				Database.setLastUpdate(appInfo.appId);
				if (!FileUtil.saveFileUpload(appInfo.appId, appInfo.fileItem)) {
					regReportWriter.write("<font color=\"red\">"
							+ ErrorMessage.ERROR_SAVING_UPLOADED_FILE
							.getDescription() + "</font>");
					appInfo.log.error(ErrorMessage.ERROR_SAVING_UPLOADED_FILE.getDescription());
					ToolStatusManager.setToolStatus(appInfo.appId, registrationTool.id,
							ToolStatus.ERROR);
					return false;
				}
				appInfo.log.debug("Saved received file\tOK");

				final int extensionIndex = appInfo.fileName.toLowerCase()
						.indexOf(".apk");
				final String projectName = appInfo.fileName.substring(0,
						extensionIndex);
				appInfo.setProjectName(projectName);
				appInfo.appName = projectName;
				appInfo.log.debug("Got project name: " + projectName + "\tOK");

				regReportWriter.write("App ID: \t" + appInfo.appId + "\n");
				regReportWriter.write("File: \t\t" + appInfo.fileName + "\n");
				final File file = new File(appInfo.getIdPath() + "/"
						+ appInfo.fileName);
				regReportWriter
				.write("Submitter: \t" + appInfo.userName + "\n");

				ToolStatusManager.setToolStatus(appInfo.appId, registrationTool.id,
						ToolStatus.PASS);
				regReportWriter.write("\n<hr>\n");
				regReportWriter
				.write("Status\t\t<font color=\"green\">PASS</font>\n");
				log.debug("End registration for appID=" + appInfo.appId);
				regReportWriter.write("</pre>\n");
				regReportWriter.write("</body>\n");
				regReportWriter.write("</HTML>\n");
				return true;
			} else {
				regReportWriter.write("<font color=\"red\">"
						+ ErrorMessage.ERROR_APP_ALREADY_REGISTERED.getDescription()
						+ "</font>");
				appInfo.log.error(ErrorMessage.ERROR_APP_ALREADY_REGISTERED.getDescription());
				ToolStatusManager.setToolStatus(appInfo.appId, registrationTool.id,
						ToolStatus.ERROR);
				return false;
			}
		} catch (final Exception e) {
			appInfo.log.error(e.getMessage());
			return false;
		} finally {
			registrationTool = null;
			reportsPath = null;
			registrationReportPath = null;
			Database.cleanUpBufferedWriter(regReportWriter);
			Database.cleanUpPreparedStatement(preparedStatement);
			Database.cleanUpConnection(connection);
		}
	}
	
	private static void setToolStartStatus(AppInfo appInfo, ToolServiceAdapter tool,
			Connection connection, PreparedStatement ps) {
		final String command = "UPDATE toolstatus SET " + tool.id + "='NA' " + "WHERE appid='" + appInfo.appId
				+ "'";
		if (!Database.update(command)) {
			appInfo.log.error("Failed to update tool start status");
		}
	}
}
