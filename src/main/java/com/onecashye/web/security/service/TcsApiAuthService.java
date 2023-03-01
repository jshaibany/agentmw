package com.onecashye.web.security.service;


import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClientException;

import com.onecashye.web.security.helper.MatcherHelper;
import com.onecashye.web.security.telepin.service.AccountService;
import com.onecashye.web.security.telepin.service.TerminalService;

import io.jsonwebtoken.Claims;
import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import reactor.netty.http.client.HttpClient;

@Service
@PropertySource(ignoreResourceNotFound = true, value = "classpath:tcs.properties")
public class TcsApiAuthService {

	Logger logger = LogManager.getLogger(TcsApiAuthService.class);
	private final Environment env;
	
	private final TcsServiceInitializer tcsServiceInitializer;
	private HttpClient httpClient;
	
	private final JwtSecurityService securityService;
	private final TerminalService terminalService;
	private final AccountService accountService;
	
	@Autowired
	public TcsApiAuthService(TcsServiceInitializer tcsServiceInitializer,
			JwtSecurityService securityService,
			TerminalService terminalService,
			AccountService accountService, 
			Environment env) {
		super();
		this.env = env;
		this.tcsServiceInitializer=tcsServiceInitializer;
		this.securityService=securityService;
		this.terminalService=terminalService;
		this.accountService=accountService;
		
		
		httpClient = HttpClient.create()
				  .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, tcsServiceInitializer.getTcsCallTimeout())
				  .responseTimeout(Duration.ofMillis(tcsServiceInitializer.getTcsCallTimeout()))
				  .doOnConnected(conn -> 
				    conn.addHandlerLast(new ReadTimeoutHandler(30))
				      .addHandlerLast(new WriteTimeoutHandler(tcsServiceInitializer.getTcsCallTimeout(), TimeUnit.MILLISECONDS)));
	}
	
	public Map<String,String> tcsAuthenticate(Map<String,Object> jsonRequest) {
		
		
		String xml = MatcherHelper.convert(jsonRequest, tcsServiceInitializer.getTcsFunctionAuthenticate());
		
		String uri = String.format("%s", tcsServiceInitializer.getTcsHost());

		try {
			
			
			Map<String,String> result = TcsHttpClientService.tcsCall(uri,xml,httpClient);
			
			//Case When TCS replys success login, password is valid, password is permanent
			//Return Token
			if(result.get("Result").equalsIgnoreCase("0") && result.get("param1").equalsIgnoreCase("0") && result.get("param2").equalsIgnoreCase("0")) {
				

				Map<String,String> successLogin = new HashMap<>();
				
				successLogin.put("Result","0");
				successLogin.put("Message","SUCCESS_LOGIN");
				
				xml = MatcherHelper.convert(jsonRequest, tcsServiceInitializer.getTcsFunctionGetMyAccountInfo());
				
				Map<String,String> resultInfo = TcsHttpClientService.tcsCall(uri,xml,httpClient);
				
				successLogin.put("Account_ID", resultInfo.get("param1"));
				successLogin.put("MSISDN", resultInfo.get("param2"));
				successLogin.put("Lang", resultInfo.get("param40"));
				successLogin.put("Universal_ID", accountService.getAccountUniversalID(resultInfo.get("param1")));
				//successLogin.put("Universal_ID","1");
				
				Map<String,Object> terminal_details = terminalService.getTerminalDetails(jsonRequest.get("username").toString(), resultInfo.get("param1"));
				//Map<String,Object> terminal_details = null;
				
				String user_profile;
				
				if(terminal_details != null) {
					
					String m = (String) terminal_details.get("mobile_number");
					
					String mail = (String) terminal_details.get("user_email");
					
					//Added 24042022
					
					successLogin.put("Alias_Number", (String) terminal_details.get("alias_number"));
					////////////////////////////////////////////////////////////////////////////////
					
					if(m == null || m.isEmpty())
						m="7";
					
					//User Profile Claim: Username:Password:AccountID:MSISDN:UserID:UserMobile:AccountUniversalID:UserEmail
					
					user_profile = String.format("%s:%s:%s:%s:%s:%s:%s:%s", jsonRequest.get("username").toString(),
							jsonRequest.get("password").toString(),
							resultInfo.get("param1"),//Account ID
							resultInfo.get("param2"),//MSISDN
							terminal_details.get("user_id").toString(),
							m,//User Mobile Number (Personal Contact)
							successLogin.get("Universal_ID"),
							mail);
				}
				else {
					
					user_profile = String.format("%s:%s:%s:%s:%s:%s:%s:%s", jsonRequest.get("username").toString(),
							jsonRequest.get("password").toString(),
							resultInfo.get("param1"),
							resultInfo.get("param2"),
							"0",
							"7",
							successLogin.get("Universal_ID"),
							"");
				}
				
				
				String jwt = securityService.getJWTToken(jsonRequest.get("username").toString(),user_profile);
				
				successLogin.put("token",jwt);
				
				return successLogin;
			}
			
			//Case When TCS replys success login, password is valid, password is temporary
			if(result.get("Result").equalsIgnoreCase("0") && result.get("param1").equalsIgnoreCase("0") && result.get("param2").equalsIgnoreCase("1")) {
				
				Map<String,String> changePassword = new HashMap<>();
				
				changePassword.put("Result","-1");
				changePassword.put("Message","CHANGE_PASSWORD_REQUIRED");
				
				return changePassword;
			}
			
			//Case When TCS replys success login, password is valid, password is temporary
			if(result.get("Result").equalsIgnoreCase("0") && result.get("param1").equalsIgnoreCase("1")) {
				
				Map<String,String> changePassword = new HashMap<>();
				
				changePassword.put("Result","-1");
				changePassword.put("Message","PASSWORD_EXPIRED");
				
				return changePassword;
			}
			
			//Case When TCS replys 100008 or 100011, user is Blocked
			if(result.get("Result").equalsIgnoreCase("100008") || result.get("Result").equalsIgnoreCase("100011")) {
				
				Map<String,String> usr = new HashMap<>();
				
				usr.put("Result","-1");
				usr.put("Message","USER_BLOCKED");
				
				return usr;
			}
			
			//Otherwise it is invalid login
			Map<String,String> invalidPassword = new HashMap<>();
			
			invalidPassword.put("Result","-2");
			invalidPassword.put("Message","INVALID_PASSWORD");
			
			logger.error(String.format("User trying to login %s ***", jsonRequest.get("username")));
			
			//Only in test mode
			logger.error(String.format("Password provided %s ***", jsonRequest.get("password")));
			
			return invalidPassword;
			
			
			
		}
		catch(WebClientException e) {
			
			e.printStackTrace();
			Map<String,String> er = new HashMap<>();
			er.put("Result","-100");
			er.put("Message","EXCEPTION");
			er.put("Error", e.getMessage());
			return er;
		}
		catch(Exception e) {
			
			e.printStackTrace();
			Map<String,String> er = new HashMap<>();
			er.put("Result","-100");
			er.put("Message","EXCEPTION");
			er.put("Error", e.getMessage());
			return er;
		}
		
		
	}
	
	public Map<String,String> tcsAuthenticateHeartbeat(Map<String,Object> jsonRequest) {
		
		
		String xml = MatcherHelper.convert(jsonRequest, tcsServiceInitializer.getTcsFunctionAuthenticate());
		
		String uri = String.format("%s", tcsServiceInitializer.getTcsHost());

		try {
			
			
			Map<String,String> result = TcsHttpClientService.tcsCall(uri,xml,httpClient);
			
			//Case When TCS replys success login, password is valid, password is permanent
			//Return Token
			if(result.get("Result").equalsIgnoreCase("0") && result.get("param1").equalsIgnoreCase("0") && result.get("param2").equalsIgnoreCase("0")) {
				

				Map<String,String> successLogin = new HashMap<>();
				
				successLogin.put("Result","0");
				successLogin.put("Message","SUCCESS");
				
				return successLogin;
			}
			
			
			
			//Otherwise it is invalid login
			Map<String,String> invalidPassword = new HashMap<>();
			
			invalidPassword.put("Result","-1");
			invalidPassword.put("Message","FAILED");
			
			return invalidPassword;
			
			
			
		}
		catch(WebClientException e) {
			
			e.printStackTrace();
			Map<String,String> er = new HashMap<>();
			er.put("Result","-100");
			er.put("Message","EXCEPTION");
			er.put("Error", e.getMessage());
			return er;
		}
		catch(Exception e) {
			
			e.printStackTrace();
			Map<String,String> er = new HashMap<>();
			er.put("Result","-100");
			er.put("Message","EXCEPTION");
			er.put("Error", e.getMessage());
			return er;
		}
		
		
	}
	
	public Map<String,String> jsonRpcAuthenticate(Map<String,Object> jsonRequest) {
		
		
		

		try {
			
			
			Map<String,Object> result = TcsHttpClientService.jsonRpcDoLogin(jsonRequest.get("username").toString(),
					jsonRequest.get("password").toString(),
					env.getProperty("tp.web.domain"),
					env.getProperty("tp.web.uri"),
					env.getProperty("tp.web.path"));
			
			
			if(result.get("result").toString().equalsIgnoreCase("0")) {
				

				Map<String,String> successLogin = new HashMap<>();
				
				successLogin.put("Result","0");
				successLogin.put("Message","SUCCESS_LOGIN");
				successLogin.put("Session",result.get("Session").toString());
				
				return successLogin;
			}
			
			
			
			//Otherwise it is invalid login
			Map<String,String> invalidPassword = new HashMap<>();
			
			invalidPassword.put("Result","-2");
			invalidPassword.put("Message","INVALID_PASSWORD");
			
			return invalidPassword;
			
			
			
		}
		catch(WebClientException e) {
			
			e.printStackTrace();
			Map<String,String> er = new HashMap<>();
			er.put("Result","-100");
			er.put("Message","EXCEPTION");
			er.put("Error", e.getMessage());
			return er;
		}
		catch(Exception e) {
			
			e.printStackTrace();
			Map<String,String> er = new HashMap<>();
			er.put("Result","-100");
			er.put("Message","EXCEPTION");
			er.put("Error", e.getMessage());
			return er;
		}
		
		
	}

	public Map<String,String> tcsChangePassword(Map<String,Object> jsonRequest) {
		
		

		String xml = MatcherHelper.convert(jsonRequest, tcsServiceInitializer.getTcsFunctionChangePassword());
		
		String uri = String.format("%s", tcsServiceInitializer.getTcsHost());

		try {
			
			
			Map<String,String> result = TcsHttpClientService.tcsCall(uri,xml,httpClient);
			
			//Case When TCS replys success change password
			if(result.get("Result").equalsIgnoreCase("0")) {
				

				Map<String,String> successLogin = new HashMap<>();
				
				successLogin.put("Result","0");
				successLogin.put("Message","SUCESS_LOGIN");
				
				xml = MatcherHelper.convert(jsonRequest, tcsServiceInitializer.getTcsFunctionGetMyAccountInfo());
				
				Map<String,String> resultInfo = TcsHttpClientService.tcsCall(uri,xml,httpClient);
				
				successLogin.put("Account_ID", resultInfo.get("param1"));
				successLogin.put("MSISDN", resultInfo.get("param2"));
				
				String user_profile = String.format("%s:%s:%s:%s", jsonRequest.get("username").toString(),
						jsonRequest.get("new_password").toString(),
						resultInfo.get("param1"),
						resultInfo.get("param2"));
				
				String jwt = securityService.getJWTToken(jsonRequest.get("username").toString(),user_profile);
				
				successLogin.put("token",jwt);
				
				return successLogin;
			}
			
			
			//Otherwise it is failed
			Map<String,String> invalidPassword = new HashMap<>();
			
			invalidPassword.put("Result","-1");
			invalidPassword.put("Message","FAILED_PASSWORD_CHANGE");
			
			return invalidPassword;
			
			
			
		}
		catch(WebClientException e) {
			
			e.printStackTrace();
			Map<String,String> er = new HashMap<>();
			er.put("Result","-100");
			er.put("Message","EXCEPTION");
			er.put("Error", e.getMessage());
			return er;
		}
		catch(Exception e) {
			
			e.printStackTrace();
			Map<String,String> er = new HashMap<>();
			er.put("Result","-100");
			er.put("Message","EXCEPTION");
			er.put("Error", e.getMessage());
			return er;
		}
		
		
	}

	public List<String> tcsGetCorePrivileges(Map<String,Object> jsonRequest){
		
		String xml = MatcherHelper.convert(jsonRequest, tcsServiceInitializer.getTcsFunctionGetPrivileges());
		
		String uri = String.format("%s", tcsServiceInitializer.getTcsHost());

		try {
			
			
			Map<String,String> result = TcsHttpClientService.tcsCall(uri,xml,httpClient);
			
			//Case When TCS replys success login, password is valid, password is permanent
			//Return Token
			if(result.get("Result").equalsIgnoreCase("0")) {
				

				List<String> r = new ArrayList<>();
				
				if(result != null) {
					
					for (Map.Entry<String, String> entry : result.entrySet()) {
				        
				        if(entry.getKey().startsWith("param")) {
				        	
				        	r.add(entry.getValue());
				        }
				    }
					
					return r;
				}
			}
			
		}
		catch(WebClientException e) {
			
			e.printStackTrace();
			
		}
		catch(Exception e) {
			
			e.printStackTrace();
			
		}
		
		return null;
	}
	
	public List<String> tcsGetFilteredCorePrivileges(Map<String,Object> jsonRequest){
		
		//Added 26042022 to get specific privilege
		
		String xml = MatcherHelper.convert(jsonRequest, tcsServiceInitializer.getTCSFunctionFilteredCorePrivilegs());
		
		String uri = String.format("%s", tcsServiceInitializer.getTcsHost());

		try {
			
			
			Map<String,String> result = TcsHttpClientService.tcsCall(uri,xml,httpClient);
			
			
			//
			if(result.get("Result").equalsIgnoreCase("0")) {
				

				List<String> r = new ArrayList<>();
				
				if(result != null) {
					
					for (Map.Entry<String, String> entry : result.entrySet()) {
				        
				        if(entry.getKey().startsWith("param")) {
				        	
				        	r.add(entry.getValue());
				        }
				    }
					
					return r;
				}
			}
			
		}
		catch(WebClientException e) {
			
			e.printStackTrace();
			
		}
		catch(Exception e) {
			
			e.printStackTrace();
			
		}
		
		return null;
	}
	
	public List<String> tcsGetFilteredUserCorePrivileges(Map<String,Object> jsonRequest){
		
		//Added 02102022 to get specific privilege for a specific user
		
		jsonRequest.put("username",tcsServiceInitializer.getTcsProxyUser());
		jsonRequest.put("password",tcsServiceInitializer.getTcsProxyPassword());
		
		String xml = MatcherHelper.convert(jsonRequest, tcsServiceInitializer.getTCSFunctionGetUserCorePrivileges());
		
		String uri = String.format("%s", tcsServiceInitializer.getTcsHost());

		try {
			
			
			Map<String,String> result = TcsHttpClientService.tcsCall(uri,xml,httpClient);
			
			
			//
			if(result.get("Result").equalsIgnoreCase("0")) {
				

				List<String> r = new ArrayList<>();
				
				if(result != null) {
					
					for (Map.Entry<String, String> entry : result.entrySet()) {
				        
				        if(entry.getKey().startsWith("param")) {
				        	
				        	r.add(entry.getValue());
				        }
				    }
					
					return r;
				}
			}
			
		}
		catch(WebClientException e) {
			
			e.printStackTrace();
			
		}
		catch(Exception e) {
			
			e.printStackTrace();
			
		}
		
		return null;
	}
	
	public String extractUserName(Map<String,String> headers) throws InvalidKeyException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, BadPaddingException, IllegalBlockSizeException, InvalidKeySpecException, UnsupportedEncodingException {
		
		Claims claims = securityService.validateToken(headers);
		
		String profile = securityService.decryptClaim(claims.get("user_profile").toString());
		
		String[] p = profile.split(":");
		
		return p[0];
	}

	public String extractUserPassword(Map<String,String> headers) throws InvalidKeyException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, BadPaddingException, IllegalBlockSizeException, InvalidKeySpecException, UnsupportedEncodingException {
		
		Claims claims = securityService.validateToken(headers);
		
		String profile = securityService.decryptClaim(claims.get("user_profile").toString());
		
		String[] p = profile.split(":");
		
		return p[1];
	}
	
	public String extractUserAccountId(Map<String,String> headers) throws InvalidKeyException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, BadPaddingException, IllegalBlockSizeException, InvalidKeySpecException, UnsupportedEncodingException {
		
		Claims claims = securityService.validateToken(headers);
		
		String profile = securityService.decryptClaim(claims.get("user_profile").toString());
		
		String[] p = profile.split(":");
		
		return p[2];
	}
	
	public String extractUserAccountNumber(Map<String,String> headers) throws InvalidKeyException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, BadPaddingException, IllegalBlockSizeException, InvalidKeySpecException, UnsupportedEncodingException {
		
		Claims claims = securityService.validateToken(headers);
		
		String profile = securityService.decryptClaim(claims.get("user_profile").toString());
		
		String[] p = profile.split(":");
		
		return p[3];
	}

	public String extractUserId(Map<String,String> headers) throws InvalidKeyException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, BadPaddingException, IllegalBlockSizeException, InvalidKeySpecException, UnsupportedEncodingException {
		
		Claims claims = securityService.validateToken(headers);
		
		String profile = securityService.decryptClaim(claims.get("user_profile").toString());
		
		String[] p = profile.split(":");
		
		return p[4];
	}
	
	public String extractUserMobile(Map<String,String> headers) throws InvalidKeyException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, BadPaddingException, IllegalBlockSizeException, InvalidKeySpecException, UnsupportedEncodingException {
		
		Claims claims = securityService.validateToken(headers);
		
		String profile = securityService.decryptClaim(claims.get("user_profile").toString());
		
		String[] p = profile.split(":");
		
		return p[5];
	}
	
	public String extractUniversalID(Map<String,String> headers) throws InvalidKeyException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, BadPaddingException, IllegalBlockSizeException, InvalidKeySpecException, UnsupportedEncodingException {
		
		Claims claims = securityService.validateToken(headers);
		
		String profile = securityService.decryptClaim(claims.get("user_profile").toString());
		
		String[] p = profile.split(":");
		
		return p[6];
	}
	
	public String extractUserEmail(Map<String,String> headers) throws InvalidKeyException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, BadPaddingException, IllegalBlockSizeException, InvalidKeySpecException, UnsupportedEncodingException {
		
		Claims claims = securityService.validateToken(headers);
		
		String profile = securityService.decryptClaim(claims.get("user_profile").toString());
		
		String[] p = profile.split(":");
		
		return p[7];
	}

	public String is2FAEnabled() {
		
		return tcsServiceInitializer.getTwoFactorAuthEnabled();
	}
	
}
