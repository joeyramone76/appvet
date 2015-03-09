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
package gov.nist.appvet.tools.preprocessor;

import gov.nist.appvet.properties.AppVetProperties;
import gov.nist.appvet.shared.FileUtil;
import gov.nist.appvet.shared.Logger;
import gov.nist.appvet.shared.app.AppInfo;
import gov.nist.appvet.shared.status.ToolStatus;
import gov.nist.appvet.shared.status.ToolStatusManager;
import gov.nist.appvet.toolmgr.ToolServiceAdapter;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class IOSMetadata {
	
	public static synchronized boolean getIosMetaData(AppInfo appInfo, Logger log) {
		log.debug("Start iOS Manifest preprocessing for appID="
				+ appInfo.appId);
		final ToolServiceAdapter appinfoTool = ToolServiceAdapter
				.getByToolId(appInfo.os, "appinfo");
		final String reportsPath = appInfo.getReportsPath();
		final String appinfoReportPath = reportsPath + "/"
				+ appinfoTool.reportName;

		BufferedWriter appinfoReport = null;

		
		try {
			boolean iconFound = false;
			final String ipaFilePath = appInfo.getIdPath() + "/" + appInfo.appFileName;
			File ipaFile = new File(ipaFilePath);
			String zipFilePath = appInfo.getIdPath() + "/" + appInfo.appName + ".zip";
			File destFile = new File(zipFilePath);
			FileUtil.copyFile(ipaFile, destFile);
			String destIconPath = AppVetProperties.APP_IMAGES + "/" + appInfo.appId + ".png";
			ZipFile zipFile = new ZipFile(destFile);
			Enumeration<? extends ZipEntry> apkFileEntries = zipFile.entries();
			log.debug("READING ZIP FILE: " + zipFile.getName());
			try {
				// Check if there is an iTunesArtwork file. If there isn't, 
				// we will need to scan below for a png file.
				while (apkFileEntries.hasMoreElements()) {
					ZipEntry entry = apkFileEntries.nextElement();
					//log.debug("ZIP ENTRY: " + entry.getName());
					if (entry.getName().indexOf("iTunesArtwork") > -1) {
						log.debug("ICON FOUND!: " + entry.getName());
						iconFound = true;
						InputStream inputStream = zipFile.getInputStream(entry);
						
						int numBytesRead;
						byte[] byteArray = new byte[1024];						
						FileOutputStream fos = 
								new FileOutputStream(destIconPath);
						
						while ((numBytesRead = inputStream.read(byteArray, 0,
								byteArray.length)) != -1) {
							fos.write(byteArray, 0, numBytesRead);
						}
						inputStream.close();
						fos.close();
						break;
					}
				}
				
				if (!iconFound) {
					// Look for the first png file
					while (apkFileEntries.hasMoreElements()) {
						ZipEntry entry = apkFileEntries.nextElement();
						log.debug("ZIP ENTRY: " + entry.getName());
						if (entry.getName().toLowerCase().indexOf(".png") > -1) {
							log.debug("ICON FOUND!: " + entry.getName());
							iconFound = true;
							InputStream inputStream = zipFile.getInputStream(entry);
							
							int numBytesRead;
							byte[] byteArray = new byte[1024];						
							FileOutputStream fos = 
									new FileOutputStream(destIconPath);
							
							while ((numBytesRead = inputStream.read(byteArray, 0,
									byteArray.length)) != -1) {
								fos.write(byteArray, 0, numBytesRead);
							}
							inputStream.close();
							fos.close();
							break;
						}
					}
					
				}
				
				// Couldn't find icon, so just use default question mark
				if (!iconFound) {
					log.debug("Couldnt find icon. Using default icon.");
					File sourceIcon = new File(AppVetProperties.APP_IMAGES + "/default.png");
					File destIcon = new File(AppVetProperties.APP_IMAGES + "/" + appInfo.appId + ".png");
					FileUtil.copyFile(sourceIcon, destIcon);
				}
				zipFile.close();
				FileUtil.deleteFile(zipFilePath);

			} catch (IOException e) {
				e.printStackTrace();
			}

			appinfoReport = new BufferedWriter(
					new FileWriter(appinfoReportPath));
			
			appinfoReport.write("<HTML>\n");
			appinfoReport.write("<head>\n");
			appinfoReport.write("<style type=\"text/css\">\n");
			appinfoReport.write("h3 {font-family:arial;}\n");
			appinfoReport.write("p {font-family:arial;}\n");
			appinfoReport.write("</style>\n");
			appinfoReport.write("<title>iOS Manifest Report</title>\n");
			appinfoReport.write("</head>\n");
			appinfoReport.write("<body>\n");
			String appVetImagesUrl = AppVetProperties.URL + "/images/appvet_logo.png";
			appinfoReport.write("<img border=\"0\" width=\"192px\" src=\"" + appVetImagesUrl + "\" alt=\"appvet\" />");
			appinfoReport.write("<HR>\n");
			appinfoReport.write("<h3>iOS Metadata Pre-Processing Report</h3>\n");
			appinfoReport.write("<pre>\n");
			final Date date = new Date();
			final SimpleDateFormat format = new SimpleDateFormat(
					"yyyy-MM-dd' 'HH:mm:ss.SSSZ");
			final String currentDate = format.format(date);
			appinfoReport.write("File: \t\t" + appInfo.appFileName + "\n");
			appinfoReport.write("Date: \t\t" + currentDate + "\n\n");
			appinfoReport.write("App ID: \t" + appInfo.appId + "\n");
			
			final String fileNameUpperCase = appInfo.appFileName.toUpperCase();
			if (fileNameUpperCase.endsWith(".IPA")) {
				ToolStatusManager.setToolStatus(appInfo.os, appInfo.appId, appinfoTool.id, ToolStatus.SUBMITTED);
//				if (!AndroidMetadata.decodeApk(appInfo, appinfoReport)) {
//					return false;
//				}
//				if (!AndroidMetadata.getManifestInfo(appInfo, appinfoReport, true)) {
//					return false;
//				}			
			} else {
//				appInfo.log.error("File " + appInfo.appFileName + " is invalid");
				ToolStatusManager.setToolStatus(appInfo.os, appInfo.appId, appinfoTool.id, ToolStatus.ERROR);
				return false;
			}
			
			ToolStatusManager.setToolStatus(appInfo.os, appInfo.appId, appinfoTool.id, ToolStatus.PASS);
			appinfoReport
			.write("\nStatus:\t\t<font color=\"green\">PASS</font>\n");
			log.debug("End iOS metadata preprocessing for appID="
					+ appInfo.appId);
			
			return true;

		} catch (final IOException e) {
			appInfo.log.error(e.getMessage());
			return false;
		} finally {
			try {
				if (appinfoReport != null) {
					appinfoReport.write("</pre>\n");
					appinfoReport.write("</body>\n");
					appinfoReport.write("</HTML>\n");
					appinfoReport.close();
					appinfoReport = null;
				}
			} catch (final IOException e) {
				appInfo.log.error(e.getMessage());
			}
		}
	}

//	public static synchronized boolean decodeApk(AppInfo appInfo,
//			String apkPath, BufferedWriter appinfoReport) {
//		String os = Native.os;
//		String apktoolCommand = null;
//		if (os.toLowerCase().indexOf("win") > -1) {
//			apktoolCommand = APKTOOL_WINDOWS_COMMAND;
//		} else if (os.toLowerCase().indexOf("nux") > -1) {
//			apktoolCommand = APKTOOL_LINUX_COMMAND;
//		}
//		final String decodeCmd = apktoolCommand
//				+ " d "
//				+ apkPath
//				+ " "
//				+ appInfo.appName;
//		try {
//			final StringBuilder outputBuffer = new StringBuilder();
//			final StringBuilder errorBuffer = new StringBuilder();
//			if (Native.exec(appInfo, decodeCmd, 120000, outputBuffer, errorBuffer, true)) {
//				appInfo.log.info("Decoded APK:\tOK");
//				return true;
//			} else {
//				//appInfo.log.error("OutputBuffer: " + outputBuffer.toString() + "|");
//				//appInfo.log.error("ErrorBuffer: " + errorBuffer.toString() + "||");
//
//				if (outputBuffer.indexOf("FileNotFound") >= 0 ||
//						outputBuffer.indexOf("was not found or was not readable") >= 0) {
//					// Anti-virus on system may have removed app if it was malware
//					appInfo.log.error(outputBuffer.toString());
//					appinfoReport.write("\n<font color=\"red\">"
//							+ ErrorMessage.FILE_NOT_FOUND.getDescription()
//							+ " (File removed by system; file may be malware)</font>");
//					appInfo.log.error(ErrorMessage.FILE_NOT_FOUND.getDescription());
//					ToolStatusManager.setToolStatus(appInfo.os, appInfo.appId, appinfoTool.id,
//							ToolStatus.ERROR);
//				} else {
//					appInfo.log.error(outputBuffer.toString());
//					appinfoReport.write("\n<font color=\"red\">"
//							+ ErrorMessage.ANDROID_APK_DECODE_ERROR.getDescription()
//							+ " (File may be corrupted)</font>");
//					appInfo.log.error(ErrorMessage.ANDROID_APK_DECODE_ERROR.getDescription());
//					ToolStatusManager.setToolStatus(appInfo.os, appInfo.appId, appinfoTool.id,
//							ToolStatus.ERROR);
//				}
//
//				return false;
//			}
//		} catch (final TimeoutException e) {
//			appInfo.log.error(e.getMessage());
//			ToolStatusManager.setToolStatus(appInfo.os, appInfo.appId, appinfoTool.id,
//					ToolStatus.ERROR);
//			try {
//				appinfoReport.write("\n<font color=\"red\">"
//						+ ErrorMessage.ANDROID_APK_DECODE_ERROR.getDescription()
//						+ "</font>");
//			} catch (final IOException e1) {
//				appInfo.log.error(e1.getMessage());
//			}
//			return false;
//		} catch (final IOException e) {
//			appInfo.log.error(e.getMessage());
//			ToolStatusManager.setToolStatus(appInfo.os, appInfo.appId, appinfoTool.id,
//					ToolStatus.ERROR);
//			try {
//				appinfoReport.write("\n<font color=\"red\">"
//						+ ErrorMessage.ANDROID_APK_DECODE_ERROR.getDescription()
//						+ "</font>");
//			} catch (final IOException e1) {
//				appInfo.log.error(e1.getMessage());
//			}
//			return false;
//		}
//	}
//
//	public static synchronized void getElementValues(AppInfo appInfo,
//			File xmlFile, BufferedWriter appinfoReport) {
//		final XmlUtil xml = new XmlUtil(xmlFile);
//		appInfo.packageName = xml.getXPathValue("/manifest/@package");
//		print(appInfo, "Package name", appInfo.packageName, appinfoReport);
//		appInfo.versionCode = xml.getXPathValue("/manifest/@versionCode");
//		print(appInfo, "Version code", appInfo.versionCode, appinfoReport);
//		appInfo.versionName = xml.getXPathValue("/manifest/@versionName");
//		print(appInfo, "Version name", appInfo.versionName, appinfoReport);
//		Database.updateApp(appInfo.appId, appInfo.appName,
//				appInfo.packageName, appInfo.versionCode, appInfo.versionName);
//		String minSdk = xml.getXPathValue("/manifest/uses-sdk/@minSdkVersion");
//		print(appInfo, "Min SDK", minSdk, appinfoReport);
//		String maxSdk = xml.getXPathValue("/manifest/uses-sdk/@maxSdkVersion");
//		print(appInfo, "Max SDK", maxSdk, appinfoReport);
//		String targetSdk = xml
//				.getXPathValue("/manifest/uses-sdk/@targetSdkVersion");
//		print(appInfo, "Target SDK", targetSdk, appinfoReport);
//		final String iconValue = xml
//				.getXPathValue("/manifest/application/@icon");
//		setIcon(appInfo, iconValue);
//	}
//
//	public static synchronized File getIcon(String resFolderPath,
//			String iconDirectoryName, String iconName) {
//		File resDirectory = new File(resFolderPath);
//		File[] resFiles = resDirectory.listFiles();
//		File[] iconDirectoryFiles = null;
//		try {
//			for (final File resFile : resFiles) {
//				if (resFile.isDirectory()) {
//					// Icon could be in one of several directories containing
//					// <iconDirectoryName>
//					if (resFile.getName().contains(iconDirectoryName)) {
//						iconDirectoryFiles = resFile.listFiles();
//						for (final File iconFile : iconDirectoryFiles) {
//							if (iconFile.getName().equals(iconName + ".png")) {
//								return iconFile;
//							}
//						}
//					}
//				}
//			}
//			return null; 
//		} finally {
//			iconDirectoryFiles = null;
//			resFiles = null;
//			resDirectory = null;
//		}
//	}
//
//	public static synchronized boolean getManifestInfo(AppInfo appInfo,
//			BufferedWriter appinfoReport, boolean apktoolGenerated) {
//		final String projectPath = appInfo.getProjectPath();
//		try {
//			File manifestFile = new File(projectPath + "/AndroidMetadata.xml");
//			try {
//				if (!manifestFile.exists()) {
//					if (apktoolGenerated) {
//						appinfoReport
//						.write("\n<font color=\"red\">"
//								+ ErrorMessage.MISSING_ANDROID_MANIFEST_ERROR
//								.getDescription()
//								+ " (apktool did not generate manifest file).</font>");
//						ToolStatusManager.setToolStatus(appInfo.os, appInfo.appId, appinfoTool.id,
//								ToolStatus.ERROR);
//					}
//					return false;
//				}
//				appInfo.log.debug("Found AndroidMetadata.xml file\tOK");
//				getElementValues(appInfo, manifestFile, appinfoReport);
//				return true;
//			} finally {
//				manifestFile = null;
//			}
//		} catch (final Exception e) {
//			appInfo.log.error(e.getMessage());
//			try {
//				appinfoReport.write("\n<font color=\"red\">" + appinfoTool.name
//						+ " " + ErrorMessage.ANDROID_MANIFEST_ERROR.getDescription()
//						+ "</font>");
//				ToolStatusManager.setToolStatus(appInfo.os, appInfo.appId, appinfoTool.id,
//						ToolStatus.ERROR);
//			} catch (final IOException e1) {
//				appInfo.log.error(e1.getMessage());
//			}
//			return false;
//		}
//	}
//
//	public static synchronized boolean print(AppInfo appInfo, String parameter,
//			String value, BufferedWriter appinfoReport) {
//		if ((value == null) || value.isEmpty()) {
//			appInfo.log.warn(parameter + " not found in AndroidMetadata manifest");
//			return true;
//		} else {
//			appInfo.log.info(parameter + ": \t" + value);
//			try {
//				appinfoReport.write(parameter + ": \t" + value + "\n");
//			} catch (final IOException e) {
//				appInfo.log.error(e.getMessage());
//			}
//			return false;
//		}
//	}
//
//	public static synchronized void setIcon(AppInfo appInfo, String iconValue) {
//		File iconFile = null;
//		if ((iconValue == null) || iconValue.isEmpty()) {
//			iconFile = new File(AppVetProperties.APP_IMAGES + "/default.png");
//		} else {
//			// Icon value will have the syntax '@'<directoryName>'/'<iconName>
//			if ((iconValue.indexOf("@") == 0) && iconValue.contains("/")) {
//				final int slashIndex = iconValue.indexOf("/");
//				final String directoryName = iconValue.substring(1, slashIndex);
//				final String iconName = iconValue.substring(slashIndex + 1,
//						iconValue.length());
//				final String resFolderPath = appInfo.getProjectPath() + "/res";
//				iconFile = getIcon(resFolderPath, directoryName, iconName);
//				if ((iconFile == null) || !iconFile.exists()) {
//					// Use default icon
//					appInfo.log.warn("No icon file found");
//					iconFile = new File(AppVetProperties.APP_IMAGES
//							+ "/default.png");
//				} else {
//					appInfo.log.debug("Icon file exists for: "
//							+ iconFile.getAbsolutePath());
//				}
//			}
//		}
//		// Save icon files in $CATALINA_HOME/webapps/appvet_images so that they
//		// can be referenced quickly by URL
//		File destFile = new File(AppVetProperties.APP_IMAGES + "/"
//				+ appInfo.appId + ".png");
//		if (iconFile != null && destFile != null) {
//			FileUtil.copyFile(iconFile, destFile);
//		}
//	}

	private IOSMetadata() {
	}
}
