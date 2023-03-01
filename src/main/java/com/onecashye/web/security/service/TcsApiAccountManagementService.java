package com.onecashye.web.security.service;

import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClientException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.onecashye.web.security.helper.MappingParamsHelper;
import com.onecashye.web.security.helper.MatcherHelper;

import reactor.netty.http.client.HttpClient;

@Service
public class TcsApiAccountManagementService {

	Logger logger = LogManager.getLogger(TcsApiAccountManagementService.class);
	
	private final TcsServiceInitializer tcsServiceInitializer;
	private HttpClient httpClient;
	
	@Autowired
	public TcsApiAccountManagementService(TcsServiceInitializer tcsServiceInitializer) {
		super();
		this.tcsServiceInitializer = tcsServiceInitializer;
		
		httpClient = HttpClientService.getHttpClient(this.tcsServiceInitializer.getTcsCallTimeout());
	}
	
	public Map<String,String> tcsCreateTerminal(Map<String,Object> jsonRequest,Map<String, String> headers){
		
		/*
		 * To create a terminal user, only WEB users are allowed
		 */

		try {
			
			logger.info("Starting tcsCreateTerminal service ..");
			
			String xml = MatcherHelper.convert(jsonRequest, tcsServiceInitializer.getTcsFunctionSignupUser());
			
			String uri = String.format("%s", tcsServiceInitializer.getTcsHost());
			
			xml = cleanseSignupUserRequest(jsonRequest, xml);
			
			Map<String,String> result = TcsHttpClientService.tcsCall(uri,xml,httpClient);
			
			ObjectMapper objectMapper = new ObjectMapper();
			
			String json = objectMapper.writeValueAsString(result);
			
			logger.info(String.format("tcsCreateTerminal Service Result:%s", json));
			
			//Case When TCS replys success 
			if(result.get("Result").equalsIgnoreCase("0")) {
				
				return MappingParamsHelper.mapSimpleResponse(result);
			}
			
			//Otherwise it is failed
			Map<String,String> failed = new HashMap<>();
			
			failed.put("Result","-1");
			failed.put("Message",result.get("Message"));
			
			return failed;
			
		}
		catch(WebClientException e) {
			
			logger.error(String.format("Exception: %s",e.getMessage()));
			
			e.printStackTrace();
			Map<String,String> er = new HashMap<>();
			er.put("Result","-100");
			er.put("Message","EXCEPTION");
			er.put("Error", e.getMessage());
			return er;
		}
		catch(Exception e) {
			
			logger.error(String.format("Exception: %s",e.getMessage()));
			
			e.printStackTrace();
			Map<String,String> er = new HashMap<>();
			er.put("Result","-100");
			er.put("Message","EXCEPTION");
			er.put("Error", e.getMessage());
			return er;
		}
		
	}
	
	public Map<String,String> tcsSetAlias(Map<String,Object> jsonRequest,Map<String, String> headers){
		
		/*
		 * To set AUTO alias/tillcode for a created terminal (WEB Only)
		 */

		try {
			
			logger.info("Starting tcsSetAlias service ..");
			
			String xml = MatcherHelper.convert(jsonRequest, tcsServiceInitializer.getTcsFunctionSetAlias());
			
			String uri = String.format("%s", tcsServiceInitializer.getTcsHost());
			
			Map<String,String> result = TcsHttpClientService.tcsCall(uri,xml,httpClient);
			
			ObjectMapper objectMapper = new ObjectMapper();
			
			String json = objectMapper.writeValueAsString(result);
			
			logger.info(String.format("tcsSetAlias Service Result:%s", json));
			
			//Case When TCS replys success 
			if(result.get("Result").equalsIgnoreCase("0")) {
				
				return MappingParamsHelper.mapSimpleResponse(result);
			}
			
			//Otherwise it is failed
			Map<String,String> failed = new HashMap<>();
			
			failed.put("Result","-1");
			failed.put("Message",result.get("Message"));
			
			return failed;
			
		}
		catch(WebClientException e) {
			
			logger.error(String.format("Exception: %s",e.getMessage()));
			
			e.printStackTrace();
			Map<String,String> er = new HashMap<>();
			er.put("Result","-100");
			er.put("Message","EXCEPTION");
			er.put("Error", e.getMessage());
			return er;
		}
		catch(Exception e) {
			
			logger.error(String.format("Exception: %s",e.getMessage()));
			
			e.printStackTrace();
			Map<String,String> er = new HashMap<>();
			er.put("Result","-100");
			er.put("Message","EXCEPTION");
			er.put("Error", e.getMessage());
			return er;
		}
		
	}
	
	public Map<String,String> tcsChangeUserStatus(Map<String,Object> jsonRequest,Map<String, String> headers){
		

		try {
			
			logger.info("Starting tcsChangeUSerStatus service ..");
			
			String xml = MatcherHelper.convert(jsonRequest, tcsServiceInitializer.getTcsFunctionChangeUserStatus());
			
			String uri = String.format("%s", tcsServiceInitializer.getTcsHost());
			
			Map<String,String> result = TcsHttpClientService.tcsCall(uri,xml,httpClient);
			
			ObjectMapper objectMapper = new ObjectMapper();
			
			String json = objectMapper.writeValueAsString(result);
			
			logger.info(String.format("tcsChangeUserStatus Service Result:%s", json));
			
			
			//Case When TCS replys success 
			if(result.get("Result").equalsIgnoreCase("0")) {
				
				return MappingParamsHelper.mapSimpleResponse(result);
			}
			
			//Otherwise it is failed
			Map<String,String> failed = new HashMap<>();
			
			failed.put("Result","-1");
			failed.put("Message",result.get("Message"));
			
			return failed;
			
		}
		catch(WebClientException e) {
			
			logger.error(String.format("Exception: %s",e.getMessage()));
			
			e.printStackTrace();
			Map<String,String> er = new HashMap<>();
			er.put("Result","-100");
			er.put("Message","EXCEPTION");
			er.put("Error", e.getMessage());
			return er;
		}
		catch(Exception e) {
			
			logger.error(String.format("Exception: %s",e.getMessage()));
			
			e.printStackTrace();
			Map<String,String> er = new HashMap<>();
			er.put("Result","-100");
			er.put("Message","EXCEPTION");
			er.put("Error", e.getMessage());
			return er;
		}
		
	}
	
	public Map<String,String> tcsResetUser(Map<String,Object> jsonRequest,Map<String, String> headers){
		

		try {
			
			logger.info("Starting tcsResetUser service ..");
			
			String xml = MatcherHelper.convert(jsonRequest, tcsServiceInitializer.getTcsFunctionResetMyUser());
			
			String uri = String.format("%s", tcsServiceInitializer.getTcsHost());
			
			Map<String,String> result = TcsHttpClientService.tcsCall(uri,xml,httpClient);
			
			ObjectMapper objectMapper = new ObjectMapper();
			
			String json = objectMapper.writeValueAsString(result);
			
			logger.info(String.format("tcsResetUser Service Result:%s", json));
			
			
			//Case When TCS replys success 
			if(result.get("Result").equalsIgnoreCase("0")) {
				
				return MappingParamsHelper.mapSimpleResponse(result);
			}
			
			//Otherwise it is failed
			Map<String,String> failed = new HashMap<>();
			
			failed.put("Result","-1");
			failed.put("Message",result.get("Message"));
			
			return failed;
			
		}
		catch(WebClientException e) {
			
			logger.error(String.format("Exception: %s",e.getMessage()));
			
			e.printStackTrace();
			Map<String,String> er = new HashMap<>();
			er.put("Result","-100");
			er.put("Message","EXCEPTION");
			er.put("Error", e.getMessage());
			return er;
		}
		catch(Exception e) {
			
			logger.error(String.format("Exception: %s",e.getMessage()));
			
			e.printStackTrace();
			Map<String,String> er = new HashMap<>();
			er.put("Result","-100");
			er.put("Message","EXCEPTION");
			er.put("Error", e.getMessage());
			return er;
		}
		
	}

	public Map<String,String> tcsAddStaff(Map<String,Object> jsonRequest,Map<String, String> headers){
		

		try {
			
			logger.info("Starting tcsAddStaff service ..");
			
			String xml = MatcherHelper.convert(jsonRequest, tcsServiceInitializer.getTcsFunctionAddStaff());
			
			String uri = String.format("%s", tcsServiceInitializer.getTcsHost());
			
			String replace;
			
			if(jsonRequest.get("email") == null) {
				
				logger.info(String.format("Email provided is null"));
				
				replace = String.format("<param4>{email}</param4>");
				
				xml = xml.replace(replace, "");
				
			}
			
			Map<String,String> result = TcsHttpClientService.tcsCall(uri,xml,httpClient);
			
			ObjectMapper objectMapper = new ObjectMapper();
			
			String json = objectMapper.writeValueAsString(result);
			
			logger.info(String.format("tcsAddStaff Service Result:%s", json));
			
			//Case When TCS replys success 
			if(result.get("Result").equalsIgnoreCase("0")) {
				
				return MappingParamsHelper.mapSimpleResponse(result);
			}
			
			//Otherwise it is failed
			Map<String,String> failed = new HashMap<>();
			
			failed.put("Result","-1");
			failed.put("Message",result.get("Message"));
			
			return failed;
			
		}
		catch(WebClientException e) {
			
			logger.error(String.format("Exception: %s",e.getMessage()));
			
			e.printStackTrace();
			Map<String,String> er = new HashMap<>();
			er.put("Result","-100");
			er.put("Message","EXCEPTION");
			er.put("Error", e.getMessage());
			return er;
		}
		catch(Exception e) {
			
			logger.error(String.format("Exception: %s",e.getMessage()));
			
			e.printStackTrace();
			Map<String,String> er = new HashMap<>();
			er.put("Result","-100");
			er.put("Message","EXCEPTION");
			er.put("Error", e.getMessage());
			return er;
		}
		
	}

	public Map<String,String> tcsDeleteStaff(Map<String,Object> jsonRequest,Map<String, String> headers){
		

		try {
			
			logger.info("Starting tcsDeleteStaff service ..");
			
			String xml = MatcherHelper.convert(jsonRequest, tcsServiceInitializer.getTcsFunctionDeleteStaff());
			
			String uri = String.format("%s", tcsServiceInitializer.getTcsHost());
			
			Map<String,String> result = TcsHttpClientService.tcsCall(uri,xml,httpClient);
			
			ObjectMapper objectMapper = new ObjectMapper();
			
			String json = objectMapper.writeValueAsString(result);
			
			logger.info(String.format("tcsDeleteStaff Service Result:%s", json));
			
			//Case When TCS replys success 
			if(result.get("Result").equalsIgnoreCase("0")) {
				
				return MappingParamsHelper.mapSimpleResponse(result);
			}
			
			//Otherwise it is failed
			Map<String,String> failed = new HashMap<>();
			
			failed.put("Result","-1");
			failed.put("Message",result.get("Message"));
			
			return failed;
			
		}
		catch(WebClientException e) {
			
			logger.error(String.format("Exception: %s",e.getMessage()));
			
			e.printStackTrace();
			Map<String,String> er = new HashMap<>();
			er.put("Result","-100");
			er.put("Message","EXCEPTION");
			er.put("Error", e.getMessage());
			return er;
		}
		catch(Exception e) {
			
			logger.error(String.format("Exception: %s",e.getMessage()));
			
			e.printStackTrace();
			Map<String,String> er = new HashMap<>();
			er.put("Result","-100");
			er.put("Message","EXCEPTION");
			er.put("Error", e.getMessage());
			return er;
		}
		
	}

	public Map<String,String> tcsResetStaff(Map<String,Object> jsonRequest,Map<String, String> headers){
		

		try {
			
			logger.info("Starting tcsResetStaff service ..");
			
			String xml = MatcherHelper.convert(jsonRequest, tcsServiceInitializer.getTcsFunctionResetStaff());
			
			String uri = String.format("%s", tcsServiceInitializer.getTcsHost());
			
			Map<String,String> result = TcsHttpClientService.tcsCall(uri,xml,httpClient);
			
			ObjectMapper objectMapper = new ObjectMapper();
			
			String json = objectMapper.writeValueAsString(result);
			
			logger.info(String.format("tcsResetStaff Service Result:%s", json));
			
			//Case When TCS replys success 
			if(result.get("Result").equalsIgnoreCase("0")) {
				
				return MappingParamsHelper.mapSimpleResponse(result);
			}
			
			//Otherwise it is failed
			Map<String,String> failed = new HashMap<>();
			
			failed.put("Result","-1");
			failed.put("Message",result.get("Message"));
			
			return failed;
			
		}
		catch(WebClientException e) {
			
			logger.error(String.format("Exception: %s",e.getMessage()));
			
			e.printStackTrace();
			Map<String,String> er = new HashMap<>();
			er.put("Result","-100");
			er.put("Message","EXCEPTION");
			er.put("Error", e.getMessage());
			return er;
		}
		catch(Exception e) {
			
			logger.error(String.format("Exception: %s",e.getMessage()));
			
			e.printStackTrace();
			Map<String,String> er = new HashMap<>();
			er.put("Result","-100");
			er.put("Message","EXCEPTION");
			er.put("Error", e.getMessage());
			return er;
		}
		
	}

	private String cleanseSignupUserRequest(Map<String,Object> jsonRequest,String xml){
		
		String replace;
		
		if(jsonRequest.get("fname") == null || jsonRequest.get("fname").toString().isEmpty()) {
			
			replace = "<param14>{fname}</param14>";
			
			xml = xml.replace(replace, "");
			
		}
		
		if(jsonRequest.get("sname") == null || jsonRequest.get("sname").toString().isEmpty()) {
			
			replace = "<param15>{sname}</param15>";
			
			xml = xml.replace(replace, "");
			
		}
		
		if(jsonRequest.get("email") == null || jsonRequest.get("email").toString().isEmpty()) {
			
			replace = "<param13>{email}</param13>";
			
			xml = xml.replace(replace, "");
			
		}
		
		if(jsonRequest.get("nt_login") == null || jsonRequest.get("nt_login").toString().isEmpty()) {
			
			replace = "<param16>{nt_login}</param16>";
			
			xml = xml.replace(replace, "");
			
		}
		
		return xml;
	}

	public String normalizeNotificationNumber(Object number) {
		
		//Added 04052022
		//This to check the notification number of Created/Modified terminals to add 967 if number 
		//is only 9 digits, this is to avoid Telepin bug in transactions with terminals don't include
		//967 in numbers
		
		if(number == null)
			return "";
		
		String n=number.toString();
		
		if(n.length() != 9)
			return n;
		
		return "967"+n;
	}
}
