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

import java.text.DecimalFormat;


public class MemoryUtil {

    public synchronized static String getFreeHeap(String message) {
	return "Free heap (" + message + "): "
		+ readableFileSize(Runtime.getRuntime().freeMemory());
    }

    public synchronized static String getMaxHeap(String message) {
	return "Max heap size (" + message + ") "
		+ readableFileSize(Runtime.getRuntime().maxMemory());
    }

    public synchronized static String readableFileSize(long size) {
	if (size <= 0) {
	    return "0";
	}
	final String[] units = new String[] { "bytes", "Kb", "Mb", "Gb", "Tb" };
	final int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
	return new DecimalFormat("#,##0.#").format(size
		/ Math.pow(1024, digitGroups))
		+ " " + units[digitGroups];
    }

    public synchronized static String showHeap(String message) {
	return "Heap (" + message + "): "
		+ readableFileSize(Runtime.getRuntime().totalMemory());
    }

    private MemoryUtil() {
    }
}
