package com.onecashye.web.security.service;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.xml.bind.DatatypeConverter;

public class MD5EncryptionService {


	static public String onewayMD5Encryption(String password) throws NoSuchAlgorithmException {
		
		MessageDigest md = MessageDigest.getInstance("MD5");
	    md.update(password.getBytes());
	    byte[] digest = md.digest();
	    String hash = DatatypeConverter
	      .printHexBinary(digest).toUpperCase();
	    
	    return hash;
	}
}
