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
package gov.nist.appvet.shared.app;

import gov.nist.appvet.shared.os.DeviceOS;
import gov.nist.appvet.shared.status.AppStatus;

import java.io.Serializable;


public class AppInfoBase implements Serializable {
    private static final long serialVersionUID = 1L;
    public String appId = null;
    public String packageName = null;
    public String versionCode = null;
    public String versionName = null;
    public DeviceOS os = null;
    // Note that if the original fileItem name contains whitespace then
	// appFileName will replace those spaces with underscore characters.*/
    public String appFileName = null;
    // The file name of the app minus the file extension. Note that the app
    // name uses underscores in place of spaces.
    public String appName = null;
    
    public long submitTime = 0;
    public AppStatus appStatus = null;
    public long statusTime = 0;
    public String userName = null;
    public String clientHost = null;

    public AppInfoBase() {
    }

    public boolean tokenMatch(String token) {
	final String lowerCaseToken = token.toLowerCase();
	if (appId.toLowerCase().indexOf(lowerCaseToken) > -1) {
	    return true;
	}
	if (appName.toLowerCase().indexOf(lowerCaseToken) > -1) {
	    return true;
	}
	if (packageName.toLowerCase().indexOf(lowerCaseToken) > -1) {
	    return true;
	}
	if (versionCode.toLowerCase().indexOf(lowerCaseToken) > -1) {
	    return true;
	}
	if (versionName.toLowerCase().indexOf(lowerCaseToken) > -1) {
	    return true;
	}
    if (os.name().toLowerCase().indexOf(lowerCaseToken) > -1) {
        return true;
    }
	if (appFileName.toLowerCase().indexOf(lowerCaseToken) > -1) {
	    return true;
	}
	if (appStatus.name().toLowerCase().indexOf(lowerCaseToken) > -1) {
	    return true;
	}
	if (userName.toLowerCase().indexOf(lowerCaseToken) > -1) {
	    return true;
	}

	return false;
    }
}
