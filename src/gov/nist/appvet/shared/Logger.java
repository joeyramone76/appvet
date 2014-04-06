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

import gov.nist.appvet.properties.AppVetProperties;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * This class is used for logging. This class was developed due to limitations
 * of java.util.logging and Log4j.
 * 
 * $$Id: Logger.java 38554 2014-03-30 16:06:12Z steveq $$
 */
public class Logger {

	private SimpleDateFormat format = null;
	private FileWriter writer = null;
	private boolean logToConsole = false;
	private Level userLevel = null;
	private File logFile = null;
	private boolean isClosed = true;

	public enum Level {

		DEBUG("DEBUG", 0), 
		INFO("INFO", 1), 
		WARNING("WARNING", 2), 
		ERROR("ERROR", 3);

		private int priority;

		public static Level getType(String name) {
			if (name != null) {
				for (final Level level : Level.values()) {
					if (name.equalsIgnoreCase(level.name())) {
						return level;
					}
				}
			}
			return null;
		}

		private Level(String level, int priority) {
			this.priority = priority;
		}

		private int getPriority() {
			return priority;
		}

	}

	public synchronized static String formatElapsed(long millis) {
		final long hr = TimeUnit.MILLISECONDS.toHours(millis);
		final long min = TimeUnit.MILLISECONDS.toMinutes(millis
				- TimeUnit.HOURS.toMillis(hr));
		final long sec = TimeUnit.MILLISECONDS.toSeconds(millis
				- TimeUnit.HOURS.toMillis(hr) - TimeUnit.MINUTES.toMillis(min));
		final long ms = TimeUnit.MILLISECONDS.toMillis(millis
				- TimeUnit.HOURS.toMillis(hr) - TimeUnit.MINUTES.toMillis(min)
				- TimeUnit.SECONDS.toMillis(sec));
		return String.format("%02d:%02d:%02d.%03d", hr, min, sec, ms);
	}

	public Logger(String logFilePath) {
		try {
			logFile = new File(logFilePath);
			if (!logFile.exists()) {
				logFile.createNewFile();
			}
			logToConsole = new Boolean(AppVetProperties.LOG_TO_CONSOLE)
			.booleanValue();
			writer = new FileWriter(logFile, true);
			isClosed = false;
			format = new SimpleDateFormat("yyyy-MM-dd' 'HH:mm:ss.SSSZ");
			userLevel = Logger.Level.getType(AppVetProperties.LOG_LEVEL);
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}

	public synchronized void close() {
		if (isClosed) {
			return;
		}
		try {
			writer.close();
			isClosed = true;
		} catch (final IOException e) {
			AppVetProperties.log.error("Error trying to close app logger: "
					+ e.getMessage());
			isClosed = false;
		}
	}

	public synchronized void debug(String message) {
		writeMessage(Level.DEBUG, message);
	}

	public synchronized void error(String message) {
		writeMessage(Level.ERROR, message);
	}

	public synchronized void info(String message) {
		writeMessage(Level.INFO, message);
	}

	public synchronized boolean isClosed() {
		return isClosed;
	}

	public synchronized void warn(String message) {
		writeMessage(Level.WARNING, message);
	}

	private synchronized void writeMessage(Level level, String message) {
		Date date = null;
		StackTraceElement[] stackTraceElements = null;
		String formattedDate = null;
		String logData = null;
		try {
			if (level.getPriority() >= userLevel.getPriority()) {
				date = new Date();
				stackTraceElements = new Throwable().getStackTrace();
				formattedDate = format.format(date);
				logData = stackTraceElements[2].getClassName()
						+ ":" + stackTraceElements[2].getMethodName() + " "
						+ "line " + stackTraceElements[2].getLineNumber();
				writer.write(formattedDate + " " + logData + "\n");
				writer.write(level.name() + ": " + message + "\n");
				if (logToConsole) {
					if (level == Level.ERROR) {
						System.err.print(formattedDate + " " + logData + "\n");
						System.err.print(level + ": " + message + "\n");
					} else {
						System.out.print(formattedDate + " " + logData + "\n");
						System.out.print(level + ": " + message + "\n");
					}
				}
				writer.flush();
				stackTraceElements = null;
			}
		} catch (final IOException e) {
			e.printStackTrace();
		} finally {
			logData = null;
			formattedDate = null;
			stackTraceElements = null;
			date = null;
		}
	}

}
