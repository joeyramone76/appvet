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
package gov.nist.appvet.shared.status;

import gov.nist.appvet.properties.AppVetProperties;
import gov.nist.appvet.shared.Database;
import gov.nist.appvet.shared.Logger;

public class AppStatusManager {

    private static final Logger log = AppVetProperties.log;

	private AppStatusManager() {
	}
	
    public synchronized static void setAppStatus(String appId,
    	    AppStatus appStatus) {
    	final String currentStatusString = getAppStatusName(appId);
    	final AppStatus currentStatus = AppStatus.getStatus(currentStatusString);
    	if (appStatus != currentStatus) {
    	    final String sql = "UPDATE apps SET appstatus='"
    		    + appStatus.name() + "' where appid='" + appId
    		    + "'";
    	    if (Database.update(sql)) {
    	    	Database.setLastUpdate(appId);
    	    }
    	}
    	log.debug("APPSTATUS -> " + appStatus.name());
    	return;
        }

	private synchronized static String getAppStatusName(String appid) {
		return Database.getString("SELECT appstatus FROM apps " 
				+ "where appid='" + appid + "'");
	}
	
	public synchronized static AppStatus getAppStatus(String appid) {
		String appStatusStr = getAppStatusName(appid);
		return AppStatus.getStatus(appStatusStr);
	}
}
