package com.onecashye.web.security.controller;

import java.util.HashMap;
import java.util.List;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.onecashye.web.security.exception.NullOrEmptyInputParameters;
import com.onecashye.web.security.helper.TcsParamsValidationHelper;
import com.onecashye.web.security.service.TcsApiAccountService;
import com.onecashye.web.security.service.TcsApiAuthService;
import com.onecashye.web.security.service.TcsApiOtpService;
import com.onecashye.web.security.telepin.service.AccountService;



@RestController
@PropertySource({ "classpath:tcs.properties" })
public class AccountController {

	Logger logger = LogManager.getLogger(AccountController.class);
	
	private final TcsApiAccountService tcsApiAccountService;
	private final TcsApiAuthService tcsApiAuthService;
	private final TcsApiOtpService tcsApiOtpService;
	private final AccountService telepinAccountService;
	
	private final Environment env;
	
	@Autowired
	public AccountController(TcsApiAccountService tcsApiAccountService, 
			TcsApiAuthService tcsApiAuthService, 
			TcsApiOtpService tcsApiOtpService, 
			AccountService telepinAccountService, Environment env) {
		super();
		this.tcsApiAccountService = tcsApiAccountService;
		this.tcsApiAuthService = tcsApiAuthService;
		this.tcsApiOtpService = tcsApiOtpService;
		this.telepinAccountService = telepinAccountService;
		this.env = env;
		
	}
	
	@RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE, 
    		method = RequestMethod.POST,
    		value = "/v1/account/check")
	@ResponseBody
	public Map<String,Object> checkExistance(@RequestBody Map<String,Object> jsonRequest,
			@RequestHeader Map<String, String> headers){
		
		
		
		Map<String,Object> response = new HashMap<>();
		
		try {
			
			
			logger.info("Starting checkExistance process ..");
			logger.info("Checking input params ..");

			TcsParamsValidationHelper.checkRequiredParams(env, "tcs.required.params.accountexists", jsonRequest, logger);
			
			logger.info("Calling TCS API ..");
			
			Map<String,String> result = tcsApiAccountService.tcsCheckAccount(jsonRequest,headers);
			
			logger.info("Return TCS response ..");
			
			return new HashMap<>(result);
			
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
    		value = "/v1/account/info")
	@ResponseBody
	public Map<String,Object> getAccountInfo(@RequestBody Map<String,Object> jsonRequest,
			@RequestHeader Map<String, String> headers){
		
		
		Map<String,Object> response = new HashMap<>();
		
		try {

			logger.info("Starting getAccountInfo process ..");
			logger.info("Checking input params ..");

			TcsParamsValidationHelper.checkRequiredParams(env, "tcs.required.params.getaccountinfoiso", jsonRequest, logger);
			
			
			logger.info("Calling TCS API ..");
			
			Map<String,String> result = tcsApiAccountService.tcsGetAccountInfoIso(jsonRequest,headers);
			
			ObjectMapper objectMapper = new ObjectMapper();
			
			String json = objectMapper.writeValueAsString(result);
			
			logger.info(String.format("Out Result:%s", json));
			
			logger.info("Return TCS response ..");
			
			return new HashMap<>(result);
			
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
    		value = "/v1/account/type")
	@ResponseBody
	public Map<String,Object> getAccountType(@RequestBody Map<String,Object> jsonRequest,
			@RequestHeader Map<String, String> headers){
		
		
		Map<String,Object> response = new HashMap<>();
		
		try {
			
			logger.info("Starting getAccountType process ..");
			logger.info("Checking input params ..");

			TcsParamsValidationHelper.checkRequiredParams(env, "tcs.required.params.viewaccounttype", jsonRequest, logger);
			
			logger.info("Calling TCS API ..");
			
			Map<String,String> result = tcsApiAccountService.tcsGetAccountType(jsonRequest,headers);
			
			ObjectMapper objectMapper = new ObjectMapper();
			
			String json = objectMapper.writeValueAsString(result);
			
			logger.info(String.format("Out Result:%s", json));
			
			logger.info("Return TCS response ..");
			
			return new HashMap<>(result);
			
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
    		value = "/v1/account/children")
	@ResponseBody
	public Map<String,Object> getAccountChildren(@RequestHeader Map<String, String> headers){
		
		
		Map<String,Object> response = new HashMap<>();
		Map<String,Object> jsonRequest = new HashMap<>();
		
		try {
			
			logger.info("Starting getAccountChildren process ..");
			
			logger.info("Calling Stage DB Query for Account Children ..");
			
			String account_id= tcsApiAuthService.extractUserAccountId(headers);
			
			logger.info(String.format("Account ID extracted from headers %s", account_id));
			
			List<Map<String,Object>> result = telepinAccountService.getAccountChildren(account_id);
			
			jsonRequest.put("username",tcsApiAuthService.extractUserName(headers));
			jsonRequest.put("password",tcsApiAuthService.extractUserPassword(headers));
			
			//Loop to get Balance Proxy for each child
			logger.info(String.format("Starting Loop to get Balance Proxy for each child, total found:%d", result.size()));
			
			for(Map<String,Object> m : result) {
				
				
				String msisdn = m.get("account_mobile").toString();
				jsonRequest.put("msisdn",msisdn);
				
				logger.info(String.format("Account Child %s..", msisdn));
				logger.info("Calling TCS Balance Proxy Owner API ..");
				
				m.put("balances", tcsApiAccountService.tcsBalanceProxyOwner(jsonRequest, headers));
			}
			
			logger.info("Prepare response ..");
			
			response.put("Result", 0);
			response.put("Message", "OK");
			response.put("List", result);
			
			ObjectMapper objectMapper = new ObjectMapper();
			
			String json = objectMapper.writeValueAsString(response);
			
			logger.info(String.format("Out Result:%s", json));
			
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
    		value = "/v1/account/nominated")
	@ResponseBody
	public Map<String,Object> getAccountNominatedBanks(@RequestHeader Map<String, String> headers){
		
		
		Map<String,Object> response = new HashMap<>();
		
		try {
			
			logger.info("Starting getAccountNominatedBanks process ..");
			
			logger.info("Calling Stage DB Query for Account Nominated Banks ..");
			
			String account_id=tcsApiAuthService.extractUserAccountId(headers);
			
			logger.info(String.format("Account ID extracted from headers %s", account_id));
			
			List<Map<String,Object>> result = telepinAccountService.getAccountNominatedDetails(account_id);
			
			
			logger.info("Prepare response ..");
			
			response.put("Result", 0);
			response.put("Message", "OK");
			response.put("List", result);
			
			ObjectMapper objectMapper = new ObjectMapper();
			
			String json = objectMapper.writeValueAsString(response);
			
			logger.info(String.format("Out Result:%s", json));
			
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
    		value = "/v1/account/subscriber/create")
	@ResponseBody
	public Map<String,Object> createSubscriber(@RequestBody Map<String,Object> jsonRequest,
			@RequestHeader Map<String, String> headers){
		
		/*
		 * This end point to create a subscriber account with verification inclusive
		 * 
		 * - Create Account on TCS
		 * - Call Verification API
		 * 
		 */
		Map<String,Object> response = new HashMap<>();
		
		try {
			
			logger.info("Starting createSubscriber process ..");
			logger.info("Checking input params ..");
			
			TcsParamsValidationHelper.checkRequiredParams(env,"tcs.required.params.createaccountnotac", jsonRequest, logger);
			
			jsonRequest.put("username",tcsApiAuthService.extractUserName(headers));
			jsonRequest.put("password",tcsApiAuthService.extractUserPassword(headers));
			jsonRequest.put("country","254");
			
			logger.info("Calling TCS API CreateAccount..");
			
			Map<String,String> result = tcsApiAccountService.tcsCreateAccountNoTac(jsonRequest,headers);
			
			ObjectMapper objectMapper = new ObjectMapper();
			
			String json = objectMapper.writeValueAsString(result);
			
			logger.info(String.format("tcsApiAccountService.tcsCreateAccountNoTac Result:%s", json));
			
			if(result.get("Result").equalsIgnoreCase("0")) {
				
				//Verify
				
				logger.info("Calling TCS API VerifyAccount ..");
				
				Map<String,String> v_result = tcsApiAccountService.tcsVerifyAccountLevel(jsonRequest,headers);
				
				v_result.put("Account_ID", result.get("Account_ID"));
				
				objectMapper = new ObjectMapper();
				
				json = objectMapper.writeValueAsString(v_result);
				
				logger.info(String.format("tcsApiAccountService.tcsVerifyAccountLevel Result:%s", json));
				
				logger.info("Return TCS VerifyAccount response ..");
				
				return new HashMap<>(v_result);
			}
			
			logger.info("Return TCS CreateAccount response ..");
			
			return new HashMap<>(result);
			
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
    		value = "/v1/account/subscriber/verify")
	@ResponseBody
	public Map<String,Object> verifySubscriber(@RequestBody Map<String,Object> jsonRequest,
			@RequestHeader Map<String, String> headers){
		
		/*
		 * This end point to verify a subscriber
		 * 
		 * There are two cases to check
		 * 
		 * 1- Account MSISDN could be Registered=0 & Verified=0 (Signup Status)
		 * 
		 * 	  In case 1, customer data is already available, GetAccountInfo to make sure and to check param39
		 * 	  If param39 != 0, update account info to make param39 = 0
		 * 	  Then, Register the account to level 1 & Verify to Level 1
		 * 	 
		 * 2- Account MSISDN could be Registered=1 & Verified=0 (Regular Verification)
		 * 
		 *    In case 2, also GetAccountInfo to check param39
		 * 	  If param39 != 0, update account info to make param39 = 0
		 * 	  Then, only Verify to Level 1
		 * 
		 */
		Map<String,Object> response = new HashMap<>();
		
		try {
			
			logger.info("Starting verifySubscriber process ..");
			logger.info("Checking input params ..");
			
			Map<String,Object> jsonRequestProxy = new HashMap<>(jsonRequest);
			
			jsonRequestProxy.put("account", jsonRequest.get("msisdn"));
			
			TcsParamsValidationHelper.checkRequiredParams(env, "tcs.required.params.verify.subscriber", jsonRequest, logger);
			
			logger.info("Extracting username & password ..");
			
			jsonRequest.put("username",tcsApiAuthService.extractUserName(headers));
			jsonRequest.put("password",tcsApiAuthService.extractUserPassword(headers));
			
			//Update on 02082022 to include Signup Subscribers in Verification and support param39
			//Add check account exist call to get Reg & Ver levels
			//Add GetAccountInfoISO API call to get param39 
			//Add SETDOCUMENTSONFILE API call to change param39 to be 0 
			
			ObjectMapper objectMapper = new ObjectMapper();
			
			logger.info("Calling TCS API AccountExist ..");
			
			Map<String,String> checkAccount = tcsApiAccountService.tcsCheckAccount(jsonRequestProxy, headers);
			
			logger.info(String.format("tcsCheckAccount Out Result:%s", objectMapper.writeValueAsString(checkAccount)));
			
			//Get Account info to get Account ID and Check param39
			
			logger.info("Calling TCS API GetAccountInfoIso to get Account ID and Check param39..");
				
			Map<String,String> accountInfo = tcsApiAccountService.tcsGetAccountInfoIso(jsonRequestProxy, headers);
			
			logger.info(String.format("tcsGetAccountInfoIso Out Result:%s", objectMapper.writeValueAsString(accountInfo)));
			
			jsonRequest.put("account", accountInfo.get("Customer_ID"));
			
			if(checkAccount.get("Registered").contentEquals("NO")) {
				
				//Register account to level 1
				//
				logger.warn(String.format("Account %s is not registered", jsonRequest.get("msisdn")));
				logger.info(String.format("Try to register account ID %s", accountInfo.get("Customer_ID")));
				
				Map<String,String> regAccount = tcsApiAccountService.tcsRegisterSubscriberLevel1(jsonRequest, headers);
				
				logger.info(String.format("tcsRegisterSubscriberLevel1 Out Result:%s", objectMapper.writeValueAsString(regAccount)));
			}
			
			logger.info("Calling TCS API VerifyAccount ..");
			
			if(checkAccount.get("Verified").contentEquals("NO")) {
				
				//Verify account to level 1
				//
				logger.warn(String.format("Account %s is not verified", jsonRequest.get("msisdn")));
				logger.info(String.format("Try to verify account ID %s", accountInfo.get("Customer_ID")));
				
				Map<String,String> verAccount = tcsApiAccountService.tcsVerifyAccountLevel(jsonRequest, headers);
				
				logger.info(String.format("tcsVerifyAccountLevel Out Result:%s", objectMapper.writeValueAsString(verAccount)));
			}
			
			if(!accountInfo.get("Docs_On_File").contentEquals("0")) {
				
				logger.warn(String.format("Docs on file = %s", accountInfo.get("Docs_On_File")));
				logger.info(String.format("Upadte Docs on file to %s","0"));
				
				//Update account info param 39
				
				jsonRequest.put("docs_value", "0");
				
				Map<String,String> updateInfo = tcsApiAccountService.tcsUpdateSubscriberParam39(jsonRequest, headers);
				
				logger.info(String.format("tcsUpdateSubscriberParam39 Out Result:%s", objectMapper.writeValueAsString(updateInfo)));
			}
			
			logger.info("Return TCS response ..");
			
			//return new HashMap<>(result);
			
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
		
		Map<String,Object> result = new HashMap<>();
		
		result.put("Result", "0");
		result.put("Message", "VERIFICATION_COMPLETED");
		
		return result;
	}
	
	@RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE, 
    		method = RequestMethod.POST,
    		value = "/v1/account/subscriber/registerlevel1")
	@ResponseBody
	public Map<String,Object> registerSubscriberLevel1(@RequestBody Map<String,Object> jsonRequest,
			@RequestHeader Map<String, String> headers){
		
		/*
		 * 01062022
		 * 
		 * This end point to register a subscriber to Level 1 (LowKYC)
		 * It should be used in case where a subscriber is in signup level
		 * 
		 */
		Map<String,Object> response = new HashMap<>();
		
		try {
			
			logger.info("Starting registerSubscriberLevel1 process ..");
			logger.info("Checking input params ..");
			
			TcsParamsValidationHelper.checkRequiredParams(env, "tcs.required.params.register.subscriber", jsonRequest, logger);
			
			logger.info("Extracting username & password ..");
			
			jsonRequest.put("username",tcsApiAuthService.extractUserName(headers));
			jsonRequest.put("password",tcsApiAuthService.extractUserPassword(headers));
			
			logger.info("Calling TCS API REGISTERACCOUNT ..");
			
			Map<String,String> result = tcsApiAccountService.tcsRegisterSubscriberLevel1(jsonRequest, headers);
			
			ObjectMapper objectMapper = new ObjectMapper();
			
			String json = objectMapper.writeValueAsString(result);
			
			logger.info(String.format("Out Result:%s", json));
			
			logger.info("Return TCS response ..");
			
			return new HashMap<>(result);
			
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
    		value = "/v1/account/requesttac")
	@ResponseBody
	public Map<String,Object> requestTac(@RequestBody Map<String,Object> jsonRequest,
			@RequestHeader Map<String, String> headers){
		
		Map<String,Object> response = new HashMap<>();
		
		try {
			
			logger.info("Starting Account:requestTac process ..");
			logger.info("Checking input params ..");

			TcsParamsValidationHelper.checkRequiredParams(env, "tcs.required.params.msisdn", jsonRequest, logger);
			
			logger.info("Calling TCS API  ..");
			
			return new HashMap<>(tcsApiOtpService.tcsRequestTac(jsonRequest));
			
			
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
    		value = "/v1/account/balance")
	@ResponseBody
	public Map<String,Object> getAccountBalance(@RequestHeader Map<String, String> headers){
		
		/*
		 * 
		 */
		Map<String,Object> response = new HashMap<>();
		Map<String,Object> jsonRequest = new HashMap<>();
		
		try {
			
			logger.info("Starting getAccountBalance process ..");
					
			logger.info("Extracting username and password ..");
			
			jsonRequest.put("username",tcsApiAuthService.extractUserName(headers));
			jsonRequest.put("password",tcsApiAuthService.extractUserPassword(headers));
			
			logger.info("Calling TCS tcsBalanceMWallet function ..");
			
			Map<String,String> result = tcsApiAccountService.tcsBalanceMWallet(jsonRequest,headers);
			
			ObjectMapper objectMapper = new ObjectMapper();
			
			String json = objectMapper.writeValueAsString(result);
			
			logger.info(String.format("Out Result:%s", json));
			
			logger.info("Return TCS response ..");
			
			return new HashMap<>(result);
			
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
    		value = "/v1/account/changelang")
	@ResponseBody
	public Map<String,Object> changeLanguage(@RequestParam String lang,
			@RequestHeader Map<String, String> headers){
		
		
		
		Map<String,Object> response = new HashMap<>();
		Map<String,Object> jsonRequest = new HashMap<>();
		
		try {
			
			logger.info("Starting changeLanguage process ..");
			logger.info("Checking input params ..");

			if(lang == null || lang.isEmpty()) {
				
				logger.error(String.format("Validation error: Mandatory params, lang=%s", 
						lang));
				
				throw new NullOrEmptyInputParameters("NULL_OR_EMPTY_INPUT");
					
			}
			
			logger.info("Extracting username and password ..");
			
			jsonRequest.put("username",tcsApiAuthService.extractUserName(headers));
			jsonRequest.put("password",tcsApiAuthService.extractUserPassword(headers));
			
			jsonRequest.put("lang",lang);
			
			logger.info("Calling TCS API ..");
			
			Map<String,String> result = tcsApiAccountService.tcsChangeLanguage(jsonRequest,headers);
			
			ObjectMapper objectMapper = new ObjectMapper();
			
			String json = objectMapper.writeValueAsString(result);
			
			logger.info(String.format("Out Result:%s", json));
			
			logger.info("Return TCS response ..");
			
			return new HashMap<>(result);
			
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

}
