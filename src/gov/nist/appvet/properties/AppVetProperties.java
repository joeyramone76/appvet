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

package gov.nist.appvet.properties;

import gov.nist.appvet.shared.Database;
import gov.nist.appvet.shared.Logger;
import gov.nist.appvet.shared.os.DeviceOS;
import gov.nist.appvet.shared.validate.Validate;
import gov.nist.appvet.toolmgr.ToolServiceAdapter;
import gov.nist.appvet.xml.XmlUtil;

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;

public class AppVetProperties {

	/** AppVet version number. */
	public static final String VERSION = "2.0";

	// Logging
	public static Logger log = null;
	private static String LOG_NAME = "appvet_log.txt";
	public static String LOG_PATH = null;
	public static String LOG_LEVEL = null;
	public static String LOG_TO_CONSOLE = null;
	public static String APP_LOG_NAME = "app_log.txt";

	// System environment variables must be set prior to launching AppVet.
	public static String APPVET_FILES_HOME = null;
	public static String CATALINA_HOME = null;  // Tomcat
	static {
		APPVET_FILES_HOME = getEnv("APPVET_FILES_HOME");
		if (APPVET_FILES_HOME == null || APPVET_FILES_HOME.isEmpty()) {
			System.err.println("The APPVET_FILES_HOME envrionment variable is null! "
					+ "Please shutdown Tomcat and set APPVET_FILES_HOME");
		}
		CATALINA_HOME = getEnv("CATALINA_HOME");
		if (CATALINA_HOME == null || CATALINA_HOME.isEmpty()) {
			System.err.println("The CATALINA_HOME envrionment variable is null! "
					+ "Please shutdown Tomcat and set CATALINA_HOME");
		}
	}

	// Paths
	private static String CONFIG_FILE_PATH = null;
	public static String APPS_ROOT = null;
	public static String CONF_ROOT = null;
	public static String TOOLS_CONF_ROOT = null;
	public static String APP_IMAGES = null;
	public static String TEMP_ROOT = null;

	// Database properties
	public static String DB_URL = null;
	public static String DB_USERNAME = null;
	public static String DB_PASSWORD = null;

	/** Timeout in milliseconds until a URL connection is established.*/
	public static int CONNECTION_TIMEOUT = 0;
	/**Defines the socket timeout (SO_TIMEOUT) in milliseconds, which is the
    timeout for waiting for data or, put differently, a maximum period
    inactivity between two consecutive data packets).*/
	public static int SO_TIMEOUT = 0;
	/** Max session idle duration. */
	public static long MAX_SESSION_IDLE_DURATION = 0;

	// Delay for retrieving updates
	public static int GET_UPDATES_DELAY = 0;

	// Max timeout for running a tool
	public static int TOOL_TIMEOUT = 0;

	// Delay when polling for pending APKs to process (in ms)
	public static int TOOL_MGR_POLLING_INTERVAL = 0;

	// Max delay between starting each test (in ms)
	public static int TOOL_MGR_STAGGER_INTERVAL = 0;

	// URLs
	public static boolean SSL = false;
	public static String PORT = null;
	/** IP address of host */
	public static String HOST = null;
	/** HTTP address of host */
	public static String HOST_URL = null;
	/** HTTP address of host /appvet directory */
	public static String URL = null;
	/** HTTP address of host /appvet/AppVetServlet directory */
	public static String SERVLET_URL = null;
	public static boolean KEEP_APPS = false;

	// Tools
	public static ArrayList<ToolServiceAdapter> androidTools = null;
	public static ArrayList<ToolServiceAdapter> iosTools = null;

	public static String STATUS_MESSAGE = null;
	static {
		CONFIG_FILE_PATH = APPVET_FILES_HOME + "/conf/AppVetProperties.xml";
		final File configFile = new File(CONFIG_FILE_PATH);
		final String configFileName = configFile.getName();
		if (configFileName == null) {
			System.err.println("ERROR: Config file name is null.");
		}
		if (!configFile.exists()) {
			System.err.println("ERROR: AppVet config file does not exist.");
		}
		final XmlUtil xml = new XmlUtil(configFile);
		LOG_PATH = APPVET_FILES_HOME + "/logs/" + LOG_NAME;
		LOG_LEVEL = xml.getXPathValue("/AppVet/Logging/Level");
		LOG_TO_CONSOLE = xml.getXPathValue("/AppVet/Logging/ToConsole");
		log = new Logger(LOG_PATH);

		log.debug("-------------------- AppVet PROPERTIES --------------------");
		printVal("VERSION", VERSION);
		printVal("APPVET_FILES_HOME", APPVET_FILES_HOME);
		printVal("LOG_PATH", LOG_PATH);
		printVal("LOG_LEVEL", LOG_LEVEL);
		printVal("LOG_TO_CONSOLE", LOG_TO_CONSOLE);
		printVal("APP_LOG_NAME", APP_LOG_NAME);

		APPS_ROOT = APPVET_FILES_HOME + "/apps";
		printVal("APPS_ROOT", APPS_ROOT);
		TEMP_ROOT = APPVET_FILES_HOME + "/tmp";
		printVal("TEMP_ROOT", TEMP_ROOT);
		CONF_ROOT = APPVET_FILES_HOME + "/conf";
		printVal("CONF_ROOT", CONF_ROOT);
		TOOLS_CONF_ROOT = CONF_ROOT + "/tool_adapters";
		printVal("TOOLS_CONF_ROOT", TOOLS_CONF_ROOT);
		APP_IMAGES = CATALINA_HOME + "/webapps/appvet_images";
		printVal("APP_IMAGES", APP_IMAGES);
		DB_URL = xml.getXPathValue("/AppVet/Database/URL");
		printVal("DB_URL", DB_URL);
		DB_USERNAME = xml.getXPathValue("/AppVet/Database/UserName");
		printVal("DB_USERNAME", DB_USERNAME);
		DB_PASSWORD = xml.getXPathValue("/AppVet/Database/Password");
		printVal("DB_PASSWORD", DB_PASSWORD);
		
		CONNECTION_TIMEOUT = new Integer(
				xml.getXPathValue("/AppVet/ToolServices/ConnectionTimeout"))
		.intValue();
		printVal("CONNECTION_TIMEOUT", CONNECTION_TIMEOUT);
		SO_TIMEOUT = new Integer(
				xml.getXPathValue("/AppVet/ToolServices/SocketTimeout"))
		.intValue();
		printVal("SO_TIMEOUT", SO_TIMEOUT);
		TOOL_MGR_POLLING_INTERVAL = new Integer(
				xml.getXPathValue("/AppVet/ToolServices/PollingInterval")).intValue();
		printVal("TOOL_MGR_POLLING_INTERVAL", TOOL_MGR_POLLING_INTERVAL);
		TOOL_MGR_STAGGER_INTERVAL = new Integer(
				xml.getXPathValue("/AppVet/ToolServices/StaggerInterval")).intValue();
		printVal("TOOL_MGR_STAGGER_INTERVAL", TOOL_MGR_STAGGER_INTERVAL);
		TOOL_TIMEOUT = new Integer(
				xml.getXPathValue("/AppVet/ToolServices/Timeout")).intValue();
		printVal("TOOL_TIMEOUT", TOOL_TIMEOUT);
		
		MAX_SESSION_IDLE_DURATION = new Long(
				xml.getXPathValue("/AppVet/Sessions/Timeout")).longValue();
		printVal("MAX_SESSION_IDLE_DURATION", MAX_SESSION_IDLE_DURATION);
		GET_UPDATES_DELAY = new Integer(
				xml.getXPathValue("/AppVet/Sessions/GetUpdatesDelay")).intValue();
		printVal("GET_UPDATES_DELAY", GET_UPDATES_DELAY);

		HOST = xml.getXPathValue("/AppVet/Host/Hostname");
		if (HOST.equals("DHCP")) {
			try {
				InetAddress addr = InetAddress.getLocalHost();
				HOST = addr.getHostAddress();
				printVal("HOST (DHCP)", HOST);

			} catch (UnknownHostException e) {
				e.printStackTrace();
			}
		} else {
			printVal("HOST (Static)", HOST);
		}
		SSL = new Boolean(xml.getXPathValue("/AppVet/Host/SSL")).booleanValue();
		printVal("SSL", SSL);
		PORT = xml.getXPathValue("/AppVet/Host/Port");
		if (!Validate.isNumeric(PORT)) {
			log.error("AppVet Port is not numeric");
		} else {
			printVal("PORT", PORT);
		}

		if (SSL) {
			HOST_URL = "https://" + HOST + ":" + PORT;
		} else {
			HOST_URL = "http://" + HOST + ":" + PORT;
		}
		printVal("HOST_URL", HOST_URL);
		URL = HOST_URL + "/appvet";
		printVal("URL", URL);
		SERVLET_URL = URL + "/AppVetServlet";
		printVal("SERVLET_URL", SERVLET_URL);
		
		KEEP_APPS = new Boolean(xml.getXPathValue("/AppVet/Apps/KeepApps")).booleanValue();
		printVal("KEEP_APPS", KEEP_APPS);

		// Apache logging
		System.setProperty("org.apache.commons.logging.Log",
				"org.apache.commons.logging.impl.SimpleLog");
		System.setProperty("org.apache.commons.logging.simplelog.showdatetime",
				"true");
		System.setProperty(
				"org.apache.commons.logging.simplelog.log.org.apache.http",
				"ERROR");
		System.setProperty(
				"org.apache.commons.logging.simplelog.log.org.apache.http.wire",
				"ERROR");
		
		androidTools = new ArrayList<ToolServiceAdapter>();

		iosTools = new ArrayList<ToolServiceAdapter>();

		if (!Database.adminExists()) {
			log.error("No AppVet administrator found in database.");
		}
		
		setupTools(DeviceOS.ANDROID);
		setupTools(DeviceOS.IOS);
		
		log.debug("---------- END AppVet PROPERTIES -------------------");
	}

	private static String getEnv(String envVar) {
		return System.getenv(envVar);
	}

	private static synchronized void printVal(String parameter, Object obj) {
		if (obj == null) {
			log.error(parameter + ": \tnull");
		} else {
			log.debug(parameter + ": \t" + obj.toString());
		}
	}
	
	private static void setupTools(DeviceOS os) {
		File folder = null;

		if (os == DeviceOS.ANDROID){

			folder = new File(TOOLS_CONF_ROOT + "/android");
		} else if (os == DeviceOS.IOS){

			folder = new File(TOOLS_CONF_ROOT + "/ios");
		} else {
			log.error("Unknown OS found");
		}
		
		File[] listOfFiles = folder.listFiles();

		for (final File toolConfigFile : listOfFiles) {

			if (toolConfigFile.isFile()) {

				final String toolConfigFileName = toolConfigFile.getName();
				if (toolConfigFileName.endsWith(".xml")) {

					final ToolServiceAdapter adapter = new ToolServiceAdapter(toolConfigFile);
					if (os == DeviceOS.ANDROID){

						log.debug("Adding Android tool " + adapter.name);
						androidTools.add(adapter);
					} else if (os == DeviceOS.IOS){

						log.debug("Adding iOS tool " + adapter.name);
						iosTools.add(adapter);					
					}
				}
			}
		}
		listOfFiles = null;
		folder = null;
		
		ArrayList<String> tableColumnNames = null;
		if (os == DeviceOS.ANDROID){

			folder = new File(TOOLS_CONF_ROOT + "/android");
			tableColumnNames = Database
					.getTableColumnNames("androidtoolstatus");
			// Check that all Android tools have a column in the androidtoolstatus table.
			if (tableColumnNames == null) {
				log.error("Could not get androidtoolstatus column names");
				return;
			}
			for (int i = 0; i < androidTools.size(); i++) {

				final ToolServiceAdapter tool = androidTools.get(i);
				if (!tableColumnNames.contains(tool.id)) {
					// Add to table "androidtoolstatus"
					tableColumnNames.add(tool.id);
					if (Database.addTableColumn("androidtoolstatus", tool.id, "VARCHAR (120)")) {
						log.debug("Added Android tool '" + tool.id + "' to androidtoolstatus table");
					} else {
						log.error("Could not add Android tool '" + tool.id
								+ "' to androidtoolstatus table");
					}
				}
			}
			tableColumnNames = null;
			log.debug("Found " + androidTools.size() + " Android tools");
			
		} else if (os == DeviceOS.IOS){
			
			folder = new File(TOOLS_CONF_ROOT + "/ios");
			tableColumnNames = Database
					.getTableColumnNames("iostoolstatus");
			// Check that all iOS tools have a column in the iostoolstatus table.
			if (tableColumnNames == null) {
				log.error("Could not get iostoolstatus column names");
				return;
			}
			for (int i = 0; i < iosTools.size(); i++) {
				final ToolServiceAdapter tool = iosTools.get(i);
				if (!tableColumnNames.contains(tool.id)) {
					// Add to table "iostools"
					tableColumnNames.add(tool.id);
					if (Database.addTableColumn("iostoolstatus", tool.id, "VARCHAR (120)")) {
						log.debug("Added iOS tool '" + tool.id + "' to iostoolstatus table");
					} else {
						log.error("Could not add iOS tool '" + tool.id
								+ "' to iostoolstatus table");
					}
				}
			}
			tableColumnNames = null;
			log.debug("Found " + iosTools.size() + " iOS tools");
			
		}

	}

}
