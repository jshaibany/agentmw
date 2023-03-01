package com.onecashye.web.security.service;

import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Service;

@Service
@PropertySource(ignoreResourceNotFound = true, value = "classpath:aes.properties")
public class AESSecurityService {

	private static final String algorithm = "AES/CBC/PKCS5Padding";
	
	@Value("${aes.initvector}")
	private String initVector;
	@Value("${aes.password}")
	private String password;
	@Value("${aes.salt}")
	private String salt;
	
	public SecretKey generateKey(int n) throws NoSuchAlgorithmException {
	    
		KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
	    keyGenerator.init(n);
	    SecretKey key = keyGenerator.generateKey();
	    return key;
	}
	
	private SecretKey getKeyFromPassword()
		    throws NoSuchAlgorithmException, InvalidKeySpecException {
	    
		SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
		KeySpec spec = new PBEKeySpec(password.toCharArray(), salt.getBytes(), 65536, 128);
		SecretKey secret = new SecretKeySpec(factory.generateSecret(spec)
		        .getEncoded(), "AES");
		    
		return secret;
		
	}
	
	private IvParameterSpec generateIv() throws UnsupportedEncodingException {
	    
	    return new IvParameterSpec(initVector.getBytes("UTF-8"));
	}
	
	public String encrypt(String input) throws NoSuchPaddingException, NoSuchAlgorithmException,
		    InvalidAlgorithmParameterException, InvalidKeyException,
		    BadPaddingException, IllegalBlockSizeException, InvalidKeySpecException, UnsupportedEncodingException {
		    
		    
		Cipher cipher = Cipher.getInstance(algorithm);	    
		cipher.init(Cipher.ENCRYPT_MODE, getKeyFromPassword(), generateIv());
		    
		byte[] cipherText = cipher.doFinal(input.getBytes());
		    
		return Base64.getEncoder().encodeToString(cipherText);
		
	}
	
	public String decrypt(String cipherText) throws NoSuchPaddingException, NoSuchAlgorithmException,
		    InvalidAlgorithmParameterException, InvalidKeyException,
		    BadPaddingException, IllegalBlockSizeException, InvalidKeySpecException, UnsupportedEncodingException {
		    
		    
		Cipher cipher = Cipher.getInstance(algorithm);
		cipher.init(Cipher.DECRYPT_MODE, getKeyFromPassword(), generateIv());
		    
		byte[] plainText = cipher.doFinal(Base64.getDecoder()
		        .decode(cipherText));
		    
		return new String(plainText);
		
	}
}
