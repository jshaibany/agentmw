package com.onecashye.web.security.controller;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.onecashye.web.security.exception.NullOrEmptyInputParameters;
import com.onecashye.web.security.exception.UserHasNoPrivileges;
import com.onecashye.web.security.helper.TcsParamsValidationHelper;
import com.onecashye.web.security.service.TcsApiAccountManagementService;
import com.onecashye.web.security.service.TcsApiAuthService;
import com.onecashye.web.security.service.TelepinRpcAccountManagementService;
import com.onecashye.web.security.telepin.service.TerminalService;

@RestController
@PropertySource({ "classpath:tcs.properties" })
public class AccountManagementController {

	Logger logger = LogManager.getLogger(AccountManagementController.class);
	
	private final TcsApiAuthService tcsApiAuthService;
	private final TcsApiAccountManagementService tcsApiAccountManagementService;
	private final TerminalService terminalService;
	private final TelepinRpcAccountManagementService telepinRpcAccountManagementService;
	
	private final Environment env;
	
	@Autowired
	public AccountManagementController(TcsApiAuthService tcsApiAuthService, 
			TcsApiAccountManagementService tcsApiAccountManagementService, 
			TerminalService terminalService, 
			TelepinRpcAccountManagementService telepinRpcAccountManagementService, 
			Environment env) {
		
		super();
		this.tcsApiAuthService = tcsApiAuthService;
		this.tcsApiAccountManagementService = tcsApiAccountManagementService;
		this.terminalService = terminalService;
		this.telepinRpcAccountManagementService = telepinRpcAccountManagementService;
		this.env = env;
		
	}
	
	@RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE, 
    		method = RequestMethod.GET,
    		value = "/v1/account/terminal/list")
	@ResponseBody
	public Map<String,Object> getAccountTerminals(@RequestHeader Map<String, String> headers){
		
		/*
		 * To list account Terminals
		 * 
		 */
		
		Map<String,Object> response = new HashMap<>();
		
		try {
			
			logger.info("Starting getAccountTerminals process ..");
			
			logger.info("Extract Account ID ..");
			
			String account_id=tcsApiAuthService.extractUserAccountId(headers);
			String user_name=tcsApiAuthService.extractUserName(headers);
			String password=tcsApiAuthService.extractUserPassword(headers);
			
			logger.info(String.format("Account ID found: %s",account_id));
			logger.info(String.format("User Name found: %s",user_name));
			
			Map<String,Object> request = new HashMap<>();
			
			request.put("username", user_name);
			request.put("password", password);
			request.put("filter", "WebRole");
			
			List<String> privileges = tcsApiAuthService.tcsGetFilteredCorePrivileges(request);
			
			if(privileges == null || privileges.size() <= 0) {
				
				//User has no WebRole* privileges
				throw new UserHasNoPrivileges("User has no privileges");
			}
			
			switch(privileges.get(0)) {
			
			case "WebRoleMasterManager":
				privileges.remove(0);
				privileges.add("WebRoleManager");
				privileges.add("WebRoleStaff");
				break;
			case "WebRoleManager":
				privileges.remove(0);
				privileges.add("WebRoleStaff");
				break;
			case "WebRoleStaff":
				throw new UserHasNoPrivileges("User has no privileges");
			}
			
			List<Map<String,Object>>  result = terminalService.getAccountTerminals(privileges,user_name,account_id, "WEB");
			
			response.put("Result", 0);
			response.put("Message", "OK");
			response.put("List", result);
			
			ObjectMapper objectMapper = new ObjectMapper();
			
			String json = objectMapper.writeValueAsString(response);
			
			logger.info(String.format("Out Result:%s", json));
			
			return response;
			
		}
		catch(UserHasNoPrivileges e) {
			
			response.put("Result", String.format("%d", -100));
			response.put("Message", "EXCEPTION");
			response.put("Error", e.getMessage());
			
			return response;
		}
		catch(Exception e) {
			
			logger.error("Exception is caught ..",e);
			logger.error(e.getMessage());
			
			response.put("Result", String.format("%d", -100));
			response.put("Message", "EXCEPTION");
			response.put("Error", e.getMessage());
			
			return response;
		}
	}
	
	@RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE, 
    		method = RequestMethod.GET,
    		value = "/v1/account/staff/list")
	@ResponseBody
	public Map<String,Object> getAccountStaffV2(@RequestHeader Map<String, String> headers){
		
		/*
		 * To list account Staff V2
		 * 
		 */
		
		Map<String,Object> response = new HashMap<>();
		
		try {
			
			logger.info("Starting getAccountStaffV2 process ..");
			
			logger.info("Extract Account ID ..");
			
			String account_id=tcsApiAuthService.extractUserAccountId(headers);
			String user_name=tcsApiAuthService.extractUserName(headers);
			String password=tcsApiAuthService.extractUserPassword(headers);
			
			logger.info(String.format("Account ID found: %s",account_id));
			logger.info(String.format("User Name found: %s",user_name));
			
			Map<String,Object> request = new HashMap<>();
			
			request.put("username", user_name);
			request.put("password", password);
			request.put("filter", "WebRole");
			
			List<String> privileges = tcsApiAuthService.tcsGetFilteredCorePrivileges(request);
			
			if(privileges == null || privileges.size() <= 0) {
				
				//User has no WebRole* privileges
				throw new UserHasNoPrivileges("User has no privileges");
			}
			
			switch(privileges.get(0)) {
			
			case "WebRoleMasterManager":
				privileges.remove(0);
				privileges.add("AppRoleManager");
				privileges.add("AppRoleStaff");
				break;
			case "WebRoleManager":
				privileges.remove(0);
				privileges.add("AppRoleStaff");
				break;
			case "WebRoleStaff":
				throw new UserHasNoPrivileges("User has no privileges");
			}
			
			List<Map<String,Object>>  result = terminalService.getAccountTerminals(privileges,user_name,account_id, "GPT");
			
			response.put("Result", 0);
			response.put("Message", "OK");
			response.put("List", result);
			
			ObjectMapper objectMapper = new ObjectMapper();
			
			String json = objectMapper.writeValueAsString(response);
			
			logger.info(String.format("Out Result:%s", json));
			
			return response;
			
		}
		catch(UserHasNoPrivileges e) {
			
			response.put("Result", String.format("%d", -100));
			response.put("Message", "EXCEPTION");
			response.put("Error", e.getMessage());
			
			return response;
		}
		catch(Exception e) {
			
			logger.error("Exception is caught ..",e);
			logger.error(e.getMessage());
			
			response.put("Result", String.format("%d", -100));
			response.put("Message", "EXCEPTION");
			response.put("Error", e.getMessage());
			
			return response;
		}
	}
	
	@Deprecated
	@RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE, 
    		method = RequestMethod.POST,
    		value = "/v1/account/terminal/signup")
	@ResponseBody
	public Map<String,Object> signupUser(@RequestBody Map<String,Object> jsonRequest,
			@RequestHeader Map<String, String> headers){
		
		//This API is Deprecated
		/*
		 * To create a new Terminal
		 * First check if Terminal (User) is already existed as TCS API SIGNUPUSER overwrites when creating new users
		 * 
		 *Update 08/03/2022:
		 *	
		 *	- Once the terminal is created successfully, SETALIAS API should be called
		 *	- The Alias should be AUTO mode
		 */
		
		Map<String,Object> response = new HashMap<>();
		
		try {
			
			logger.info("Starting signupUser process ..");
			logger.info("Checking input params ..");
			
			//Check if any parameter is null
			
			for (Map.Entry<String, Object> entry : jsonRequest.entrySet()) {
		        
				if(entry.getKey().equalsIgnoreCase("user_mobile") || 
	        			entry.getKey().equalsIgnoreCase("user_name") ||
	        			entry.getKey().equalsIgnoreCase("interface_id")) {
	        		
					
					if(entry.getValue()==null) {
						
						response.put("Result", String.format("%d", -200));
						response.put("Message", "NULL_OR_EMPTY_INPUT");
						response.put("Details", String.format("%s is required",entry.getKey()));
						
						return response;
					}
		        	
	        	}
		    }
			
			if(!terminalService.isTerminalExisted(jsonRequest.get("user_name").toString())) {
				
				//If not existed before, proceed with terminal signup ...
				
				logger.info("Terminal is not found, starting new terminal signup ...");
				
				jsonRequest.put("username",tcsApiAuthService.extractUserName(headers));
				jsonRequest.put("password",tcsApiAuthService.extractUserPassword(headers));
				jsonRequest.put("account_id",tcsApiAuthService.extractUserAccountId(headers));
				jsonRequest.put("universal_id",tcsApiAuthService.extractUniversalID(headers));
				jsonRequest.put("account_msisdn",tcsApiAuthService.extractUserAccountNumber(headers));
				
				Map<String,String> result = tcsApiAccountManagementService.tcsCreateTerminal(jsonRequest, headers);
				
				ObjectMapper objectMapper = new ObjectMapper();
				
				String json = objectMapper.writeValueAsString(result);
				
				logger.info(String.format("tcsCreateTerminal Result:%s", json));
				
				if(result.get("Result").equalsIgnoreCase("0")) {
					
					logger.info("Terminal is created successfully, Starting SETALIAS call ...");
					
					result = tcsApiAccountManagementService.tcsSetAlias(jsonRequest, headers);
					
					json = objectMapper.writeValueAsString(result);
					
					logger.info(String.format("tcsSetAlias Result:%s", json));
					
					if(result.get("Result").equalsIgnoreCase("0")) {
						
						logger.info("Alias has been set successfully");
						
					}
					else {
						
						logger.warn("Alias is failed to be set ... ");
					}
					
					return new HashMap<>(result);
				}
				
				
				
				return new HashMap<>(result);
			}
			else {
				
				//Return error
				//Exist
				logger.warn("Terminal already existed ... ");
				
	        	response.put("Result", String.format("%d", -200));
				response.put("Message", "TERMINAL_EXISTS");
				
				return response;
			}
		}
		catch(NullOrEmptyInputParameters e) {
			
			logger.warn("Input param is null  ..");
			logger.warn("Prepare NULL_OR_EMPTY_INPUT response  ..");
			
			response.put("Result", String.format("%d", -200));
			response.put("Message", "NULL_OR_EMPTY_INPUT");
			
			return response;
			
		}
		catch(Exception e) {
			
			logger.error("Exception is caught ..",e);
			logger.error(e.getMessage());
			
			response.put("Result", String.format("%d", -100));
			response.put("Message", "EXCEPTION");
			response.put("Error", e.getMessage());
			
			return response;
		}
		
	}
	
	@Deprecated
	@RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE, 
    		method = RequestMethod.POST,
    		value = "/v1/rpc/account/terminal/request")
	@ResponseBody
	public Map<String,Object> newTerminalRequest(@RequestBody Map<String,Object> jsonRequest,
			@RequestHeader Map<String, String> headers){
		
		//Deprecate this API
		
		/*
		 * To create a new Terminal Request (Telepin RPC)
		 * First check if Terminal (User) is already existed 
		 */
		
		Map<String,Object> response = new HashMap<>();
		
		try {
			
			logger.info("Starting newTerminalRequest process ..");
			logger.info("Checking input params ..");
			
			//Check if any parameter is null
			
			for (Map.Entry<String, Object> entry : jsonRequest.entrySet()) {
		        
				if(entry.getKey().equalsIgnoreCase("user_mobile") || 
	        			entry.getKey().equalsIgnoreCase("user_name") ||
	        			entry.getKey().equalsIgnoreCase("interface_id")) {
	        		
	        		//Exist
					
					if(entry.getValue()==null) {
						
						response.put("Result", String.format("%d", -200));
						response.put("Message", "NULL_OR_EMPTY_INPUT");
						response.put("Details", String.format("%s is required",entry.getKey()));
						
						return response;
					}
		        	
	        	}
		    }
			
			//!terminalService.isTerminalExisted(jsonRequest.get("user_name").toString())
			if(!terminalService.isTerminalExisted(jsonRequest.get("user_name").toString())) {
				
				//If not existed continue
				
				jsonRequest.put("username",tcsApiAuthService.extractUserName(headers));
				jsonRequest.put("password",tcsApiAuthService.extractUserPassword(headers));
				//jsonRequest.put("account_id",tcsApiAuthService.extractUserAccountId(headers));
				//jsonRequest.put("universal_id",tcsApiAuthService.extractUniversalID(headers));
				//jsonRequest.put("account_msisdn",tcsApiAuthService.extractUserAccountNumber(headers));
				
				jsonRequest.put("method", "TerminalUserRequest.createAccountTerminalRequest");
				
				Map<String,Object> r = telepinRpcAccountManagementService.rpcCall(jsonRequest, headers);
				
				return r;
			}	
			else {
				
				//Return error
				//Exist
	        	response.put("Result", String.format("%d", -200));
				response.put("Message", "TERMINAL_EXISTS");
				
				return response;
			}
			
			
		}
		
		catch(NullOrEmptyInputParameters e) {
			
			logger.warn("Input param is null  ..");
			logger.warn("Prepare NULL_OR_EMPTY_INPUT response  ..");
			
			response.put("Result", String.format("%d", -200));
			response.put("Message", "NULL_OR_EMPTY_INPUT");
			
			return response;
			
		}
		catch(Exception e) {
			
			logger.error("Exception is caught ..",e);
			logger.error(e.getMessage());
			
			response.put("Result", String.format("%d", -100));
			response.put("Message", "EXCEPTION");
			response.put("Error", e.getMessage());
			
			return response;
		}
		
	}
	
	@RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE, 
    		method = RequestMethod.POST,
    		value = "/v1/rpc/account/terminal/add")
	@ResponseBody
	public Map<String,Object> addWebTerminal(@RequestBody Map<String,Object> jsonRequest,
			@RequestHeader Map<String, String> headers){
		
		/*
		 * To create a new Terminal WEB (Telepin RPC)
		 * 
		 */
		
		Map<String,Object> response = new HashMap<>();
		
		try {
			
			logger.info("Starting addWebTerminal process ..");
			logger.info("Checking input params ..");
			
			//Check if any parameter is null
			
			logger.info(String.format("Input params, user_mobile=%s, user_name=%s, nt_login=%s, id_number=%s, id_type=%s, dob=%s, interface_id=%s, staff_code=%s, email=%s, fname=%s, sname=%s", 
					jsonRequest.get("user_mobile"),
					jsonRequest.get("user_name"),
					jsonRequest.get("nt_login"),
					jsonRequest.get("id_number"),
					jsonRequest.get("id_type"),
					jsonRequest.get("dob"),
					jsonRequest.get("interface_id"),
					jsonRequest.get("staff_code"),
					jsonRequest.get("email"),
					jsonRequest.get("fname"),
					jsonRequest.get("sname")));
			
			TcsParamsValidationHelper.checkRequiredParams(env, "tcs.required.params.terminal.add", jsonRequest, logger);
			
			//Added 16042022
			//To avoid API error because of space in email
			
			if(jsonRequest.get("email")!=null)
				jsonRequest.put("email", jsonRequest.get("email").toString().trim());
			///////////////////////////////////////////////////////////////////////////////////////
			
			jsonRequest.put("username",tcsApiAuthService.extractUserName(headers));
			jsonRequest.put("password",tcsApiAuthService.extractUserPassword(headers));
			
			//Added 04052022
			//To solve the 967 in notification mobile numbers
			
			jsonRequest.put("user_mobile", tcsApiAccountManagementService
					.normalizeNotificationNumber(jsonRequest.get("user_mobile")));
			
			////////////////////////////////////////////////////////////////////////////////////////
			
			jsonRequest.put("method", "AccountWebTerminals.addMyAccountWebTerminal");
			
			logger.info("Call AccountWebTerminals.addMyAccountWebTerminal ...");
			
			
			Map<String,Object> r = telepinRpcAccountManagementService.rpcCall(jsonRequest, headers);
			
			ObjectMapper objectMapper = new ObjectMapper();
			
			String json = objectMapper.writeValueAsString(r);
			
			logger.info(String.format("telepinRpcAccountManagementService.rpcCall Result:%s", json));
			
			return r;
			
		}
		catch(NullOrEmptyInputParameters e) {
			
			logger.warn("Input param is null  ..");
			logger.warn("Prepare NULL_OR_EMPTY_INPUT response  ..");
			
			response.put("Result", String.format("%d", -200));
			response.put("Message", "NULL_OR_EMPTY_INPUT");
			
			return response;
			
		}
		catch(Exception e) {
			
			logger.error("Exception is caught ..",e);
			logger.error(e.getMessage());
			
			response.put("Result", String.format("%d", -100));
			response.put("Message", "EXCEPTION");
			response.put("Error", e.getMessage());
			
			return response;
		}
		
	}
	
	@RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE, 
    		method = RequestMethod.POST,
    		value = "/v1/rpc/account/terminal/read")
	@ResponseBody
	public Map<String,Object> readWebTerminal(@RequestBody Map<String,Object> jsonRequest,
			@RequestHeader Map<String, String> headers){
		
		/*
		 * To read Terminal details (Telepin RPC)
		 * 
		 */
		
		Map<String,Object> response = new HashMap<>();
		
		try {
			
			logger.info("Starting readWebTerminal process ..");
			logger.info("Checking input params ..");
			
			logger.info(String.format("Input params, user_key=%s", 
					jsonRequest.get("user_key")));
			
			//Check if any parameter is null
			
			TcsParamsValidationHelper.checkRequiredParams(env, "tcs.required.params.terminal.read", jsonRequest, logger);
			
			jsonRequest.put("username",tcsApiAuthService.extractUserName(headers));
			jsonRequest.put("password",tcsApiAuthService.extractUserPassword(headers));
			
			jsonRequest.put("method", "AccountWebTerminals.readWEBUser");
			
			logger.info("Call AccountWebTerminals.readWEBUser ...");
			
			Map<String,Object> r = telepinRpcAccountManagementService.rpcSearchCall(jsonRequest, headers);
			
			ObjectMapper objectMapper = new ObjectMapper();
			
			String json = objectMapper.writeValueAsString(r);
			
			logger.info(String.format("telepinRpcAccountManagementService.rpcSearchCall Result:%s", json));
			
			if(r.get("result")!=null) {
				
				//There is some result returned
				
				logger.info("Result=" + r.get("result"));
				
				Object o = r.get("result");
				
				r.put("result", "0");
				r.put("terminal", o);
				
				return r;
			}
			
			r.put("result", "-1");
			r.put("terminal", null);
			r.put("error", r.get("error"));
			
			return r;
			
		}
		catch(NullOrEmptyInputParameters e) {
			
			logger.warn("Input param is null  ..");
			logger.warn("Prepare NULL_OR_EMPTY_INPUT response  ..");
			
			response.put("Result", String.format("%d", -200));
			response.put("Message", "NULL_OR_EMPTY_INPUT");
			
			return response;
			
		}
		catch(Exception e) {
			
			logger.error("Exception is caught ..",e);
			logger.error(e.getMessage());
			
			response.put("Result", String.format("%d", -100));
			response.put("Message", "EXCEPTION");
			response.put("Error", e.getMessage());
			
			return response;
		}
		
	}
	
	@RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE, 
    		method = RequestMethod.POST,
    		value = "/v1/rpc/account/terminal/alias/set")
	@ResponseBody
	public Map<String,Object> setWebTerminalAlias(@RequestBody Map<String,Object> jsonRequest,
			@RequestHeader Map<String, String> headers){
		
		/*
		 * update 12-03-2022
		 * As requested by Samir: Set Alias should be a separated API
		 * 
		 */
		
		Map<String,Object> response = new HashMap<>();
		
		try {
			
			logger.info("Starting setWebTerminalAlias process ..");
			logger.info("Checking input params ..");
			
			logger.info(String.format("Input params, user_name=%s", 
					jsonRequest.get("user_name")));
			
			//Check if any parameter is null
			
			TcsParamsValidationHelper.checkRequiredParams(env, "tcs.required.params.terminal.setalias", jsonRequest, logger);
			
			jsonRequest.put("username",tcsApiAuthService.extractUserName(headers));
			jsonRequest.put("password",tcsApiAuthService.extractUserPassword(headers));
			
			jsonRequest.put("method", "HelperFunctions.addUserMsisdnAlias");
			
			logger.info("Call HelperFunctions.addUserMsisdnAlias ...");
			
			Map<String,Object> r = telepinRpcAccountManagementService.rpcCall4SetAlias(jsonRequest, headers);
			
			ObjectMapper objectMapper = new ObjectMapper();
			
			String json = objectMapper.writeValueAsString(r);
			
			logger.info(String.format("telepinRpcAccountManagementService.rpcCall Result:%s", json));
			
			
			return r;
			
		}
		
		
		
		catch(NullOrEmptyInputParameters e) {
			
			logger.warn("Input param is null  ..");
			logger.warn("Prepare NULL_OR_EMPTY_INPUT response  ..");
			
			response.put("Result", String.format("%d", -200));
			response.put("Message", "NULL_OR_EMPTY_INPUT");
			
			return response;
			
		}
		catch(Exception e) {
			
			logger.error("Exception is caught ..",e);
			logger.error(e.getMessage());
			
			response.put("Result", String.format("%d", -100));
			response.put("Message", "EXCEPTION");
			response.put("Error", e.getMessage());
			
			return response;
		}
		
	}
	
	@RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE, 
    		method = RequestMethod.POST,
    		value = "/v1/rpc/account/terminal/modify")
	@ResponseBody
	public Map<String,Object> modifyWebTerminal(@RequestBody Map<String,Object> jsonRequest,
			@RequestHeader Map<String, String> headers){
		
		/*
		 * To modify Terminal WEB (Telepin RPC)
		 * 
		 */
		
		Map<String,Object> response = new HashMap<>();
		
		try {
			
			logger.info("Starting modifyWebTerminal process ..");
			logger.info("Checking input params ..");
			
			logger.info(String.format("Input params, user_mobile=%s, user_key=%s, nt_login=%s, id_number=%s, id_type=%s, dob=%s, email=%s, fname=%s, sname=%s", 
					jsonRequest.get("user_mobile"),
					jsonRequest.get("user_key"),
					jsonRequest.get("nt_login"),
					jsonRequest.get("id_number"),
					jsonRequest.get("id_type"),
					jsonRequest.get("dob"),
					jsonRequest.get("email"),
					jsonRequest.get("fname"),
					jsonRequest.get("sname")));
			
			//Check if any parameter is null
			
			TcsParamsValidationHelper.checkRequiredParams(env, "tcs.required.params.terminal.modify", jsonRequest, logger);
			
			//Added 16042022
			//To avoid API error because of space in email
			
			if(jsonRequest.get("email")!=null)
				jsonRequest.put("email", jsonRequest.get("email").toString().trim());
			///////////////////////////////////////////////////////////////////////////////////////
			
			jsonRequest.put("username",tcsApiAuthService.extractUserName(headers));
			jsonRequest.put("password",tcsApiAuthService.extractUserPassword(headers));
			//jsonRequest.put("account_id",tcsApiAuthService.extractUserAccountId(headers));
			//jsonRequest.put("universal_id",tcsApiAuthService.extractUniversalID(headers));
			//jsonRequest.put("account_msisdn",tcsApiAuthService.extractUserAccountNumber(headers));
			
			//Added 04052022
			//To solve the 967 in notification mobile numbers
			
			jsonRequest.put("user_mobile", tcsApiAccountManagementService
					.normalizeNotificationNumber(jsonRequest.get("user_mobile")));
			
			////////////////////////////////////////////////////////////////////////////////////////
			
			jsonRequest.put("method", "AccountWebTerminals.modifyMyWEBUser");
			
			Map<String,Object> r = telepinRpcAccountManagementService.rpcCall(jsonRequest, headers);
			
			ObjectMapper objectMapper = new ObjectMapper();
			
			String json = objectMapper.writeValueAsString(r);
			
			logger.info(String.format("telepinRpcAccountManagementService.rpcCall Result:%s", json));
			
			return r;
			
		}
		
		
		
		catch(NullOrEmptyInputParameters e) {
			
			logger.warn("Input param is null  ..");
			logger.warn("Prepare NULL_OR_EMPTY_INPUT response  ..");
			
			response.put("Result", String.format("%d", -200));
			response.put("Message", "NULL_OR_EMPTY_INPUT");
			
			return response;
			
		}
		catch(Exception e) {
			
			logger.error("Exception is caught ..",e);
			logger.error(e.getMessage());
			
			response.put("Result", String.format("%d", -100));
			response.put("Message", "EXCEPTION");
			response.put("Error", e.getMessage());
			
			return response;
		}
		
	}
	
	@RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE, 
    		method = RequestMethod.POST,
    		value = "/v1/rpc/account/terminal/terminate")
	@ResponseBody
	public Map<String,Object> terminateWebTerminal(@RequestBody Map<String,Object> jsonRequest,
			@RequestHeader Map<String, String> headers){
		
		/*
		 * To terminate Terminal WEB (Telepin RPC)
		 * 
		 */
		
		Map<String,Object> response = new HashMap<>();
		
		try {
			
			logger.info("Starting terminateWebTerminal process ..");
			logger.info("Checking input params ..");
			
			logger.info(String.format("Input params, user_key=%s", 
					
					jsonRequest.get("user_key")));
			
			//Check if any parameter is null
			
			TcsParamsValidationHelper.checkRequiredParams(env, "tcs.required.params.terminal.terminate", jsonRequest, logger);
			
			//Added 10052022
		
			
			jsonRequest.put("username",tcsApiAuthService.extractUserName(headers));
			jsonRequest.put("password",tcsApiAuthService.extractUserPassword(headers));
			
			jsonRequest.put("method", "AccountWebTerminals.suspendMyAccountWebTerminal");
			
			Map<String,Object> r = telepinRpcAccountManagementService.rpcCall(jsonRequest, headers);
			
			ObjectMapper objectMapper = new ObjectMapper();
			
			String json = objectMapper.writeValueAsString(r);
			
			logger.info(String.format("telepinRpcAccountManagementService.rpcCall Result:%s", json));
			
			return r;
			
		}
		
		
		
		catch(NullOrEmptyInputParameters e) {
			
			logger.warn("Input param is null  ..");
			logger.warn("Prepare NULL_OR_EMPTY_INPUT response  ..");
			
			response.put("Result", String.format("%d", -200));
			response.put("Message", "NULL_OR_EMPTY_INPUT");
			
			return response;
			
		}
		catch(Exception e) {
			
			logger.error("Exception is caught ..",e);
			logger.error(e.getMessage());
			
			response.put("Result", String.format("%d", -100));
			response.put("Message", "EXCEPTION");
			response.put("Error", e.getMessage());
			
			return response;
		}
		
	}
	
	@Deprecated
	@SuppressWarnings("unchecked")
	@RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE, 
    		method = RequestMethod.POST,
    		value = "/v1/rpc/account/terminals")
	@ResponseBody
	public Map<String,Object> listTerminals(@RequestBody Map<String,Object> jsonRequest,
			@RequestHeader Map<String, String> headers){
		//Deprecate this API
		/*
		 * To list Terminals (Telepin RPC)
		 * 
		 */
		
		Map<String,Object> response = new HashMap<>();
		
		try {
			
			logger.info("Starting listTerminals process ..");
			logger.info("Checking input params ..");
			
			jsonRequest.put("username",tcsApiAuthService.extractUserName(headers));
			jsonRequest.put("password",tcsApiAuthService.extractUserPassword(headers));
			
			jsonRequest.put("method", "AccountTerminals.listMyAccountTerminals");
			
			Map<String,Object> r = telepinRpcAccountManagementService.rpcSearchCall(jsonRequest, headers);
			
			ObjectMapper objectMapper = new ObjectMapper();
			
			String json = objectMapper.writeValueAsString(r);
			
			logger.info(String.format("telepinRpcAccountManagementService.rpcSearchCall Result:%s", json));
			
			if(r.get("result")!=null) {
				
				//There is some result returned
				
				logger.info("Result=" + r.get("result"));
				
				ArrayList<Object> o = (ArrayList<Object>) r.get("result");
				
				ArrayList<String> headColumns = (ArrayList<String>) o.get(0);
				
				o.remove(0);
				
				
				r.put("result", "0");
				r.put("header", headColumns);
				r.put("terminals", o);
				
				return r;
			}
			
			r.put("result", "-1");
			r.put("terminal", null);
			r.put("error", r.get("error"));
			
			return r;
			
		}
		catch(Exception e) {
			
			logger.error("Exception is caught ..",e);
			logger.error(e.getMessage());
			
			response.put("Result", String.format("%d", -100));
			response.put("Message", "EXCEPTION");
			response.put("Error", e.getMessage());
			
			return response;
		}
		
	}

	@RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE, 
    		method = RequestMethod.POST,
    		value = "/v1/account/terminal/suspend")
	@ResponseBody
	public Map<String,Object> suspendTerminal(@RequestBody Map<String,Object> jsonRequest,
			@RequestHeader Map<String, String> headers){
		
		/*
		 * To suspend Terminal
		 * 
		 */
		
		Map<String,Object> response = new HashMap<>();
		
		try {
			
			logger.info("Starting suspendTerminal process ..");
			logger.info("Checking input params ..");
			
			//Check if any parameter is null
			
			TcsParamsValidationHelper.checkRequiredParams(env, "tcs.required.params.terminal.username", jsonRequest, logger);
			
			jsonRequest.put("username",tcsApiAuthService.extractUserName(headers));
			jsonRequest.put("password",tcsApiAuthService.extractUserPassword(headers));
			jsonRequest.put("new_status", "D");
			jsonRequest.put("staff_code", "000");
			
			return new HashMap<>(tcsApiAccountManagementService.tcsChangeUserStatus(jsonRequest, headers));
			
			
			
		}
		catch(NullOrEmptyInputParameters e) {
			
			logger.warn("Input param is null  ..");
			logger.warn("Prepare NULL_OR_EMPTY_INPUT response  ..");
			
			response.put("Result", String.format("%d", -200));
			response.put("Message", "NULL_OR_EMPTY_INPUT");
			
			return response;
			
		}
		catch(Exception e) {
			
			logger.error("Exception is caught ..",e);
			logger.error(e.getMessage());
			
			response.put("Result", String.format("%d", -100));
			response.put("Message", "EXCEPTION");
			response.put("Error", e.getMessage());
			
			return response;
		}
	}
	
	@RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE, 
    		method = RequestMethod.POST,
    		value = "/v1/account/terminal/activate")
	@ResponseBody
	public Map<String,Object> activateTerminal(@RequestBody Map<String,Object> jsonRequest,
			@RequestHeader Map<String, String> headers){
		
		/*
		 * To activate a Terminal
		 * 
		 */
		
		Map<String,Object> response = new HashMap<>();
		
		try {
			
			logger.info("Starting activateTerminal process ..");
			logger.info("Checking input params ..");
			
			//Check if any parameter is null
			
			TcsParamsValidationHelper.checkRequiredParams(env, "tcs.required.params.terminal.username", jsonRequest, logger);
			
			jsonRequest.put("username",tcsApiAuthService.extractUserName(headers));
			jsonRequest.put("password",tcsApiAuthService.extractUserPassword(headers));
			jsonRequest.put("new_status", "A");
			jsonRequest.put("staff_code", "000");
			
			return new HashMap<>(tcsApiAccountManagementService.tcsChangeUserStatus(jsonRequest, headers));
			
			
		}
		catch(NullOrEmptyInputParameters e) {
			
			logger.warn("Input param is null  ..");
			logger.warn("Prepare NULL_OR_EMPTY_INPUT response  ..");
			
			response.put("Result", String.format("%d", -200));
			response.put("Message", "NULL_OR_EMPTY_INPUT");
			
			return response;
			
		}
		catch(Exception e) {
			
			logger.error("Exception is caught ..",e);
			logger.error(e.getMessage());
			
			response.put("Result", String.format("%d", -100));
			response.put("Message", "EXCEPTION");
			response.put("Error", e.getMessage());
			
			return response;
		}
	}
	
	@RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE, 
    		method = RequestMethod.POST,
    		value = "/v1/account/terminal/reset")
	@ResponseBody
	public Map<String,Object> resetTerminal(@RequestBody Map<String,Object> jsonRequest,
			@RequestHeader Map<String, String> headers){
		
		/*
		 * To reset a Terminal
		 * 
		 */
		
		Map<String,Object> response = new HashMap<>();
		
		try {
			
			logger.info("Starting activateTerminal process ..");
			logger.info("Checking input params ..");
			
			//Check if any parameter is null
			
			TcsParamsValidationHelper.checkRequiredParams(env, "tcs.required.params.terminal.username", jsonRequest, logger);
			
			jsonRequest.put("username",tcsApiAuthService.extractUserName(headers));
			jsonRequest.put("password",tcsApiAuthService.extractUserPassword(headers));
			
			
			return new HashMap<>(tcsApiAccountManagementService.tcsResetUser(jsonRequest, headers));
			
			
		}
		catch(NullOrEmptyInputParameters e) {
			
			logger.warn("Input param is null  ..");
			logger.warn("Prepare NULL_OR_EMPTY_INPUT response  ..");
			
			response.put("Result", String.format("%d", -200));
			response.put("Message", "NULL_OR_EMPTY_INPUT");
			
			return response;
			
		}
		catch(Exception e) {
			
			logger.error("Exception is caught ..",e);
			logger.error(e.getMessage());
			
			response.put("Result", String.format("%d", -100));
			response.put("Message", "EXCEPTION");
			response.put("Error", e.getMessage());
			
			return response;
		}
	}
	
	@Deprecated
	@RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE, 
    		method = RequestMethod.GET,
    		value = "/vx/231548674531564/account/staff/list")
	@ResponseBody
	public Map<String,Object> getAccountStaff(@RequestHeader Map<String, String> headers){
		
		/*
		 * @Deprecated
		 * To list account Staff
		 * 
		 */
		
		Map<String,Object> response = new HashMap<>();
		
		try {
			
			logger.info("Starting getAccountStaff process ..");
			
			logger.info("Extract Account ID ..");
			
			String account_id=tcsApiAuthService.extractUserAccountId(headers);
			//String username=tcsApiAuthService.extractUserName(headers);
			
			logger.info(String.format("Account ID found: %s",account_id));
			
			List<Map<String,Object>>  result = terminalService.getAccountTerminals(account_id, "GPT");
			
			ObjectMapper objectMapper = new ObjectMapper();
			
			String json = objectMapper.writeValueAsString(result);
			
			logger.info(String.format("terminalService.getAccountTerminals Result:%s", json));
			
			response.put("Result", 0);
			response.put("Message", "OK");
			response.put("List", result);
			
			return response;
			
		}
		catch(Exception e) {
			
			logger.error("Exception is caught ..",e);
			logger.error(e.getMessage());
			
			response.put("Result", String.format("%d", -100));
			response.put("Message", "EXCEPTION");
			response.put("Error", e.getMessage());
			
			return response;
		}
	}
	
	@RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE, 
    		method = RequestMethod.POST,
    		value = "/v1/account/staff/suspend")
	@ResponseBody
	public Map<String,Object> suspendStaff(@RequestBody Map<String,Object> jsonRequest,
			@RequestHeader Map<String, String> headers){
		
		/*
		 * To suspend a Staff
		 * 
		 */
		
		Map<String,Object> response = new HashMap<>();
		
		try {
			
			logger.info("Starting suspendStaff process ..");
			logger.info("Checking input params ..");
			
			//Check if any parameter is null
			
			
			TcsParamsValidationHelper.checkRequiredParams(env, "tcs.required.params.staff.general", jsonRequest, logger);
			
			jsonRequest.put("username",tcsApiAuthService.extractUserName(headers));
			jsonRequest.put("password",tcsApiAuthService.extractUserPassword(headers));
			jsonRequest.put("new_status", "D");
			
			return new HashMap<>(tcsApiAccountManagementService.tcsChangeUserStatus(jsonRequest, headers));
			
			
		}
		catch(NullOrEmptyInputParameters e) {
			
			logger.warn("Input param is null  ..");
			logger.warn("Prepare NULL_OR_EMPTY_INPUT response  ..");
			
			response.put("Result", String.format("%d", -200));
			response.put("Message", "NULL_OR_EMPTY_INPUT");
			
			return response;
			
		}
		catch(Exception e) {
			
			logger.error("Exception is caught ..",e);
			logger.error(e.getMessage());
			
			response.put("Result", String.format("%d", -100));
			response.put("Message", "EXCEPTION");
			response.put("Error", e.getMessage());
			
			return response;
		}
	}
	
	@RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE, 
    		method = RequestMethod.POST,
    		value = "/v1/account/staff/activate")
	@ResponseBody
	public Map<String,Object> activateStaff(@RequestBody Map<String,Object> jsonRequest,
			@RequestHeader Map<String, String> headers){
		
		/*
		 * To activate a Staff
		 * 
		 */
		
		Map<String,Object> response = new HashMap<>();
		
		try {
			
			logger.info("Starting activateStaff process ..");
			logger.info("Checking input params ..");
			
			//Check if any parameter is null
			
			TcsParamsValidationHelper.checkRequiredParams(env, "tcs.required.params.staff.general", jsonRequest, logger);
			
			jsonRequest.put("username",tcsApiAuthService.extractUserName(headers));
			jsonRequest.put("password",tcsApiAuthService.extractUserPassword(headers));
			jsonRequest.put("new_status", "A");
			
			return new HashMap<>(tcsApiAccountManagementService.tcsChangeUserStatus(jsonRequest, headers));
			
			
		}
		catch(NullOrEmptyInputParameters e) {
			
			logger.warn("Input param is null  ..");
			logger.warn("Prepare NULL_OR_EMPTY_INPUT response  ..");
			
			response.put("Result", String.format("%d", -200));
			response.put("Message", "NULL_OR_EMPTY_INPUT");
			
			return response;
			
		}
		catch(Exception e) {
			
			logger.error("Exception is caught ..",e);
			logger.error(e.getMessage());
			
			response.put("Result", String.format("%d", -100));
			response.put("Message", "EXCEPTION");
			response.put("Error", e.getMessage());
			
			return response;
		}
	}

	@RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE, 
    		method = RequestMethod.POST,
    		value = "/v1/account/staff/add")
	@ResponseBody
	public Map<String,Object> addStaff(@RequestBody Map<String,Object> jsonRequest,
			@RequestHeader Map<String, String> headers){
		
		/*
		 * To create a new Staff
		 * 
		 */
		
		Map<String,Object> response = new HashMap<>();
		
		try {
			
			logger.info("Starting addStaff process ..");
			logger.info("Checking input params ..");
			
			//Check if any parameter is null
			
			logger.info(String.format("Input params, user_mobile=%s, staff_code=%s, interface_id=%s, fname=%s, sname=%s, email=%s", 
					jsonRequest.get("user_mobile"),
					jsonRequest.get("staff_code"),
					jsonRequest.get("interface_id"),
					jsonRequest.get("fname"),
					jsonRequest.get("sname"),
					jsonRequest.get("email")));
			
			TcsParamsValidationHelper.checkRequiredParams(env, "tcs.required.params.staff.add", jsonRequest, logger);
			
			jsonRequest.put("username",tcsApiAuthService.extractUserName(headers));
			jsonRequest.put("password",tcsApiAuthService.extractUserPassword(headers));
			
			NumberFormat nf = NumberFormat.getInstance(Locale.ENGLISH);
			
			for (int i = 0; i < jsonRequest.get("staff_code").toString().length(); i++) {
			    char ch = jsonRequest.get("staff_code").toString().charAt(i);
			    
			    System.out.println(ch);
			    System.out.println(nf.format(ch));
			}
			
			return new HashMap<>(tcsApiAccountManagementService.tcsAddStaff(jsonRequest, headers));
		}
		catch(NullOrEmptyInputParameters e) {
			
			logger.warn("Input param is null  ..");
			logger.warn("Prepare NULL_OR_EMPTY_INPUT response  ..");
			
			response.put("Result", String.format("%d", -200));
			response.put("Message", "NULL_OR_EMPTY_INPUT");
			
			return response;
			
		}
		catch(Exception e) {
			
			logger.error("Exception is caught ..",e);
			logger.error(e.getMessage());
			
			response.put("Result", String.format("%d", -100));
			response.put("Message", "EXCEPTION");
			response.put("Error", e.getMessage());
			
			return response;
		}
		
	}
	
	@RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE, 
    		method = RequestMethod.POST,
    		value = "/v1/rpc/account/staff/modify")
	@ResponseBody
	public Map<String,Object> modifyStaff(@RequestBody Map<String,Object> jsonRequest,
			@RequestHeader Map<String, String> headers){
		
		/*
		 * To edit Staff
		 * 
		 */
		
		Map<String,Object> response = new HashMap<>();
		
		try {
			
			logger.info("Starting modifyStaff process ..");
			logger.info("Checking input params ..");
			
			//Check if any parameter is null
			
			logger.info(String.format("Input params, user_mobile=%s, fname=%s, sname=%s", 
					jsonRequest.get("user_mobile"),
					jsonRequest.get("fname"),
					jsonRequest.get("sname")));
			
			TcsParamsValidationHelper.checkRequiredParams(env, "tcs.required.params.staff.modify", jsonRequest, logger);
			
			//Added 10052022
			//To avoid API error because of space in email
			
			if(jsonRequest.get("email")!=null)
				jsonRequest.put("email", jsonRequest.get("email").toString().trim());
			///////////////////////////////////////////////////////////////////////////////////////
			
			jsonRequest.put("username",tcsApiAuthService.extractUserName(headers));
			jsonRequest.put("password",tcsApiAuthService.extractUserPassword(headers));
			
			//Added 10052022
			//To solve the 967 in notification mobile numbers
			
			jsonRequest.put("user_mobile", tcsApiAccountManagementService
					.normalizeNotificationNumber(jsonRequest.get("user_mobile")));
			
			////////////////////////////////////////////////////////////////////////////////////////
			
			jsonRequest.put("method", "AccountStaffTerminals.modifyMyStaffUser");
			
			Map<String,Object> r = telepinRpcAccountManagementService.rpcCall(jsonRequest, headers);
			
			ObjectMapper objectMapper = new ObjectMapper();
			
			String json = objectMapper.writeValueAsString(r);
			
			logger.info(String.format("telepinRpcAccountManagementService.rpcCall Result:%s", json));
			
			return r;
		}
		catch(NullOrEmptyInputParameters e) {
			
			logger.warn("Input param is null  ..");
			logger.warn("Prepare NULL_OR_EMPTY_INPUT response  ..");
			
			response.put("Result", String.format("%d", -200));
			response.put("Message", "NULL_OR_EMPTY_INPUT");
			
			return response;
			
		}
		catch(Exception e) {
			
			logger.error("Exception is caught ..",e);
			logger.error(e.getMessage());
			
			response.put("Result", String.format("%d", -100));
			response.put("Message", "EXCEPTION");
			response.put("Error", e.getMessage());
			
			return response;
		}
		
	}
	
	@RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE, 
    		method = RequestMethod.POST,
    		value = "/v1/account/staff/delete")
	@ResponseBody
	public Map<String,Object> deleteStaff(@RequestBody Map<String,Object> jsonRequest,
			@RequestHeader Map<String, String> headers){
		
		/*
		 * To delete a Staff
		 * 
		 */
		
		Map<String,Object> response = new HashMap<>();
		
		try {
			
			logger.info("Starting deleteStaff process ..");
			logger.info("Checking input params ..");
			
			logger.info(String.format("Input params, staff_code=%s", 
					jsonRequest.get("staff_code")));
			
			//Check if any parameter is null
			
			TcsParamsValidationHelper.checkRequiredParams(env, "tcs.required.params.staff.delete", jsonRequest, logger);
			
			jsonRequest.put("username",tcsApiAuthService.extractUserName(headers));
			jsonRequest.put("password",tcsApiAuthService.extractUserPassword(headers));
			
			return new HashMap<>(tcsApiAccountManagementService.tcsDeleteStaff(jsonRequest, headers));
		}
		catch(NullOrEmptyInputParameters e) {
			
			logger.warn("Input param is null  ..");
			logger.warn("Prepare NULL_OR_EMPTY_INPUT response  ..");
			
			response.put("Result", String.format("%d", -200));
			response.put("Message", "NULL_OR_EMPTY_INPUT");
			
			return response;
			
		}
		catch(Exception e) {
			
			logger.error("Exception is caught ..",e);
			logger.error(e.getMessage());
			
			response.put("Result", String.format("%d", -100));
			response.put("Message", "EXCEPTION");
			response.put("Error", e.getMessage());
			
			return response;
		}
		
	}
	
	@RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE, 
    		method = RequestMethod.POST,
    		value = "/v1/account/staff/reset")
	@ResponseBody
	public Map<String,Object> resetStaff(@RequestBody Map<String,Object> jsonRequest,
			@RequestHeader Map<String, String> headers){
		
		/*
		 * To reset a Staff
		 * 
		 */
		
		Map<String,Object> response = new HashMap<>();
		
		try {
			
			logger.info("Starting resetStaff process ..");
			logger.info("Checking input params ..");
			
			logger.info(String.format("Input params, staff_code=%s", 
					jsonRequest.get("staff_code")));
			
			//Check if any parameter is null
			
			TcsParamsValidationHelper.checkRequiredParams(env, "tcs.required.params.staff.delete", jsonRequest, logger);
			
			jsonRequest.put("username",tcsApiAuthService.extractUserName(headers));
			jsonRequest.put("password",tcsApiAuthService.extractUserPassword(headers));
			
			return new HashMap<>(tcsApiAccountManagementService.tcsResetStaff(jsonRequest, headers));
		}
		catch(NullOrEmptyInputParameters e) {
			
			logger.warn("Input param is null  ..");
			logger.warn("Prepare NULL_OR_EMPTY_INPUT response  ..");
			
			response.put("Result", String.format("%d", -200));
			response.put("Message", "NULL_OR_EMPTY_INPUT");
			
			return response;
			
		}
		catch(Exception e) {
			
			logger.error("Exception is caught ..",e);
			logger.error(e.getMessage());
			
			response.put("Result", String.format("%d", -100));
			response.put("Message", "EXCEPTION");
			response.put("Error", e.getMessage());
			
			return response;
		}
		
	}
	
	@RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE, 
    		method = RequestMethod.GET,
    		value = "/v1/terminal/mobile")
	@ResponseBody
	public Map<String,Object> getUserMobileNumber(@RequestHeader Map<String, String> headers){
		
		/*
		 * To get Web user mobile number
		 * 
		 */
		
		Map<String,Object> response = new HashMap<>();
		
		try {
			
			logger.info("Starting getUserMobileNumber process ..");
			
			logger.info("Extract Mobile Number ..");
			
			String mobile=tcsApiAuthService.extractUserMobile(headers);
			
			logger.info(String.format("Mobile found: %s",mobile));
			
			response.put("Result", 0);
			response.put("Message", "OK");
			response.put("Mobile", mobile);
			
			return response;
			
		}
		catch(Exception e) {
			
			logger.error("Exception is caught ..",e);
			logger.error(e.getMessage());
			
			response.put("Result", String.format("%d", -100));
			response.put("Message", "EXCEPTION");
			response.put("Error", e.getMessage());
			
			return response;
		}
	}
	
	@RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE, 
    		method = RequestMethod.GET,
    		value = "/v1/terminal/email")
	@ResponseBody
	public Map<String,Object> getUserEmail(@RequestHeader Map<String, String> headers){
		
		/*
		 * To get Web user email
		 * 
		 */
		
		Map<String,Object> response = new HashMap<>();
		
		try {
			
			logger.info("Starting getUserEmail process ..");
			
			logger.info("Extract Email ..");
			
			String email=tcsApiAuthService.extractUserEmail(headers);
			
			logger.info(String.format("Email found: %s",email));
			
			response.put("Result", 0);
			response.put("Message", "OK");
			response.put("Email", email);
			
			return response;
			
		}
		catch(Exception e) {
			
			logger.error("Exception is caught ..",e);
			logger.error(e.getMessage());
			
			response.put("Result", String.format("%d", -100));
			response.put("Message", "EXCEPTION");
			response.put("Error", e.getMessage());
			
			return response;
		}
	}
	
	@RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE, 
    		method = RequestMethod.GET,
    		value = "/v1/terminal/user_key")
	@ResponseBody
	public Map<String,Object> getUserKey(@RequestHeader Map<String, String> headers){
		
		/*
		 * To get Web user key
		 * 
		 */
		
		Map<String,Object> response = new HashMap<>();
		
		try {
			
			logger.info("Starting getUserKey process ..");
			
			logger.info("Extract User Key ..");
			
			String key=tcsApiAuthService.extractUserId(headers);
			
			logger.info(String.format("User Key found: %s",key));
			
			response.put("Result", 0);
			response.put("Message", "OK");
			response.put("Key", key);
			
			return response;
			
		}
		catch(Exception e) {
			
			logger.error("Exception is caught ..",e);
			logger.error(e.getMessage());
			
			response.put("Result", String.format("%d", -100));
			response.put("Message", "EXCEPTION");
			response.put("Error", e.getMessage());
			
			return response;
		}
	}
}
