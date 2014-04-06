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
package gov.nist.appvet.installer.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class Database {

	public String URL;
	public String username;
	public String password;

	private Connection getConnection() {
		Connection connection = null;
		try {
			Class.forName("com.mysql.jdbc.Driver");
			connection = DriverManager.getConnection(URL + "?user=" + username
					+ "&password=" + password);
			if (connection != null) {
				connection.setAutoCommit(true); // No need to manually commit
			} else {
				System.err
						.println("Could not connect to database.");
			}
		} catch (final Exception e) {
			System.err.println("Could not connect to database: "
					+ e.getMessage() + "\nShutting down...");
			e.printStackTrace();
		}
		return connection;
	}

	public Database(String URL, String username, String password) {
		this.URL = URL;
		this.username = username;
		this.password = password;
	}

	public void setDatabase(String dbName) {
		this.URL += "/" + dbName;
	}

	public synchronized boolean update(String sql) {
		Connection connection = null;
		Statement statement = null;
		try {
			connection = getConnection();
			statement = connection.createStatement();
			statement.executeUpdate(sql);
			return true;
		} catch (final SQLException e) {
			System.err.println(e.getMessage() + " using: " + sql);
			return false;
		} finally {
			cleanUpStatement(statement);
			cleanUpConnection(connection);
		}
	}

	private synchronized static boolean cleanUpStatement(Statement statement) {
		if (statement != null) {
			try {
				statement.close();
				statement = null;
				return true;
			} catch (SQLException e) {
				System.err.println(e.getMessage());
				return false;
			}
		} else {
			return true;
		}
	}

	private synchronized static boolean cleanUpConnection(Connection connection) {
		if (connection != null) {
			try {
				connection.close();
				connection = null;
				return true;
			} catch (SQLException e) {
				System.err.println(e.getMessage());
				return false;
			}
		} else {
			return true;
		}
	}


}
