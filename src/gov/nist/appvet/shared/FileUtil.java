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
package gov.nist.appvet.shared;

import gov.nist.appvet.properties.AppVetProperties;
import gov.nist.appvet.shared.app.AppInfo;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import org.apache.commons.fileupload.FileItem;


public class FileUtil {

	private static final Logger log = AppVetProperties.log;

	public static synchronized boolean copyFile(File sourceFile, File destFile) {
		if (sourceFile == null || !sourceFile.exists() || destFile == null) {
			return false;
		}
		try {
			Files.copy(sourceFile.toPath(), destFile.toPath(),
					StandardCopyOption.REPLACE_EXISTING);
		} catch (final IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	public static synchronized boolean copyFile(String sourceFilePath,
			String destFilePath) {
		if (sourceFilePath == null || destFilePath == null) {
			return false;
		}
		File sourceFile = new File(sourceFilePath);
		if (!sourceFile.exists()) {
			return false;
		}
		File destFile = new File(destFilePath);
		try {
			Files.copy(sourceFile.toPath(), destFile.toPath(),
					StandardCopyOption.REPLACE_EXISTING);
		} catch (final IOException e) {
			log.error(e.getMessage());
			return false;
		} finally {
			sourceFile = null;
			destFile = null;
		}
		return true;
	}

	public static synchronized void deleteDirectory(File file) {
		if (file == null) {
			return;
		}
		if (file.exists()) {
			for (final File f : file.listFiles()) {
				if (f.isDirectory()) {
					deleteDirectory(f);
					f.delete();
				} else {
					f.delete();
				}
			}
			file.delete();
		}
	}

	public static synchronized boolean deleteFile(String sourceFilePath) {
		File file = new File(sourceFilePath);
		try {
			if (file.exists()) {
				return file.delete();
			} else {
				log.error("Cannot find file '" + sourceFilePath
						+ "' to delete");
				return false;
			}	
		} finally {
			file = null;
		}
	}

	/**
	 * Remove the prepended path of the file name.
	 * @param filePath
	 * @return
	 */
	public static synchronized String getNormalizedFileName(String filePath) {
		final int forwardSlashIndex = filePath.lastIndexOf("/");
		final int backSlashIndex = filePath.lastIndexOf("\\");
		if (forwardSlashIndex > backSlashIndex) {
			return filePath.substring(forwardSlashIndex + 1);
		} else if (backSlashIndex > forwardSlashIndex) {
			return filePath.substring(backSlashIndex + 1);
		} else {
			return filePath;
		}
	}

	public static synchronized boolean saveFileUpload(AppInfo appInfo) {
		File file = null;
		try {
			// Note that if appInfo.fileItem.name contained spaces then those
			// spaces are replaced with underscores for appInfo.fileName.
			file = new File(appInfo.getAppFilePath());
			appInfo.fileItem.write(file);
			return true;
		} catch (final Exception e) {
			e.printStackTrace();
			log.error(e.getMessage());
			return false;
		} finally {
			file = null;
		}
	}
	
	public static String replaceSpaceWithUnderscore(String str) {
		return str.replaceAll(" ", "_");
	}

	public static synchronized boolean saveReportUpload(String appId,
			String reportName, FileItem fileItem) {
		String reportsPath = null;
		String outputFilePath = null;
		File file = null;
		try {
			reportsPath = AppVetProperties.APPS_ROOT + "/"
					+ appId + "/reports";
			outputFilePath = reportsPath + "/" + reportName;
			file = new File(outputFilePath);
			if (file.exists()) {
				file.delete();
			}
			fileItem.write(file);
			return true;
		} catch (final Exception e) {
			log.error(e.getMessage());
			return false;
		} finally {
			reportsPath = null;
			outputFilePath = null;
			file = null;
			fileItem.delete();
		}
	}

	private FileUtil() {
	}
}
