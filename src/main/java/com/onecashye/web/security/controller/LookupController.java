package com.onecashye.web.security.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.onecashye.web.security.exception.NullOrEmptyInputParameters;
import com.onecashye.web.security.exception.UserHasNoPrivileges;
import com.onecashye.web.security.middleware.dao.BankLookupDao;
import com.onecashye.web.security.middleware.dao.BillerDao;
import com.onecashye.web.security.service.TcsApiAuthService;
import com.onecashye.web.security.telepin.dao.LocationLookupDao;
import com.onecashye.web.security.telepin.service.AccountService;
import com.onecashye.web.security.telepin.service.TerminalService;

@RestController
@PropertySource(ignoreResourceNotFound = true, value = "classpath:tcs.properties")
public class LookupController {

	Logger logger = LogManager.getLogger(YKBServicesController.class);
	
	private final BankLookupDao bankLookupDao;
	private final BillerDao billerDao;
	private final LocationLookupDao locationLookupDao;
	private final TerminalService terminalService;
	private final TcsApiAuthService tcsApiAuthService;
	private final AccountService accountService;
	
	@Autowired
	public LookupController(BankLookupDao bankLookupDao, 
			BillerDao billerDao, 
			LocationLookupDao locationLookupDao, 
			TerminalService terminalService,
			TcsApiAuthService tcsApiAuthService, AccountService accountService) {
		super();
		this.bankLookupDao = bankLookupDao;
		this.billerDao = billerDao;
		this.locationLookupDao = locationLookupDao;
		this.terminalService = terminalService;
		this.tcsApiAuthService = tcsApiAuthService;
		this.accountService = accountService;
		
	}
	
	@RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE, 
    		method = RequestMethod.GET,
    		value = "/v1/lookup/bank")
	@ResponseBody
	public Map<String,Object> getBankList(@RequestParam String currency, @RequestHeader Map<String, String> headers){
		
		Map<String,Object> response = new HashMap<>();
		
		try {
			
			logger.info(String.format("Starting %s function call", "getBankList"));
			
			List<Map<String,Object>> banks = bankLookupDao.getBankDefinitions(currency);
			
			if(banks != null && banks.size()>0) {
				
				response.put("Result", 0);
				response.put("Message", String.format("Total banks found [%d]", banks.size()));
				response.put("Banks", banks);
				
				ObjectMapper objectMapper = new ObjectMapper();
				
				String json = objectMapper.writeValueAsString(response);
				
				logger.info(String.format("Out Result:%s", json));
				
				return response;
			}
			
			response.put("Result", -1);
			response.put("Message", String.format("Total banks found [%d]", banks.size()));
			
			ObjectMapper objectMapper = new ObjectMapper();
			
			String json = objectMapper.writeValueAsString(response);
			
			logger.info(String.format("Out Result:%s", json));
			
			return response;
		}
		catch(Exception e) {
			
			logger.error("Exception is caught ..",e);
			
			response.put("Result", String.format("%d", -100));
			response.put("Message", e.getMessage());
			
			return response;
		}
		
	}

	@RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE, 
    		method = RequestMethod.GET,
    		value = "/v1/lookup/forex/bank")
	@ResponseBody
	public Map<String,Object> getForexBankList(@RequestHeader Map<String, String> headers){
		
		Map<String,Object> response = new HashMap<>();
		
		try {
			
			logger.info(String.format("Starting %s function call", "getForexBankList"));
			
			List<Map<String,Object>> banks = bankLookupDao.getForexBankDefinitions();
			
			if(banks != null && banks.size()>0) {
				
				response.put("Result", 0);
				response.put("Message", String.format("Total banks found [%d]", banks.size()));
				response.put("Banks", banks);
				
				ObjectMapper objectMapper = new ObjectMapper();
				
				String json = objectMapper.writeValueAsString(response);
				
				logger.info(String.format("Out Result:%s", json));
				
				return response;
			}
			
			response.put("Result", -1);
			response.put("Message", String.format("Total banks found [%d]", banks.size()));
			
			ObjectMapper objectMapper = new ObjectMapper();
			
			String json = objectMapper.writeValueAsString(response);
			
			logger.info(String.format("Out Result:%s", json));
			
			return response;
		}
		catch(Exception e) {
			
			logger.error("Exception is caught ..",e);
			
			response.put("Result", String.format("%d", -100));
			response.put("Message", e.getMessage());
			
			return response;
		}
		
	}
	
	@RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE, 
    		method = RequestMethod.GET,
    		value = "/v1/lookup/yp/biller")
	@ResponseBody
	public Map<String,Object> getYemenPostBillers(@RequestHeader Map<String, String> headers){
		
		Map<String,Object> response = new HashMap<>();
		
		try {
			
			logger.info(String.format("Starting %s function call", "getYemenPostBillers"));
			
			response.put("Billers", billerDao.getBillerDefinition("YP"));
			response.put("Result", 0);
			response.put("Message", "");
			
			return response;

		}
		catch(Exception e) {
			
			logger.error("Exception is caught ..",e);
			
			response.put("Result", String.format("%d", -100));
			response.put("Message", e.getMessage());
			
			return response;
		}
		
		
		
		
	}
	
	@RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE, 
    		method = RequestMethod.GET,
    		value = "/v1/lookup/yp/biller/details")
	@ResponseBody
	public Map<String,Object> getYemenPostBillerDetails(@RequestParam Integer id,
			@RequestHeader Map<String, String> headers){
		
		Map<String,Object> response = new HashMap<>();
		
		try {
			
			logger.info(String.format("Starting %s function call", "getYemenPostBillerDetails"));
			
			if(id==null) {
				
				throw new NullOrEmptyInputParameters("NULL_OR_EMPTY_INPUT");
			}
			
			response.put("Details", billerDao.getBillerDetails(id));
			response.put("Result", 0);
			response.put("Message", "");
			
			return response;

			
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
			
			response.put("Result", String.format("%d", -100));
			response.put("Message", e.getMessage());
			
			return response;
		}
		
		
		
		
	}

	@RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE, 
    		method = RequestMethod.GET,
    		value = "/v1/lookup/location/province")
	@ResponseBody
	public Map<String,Object> getAllProvinces(@RequestHeader Map<String, String> headers){
		
		Map<String,Object> response = new HashMap<>();
		
		try {
			
			logger.info(String.format("Starting %s function call", "getAllProvinces"));
			
			response.put("Provinces", locationLookupDao.getAllProvinces());
			response.put("Result", 0);
			response.put("Message", "");
			
			ObjectMapper objectMapper = new ObjectMapper();
			
			String json = objectMapper.writeValueAsString(response);
			
			logger.info(String.format("Out Result:%s", json));
			
			return response;
			
		}
		catch(Exception e) {
			
			logger.error("Exception is caught ..",e);
			
			response.put("Result", String.format("%d", -100));
			response.put("Message", e.getMessage());
			
			return response;
		}
		
		
		
		
	}
	
	@RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE, 
    		method = RequestMethod.GET,
    		value = "/v1/lookup/location/city")
	@ResponseBody
	public Map<String,Object> getCities(@RequestParam(required=true) String province
			,@RequestHeader Map<String, String> headers){
		
		Map<String,Object> response = new HashMap<>();
		
		try {
			
			logger.info(String.format("Starting %s function call", "getCities"));
			
			if(province == null || province.isEmpty())
				throw new NullOrEmptyInputParameters("NULL_OR_EMPTY_INPUT");
			
			response.put("Cities", locationLookupDao.getCities(province));
			response.put("Result", 0);
			response.put("Message", "");
			
			ObjectMapper objectMapper = new ObjectMapper();
			
			String json = objectMapper.writeValueAsString(response);
			
			logger.info(String.format("Out Result:%s", json));
			
			return response;
			
			
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
			
			response.put("Result", String.format("%d", -100));
			response.put("Message", e.getMessage());
			
			return response;
		}
		
		
		
		
	}
	
	
	
	@RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE, 
    		method = RequestMethod.GET,
    		value = "/v1/lookup/interface")
	@ResponseBody
	public Map<String,Object> getInterfacesV2(@RequestParam String terminal_type,
			@RequestHeader Map<String, String> headers){
		
		/*
		 * Created 16052022
		 * 
		 * To replace old API to get role names for drop lists
		 */
		Map<String,Object> response = new HashMap<>();
		
		try {
			
			
			
			logger.info(String.format("Starting %s function call", "getInterfacesV2"));
			
			if(terminal_type==null || terminal_type.isEmpty()) {
				
				throw new NullOrEmptyInputParameters("NULL_OR_EMPTY_INPUT");
			}
			
			
			
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
				response.put("Roles", terminalService.getTerminalTypesLookup(tcsApiAuthService.extractUserName(headers),tcsApiAuthService.extractUserAccountId(headers), terminal_type, "WebRoleMasterManager"));
				response.put("Result", 0);
				response.put("Message", "");
				
				return response;
			case "WebRoleManager":
				response.put("Roles", terminalService.getTerminalTypesLookup(tcsApiAuthService.extractUserName(headers),tcsApiAuthService.extractUserAccountId(headers), terminal_type, "WebRoleManager"));
				response.put("Result", 0);
				response.put("Message", "");
				
				return response;
			default:
				throw new UserHasNoPrivileges("User has no privileges");
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
			
			response.put("Result", String.format("%d", -100));
			response.put("Message", e.getMessage());
			
			return response;
		}
	
	}


	@RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE, 
    		method = RequestMethod.GET,
    		value = "/v1/lookup/cashup/filter/terminals")
	@ResponseBody
	public Map<String,Object> filterTerminals(@RequestHeader Map<String, String> headers,
			@Value("${tcs.privilege.for.cashup.filters}") String targetPrivilege){
		
		/*
		 * 
		 * 
		 * 
		 * 
		 * This API is only to be used for Edgecom Extrnal Web
		 * 
		 * 
		 * 
		 */
		
		
		Map<String,Object> request = new HashMap<>(); // for getting privileges
		Map<String,Object> response = new HashMap<>();
		
		try {
			
			logger.info(String.format("Starting %s function call", "filterTerminals"));
			
			//1- Get privilege
			
			request.put("username", tcsApiAuthService.extractUserName(headers));
			request.put("password", tcsApiAuthService.extractUserPassword(headers));
			
			List<String> privileges = tcsApiAuthService.tcsGetCorePrivileges(request);
			
			if(privileges.contains(targetPrivilege)) {
				
				//Return all Terminals
				Object terminals = terminalService.getAccountTerminals(tcsApiAuthService.extractUserAccountId(headers));
				
				response.put("Terminals", terminals);
				response.put("Result", "0");
			}
			else {
				
				//Return the logged in user
				Object terminal = terminalService.getSelfTerminal(tcsApiAuthService.extractUserAccountId(headers),
						tcsApiAuthService.extractUserName(headers));
				
				response.put("Terminals", terminal);
				response.put("Result", "0");
			}
			
			
			return response;
				
		}
		catch(Exception e) {
			
			logger.error("Exception is caught ..",e);
			
			response.put("Result", String.format("%d", -100));
			response.put("Message", e.getMessage());
			
			return response;
		}
	
	}
	
	@RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE, 
    		method = RequestMethod.GET,
    		value = "/v2/lookup/cashup/filter/terminals")
	@ResponseBody
	public Map<String,Object> filterTerminals(@RequestHeader Map<String, String> headers){
		
		Map<String,Object> response = new HashMap<>();
		
		try {
			
			logger.info("Starting filterTerminals process ..");
			
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
			
			List<Map<String,Object>>  result = terminalService.getAccountWebTerminals4Cashup(privileges,user_name,account_id, "WEB");
			
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
			
			response.put("Result", String.format("%d", -100));
			response.put("Message", "EXCEPTION");
			response.put("Error", e.getMessage());
			
			return response;
		}
	
	}
	
	@RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE, 
    		method = RequestMethod.GET,
    		value = "/v1/lookup/cashup/filter/staff")
	@ResponseBody
	public Map<String,Object> filterStaff(@RequestHeader Map<String, String> headers,
			@Value("${tcs.privilege.for.cashup.filters}") String targetPrivilege){
		
		Map<String,Object> request = new HashMap<>(); // for getting privileges
		Map<String,Object> response = new HashMap<>();
		
		
		try {
			
			logger.info(String.format("Starting %s function call", "filterStaff"));
			
			//1- Get privilege
			
			request.put("username", tcsApiAuthService.extractUserName(headers));
			request.put("password", tcsApiAuthService.extractUserPassword(headers));
			
			List<String> privileges = tcsApiAuthService.tcsGetCorePrivileges(request);
			
			if(privileges.contains(targetPrivilege)) {
				
				//Return all Terminals
				Object terminals = terminalService.getAccountGptStaff(tcsApiAuthService.extractUserAccountId(headers));
				
				response.put("Terminals", terminals);
				response.put("Result", "0");
			}
			else {
				
				//Return empty list
				
				
				response.put("Terminals", new ArrayList<Object>());
				response.put("Result", "0");
			}
			
			
			return response;
				
		}
		catch(Exception e) {
			
			logger.error("Exception is caught ..",e);
			
			response.put("Result", String.format("%d", -100));
			response.put("Message", e.getMessage());
			
			return response;
		}
	
	}
	
	@RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE, 
    		method = RequestMethod.GET,
    		value = "/v2/lookup/cashup/filter/staff")
	@ResponseBody
	public Map<String,Object> filterStaff(@RequestHeader Map<String, String> headers){
		
		Map<String,Object> response = new HashMap<>();
		
		try {
			
			logger.info("Starting filterStaff process ..");
			
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
			
			List<Map<String,Object>>  result = terminalService.getAccountAppTerminals4Cashup(privileges,user_name,account_id, "GPT");
			
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
			
			response.put("Result", String.format("%d", -100));
			response.put("Message", "EXCEPTION");
			response.put("Error", e.getMessage());
			
			return response;
		}
	
	}
	
	@RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE, 
    		method = RequestMethod.GET,
    		value = "/v1/lookup/cashup/filter/store")
	@ResponseBody
	public Map<String,Object> filterStore(@RequestHeader Map<String, String> headers,
			@Value("${tcs.privilege.for.cashup.filters}") String targetPrivilege){
		
		Map<String,Object> request = new HashMap<>(); // for getting privileges
		Map<String,Object> response = new HashMap<>();
		
		
		try {
			
			logger.info(String.format("Starting %s function call", "filterStore"));
			
			//1- Get privilege
			
			request.put("username", tcsApiAuthService.extractUserName(headers));
			request.put("password", tcsApiAuthService.extractUserPassword(headers));
			
			List<String> privileges = tcsApiAuthService.tcsGetCorePrivileges(request);
			
			if(privileges.contains(targetPrivilege)) {
				
				//Return all Terminals
				Object terminals = accountService.getAccountChildrenForCashupReport(tcsApiAuthService.extractUserAccountId(headers));
				
				response.put("Terminals", terminals);
				response.put("Result", "0");
			}
			else {
				
				//Return empty list
				
				
				response.put("Terminals", new ArrayList<Object>());
				response.put("Result", "0");
			}
			
			
			return response;
				
		}
		catch(Exception e) {
			
			logger.error("Exception is caught ..",e);
			
			response.put("Result", String.format("%d", -100));
			response.put("Message", e.getMessage());
			
			return response;
		}
	
	}

	@RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE, 
    		method = RequestMethod.GET,
    		value = "/v2/lookup/cashup/filter/store")
	@ResponseBody
	public Map<String,Object> filterStore(@RequestHeader Map<String, String> headers){
		
		Map<String,Object> response = new HashMap<>();
		
		try {
			
			logger.info("Starting filterStore process ..");
			
			logger.info("Extract Account ID ..");
			
			String account_id=tcsApiAuthService.extractUserAccountId(headers);
			String user_name=tcsApiAuthService.extractUserName(headers);
			String password=tcsApiAuthService.extractUserPassword(headers);
			
			logger.info(String.format("Account ID found: %s",account_id));
			logger.info(String.format("User Name found: %s",user_name));
			
			Map<String,Object> request = new HashMap<>();
			
			request.put("username", user_name);
			request.put("password", password);
			request.put("filter", "WebRoleMasterManager");
			
			List<String> privileges = tcsApiAuthService.tcsGetFilteredCorePrivileges(request);
			
			if(privileges == null || privileges.size() <= 0) {
				
				//User has no WebRole* privileges
				throw new UserHasNoPrivileges("User has no privileges");
			}
			
			
			Object stores = accountService.getAccountChildrenForCashupReport(tcsApiAuthService.extractUserAccountId(headers));
			
			
			response.put("Result", 0);
			response.put("Message", "OK");
			response.put("List", stores);
			
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
			
			response.put("Result", String.format("%d", -100));
			response.put("Message", "EXCEPTION");
			response.put("Error", e.getMessage());
			
			return response;
		}
	
	}
}
	
	