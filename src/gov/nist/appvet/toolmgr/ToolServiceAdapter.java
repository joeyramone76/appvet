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
package gov.nist.appvet.toolmgr;

import gov.nist.appvet.properties.AppVetProperties;
import gov.nist.appvet.servlet.shared.SSLWrapper;
import gov.nist.appvet.shared.ErrorMessage;
import gov.nist.appvet.shared.Logger;
import gov.nist.appvet.shared.ReportFileType;
import gov.nist.appvet.shared.app.AppInfo;
import gov.nist.appvet.shared.status.ToolStatus;
import gov.nist.appvet.shared.status.ToolStatusManager;
import gov.nist.appvet.xml.XmlUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.util.PDFTextStripper;


public class ToolServiceAdapter implements Runnable {

	private String appvetResultHeaderName = null;
	public boolean reportPayload = false;
	private static final Logger log = AppVetProperties.log;
	public Thread thread = null;
	public AppInfo appInfo = null;
	public String name = null; // Display Name (e.g., Androwarn (Maaaaz))
	public String id = null; // ID used as a database table column name
	public String vendorName = null;
	public Protocol protocol = null;
	public String webSite = null;
	public String URL = null;
	public String method = null;
	public ArrayList<String> formParameterNames = null;
	public ArrayList<String> formParameterValues = null;
	public ReportFileType reportFileType = null;
	public String reportName = null;
	public ArrayList<String> securityPassPhrases = null;
	public ArrayList<String> securityWarningPhrases = null;
	public ArrayList<String> securityFailPhrases = null;
	public ArrayList<String> errorPhrases = null;
	public enum Protocol {

		SYNCHRONOUS, 
		ASYNCHRONOUS, 
		PUSH, 
		INTERNAL;

		public static Protocol getProtocol(String protocolName) {
			if (protocolName != null) {
				for (final Protocol m : Protocol.values()) {
					if (protocolName.equalsIgnoreCase(m.name())) {
						return m;
					}
				}
			}
			return null;
		}

	};

	public static ToolServiceAdapter getById(String toolId) {
		for (int i = 0; i < AppVetProperties.availableTools.size(); i++) {
			final ToolServiceAdapter adapter = AppVetProperties.availableTools.get(i);
			if (adapter.id.equals(toolId)) {
				return adapter;
			}
		}
		log.error("Tool id '" + toolId + "' does not exist!");
		return null;
	}


	public static String getHtmlReportString(String reportPath, AppInfo appInfo) {
		byte[] encoded = null;
		try {
			encoded = Files.readAllBytes(Paths.get(reportPath));
			return Charset.defaultCharset().decode(ByteBuffer.wrap(encoded))
					.toString();
		} catch (final IOException e) {
			appInfo.log.error(e.getMessage());
			return null;
		} finally {
			encoded = null;
		}
	}

	public static String getPdfReportString(String reportPath, AppInfo appInfo) {
		File file = new File(reportPath);
		PDDocument pddDocument = null;
		PDFTextStripper textStripper = null;
		try {
			pddDocument = PDDocument.load(file);
			textStripper = new PDFTextStripper();
			textStripper.setStartPage(1);
			textStripper.setEndPage(1);
			final String report = textStripper.getText(pddDocument);
			return report;
		} catch (final IOException e) {
			appInfo.log.error(e.getMessage());
			return null;
		} finally {
			if (pddDocument != null) {
				try {
					pddDocument.close();
					pddDocument = null;
				} catch (IOException e) {
					appInfo.log.error(e.getMessage());
				}
			}
			textStripper = null;
			file = null;
		}
	}

	public static String getTextReportString(String reportPath, AppInfo appInfo) {
		byte[] encoded = null;
		try {
			encoded = Files.readAllBytes(Paths.get(reportPath));
			return Charset.defaultCharset().decode(ByteBuffer.wrap(encoded))
					.toString();
		} catch (final IOException e) {
			appInfo.log.error(e.getMessage());
			return null;
		} finally {
			encoded = null;
		}
	}

	public ToolServiceAdapter(File configFile) {
		if (!configFile.exists()) {
			log.error("Test service config file "
					+ configFile.getName() + " does not exist.");
			return;
		}
		XmlUtil xml = new XmlUtil(configFile);
		final String configFileName = configFile.getName();

		// Tool configuration
		name = xml.getXPathValue("/ToolServiceAdapter/Description/Name");
		checkNullString(configFileName, "name", name);
		id = xml.getXPathValue("/ToolServiceAdapter/Description/Id");
		checkNullString(configFileName, "id", id);
		vendorName = xml.getXPathValue("/ToolServiceAdapter/Description/VendorName");
		webSite = xml.getXPathValue("/ToolServiceAdapter/Description/VendorWebsite");
		// Report configuration  
		final String reportFileTypeString = xml.getXPathValue("/ToolServiceAdapter/Description/ReportFile");
		checkNullString(configFileName, "reportFileTypeString",
				reportFileTypeString);
		reportFileType = ReportFileType.getFileType(reportFileTypeString);
		reportName = getReportName();
		
		// Protocol config
		final String protocolName = xml.getXPathValue("/ToolServiceAdapter/Protocol/Type");
		checkNullString(configFileName, "protocolName", protocolName);
		protocol = Protocol.getProtocol(protocolName);
		String protocolXPath = "/ToolServiceAdapter/Protocol";
		switch (protocol) {
		case SYNCHRONOUS:
			protocolXPath += "/Synchronous";
			checkNullString(configFileName, "protocolXPath", protocolXPath);
			// HTTPRequestType
			URL = xml.getXPathValue(protocolXPath + "/Request/URL");
			checkNullString(configFileName, "URL", URL);
			method = xml.getXPathValue(protocolXPath + "/Request/Method");
			checkNullString(configFileName, "method", method);			
			formParameterNames = xml.getXPathValues(protocolXPath
					+ "/Request/Parameter/Name");
			checkNullArrayList(configFileName, "formParameterNames",
					formParameterNames);
			formParameterValues = xml.getXPathValues(protocolXPath
					+ "/Request/Parameter/Value");
			checkNullArrayList(configFileName, "formParameterValues",
					formParameterValues);
			// HTTPResponseType
			appvetResultHeaderName = xml.getXPathValue(protocolXPath
					+ "/Response/AppVetRiskHeaderName");
			String reportPayloadString = xml.getXPathValue(protocolXPath
					+ "/Response/ReportPayload");
			reportPayload = new Boolean(reportPayloadString).booleanValue();
			break;
		case ASYNCHRONOUS:
			protocolXPath += "/Asynchronous";
			checkNullString(configFileName, "protocolXPath", protocolXPath);
			// HTTPRequestType
			URL = xml.getXPathValue(protocolXPath + "/Request/URL");
			checkNullString(configFileName, "URL", URL);
			method = xml.getXPathValue(protocolXPath + "/Request/Method");
			checkNullString(configFileName, "method", method);			
			formParameterNames = xml.getXPathValues(protocolXPath
					+ "/Request/Parameter/Name");
			checkNullArrayList(configFileName, "formParameterNames",
					formParameterNames);
			formParameterValues = xml.getXPathValues(protocolXPath
					+ "/Request/Parameter/Value");
			checkNullArrayList(configFileName, "formParameterValues",
					formParameterValues);
			// HTTPResponseType (not used for asynchronous services)						
			break;
		case PUSH:
			protocolXPath += "/Push";
			break;
		case INTERNAL:
			protocolXPath += "/Internal";
			break;
		}

		xml = null;
	}

	public void checkNullArrayList(String fileName, String parameter,
			ArrayList<String> value) {
		if ((value == null) || value.isEmpty()) {
			log.error("Required parameter '" + parameter
					+ "' in file " + fileName
					+ " is null or empty.");
		}
	}

	public void checkNullString(String fileName, String parameter, String value) {
		if ((value == null) || value.isEmpty()) {
			log.error("Required parameter '" + parameter
					+ "' in file " + fileName
					+ " is null or empty.");
		}
	}

	public void cleanUp(AppInfo appInfo, boolean sendMobilizeReport) {
		if ((protocol == Protocol.PUSH) || (protocol == Protocol.INTERNAL)) {
			// PUSH adapters should not have a thread to clean up.
			return;
		}
		if (thread.isAlive()) {
			appInfo.log.debug("Thread for " + name
					+ " is still alive.  Interrupting...");
			thread.interrupt();
			appInfo.log.error(ErrorMessage.TOOL_TIMEOUT_ERROR.getDescription());
			ToolStatusManager.setToolStatus(appInfo.appId, this.id, ToolStatus.ERROR);
		}
		thread = null;
	}

	public MultipartEntity getMultipartEntity() {
		final MultipartEntity entity = new MultipartEntity();
		File apkFile = null;
		FileBody fileBody = null;
		try {
			String fileUploadParamName = null;
			// Add parameter name-value pairs
			if (formParameterNames != null) {
				for (int i = 0; i < formParameterNames.size(); i++) {
					final String paramName = formParameterNames.get(i);
					String paramValue = formParameterValues.get(i);
					if (paramValue.equals("APP_FILE")) {
						fileUploadParamName = paramName;
					} else {
						if (paramValue.equals("APPVET_DEFINED")) {
							if (paramName.equals("appid")) {
								appInfo.log.debug("Found " + paramName + " = "
										+ "'APPVET_DEFINED' for tool '" + id
										+ "'. Setting to appid = '"
										+ appInfo.appId + "'");
								paramValue = appInfo.appId;
							} else {
								appInfo.log
								.error("Found "
										+ paramName
										+ " = "
										+ "'APPVET_DEFINED' for tool '"
										+ id
										+ "' but no actual value is set by AppVet. Aborting.");
								return null;
							}
						}
						if ((paramName == null) || paramName.isEmpty()) {
							appInfo.log.warn("Param name is null or empty "
									+ "for tool '" + name + "'");
						} else if ((paramValue == null) || paramValue.isEmpty()) {
							appInfo.log.warn("Param value is null or empty "
									+ "for tool '" + name + "'");
						}
						StringBody partValue = new StringBody(paramValue,
								Charset.forName("UTF-8"));
						entity.addPart(paramName, partValue);
						partValue = null;
					}
				}
			}
			final String apkFilePath = appInfo.getIdPath() + "/"
					+ appInfo.fileName;
			appInfo.log.debug("Sending file: " + apkFilePath);
			apkFile = new File(apkFilePath);
			fileBody = new FileBody(apkFile);
			entity.addPart(fileUploadParamName, fileBody);
			return entity;
		} catch (final UnsupportedEncodingException e) {
			appInfo.log.error(e.getMessage());
			return null;
		}
	}

	private String getReportName() {
		final String reportSuffix = "_security_report";
		switch (reportFileType) {
		case TXT:
			return id + reportSuffix + ".txt";
		case HTML:
			return id + reportSuffix + ".html";
		case PDF:
			return id + reportSuffix + ".pdf";
		case RTF:
			return id + reportSuffix + ".rtf";
		case XML:
			return id + reportSuffix + ".xml";
		default:
			return null;
		}
	}

	public Thread getThread() {
		thread = new Thread(this);
		return thread;
	}

	@Override
	public void run() {
		if ((protocol == Protocol.PUSH) || (protocol == Protocol.INTERNAL)) {
			// PUSH/INTERNAL adapters do not send requests to a service.
			return;
		}
		if (appInfo == null) {
			log.error("AppInfo object is null in tool adapter. "
					+ "Aborting processing app.");
			return;
		}
		try {
			Date startDate = new Date();
			final long startTime = startDate.getTime();
			startDate = null;
			ToolStatusManager.setToolStatus(appInfo.appId, this.id, ToolStatus.SUBMITTED);
			HttpParams httpParameters = new BasicHttpParams();
			HttpConnectionParams.setConnectionTimeout(httpParameters,
					AppVetProperties.CONNECTION_TIMEOUT);
			HttpConnectionParams.setSoTimeout(httpParameters,
					AppVetProperties.SO_TIMEOUT);
			HttpClient httpclient = new DefaultHttpClient(httpParameters);
			httpclient = SSLWrapper.wrapClient(httpclient);
			MultipartEntity entity = getMultipartEntity();
			if (entity == null) {
				appInfo.log.error("MultipartEntity is null. Aborting " + name);
				ToolStatusManager.setToolStatus(appInfo.appId, this.id, ToolStatus.ERROR);
				return;
			}
			HttpPost httpPost = new HttpPost(URL);
			httpPost.setEntity(entity);
			appInfo.log.info(name + " adapter sending app " + appInfo.appId
					+ " to " + URL);

			// Send the app to the tool
			final HttpResponse response = httpclient.execute(httpPost);
			httpPost = null;
			appInfo.log.info(name + " adapter received: "
					+ response.getStatusLine());

			if (protocol == Protocol.SYNCHRONOUS) {
				// We only handle report from Synchronous tools here. Reports for
				// asynchronous tools are handled by the AppVetServlet.
				final HttpEntity responseEntity = response.getEntity();
				final InputStream inputStream = responseEntity.getContent();
				final String reportPath = appInfo.getReportsPath() + "/"
						+ getReportName();
				File fileOut = new File(reportPath);
				FileOutputStream fileOutputStream = new FileOutputStream(
						fileOut, false);
				int c;
				while ((c = inputStream.read()) != -1) {
					fileOutputStream.write(c);
				}
				fileOutputStream.flush();
				inputStream.close();
				fileOutputStream.close();
				fileOutputStream = null;
				fileOut = null;

				String toolResult = 
						response.getFirstHeader(appvetResultHeaderName).getValue();
				appInfo.log.debug("Received tool result: " + toolResult + " from " + this.id);
				
				ToolStatus toolStatus = ToolStatus.getStatus(toolResult);
				
				if (toolStatus == null) {
					appInfo.log.error("Error "
							+ "reading report");
					ToolStatusManager.setToolStatus(appInfo.appId, this.id, ToolStatus.ERROR);
					return;
				} else {
					ToolStatusManager.setToolStatus(appInfo.appId, this.id, toolStatus);
				}

			} else if (protocol == Protocol.ASYNCHRONOUS) {

				final String httpResponseVal = response.getStatusLine()
						.toString();
				if ((httpResponseVal.indexOf("HTTP/1.1 202 Accepted") > -1)
						|| (httpResponseVal.indexOf("HTTP/1.1 200 OK") > -1)) {
					appInfo.log.info("Asynchronous Kryptowire received: " + httpResponseVal);
				} else {
					appInfo.log.error("Tool '" + id + "' received: "
							+ httpResponseVal
							+ ". Could not process app. Aborting");
					ToolStatusManager.setToolStatus(appInfo.appId, this.id, ToolStatus.ERROR);

				}
			}

			entity = null;
			httpclient = null;
			httpParameters = null;
			Date endDate = new Date();
			final long endTime = endDate.getTime();
			endDate = null;
			final long elapsedTime = endTime - startTime;
			appInfo.log.info(name + " elapsed: "
					+ Logger.formatElapsed(elapsedTime));
		} catch (final Exception e) {
			e.printStackTrace();
			appInfo.log.error(e.getMessage());
			ToolStatusManager.setToolStatus(appInfo.appId, this.id, ToolStatus.ERROR);
		}
	}

	public void setApp(AppInfo appInfo) {
		this.appInfo = appInfo;
	}
}
