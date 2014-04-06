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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;


public class StreamConsumer extends Thread {

    private InputStream inputStream = null;
    private InputStreamReader inputStreamReader = null;
    private BufferedReader bufferedReader = null;
    private StringBuilder stringBuffer = null;

    public StreamConsumer(InputStream inputStream,
	    StringBuilder stringBuffer) {
	this.inputStream = inputStream;
	this.inputStreamReader = new InputStreamReader(inputStream);
	this.bufferedReader = new BufferedReader(inputStreamReader);
	this.stringBuffer = stringBuffer;
    }

    @Override
    public void run() {
	try {
	    if (inputStream == null) {
		if (stringBuffer != null) {
		    stringBuffer.append("StreamConsumer input is null");
		}
		return;
	    }
	    String line;
	    // This loop will break if Native.exec() is destroyed.
	    while ((line = bufferedReader.readLine()) != null) {
		if (stringBuffer != null) {
		    stringBuffer.append(line + "\n");
		}
	    }
	} catch (final IOException e) {
	    if (stringBuffer != null) {
		stringBuffer.append("IOException in StreamConsumer: "
			+ e.getMessage());
	    }
	} finally {
	    try {
		if (bufferedReader != null) {
		    bufferedReader.close();
		    bufferedReader = null;
		}
		if (inputStreamReader != null) {
		    inputStreamReader.close();
		    inputStreamReader = null;
		}
		if (inputStream != null) {
		    inputStream.close();
		    inputStream = null;
		}
	    } catch (final IOException e) {
		if (stringBuffer != null) {
		    stringBuffer
		    .append("IOException in StreamConsumer.finally(): "
			    + e.getMessage());
		}
	    }
	}
    }
}
