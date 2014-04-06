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

import gov.nist.appvet.gwt.client.gui.AppVetPanel;
import gov.nist.appvet.shared.validate.Validate;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * $$Id: UserInfoGwt.java 38554 2014-03-30 16:06:12Z steveq $$
 */
public class UserInfoGwt implements IsSerializable {

    private boolean newUser = false;
    
    //-------------- Updated by user/admin -------------
    private String userName = null;
    private String lastName = null;
    private String firstName = null;
    private String organization = null;
    private String email = null;
    private boolean changePassword = false;
    private String password = null;
    private String passwordAgain = null;
    private String role = null;
    
    //--------------- Updated only by AppVet --------------
    private String lastLogon = null;
    private String fromHost = null;

    public UserInfoGwt() {
    }

    public String getEmail() {
	return email;
    }

    public String getFirstName() {
	return firstName;
    }

    public String getFromHost() {
	return fromHost;
    }

    public String getFullName() {
	return firstName + " " + lastName;
    }

    public String getLastLogon() {
	return lastLogon;
    }

    public String getLastName() {
	return lastName;
    }

    public String getNameWithLastNameInitial() {
	return firstName + " " + lastName.substring(0, 1) + ".";
    }

    public String getOrganization() {
	return organization;
    }

    public String getPassword() {
	return password;
    }

    public String getRole() {
	return role;
    }

    public String getUserName() {
	return userName;
    }

    public boolean isChangePassword() {
	return changePassword;
    }

    public boolean isNewUser() {
	return newUser;
    }

    public void setChangePassword(boolean changePassword) {
	this.changePassword = changePassword;
    }

    public void setEmail(String email) {
	this.email = email;
    }

    public void setFirstName(String firstName) {
	this.firstName = firstName;
    }

    public void setFromHost(String fromHost) {
	this.fromHost = fromHost;
    }

    public void setLastLogon(String lastLogon) {
	this.lastLogon = lastLogon;
    }

    public void setLastName(String lastName) {
	this.lastName = lastName;
    }

    public void setNewUser(boolean newUser) {
	this.newUser = newUser;
    }

    public void setOrganization(String organization) {
	this.organization = organization;
    }

    public void setPassword(String password) {
	this.password = password;
    }

    public void setPasswords(String password, String passwordAgain) {
	this.password = password;
	this.passwordAgain = passwordAgain;
    }

    public void setRole(String role) {
	this.role = role;
    }

    public void setUserName(String username) {
	userName = username;
    }

    public String getPasswordAgain() {
	return passwordAgain;
    }

    public void setPasswordAgain(String passwordAgain) {
	this.passwordAgain = passwordAgain;
    }

    public boolean tokenMatch(String token) {
	String lowerCaseToken = token.toLowerCase();
	if (userName.toLowerCase().indexOf(lowerCaseToken) > -1) {
	    return true;
	}
	if (organization.toLowerCase().indexOf(lowerCaseToken) > -1) {
	    return true;
	}
	if (email.toLowerCase().indexOf(lowerCaseToken) > -1) {
	    return true;
	}
	if (role.toLowerCase().indexOf(lowerCaseToken) > -1) {
	    return true;
	}
	if (lastLogon.toLowerCase().indexOf(lowerCaseToken) > -1) {
	    return true;
	}
	if (fromHost.toLowerCase().indexOf(lowerCaseToken) > -1) {
	    return true;
	}
	if (lastName.toLowerCase().indexOf(lowerCaseToken) > -1) {
	    return true;
	}
	if (firstName.toLowerCase().indexOf(lowerCaseToken) > -1) {
	    return true;
	}
	return false;
    }

    public boolean isValid() {
	if (!Validate.isValidUserName(userName)) {
	    AppVetPanel.showMessageDialog("Account Setting Error",
		    "Invalid username", true);
	    return false;
	}
	if (!Validate.isAlpha(lastName)) {
	    AppVetPanel.showMessageDialog("Account Setting Error",
		    "Invalid last name", true);
	    return false;
	}
	if (!Validate.isAlpha(firstName)) {
	    AppVetPanel.showMessageDialog("Account Setting Error",
		    "Invalid first name", true);
	    return false;
	}
	if (!Validate.isPrintable(organization)) {
	    AppVetPanel.showMessageDialog("Account Setting Error",
		    "Invalid organization", true);
	    return false;
	}
	if (!Validate.isValidEmail(email)) {
	    AppVetPanel.showMessageDialog("Account Setting Error",
		    "Invalid email", true);
	    return false;
	}
	if (changePassword) {
	    if (!Validate.isValidPassword(password)) {
		AppVetPanel.showMessageDialog("Account Setting Error",
			"Invalid password", true);
		return false;
	    }
	    if (!password.equals(passwordAgain)) {
		AppVetPanel.showMessageDialog("Account Setting Error",
			"Passwords do not match", true);
		return false;
	    }
	}
	if (!Validate.isValidRole(role)) {
	    AppVetPanel.showMessageDialog("Account Setting Error", "Invalid role",
		    true);
	    return false;
	}
	return true;
    }
}
