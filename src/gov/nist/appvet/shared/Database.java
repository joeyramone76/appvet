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
package gov.nist.appvet.shared;

import gov.nist.appvet.gwt.shared.AppInfoGwt;
import gov.nist.appvet.gwt.shared.UserInfoGwt;
import gov.nist.appvet.properties.AppVetProperties;
import gov.nist.appvet.shared.os.DeviceOS;
import gov.nist.appvet.shared.role.Role;
import gov.nist.appvet.shared.status.AppStatus;

import java.io.BufferedWriter;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.Vector;


public class Database {


	private static final Logger log = AppVetProperties.log;

	public static boolean addTableColumn(String tableName, String columnName,
			String type) {
		return update("ALTER TABLE " + tableName + " ADD " + columnName + " "
				+ type);
	}

	public static boolean appExists(String appid) {
		return exists("SELECT * FROM apps " + "where appid='" + appid + "'");
	}

	public static boolean clearExpiredSessions() {
		return update("DELETE FROM sessions WHERE expiretime < "
				+ new Date().getTime());
	}

	public static boolean deleteUser(String username) {
		return update("DELETE FROM users " + "where username='" + username
				+ "'");
	}

	public static boolean adminAddNewUser(String username, String password,
			String org, String email, String role, String lastName,
			String firstName) {
		Connection connection = null;
		PreparedStatement preparedStatement = null;
		try {
			connection = getConnection();
			preparedStatement = connection.prepareStatement(
					"REPLACE INTO USERS (username, password, "
							+ "org, email, role, lastName, firstName) "
							+ "values (?, ?, ?, ?, ?, ?, ?)");
			preparedStatement.setString(1, username);
			preparedStatement.setString(2, getPBKDF2Password(password));
			preparedStatement.setString(3, org);
			preparedStatement.setString(4, email);
			preparedStatement.setString(5, role);
			preparedStatement.setString(6, lastName);
			preparedStatement.setString(7, firstName);
			preparedStatement.executeUpdate();
			return true;

		} catch (final SQLException e) {
			log.error(e.getMessage());
			return false;
		} finally {
			cleanUpPreparedStatement(preparedStatement);
			cleanUpConnection(connection);
		}
	}

	public static boolean adminAddNewUser(UserInfoGwt userInfo) {
		Connection connection = null;
		PreparedStatement preparedStatement = null;
		try {
			connection = getConnection();
			preparedStatement = connection.prepareStatement(""
					+ "REPLACE INTO USERS (username,"
					+ "org, email, role, " + "lastName, firstName) "
					+ "values (?, ?, ?, ?, ?, ?)");
			preparedStatement.setString(1, userInfo.getUserName());
			preparedStatement.setString(2, userInfo.getOrganization());
			preparedStatement.setString(3, userInfo.getEmail());
			preparedStatement.setString(4, userInfo.getRole());
			preparedStatement.setString(5, userInfo.getLastName());
			preparedStatement.setString(6, userInfo.getFirstName());
			preparedStatement.executeUpdate();
			final String username = userInfo.getUserName();
			if (userInfo.isChangePassword()) {
				final String password = userInfo.getPassword();
				try {
					if (setPBKDF2Password(username, password)) {
						return true;
					} else {
						return false;
					}
				} catch (Exception e) {
					log.error(e.getMessage());
					return false;
				}
			} else {
				return true;
			}
		} catch (final SQLException e) {
			log.error(e.getMessage());
			return false;
		} finally {
			cleanUpPreparedStatement(preparedStatement);
			cleanUpConnection(connection);
		}
	}



	public static boolean updateUser(UserInfoGwt userInfo) {
		Connection connection = null;
		Statement statement = null;
		try {
			connection = getConnection();
			statement = connection.createStatement();
			statement.executeUpdate("UPDATE users SET " 
					+ "username='" + userInfo.getUserName() 
					+ "', org='" + userInfo.getOrganization() 
					+ "', email='" + userInfo.getEmail() 
					+ "', role='" + userInfo.getRole()
					+ "', lastName='" + userInfo.getLastName()
					 + "', firstName='" + userInfo.getFirstName() 
					+ "' WHERE username='" + userInfo.getUserName() + "'");
			if (userInfo.isChangePassword()) {
				final String userName = userInfo.getUserName();
				final String password = userInfo.getPassword();
				try {
					if (setPBKDF2Password(userName, password)) {
						return true;
					} else {
						return false;
					}
				} catch (Exception e) {
					log.error(e.getMessage());
					return false;
				}
			} else {
				return true;
			}
		} catch (final SQLException e) {
			log.error(e.getMessage());
			return false;
		} finally {
			cleanUpStatement(statement);
			cleanUpConnection(connection);
		}
	}

	public static UserInfoGwt getUser(String username) {
		Connection connection = null;
		Statement statement = null;
		ResultSet resultSet = null;
		UserInfoGwt userInfo = null;
		String sql = null;
		try {
			sql = "SELECT * FROM users where username='" + username + "'";
			connection = getConnection();
			statement = connection.createStatement();
			resultSet = statement.executeQuery(sql);
			userInfo = new UserInfoGwt();
			resultSet.next();
			userInfo.setUserName(resultSet.getString(1));
			userInfo.setPassword(getAttributeValue(resultSet.getString(2)));
			userInfo.setOrganization(getAttributeValue(resultSet.getString(3)));
			userInfo.setEmail(getAttributeValue(resultSet.getString(4)));
			userInfo.setRole(getAttributeValue(resultSet.getString(5)));
			userInfo.setLastLogon(getAttributeValue(resultSet.getString(6)));
			userInfo.setFromHost(getAttributeValue(resultSet.getString(7)));
			userInfo.setLastName(getAttributeValue(resultSet.getString(8)));
			userInfo.setFirstName(getAttributeValue(resultSet.getString(9)));
		} catch (final SQLException e) {
			log.error(e.getMessage());
			return null;
		} finally {
			sql = null;
			cleanUpResultSet(resultSet);
			cleanUpStatement(statement);
			cleanUpConnection(connection);
		}
		return userInfo;
	}

	public static boolean adminExists() {
		Connection connection = null;
		Statement statement = null;
		ResultSet resultSet = null;
		UserInfoGwt userInfo = null;
		String sql = null;
		boolean foundAdmin = false;

		try {
			connection = getConnection();
			sql = "SELECT * FROM users";
			statement = connection.createStatement();
			resultSet = statement.executeQuery(sql);
			while (resultSet.next()) {
				String role = getAttributeValue(resultSet.getString(5));
				if (role.equals("ADMIN")) {
					foundAdmin = true;
					break;
				}
			}
		} catch (final SQLException e) {
			log.error(e.getMessage());
		} finally {
			sql = null;
			userInfo = null;
			cleanUpResultSet(resultSet);
			cleanUpStatement(statement);
			cleanUpConnection(connection);
		}
		return foundAdmin;
	}

	public static List<UserInfoGwt> getUsers() {
		Connection connection = null;
		Statement statement = null;
		ResultSet resultSet = null;
		UserInfoGwt userInfo = null;
		String sql = null;
		ArrayList<UserInfoGwt> arrayList = null;
		try {
			connection = getConnection();
			sql = "SELECT * FROM users ORDER BY lastName ASC";
			arrayList = new ArrayList<UserInfoGwt>();
			statement = connection.createStatement();
			resultSet = statement.executeQuery(sql);
			while (resultSet.next()) {
				userInfo = new UserInfoGwt();
				userInfo.setUserName(resultSet.getString(1));
				userInfo.setPassword(getAttributeValue(resultSet.getString(2)));
				userInfo.setOrganization(getAttributeValue(resultSet
						.getString(3)));
				userInfo.setEmail(getAttributeValue(resultSet.getString(4)));
				userInfo.setRole(getAttributeValue(resultSet.getString(5)));
				userInfo.setLastLogon(getAttributeValue(resultSet.getString(6)));
				userInfo.setFromHost(getAttributeValue(resultSet.getString(7)));
				userInfo.setLastName(getAttributeValue(resultSet.getString(8)));
				userInfo.setFirstName(getAttributeValue(resultSet.getString(9)));
				arrayList.add(userInfo);
			}
		} catch (final SQLException e) {
			log.error(e.getMessage());
			arrayList = null;
		} finally {
			sql = null;
			userInfo = null;
			cleanUpResultSet(resultSet);
			cleanUpStatement(statement);
			cleanUpConnection(connection);
		}
		return arrayList;
	}

	public synchronized static long getLong(String sql) {
		Connection connection = null;
		Statement statement = null;
		ResultSet resultSet = null;
		try {
			connection = getConnection();
			statement = connection.createStatement();
			resultSet = statement.executeQuery(sql);
			while (resultSet.next()) {
				return resultSet.getLong(1);
			}
		} catch (final SQLException e) {
			log.error(e.getMessage() + " using: " + sql);
		} finally {
			cleanUpResultSet(resultSet);
			cleanUpStatement(statement);
			cleanUpConnection(connection);
		}
		return -1;
	}

	public synchronized static String getString(String sql) {
		Connection connection = null;
		Statement statement = null;
		ResultSet resultSet = null;
		try {
			connection = getConnection();
			statement = connection.createStatement();
			resultSet = statement.executeQuery(sql);
			while (resultSet.next()) {
				return resultSet.getString(1);
			}
		} catch (final SQLException e) {
			log.error(e.getMessage() + " using: " + sql);
		} finally {
			cleanUpResultSet(resultSet);
			cleanUpStatement(statement);
			cleanUpConnection(connection);
		}
		return null;
	}

	// Called by AppVet installer
	public synchronized static boolean createAppVetDb(String url, String username,
			String password) {
		Connection connection = null;
		Statement statement = null;
		try {
			Class.forName("com.mysql.jdbc.Driver");
			connection = DriverManager.getConnection(url
					+ "?user=" + username + "&password="
					+ password);
			if (connection != null) {
				connection.setAutoCommit(true); // No need to manually commit
			} else {
				System.err.println("Could not connect to database.");
			}
		} catch (final Exception e) {
			e.printStackTrace();
		}
		String sql = null;
		try {
			statement = connection.createStatement();
			sql = "create database appvet";
			statement.executeUpdate(sql);
			return true;
		} catch (final SQLException e) {
			log.error(e.getMessage() + " using: " + sql);
			return false;
		} finally {
			cleanUpStatement(statement);
			cleanUpConnection(connection);
		}
	}

	public synchronized static boolean update(String sql) {
		Connection connection = null;
		Statement statement = null;
		try {
			connection = getConnection();
			statement = connection.createStatement();
			statement.executeUpdate(sql);
			return true;
		} catch (final SQLException e) {
			log.error(e.getMessage() + " using: " + sql);
			return false;
		} finally {
			cleanUpStatement(statement);
			cleanUpConnection(connection);
		}
	}

	public synchronized static boolean exists(String sql) {
		if (getString(sql) != null) {
			return true;
		} else {
			return false;
		}
	}

	public synchronized static boolean getBoolean(String sql) {
		Connection connection = null;
		Statement statement = null;
		ResultSet resultSet = null;
		try {
			connection = getConnection();
			statement = connection.createStatement();
			resultSet = statement.executeQuery(sql);
			while (resultSet.next()) {
				return resultSet.getBoolean(1);
			}
		} catch (final SQLException e) {
			log.error(e.getMessage());
		} finally {
			cleanUpResultSet(resultSet);
			cleanUpStatement(statement);
			cleanUpConnection(connection);
		}
		return false;
	}
	
//	/** This method gets tool IDs from the database. This information is also
//	 * available from AppVetProperties.androidtools and AppVetProperties.iostools.
//	 * @param os
//	 * @return
//	 */
//	public synchronized static ArrayList<String> getToolIDs(DeviceOS os) {
//		Connection connection = null;
//		Statement statement = null;
//		ResultSet resultSet = null;
//		String sql;
//		if (os == DeviceOS.ANDROID) {
//			sql = "SHOW COLUMNS FROM androidtoolstatus";
//		} else {
//			sql = "SHOW COLUMNS FROM iostoolstatus";
//		}
//		ArrayList<String> toolIDs = new ArrayList<String>();
//		try {
//			connection = getConnection();
//			statement = connection.createStatement();
//			resultSet = statement.executeQuery(sql);
//			while (resultSet.next()) {
//				//return resultSet.getString(1);
//				toolIDs.add(resultSet.getString(1));
//			}
//		} catch (final SQLException e) {
//			log.error(e.getMessage());
//		} finally {
//			cleanUpResultSet(resultSet);
//			cleanUpStatement(statement);
//			cleanUpConnection(connection);
//		}
//		return toolIDs;
//	}

	/**
	 * Get the next app that has the given appstatus, in ascending order.
	 * 
	 * @return The appid of the next app.
	 */
	public synchronized static String getNextApp(AppStatus appStatus) {
		Connection connection = null;
		Statement statement = null;
		ResultSet resultSet = null;
		final String sql = "SELECT appid FROM apps " + "where appstatus='"
				+ appStatus.name() + "' ORDER BY " + "submittime ASC";
		try {
			connection = getConnection();
			statement = connection.createStatement();
			resultSet = statement.executeQuery(sql);
			while (resultSet.next()) {
				return resultSet.getString(1);
			}
		} catch (final SQLException e) {
			log.error(e.getMessage());
		} finally {
			cleanUpResultSet(resultSet);
			cleanUpStatement(statement);
			cleanUpConnection(connection);
		}
		return null;
	}

	public synchronized static String getAppName(String appid) {
		return getString("SELECT appname FROM apps " + "where appid='" + appid
				+ "'");
	}

	public synchronized static DeviceOS getAppOS(String appid) {
		String appOSStr = getString("SELECT os FROM apps " + "where appid='" + appid
				+ "'");
		if (appOSStr == null) {
			return null;
		} else {
			return DeviceOS.getOS(appOSStr);
		}
	}

	public synchronized static String getClientIPAddress(String appid) {
		return getString("SELECT clienthost FROM apps " + "where appid='"
				+ appid + "'");
	}

	public static Connection getConnection() {
		Connection connection = null;
		try {
			Class.forName("com.mysql.jdbc.Driver");
			connection = DriverManager.getConnection(AppVetProperties.DB_URL
					+ "/appvet?user=" + AppVetProperties.DB_USERNAME + "&password="
					+ AppVetProperties.DB_PASSWORD);
			if (connection != null) {
				connection.setAutoCommit(true); // No need to manually commit
			} else {
				log.error("Could not connect to database.");
			}
		} catch (final Exception e) {
			log.error("Could not connect to database: " + e.getMessage());
		}
		return connection;
	}

	public synchronized static List<AppInfoGwt> getAllApps(String username) {
		Connection connection = null;
		ArrayList<AppInfoGwt> appsList = null;
		Statement statement = null;
		ResultSet resultSet = null;
		try {
			connection = getConnection();
			appsList = new ArrayList<AppInfoGwt>();
			String sql = null;
			final Role userRole = getRole(username);
			if (userRole == Role.DEV) {
				sql = "SELECT * FROM apps where username='" + username
						+ "' ORDER BY submittime DESC";
			} else {
				sql = "SELECT * FROM apps ORDER BY submittime DESC";
			}
			statement = connection.createStatement();
			resultSet = statement.executeQuery(sql);
			Timestamp mostRecentUpdateTimestamp = null;
			Timestamp appUpdateTimestamp = null;
			while (resultSet.next()) {
				final AppInfoGwt appInfo = getAppInfo(resultSet);
				if (appUpdateTimestamp == null) {
					appUpdateTimestamp = new Timestamp(appInfo.statusTime);
				} else {
					appUpdateTimestamp.setTime(appInfo.statusTime);
				}
				if ((mostRecentUpdateTimestamp == null)
						|| appUpdateTimestamp.after(mostRecentUpdateTimestamp)) {
					mostRecentUpdateTimestamp = new Timestamp(
							appUpdateTimestamp.getTime());
				}
				appsList.add(appInfo);
			}
			final AppInfoGwt clientUpdateTimestamp = new AppInfoGwt();
			if (mostRecentUpdateTimestamp == null) {
				mostRecentUpdateTimestamp = new Timestamp(new Date().getTime());
			}
			clientUpdateTimestamp.setLastAppUpdate(mostRecentUpdateTimestamp
					.getTime());
			// Add time stamp to beginning of list
			appsList.add(0, clientUpdateTimestamp);
		} catch (final SQLException e) {
			log.error(username + ": " + e.getMessage());
			appsList = null;
		} finally {
			cleanUpConnection(connection);
			cleanUpStatement(statement);
			cleanUpResultSet(resultSet);
		}
		return appsList;
	}

	public synchronized static List<AppInfoGwt> getUpdatedApps(String username,
			long lastClientUpdateDate) {
		Connection connection = null;
		Statement statement = null;
		ResultSet resultSet = null;
		Role userRole = null;
		String sql = null;
		Timestamp lastClientUpdateTimestamp = null;
		Timestamp newLastClientUpdateTimestamp = null;
		Timestamp appUpdateTimestamp = null;
		AppInfoGwt appInfo = null;
		ArrayList<AppInfoGwt> appsList = null;
		try {
			connection = getConnection();
			userRole = getRole(username);
			if (userRole == Role.DEV) {
				sql = "SELECT * FROM apps where username='" + username
						+ "' ORDER BY statustime DESC";
			} else {
				sql = "SELECT * FROM apps ORDER BY statustime DESC";
			}
			lastClientUpdateTimestamp = new Timestamp(lastClientUpdateDate);
			appsList = new ArrayList<AppInfoGwt>();
			statement = connection.createStatement();
			resultSet = statement.executeQuery(sql);
			while (resultSet.next()) {
				appInfo = getAppInfo(resultSet);
				if (appUpdateTimestamp == null) {
					appUpdateTimestamp = new Timestamp(appInfo.statusTime);
				} else {
					appUpdateTimestamp.setTime(appInfo.statusTime);
				}
				if (appUpdateTimestamp.after(lastClientUpdateTimestamp)) {
					appsList.add(appInfo);
					if (newLastClientUpdateTimestamp == null) {
						newLastClientUpdateTimestamp = new Timestamp(
								appUpdateTimestamp.getTime());
					}
				} else {
					// Since resultSet is ordered in statustime-descending
					// order, we have reached the end of possible updates.
					break;
				}
			}
			// This object containing the last updated timestamp will be
			// removed first by the AppVet client.
			final AppInfoGwt updatedTimestamp = new AppInfoGwt();
			updatedTimestamp.appId = "lastServerUpdateTimestamp";
			if (newLastClientUpdateTimestamp == null) {
				newLastClientUpdateTimestamp = new Timestamp(
						lastClientUpdateDate);
			}
			updatedTimestamp.setLastAppUpdate(newLastClientUpdateTimestamp
					.getTime());
			appsList.add(0, updatedTimestamp);
		} catch (final SQLException e) {
			log.error(username + ": " + e.getMessage());
			appsList = null;
		} finally {
			appInfo = null;
			appUpdateTimestamp = null;
			newLastClientUpdateTimestamp = null;
			lastClientUpdateTimestamp = null;
			sql = null;
			userRole = null;
			cleanUpConnection(connection);
			cleanUpStatement(statement);
			cleanUpResultSet(resultSet);
		}
		return appsList;
	}

	public synchronized static AppInfoGwt getAppInfo(ResultSet resultSet) {
		final AppInfoGwt appInfo = new AppInfoGwt();
		try {
			appInfo.appId = resultSet.getString(1);
			appInfo.appName = getAttributeValue(resultSet.getString(2));
			appInfo.packageName = getAttributeValue(resultSet.getString(3));
			appInfo.versionCode = getAttributeValue(resultSet.getString(4));
			appInfo.versionName = getAttributeValue(resultSet.getString(5));
			appInfo.appFileName = resultSet.getString(6);
			appInfo.submitTime = resultSet.getTimestamp(7).getTime();
			String appStatusString = resultSet.getString(8);
			appInfo.appStatus = AppStatus.getStatus(appStatusString);
			appInfo.statusTime = resultSet.getTimestamp(9).getTime();
			appInfo.userName = getAttributeValue(resultSet.getString(10));
			appInfo.clientHost = getAttributeValue(resultSet.getString(11));
			String osName = getAttributeValue(resultSet.getString(12));
			appInfo.os = DeviceOS.getOS(osName);
		} catch (final SQLException e) {
			e.printStackTrace();
		}
		return appInfo;
	}

	public static String getAttributeValue(String string) {
		return (string == null) ? "" : string;
	}

	public synchronized static String getAppFileName(String appid) {
		return getString("SELECT filename FROM apps " + "where appid='" + appid
				+ "'");
	}

	public synchronized static String getOwner(String appid) {
		return getString("SELECT username FROM apps " + "where appid='" + appid
				+ "'");
	}
	
	public synchronized static String getOs(String appid) {
		return getString("SELECT os FROM apps " + "where appid='" + appid
				+ "'");
	}

	protected synchronized static String getPasswordHash(String username) {
		return getString("SELECT password FROM users " + "where username='"
				+ username + "'");
	}

	public synchronized static Role getRole(String username) {
		String roleString = getString("SELECT role FROM users "
				+ "where username='" + username + "'");
		final Role userRole = Role.valueOf(roleString);
		roleString = null;
		if (userRole == null) {
			log.error("Error getting user role");
		}
		return userRole;
	}

	public synchronized static long getSessionExpiration(String sessionId,
			String clientIpAddress) {
		return getLong("SELECT expiretime FROM sessions "
				+ "where (clientaddress='" + clientIpAddress + "') "
				+ "AND sessionid='" + sessionId + "'");
	}

	public static String getSessionUser(String sessionId) {
		final String cmd = "SELECT username FROM sessions "
				+ "where sessionId='" + sessionId + "'";
		return getString(cmd);
	}

	public static Date getSubmitTime(String appid) {
		Timestamp value = null;
		Connection connection = null;
		Statement statement = null;
		ResultSet resultSet = null;
		String sql = "SELECT submittime FROM apps " + "where appid='" + appid
				+ "'";
		try {
			connection = getConnection();
			statement = connection.createStatement();
			resultSet = statement.executeQuery(sql);
			while (resultSet.next()) {
				return resultSet.getTimestamp(1);
			}
		} catch (final SQLException e) {
			log.error(e.getMessage() + " using: " + sql);
		} finally {
			sql = null;
			cleanUpConnection(connection);
			cleanUpStatement(statement);
			cleanUpResultSet(resultSet);
		}
		return value;
	}

	public static ArrayList<String> getTableColumnNames(String tableName) {
		ArrayList<String> columnNames = null;
		String sql = "SELECT * FROM " + tableName;
		Connection connection = null;
		Statement statement = null;
		ResultSet resultSet = null;
		ResultSetMetaData rsmd = null;
		try {
			connection = getConnection();
			statement = connection.createStatement();
			resultSet = statement.executeQuery(sql);
			rsmd = resultSet.getMetaData();
			final int columnCount = rsmd.getColumnCount();
			columnNames = new ArrayList<String>();
			// Column count starts from 1
			for (int i = 1; i < (columnCount + 1); i++) {
				final String name = rsmd.getColumnName(i);
				columnNames.add(name);
			}
		} catch (SQLException e) {
			log.error(e.getMessage() + " using: " + sql);
		} finally {
			rsmd = null;
			cleanUpConnection(connection);
			cleanUpStatement(statement);
			cleanUpResultSet(resultSet);
		}
		return columnNames;
	}


	/** Check if current session is valid. This method does not update the
	 * session expiration date/time.
	 * @param sessionId
	 * @param clientIpAddress
	 * @return
	 */
	public synchronized static boolean isValidSession(String sessionId,
			String clientIpAddress) {
		Date date = null;
		try {
			if ((sessionId == null) || (clientIpAddress == null)) {
				return false;
			}
			final long expireTimeLong = getSessionExpiration(sessionId,
					clientIpAddress);
			if (expireTimeLong == -1) {
				return false;
			} else {
				date = new Date();
				final long currentTimeLong = date.getTime();
				date = null;
				if (expireTimeLong < currentTimeLong) {
					update("DELETE FROM sessions WHERE sessionid='" + sessionId
							+ "'");
					return false;
				} else {
					return true;
				}
			}
		} finally {
			date = null;
		}
	}

	public static boolean removeSession(String sessionId, String clientIpAddress) {
		return update("DELETE FROM sessions WHERE (clientaddress='"
				+ clientIpAddress + "' OR clientaddress='127.0.0.1') "
				+ "AND sessionid='" + sessionId + "'");
	}

	public synchronized static void setLastUpdate(String appId) {
		Date date = null;
		java.sql.Timestamp timeStamp = null;
		try {
			date = new Date();
			timeStamp = new java.sql.Timestamp(date.getTime());
			update("UPDATE apps SET statustime='" + timeStamp
					+ "' where appid='" + appId + "'");

		} finally {
			timeStamp = null;
			date = null;
		}
	}

	public synchronized static String getPBKDF2Password(String password) {
		try {
			return Authenticate.createHash(password);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		} 
	}

	public synchronized static boolean setPBKDF2Password(String username,
			String password) {
		try {
			// Generate salted PBKDF2 hash
			final String passwordHash = Authenticate.createHash(password);
			return update("UPDATE users SET password='" + passwordHash + "'"
					+ " WHERE username='" + username + "'");
		} catch (NoSuchAlgorithmException e) {
			log.error(e.getMessage());
		} catch (InvalidKeySpecException e) {
			log.error(e.getMessage());
		}
		return false;
	}

	public synchronized static String setSession(String username,
			String clientIpAddress) {
		UUID uuid = null;
		String sessionId = null;
		Date sessionDateTime = null;
		long sessionDateTimeLong = -1;
		long sessionExpireTimeLong = -1;
		String sql = "INSERT INTO sessions (sessionid, username, expiretime, clientaddress) "
				+ "VALUES(?, ?, ?, ?)";
		Connection connection = null;
		PreparedStatement ps = null;
		try {
			uuid = UUID.randomUUID();
			sessionId = uuid.toString().replaceAll("-", "");
			sessionDateTime = new Date();
			sessionDateTimeLong = sessionDateTime.getTime();
			sessionExpireTimeLong = sessionDateTimeLong
					+ AppVetProperties.MAX_SESSION_IDLE_DURATION;
			connection = getConnection();
			ps = connection.prepareStatement(sql);
			ps.setString(1, sessionId);
			ps.setString(2, username);
			ps.setLong(3, sessionExpireTimeLong);
			ps.setString(4, clientIpAddress);
			ps.executeUpdate();
		} catch (final SQLException e) {
			log.error(e.getMessage());
		} finally {
			sql = null;
			sessionDateTime = null;
			uuid = null;
			cleanUpPreparedStatement(ps);
			cleanUpConnection(connection);
		}
		return sessionId;
	}

	public static boolean updateApp(String appid, String appName,
			String packageName, String versionCode, String versionName) {
		return update("UPDATE apps SET appname='" + appName
				+ "', packagename='" + packageName + "', versioncode='"
				+ versionCode + "', versionname='" + versionName + "' "
				+ "WHERE appid='" + appid + "'");
	}

//	public static boolean updateAppSdk(String appid, String minsdk,
//			String maxsdk, String targetsdk) {
//		if (minsdk != null) {
//			if (!update("UPDATE apps SET minsdk='" + minsdk + "' where appid='"
//					+ appid + "'")) {
//				return false;
//			}
//		}
//		if (maxsdk != null) {
//			if (!update("UPDATE apps SET maxsdk='" + maxsdk + "' where appid='"
//					+ appid + "'")) {
//				return false;
//			}
//		}
//		if (targetsdk != null) {
//			if (!update("UPDATE apps SET targetsdk='" + targetsdk
//					+ "' where appid='" + appid + "'")) {
//				return false;
//			}
//		}
//		return true;
//	}

	public static boolean updateClientHost(String username, String host) {
		return update("UPDATE users SET fromhost ='" + host
				+ "' WHERE username = '" + username + "'");
	}

	public static boolean updateSessionExpiration(String sessionId,
			String clientIpAddress, long newExpirationTimeLong) {
		return update("UPDATE sessions SET expiretime='"
				+ newExpirationTimeLong + "' WHERE (clientaddress='"
				+ clientIpAddress + "' OR clientaddress='127.0.0.1') "
				+ "AND sessionid='" + sessionId + "'");
	}

	public synchronized static boolean cleanUpConnection(Connection connection) {
		if (connection != null) {
			try {
				connection.close();
				connection = null;
				return true;
			} catch (SQLException e) {
				log.error(e.getMessage());
				return false;
			}
		} else {
			return true;
		}
	}

	// Deleting an app will not be immediately reflected to users
	// (other than the user deleting the app) until a new AppVet session is
	// started. A better but more complex approach is to
	// update the app's status to "DELETED" and update users' display. However,
	// it is not clear when the app should be deleted.
	public static boolean deleteApp(DeviceOS os, String appid) {
		final boolean appDeleted = update("DELETE FROM apps " + "where appid='"
				+ appid + "'");
		boolean statusDeleted = false;
		if (os == DeviceOS.ANDROID) {
			statusDeleted = update("DELETE FROM androidtoolstatus "
					+ "where appid='" + appid + "'");
		} else {
			statusDeleted = update("DELETE FROM iostoolstatus "
					+ "where appid='" + appid + "'");
		}

		if (appDeleted && statusDeleted) {
			return true;
		} else {
			return false;
		}
	}

	private synchronized static boolean cleanUpStatement(Statement statement) {
		if (statement != null) {
			try {
				statement.close();
				statement = null;
				return true;
			} catch (SQLException e) {
				log.error(e.getMessage());
				return false;
			}
		} else {
			return true;
		}
	}

	public synchronized static boolean cleanUpPreparedStatement(
			PreparedStatement preparedStatement) {
		if (preparedStatement != null) {
			try {
				preparedStatement.close();
				preparedStatement = null;
				return true;
			} catch (SQLException e) {
				log.error(e.getMessage());
				return false;
			}
		} else {
			return true;
		}
	}

	public synchronized static boolean cleanUpBufferedWriter(
			BufferedWriter bufferedWriter) {
		if (bufferedWriter != null) {
			try {
				bufferedWriter.close();
				bufferedWriter = null;
				return true;
			} catch (Exception e) {
				log.error(e.getMessage());
				return false;
			}
		} else {
			return true;
		}
	}

	private synchronized static boolean cleanUpResultSet(ResultSet resultSet) {
		if (resultSet != null) {
			try {
				resultSet.close();
				resultSet = null;
				return true;
			} catch (SQLException e) {
				log.error(e.getMessage());
				return false;
			}
		} else {
			return true;
		}
	}

	private Database() {
	}
}
