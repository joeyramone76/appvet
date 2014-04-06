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

import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/*
 * PBKDF2 salted password hashing.
 * Author: havoc AT defuse.ca
 * www: http://crackstation.net/hashing-security.htm
 */
public class Authenticate {

    private static final Logger log = AppVetProperties.log;

    public static final String PBKDF2_ALGORITHM = "PBKDF2WithHmacSHA1";
    // The following constants may be changed without breaking existing hashes.
    public static final int SALT_BYTE_SIZE = 24;
    public static final int HASH_BYTE_SIZE = 24;
    public static final int PBKDF2_ITERATIONS = 1000;
    public static final int ITERATION_INDEX = 0;
    public static final int SALT_INDEX = 1;
    public static final int PBKDF2_INDEX = 2;

    /**
     * Returns a salted PBKDF2 hash of the password.
     * 
     * @param password
     *            the password to hash
     * @return a salted PBKDF2 hash of the password
     */
    private static String createHash(char[] password)
	    throws NoSuchAlgorithmException, InvalidKeySpecException {
	// Generate a random salt
	final SecureRandom random = new SecureRandom();
	final byte[] salt = new byte[SALT_BYTE_SIZE];
	random.nextBytes(salt);

	// Hash the password
	final byte[] hash = pbkdf2(password, salt, PBKDF2_ITERATIONS,
		HASH_BYTE_SIZE);
	// format iterations:salt:hash
	return PBKDF2_ITERATIONS + ":" + toHex(salt) + ":" + toHex(hash);
    }

    /**
     * Returns a salted PBKDF2 hash of the password.
     * 
     * @param password
     *            the password to hash
     * @return a salted PBKDF2 hash of the password
     */
    public static String createHash(String password)
	    throws NoSuchAlgorithmException, InvalidKeySpecException {
	return createHash(password.toCharArray());
    }

    /**
     * Converts a string of hexadecimal characters into a byte array.
     * 
     * @param hex
     *            the hex string
     * @return the hex string decoded into a byte array
     */
    private static byte[] fromHex(String hex) {
	final byte[] binary = new byte[hex.length() / 2];
	for (int i = 0; i < binary.length; i++) {
	    binary[i] = (byte) Integer.parseInt(
		    hex.substring(2 * i, (2 * i) + 2), 16);
	}
	return binary;
    }

    /** For AppVet. */
    public static boolean isAuthenticated(String username, String password) {
	try {
	    final String storedPasswordHash = Database
		    .getPasswordHash(username);
	    return validatePassword(password, storedPasswordHash);
	} catch (final NoSuchAlgorithmException e) {
	    log.error(e.getMessage());
	} catch (final InvalidKeySpecException e) {
	    log.error(e.getMessage());
	}
	return false;
    }

    /**
     * Tests the basic functionality of the Authenticate class
     * 
     * @param args
     *            ignored
     */
    public static void main(String[] args) {
	try {
	    // Test
	    final String username = "xxx";
	    final String password = "yyy";
	    if (Database.setPBKDF2Password(username, password)) {
		log.debug("Changed password");
	    } else {
		log.error("Could not change password");
	    }
	} catch (final Exception ex) {
	    System.out.println("ERROR: " + ex);
	}
    }

    /**
     * Computes the PBKDF2 hash of a password.
     * 
     * @param password
     *            the password to hash.
     * @param salt
     *            the salt
     * @param iterations
     *            the iteration count (slowness factor)
     * @param bytes
     *            the length of the hash to compute in bytes
     * @return the PBDKF2 hash of the password
     */
    private static byte[] pbkdf2(char[] password, byte[] salt, int iterations,
	    int bytes) throws NoSuchAlgorithmException, InvalidKeySpecException {
	final PBEKeySpec spec = new PBEKeySpec(password, salt, iterations,
		bytes * 8);
	final SecretKeyFactory skf = SecretKeyFactory
		.getInstance(PBKDF2_ALGORITHM);
	return skf.generateSecret(spec).getEncoded();
    }

    /**
     * Compares two byte arrays in length-constant time. This comparison method
     * is used so that password hashes cannot be extracted from an on-line
     * system using a timing attack and then attacked off-line.
     * 
     * @param a
     *            the first byte array
     * @param b
     *            the second byte array
     * @return true if both byte arrays are the same, false if not
     */
    private static boolean slowEquals(byte[] a, byte[] b) {
	int diff = a.length ^ b.length;
	for (int i = 0; (i < a.length) && (i < b.length); i++) {
	    diff |= a[i] ^ b[i];
	}
	return diff == 0;
    }

    /**
     * Converts a byte array into a hexadecimal string.
     * 
     * @param array
     *            the byte array to convert
     * @return a length*2 character string encoding the byte array
     */
    private static String toHex(byte[] array) {
	final BigInteger bi = new BigInteger(1, array);
	final String hex = bi.toString(16);
	final int paddingLength = (array.length * 2) - hex.length();
	if (paddingLength > 0) {
	    return String.format("%0" + paddingLength + "d", 0) + hex;
	} else {
	    return hex;
	}
    }

    /**
     * Validates a password using a hash.
     * 
     * @param password
     *            the password to check
     * @param storedPasswordHash
     *            the hash of the valid password
     * @return true if the password is correct, false if not
     */
    private static boolean validatePassword(char[] password, String storedPasswordHash)
	    throws NoSuchAlgorithmException, InvalidKeySpecException {
	// Decode the hash into its parameters
	final String[] params = storedPasswordHash.split(":");
	final int iterations = Integer.parseInt(params[ITERATION_INDEX]);
	final byte[] salt = fromHex(params[SALT_INDEX]);
	final byte[] hash = fromHex(params[PBKDF2_INDEX]);
	// Compute the hash of the provided password, using the same salt,
	// iteration count, and hash length
	final byte[] testHash = pbkdf2(password, salt, iterations, hash.length);
	// Compare the hashes in constant time. The password is correct if
	// both hashes match.
	return slowEquals(hash, testHash);
    }

    /**
     * Validates a password using a hash.
     * 
     * @param password
     *            the password to check
     * @param storedPasswordHash
     *            the hash of the valid password
     * @return true if the password is correct, false if not
     */
    private static boolean validatePassword(String password, String storedPasswordHash)
	    throws NoSuchAlgorithmException, InvalidKeySpecException {
	return validatePassword(password.toCharArray(), storedPasswordHash);
    }

}