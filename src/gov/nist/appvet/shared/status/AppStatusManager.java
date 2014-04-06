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
