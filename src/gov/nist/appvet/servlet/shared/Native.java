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
package gov.nist.appvet.servlet.shared;

import gov.nist.appvet.shared.Logger;
import gov.nist.appvet.shared.app.AppInfo;

import java.io.IOException;
import java.io.InputStream;
import java.util.Timer;

/**
 * This class is used to execute native commands.
 * 
 * $$Id: Native.java 38554 2014-03-30 16:06:12Z steveq $$
 */
public class Native {

    public static String os = null;
    static {
	os = System.getProperty("os.name");
    }

    public static synchronized boolean exec(AppInfo appInfo, String command,
	    long timeout, StringBuilder outputBuffer, StringBuilder errorBuffer, 
	    boolean logErrors) throws TimeoutException, IOException {
	final boolean successful = 
		exec(command, timeout, appInfo.log, outputBuffer, errorBuffer);
	if (!successful && logErrors) {
	    appInfo.log.error("Error executing " + command + ". Details: \n"
		    + outputBuffer.toString() + ", " + errorBuffer.toString());
	}
	return successful;
    }

    public static synchronized boolean exec(String command, long timeout,
	    Logger log, StringBuilder outputBuffer, StringBuilder errorBuffer) 
		    throws TimeoutException, IOException {
	boolean success = false;
	Timer timer = null;
	InterruptTimerTask interrupter = null;
	Process process = null;
	InputStream inputStream = null;
	StreamConsumer errorOutputStreamConsumer = null;
	InputStream errorInputStream = null;
	StreamConsumer outputStreamConsumer = null;
	try {
	    log.debug("Native.exec(): " + command);
	    interrupter = new InterruptTimerTask(Thread.currentThread());
	    timer = new Timer(true);
	    timer.schedule(interrupter, timeout);
	    process = Runtime.getRuntime().exec(command);
	    inputStream = process.getInputStream();
	    outputStreamConsumer = 
		    new StreamConsumer(inputStream, outputBuffer);
	    errorInputStream = process.getErrorStream();
	    errorOutputStreamConsumer = 
		    new StreamConsumer(errorInputStream, errorBuffer);
	    outputStreamConsumer.start();
	    errorOutputStreamConsumer.start();

	    final int exitVal = process.waitFor();
	    log.debug("Exit value: " + exitVal + " for \"" + command + "\"");
	    if (exitVal == 0) {
		success = true;
	    }
	} catch (final InterruptedException e) {
	    throw new TimeoutException("Timeout for " + command + " after "
		    + timeout + "ms");
	} finally {
	    if (errorOutputStreamConsumer != null) {
		if (errorOutputStreamConsumer.isAlive()) {
		    errorOutputStreamConsumer.interrupt();
		}
		errorOutputStreamConsumer = null;
	    }
	    if (outputStreamConsumer != null) {
		if (outputStreamConsumer.isAlive()) {
		    outputStreamConsumer.interrupt();
		}
		outputStreamConsumer = null;
	    }
	    if (errorInputStream != null) {
		errorInputStream.close();
		errorInputStream = null;
	    }
	    if (inputStream != null) {
		inputStream.close();
		inputStream = null;
	    }

	    // WARNING: A Windows/Solaris JDK bug might allow this process
	    // to continue after it is destroyed!
	    if (process != null) {
		process.destroy();
	    }

	    if (timer != null) {
		timer.cancel();
		timer = null;
	    }

	    // If the process returns within the timeout period, we have to
	    // stop the interrupter so that it does not unexpectedly interrupt
	    // some other code later.
	    if (interrupter != null) {
		interrupter.cancel();
		interrupter = null;
	    }

	    // We need to clear the interrupt flag on the current thread just
	    // in case interrupter executed after waitFor had already returned
	    // but before timer.cancel took effect.
	    Thread.interrupted();
	}
	return success;
    }
}
