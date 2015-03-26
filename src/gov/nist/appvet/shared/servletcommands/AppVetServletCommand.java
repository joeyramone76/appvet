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
package gov.nist.appvet.shared.servletcommands;

import java.io.Serializable;

import com.google.gwt.user.client.rpc.IsSerializable;

public enum AppVetServletCommand {

	GET_APP_STATUS, 
	GET_APP_TOOLS_STATUS,
	GET_APP_TOOL_IDS,
	GET_ALL_TOOL_IDS,
	GET_TOOL_REPORT, 
	GET_APP_LOG, 
	GET_APPVET_LOG, 
	DOWNLOAD_REPORTS, 
	SUBMIT_APP, 
	SUBMIT_REPORT;

	private AppVetServletCommand() {
	}

	public static AppVetServletCommand getCommand(String cmdString) {
		if (cmdString != null) {
			for (AppVetServletCommand cmd : AppVetServletCommand.values()) {
				if (cmdString.equalsIgnoreCase(cmd.name())) {
					return cmd;
				}
			}
		}
		return null;
	}

}
