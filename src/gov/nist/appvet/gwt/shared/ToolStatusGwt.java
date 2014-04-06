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

/**
 * $$Id: ToolStatusGwt.java 38554 2014-03-30 16:06:12Z steveq $$
 */
public class ToolStatusGwt implements IsSerializable {

	private String appId = null;
	private String tool = null;
	private String statusDescription = null;
	private String report = null;

	public ToolStatusGwt() {
	}

	public String getAppId() {
		return appId;
	}

	public String getReport() {
		return report;
	}

	public String getStatusDescription() {
		return statusDescription;
	}

	public String getTool() {
		return tool;
	}

	public void setAppId(String appId) {
		this.appId = appId;
	}

	public void setReport(String report) {
		this.report = report;
	}

	public void setStatusDescription(String desc) {
		statusDescription = desc;
	}

	public void setTool(String tool) {
		this.tool = tool;
	}

}
