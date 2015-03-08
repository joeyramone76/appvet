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
package gov.nist.appvet.gwt.shared;

import com.google.gwt.user.client.rpc.IsSerializable;


public class ConfigInfoGwt implements IsSerializable {

    private UserInfoGwt userInfo = null;
    private String hostname = null;
    private String appVetHostUrl = null;
    private String appVetUrl = null;
    private String appVetServletUrl = null;
    private String appVetVersion = null;
//    private String appVetBuild = null;
//    private String lastUpdated = null;
    private String sessionId = null;
    private long maxIdleTime = 0;
    private int getUpdatesDelay = 0;
    private long sessionExpirationLong = 0;
    
    // Android tools
    private String[] androidToolNames = null;
    private String[] androidToolIDs = null;
    private String[] androidToolTypes = null;
    
    // iOS tools
    private String[] iOSToolNames = null;
    private String[] iOSToolIDs = null;
    private String[] iOSToolTypes = null;
    
    private String systemMessage = null;

    public ConfigInfoGwt() {
    }

//    public String getAppVetBuild() {
//	return appVetBuild;
//    }

    public String getAppVetHostUrl() {
	return appVetHostUrl;
    }

    public String getAppVetServletUrl() {
	return appVetServletUrl;
    }

    public String getAppVetUrl() {
	return appVetUrl;
    }

    public String getAppVetVersion() {
	return appVetVersion;
    }

    public String[] getAndroidToolNames() {
	return androidToolNames;
    }

    public String[] getAndroidToolTypes() {
	return androidToolTypes;
    }

    public String getHostname() {
	return hostname;
    }

//    public String getLastUpdated() {
//	return lastUpdated;
//    }

    public long getMaxIdleTime() {
	return maxIdleTime;
    }

    public long getSessionExpirationLong() {
	return sessionExpirationLong;
    }

    public String getSessionId() {
	return sessionId;
    }

    public String getSystemMessage() {
	return systemMessage;
    }

    public int getUpdatesDelay() {
	return getUpdatesDelay;
    }

    public UserInfoGwt getUserInfo() {
	return userInfo;
    }

//    public void setAppVetBuild(String appvetBuild) {
//	this.appVetBuild = appvetBuild;
//    }

    public void setAppVetHostUrl(String appvetHostUrl) {
	this.appVetHostUrl = appvetHostUrl;
    }

    public void setAppVetServletUrl(String appvetServletUrl) {
	this.appVetServletUrl = appvetServletUrl;
    }

    public void setAppVetUrl(String appvetUrl) {
	this.appVetUrl = appvetUrl;
    }

    public void setAppVetVersion(String appvetVersion) {
	this.appVetVersion = appvetVersion;
    }

    public void setAndroidToolNames(String[] androidToolNames) {
	this.androidToolNames = androidToolNames;
    }

    public void setAndroidToolType(String[] androidToolTypes) {
	this.androidToolTypes = androidToolTypes;
    }

    public void setGetUpdatesDelay(int value) {
	getUpdatesDelay = value;
    }

    public void setHostname(String hostname) {
	this.hostname = hostname;
    }

//    public void setLastUpdated(String lastUpdated) {
//	this.lastUpdated = lastUpdated;
//    }

    public void setMaxIdleTime(long maxIdleTime) {
	this.maxIdleTime = maxIdleTime;
    }

    public void setSessionExpirationLong(long sessionExpirationLong) {
	this.sessionExpirationLong = sessionExpirationLong;
    }

    public void setSessionId(String sessionId) {
	this.sessionId = sessionId;
    }

    public void setSystemMessage(String systemMessage) {
	this.systemMessage = systemMessage;
    }

    public void setUserInfo(UserInfoGwt userInfo) {
	this.userInfo = userInfo;
    }

	public String[] getAndroidToolIDs() {
		return androidToolIDs;
	}

	public void setAndroidToolIDs(String[] androidToolIDs) {
		this.androidToolIDs = androidToolIDs;
	}

	public String[] getiOSToolNames() {
		return iOSToolNames;
	}

	public void setiOSToolNames(String[] iOSToolNames) {
		this.iOSToolNames = iOSToolNames;
	}

	public String[] getiOSToolIDs() {
		return iOSToolIDs;
	}

	public void setiOSToolIDs(String[] iOSToolIDs) {
		this.iOSToolIDs = iOSToolIDs;
	}

	public String[] getiOSToolTypes() {
		return iOSToolTypes;
	}

	public void setiOSToolTypes(String[] iOSToolTypes) {
		this.iOSToolTypes = iOSToolTypes;
	}
}
