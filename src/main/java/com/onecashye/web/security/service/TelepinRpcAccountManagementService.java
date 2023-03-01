package com.onecashye.web.security.service;

import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClientException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.onecashye.web.security.helper.JsonHelper;
import com.onecashye.web.security.helper.MatcherHelper;

@Service
@PropertySource(ignoreResourceNotFound = true, value = "classpath:tcs.properties")
public class TelepinRpcAccountManagementService {

	Logger logger = LogManager.getLogger(TelepinRpcAccountManagementService.class);
	
	private final Environment env;
	
	@Autowired	
	public TelepinRpcAccountManagementService(Environment env) {
		this.env = env;
		
	}
	
	public Map<String,Object> rpcCall(Map<String,Object> jsonRequest,Map<String, String> headers){
		
		String cookie = getCookie(jsonRequest, headers);
		
		logger.info(String.format("Obtained Cookie:%s", cookie));
		
		for (Map.Entry<String, Object> entry : jsonRequest.entrySet()) {
	        
			logger.info(String.format("Key:%s", entry.getKey()));
			//logger.info(String.format("Value:%s", entry.getValue()));
			
	    }
		
		Map<String,Object> result=new HashMap<>();
		
		if(cookie != null) {
			
			try {
				
				String jsonBody = getApiBody(jsonRequest, headers);
				
				logger.info(String.format("JSON Body:%s", jsonBody));
				
				result = TcsHttpClientService.jsonRpcCall(jsonBody, 
						cookie,
						env.getProperty("tp.web.domain"),
						env.getProperty("tp.web.uri"),
						env.getProperty("tp.web.path"));
				
				//Case When Telepin RPC replys success 
			
				if(result.get("result") != null && result.get("result").toString().equalsIgnoreCase("0")) {
					
					ObjectMapper objectMapper = new ObjectMapper();
					
					String json = objectMapper.writeValueAsString(result);
					
					logger.info(String.format("RPC Call Service Result:%s", json));
					
					//Bug Fix: 12042022
					//Logout after each RPC call
					
					logger.info(String.format("Start RPC Logout:%s", cookie));
					
					TcsHttpClientService.jsonRpcDoLogout(cookie,
							env.getProperty("tp.web.domain"),
							env.getProperty("tp.web.uri"),
							env.getProperty("tp.web.path"));
					
					logger.info(String.format("Cookie:%s is destroyed", cookie));
					/////////////////////////////////////////////////////////////
					
					Map<String,Object> success = new HashMap<>();
					
					success.put("Result","0");
					success.put("Message","REQUEST_SUCESS");
					success.put("RPC_Response",result);
					
					return success;
				}
				
				//Otherwise it is failed
				Map<String,Object> failed = new HashMap<>();
				
				failed.put("Result","-1");
				failed.put("Message","REQUEST_FAILD");
				failed.put("RPC_Response",result);
				
				return failed;
				
			}
			catch(WebClientException e) {
				
				e.printStackTrace();
				Map<String,Object> er = new HashMap<>();
				er.put("Result","-100");
				er.put("Message","EXCEPTION");
				er.put("Error", e.getMessage());
				return er;
			}
			catch(Exception e) {
				
				e.printStackTrace();
				Map<String,Object> er = new HashMap<>();
				er.put("Result","-100");
				er.put("Message","EXCEPTION");
				er.put("Error", e.getMessage());
				return er;
			}
		}
		else {
			
			result.put("Result", String.format("%d", -1));
			result.put("Message", "INVALID_PASSWORD");
			
			return result;
		}
	}
	
	public Map<String,Object> rpcCall4SetAlias(Map<String,Object> jsonRequest,Map<String, String> headers){
		
		String cookie = getCookie(jsonRequest, headers);
		
		logger.info(String.format("Obtained Cookie:%s", cookie));
		
		for (Map.Entry<String, Object> entry : jsonRequest.entrySet()) {
	        
			logger.info(String.format("Key:%s", entry.getKey()));
			//logger.info(String.format("Value:%s", entry.getValue()));
			
	    }
		
		Map<String,Object> result=new HashMap<>();
		
		if(cookie != null) {
			
			try {
				
				String jsonBody = getApiBody(jsonRequest, headers);
				
				logger.info(String.format("JSON Body:%s", jsonBody));
				
				result = TcsHttpClientService.jsonRpcCall(jsonBody, 
						cookie,
						env.getProperty("tp.web.domain"),
						env.getProperty("tp.web.uri"),
						env.getProperty("tp.web.path"));
				
				//Case When Telepin RPC replys success 
				//With witness of Marzooq Result <>0 when success
				//if(result.get("result") != null && result.get("result").toString().equalsIgnoreCase("0"))
				if(result.get("result") != null) {
					
					ObjectMapper objectMapper = new ObjectMapper();
					
					String json = objectMapper.writeValueAsString(result);
					
					logger.info(String.format("RPC Call Service Result:%s", json));
					
					//Bug Fix: 12042022
					//Logout after each RPC call
					
					logger.info(String.format("Start RPC Logout:%s", cookie));
					
					TcsHttpClientService.jsonRpcDoLogout(cookie,
							env.getProperty("tp.web.domain"),
							env.getProperty("tp.web.uri"),
							env.getProperty("tp.web.path"));
					
					logger.info(String.format("Cookie:%s is destroyed", cookie));
					/////////////////////////////////////////////////////////////
					
					Map<String,Object> success = new HashMap<>();
					
					success.put("Result","0");
					success.put("Message","REQUEST_SUCESS");
					success.put("RPC_Response",result);
					
					return success;
				}
				
				//Otherwise it is failed
				Map<String,Object> failed = new HashMap<>();
				
				failed.put("Result","-1");
				failed.put("Message","REQUEST_FAILD");
				failed.put("RPC_Response",result);
				
				return failed;
				
			}
			catch(WebClientException e) {
				
				e.printStackTrace();
				Map<String,Object> er = new HashMap<>();
				er.put("Result","-100");
				er.put("Message","EXCEPTION");
				er.put("Error", e.getMessage());
				return er;
			}
			catch(Exception e) {
				
				e.printStackTrace();
				Map<String,Object> er = new HashMap<>();
				er.put("Result","-100");
				er.put("Message","EXCEPTION");
				er.put("Error", e.getMessage());
				return er;
			}
		}
		else {
			
			result.put("Result", String.format("%d", -1));
			result.put("Message", "INVALID_PASSWORD");
			
			return result;
		}
	}
	
	public Map<String,Object> rpcSearchCall(Map<String,Object> jsonRequest,Map<String, String> headers){
		
		String cookie = getCookie(jsonRequest, headers);
		Map<String,Object> result=new HashMap<>();
		
		if(cookie != null) {
			
			try {
				
				String jsonBody = MatcherHelper.convert(jsonRequest, getApiBody(jsonRequest, headers) );
				
				logger.info(String.format("JSON Body:%s", jsonBody));
				
				result = TcsHttpClientService.jsonRpcCall(jsonBody, 
						cookie,
						env.getProperty("tp.web.domain"),
						env.getProperty("tp.web.uri"),
						env.getProperty("tp.web.path"));
				
				if(result != null) {
					
					ObjectMapper objectMapper = new ObjectMapper();
					
					String json = objectMapper.writeValueAsString(result);
					
					logger.info(String.format("RPC Search Call Service Result:%s", json));
					
					//Bug Fix: 12042022
					//Logout after each RPC call
					
					logger.info(String.format("Start RPC Logout:%s", cookie));
					
					TcsHttpClientService.jsonRpcDoLogout(cookie,
							env.getProperty("tp.web.domain"),
							env.getProperty("tp.web.uri"),
							env.getProperty("tp.web.path"));
					
					logger.info(String.format("Cookie:%s is destroyed", cookie));
					/////////////////////////////////////////////////////////////
				}
				
				return result;
				
			}
			catch(WebClientException e) {
				
				e.printStackTrace();
				Map<String,Object> er = new HashMap<>();
				er.put("Result","-100");
				er.put("Message","EXCEPTION");
				er.put("Error", e.getMessage());
				return er;
			}
			catch(Exception e) {
				
				e.printStackTrace();
				Map<String,Object> er = new HashMap<>();
				er.put("Result","-100");
				er.put("Message","EXCEPTION");
				er.put("Error", e.getMessage());
				return er;
			}
		}
		else {
			
			result.put("Result", -1);
			result.put("Message", "INVALID_PASSWORD");
			
			return result;
		}
	}
	
	private String getCookie(Map<String,Object> jsonRequest,Map<String, String> headers) {
		
		String cookie="";
		Map<String,Object> result=new HashMap<>();
		
		//Step 1
		try {
			

			result = TcsHttpClientService.jsonRpcDoLogin(jsonRequest.get("username").toString(),
					jsonRequest.get("password").toString(),
					env.getProperty("tp.web.domain"),
					env.getProperty("tp.web.uri"),
					env.getProperty("tp.web.path"));
			
			
			if(result.get("result").toString().equalsIgnoreCase("0")) {
				
				cookie = result.get("Session").toString();
				
				return cookie;
			}
			else {
				
				return null;
			}
		}
		catch(Exception e) {
			
			return null;
		}
	}

	private String getApiBody(Map<String,Object> jsonRequest,Map<String, String> headers) {
		
		switch(jsonRequest.get("method").toString()) {
		
		case "TerminalUserRequest.createAccountTerminalRequest":
			
			return "{" + 
			"    \"method\": \"TerminalUserRequest.createAccountTerminalRequest\"," + 
			"    \"id\": 1," + 
			"    \"params\": [" + 
			"        1," + 
			"        \""+jsonRequest.get("user_mobile")+"\"," + 
			"        \""+jsonRequest.get("user_name")+"\"," + 
			"        \""+JsonHelper.stringBuilder(jsonRequest.get("first_name").toString())+"\"," + 
			"        \""+JsonHelper.checkStringObject(jsonRequest.get("last_name"))+"\"," + 
			"        \""+JsonHelper.checkStringObject(jsonRequest.get("nt_login"))+"\"," + 
			"        "+jsonRequest.get("interface_id")+"," + 
			"        \""+JsonHelper.checkStringObject(jsonRequest.get("email"))+"\"," + 
			"        \""+JsonHelper.checkStringObject(jsonRequest.get("id_number"))+"\"," +  
			"        \""+JsonHelper.checkStringObject(jsonRequest.get("id_type"))+"\"," +
			"        \""+JsonHelper.checkStringObject(jsonRequest.get("dob"))+"\"," + 
			"        "+jsonRequest.get("account_id")+"," +  
			"        true," + 
			"        \"\"," + 
			"        \""+JsonHelper.checkStringObject(jsonRequest.get("note"))+"\"" + 
			"    ]" + 
			"}";
			
		case "AccountTerminals.listMyAccountTerminals":
			
			return "{" + 
			"    \"method\": \"AccountTerminals.listMyAccountTerminals\"," + 
			"    \"id\": 1," + 
			"    \"params\": [" + 
			"        \"\"," + 
			"        \"0\"," + 
			"        300," + 
			"        1" + 
			"    ]" + 
			"}";
			
		case "AccountWebTerminals.addMyAccountWebTerminal":
			
			return "{" + 
			"    \"method\": \"AccountWebTerminals.addMyAccountWebTerminal\"," + 
			"    \"id\": 1," + 
			"    \"params\": [" + 
			"        \""+JsonHelper.checkStringObject(jsonRequest.get("user_name"))+"\"," + 
			"        \""+JsonHelper.stringBuilder(jsonRequest.get("fname"))+"\"," + 
			"        \""+JsonHelper.stringBuilder(jsonRequest.get("sname"))+"\"," +
			"        \""+JsonHelper.stringBuilder(jsonRequest.get("nt_login"))+"\"," +
			"        \""+JsonHelper.stringBuilder(jsonRequest.get("user_mobile"))+"\"," +
			"        "+JsonHelper.checkStringObject(jsonRequest.get("interface_id"))+"," +
			"        \""+JsonHelper.checkStringObject(jsonRequest.get("email"))+"\"," + 
			"        \""+JsonHelper.stringBuilder(jsonRequest.get("id_number"))+"\"," +
			"        \""+JsonHelper.stringBuilder(jsonRequest.get("id_type"))+"\"," +
			"        \""+JsonHelper.checkStringObject(jsonRequest.get("dob"))+"\"" + 
			"    ]" + 
			"}";
			
		case "HelperFunctions.addUserMsisdnAlias":
			
			return "{" + 
			"    \"method\": \"HelperFunctions.addUserMsisdnAlias\"," + 
			"    \"id\": 1," + 
			"    \"params\": [" + 
			"        \""+JsonHelper.checkStringObject(jsonRequest.get("user_name"))+"\"," + 
			"        false," + 
			"        \"\"" +
			"    ]" + 
			"}";
			
		case "AccountWebTerminals.readWEBUser":
			
			return "{" + 
			"    \"method\": \"AccountWebTerminals.readMyWEBUser\"," + 
			"    \"id\": 2," + 
			"    \"params\": [" + 
			"     "+JsonHelper.checkStringObject(jsonRequest.get("user_key"))+
			"    ]" + 
			"}";
			
		case "AccountWebTerminals.suspendMyAccountWebTerminal":
			
			return "{" + 
			"    \"method\": \"AccountWebTerminals.suspendMyAccountWebTerminal\"," + 
			"    \"id\": 1," + 
			"    \"params\": [" + 
			"     "+JsonHelper.checkStringObject(jsonRequest.get("user_key"))+
			"    ]" + 
			"}";
			
		case "AccountWebTerminals.modifyMyWEBUser":
			
			return "{" + 
			"    \"method\": \"AccountWebTerminals.modifyMyWEBUser\"," + 
			"    \"id\": 2," + 
			"    \"params\": [" + 
			"        "+JsonHelper.checkStringObject(jsonRequest.get("user_key"))+"," +
			"        \""+JsonHelper.checkStringObject(jsonRequest.get("user_mobile"))+"\"," + 
			"        \""+JsonHelper.stringBuilder(jsonRequest.get("fname"))+"\"," + 
			"        \""+JsonHelper.stringBuilder(jsonRequest.get("sname"))+"\"," +
			"        \""+JsonHelper.checkStringObject(jsonRequest.get("email"))+"\"," +
			"        \""+JsonHelper.stringBuilder(jsonRequest.get("id_number"))+"\"," +
			"        \""+JsonHelper.stringBuilder(jsonRequest.get("id_type"))+"\"," +
			"        \""+JsonHelper.checkStringObject(jsonRequest.get("dob"))+"\"," + 
			"        \""+JsonHelper.stringBuilder(jsonRequest.get("nt_login"))+"\"," +
			"        false," + 
			"        0," + 
			"        \"\"," + 
			"        0," + 
			"        \"\"," +
			"        \"\"" +
			
			
			"    ]" + 
			"}";
			
		case "AccountStaffTerminals.modifyMyStaffUser":
			
			return "{" + 
			"    \"method\": \"AccountStaffTerminals.modifyMyStaffUser\"," + 
			"    \"id\": 1," + 
			"    \"params\": [" + 
			"        "+JsonHelper.checkStringObject(jsonRequest.get("user_key"))+"," +
			"        \""+JsonHelper.checkStringObject(jsonRequest.get("user_mobile"))+"\"," + 
			"        \""+JsonHelper.stringBuilder(jsonRequest.get("fname"))+"\"," + 
			"        \""+JsonHelper.stringBuilder(jsonRequest.get("sname"))+"\"," +
			"        \""+JsonHelper.checkStringObject(jsonRequest.get("email"))+"\"," +
			"        \"\"," +
			"        \"\"," +
			"        \"\"," + 
			"        \"\"," +
			"        false," + 
			"        false," + 
			"        0," + 
			"        0" + 
			
			"    ]" + 
			"}";
			
			default:
				return null;
		}
	}
}
