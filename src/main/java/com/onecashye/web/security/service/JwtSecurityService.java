package com.onecashye.web.security.service;

import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.sql.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

@Service
@PropertySource(ignoreResourceNotFound = true, value = "classpath:jwt.properties")
public class JwtSecurityService {

	@Value("${jwt.secret}")
	private String secretKey;
	@Value("${jwt.id}")
	private String jwtID;
	@Value("${jwt.add.bearer}")
	private String addBearer;
	@Value("${jwt.token.expiry}")
	private long tokenExpiry;
	@Value("${rsa.public.key}")
	private String rsaPublicKeyFile;
	@Value("${rsa.private.key}")
	private String rsaPrivateKeyFile;
	private final String HEADER = "Authorization";
	private final String PREFIX = "Bearer ";
	
	@Autowired
	public JwtSecurityService() {
		super();
		
	}

	public String getJWTToken(String username,String user_profile) throws InvalidKeyException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, BadPaddingException, IllegalBlockSizeException, InvalidKeySpecException, UnsupportedEncodingException {
		
		List<GrantedAuthority> grantedAuthorities = AuthorityUtils
				.commaSeparatedStringToAuthorityList("ROLE_USER");
		
		String token = Jwts
				.builder()
				.setId(RSASecurityService.decrypt(jwtID,rsaPrivateKeyFile))
				.setSubject(username)
				.claim("user_profile", RSASecurityService.encrypt(user_profile,rsaPublicKeyFile))
				.claim("authorities",
						grantedAuthorities.stream()
								.map(GrantedAuthority::getAuthority)
								.collect(Collectors.toList()))
				.setIssuedAt(new Date(System.currentTimeMillis()))
				.setExpiration(new Date(System.currentTimeMillis() + tokenExpiry))
				.signWith(SignatureAlgorithm.HS512,
						RSASecurityService.decrypt(secretKey,rsaPrivateKeyFile).getBytes()).compact();

		if(addBearer.equals("YES"))
		return "Bearer " + token;
		
		return "" + token;
	}
	
	public Claims validateToken(Map<String, String> headers) {
		
		String jwtToken = headers.get(HEADER.toLowerCase()).replace(PREFIX, "").trim();
		return Jwts.parser().setSigningKey(RSASecurityService.decrypt(secretKey,rsaPrivateKeyFile).getBytes()).parseClaimsJws(jwtToken).getBody();
	}
	
	public String decryptClaim(String claim) throws InvalidKeyException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, BadPaddingException, IllegalBlockSizeException, InvalidKeySpecException, UnsupportedEncodingException {
		
		return RSASecurityService.decrypt(claim,rsaPrivateKeyFile);
	}
}
