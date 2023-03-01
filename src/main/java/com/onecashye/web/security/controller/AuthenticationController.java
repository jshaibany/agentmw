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
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.onecashye.web.security.exception.InvalidTac;
import com.onecashye.web.security.exception.NullOrEmptyInputParameters;
import com.onecashye.web.security.helper.TcsParamsValidationHelper;
import com.onecashye.web.security.service.TcsApiAccountService;
import com.onecashye.web.security.service.TcsApiAuthService;
import com.onecashye.web.security.service.TcsApiOtpService;


@RestController
@PropertySource({ "classpath:tcs.properties" })
public class AuthenticationController {
	
	Logger logger = LogManager.getLogger(AuthenticationController.class);
	
	private final TcsApiAuthService tcsApiAuthService;
	private final TcsApiOtpService tcsApiOtpService;
	private final TcsApiAccountService tcsApiAccountService;
	
	private final Environment env;
	
	
	@Autowired
	public AuthenticationController(TcsApiAuthService tcsApiAuthService, 
			TcsApiOtpService tcsApiOtpService, 
			TcsApiAccountService tcsApiAccountService, 
			Environment env) {
		
		this.tcsApiAuthService = tcsApiAuthService;
		this.tcsApiOtpService = tcsApiOtpService;
		this.tcsApiAccountService = tcsApiAccountService;
		this.env = env;
	
	}
	
	@Deprecated
	@RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE, 
    		method = RequestMethod.POST,
    		value = "/v1/rpc/authenticate")
	@ResponseBody
	public Map<String,Object> rpcAuthenticate(@RequestBody Map<String,Object> jsonRequest,
			@RequestHeader Map<String, String> headers){
		 
		
		Map<String,Object> response = new HashMap<>();
		
		try {
			
			
			logger.info("Starting RPC authentication process ..");
			logger.info("Checking input params ..");
			
			if(jsonRequest.get("username") == null || jsonRequest.get("password") == null) {
				
				throw new NullOrEmptyInputParameters("NULL_OR_EMPTY_INPUT");
			}
			
			if(jsonRequest.get("username").toString().isEmpty() || jsonRequest.get("password").toString().isEmpty()) {
				
				throw new NullOrEmptyInputParameters("NULL_OR_EMPTY_INPUT");
			}
			
			
			logger.info("Calling TCS auth service ..");
			
			Map<String,String> r = tcsApiAuthService.jsonRpcAuthenticate(jsonRequest);
			
			ObjectMapper objectMapper = new ObjectMapper();
			
			String json = objectMapper.writeValueAsString(r);
			
			logger.info(String.format("Out Result:%s", json));
			
			logger.info("Return TCS auth response ..");
			
			return new HashMap<>(r);
			
			
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
    		value = "/v1/authenticate")
	@ResponseBody
	public Map<String,Object> authenticate(@RequestBody Map<String,Object> jsonRequest,
			@RequestHeader Map<String, String> headers){
		 
		
		Map<String,Object> response = new HashMap<>();
		
		try {
			
			
			logger.info("Starting authentication process ..");
			logger.info("Checking input params ..");
			
			TcsParamsValidationHelper.checkRequiredParams(env, "tcs.required.params.auth", jsonRequest, logger);
			
			//Fuckin Telepin accepts usernames with white spaces
			//jsonRequest.put("username", jsonRequest.get("username").toString().trim());
			
			logger.info(String.format("User trying to login %s ***", jsonRequest.get("username")));
			
			logger.info("Calling TCS auth service ..");
			
			Map<String,String> r = tcsApiAuthService.tcsAuthenticate(jsonRequest);
			
			r.put("_2FA",tcsApiAuthService.is2FAEnabled());
			
			ObjectMapper objectMapper = new ObjectMapper();
			
			String json = objectMapper.writeValueAsString(r);
			
			logger.info(String.format("Out Result:%s", json));
			
			logger.info("Return TCS auth response ..");
			
			return new HashMap<>(r);
			
			
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
    		value = "/v1/authenticate/changepassword")
	@ResponseBody
	public Map<String,Object> changePassword(@RequestBody Map<String,Object> jsonRequest,
			@RequestHeader Map<String, String> headers){
		
		Map<String,Object> response = new HashMap<>();
		
		try {
			
			logger.info("Starting change password process ..");
			logger.info("Checking input params ..");

			TcsParamsValidationHelper.checkRequiredParams(env, "tcs.required.params.change.password", jsonRequest, logger);
			
			logger.info("Calling TCS change password service ..");
			
			Map<String,String> r = tcsApiAuthService.tcsChangePassword(jsonRequest);
			r.put("2FA",tcsApiAuthService.is2FAEnabled());
			
			ObjectMapper objectMapper = new ObjectMapper();
			
			String json = objectMapper.writeValueAsString(r);
			
			logger.info(String.format("Out Result:%s", json));
			
			logger.info("Return TCS auth response ..");
			
			return new HashMap<>(r);
			
			
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
    		value = "/v1/authenticate/2fa/requesttac")
	@ResponseBody
	public Map<String,Object> requestTac(@RequestBody Map<String,Object> jsonRequest,
			@RequestHeader Map<String, String> headers){
		
		Map<String,Object> response = new HashMap<>();
		
		try {
			
			logger.info("Starting request tac 2FA process ..");
			logger.info("Checking input params ..");
			
			TcsParamsValidationHelper.checkRequiredParams(env, "tcs.required.params.msisdn", jsonRequest, logger);
			
			logger.info("Calling TCS request tac service ..");
			
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
    		value = "/v1/authenticate/requesttac")
	@ResponseBody
	public Map<String,Object> requestTacForCurrentUser(@RequestHeader Map<String, String> headers){
		
		Map<String,Object> response = new HashMap<>();
		
		try {
	
			logger.info("Starting request tac process ..");
			logger.info("Checking input params ..");
			
			logger.info("No input params required ..");
			
			Map<String, Object> jsonRequest = new HashMap<>();
			
			jsonRequest.put("msisdn",tcsApiAuthService.extractUserMobile(headers));
			
			logger.info("Calling TCS request tac service ..");
			
			return new HashMap<>(tcsApiOtpService.tcsRequestTac(jsonRequest));
			
			
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
    		value = "/v1/authenticate/validatetac")
	@ResponseBody
	public Map<String,Object> validateCurrentUserTac(@RequestBody Map<String,Object> jsonRequest,
			@RequestHeader Map<String, String> headers){
		
		/*
		 * For Login 2FA TAC validation
		 */
		Map<String,Object> response = new HashMap<>();
		Map<String,Object> tacRequest = jsonRequest;
		
		try {
			
			logger.info("Starting validate tac process ..");
			logger.info("Checking input params ..");

			TcsParamsValidationHelper.checkRequiredParams(env, "tcs.required.params.username", jsonRequest, logger);
			
			logger.info("Calling getUserNotificationNumber function ...");
			
			tacRequest.put("msisdn",tcsApiAccountService.getUserNotificationNumber(tacRequest.get("username").toString()));
			
			logger.info("Calling TCS validate tac function ...");
			
			Map<String,String> authValidateTac = new HashMap<>(tcsApiOtpService.tcsValidateTac(tacRequest));
			
			if(authValidateTac.get("Result").equalsIgnoreCase("0")){
				
				//TAC is valid
				logger.info("TAC is valid ...");
				
				return new HashMap<>(tcsApiAuthService.tcsAuthenticate(jsonRequest));
			}
			else {
				
				throw new InvalidTac("INVALID_TAC");
			}
		}
		catch(NullOrEmptyInputParameters e) {
			
			logger.warn("Input param is null  ..");
			logger.warn("Prepare NULL_OR_EMPTY_INPUT response  ..");
			
			response.put("Result", String.format("%d", -200));
			response.put("Message", "NULL_OR_EMPTY_INPUT");
			
			return response;
			
		}
		catch(InvalidTac e) {
			
			//Failed login with TAC
			
			logger.warn("TAC is invalid ...");
			logger.warn("Prepare INVALID_TAC response ...");
			
			response.put("result", -1);
			response.put("message", "INVALID_TAC");
			
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
    		value = "/v1/tac/validate")
	@ResponseBody
	public Map<String,Object> validateTac(@RequestBody Map<String,Object> jsonRequest,
			@RequestHeader Map<String, String> headers){
		
		Map<String,Object> response = new HashMap<>();
		Map<String,Object> tacRequest = jsonRequest;
		
		try {

			logger.info("Starting general validate tac process ..");
			logger.info("Checking input params ..");

			TcsParamsValidationHelper.checkRequiredParams(env, "tcs.required.params.msisdn", jsonRequest, logger);
		
			logger.info("Calling TCS validate tac function ..");
			
			Map<String,String> authValidateTac = new HashMap<>(tcsApiOtpService.tcsValidateTac(tacRequest));
			
			ObjectMapper objectMapper = new ObjectMapper();
			
			String json = objectMapper.writeValueAsString(authValidateTac);
			
			logger.info(String.format("tcsValidateTac Result:%s", json));
			
			if(authValidateTac.get("Result").equalsIgnoreCase("0")){
				
				//TAC is valid
				
				logger.info("TAC is valid ..");
				
				response.put("Result", 0);
				response.put("Message", "VALID_TAC");
				
				return response;
				
			}
			else {
				
				throw new InvalidTac("INVALID_TAC");
			}
		}
		catch(NullOrEmptyInputParameters e) {
			
			logger.warn("Input param is null  ..");
			logger.warn("Prepare NULL_OR_EMPTY_INPUT response  ..");
			
			response.put("Result", String.format("%d", -200));
			response.put("Message", "NULL_OR_EMPTY_INPUT");
			
			return response;
			
		}
		catch(InvalidTac e) {
			
			//Failed login with TAC
			
			logger.warn("TAC is invalid ...");
			logger.warn("Prepare INVALID_TAC response ...");
			
			response.put("result", -1);
			response.put("message", "INVALID_TAC");
			
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
    		value = "/v1/privileges/all")
	@ResponseBody
	public Map<String,Object> getCorePrivileges(@RequestHeader Map<String, String> headers){
		
		Map<String,Object> response = new HashMap<>();
		
		try {

			logger.info("Starting getCorePrivileges process ..");
			logger.info("Checking input params ..");
			
			Map<String,Object> request = new HashMap<>(); // for getting privileges
			
			
			try {
				
				request.put("username", tcsApiAuthService.extractUserName(headers));
				request.put("password", tcsApiAuthService.extractUserPassword(headers));
				
				List<String> privileges = tcsApiAuthService.tcsGetCorePrivileges(request);
				
				response.put("Result", 0);
				response.put("Message", "OK");
				response.put("Privileges", privileges);
				
				ObjectMapper objectMapper = new ObjectMapper();
				
				String json = objectMapper.writeValueAsString(response);
				
				logger.info(String.format("Out Result:%s", json));
				
				return response;
				
			} catch (Exception e) {
				
				logger.error("Exception is caught ..",e);
				logger.error(e.getMessage());
				
				response.put("Result", String.format("%d", -100));
				response.put("Message", "EXCEPTION");
				response.put("Error", e.getMessage());
				
				return response;
			}
			

			
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
    		value = "/v1/privileges/filter")
	@ResponseBody
	public Map<String,Object> getFilteredCorePrivileges(@RequestBody Map<String,Object> jsonRequest,
			@RequestHeader Map<String, String> headers){
		
		Map<String,Object> response = new HashMap<>();
		
		try {

			logger.info("Starting getCorePrivileges process ..");
			logger.info("Checking input params ..");
			
			
			try {
				
				jsonRequest.put("username", tcsApiAuthService.extractUserName(headers));
				jsonRequest.put("password", tcsApiAuthService.extractUserPassword(headers));
				
				List<String> privileges = tcsApiAuthService.tcsGetFilteredCorePrivileges(jsonRequest);
				
				response.put("Result", 0);
				response.put("Message", "OK");
				response.put("Privileges", privileges);
				
				ObjectMapper objectMapper = new ObjectMapper();
				
				String json = objectMapper.writeValueAsString(response);
				
				logger.info(String.format("Out Result:%s", json));
				
				return response;
				
			} catch (Exception e) {
				
				logger.error("Exception is caught ..",e);
				logger.error(e.getMessage());
				
				response.put("Result", String.format("%d", -100));
				response.put("Message", "EXCEPTION");
				response.put("Error", e.getMessage());
				
				return response;
			}
			

			
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
    		value = "/v1/authentication/user/number")
	@ResponseBody
	public Map<String,Object> getUserNotificationNumber(@RequestBody Map<String,Object> jsonRequest,
			@RequestHeader Map<String, String> headers){
		
		//This API gets data from STAGE DB
		
		Map<String,Object> response = new HashMap<>();
		
		logger.info("Starting getUserNotificationNumber process ..");
		logger.info("Checking input params ..");
		
		
		try {
			
			String n = tcsApiAccountService.getUserNotificationNumber(jsonRequest.get("username").toString());
			
			response.put("Result", 0);
			response.put("Message", "OK");
			response.put("Notification_NUmber", n);
			
			ObjectMapper objectMapper = new ObjectMapper();
			
			String json = objectMapper.writeValueAsString(response);
			
			logger.info(String.format("Out Result:%s", json));
			
			return response;
			
		} catch (Exception e) {
			
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
    		value = "/v1/heartbeat")
	@ResponseBody
	public Map<String,Object> heartbeat(@RequestBody Map<String,Object> jsonRequest,
			@RequestHeader Map<String, String> headers){
		 
		
		Map<String,Object> response = new HashMap<>();
		
		try {
			
			
			logger.info("Starting heartbeat process ..");
			logger.info("Checking input params ..");
			
			jsonRequest.put("username",tcsApiAuthService.extractUserName(headers));
			
			if(jsonRequest.get("username") == null || jsonRequest.get("password") == null) {
				
				throw new NullOrEmptyInputParameters("NULL_OR_EMPTY_INPUT");
			}
			
			if(jsonRequest.get("username").toString().isEmpty() || jsonRequest.get("password").toString().isEmpty()) {
				
				throw new NullOrEmptyInputParameters("NULL_OR_EMPTY_INPUT");
			}
			
			
			logger.info("Calling TCS auth service for heartbeat ..");
			
			Map<String,String> r = tcsApiAuthService.tcsAuthenticateHeartbeat(jsonRequest);
			
			ObjectMapper objectMapper = new ObjectMapper();
			
			String json = objectMapper.writeValueAsString(r);
			
			logger.info(String.format("Out Result:%s", json));
			
			logger.info("Return TCS auth response for heartbeat ..");
			
			return new HashMap<>(r);
			
			
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
    		value = "/v1/authenticate/RSA")
	@ResponseBody
	public Map<String,Object> testRSA(@RequestBody Map<String,Object> jsonRequest,
			@RequestHeader Map<String, String> headers){
		
		
		try {
			
			//String jwtk1= RSASecurityService.encrypt("JKHDJAGADDKJPOIRUWERYUWTWTASDHJEWFFGDS");
			//String jwtk2= RSASecurityService.encrypt("IEQYW343:HJKSFDF5:POIOTEF5");
			
			//System.out.println(jwtk1);
			//System.out.println(jwtk2);
			
			/*
			KeyPairGenerator generator;
			
			generator = KeyPairGenerator.getInstance("RSA");
			generator.initialize(1024);
			KeyPair pair = generator.generateKeyPair();
			
			PrivateKey privateKey = pair.getPrivate();
			PublicKey publicKey = pair.getPublic();
			
			try (FileOutputStream fos = new FileOutputStream("src/main/java/public.key")) {
			    fos.write(publicKey.getEncoded());
			} catch (FileNotFoundException e) {
				
				e.printStackTrace();
			} catch (IOException e) {
				
				e.printStackTrace();
			}
			
			try (FileOutputStream fos = new FileOutputStream("src/main/java/private.key")) {
			    fos.write(privateKey.getEncoded());
			} catch (FileNotFoundException e) {
			
				e.printStackTrace();
			} catch (IOException e) {
				
				e.printStackTrace();
			}
			*/
			
			
			
		} catch (Exception e) {
			
			e.printStackTrace();
			
			logger.error("Exception is caught ..",e);
			logger.error(e.getMessage());
		} 
		
		return null;
		
	}

}
