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
package gov.nist.appvet.shared.validate;

import gov.nist.appvet.shared.role.Role;

public class ValidateBase {

	public static final int CNRI_HANDLE_HASH_MAX_LENGTH = 128;
	public static final int USERNAME_MIN_LENGTH = 3;
	public static final int USERNAME_MAX_LENGTH = 32;
	public static final int PASSWORD_MIN_LENGTH = 4;
	public static final int PASSWORD_MAX_LENGTH = 32;
	public static final int FULLNAME_MAX_LENGTH = 32;
	public static final int ORG_MAX_LENGTH = 32;
	public static final int EMAIL_MAX_LENGTH = 32;
	public static final int ROLE_MAX_LENGTH = 32;
	public static final int APPID_MAX_LENGTH = 32;
	public static final int PACKAGENAME_MAX_LENGTH = 32;
	public static final int VERSIONCODE_MAX_LENGTH = 32;
	public static final int VERSIONNAME_MAX_LENGTH = 32;
	public static final int FILENAME_MAX_LENGTH = 120;
	public static final int STATUS_MAX_LENGTH = 32;
	public static final int CLIENTHOST_MAX_LENGTH = 32;

	public static boolean isPrintable(String s) {
		if (s == null) {
			return false;
		}
		for (int i = 0; i < s.length(); i++) {
			if (!isAsciiPrintable(s.charAt(i))) {
				return false;
			}
		}
		return true;
	}

	private static boolean isAsciiPrintable(char ch) {
		return ch >= 32 && ch < 127;
	}

	public static boolean isAlpha(String s) {
		return s.matches("[a-zA-Z]+");
	}

	public static boolean isNumeric(String s) {
		return s.matches("[0-9]+");
	}

	public static boolean isAlphaNumeric(String s) {
		return s.matches("^[a-zA-Z0-9]*$");
	}

	public static boolean hasWhiteSpace(String s) {
		return s.matches(".*\\s+.*");
	}

	public static boolean isDate(String str) {
		// Match format YYYY-MM-DD
		return str.matches("\\d{4}-\\d{2}-\\d{2}");
	}

	public static boolean isTime(String str) {
		// Match format HH:MM:SSZ
		return str.matches("\\d{2}:\\d{2}:\\d{2}Z");
	}

	public static boolean isValidUserName(String userName) {
		return userName != null && !userName.isEmpty()
				&& isAlphaNumeric(userName)
				&& userName.length() <= USERNAME_MAX_LENGTH
				&& userName.length() >= USERNAME_MIN_LENGTH;
	}

	public static boolean isValidPassword(String password) {
		return password != null && !password.isEmpty()
				&& !hasWhiteSpace(password) && isPrintable(password)
				&& (password.length() <= PASSWORD_MAX_LENGTH)
				&& (password.length() >= PASSWORD_MIN_LENGTH);
	}

	public static boolean isValidEmail(String email) {
		return email
				.matches("[A-Za-z0-9._%+-][A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{3}");
	}

	public static boolean isUrl(String s) {
		return s.matches("([a-zA-Z0-9\\-_\\.!\\~\\*'\\(\\);/\\?:\\@\\&=\\+$,]|(%[a-fA-F0-9]{2}))*");
	}

	public static boolean isLegalFileName(String fileName) {
		return isPrintable(fileName) && !hasWhiteSpace(fileName);
	}

	public static boolean hasValidAppFileExtension(String fileName) {
		final String fileNameUpperCase = fileName.toUpperCase();
		return fileNameUpperCase.endsWith(".APK")
				|| fileNameUpperCase.endsWith(".IPA");
	}

	public static boolean hasValidReportFileExtension(String fileName) {
		final String fileNameUpperCase = fileName.toUpperCase();
		return fileNameUpperCase.endsWith(".PDF")
				|| fileNameUpperCase.endsWith(".HTML")
				|| fileNameUpperCase.endsWith(".TXT")
				|| fileNameUpperCase.endsWith(".RTF")
				|| fileNameUpperCase.endsWith(".XML");
	}

	public static boolean isLegalSearchString(String str) {
		return isPrintable(str) && !hasWhiteSpace(str);
	}

	public static boolean isValidRole(String roleName) {
		Role role = Role.getRole(roleName);
		if (role != null)
			return true;
		else
			return false;
	}
	
	public static boolean hasValidOs(String appOS) {
		String os = appOS.toLowerCase();
		if (os.equals("android") || os.equals("ios")) {
			return true;
		} else {
			return false;
		}
	}

	public ValidateBase() {
	}
}
