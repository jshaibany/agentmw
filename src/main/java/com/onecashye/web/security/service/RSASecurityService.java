package com.onecashye.web.security.service;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.EncodedKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import org.springframework.core.io.ClassPathResource;

public class RSASecurityService {

	public static String encrypt(String input,String pubKey) {
		
		try {
			
			//URL res = RSASecurityService.class.getClassLoader().getResource("public.key");
			//File file= Paths.get(res.toURI()).toFile();
			File resource = new ClassPathResource(
				      "public.key").getFile();
			
			//System.out.println(file.getAbsolutePath());
			
			byte[] fileBytes= Files.readAllBytes(resource.toPath());
			
			KeyFactory keyFactory = KeyFactory.getInstance("RSA");
			EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(fileBytes);
			PublicKey publicKey = keyFactory.generatePublic(publicKeySpec);
			
			
			Cipher encryptCipher = Cipher.getInstance("RSA");
			encryptCipher.init(Cipher.ENCRYPT_MODE, publicKey);
			
			byte[] secretMessageBytes = input.getBytes(StandardCharsets.UTF_8);
			byte[] encryptedMessageBytes = encryptCipher.doFinal(secretMessageBytes);
			
			return Base64.getEncoder().encodeToString(encryptedMessageBytes);
			
		} catch (IOException e) {
			
			e.printStackTrace();
			return "";
			
		} catch (NoSuchAlgorithmException e) {
			
			e.printStackTrace();
			return "";
			
		} catch (InvalidKeySpecException e) {
			
			e.printStackTrace();
			return "";
			
		} catch (NoSuchPaddingException e) {
			
			e.printStackTrace();
			return "";
			
		} catch (InvalidKeyException e) {
			
			e.printStackTrace();
			return "";
			
		} catch (IllegalBlockSizeException e) {
			
			e.printStackTrace();
			return "";
			
		} catch (BadPaddingException e) {
			
			e.printStackTrace();
			return "";
		} /*catch (URISyntaxException e) {
		
			e.printStackTrace();
			return "";
		}*/
		
	}
	
	/*
	public static String encrypt(String input,String pubKey) {
		
		File publicKeyFile = new File(pubKey);
		byte[] publicKeyBytes;
		
		try {
			publicKeyBytes = Files.readAllBytes(publicKeyFile.toPath());
			KeyFactory keyFactory = KeyFactory.getInstance("RSA");
			EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(publicKeyBytes);
			PublicKey publicKey = keyFactory.generatePublic(publicKeySpec);
			
			
			Cipher encryptCipher = Cipher.getInstance("RSA");
			encryptCipher.init(Cipher.ENCRYPT_MODE, publicKey);
			
			byte[] secretMessageBytes = input.getBytes(StandardCharsets.UTF_8);
			byte[] encryptedMessageBytes = encryptCipher.doFinal(secretMessageBytes);
			
			return Base64.getEncoder().encodeToString(encryptedMessageBytes);
			
		} catch (IOException e) {
			
			e.printStackTrace();
			return "";
			
		} catch (NoSuchAlgorithmException e) {
			
			e.printStackTrace();
			return "";
			
		} catch (InvalidKeySpecException e) {
			
			e.printStackTrace();
			return "";
			
		} catch (NoSuchPaddingException e) {
			
			e.printStackTrace();
			return "";
			
		} catch (InvalidKeyException e) {
			
			e.printStackTrace();
			return "";
			
		} catch (IllegalBlockSizeException e) {
			
			e.printStackTrace();
			return "";
			
		} catch (BadPaddingException e) {
			
			e.printStackTrace();
			return "";
		}
		
	}
	*/

	public static String decrypt(String input,String privKey) {
		
		try {
			
			//URL res = RSASecurityService.class.getClassLoader().getResource("private.key");
			
			//File file= Paths.get(res.toURI()).toFile();
			File resource = new ClassPathResource(
				      "private.key").getFile();
			
			
			//System.out.println(file.getAbsolutePath());
			
			byte[] fileBytes= Files.readAllBytes(resource.toPath());
			
			
			KeyFactory keyFactory = KeyFactory.getInstance("RSA");
			EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(fileBytes);
			PrivateKey privateKey = keyFactory.generatePrivate(privateKeySpec);
			
			Cipher decryptCipher = Cipher.getInstance("RSA");
			decryptCipher.init(Cipher.DECRYPT_MODE, privateKey);	
			
			byte[] decryptedMessageBytes = decryptCipher.doFinal(Base64.getDecoder().decode(input));
			return new String(decryptedMessageBytes, StandardCharsets.UTF_8);
		}
		catch(Exception e) {
			
			e.printStackTrace();
			return "";
		}
		
        
		
	}

	/*
	public static String decrypt(String input,String privKey) {
		
		String currentWorkingDir = System.getProperty("user.dir");
        System.out.println(currentWorkingDir);
        
		File privateKeyFile = new File(privKey);
		byte[] privateKeyBytes;
		
		try {
			
			Path p = privateKeyFile.toPath();
			URI u = p.toUri();
			
			System.out.println(u.getPath());
			
			privateKeyBytes = Files.readAllBytes(privateKeyFile.toPath());
			KeyFactory keyFactory = KeyFactory.getInstance("RSA");
			EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
			PrivateKey privateKey = keyFactory.generatePrivate(privateKeySpec);
			
			Cipher decryptCipher = Cipher.getInstance("RSA");
			decryptCipher.init(Cipher.DECRYPT_MODE, privateKey);	
			
			byte[] decryptedMessageBytes = decryptCipher.doFinal(Base64.getDecoder().decode(input));
			return new String(decryptedMessageBytes, StandardCharsets.UTF_8);
			
		} catch (IOException e) {
			
			e.printStackTrace();
			return "";
			
		} catch (NoSuchAlgorithmException e) {
			
			e.printStackTrace();
			return "";
			
		} catch (InvalidKeySpecException e) {
			
			e.printStackTrace();
			return "";
			
		} catch (NoSuchPaddingException e) {
			
			e.printStackTrace();
			return "";
			
		} catch (InvalidKeyException e) {
			
			e.printStackTrace();
			return "";
			
		} catch (IllegalBlockSizeException e) {
			
			e.printStackTrace();
			return "";
			
		} catch (BadPaddingException e) {
			
			e.printStackTrace();
			return "";
		}
		
	}
	*/



}
