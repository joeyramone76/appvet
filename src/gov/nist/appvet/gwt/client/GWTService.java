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
package gov.nist.appvet.gwt.client;

import gov.nist.appvet.gwt.shared.AppInfoGwt;
import gov.nist.appvet.gwt.shared.ConfigInfoGwt;
import gov.nist.appvet.gwt.shared.ToolStatusGwt;
import gov.nist.appvet.gwt.shared.UserInfoGwt;

import java.util.List;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

/**
 * $$Id: GWTService.java 38554 2014-03-30 16:06:12Z steveq $$
 */
@RemoteServiceRelativePath("greet")
public interface GWTService extends RemoteService {

    List<UserInfoGwt> adminSetUser(UserInfoGwt userInfo)
	    throws IllegalArgumentException;

    ConfigInfoGwt authenticate(String username, String password)
	    throws IllegalArgumentException;

    Boolean deleteApp(String appId, String username)
	    throws IllegalArgumentException;

    Boolean deleteUser(String username) throws IllegalArgumentException;

    List<AppInfoGwt> getAllApps(String username)
	    throws IllegalArgumentException;

    List<ToolStatusGwt> getToolResults(String sessionId, String appId)
	    throws IllegalArgumentException;

    List<AppInfoGwt> getUpdatedApps(long lastClientUpdate, String username)
	    throws IllegalArgumentException;

    List<UserInfoGwt> getUsersList() throws IllegalArgumentException;

    Boolean removeSession(String sessionId) throws IllegalArgumentException;

    Boolean updateSessionTimeout(String sessionId, long sessionTimeout)
	    throws IllegalArgumentException;

    boolean updateSelf(UserInfoGwt userInfo) throws IllegalArgumentException;

}
