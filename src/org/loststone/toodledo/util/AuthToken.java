package org.loststone.toodledo.util;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;;

/**
 * This class holds the key for the usage of one single user.
 * @author lant
 *
 */
public class AuthToken {
	private String token; 
	private String key;
	private DateTime date;
	
	/**
	 * Creates the key for a user.
	 * @param password
	 * @param username
	 * @param token
	 */
	public AuthToken(String password, String username, String token) {
		this.token = token; 
		
		// get the key
		String total; 
		
		try {
			total = MD5(password)+token+username;
			this.key = MD5(total);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		
		this.date = new DateTime().plusHours(4);
	}
	
	
	/**
	 * Cedric GAVA : create an AuthToken from the value stored in a property file rather than getting the authorisation from toodledo server
	 * Creates the key for a user.
	 * @param property in the form token|key|date
	 */
	public AuthToken(String property) {
		//TODO : CGAVA verifier si property a bien une taille suffisante ?
		//TODO : CGAVA verifier les exceptions susceptibles d'être levées
		System.out.println("DEBUG property = "+property);
		String str[]=property.split("\\|");
		System.out.println("DEBUG token = "+str[0]);
		System.out.println("DEBUG key = "+str[1]);
		this.token = str[0];
		this.key = str[1];
		DateTimeFormatter fmt = ISODateTimeFormat.dateTime();
		System.out.println("date = "+str[2]);
		this.date = fmt.parseDateTime(str[2]);
	}
	
	@Override
	public String toString() {
		DateTimeFormatter fmt = ISODateTimeFormat.dateTime();
		return token+"|"+key+"|"+fmt.print(date);
	}
	
	/**
	 * @return the token
	 */
	public String getToken() {
		return token;
	}
	
	
	/**
	 * The key is what is needed to use the REST API.
	 * @return the key
	 */
	public String getKey() {
		return key;
	}
	
	
	/**
	 * @return the date
	 */
	public DateTime getDate() {
		return date;
	}
	
	/**
	 * Returns the remaining time of validity for this token in seconds.
	 * @return seconds until this token will be canceled.
	 */
	public int getRemainingTime() {
		return Math.max(0, this.date.getSecondOfDay() - new DateTime().getSecondOfDay());
	}
	
	/**
	 * Get the stuff and convert it to Hex.
	 * creepy, got it from the internets :P
	 * @param data
	 * @return
	 */
	private String convertToHex(byte[] data) {
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < data.length; i++) {
        	int halfbyte = (data[i] >>> 4) & 0x0F;
        	int two_halfs = 0;
        	do {
	        	if ((0 <= halfbyte) && (halfbyte <= 9))
	                buf.append((char) ('0' + halfbyte));
	            else
	            	buf.append((char) ('a' + (halfbyte - 10)));
	        	halfbyte = data[i] & 0x0F;
        	} while(two_halfs++ < 1);
        }
        return buf.toString();
    }
 
	/**
	 * private stuff for converting strings to md5. 
	 * @param text
	 * @return
	 * @throws NoSuchAlgorithmException
	 * @throws UnsupportedEncodingException
	 */
	private String MD5(String text) throws NoSuchAlgorithmException, UnsupportedEncodingException  {
		MessageDigest md;
		md = MessageDigest.getInstance("MD5");
		byte[] md5hash = new byte[32];
		md.update(text.getBytes("UTF-8"), 0, text.length());
		md5hash = md.digest();
		return convertToHex(md5hash);
	}

}
