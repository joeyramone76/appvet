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
import gov.nist.appvet.servlet.shared.Native;
import gov.nist.appvet.servlet.shared.TimeoutException;
import gov.nist.appvet.shared.Database;
import gov.nist.appvet.shared.ErrorMessage;
import gov.nist.appvet.shared.FileUtil;
import gov.nist.appvet.shared.app.AppInfo;
import gov.nist.appvet.shared.status.ToolStatus;
import gov.nist.appvet.shared.status.ToolStatusManager;
import gov.nist.appvet.toolmgr.ToolServiceAdapter;
import gov.nist.appvet.xml.XmlUtil;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;

public class AndroidManifest {
	
	public static final String APKTOOL_WINDOWS_COMMAND = "apktool.bat";
	public static final String APKTOOL_LINUX_COMMAND = "apktool";

	private static ToolServiceAdapter appinfoTool = ToolServiceAdapter
			.getById("appinfo");

	public static synchronized boolean decodeApk(AppInfo appInfo,
			String apkPath, BufferedWriter appinfoReport) {
		final String projectPath = appInfo.getProjectPath();
		String os = Native.os;
		String apktoolCommand = null;
		if (os.toLowerCase().indexOf("win") > -1) {
			apktoolCommand = APKTOOL_WINDOWS_COMMAND;
		} else if (os.toLowerCase().indexOf("nux") > -1) {
			apktoolCommand = APKTOOL_LINUX_COMMAND;
		}
		final String decodeCmd = apktoolCommand
				+ " d "
				+ apkPath
				+ " "
				+ projectPath;
		try {
			final StringBuilder outputBuffer = new StringBuilder();
			final StringBuilder errorBuffer = new StringBuilder();
			if (Native.exec(appInfo, decodeCmd, 120000, outputBuffer, errorBuffer, true)) {
				appInfo.log.info("Decoded APK:\tOK");
				return true;
			} else {
				appInfo.log.error(outputBuffer.toString());
				appinfoReport.write("\n<font color=\"red\">"
						+ ErrorMessage.ANDROID_APK_DECODE_ERROR.getDescription()
						+ " (File may be corrupted)</font>");
				appInfo.log.error(ErrorMessage.ANDROID_APK_DECODE_ERROR.getDescription());
				ToolStatusManager.setToolStatus(appInfo.appId, appinfoTool.id,
						ToolStatus.ERROR);
				return false;
			}
		} catch (final TimeoutException e) {
			appInfo.log.error(e.getMessage());
			ToolStatusManager.setToolStatus(appInfo.appId, appinfoTool.id,
					ToolStatus.ERROR);
			try {
				appinfoReport.write("\n<font color=\"red\">"
						+ ErrorMessage.ANDROID_APK_DECODE_ERROR.getDescription()
						+ "</font>");
			} catch (final IOException e1) {
				appInfo.log.error(e1.getMessage());
			}
			return false;
		} catch (final IOException e) {
			appInfo.log.error(e.getMessage());
			ToolStatusManager.setToolStatus(appInfo.appId, appinfoTool.id,
					ToolStatus.ERROR);
			try {
				appinfoReport.write("\n<font color=\"red\">"
						+ ErrorMessage.ANDROID_APK_DECODE_ERROR.getDescription()
						+ "</font>");
			} catch (final IOException e1) {
				appInfo.log.error(e1.getMessage());
			}
			return false;
		}
	}

	public static synchronized void getElementValues(AppInfo appInfo,
			File xmlFile, BufferedWriter appinfoReport) {
		final XmlUtil xml = new XmlUtil(xmlFile);
		appInfo.packageName = xml.getXPathValue("/manifest/@package");
		print(appInfo, "Package name", appInfo.packageName, appinfoReport);
		appInfo.versionCode = xml.getXPathValue("/manifest/@versionCode");
		print(appInfo, "Version code", appInfo.versionCode, appinfoReport);
		appInfo.versionName = xml.getXPathValue("/manifest/@versionName");
		print(appInfo, "Version name", appInfo.versionName, appinfoReport);
		Database.updateApp(appInfo.appId, appInfo.getProjectName(),
				appInfo.packageName, appInfo.versionCode, appInfo.versionName);
		String minSdk = xml.getXPathValue("/manifest/uses-sdk/@minSdkVersion");
		print(appInfo, "Min SDK", minSdk, appinfoReport);
		String maxSdk = xml.getXPathValue("/manifest/uses-sdk/@maxSdkVersion");
		print(appInfo, "Max SDK", maxSdk, appinfoReport);
		String targetSdk = xml
				.getXPathValue("/manifest/uses-sdk/@targetSdkVersion");
		print(appInfo, "Target SDK", targetSdk, appinfoReport);
		final String iconValue = xml
				.getXPathValue("/manifest/application/@icon");
		setIcon(appInfo, iconValue);
	}

	public static synchronized File getIcon(String resFolderPath,
			String iconDirectoryName, String iconName) {
		File resDirectory = new File(resFolderPath);
		File[] resFiles = resDirectory.listFiles();
		File[] iconDirectoryFiles = null;
		try {
			for (final File resFile : resFiles) {
				if (resFile.isDirectory()) {
					// Icon could be in one of several directories containing
					// <iconDirectoryName>
					if (resFile.getName().contains(iconDirectoryName)) {
						iconDirectoryFiles = resFile.listFiles();
						for (final File iconFile : iconDirectoryFiles) {
							if (iconFile.getName().equals(iconName + ".png")) {
								return iconFile;
							}
						}
					}
				}
			}
			return null; 
		} finally {
			iconDirectoryFiles = null;
			resFiles = null;
			resDirectory = null;
		}
	}

	public static synchronized boolean getManifestInfo(AppInfo appInfo,
			BufferedWriter appinfoReport, boolean apktoolGenerated) {
		final String projectPath = appInfo.getProjectPath();
		try {
			File manifestFile = new File(projectPath + "/AndroidManifest.xml");
			try {
				if (!manifestFile.exists()) {
					if (apktoolGenerated) {
						appinfoReport
						.write("\n<font color=\"red\">"
								+ ErrorMessage.MISSING_ANDROID_MANIFEST_ERROR
								.getDescription()
								+ " (apktool did not generate manifest file).</font>");
						ToolStatusManager.setToolStatus(appInfo.appId, appinfoTool.id,
								ToolStatus.ERROR);
					}
					return false;
				}
				appInfo.log.debug("Found AndroidManifest.xml file\tOK");
				getElementValues(appInfo, manifestFile, appinfoReport);
				return true;
			} finally {
				manifestFile = null;
			}
		} catch (final Exception e) {
			appInfo.log.error(e.getMessage());
			try {
				appinfoReport.write("\n<font color=\"red\">" + appinfoTool.name
						+ " " + ErrorMessage.ANDROID_MANIFEST_ERROR.getDescription()
						+ "</font>");
				ToolStatusManager.setToolStatus(appInfo.appId, appinfoTool.id,
						ToolStatus.ERROR);
			} catch (final IOException e1) {
				appInfo.log.error(e1.getMessage());
			}
			return false;
		}
	}

	public static synchronized boolean print(AppInfo appInfo, String parameter,
			String value, BufferedWriter appinfoReport) {
		if ((value == null) || value.isEmpty()) {
			appInfo.log.warn(parameter + " not found in AndroidManifest manifest");
			return true;
		} else {
			appInfo.log.info(parameter + ": \t" + value);
			try {
				appinfoReport.write(parameter + ": \t" + value + "\n");
			} catch (final IOException e) {
				appInfo.log.error(e.getMessage());
			}
			return false;
		}
	}

	public static synchronized void setIcon(AppInfo appInfo, String iconValue) {
		File iconFile = null;
		if ((iconValue == null) || iconValue.isEmpty()) {
			iconFile = new File(AppVetProperties.APP_IMAGES + "/default.png");
		} else {
			// Icon value will have the syntax '@'<directoryName>'/'<iconName>
			if ((iconValue.indexOf("@") == 0) && iconValue.contains("/")) {
				final int slashIndex = iconValue.indexOf("/");
				final String directoryName = iconValue.substring(1, slashIndex);
				final String iconName = iconValue.substring(slashIndex + 1,
						iconValue.length());
				final String resFolderPath = appInfo.getProjectPath() + "/res";
				iconFile = getIcon(resFolderPath, directoryName, iconName);
				if ((iconFile == null) || !iconFile.exists()) {
					// Use default icon
					appInfo.log.warn("No icon file found");
					iconFile = new File(AppVetProperties.APP_IMAGES
							+ "/default.png");
				} else {
					appInfo.log.debug("Icon file exists for: "
							+ iconFile.getAbsolutePath());
				}
			}
		}
		// Save icon files in $CATALINA_HOME/webapps/appvet_images so that they
		// can be referenced quickly by URL
		File destFile = new File(AppVetProperties.APP_IMAGES + "/"
				+ appInfo.appId + ".png");
		if (iconFile != null && destFile != null) {
			FileUtil.copyFile(iconFile, destFile);
		}
	}

	private AndroidManifest() {
	}
}
