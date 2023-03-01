package com.onecashye.web.security.controller;


import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
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
import com.onecashye.web.security.exception.BrandNotFound;
import com.onecashye.web.security.exception.InvalidCheckerAction;
import com.onecashye.web.security.exception.NullOrEmptyInputParameters;
import com.onecashye.web.security.exception.SalesOrderNotFound;
import com.onecashye.web.security.exception.SalesRequestNotFound;
import com.onecashye.web.security.exception.UserHasNoPrivileges;
import com.onecashye.web.security.exception.VoucherNotFound;
import com.onecashye.web.security.helper.MappingParamsHelper;
import com.onecashye.web.security.helper.TcsParamsValidationHelper;
import com.onecashye.web.security.middleware.service.BrandLookupService;
import com.onecashye.web.security.middleware.service.MakerCheckerService;
import com.onecashye.web.security.middleware.service.SalesOrderService;
import com.onecashye.web.security.service.TcsApiAccountService;
import com.onecashye.web.security.service.TcsApiAuthService;
import com.onecashye.web.security.service.TcsApiPaymentService;
import com.onecashye.web.security.telepin.service.VoucherService;

@RestController
@PropertySources({
    @PropertySource("classpath:brands.properties"),
    @PropertySource("classpath:tcs.properties")
})
public class PaymentController {

	Logger logger = LogManager.getLogger(PaymentController.class);
	
	private final TcsApiPaymentService tcsApiPaymentService;
	private final TcsApiAuthService tcsApiAuthService;
	private final TcsApiAccountService tcsApiAccountService;
	private final Environment env;
	private final MakerCheckerService makerCheckerService;
	private final BrandLookupService brandLookupService;
	private final VoucherService voucherService;
	private final SalesOrderService salesOrderService;
	
	private static String BZCASHOUT="BZCASHOUT";
	private static String FILTER_CHECKER="WebChecker";
	private static String FILTER_FOREX_MASTER="WebAddForexRates";
	
	private static String CHECKER1="WebChecker1";
	private static String CHECKER2="WebChecker2";
	
	@Autowired
	public PaymentController(TcsApiPaymentService tcsApiPaymentService, 
			TcsApiAuthService tcsApiAuthService, 
			Environment env, 
			MakerCheckerService makerCheckerService, 
			BrandLookupService brandLookupService, 
			VoucherService voucherService, 
			SalesOrderService salesOrderService, 
			TcsApiAccountService tcsApiAccountService) {
		super();
		this.tcsApiPaymentService = tcsApiPaymentService;
		this.tcsApiAuthService = tcsApiAuthService;
		this.tcsApiAccountService = tcsApiAccountService;
		this.env = env;
		this.makerCheckerService = makerCheckerService;
		this.brandLookupService = brandLookupService;
		this.voucherService = voucherService;
		this.salesOrderService = salesOrderService;
		
	}
	
	@RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE, 
    		method = RequestMethod.POST,
    		value = "/v1/payment/customer/cashin")
	@ResponseBody
	public Map<String,Object> customerCashin(@RequestBody Map<String,Object> jsonRequest,
			@RequestHeader Map<String, String> headers){
		
		/*
		 * This is used for one shot Customer Cash In Transactions
		 */
		
		Map<String,Object> response = new HashMap<>();
		
		try {

			logger.info("Starting customerCashin process ..");
			logger.info("Checking input params ..");

			TcsParamsValidationHelper.checkRequiredParams(env, "tcs.required.params.payment.customer.cashin", jsonRequest, logger);
			
			if((jsonRequest.get("check")==null || 
					jsonRequest.get("check").toString().isEmpty() || 
					jsonRequest.get("check").toString().equalsIgnoreCase("false"))) {
				
				logger.info("Calling in CheckOnly mode=false  ..");
				
				if(jsonRequest.get("password") == null || jsonRequest.get("password").toString().isEmpty()) {
					
					logger.warn("Password is mandatory  ..");
					logger.warn("Prepare PASSWORD_REQUIRED response  ..");
					
					response.put("Result", String.format("%d", -200));
					response.put("Message", "PASSWORD_REQUIRED");
					
					return response;
				}
				else {
					
					logger.info("Extracting username  ..");
					
					jsonRequest.put("username",tcsApiAuthService.extractUserName(headers));
					jsonRequest.put("check","false");
					
					logger.info("Extracting Brand ID for tcs.payment.customer.cashin  ..");
					
					//Change done for multicurrency operations
					//11-03-2022
					
					Optional<List<Map<String,Object>>> brand = brandLookupService.findBrand("tcs.payment.customer.cashin", jsonRequest.get("currency").toString());
					
					if(!brand.isPresent()) {
						
						logger.warn("Brand not found for tcs.payment.customer.cashin  ..");
						
						throw new BrandNotFound("");
					}
					
					List<Map<String,Object>> r = brand.get();
					
					logger.info(String.format("List of Brand ID found for tcs.payment.customer.cashin  = %d", r.size()));
					logger.info(String.format("Brand ID found for tcs.payment.customer.cashin  = %d", r.get(0).get("Brand")));
					
					jsonRequest.put("brand",r.get(0).get("Brand"));
					
					//End of multicurrency change
					
					logger.info("Calling TCS payment function  ..");
					
					Map<String,String> result = tcsApiPaymentService.tcsCashPayment(jsonRequest,headers);
					
					ObjectMapper objectMapper = new ObjectMapper();
					
					String json = objectMapper.writeValueAsString(result);
					
					logger.info(String.format("Out Result:%s", json));
					
					logger.info("Return TCS response  ..");
					
					return new HashMap<>(result);
				}
				
			}
			else {
				
				logger.info("Calling in CheckOnly mode=true  ..");
				logger.info("Extracting username and password  ..");
				
				jsonRequest.put("username",tcsApiAuthService.extractUserName(headers));
				jsonRequest.put("password",tcsApiAuthService.extractUserPassword(headers));
				
				//11-03-2022
				jsonRequest.put("code","tcs.payment.customer.cashin");
				
				logger.info("Extracting Brand ID for tcs.payment.customer.cashin  ..");
				
				jsonRequest.put("brand",getBrandId(jsonRequest));
				
				///////////////////////////////////////////////////////////////////////////////////////////
				
				logger.info("Calling TCS payment function  ..");
				
				Map<String,String> result = tcsApiPaymentService.tcsCashPaymentCheck(jsonRequest,headers);
				
				ObjectMapper objectMapper = new ObjectMapper();
				
				String json = objectMapper.writeValueAsString(result);
				
				logger.info(String.format("Out Result:%s", json));
				
				logger.info("Return TCS response  ..");
				
				return new HashMap<>(result);
			}
			
			
			
		}
		catch(NullOrEmptyInputParameters e) {
			
			logger.warn("Input param is null  ..",e);
			logger.warn("Prepare NULL_OR_EMPTY_INPUT response  ..");
			
			response.put("Result", String.format("%d", -200));
			response.put("Message", "NULL_OR_EMPTY_INPUT");
			
			return response;
			
		}
		catch(BrandNotFound e) {
			
			response.put("Result", String.format("%d", -200));
			response.put("Message", "BRAND_NOT_FOUND");
			
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
    		value = "/v1/payment/business/cash")
	@ResponseBody
	public Map<String,Object> businessCashinAndOut(@RequestBody Map<String,Object> jsonRequest,
			@RequestHeader Map<String, String> headers){
		
		/*
		 * This is used for one shot Business Cash In & Out Transactions
		 * 
		 * Update 18042022, This should be used only for BZCASHIN
		 * 
		 * Update 06092022, This could be used for Cash IN & OUT single shot
		 * 
		 */
		
		Map<String,Object> response = new HashMap<>();
		
		final String code=jsonRequest.get("code").toString();
		
		try {
			
			logger.info("Starting businessCashinAndOut process ..");
			logger.info("Checking input params ..");

			TcsParamsValidationHelper.checkRequiredParams(env, "tcs.required.params.payment.business", jsonRequest, logger);
			
			if((jsonRequest.get("check")==null || 
					jsonRequest.get("check").toString().isEmpty() || 
					jsonRequest.get("check").toString().equalsIgnoreCase("false"))) {
				
				logger.info("Calling in CheckOnly mode=false  ..");
				
				if(jsonRequest.get("password") == null || jsonRequest.get("password").toString().isEmpty()) {
					
					logger.warn("Password is mandatory  ..");
					logger.warn("Prepare PASSWORD_REQUIRED response  ..");
					
					response.put("Result", String.format("%d", -200));
					response.put("Message", "PASSWORD_REQUIRED");
					
					return response;
				}
				else {
					
					Map<String,String> result = new HashMap<>();
					
					logger.info("Extracting username  ..");
					
					jsonRequest.put("username",tcsApiAuthService.extractUserName(headers));
					jsonRequest.put("check","false");
					
					logger.info(String.format("Extracting Brand for code = %s ..", code));
					
					jsonRequest.put("brand",getBrandId(jsonRequest));
					
					if(code.contentEquals(BZCASHOUT)) {
						
						logger.info("Calling TCS payment function for CashOut Trx ..");
						
						result = tcsApiPaymentService.tcsBusinessCashOutPayment(jsonRequest,headers);
					}
					else {
						
						logger.info("Calling TCS payment function ..");
						
						result = tcsApiPaymentService.tcsCashPayment(jsonRequest,headers);
					}
					
					
					
					logger.info("Return TCS response ..");
					
					return new HashMap<>(result);
				}
				
			}
			else {
				
				logger.info("Calling in CheckOnly mode=true  ..");
				logger.info("Extracting Username & Password ..");
				
				jsonRequest.put("username",tcsApiAuthService.extractUserName(headers));
				jsonRequest.put("password",tcsApiAuthService.extractUserPassword(headers));
				
				logger.info(String.format("Extracting Brand for code = %s ..", jsonRequest.get("code")));
				
				jsonRequest.put("brand",getBrandId(jsonRequest));
				
				logger.info("Calling TCS payment function ..");
				
				Map<String,String> result = tcsApiPaymentService.tcsCashPaymentCheck(jsonRequest,headers);
				
				logger.info("Return TCS response ..");
				
				return new HashMap<>(result);
			}
			
			
			
		}
		catch(BrandNotFound e) {
			
			response.put("Result", String.format("%d", -200));
			response.put("Message", "BRAND_NOT_FOUND");
			
			return response;
			
		}
		catch(NullOrEmptyInputParameters e) {
			
			logger.warn("Input param is null  ..",e);
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
    		value = "/v1/payment/business/transferonbehalf")
	@ResponseBody
	public Map<String,Object> businessTransferOnBehalf(@RequestBody Map<String,Object> jsonRequest,
			@RequestHeader Map<String, String> headers){
		
		/*
		 * 
		 * 
		 * 05022023 This API to support [transfer on-behalf] transactions
		 * 
		 */
		
		Map<String,Object> response = new HashMap<>();
		
		//Code is fixed for this operation
		final String code="ONBEHALF2BUSINESS";
		jsonRequest.put("code", code);
		
		try {
			
			logger.info("Starting businessTransferOnBehalf process ..");
			logger.info("Checking input params ..");

			TcsParamsValidationHelper.checkRequiredParams(env, "tcs.required.params.payment.business.onbehalf", jsonRequest, logger);
			
			if((jsonRequest.get("check")==null || 
					jsonRequest.get("check").toString().isEmpty() || 
					jsonRequest.get("check").toString().equalsIgnoreCase("false"))) {
				
				logger.info("Calling in CheckOnly mode=false  ..");
				
				if(jsonRequest.get("password") == null || jsonRequest.get("password").toString().isEmpty()) {
					
					logger.warn("Password is mandatory  ..");
					logger.warn("Prepare PASSWORD_REQUIRED response  ..");
					
					response.put("Result", String.format("%d", -200));
					response.put("Message", "PASSWORD_REQUIRED");
					
					return response;
				}
				else {
					
					Map<String,String> result = new HashMap<>();
					
					logger.info("Extracting username  ..");
					
					jsonRequest.put("username",tcsApiAuthService.extractUserName(headers));
					jsonRequest.put("check","false");
					
					logger.info(String.format("Extracting Brand for code = %s ..", code));
					
					jsonRequest.put("brand",getBrandId(jsonRequest));
					
					logger.info("Calling TCS payment function for CashOut Trx ..");
					
					result = tcsApiPaymentService.tcsBusinessOnbehalfPayment(jsonRequest,headers);
					
					logger.info("Return TCS response ..");
					
					return new HashMap<>(result);
				}
				
			}
			else {
				
				logger.info("Calling in CheckOnly mode=true  ..");
				logger.info("Extracting Username & Password ..");
				
				jsonRequest.put("username",tcsApiAuthService.extractUserName(headers));
				jsonRequest.put("password",tcsApiAuthService.extractUserPassword(headers));
				
				logger.info(String.format("Extracting Brand for code = %s ..", jsonRequest.get("code")));
				
				jsonRequest.put("brand",getBrandId(jsonRequest));
				
				logger.info("Calling TCS payment function ..");
				
				Map<String,String> result = tcsApiPaymentService.tcsCashPaymentCheck(jsonRequest,headers);
				
				logger.info("Return TCS response ..");
				
				return new HashMap<>(result);
			}
			
			
			
		}
		catch(BrandNotFound e) {
			
			response.put("Result", String.format("%d", -200));
			response.put("Message", "BRAND_NOT_FOUND");
			
			return response;
			
		}
		catch(NullOrEmptyInputParameters e) {
			
			logger.warn("Input param is null  ..",e);
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
    		value = "/v1/payment/business/forex")
	@ResponseBody
	public Map<String,Object> businessForex(@RequestBody Map<String,Object> jsonRequest,
			@RequestHeader Map<String, String> headers){
		
		/*
		 * This is used for one shot Business Forex Transactions
		 * 
		 * 
		 */
		
		Map<String,Object> response = new HashMap<>();
		
		/*
		 * Transaction Code and Currency are fixed
		 */
		jsonRequest.put("code","FOREX");
		jsonRequest.put("currency","YER");
		
		final String code=jsonRequest.get("code").toString();
		
		try {
			
			logger.info("Starting businessForex process ..");
			logger.info("Checking input params ..");

			TcsParamsValidationHelper.checkRequiredParams(env, "tcs.required.params.payment.forex", jsonRequest, logger);
			
			if((jsonRequest.get("check")==null || 
					jsonRequest.get("check").toString().isEmpty() || 
					jsonRequest.get("check").toString().equalsIgnoreCase("false"))) {
				
				logger.info("Calling in CheckOnly mode=false  ..");
				
				if(jsonRequest.get("password") == null || jsonRequest.get("password").toString().isEmpty()) {
					
					logger.warn("Password is mandatory  ..");
					logger.warn("Prepare PASSWORD_REQUIRED response  ..");
					
					response.put("Result", String.format("%d", -200));
					response.put("Message", "PASSWORD_REQUIRED");
					
					return response;
				}
				else {
					
					Map<String,String> result = new HashMap<>();
					
					logger.info("Extracting username  ..");
					
					jsonRequest.put("username",tcsApiAuthService.extractUserName(headers));
					jsonRequest.put("check","false");
					
					logger.info(String.format("Extracting Brand for code = %s ..", code));
					
					jsonRequest.put("brand",getBrandId(jsonRequest));
					
					result = tcsApiPaymentService.tcsForexPayment(jsonRequest,headers);
					
					
					logger.info("Return TCS response ..");
					
					return new HashMap<>(result);
				}
				
			}
			else {
				
				logger.info("Calling in CheckOnly mode=true  ..");
				logger.info("Extracting Username & Password ..");
				
				jsonRequest.put("username",tcsApiAuthService.extractUserName(headers));
				jsonRequest.put("password",tcsApiAuthService.extractUserPassword(headers));
				
				logger.info(String.format("Extracting Brand for code = %s ..", jsonRequest.get("code")));
				
				jsonRequest.put("brand",getBrandId(jsonRequest));
				
				logger.info("Calling TCS payment function ..");
				
				Map<String,String> result = tcsApiPaymentService.tcsCashPaymentCheck(jsonRequest,headers);
				
				logger.info("Return TCS response ..");
				
				return new HashMap<>(result);
			}
			
			
			
		}
		catch(BrandNotFound e) {
			
			response.put("Result", String.format("%d", -200));
			response.put("Message", "BRAND_NOT_FOUND");
			
			return response;
			
		}
		catch(NullOrEmptyInputParameters e) {
			
			logger.warn("Input param is null  ..",e);
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
    		value = "/v1/float/sell")
	@ResponseBody
	public Map<String,Object> sellFloat(@RequestBody Map<String,Object> jsonRequest,
			@RequestHeader Map<String, String> headers){
		
		/*
		 * This is used for one shot Sell Float Transactions
		 */
		
		Map<String,Object> response = new HashMap<>();
		
		try {
			
			logger.info("Starting sellFloat process ..");
			logger.info("Checking input params ..");

			TcsParamsValidationHelper.checkRequiredParams(env, "tcs.required.params.payment.business", jsonRequest, logger);
			
			if((jsonRequest.get("check")==null || 
					jsonRequest.get("check").toString().isEmpty() || 
					jsonRequest.get("check").toString().equalsIgnoreCase("false"))) {
				
				logger.info("Calling in CheckOnly mode=false  ..");
				
				if(jsonRequest.get("password") == null || jsonRequest.get("password").toString().isEmpty()) {
					
					logger.warn("Password is mandatory  ..");
					logger.warn("Prepare PASSWORD_REQUIRED response  ..");
					
					response.put("Result", String.format("%d", -200));
					response.put("Message", "PASSWORD_REQUIRED");
					
					return response;
				}
				else {
					
					logger.info("Extracting username  ..");
					
					jsonRequest.put("username",tcsApiAuthService.extractUserName(headers));
					jsonRequest.put("check","false");
					
					//11-03-2022
					jsonRequest.put("code","SELLFLOAT");
					
					logger.info(String.format("Extracting Brand for code = %s ..", jsonRequest.get("code")));
					
					jsonRequest.put("brand",getBrandId(jsonRequest));
					
					/////////////////////////////////////////////////////////////////////////////////////////
					
					
					logger.info("Calling TCS payment function  ..");
					
					Map<String,String> result = tcsApiPaymentService.tcsSellFloat(jsonRequest,headers);
					
					ObjectMapper objectMapper = new ObjectMapper();
					
					String json = objectMapper.writeValueAsString(result);
					
					logger.info(String.format("Out Result:%s", json));
					
					logger.info("Return TCS response  ..");
					
					return new HashMap<>(result);
				}
				
			}
			else {
				
				logger.info("Extracting username and password  ..");
				
				jsonRequest.put("username",tcsApiAuthService.extractUserName(headers));
				jsonRequest.put("password",tcsApiAuthService.extractUserPassword(headers));
				
				//11-03-2022
				jsonRequest.put("code","SELLFLOAT");
				
				logger.info(String.format("Extracting Brand for code = %s ..", jsonRequest.get("code")));
				
				jsonRequest.put("brand",getBrandId(jsonRequest));
				
				/////////////////////////////////////////////////////////////////////////////////////////
				
				logger.info("Calling TCS payment function  ..");
				
				Map<String,String> result = tcsApiPaymentService.tcsSellFloatCheck(jsonRequest,headers);
				
				ObjectMapper objectMapper = new ObjectMapper();
				
				String json = objectMapper.writeValueAsString(result);
				
				logger.info(String.format("Out Result:%s", json));
				
				logger.info("Return TCS response  ..");
				
				return new HashMap<>(result);
			}
			
			
			
		}
		catch(BrandNotFound e) {
			
			response.put("Result", String.format("%d", -200));
			response.put("Message", "BRAND_NOT_FOUND");
			
			return response;
			
		}
		catch(NullOrEmptyInputParameters e) {
			
			logger.warn("Input param is null  ..",e);
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
    		value = "/v1/payment/oneshot/deletion")
	@ResponseBody
	public Map<String,Object> oneShotDeletion(@RequestBody Map<String,Object> jsonRequest,
			@RequestHeader Map<String, String> headers){
		
		/*
		 * Updated 30102022
		 * 
		 * The latest scenario agreed upon is:
		 * 
		 * For money deletion there will be only one shot service, where master manager user requests
		 * a money deletion sales request and based on that a request for approval should be raised to ONE 
		 * finance operations team for approval.
		 * 
		 * This is used for one shot money deletion (Cancelled Scenario)
		 * destination=967710001 (fixed)
		 */
		
		Map<String,Object> response = new HashMap<>();
		
		try {
			
			logger.info("Starting oneShotDeletion process ..");
			logger.info("Checking input params ..");

			TcsParamsValidationHelper.checkRequiredParams(env, "tcs.required.params.payment.business", jsonRequest, logger);
			
			if((jsonRequest.get("check")==null || 
					jsonRequest.get("check").toString().isEmpty() || 
					jsonRequest.get("check").toString().equalsIgnoreCase("false"))) {
				
				logger.info("Calling in CheckOnly mode=false  ..");
				
				if(jsonRequest.get("password") == null || jsonRequest.get("password").toString().isEmpty()) {
					
					logger.warn("Password is mandatory  ..");
					logger.warn("Prepare PASSWORD_REQUIRED response  ..");
					
					response.put("Result", String.format("%d", -200));
					response.put("Message", "PASSWORD_REQUIRED");
					
					return response;
				}
				else {
					
					logger.info("Extracting username  ..");
					
					jsonRequest.put("username",tcsApiAuthService.extractUserName(headers));
					jsonRequest.put("source_id",tcsApiAuthService.extractUserAccountId(headers));
					jsonRequest.put("source",tcsApiAuthService.extractUserAccountNumber(headers));
					jsonRequest.put("destination","967710001");
					jsonRequest.put("code","MONEYUNALLOC");
					jsonRequest.put("remarks","Money Deletion Request");
					jsonRequest.put("check","false");
					
					//22-10-2022
					//jsonRequest.put("code","MONEYUNALLOC");
					//jsonRequest.put("account","967710001");
					
					logger.info(String.format("Extracting Brand for code = %s ..", jsonRequest.get("code")));
					
					jsonRequest.put("brand",getBrandId(jsonRequest));
					
					/////////////////////////////////////////////////////////////////////////////////////////
					
					
					//logger.info("Calling TCS payment function  ..");
					
					//Map<String,String> result = tcsApiPaymentService.tcsDeleteMoney(jsonRequest,headers);
					
					logger.info("Calling TCS sales request for money deletion function  ..");
					
					Map<String,String> result = tcsApiPaymentService.tcsSalesRequest(jsonRequest,headers,false);
					
					logger.info(String.format("tcsSalesRequest result=%s", result.get("Result")));
					logger.info(String.format("tcsSalesRequest message=%s", result.get("Message")));
					
					ObjectMapper objectMapper = new ObjectMapper();
					
					String json = objectMapper.writeValueAsString(result);
					
					logger.info(String.format("Out Result:%s", json));
					
					logger.info("Return TCS response  ..");
					
					return new HashMap<>(result);
				}
				
			}
			else {
				
				logger.info("Extracting username and password  ..");
				
				//jsonRequest.put("username",tcsApiAuthService.extractUserName(headers));
				//jsonRequest.put("password",tcsApiAuthService.extractUserPassword(headers));
				
				jsonRequest.put("username",tcsApiAuthService.extractUserName(headers));
				jsonRequest.put("password",tcsApiAuthService.extractUserPassword(headers));
				jsonRequest.put("source_id",tcsApiAuthService.extractUserAccountId(headers));
				jsonRequest.put("source",tcsApiAuthService.extractUserAccountNumber(headers));
				jsonRequest.put("destination","967710001");
				
				jsonRequest.put("code","MONEYUNALLOC");
				
				//22-10-2022
				//jsonRequest.put("code","MONEYUNALLOC");
				//jsonRequest.put("account","967710001");
				
				logger.info(String.format("Extracting Brand for code = %s ..", jsonRequest.get("code")));
				
				jsonRequest.put("brand",getBrandId(jsonRequest));
				
				/////////////////////////////////////////////////////////////////////////////////////////
				
				//logger.info("Calling TCS payment function  ..");
				
				//Map<String,String> result = tcsApiPaymentService.tcsDeleteMoneyCheck(jsonRequest,headers);
				
				logger.info("Calling TCS sales request for money deletion function  ..");
				
				Map<String,String> result = tcsApiPaymentService.tcsCheckSalesRequest(jsonRequest,headers);
				
				ObjectMapper objectMapper = new ObjectMapper();
				
				String json = objectMapper.writeValueAsString(result);
				
				logger.info(String.format("Out Result:%s", json));
				
				logger.info("Return TCS response  ..");
				
				return new HashMap<>(result);
			}
			
			
			
		}
		catch(BrandNotFound e) {
			
			response.put("Result", String.format("%d", -200));
			response.put("Message", "BRAND_NOT_FOUND");
			
			return response;
			
		}
		catch(NullOrEmptyInputParameters e) {
			
			logger.warn("Input param is null  ..",e);
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
    		value = "/v1/float/push")
	@ResponseBody
	public Map<String,Object> pushFloat(@RequestBody Map<String,Object> jsonRequest,
			@RequestHeader Map<String, String> headers){
		
		/*
		 * This is used for one shot Push Float Transactions
		 */
		
		Map<String,Object> response = new HashMap<>();
		
		try {
			
			logger.info("Starting pushFloat process ..");
			logger.info("Checking input params ..");

			TcsParamsValidationHelper.checkRequiredParams(env, "tcs.required.params.payment.business", jsonRequest, logger);
			
			if((jsonRequest.get("check")==null || 
					jsonRequest.get("check").toString().isEmpty() || 
					jsonRequest.get("check").toString().equalsIgnoreCase("false"))) {
				
				logger.info("Calling in CheckOnly mode=false  ..");
				
				if(jsonRequest.get("password") == null || jsonRequest.get("password").toString().isEmpty()) {
					
					logger.warn("Password is mandatory  ..");
					logger.warn("Prepare PASSWORD_REQUIRED response  ..");
					
					response.put("Result", String.format("%d", -200));
					response.put("Message", "PASSWORD_REQUIRED");
					
					return response;
				}
				else {
					
					logger.info("Extracting username  ..");
					
					jsonRequest.put("username",tcsApiAuthService.extractUserName(headers));
					jsonRequest.put("check","false");
					
					//11-03-2022
					jsonRequest.put("code","PUSHFLOAT");
					
					logger.info(String.format("Extracting Brand for code = %s ..", jsonRequest.get("code")));
					
					jsonRequest.put("brand",getBrandId(jsonRequest));
					
					/////////////////////////////////////////////////////////////////////////////////////////
					
					logger.info("Calling TCS payment function  ..");
					
					Map<String,String> result = tcsApiPaymentService.tcsPushFloat(jsonRequest,headers);
					
					ObjectMapper objectMapper = new ObjectMapper();
					
					String json = objectMapper.writeValueAsString(result);
					
					logger.info(String.format("Out Result:%s", json));
					
					logger.info("Return TCS response  ..");
					
					return new HashMap<>(result);
				}
				
			}
			else {
				
				logger.info("Extracting username and password  ..");
				
				jsonRequest.put("username",tcsApiAuthService.extractUserName(headers));
				jsonRequest.put("password",tcsApiAuthService.extractUserPassword(headers));
				
				//11-03-2022
				jsonRequest.put("code","PUSHFLOAT");
				
				logger.info(String.format("Extracting Brand for code = %s ..", jsonRequest.get("code")));
				
				jsonRequest.put("brand",getBrandId(jsonRequest));
				
				/////////////////////////////////////////////////////////////////////////////////////////
				
				logger.info("Calling TCS payment function  ..");
				
				Map<String,String> result = tcsApiPaymentService.tcsPushFloatCheck(jsonRequest,headers);
				
				ObjectMapper objectMapper = new ObjectMapper();
				
				String json = objectMapper.writeValueAsString(result);
				
				logger.info(String.format("Out Result:%s", json));
				
				logger.info("Return TCS response  ..");
				
				return new HashMap<>(result);
			}
			
			
			
		}
		catch(BrandNotFound e) {
			
			response.put("Result", String.format("%d", -200));
			response.put("Message", "BRAND_NOT_FOUND");
			
			return response;
			
		}
		catch(NullOrEmptyInputParameters e) {
			
			logger.warn("Input param is null  ..",e);
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
    		value = "/v1/float/pull")
	@ResponseBody
	public Map<String,Object> pullFloat(@RequestBody Map<String,Object> jsonRequest,
			@RequestHeader Map<String, String> headers){
		
		/*
		 * This is used for one shot Pull Float Transactions
		 */
		
		Map<String,Object> response = new HashMap<>();
		
		try {
			
			logger.info("Starting pullFloat process ..");
			logger.info("Checking input params ..");

			TcsParamsValidationHelper.checkRequiredParams(env, "tcs.required.params.payment.business.pull.float", jsonRequest, logger);
			
			if((jsonRequest.get("check")==null || 
					jsonRequest.get("check").toString().isEmpty() || 
					jsonRequest.get("check").toString().equalsIgnoreCase("false"))) {
				
				logger.info("Calling in CheckOnly mode=false  ..");
				
				if(jsonRequest.get("password") == null || jsonRequest.get("password").toString().isEmpty()) {
					
					logger.warn("Password is mandatory  ..");
					logger.warn("Prepare PASSWORD_REQUIRED response  ..");
					
					response.put("Result", String.format("%d", -200));
					response.put("Message", "PASSWORD_REQUIRED");
					
					return response;
				}
				else {
					
					logger.info("Extracting username  ..");
					
					jsonRequest.put("username",tcsApiAuthService.extractUserName(headers));
					jsonRequest.put("check","false");
					
					//11-03-2022
					jsonRequest.put("code","PULLFLOAT");
					
					logger.info(String.format("Extracting Brand for code = %s ..", jsonRequest.get("code")));
					
					jsonRequest.put("brand",getBrandId(jsonRequest));
					
					/////////////////////////////////////////////////////////////////////////////////////////
					
					logger.info("Calling TCS payment function  ..");
					
					Map<String,String> result = tcsApiPaymentService.tcsPullFloat(jsonRequest,headers);
					
					ObjectMapper objectMapper = new ObjectMapper();
					
					String json = objectMapper.writeValueAsString(result);
					
					logger.info(String.format("Out Result:%s", json));
					
					logger.info("Return TCS response  ..");
					
					return new HashMap<>(result);
				}
				
			}
			else {
				
				logger.info("Extracting username and password  ..");
				
				jsonRequest.put("username",tcsApiAuthService.extractUserName(headers));
				jsonRequest.put("password",tcsApiAuthService.extractUserPassword(headers));
				
				//11-03-2022
				jsonRequest.put("code","PULLFLOAT");
				
				logger.info(String.format("Extracting Brand for code = %s ..", jsonRequest.get("code")));
				
				jsonRequest.put("brand",getBrandId(jsonRequest));
				
				/////////////////////////////////////////////////////////////////////////////////////////
				
				logger.info("Calling TCS payment function  ..");
				
				Map<String,String> result = tcsApiPaymentService.tcsPullFloatCheck(jsonRequest,headers);
				
				ObjectMapper objectMapper = new ObjectMapper();
				
				String json = objectMapper.writeValueAsString(result);
				
				logger.info(String.format("Out Result:%s", json));
				
				logger.info("Return TCS response  ..");
				
				return new HashMap<>(result);
			}
			
			
			
		}
		catch(BrandNotFound e) {
			
			response.put("Result", String.format("%d", -200));
			response.put("Message", "BRAND_NOT_FOUND");
			
			return response;
			
		}
		catch(NullOrEmptyInputParameters e) {
			
			logger.warn("Input param is null  ..",e);
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
    		value = "/v1/payment/customer/redeem")
	@ResponseBody
	public Map<String,Object> customerRedeem(@RequestBody Map<String,Object> jsonRequest,
			@RequestHeader Map<String, String> headers){
		
		/*
		 * This is used for one shot Redeem Voucher Transactions
		 */
		
		Map<String,Object> response = new HashMap<>();
		
		try {
			
			logger.info("Starting customerRedeem process ..");
			logger.info("Checking input params ..");

			TcsParamsValidationHelper.checkRequiredParams(env,"tcs.required.params.payment.redeem.voucher", jsonRequest, logger);
					
			if((jsonRequest.get("check")==null || 
					jsonRequest.get("check").toString().isEmpty() || 
					jsonRequest.get("check").toString().equalsIgnoreCase("false"))) {
				
				logger.info("Calling in CheckOnly mode=false  ..");
				
				if(jsonRequest.get("password") == null || jsonRequest.get("password").toString().isEmpty()) {
					
					logger.warn("Password is mandatory  ..");
					logger.warn("Prepare PASSWORD_REQUIRED response  ..");
					
					response.put("Result", String.format("%d", -200));
					response.put("Message", "PASSWORD_REQUIRED");
					
					return response;
				}
				else {
					
					logger.info("Extracting username  ..");
					
					jsonRequest.put("username",tcsApiAuthService.extractUserName(headers));
					jsonRequest.put("check","false");
					
					//11-03-2022
					jsonRequest.put("code","tcs.payment.customer.redeem");
					
					logger.info(String.format("Extracting Brand for code = %s ..", jsonRequest.get("code")));
					
					jsonRequest.put("brand",getBrandId(jsonRequest));
					
					/////////////////////////////////////////////////////////////////////////////////////////
					
					logger.info("Calling TCS payment function  ..");
					
					Map<String,String> result = tcsApiPaymentService.tcsRedeemPayment(jsonRequest,headers);
					
					ObjectMapper objectMapper = new ObjectMapper();
					
					String json = objectMapper.writeValueAsString(result);
					
					logger.info(String.format("Out Result:%s", json));
					
					logger.info("Return TCS response  ..");
					
					return new HashMap<>(result);
				}
				
			}
			else {
				
				jsonRequest.put("username",tcsApiAuthService.extractUserName(headers));
				jsonRequest.put("password",tcsApiAuthService.extractUserPassword(headers));
				
				//11-03-2022
				jsonRequest.put("code","tcs.payment.customer.redeem");
				
				logger.info(String.format("Extracting Brand for code = %s ..", jsonRequest.get("code")));
				
				jsonRequest.put("brand",getBrandId(jsonRequest));
				
				/////////////////////////////////////////////////////////////////////////////////////////
				
				logger.info("Calling TCS payment function  ..");
				
				Map<String,String> result = tcsApiPaymentService.tcsRedeemCheckPayment(jsonRequest,headers);
				
				ObjectMapper objectMapper = new ObjectMapper();
				
				String json = objectMapper.writeValueAsString(result);
				
				logger.info(String.format("Out Result:%s", json));
				
				logger.info("Return TCS response  ..");
				
				return new HashMap<>(result);
			}
			
			
			
		}
		catch(BrandNotFound e) {
			
			response.put("Result", String.format("%d", -200));
			response.put("Message", "BRAND_NOT_FOUND");
			
			return response;
			
		}
		catch(NullOrEmptyInputParameters e) {
			
			logger.warn("Input param is null  ..",e);
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
    		value = "/v1/payment/voucher/send")
	@ResponseBody
	public Map<String,Object> businessSendVoucher(@RequestBody Map<String,Object> jsonRequest,
			@RequestHeader Map<String, String> headers){
		
		/*
		 * This is used for one shot Send Voucher Transactions
		 */
		
		Map<String,Object> response = new HashMap<>();
		
		try {
			
			logger.info("Starting businessSendVoucher process ..");
			logger.info("Checking input params ..");

			TcsParamsValidationHelper.checkRequiredParams(env,"tcs.required.params.payment.send.voucher", jsonRequest, logger);
					
			if((jsonRequest.get("check")==null || 
					jsonRequest.get("check").toString().isEmpty() || 
					jsonRequest.get("check").toString().equalsIgnoreCase("false"))) {
				
				logger.info("Calling in CheckOnly mode=false  ..");
				
				if(jsonRequest.get("password") == null || jsonRequest.get("password").toString().isEmpty()) {
					
					logger.warn("Password is mandatory  ..");
					logger.warn("Prepare PASSWORD_REQUIRED response  ..");
					
					response.put("Result", String.format("%d", -200));
					response.put("Message", "PASSWORD_REQUIRED");
					
					return response;
				}
				else {
					
					logger.info("Extracting username  ..");
					
					jsonRequest.put("username",tcsApiAuthService.extractUserName(headers));
					jsonRequest.put("check","false");
					
					//22-03-2022
					if(jsonRequest.get("code")!=null && jsonRequest.get("code").toString().equalsIgnoreCase("m2p")) {
						
						jsonRequest.put("code","tcs.payment.m2p.voucher.send");
					}
					if(jsonRequest.get("code")!=null && jsonRequest.get("code").toString().equalsIgnoreCase("b2p")) {
						
						jsonRequest.put("code","tcs.payment.b2p.voucher.send");
					}
					//11-03-2022
					
					logger.info(String.format("Extracting Brand for code = %s ..", jsonRequest.get("code")));
					
					jsonRequest.put("brand",getBrandId(jsonRequest));
					
					/////////////////////////////////////////////////////////////////////////////////////////
					
					logger.info("Calling TCS payment function  ..");
					
					Map<String,String> result = tcsApiPaymentService.tcsVoucherPayment(jsonRequest,headers);
					
					ObjectMapper objectMapper = new ObjectMapper();
					
					String json = objectMapper.writeValueAsString(result);
					
					logger.info(String.format("Out Result:%s", json));
					
					logger.info("Return TCS response  ..");
					
					return new HashMap<>(result);
				}
				
			}
			else {
				
				jsonRequest.put("username",tcsApiAuthService.extractUserName(headers));
				jsonRequest.put("password",tcsApiAuthService.extractUserPassword(headers));
				
				//22-03-2022
				if(jsonRequest.get("code")!=null && jsonRequest.get("code").toString().equalsIgnoreCase("m2p")) {
					
					jsonRequest.put("code","tcs.payment.m2p.voucher.send");
				}
				if(jsonRequest.get("code")!=null && jsonRequest.get("code").toString().equalsIgnoreCase("b2p")) {
					
					jsonRequest.put("code","tcs.payment.b2p.voucher.send");
				}
				
				//11-03-2022
				
				logger.info(String.format("Extracting Brand for code = %s ..", jsonRequest.get("code")));
				
				jsonRequest.put("brand",getBrandId(jsonRequest));
				
				/////////////////////////////////////////////////////////////////////////////////////////
				
				logger.info("Calling TCS payment function  ..");
				
				Map<String,String> result = tcsApiPaymentService.tcsCheckVoucherPayment(jsonRequest,headers);
				
				ObjectMapper objectMapper = new ObjectMapper();
				
				String json = objectMapper.writeValueAsString(result);
				
				logger.info(String.format("Out Result:%s", json));
				
				logger.info("Return TCS response  ..");
				
				return new HashMap<>(result);
			}
			
			
			
		}
		catch(BrandNotFound e) {
			
			response.put("Result", String.format("%d", -200));
			response.put("Message", "BRAND_NOT_FOUND");
			
			return response;
			
		}
		catch(NullOrEmptyInputParameters e) {
			
			logger.warn("Input param is null  ..",e);
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
    		value = "/v1/payment/bill")
	@ResponseBody
	public Map<String,Object> billPayment(@RequestBody Map<String,Object> jsonRequest,
			@RequestHeader Map<String, String> headers){
		
		/*
		 * This is used for one shot Bill Payment, Topup, Bundles
		 */
		
		Map<String,Object> response = new HashMap<>();
		
		try {

			logger.info("Starting billPayment process ..");
			logger.info("Checking input params ..");

			TcsParamsValidationHelper.checkRequiredParams(env, "tcs.required.params.payment.bill", jsonRequest, logger);
			
			if((jsonRequest.get("check")==null || 
					jsonRequest.get("check").toString().isEmpty() || 
					jsonRequest.get("check").toString().equalsIgnoreCase("false"))) {
				
				logger.info("Calling in CheckOnly mode=false  ..");
				
				if(jsonRequest.get("password") == null || jsonRequest.get("password").toString().isEmpty()) {
					
					logger.warn("Password is mandatory  ..");
					logger.warn("Prepare PASSWORD_REQUIRED response  ..");
					
					response.put("Result", String.format("%d", -200));
					response.put("Message", "PASSWORD_REQUIRED");
					
					return response;
				}
				else {
					
					
					logger.info("Extracting username  ..");
					
					jsonRequest.put("username",tcsApiAuthService.extractUserName(headers));
					jsonRequest.put("check","false");
					
					jsonRequest.put("brand",jsonRequest.get("code").toString());
					
					logger.info("Calling TCS payment function  ..");
					
					Map<String,String> result = tcsApiPaymentService.tcsBillPayment(jsonRequest,headers);
					
					ObjectMapper objectMapper = new ObjectMapper();
					
					String json = objectMapper.writeValueAsString(result);
					
					logger.info(String.format("Out Result:%s", json));
					
					logger.info("Return TCS response  ..");
					
					return new HashMap<>(result);
				}
				
			}
			else {
				
				
				logger.info("Extracting username and password  ..");
				
				jsonRequest.put("username",tcsApiAuthService.extractUserName(headers));
				jsonRequest.put("password",tcsApiAuthService.extractUserPassword(headers));
				
				jsonRequest.put("brand",jsonRequest.get("code").toString());
				
				logger.info("Calling TCS payment function  ..");
				
				Map<String,String> result = tcsApiPaymentService.tcsBillCheckPayment(jsonRequest,headers);
				
				ObjectMapper objectMapper = new ObjectMapper();
				
				String json = objectMapper.writeValueAsString(result);
				
				logger.info(String.format("Out Result:%s", json));
				
				logger.info("Return TCS response  ..");
				
				return new HashMap<>(result);
				
			}
			
			
			
		}
		catch(NullOrEmptyInputParameters e) {
			
			logger.warn("Input param is null  ..",e);
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
    		value = "/v1/payment/w2ba")
	@ResponseBody
	public Map<String,Object> wallet2BankPayment(@RequestBody Map<String,Object> jsonRequest,
			@RequestHeader Map<String, String> headers){
		
		/*
		 * This is used for one shot Send To Bank Transactions
		 */
		
		Map<String,Object> response = new HashMap<>();
		
		try {
			
			logger.info("Starting wallet2BankPayment process ..");
			logger.info("Checking input params ..");

			TcsParamsValidationHelper.checkRequiredParams(env, "tcs.required.params.payment.w2ba", jsonRequest, logger);
			
			if((jsonRequest.get("check")==null || 
					jsonRequest.get("check").toString().isEmpty() || 
					jsonRequest.get("check").toString().equalsIgnoreCase("false"))) {
				
				logger.info("Calling in CheckOnly mode=false  ..");
				
				if(jsonRequest.get("password") == null || jsonRequest.get("password").toString().isEmpty()) {
					
					logger.warn("Password is mandatory  ..");
					logger.warn("Prepare PASSWORD_REQUIRED response  ..");
					
					response.put("Result", String.format("%d", -200));
					response.put("Message", "PASSWORD_REQUIRED");
					
					return response;
				}
				else {
					
					logger.info("Extracting username  ..");
					
					jsonRequest.put("username",tcsApiAuthService.extractUserName(headers));
					jsonRequest.put("check","false");
					
					jsonRequest.put("brand",jsonRequest.get("brand").toString());
					
					logger.info("Calling TCS payment function  ..");
					
					Map<String,String> result = tcsApiPaymentService.tcsW2BAPayment(jsonRequest,headers);
					
					ObjectMapper objectMapper = new ObjectMapper();
					
					String json = objectMapper.writeValueAsString(result);
					
					logger.info(String.format("Out Result:%s", json));
					
					logger.info("Return TCS response  ..");
					
					return new HashMap<>(result);
				}
				
			}
			else {
				
				logger.info("Extracting username and password  ..");
				
				jsonRequest.put("username",tcsApiAuthService.extractUserName(headers));
				jsonRequest.put("password",tcsApiAuthService.extractUserPassword(headers));
				
				jsonRequest.put("brand",jsonRequest.get("brand").toString());
				
				logger.info("Calling TCS payment function  ..");
				
				Map<String,String> result = tcsApiPaymentService.tcsW2BACheckPayment(jsonRequest,headers);
				
				ObjectMapper objectMapper = new ObjectMapper();
				
				String json = objectMapper.writeValueAsString(result);
				
				logger.info(String.format("Out Result:%s", json));
				
				logger.info("Return TCS response  ..");
				
				return new HashMap<>(result);
			}
			
			
			
		}
		catch(NullOrEmptyInputParameters e) {
			
			logger.warn("Input param is null  ..",e);
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
    		value = "/v1/payment/business/b2all")
	@ResponseBody
	public Map<String,Object> businessB2All(@RequestBody Map<String,Object> jsonRequest,
			@RequestHeader Map<String, String> headers){
		
		/*
		 * This is used for one shot Business to Business,Person,Merchant Transactions
		 *
		 * Valid values for request parameter (code) = B2P, B2B & B2M
		 * 
		 * If not provided correctly will use B2P by default
		 * 
		 * * NOTE:
		 * 
		 * We need to differentiate between OnNet & OffNet Subscribers in case of B2P
		 * 
		 * 
		 */
		
		
		
		Map<String,Object> response = new HashMap<>();
		
		try {
			
			logger.info("Starting businessB2All process ..");
			logger.info("Checking input params ..");

			TcsParamsValidationHelper.checkRequiredParams(env, "tcs.required.params.payment.b2all", jsonRequest, logger);
			
			if((jsonRequest.get("check")==null || 
					jsonRequest.get("check").toString().isEmpty() || 
					jsonRequest.get("check").toString().equalsIgnoreCase("false"))) {
				
				logger.info("Calling in CheckOnly mode=false  ..");
				
				if(jsonRequest.get("password") == null || jsonRequest.get("password").toString().isEmpty()) {
					
					logger.warn("Password is mandatory  ..");
					logger.warn("Prepare PASSWORD_REQUIRED response  ..");
					
					response.put("Result", String.format("%d", -200));
					response.put("Message", "PASSWORD_REQUIRED");
					
					return response;
				}
				else {
					
					logger.info("Extracting username  ..");
					
					jsonRequest.put("username",tcsApiAuthService.extractUserName(headers));
					jsonRequest.put("check","false");
					
					//11-03-2022
					
					logger.info(String.format("Extracting Brand for code = %s ..", jsonRequest.get("code")));
					
					jsonRequest.put("brand",getBrandId(jsonRequest));
					
					/////////////////////////////////////////////////////////////////////////////////////////
					
					logger.info("Calling TCS payment function  ..");
					
					Map<String,String> result = tcsApiPaymentService.tcsB2AllPayment(jsonRequest,headers);
					
					ObjectMapper objectMapper = new ObjectMapper();
					
					String json = objectMapper.writeValueAsString(result);
					
					logger.info(String.format("Out Result:%s", json));
					
					logger.info("Return TCS response  ..");
					
					return new HashMap<>(result);
				}
				
			}
			else {
				
				logger.info("Extracting username and password  ..");
				
				jsonRequest.put("username",tcsApiAuthService.extractUserName(headers));
				jsonRequest.put("password",tcsApiAuthService.extractUserPassword(headers));
				
				//11-03-2022
				
				logger.info(String.format("Extracting Brand for code = %s ..", jsonRequest.get("code")));
				
				jsonRequest.put("brand",getBrandId(jsonRequest));
				
				/////////////////////////////////////////////////////////////////////////////////////////
				
				logger.info("Calling TCS payment function  ..");
				
				Map<String,String> result = tcsApiPaymentService.tcsB2AllCheckPayment(jsonRequest,headers);
				
				ObjectMapper objectMapper = new ObjectMapper();
				
				String json = objectMapper.writeValueAsString(result);
				
				logger.info(String.format("Out Result:%s", json));
				
				logger.info("Return TCS response  ..");
				
				return new HashMap<>(result);
			}
			
			
			
		}
		catch(BrandNotFound e) {
			
			response.put("Result", String.format("%d", -200));
			response.put("Message", "BRAND_NOT_FOUND");
			
			return response;
			
		}
		catch(NullOrEmptyInputParameters e) {
			
			logger.warn("Input param is null  ..",e);
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
    		value = "/v1/payment/merchant/m2all")
	@ResponseBody
	public Map<String,Object> merchantM2All(@RequestBody Map<String,Object> jsonRequest,
			@RequestHeader Map<String, String> headers){
		
		/*
		 * This is used for one shot Merchant to Business,Person,Merchant Transactions
		 *
		 *
		 * Valid values for request parameter (code) = M2P, M2B & M2M
		 * 
		 * If not provided correctly will use M2P by default
		 * 
		 * NOTE:
		 * 
		 * We need to differentiate between OnNet & OffNet Subscribers in case of M2P
		 * 
		 * 
		 */
		
		
		
		Map<String,Object> response = new HashMap<>();
		
		try {
			
			logger.info("Starting merchantM2All process ..");
			logger.info("Checking input params ..");

			TcsParamsValidationHelper.checkRequiredParams(env, "tcs.required.params.payment.b2all", jsonRequest, logger);
			
			if((jsonRequest.get("check")==null || 
					jsonRequest.get("check").toString().isEmpty() || 
					jsonRequest.get("check").toString().equalsIgnoreCase("false"))) {
				
				logger.info("Calling in CheckOnly mode=false  ..");
				
				if(jsonRequest.get("password") == null || jsonRequest.get("password").toString().isEmpty()) {
					
					logger.warn("Password is mandatory  ..");
					logger.warn("Prepare PASSWORD_REQUIRED response  ..");
					
					response.put("Result", String.format("%d", -200));
					response.put("Message", "PASSWORD_REQUIRED");
					
					return response;
				}
				else {
					
					logger.info("Extracting username  ..");
					
					jsonRequest.put("username",tcsApiAuthService.extractUserName(headers));
					jsonRequest.put("check","false");
					
					//11-03-2022
					
					logger.info(String.format("Extracting Brand for code = %s ..", jsonRequest.get("code")));
					
					jsonRequest.put("brand",getBrandId(jsonRequest));
					
					/////////////////////////////////////////////////////////////////////////////////////////
					
					logger.info("Calling TCS payment function  ..");
					
					Map<String,String> result = tcsApiPaymentService.tcsB2AllPayment(jsonRequest,headers);
					
					ObjectMapper objectMapper = new ObjectMapper();
					
					String json = objectMapper.writeValueAsString(result);
					
					logger.info(String.format("Out Result:%s", json));
					
					logger.info("Return TCS response  ..");
					
					return new HashMap<>(result);
				}
				
			}
			else {
				
				logger.info("Extracting username and password  ..");
				
				jsonRequest.put("username",tcsApiAuthService.extractUserName(headers));
				jsonRequest.put("password",tcsApiAuthService.extractUserPassword(headers));
				
				//11-03-2022
				
				logger.info(String.format("Extracting Brand for code = %s ..", jsonRequest.get("code")));
				
				jsonRequest.put("brand",getBrandId(jsonRequest));
				
				/////////////////////////////////////////////////////////////////////////////////////////
				
				logger.info("Calling TCS payment function  ..");
				
				Map<String,String> result = tcsApiPaymentService.tcsB2AllCheckPayment(jsonRequest,headers);
				
				ObjectMapper objectMapper = new ObjectMapper();
				
				String json = objectMapper.writeValueAsString(result);
				
				logger.info(String.format("Out Result:%s", json));
				
				logger.info("Return TCS response  ..");
				
				return new HashMap<>(result);
			}
			
			
			
		}
		catch(BrandNotFound e) {
			
			response.put("Result", String.format("%d", -200));
			response.put("Message", "BRAND_NOT_FOUND");
			
			return response;
			
		}
		catch(NullOrEmptyInputParameters e) {
			
			logger.warn("Input param is null  ..",e);
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
    		value = "/v1/payment/merchant/refund")
	@ResponseBody
	public Map<String,Object> merchantRefund(@RequestBody Map<String,Object> jsonRequest,
			@RequestHeader Map<String, String> headers){
		
		/*
		 * This is used for one shot Merchant Refund Transactions
		 *
		 */
		
		Map<String,Object> response = new HashMap<>();
		
		try {

			logger.info("Starting merchantRefund process ..");
			logger.info("Checking input params ..");

			TcsParamsValidationHelper.checkRequiredParams(env,"tcs.required.params.payment.refund", jsonRequest, logger);
						
			
			if((jsonRequest.get("check")==null || 
					jsonRequest.get("check").toString().isEmpty() || 
					jsonRequest.get("check").toString().equalsIgnoreCase("false"))) {
				
				logger.info("Calling in CheckOnly mode=false  ..");
				
				if(jsonRequest.get("password") == null || jsonRequest.get("password").toString().isEmpty()) {
					
					logger.warn("Password is mandatory  ..");
					logger.warn("Prepare PASSWORD_REQUIRED response  ..");
					
					response.put("Result", String.format("%d", -200));
					response.put("Message", "PASSWORD_REQUIRED");
					
					return response;
				}
				else {
					
					logger.info("Extracting username  ..");
					
					jsonRequest.put("username",tcsApiAuthService.extractUserName(headers));
					jsonRequest.put("check","false");
					
					logger.info("Calling TCS refund function  ..");
					
					Map<String,String> result = tcsApiPaymentService.tcsMerchantRefund(jsonRequest,headers);
					
					ObjectMapper objectMapper = new ObjectMapper();
					
					String json = objectMapper.writeValueAsString(result);
					
					logger.info(String.format("Out Result:%s", json));
					
					logger.info("Return TCS response  ..");
					
					return new HashMap<>(result);
				}
				
			}
			else {
				
				response.put("Result", String.format("%d", -100));
				response.put("Message", "TRX_HAS_NO_CHECK_MODE");
				
				return response;
			}
			
			
			
		}
		catch(NullOrEmptyInputParameters e) {
			
			logger.warn("Input param is null  ..",e);
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
    		value = "/v1/payment/money/deletion")
	@ResponseBody
	public Map<String,Object> deleteMoney(@RequestBody Map<String,Object> jsonRequest,
			@RequestHeader Map<String, String> headers){
		
		/*
		 * Only used for money deletion Sales Requests
		 * destination=967710001 (fixed)
		 */
		
		Map<String,Object> response = new HashMap<>();
		
		try {
			
			logger.info("Starting salesRequest process ..");
			logger.info("Checking input params ..");

			TcsParamsValidationHelper.checkRequiredParams(env, "tcs.required.params.money.deletion", jsonRequest, logger);
			
			if((jsonRequest.get("check")==null || 
					jsonRequest.get("check").toString().isEmpty() || 
					jsonRequest.get("check").toString().equalsIgnoreCase("false"))) {
				
				logger.info("Calling in CheckOnly mode=false  ..");
				
				if(jsonRequest.get("password") == null || jsonRequest.get("password").toString().isEmpty()) {
					
					logger.warn("Password is mandatory  ..");
					logger.warn("Prepare PASSWORD_REQUIRED response  ..");
					
					response.put("Result", String.format("%d", -200));
					response.put("Message", "PASSWORD_REQUIRED");
					
					return response;
				}
				else {
					
					logger.info("Extracting username  ..");
					
					jsonRequest.put("username",tcsApiAuthService.extractUserName(headers));
					jsonRequest.put("source_id",tcsApiAuthService.extractUserAccountId(headers));
					jsonRequest.put("source",tcsApiAuthService.extractUserAccountNumber(headers));
					jsonRequest.put("destination","967710001");
					jsonRequest.put("code","MONEYUNALLOC");
					jsonRequest.put("check","false");
					
					//11-03-2022
					
					logger.info(String.format("Extracting Brand for code = %s ..", jsonRequest.get("code")));
					
					jsonRequest.put("brand",getBrandId(jsonRequest));
					
					/////////////////////////////////////////////////////////////////////////////////////////
					
					logger.info("Calling TCS sales request for money deletion function  ..");
					
					Map<String,String> result = tcsApiPaymentService.tcsSalesRequest(jsonRequest,headers,false);
					
					logger.info(String.format("tcsSalesRequest result=%s", result.get("Result")));
					logger.info(String.format("tcsSalesRequest message=%s", result.get("Message")));
					
					if(result.get("Result").toString().contentEquals("0")) {
						
						logger.info("Create first checker record for approval flow ..");
						
						try {
							
							logger.info("Create first checker record for approval flow ..");
							
							result.put("account_id", jsonRequest.get("source_id").toString());
							
							Optional<Map<String,Object>> chk = makerCheckerService.createFirstChecker(result);
							
							if(chk.isPresent()) {
								
								logger.info(String.format("Created checker lines=%d ..", chk.get().get("created_rows")));
								
							}else {
								
								logger.error("createFirstChecker unknown error ..");
							}
						}
						catch(Exception e) {
							
							e.printStackTrace();
							logger.error(e.getMessage());
						}
					}
					else {
						
						logger.warn("tcsSalesRequest has failed, no checker line is created ..");
					}
					
					ObjectMapper objectMapper = new ObjectMapper();
					
					String json = objectMapper.writeValueAsString(result);
					
					logger.info(String.format("Out Result:%s", json));
					
					logger.info("Return TCS response  ..");
					
					return new HashMap<>(result);
				}
				
			}
			else {
				
				logger.info("Extracting username and password  ..");
				
				jsonRequest.put("username",tcsApiAuthService.extractUserName(headers));
				jsonRequest.put("password",tcsApiAuthService.extractUserPassword(headers));
				jsonRequest.put("source_id",tcsApiAuthService.extractUserAccountId(headers));
				jsonRequest.put("source",tcsApiAuthService.extractUserAccountNumber(headers));
				jsonRequest.put("destination","967710001");
				jsonRequest.put("code","MONEYUNALLOC");
				
				//11-03-2022
				
				logger.info(String.format("Extracting Brand for code = %s ..", jsonRequest.get("code")));
				
				jsonRequest.put("brand",getBrandId(jsonRequest));
				
				/////////////////////////////////////////////////////////////////////////////////////////
				
				logger.info("Calling TCS sales request for money deletion function  ..");
				
				Map<String,String> result = tcsApiPaymentService.tcsCheckSalesRequest(jsonRequest,headers);
				
				ObjectMapper objectMapper = new ObjectMapper();
				
				String json = objectMapper.writeValueAsString(result);
				
				logger.info(String.format("Out Result:%s", json));
				
				logger.info("Return TCS response  ..");
				
				return new HashMap<>(result);
			}
			
			
			
		}
		catch(BrandNotFound e) {
			
			response.put("Result", String.format("%d", -200));
			response.put("Message", "BRAND_NOT_FOUND");
			
			return response;
			
		}
		catch(NullOrEmptyInputParameters e) {
			
			logger.warn("Input param is null  ..",e);
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
    		value = "/v1/payment/sales/request")
	@ResponseBody
	public Map<String,Object> salesRequest(@RequestBody Map<String,Object> jsonRequest,
			@RequestHeader Map<String, String> headers){
		
		/*
		 * This is used for all types of Transactions to support Maker/Checker
		 * 
		 * This includes:
		 * 
		 * 
		 * 
		 * - Sell, Push & Pull Float
		 * 
		 * 24082022 Need to exclude BZCASHOUT service code
		 *
		 */
		/*
		 * 
		 */
		
		Map<String,Object> response = new HashMap<>();
		
		try {
			
			logger.info("Starting salesRequest process ..");
			logger.info("Checking input params ..");

			TcsParamsValidationHelper.checkRequiredParams(env, "tcs.required.params.sales.request", jsonRequest, logger);
			
			if((jsonRequest.get("check")==null || 
					jsonRequest.get("check").toString().isEmpty() || 
					jsonRequest.get("check").toString().equalsIgnoreCase("false"))) {
				
				logger.info("Calling in CheckOnly mode=false  ..");
				
				if(jsonRequest.get("password") == null || jsonRequest.get("password").toString().isEmpty()) {
					
					logger.warn("Password is mandatory  ..");
					logger.warn("Prepare PASSWORD_REQUIRED response  ..");
					
					response.put("Result", String.format("%d", -200));
					response.put("Message", "PASSWORD_REQUIRED");
					
					return response;
				}
				else {
					
					logger.info("Extracting username  ..");
					
					jsonRequest.put("username",tcsApiAuthService.extractUserName(headers));
					jsonRequest.put("source_id",tcsApiAuthService.extractUserAccountId(headers));
					jsonRequest.put("check","false");
					
					/*
					 * 30082022
					 * 
					 * Added this part for M2P to check first if destination is ONNET then keep brand as M2P
					 * Else
					 * If destination is OFFNET then use tcs.payment.b2p.voucher.send.[CURRENCY]
					 */
					
					String svc_code = (String) jsonRequest.get("code");
					
					if(svc_code.contentEquals("M2P") || 
							svc_code.contentEquals("B2P")) {
					
						Map<String,Object> tmp = new HashMap<>(jsonRequest);
						
						String account = (String) jsonRequest.get("destination");
						String replace_code = String.format("tcs.payment.b2p.voucher.send");
						tmp.put("account", account);
						Map<String,String> dest_info = tcsApiAccountService.tcsGetAccountInfoIso(tmp, headers);
						
						if(!dest_info.get("Result").contentEquals("0")) {
							
							logger.info("Account Destination is OFFNET");
							jsonRequest.put("code", replace_code);
						}
					}
					
					
					//End of 30082022
					
					//11-03-2022
					
					logger.info(String.format("Extracting Brand for code = %s ..", jsonRequest.get("code")));
					
					jsonRequest.put("brand",getBrandId(jsonRequest));
					
					/////////////////////////////////////////////////////////////////////////////////////////
					
					logger.info("Calling TCS sales request function  ..");
					
					Map<String,String> result = tcsApiPaymentService.tcsSalesRequest(jsonRequest,headers,false);
					
					logger.info(String.format("tcsSalesRequest result=%s", result.get("Result")));
					logger.info(String.format("tcsSalesRequest message=%s", result.get("Message")));
					
					if(result.get("Result").toString().contentEquals("0")) {
						
						logger.info("Create first checker record for approval flow ..");
						
						try {
							
							logger.info("Create first checker record for approval flow ..");
							
							result.put("account_id", jsonRequest.get("source_id").toString());
							
							Optional<Map<String,Object>> chk = makerCheckerService.createFirstChecker(result);
							
							if(chk.isPresent()) {
								
								logger.info(String.format("Created checker lines=%d ..", chk.get().get("created_rows")));
								
							}else {
								
								logger.error("createFirstChecker unknown error ..");
							}
						}
						catch(Exception e) {
							
							e.printStackTrace();
							logger.error(e.getMessage());
						}
					}
					else {
						
						logger.warn("tcsSalesRequest has failed, no checker line is created ..");
					}
					
					
					
					ObjectMapper objectMapper = new ObjectMapper();
					
					String json = objectMapper.writeValueAsString(result);
					
					logger.info(String.format("Out Result:%s", json));
					
					logger.info("Return TCS response  ..");
					
					return new HashMap<>(result);
				}
				
			}
			else {
				
				logger.info("Extracting username and password  ..");
				
				jsonRequest.put("username",tcsApiAuthService.extractUserName(headers));
				jsonRequest.put("password",tcsApiAuthService.extractUserPassword(headers));
				jsonRequest.put("source_id",tcsApiAuthService.extractUserAccountId(headers));
				
				/*
				 * 30082022
				 * 
				 * Added this part for M2P to check first if destination is ONNET then keep brand as M2P
				 * Else
				 * If destination is OFFNET then use tcs.payment.b2p.voucher.send.[CURRENCY]
				 */
				
				String svc_code = (String) jsonRequest.get("code");
				
				if(svc_code.contentEquals("M2P")) {
				
					Map<String,Object> tmp = new HashMap<>(jsonRequest);
					
					String account = (String) jsonRequest.get("destination");
					String replace_code = String.format("tcs.payment.b2p.voucher.send");
					tmp.put("account", account);
					Map<String,String> dest_info = tcsApiAccountService.tcsGetAccountInfoIso(tmp, headers);
					
					if(!dest_info.get("Result").contentEquals("0")) {
						
						logger.info("Account Destination is OFFNET");
						jsonRequest.put("code", replace_code);
					}
				}
				
				
				//End of 30082022
				
				//11-03-2022
				
				logger.info(String.format("Extracting Brand for code = %s ..", jsonRequest.get("code")));
				
				jsonRequest.put("brand",getBrandId(jsonRequest));
				
				/////////////////////////////////////////////////////////////////////////////////////////
				
				logger.info("Calling TCS sales request function  ..");
				
				Map<String,String> result = tcsApiPaymentService.tcsCheckSalesRequest(jsonRequest,headers);
				
				ObjectMapper objectMapper = new ObjectMapper();
				
				String json = objectMapper.writeValueAsString(result);
				
				logger.info(String.format("Out Result:%s", json));
				
				logger.info("Return TCS response  ..");
				
				return new HashMap<>(result);
			}
			
			
			
		}
		catch(BrandNotFound e) {
			
			response.put("Result", String.format("%d", -200));
			response.put("Message", "BRAND_NOT_FOUND");
			
			return response;
			
		}
		catch(NullOrEmptyInputParameters e) {
			
			logger.warn("Input param is null  ..",e);
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
    		value = "/v1/bulk/sales/request")
	@ResponseBody
	public Map<String,Object> bulkSalesRequest(@RequestBody Map<String,Object> jsonRequest,
			@RequestHeader Map<String, String> headers){
		
		/*
		 * This is used for all types of Transactions to support BULK Maker/Checker
		 * 
		 * Added 10042022
		 * Updated 18042022, to add bulk_ref
		 */
		
		Map<String,Object> response = new HashMap<>();
		
		try {
			
			logger.info("Starting bulkSalesRequest process ..");
			logger.info("Checking input params ..");

			TcsParamsValidationHelper.checkRequiredParams(env, "tcs.required.params.bulk.sales.request", jsonRequest, logger);
			
			//28092022
			String remarks = String.format("%s", jsonRequest.get("remarks"));
			
			if(remarks.length()>200)
				remarks=remarks.substring(0, 200);
			
			jsonRequest.put("remarks", remarks);
			
			String source = String.format("%s", jsonRequest.get("source"));
			String destination = String.format("%s", jsonRequest.get("destination"));
			
			if(source.length()>6 && source.length()<=9)
				source=String.format("967%s", source);
			
			jsonRequest.put("source", source);
			
			if(destination.length()>6 && destination.length()<=9)
				destination=String.format("967%s", destination);
			
			jsonRequest.put("destination", destination);
			
			/////////////////////////////////////////////////////////////////////
			
			
			if((jsonRequest.get("check")==null || 
					jsonRequest.get("check").toString().isEmpty() || 
					jsonRequest.get("check").toString().equalsIgnoreCase("false"))) {
				
				logger.info("Calling in CheckOnly mode=false  ..");
				
				if(jsonRequest.get("password") == null || jsonRequest.get("password").toString().isEmpty()) {
					
					logger.warn("Password is mandatory  ..");
					logger.warn("Prepare PASSWORD_REQUIRED response  ..");
					
					response.put("Result", String.format("%d", -200));
					response.put("Message", "PASSWORD_REQUIRED");
					
					return response;
				}
				else {
					
					logger.info("Extracting username  ..");
					
					jsonRequest.put("username",tcsApiAuthService.extractUserName(headers));
					jsonRequest.put("source_id",tcsApiAuthService.extractUserAccountId(headers));
					jsonRequest.put("check","false");
					
					/*
					 * 01112022
					 * 
					 * Check for Bulk B2P if destination is Subscriber or OffNet
					 * 
					 * It was added on Sales Request on Aug and not added on bulk!
					 */
					String svc_code = (String) jsonRequest.get("code");
					
					if(svc_code.contentEquals("M2P") ||
							svc_code.contentEquals("tcs.payment.m2p.send") ||
							svc_code.contentEquals("tcs.payment.b2p.send") ||
							svc_code.contentEquals("B2P")) {
					
						Map<String,Object> tmp = new HashMap<>(jsonRequest);
						
						String account = (String) jsonRequest.get("destination");
						String replace_code = String.format("tcs.payment.b2p.voucher.send");
						tmp.put("account", account);
						Map<String,String> dest_info = tcsApiAccountService.tcsGetAccountInfoIso(tmp, headers);
						
						if(!dest_info.get("Result").contentEquals("0")) {
							
							logger.info("Account Destination is OFFNET");
							jsonRequest.put("code", replace_code);
						}
					}
					/*
					 * End of 01112022
					 */
					
					logger.info(String.format("Extracting Brand for code = %s ..", jsonRequest.get("code")));
					
					jsonRequest.put("brand",getBrandId(jsonRequest));
					
					/////////////////////////////////////////////////////////////////////////////////////////
					
					logger.info("Calling TCS sales request function  ..");
					
					Map<String,String> result = tcsApiPaymentService.tcsSalesRequest(jsonRequest,headers,true);
					
					ObjectMapper objectMapper = new ObjectMapper();
					
					String json = objectMapper.writeValueAsString(result);
					
					logger.info(String.format("Out Result:%s", json));
					
					logger.info("Return TCS response  ..");
					
					return new HashMap<>(result);
				}
				
			}
			else {
				
				logger.warn("Check mode is not supported  ..");
				
				response.put("Result", String.format("%d", -100));
				response.put("Message", "TRX_HAS_NO_CHECK_MODE");
				
				return response;
			}
			
			
			
		}
		catch(BrandNotFound e) {
			
			response.put("Result", String.format("%d", -200));
			response.put("Message", "BRAND_NOT_FOUND");
			
			return response;
			
		}
		catch(NullOrEmptyInputParameters e) {
			
			logger.warn("Input param is null  ..",e);
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
    		value = "/v1/payment/sales/check")
	@ResponseBody
	public Map<String,Object> checkSalesRequest(@RequestBody Map<String,Object> jsonRequest,
			@RequestHeader Map<String, String> headers){
		
		/*
		 * This end point to get Sales Request status from TCS 
		 * and to get details of the authorized person in case recorded during sales 
		 * request creation for the case of (Business Cash Out)
		 */
		
		
		
		Map<String,Object> response = new HashMap<>();
		
		try {
			
			//16052022
			//Added logic to prevent check of cashout if the user is not source nor destination
			Optional<List<Map<String,String>>> salesRequest = makerCheckerService.findSalesRequest(jsonRequest.get("request_id").toString(),
					tcsApiAuthService.extractUserName(headers));
			
			if(salesRequest.isPresent()) {
				
				/*
				 * Note: salesRequest.get() could return a 0 size list
				 * 
				 * As the user will not enter the sales request number manually, this case is not likely to happen
				 */
				List<Map<String,String>> r = salesRequest.get();
				
				
				String dest_msisdn = r.get(0).get("Destination_Account");
				String source_msisdn = r.get(0).get("Source_Account");
				String service = r.get(0).get("Service");
				
				logger.info(String.format("Sales Request Destination MSISDN %s", dest_msisdn));
				logger.info(String.format("Sales Request Source MSISDN %s", source_msisdn));
				logger.info(String.format("Sales Request Service Code %s", service));
				
				String user_msisdn=tcsApiAuthService.extractUserAccountNumber(headers);
				
				/*
				 * Wrong conditions:
				 * 1- If user is not the source && not the destination && Service = BZCASHOUT
				 * 
				 */
				
				if(service.contentEquals("BZCASHOUT")) {
					
					if(!source_msisdn.contentEquals(user_msisdn)) {
						
						if(!dest_msisdn.contentEquals(user_msisdn)) {
							
							throw new UserHasNoPrivileges("WRONG_DEST_ACCOUNT");
						}
					}
				}
					
			}
			
			/////////////////////////////////////////////////////////////////////////////////////////////////////
			
			
			logger.info("Starting checkSalesRequest process ..");
			logger.info("Checking input params ..");

			if(jsonRequest.get("request_id") == null || jsonRequest.get("request_id").toString().isEmpty()) {
				
				logger.error(String.format("Validation error: Mandatory params, request_id=%s", jsonRequest.get("request_id")));
				throw new NullOrEmptyInputParameters("NULL_OR_EMPTY_INPUT");
			}
			
			
			if((jsonRequest.get("check")==null || 
					jsonRequest.get("check").toString().isEmpty() || 
					jsonRequest.get("check").toString().equalsIgnoreCase("false"))) {
				
				logger.info("Calling in CheckOnly mode=false  ..");
				
				if(jsonRequest.get("password") == null || jsonRequest.get("password").toString().isEmpty()) {
					
					logger.warn("Password is mandatory  ..");
					logger.warn("Prepare PASSWORD_REQUIRED response  ..");
					
					response.put("Result", String.format("%d", -200));
					response.put("Message", "PASSWORD_REQUIRED");
					
					return response;
				}
				else {
					
					logger.info("Extracting username  ..");
					
					jsonRequest.put("username",tcsApiAuthService.extractUserName(headers));
					jsonRequest.put("check","false");
					
					logger.info("Calling TCS sales request check function  ..");
					
					Map<String,String> result = tcsApiPaymentService.tcsSalesRequestCheck(jsonRequest,headers);
					
					if(result.get("TCS_Status").equalsIgnoreCase("RQS")) {
						
						//Get authorized person details from internal database
						
						Optional<List<Map<String,String>>> authorizedPerson = makerCheckerService.findSalesRequest(jsonRequest.get("request_id").toString(),
								tcsApiAuthService.extractUserName(headers));
						
						if(authorizedPerson.isPresent()) {
							
							/*
							 * 
							 */
							List<Map<String,String>> r = authorizedPerson.get();
							
							result.put("Authorized_Person", r.get(0).get("Authorized_Person"));
							result.put("Authorized_ID", r.get(0).get("Authorized_ID"));
							result.put("Authorized_Mobile", r.get(0).get("Authorized_Mobile"));
							//22-03-2022
							result.put("Currency", r.get(0).get("Currency"));
						}
						
					}
					
					ObjectMapper objectMapper = new ObjectMapper();
					
					String json = objectMapper.writeValueAsString(result);
					
					logger.info(String.format("Out Result:%s", json));
					
					logger.info("Return TCS response  ..");
					
					return new HashMap<>(result);
				}
				
			}
			else {
				
				response.put("Result", String.format("%d", -100));
				response.put("Message", "TRX_HAS_NO_CHECK_MODE");
				
				return response;
			}
			
			
			
		}
		catch(NullOrEmptyInputParameters e) {
			
			logger.warn("Input param is null  ..",e);
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
    		value = "/v1/deprecated/payment/sales/cashout/check")
	@ResponseBody
	public Map<String,Object> checkCashOutSalesOrder(@RequestBody Map<String,Object> jsonRequest,
			@RequestHeader Map<String, String> headers){
		
		/*06092022
		 * 
		 * This end point to replace /v1/payment/sales/check
		 * 
		 * It should be used for finding a Sales Order (CashOut Only) and recording audit log once user retrieved data successfully
		 * 
		 */
		
		
		
		Map<String,Object> response = new HashMap<>();
		
		try {
			
			//06092022
			Optional<Map<String,String>> salesOrder = salesOrderService.findCashOutSalesOrder(jsonRequest.get("request_id").toString(), 
					tcsApiAuthService.extractUserAccountNumber(headers));
			
			if(!salesOrder.isPresent()) {
				
				/*
				 * 
				 * 
				 * 
				 */
				throw new SalesOrderNotFound("SALES_ORDER_NOT_FOUND");
				
			}
			
			/////////////////////////////////////////////////////////////////////////////////////////////////////
			
			
			logger.info("Starting checkCashOutSalesOrder process ..");
			logger.info("Checking input params ..");
			
			Map<String,Object> entity = new HashMap<>();
			
			entity.put("Order_Number", jsonRequest.get("request_id"));
			
			entity.put("User_Id",tcsApiAuthService.extractUserId(headers));
			entity.put("Msisdn",tcsApiAuthService.extractUserAccountNumber(headers));
			
			if((jsonRequest.get("check")==null || 
					jsonRequest.get("check").toString().isEmpty() || 
					jsonRequest.get("check").toString().equalsIgnoreCase("false"))) {
				
				//insert function for audit log type (CONFIRMED)
				
				entity.put("Action","CONFIRMED");
				
			}
			else {
				
				//insert function for audit log type (SEARCH)
				
				entity.put("Action","SEARCH");
				
				
			}

			Optional<String> audit_log=makerCheckerService.insertCashOutAuditLog(entity);
			
			if(!audit_log.isPresent()) {
				
				throw new SalesOrderNotFound("");
			}
			
			ObjectMapper objectMapper = new ObjectMapper();
			
			String json = objectMapper.writeValueAsString(salesOrder);
			
			logger.info(String.format("Out Result:%s", json));
			
			logger.info("Return TCS response  ..");
			
			return new HashMap<String,Object>(salesOrder.get());
			
			
			
		}
		catch(SalesOrderNotFound e) {
			
			logger.warn("SALES_ORDER_NOT_FOUND  ..",e);
			logger.warn("Prepare SALES_ORDER_NOT_FOUND response  ..");
			
			response.put("Result", String.format("%d", -200));
			response.put("Message", "SALES_ORDER_NOT_FOUND");
			
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
    		value = "/v1/payment/request/find")
	@ResponseBody
	public Map<String,Object> findSalesRequest(@RequestBody Map<String,Object> jsonRequest,
			@RequestHeader Map<String, String> headers){
		
		/*
		 * This end point to get Sales Request by ID
		 * 
		 */
		
		Map<String,Object> response = new HashMap<>();
		
		try {
			
			logger.info("Starting findSalesRequest process ..");
			logger.info("Checking input params ..");

			TcsParamsValidationHelper.checkRequiredParams(env, "tcs.required.params.sales.request.find", jsonRequest, logger);
			
			Optional<List<Map<String,String>>> sales_request = makerCheckerService.findSalesRequest(jsonRequest.get("request_id").toString(),
					tcsApiAuthService.extractUserName(headers));
			
			if(sales_request.isPresent()) {
				
				if(sales_request.get().size()<=0)
					throw new SalesRequestNotFound("");
				/*
				 * 
				 */
				
				if(!makerCheckerService.isSourceOrDestination(sales_request.get().get(0), 
						tcsApiAuthService.extractUserAccountNumber(headers)))
					throw new UserHasNoPrivileges("USER_IS_NOT_SRC_OR_DEST");
					
				List<Map<String,String>> r = sales_request.get();
				
				response.put("Sales_Request", r.get(0));
				response.put("Result", "0");
				response.put("Message", "OK");
				
				ObjectMapper objectMapper = new ObjectMapper();
				
				String json = objectMapper.writeValueAsString(response);
				
				logger.info(String.format("Out Result:%s", json));
				
				logger.info("Return TCS response  ..");
				
				
				return response;
			}
			
			throw new Exception();
			
			
		}
		catch(NullOrEmptyInputParameters e) {
			
			logger.warn("Input param is null  ..",e);
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
    		value = "/v1_backup/payment/sales/exec")
	@ResponseBody
	public Map<String,Object> execSalesRequest_Backup(@RequestBody Map<String,Object> jsonRequest,
			@RequestHeader Map<String, String> headers){
		
		/*
		 * This is used to execute Sales Request (Approve or Reject)
		 */
		
		Map<String,Object> response = new HashMap<>();
		
		try {

			logger.info("Starting execSalesRequest process ..");
			logger.info("Checking input params ..");

			TcsParamsValidationHelper.checkRequiredParams(env, "tcs.required.params.sales.request.exec", jsonRequest, logger);
			
			if((jsonRequest.get("check")==null || 
					jsonRequest.get("check").toString().isEmpty() || 
					jsonRequest.get("check").toString().equalsIgnoreCase("false"))) {
				
				logger.info("Calling in CheckOnly mode=false  ..");
				
				if(jsonRequest.get("password") == null || jsonRequest.get("password").toString().isEmpty()) {
					
					logger.warn("Password is mandatory  ..");
					logger.warn("Prepare PASSWORD_REQUIRED response  ..");
					
					response.put("Result", String.format("%d", -200));
					response.put("Message", "PASSWORD_REQUIRED");
					
					return response;
				}
				else {
					
					logger.info("Extracting username  ..");
					
					jsonRequest.put("username",tcsApiAuthService.extractUserName(headers));
					jsonRequest.put("check","false");
					
					logger.info("Calling TCS sales request exec function  ..");
					
					Map<String,String> result = tcsApiPaymentService.tcsSalesRequestExec(jsonRequest,headers);
					
					//Get authorized person details from internal database, just to get currency
					
					Optional<List<Map<String,String>>> salesRequest = makerCheckerService.findSalesRequest(jsonRequest.get("request_id").toString(),
							tcsApiAuthService.extractUserName(headers));
					
					
					
					if(salesRequest.isPresent()) {
						
						/*
						 * 
						 */
						List<Map<String,String>> r = salesRequest.get();
						
						//22-03-2022
						result.put("Currency", r.get(0).get("Currency"));
					}
					
					ObjectMapper objectMapper = new ObjectMapper();
					
					String json = objectMapper.writeValueAsString(result);
					
					logger.info(String.format("Out Result:%s", json));
					
					logger.info("Return TCS response  ..");
					
					return new HashMap<>(result);
				}
				
			}
			else {
				
				/*
				jsonRequest.put("username",tcsApiAuthService.extractUserName(headers));
				jsonRequest.put("password",tcsApiAuthService.extractUserPassword(headers));
				
				
				Map<String,String> result = tcsApiPaymentService.tcsCheckSalesRequestExec(jsonRequest,headers);
				
				return new HashMap<>(result);*/
				
				response.put("Result", String.format("%d", -100));
				response.put("Message", "TRX_HAS_NO_CHECK_MODE");
				
				return response;
			}
			
			
			
		}
		catch(NullOrEmptyInputParameters e) {
			
			logger.warn("Input param is null  ..",e);
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
    		value = "/v1/payment/sales/exec")
	@ResponseBody
	public Map<String,Object> execSalesRequest(@RequestBody Map<String,Object> jsonRequest,
			@RequestHeader Map<String, String> headers){
		
		/*
		 * jalshaibani
		 * 
		 * This is used to execute Sales Request (Approve or Reject)
		 * 
		 * 30082022
		 * 
		 * Whole refactoring is done on 30 AUG 2022:
		 * 
		 * All SRs of all Trx Types to be actioned using this API including BZCASHOUT
		 * 
		 * Validation Points:
		 * 1- Get user privileges starting with WebChecker*, if non found, throw exception UserHasNoPrivilege
		 * 2- Find pending approval lines, given SRID|ACCTID|PRIVILEGES, if non found, throw exception UserHasNoPrivilege
		 * 3- Check if the requester is the Current Approver of the given SRID
		 * 
		 * Note:
		 * - Each SR could have at least one checker line and at most two checker lines
		 * - The first line is created when the SR is created, and updated by Checker1 either by APPROVE or REJECT
		 * - If Checker1 approved the SR, the first line is updated and a second line is created and pending at Checker2
		 * - If Checker1 rejected the SR, no more lines are created and the SR will have only one checker line, this is an END POINT
		 * - If Checker2 approved or rejected the SR, the second line is updated and the process is complete,this is an END POINT too
		 *  
		 * 
		 * Main Conditions:
		 * 1- In case user action is REJECT the SR is executed (Regardless the Checker Level) and then update Internal DB (Checker Lines)
		 * 2- In case user action is APPROVE and user privilege is WebChecker1, update Internal DB (Checker1 Line), insert (Checker2 Line) ,then , return dummy response 
		 * 3- In case user action is APPROVE and user privilege is WebChecker2, first execute the SR, then, update Internal DB (Checker2 Line) ,then , return TCS response 
		 */
		
		Map<String,Object> response = new HashMap<>();
		
		try {

			logger.info("Starting execSalesRequest process ..");
			logger.info("Checking input params ..");

			//1- Check the mandatory Request's Parameters
			TcsParamsValidationHelper.checkRequiredParams(env, "tcs.required.params.sales.request.exec", jsonRequest, logger);
			
			if((jsonRequest.get("check")==null || 
					jsonRequest.get("check").toString().isEmpty() || 
					jsonRequest.get("check").toString().equalsIgnoreCase("false"))) {
				
				logger.info("Calling in CheckOnly mode=false  ..");
				
				if(jsonRequest.get("password") == null || jsonRequest.get("password").toString().isEmpty()) {
					
					logger.warn("Password is mandatory  ..");
					logger.warn("Prepare PASSWORD_REQUIRED response  ..");
					
					response.put("Result", String.format("%d", -200));
					response.put("Message", "PASSWORD_REQUIRED");
					
					return response;
				}
				else {
					
					String account_id=tcsApiAuthService.extractUserAccountId(headers);
					String user_name=tcsApiAuthService.extractUserName(headers);
					String password=tcsApiAuthService.extractUserPassword(headers);
					String user_id=tcsApiAuthService.extractUserId(headers);
					
					logger.info(String.format("Account ID found: %s",account_id));
					logger.info(String.format("User Name found: %s",user_name));
					
					Map<String,Object> request = new HashMap<>();
					
					request.put("username", user_name);
					//password is extracted in checkonly=false for get core privileges
					request.put("password", password);
					request.put("filter", FILTER_CHECKER);
					
					logger.info("Starting tcsGetFilteredCorePrivileges ..");
					
					//2-Get user privileges related to Maker/Checker process [WebChecker1 & WebChecker2]
					List<String> privileges = tcsApiAuthService.tcsGetFilteredCorePrivileges(request);
					
					if(privileges == null || privileges.size()<=0) {
						
						logger.warn("WebChecker* filter found no privileges ..");
						logger.warn("System to throw UserHasNoPrivileges exception! ..");
						
						throw new UserHasNoPrivileges("");
					}
						
					logger.info(String.format("WebChecker* filter found %d privileges ..", privileges.size()));
					
					/*
					 * 30082022
					 * 
					 * This part is refactored
					 * 
					 * 3- Find SR related checker lines given SRID, User Account ID & Checker Privilege(s)
					 */
					Optional<List<Map<String,Object>>> salesRequestCheckersLine = makerCheckerService.findSalesRequestCheckersLine(jsonRequest.get("request_id").toString(),
							account_id,
							privileges);
					
					//If no lines found, this means the user is not eligible to action the request
					if(!salesRequestCheckersLine.isPresent()) {
						
						logger.error("User has no checker lines");
						throw new UserHasNoPrivileges("");

					}
					else {
						

						try {
							
							
							logger.info("Current Checker "+getSalesRequestCurrentChecker(salesRequestCheckersLine.get().get(0)));
							
						}
						catch(Exception e) {
							
							throw new UserHasNoPrivileges("");
						}
					}
					
					//End of 30082022
					/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
					
					//In case all previous conditions are satisfied:
					//Get Sales Request, checkSalesRequest checks if SR is there and get it, otherwise an exception SalesRequestIsNotFound is thrown
					
					logger.info("Get Sales Request details from internal database  ..");
					
					Map<String,String> sr = checkSalesRequest(jsonRequest.get("request_id").toString(), tcsApiAuthService.extractUserName(headers));
					
					String svc_code=sr.get("Service");
					
					//4- Validate the checker, to check if the requesting user is the current Approver
					validateChecker(jsonRequest.get("request_id").toString(), privileges, svc_code,account_id);
					
					////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
					
					
					
					logger.info("Check user action input ..");
					
					if(jsonRequest.get("action").toString().contentEquals("REJECT")) {
						
						/*
						 * Execute the request then update the Checker Line
						 */
						
						logger.info("User Action is REJECT ..");
						
						logger.info("Execute the request, then update the Checker Line ..");
						
						logger.info("Extracting username  ..");
						
						jsonRequest.put("username",user_name);
						jsonRequest.put("check","false");
						
						logger.info("Calling TCS sales request exec function  ..");
						
						Map<String,String> result = tcsApiPaymentService.tcsSalesRequestExec(jsonRequest,headers);
						
						logger.info(String.format("tcsSalesRequestExec result=%s", result.get("Result")));
						logger.info(String.format("tcsSalesRequestExec message=%s", result.get("Message")));
						
						/*
						 * Update the checker line
						 */
						
						logger.info("Starting Update the checker line  ..");
						
						jsonRequest.put("checker_privilege", privileges.get(0));
						jsonRequest.put("user_id", user_id);
						jsonRequest.put("account_id", account_id);
						
						if(privileges.get(0).contentEquals("WebChecker1"))
							jsonRequest.put("status", "Checked1");
						if(privileges.get(0).contentEquals("WebChecker2"))
							jsonRequest.put("status", "Checked2");
						
						logger.info(String.format("User privilege found=%s",privileges.get(0)));
						logger.info("Update as Rejected  ..");
						
						
						Optional<Map<String,Object>> inDB_Update=makerCheckerService.rejectRequest(jsonRequest);
						
						if(inDB_Update.isPresent()) {
							
							logger.info(String.format("makerCheckerService.approveRequest affected rows %d ..", inDB_Update.get().get("affected_rows")));
						}
						
						/*
						 * Finalize the out result
						 */
						
						logger.info("Get currency for final result  ..");
						
						result.put("Currency", sr.get("Currency"));
						
						ObjectMapper objectMapper = new ObjectMapper();
						
						String json = objectMapper.writeValueAsString(result);
						
						logger.info(String.format("Out Result:%s", json));
						
						logger.info("Return TCS response  ..");
						
						return new HashMap<>(result);
					}
					
					if(jsonRequest.get("action").toString().contentEquals("APPROVE")) {
						
						/*
						 * Update the Checker Line then if privilege=Checker2 Exec the request
						 * 
						 * 								else return fake response
						 */
						
						//Update
						
						logger.info("User Action is APRROVE ..");
						
						logger.info("Update the Checker Line then if privilege=Checker2 Exec the request ..");
						
						if(privileges.get(0).contentEquals("WebChecker2")) {
							
							logger.info("privilege=WebChecker2 ..");
							
							logger.info("Extracting username  ..");
							
							jsonRequest.put("username",user_name);
							jsonRequest.put("check","false");
							
							logger.info("Calling TCS sales request exec function  ..");
							
							
							
							/*
							 * MODIFIED: Add condition check if request is CASHOUT then dont execute the request
							 * 
							 * NEW BEHAVIOR: in case action=APPROVE and checker=Checker2, then execute the SR regardless the service type
							 */
							
							Map<String,String> result = new HashMap<>();
							
							logger.info("Exec Sales Request normally ..");
							result = tcsApiPaymentService.tcsSalesRequestExec(jsonRequest,headers);
							
							
							/*
							 * Update the checker2 line
							 * 
							 * Note: in case updating external DB is failed while SR is already executed,
							 * 		 this is not a risk as the SR could not be executed more than once.
							 */
							
							if(result.get("Result").equalsIgnoreCase("0")) {
								
								logger.info(String.format("TCS API request is success, TCS Message : %s", result.get("Message")));
								logger.info("Starting to update checker2 line ..");
								
								jsonRequest.put("checker_privilege", privileges.get(0));
								jsonRequest.put("user_id", user_id);
								jsonRequest.put("account_id", account_id);
								
								jsonRequest.put("status", "Checked2");
								
								makerCheckerService.approveRequest(jsonRequest);
								
								logger.info("Request approved as checked2 ..");
								
								
							}
							
							/*
							 * Finlize out result
							 */
							
							result.put("Currency", sr.get("Currency"));
							
							ObjectMapper objectMapper = new ObjectMapper();
							
							String json = objectMapper.writeValueAsString(result);
							
							logger.info(String.format("Out Result:%s", json));
							
							logger.info("Return TCS response  ..");
							
							return new HashMap<>(result);
							
						}
						else {
							
							/*
							 * Update the checker1 line
							 */
							logger.info("privilege=WebChecker1 ..");
							
							jsonRequest.put("checker_privilege", privileges.get(0));
							jsonRequest.put("user_id", user_id);
							jsonRequest.put("account_id", account_id);
							
							jsonRequest.put("status", "Checked1");
							
							logger.info("Approve request as Checked1 ..");
							
							Optional<Map<String,Object>> inDB_Update=makerCheckerService.approveRequest(jsonRequest);
							
							if(inDB_Update.isPresent()) {
								
								logger.info(String.format("makerCheckerService.approveRequest affected rows %d ..", inDB_Update.get().get("affected_rows")));
							}
							
							logger.info("Request is approved ..");
							
							//Return dummy response as no Sales Order is generated in Checker1 level APPROVE action
							
							logger.info("Return dummy response as no Sales Order is generated ..");
							
							Map<String,String> result = new HashMap<>();
							
							/*
							 * 18072022
							 * 
							 * Added for multi checker flow to return sales request details instead of empty details 
							 */
							
							result = tcsApiPaymentService.tcsDummySalesRequestExec(sr);
							
							logger.info("Get currency for final result  ..");
							
							result.put("Currency", sr.get("Currency"));
							
							ObjectMapper objectMapper = new ObjectMapper();
							
							String json = objectMapper.writeValueAsString(result);
							
							logger.info(String.format("Out Result:%s", json));
							
							logger.info("Return TCS response  ..");
							
							return new HashMap<>(result);
						}
					}
					else {
						
						throw new InvalidCheckerAction("");
					}
					
					
					
					
				}
				
			}
			else {
				
				
				response.put("Result", String.format("%d", -100));
				response.put("Message", "TRX_HAS_NO_CHECK_MODE");
				
				return response;
			}
			
			
			
		}
		catch(InvalidCheckerAction e) {
			
			logger.warn("Input param is null  ..",e);
			logger.warn("Prepare InvalidCheckerAction response  ..");
			
			response.put("Result", String.format("%d", -200));
			response.put("Message", "InvalidCheckerAction");
			
			return response;
			
		}
		catch(NullOrEmptyInputParameters e) {
			
			logger.warn("Input param is null  ..",e);
			logger.warn("Prepare NULL_OR_EMPTY_INPUT response  ..");
			
			response.put("Result", String.format("%d", -200));
			response.put("Message", "NULL_OR_EMPTY_INPUT");
			
			return response;
			
		}
		catch(UserHasNoPrivileges e) {
			
			logger.warn("Input param is null  ..",e);
			logger.warn("Prepare UserHasNoPrivileges response  ..");
			
			response.put("Result", String.format("%d", -200));
			response.put("Message", "UserHasNoPrivileges");
			
			return response;
			
		}
		catch(SalesRequestNotFound e) {
			
			logger.warn("SalesRequestNotFound  ..",e);
			logger.warn("Prepare SalesRequestNotFound response  ..");
			
			response.put("Result", String.format("%d", -200));
			response.put("Message", "SalesRequestNotFound");
			
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
    		value = "/v1/backup/payment/sales/exec")
	@ResponseBody
	public Map<String,Object> execSalesRequest_BAK30082022(@RequestBody Map<String,Object> jsonRequest,
			@RequestHeader Map<String, String> headers){
		
		/*
		 * This is used to execute Sales Request (Approve or Reject)
		 * 
		 * Based on the new requirements, all SRs should be approved/rejected through this API, including BCashOut SRs
		 * The logic is based on:
		 * 1- Users with WebChecker1 privilege will view pending requests in status RQS and Approver=Checker1
		 * 2- Users with WebChecker2 privilege will view pending requests already approved by Checker1
		 * 3- SR should be exceuted when Checker2 approve or reject, unless the SR is BCashOut
		 * 
		 * 
		 * 
		 * 
		 * 14082022
		 * 4- SR could be Rejected (Executed) in case Checker2 & SR in Pending Status for BCashOut (Only)
		 * 5- Point 4 is applicable in two cases:
		 * 		5.1- SR is Pending Approval in Checker2 step
		 * 		5.2- SR is already approved by Checker2 but still in Pending Status (Not Executed by Agent)
		 */
		
		/*
		 * Logic Modified 14082022
		 * 
		 * 1- Find Sales Request by SR ID, if not found throw exception
		 * 2- If SR is found and Service Type= BCashOut & Action = Reject, then Check Special Valid Checker
		 * 												& Action = Approve, then Check Normal Valid Checker
		 * 3- If SR is not BCashOut check Normal Valid Checker regardless the action
		 */
		
		Map<String,Object> response = new HashMap<>();
		
		try {

			logger.info("Starting execSalesRequest process ..");
			logger.info("Checking input params ..");

			TcsParamsValidationHelper.checkRequiredParams(env, "tcs.required.params.sales.request.exec", jsonRequest, logger);
			
			if((jsonRequest.get("check")==null || 
					jsonRequest.get("check").toString().isEmpty() || 
					jsonRequest.get("check").toString().equalsIgnoreCase("false"))) {
				
				logger.info("Calling in CheckOnly mode=false  ..");
				
				if(jsonRequest.get("password") == null || jsonRequest.get("password").toString().isEmpty()) {
					
					logger.warn("Password is mandatory  ..");
					logger.warn("Prepare PASSWORD_REQUIRED response  ..");
					
					response.put("Result", String.format("%d", -200));
					response.put("Message", "PASSWORD_REQUIRED");
					
					return response;
				}
				else {
					
					String account_id=tcsApiAuthService.extractUserAccountId(headers);
					String user_name=tcsApiAuthService.extractUserName(headers);
					String password=tcsApiAuthService.extractUserPassword(headers);
					String user_id=tcsApiAuthService.extractUserId(headers);
					
					logger.info(String.format("Account ID found: %s",account_id));
					logger.info(String.format("User Name found: %s",user_name));
					
					Map<String,Object> request = new HashMap<>();
					
					request.put("username", user_name);
					//password is extracted in checkonly=false for get core privileges
					request.put("password", password);
					request.put("filter", FILTER_CHECKER);
					
					logger.info("Starting tcsGetFilteredCorePrivileges ..");
					
					List<String> privileges = tcsApiAuthService.tcsGetFilteredCorePrivileges(request);
					
					if(privileges.size()<=0) {
						
						logger.warn("WebChecker* filter found no privileges ..");
						logger.warn("System to throw UserHasNoPrivileges exception! ..");
						
						throw new UserHasNoPrivileges("");
					}
						
					logger.info(String.format("WebChecker* filter found %d privileges ..", privileges.size()));
					
					/*
					 * 30082022
					 * 
					 * This part is refactored
					 */
					Optional<List<Map<String,Object>>> salesRequestCheckersLine = makerCheckerService.findSalesRequestCheckersLine(jsonRequest.get("request_id").toString(),
							account_id,
							privileges);
					
					if(!salesRequestCheckersLine.isPresent()) {
						
						logger.error("User has no checker lines");
						throw new UserHasNoPrivileges("");

					}
					else {
						

						try {
							
							//salesRequestCheckersLine.get().get(0);
							
							logger.info("Current Checker "+getSalesRequestCurrentChecker(salesRequestCheckersLine.get().get(0)));
							//logger.info("Required Checkers "+getSalesRequestRequiredCheckers(salesRequestCheckerLine));
						}
						catch(Exception e) {
							
							throw new UserHasNoPrivileges("");
						}
					}
					
					//End of 30082022
					/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
					
					//Get Sales Request, checkSalesRequest checks if SR is there and get it, otherwise an exception SalesRequestIsNotFound is thrown
					
					logger.info("Get Sales Request details from internal database  ..");
					
					Map<String,String> sr = checkSalesRequest(jsonRequest.get("request_id").toString(), tcsApiAuthService.extractUserName(headers));
					
					String svc_code=sr.get("Service");
					
					validateChecker(jsonRequest.get("request_id").toString(), privileges, svc_code,account_id);
					
					////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
					
					
					
					logger.info("Check user action input ..");
					
					if(jsonRequest.get("action").toString().contentEquals("REJECT")) {
						
						/*
						 * Execute the request then update the Checker Line
						 */
						
						logger.info("User Action is REJECT ..");
						
						logger.info("Execute the request, then update the Checker Line ..");
						
						logger.info("Extracting username  ..");
						
						jsonRequest.put("username",user_name);
						jsonRequest.put("check","false");
						
						logger.info("Calling TCS sales request exec function  ..");
						
						Map<String,String> result = tcsApiPaymentService.tcsSalesRequestExec(jsonRequest,headers);
						
						logger.info(String.format("tcsSalesRequestExec result=%s", result.get("Result")));
						logger.info(String.format("tcsSalesRequestExec message=%s", result.get("Message")));
						
						/*
						 * Update the checker line
						 */
						
						logger.info("Starting Update the checker line  ..");
						
						jsonRequest.put("checker_privilege", privileges.get(0));
						jsonRequest.put("user_id", user_id);
						jsonRequest.put("account_id", account_id);
						
						if(privileges.get(0).contentEquals("WebChecker1"))
							jsonRequest.put("status", "Checked1");
						if(privileges.get(0).contentEquals("WebChecker2"))
							jsonRequest.put("status", "Checked2");
						
						logger.info(String.format("User privilege found=%s",privileges.get(0)));
						logger.info("Update as Rejected  ..");
						
						makerCheckerService.rejectRequest(jsonRequest);
						
						/*
						 * Finalize the out result
						 */
						
						logger.info("Get currency for final result  ..");
						
						result.put("Currency", sr.get("Currency"));
						
						ObjectMapper objectMapper = new ObjectMapper();
						
						String json = objectMapper.writeValueAsString(result);
						
						logger.info(String.format("Out Result:%s", json));
						
						logger.info("Return TCS response  ..");
						
						return new HashMap<>(result);
					}
					
					if(jsonRequest.get("action").toString().contentEquals("APPROVE")) {
						
						/*
						 * Update the Checker Line then if privilege=Checker2 Exec the request
						 * 
						 * 								else return fake response
						 */
						
						//Update
						
						logger.info("User Action is APRROVE ..");
						
						logger.info("Update the Checker Line then if privilege=Checker2 Exec the request ..");
						
						if(privileges.get(0).contentEquals("WebChecker2")) {
							
							logger.info("privilege=WebChecker2 ..");
							
							logger.info("Extracting username  ..");
							
							jsonRequest.put("username",user_name);
							jsonRequest.put("check","false");
							
							logger.info("Calling TCS sales request exec function  ..");
							
							
							
							/*
							 * Add condition check if request is CASHOUT then dont execute the request
							 */
							
							Map<String,String> result = new HashMap<>();
							
							logger.info("Check svc_code  ..");
							
							if(!svc_code.isEmpty() && svc_code.contentEquals("BZCASHOUT")) {
								
								//Return dummy response
								logger.info("Return dummy TCS response, Sales Request is BCASHOUT  ..");
								
								result = tcsApiPaymentService.tcsDummySalesRequestExec(sr);
								
							}
							else {
								
								//Do normally
								logger.info("Exec Sales Request normally ..");
								result = tcsApiPaymentService.tcsSalesRequestExec(jsonRequest,headers);
							}
							
							
							/*
							 * Update the checker2 line
							 * 
							 * Note: in case updating external DB is failed while SR is already executed,
							 * 		 this is not a risk as the SR could not be executed more than once.
							 */
							logger.info("Starting to update checker2 line ..");
							
							jsonRequest.put("checker_privilege", privileges.get(0));
							jsonRequest.put("user_id", user_id);
							jsonRequest.put("account_id", account_id);
							
							jsonRequest.put("status", "Checked2");
							
							makerCheckerService.approveRequest(jsonRequest);
							
							logger.info("Request approved as checked2 ..");
							
							/*
							 * Finlize out result
							 */
							
							result.put("Currency", sr.get("Currency"));
							
							ObjectMapper objectMapper = new ObjectMapper();
							
							String json = objectMapper.writeValueAsString(result);
							
							logger.info(String.format("Out Result:%s", json));
							
							logger.info("Return TCS response  ..");
							
							return new HashMap<>(result);
						}
						else {
							
							/*
							 * Update the checker1 line
							 */
							logger.info("privilege=WebChecker1 ..");
							
							jsonRequest.put("checker_privilege", privileges.get(0));
							jsonRequest.put("user_id", user_id);
							jsonRequest.put("account_id", account_id);
							
							jsonRequest.put("status", "Checked1");
							
							logger.info("Approve request as Checked1 ..");
							
							makerCheckerService.approveRequest(jsonRequest);
							
							logger.info("Request is approved ..");
							
							//Return dummy response as no Sales Order is generated in Checker1 level APPROVE action
							
							logger.info("Return dummy response as no Sales Order is generated ..");
							
							Map<String,String> result = new HashMap<>();
							
							/*
							 * 18072022
							 * 
							 * Added for multi checker flow to return sales request details instead of empty details 
							 */
							
							result = tcsApiPaymentService.tcsDummySalesRequestExec(sr);
							
							logger.info("Get currency for final result  ..");
							
							result.put("Currency", sr.get("Currency"));
							
							ObjectMapper objectMapper = new ObjectMapper();
							
							String json = objectMapper.writeValueAsString(result);
							
							logger.info(String.format("Out Result:%s", json));
							
							logger.info("Return TCS response  ..");
							
							return new HashMap<>(result);
						}
					}
					else {
						
						throw new InvalidCheckerAction("");
					}
					
					
					
					
				}
				
			}
			else {
				
				
				response.put("Result", String.format("%d", -100));
				response.put("Message", "TRX_HAS_NO_CHECK_MODE");
				
				return response;
			}
			
			
			
		}
		catch(InvalidCheckerAction e) {
			
			logger.warn("Input param is null  ..",e);
			logger.warn("Prepare InvalidCheckerAction response  ..");
			
			response.put("Result", String.format("%d", -200));
			response.put("Message", "InvalidCheckerAction");
			
			return response;
			
		}
		catch(NullOrEmptyInputParameters e) {
			
			logger.warn("Input param is null  ..",e);
			logger.warn("Prepare NULL_OR_EMPTY_INPUT response  ..");
			
			response.put("Result", String.format("%d", -200));
			response.put("Message", "NULL_OR_EMPTY_INPUT");
			
			return response;
			
		}
		catch(UserHasNoPrivileges e) {
			
			logger.warn("Input param is null  ..",e);
			logger.warn("Prepare UserHasNoPrivileges response  ..");
			
			response.put("Result", String.format("%d", -200));
			response.put("Message", "UserHasNoPrivileges");
			
			return response;
			
		}
		catch(SalesRequestNotFound e) {
			
			logger.warn("SalesRequestNotFound  ..",e);
			logger.warn("Prepare SalesRequestNotFound response  ..");
			
			response.put("Result", String.format("%d", -200));
			response.put("Message", "SalesRequestNotFound");
			
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
    		value = "/v2/payment/sales/exec")
	@ResponseBody
	public Map<String,Object> execSalesRequest2(@RequestBody Map<String,Object> jsonRequest,
			@RequestHeader Map<String, String> headers){
		
		/*
		 * 21082022
		 * 
		 * This API to action any SR (including BZCashOut)
		 * 
		 * 1- We need to differentiate if the provided ID is a CASHOUT or other transaction type
		 * 2- If not a CASHOUT then the process should go normally based on the caller privileges and approval flow
		 * 3- If it is a CASHOUT and caller is a Checker1 THEN:
		 *    3.1- If the caller is a Checker1 and Action is APPROVE, Update the internal Table CASHOUT_SALES_REQUEST_DETAILS with next approver
		 *    3.2- If the caller is a Checker1 and Action is REJECT, finalize the internal record and change status to DCL
		 * 4- If it is a CASHOUT and caller is a Checker2 THEN:
		 *    4.1- If the caller is a Checker2 and Action is REJECT, finalize the internal record and change status to DCL
		 *    4.2- If the caller is a Checker2 and Action is APPROVE:
		 *    	4.2.1- Get internal request details from CASHOUT_SALES_REQUEST_DETAILS table
		 *    	4.2.2- Check if destination is Alias or MSISDN
		 *    	4.2.3- Make a TCS request to create a SALES REQUEST using details found in table CASHOUT_SALES_REQUEST_DETAILS
		 *    	4.2.4- If TCS reply is success, then UPDATE the table CASHOUT_SALES_REQUEST_DETAILS column REQUEST_ID and Checker2 columns
		 *
		 */
		
		Map<String,Object> response = new HashMap<>();
		
		try {

			logger.info("Starting execSalesRequest process ..");
			logger.info("Checking input params ..");

			TcsParamsValidationHelper.checkRequiredParams(env, "tcs.required.params.sales.request.exec", jsonRequest, logger);
			
			
			
			if((jsonRequest.get("check")==null || 
					jsonRequest.get("check").toString().isEmpty() || 
					jsonRequest.get("check").toString().equalsIgnoreCase("false"))) {
				
				logger.info("Calling in CheckOnly mode=false  ..");
				
				if(jsonRequest.get("password") == null || jsonRequest.get("password").toString().isEmpty()) {
					
					logger.warn("Password is mandatory  ..");
					logger.warn("Prepare PASSWORD_REQUIRED response  ..");
					
					response.put("Result", String.format("%d", -200));
					response.put("Message", "PASSWORD_REQUIRED");
					
					return response;
				}
				else {
					
					String account_id=tcsApiAuthService.extractUserAccountId(headers);
					String user_name=tcsApiAuthService.extractUserName(headers);
					String password=tcsApiAuthService.extractUserPassword(headers);
					String user_id=tcsApiAuthService.extractUserId(headers);
					
					logger.info(String.format("Account ID found: %s",account_id));
					logger.info(String.format("User Name found: %s",user_name));
					
					Map<String,Object> request = new HashMap<>();
					
					jsonRequest.put("user_id", user_id);
					
					request.put("username", user_name);
					//password is extracted in checkonly=false for get core privileges
					request.put("password", password);
					request.put("filter", FILTER_CHECKER);
					
					logger.info("Starting tcsGetFilteredCorePrivileges to get user privileges for Maker/Checker process ..");
					
					List<String> privileges = tcsApiAuthService.tcsGetFilteredCorePrivileges(request);
					
					if(privileges.size()<=0) {
						
						logger.warn("WebChecker* filter found no privileges ..");
						logger.warn("System to throw UserHasNoPrivileges exception! ..");
						
						throw new UserHasNoPrivileges("");
					}
						
					logger.info(String.format("WebChecker* filter found %d privileges ..", privileges.size()));
					
					//1- Check if the provided ID is an internal cashout request
					
					if(makerCheckerService.isValidCashOutForApproval(jsonRequest.get("request_id").toString(), account_id, privileges)) {
						
						//Process the user action (Approve or Reject) as internal Cash Out record
						//If Checker1 Reject then update the record and close the process, else update & prepare the next approver
						//If Checker2 Reject then update the record and close the process
						//If Checker2 Approve then create SR using TP SALESREQUEST API and Update the Internal Table with the generated SRID
						
						if(privileges.get(0).contentEquals(CHECKER1) && jsonRequest.get("action").toString().contentEquals("REJECT")) {
							
							Integer ar = makerCheckerService.rejectInternalCashOutRequestChecker1(jsonRequest.get("request_id").toString(), 
									user_id, 
									jsonRequest.get("remarks").toString(), 
									CHECKER1);
							
							if(ar!=1)
								throw new SalesRequestNotFound("");
							
							//Return dummy response
							
							Optional<Map<String,Object>> local_request = makerCheckerService.findInternalCashOutSalesRequestDetails(jsonRequest.get("request_id").toString());
							
							if(local_request.isPresent()) {
								
								Map<String,String> result = MappingParamsHelper.mapDummyCashOutSalesRequestApproval(local_request.get(),
										jsonRequest.get("action").toString());
								
								ObjectMapper objectMapper = new ObjectMapper();
								
								String json = objectMapper.writeValueAsString(result);
								
								logger.info(String.format("Out Result:%s", json));
								
								logger.info("Return TCS response  ..");
								
								return new HashMap<>(result);
							}
							else
								throw new SalesRequestNotFound("");
						}
						
						if(privileges.get(0).contentEquals(CHECKER2) && jsonRequest.get("action").toString().contentEquals("REJECT")) {
							
							Integer ar = makerCheckerService.rejectInternalCashOutRequestChecker2(jsonRequest.get("request_id").toString(), 
									user_id, 
									jsonRequest.get("remarks").toString(), 
									CHECKER2);
							
							if(ar!=1)
								throw new SalesRequestNotFound("");
							
							//Return dummy response
							
							Optional<Map<String,Object>> local_request = makerCheckerService.findInternalCashOutSalesRequestDetails(jsonRequest.get("request_id").toString());
							
							if(local_request.isPresent()) {
								
								Map<String,String> result = MappingParamsHelper.mapDummyCashOutSalesRequestApproval(local_request.get(),
										jsonRequest.get("action").toString());
								
								ObjectMapper objectMapper = new ObjectMapper();
								
								String json = objectMapper.writeValueAsString(result);
								
								logger.info(String.format("Out Result:%s", json));
								
								logger.info("Return TCS response  ..");
								
								return new HashMap<>(result);
							}
							else
								throw new SalesRequestNotFound("");
						}
						
						if(privileges.get(0).contentEquals(CHECKER1) && jsonRequest.get("action").toString().contentEquals("APPROVE")) {
							
							Integer ar = makerCheckerService.approveInternalCashOutRequestChecker1(jsonRequest.get("request_id").toString(), 
									user_id, 
									jsonRequest.get("remarks").toString(), 
									CHECKER1,
									CHECKER2);
							
							if(ar!=1)
								throw new SalesRequestNotFound("");
							
							//Return dummy response
							
							Optional<Map<String,Object>> local_request = makerCheckerService.findInternalCashOutSalesRequestDetails(jsonRequest.get("request_id").toString());
							
							if(local_request.isPresent()) {
								
								Map<String,String> result = MappingParamsHelper.mapDummyCashOutSalesRequestApproval(local_request.get(),
										jsonRequest.get("action").toString());
								
								ObjectMapper objectMapper = new ObjectMapper();
								
								String json = objectMapper.writeValueAsString(result);
								
								logger.info(String.format("Out Result:%s", json));
								
								logger.info("Return TCS response  ..");
								
								return new HashMap<>(result);
							}
							else
								throw new SalesRequestNotFound("");
							
							
						}
						
						if(privileges.get(0).contentEquals(CHECKER2) && jsonRequest.get("action").toString().contentEquals("APPROVE")) {
							
							Optional<Map<String,Object>> local_request = makerCheckerService.findInternalCashOutSalesRequestDetails(jsonRequest.get("request_id").toString());
							
							if(local_request.isPresent()) {
								
								Map<String,Object> lr = local_request.get();
								
								String approver_remarks= new String(jsonRequest.get("remarks").toString());
								
								Integer i = makerCheckerService.preApprovalInternalCashOutRequestChecker2(jsonRequest.get("request_id").toString());
								
								if(i<=0) {
									
									//This is an indicator of a disconnection and process should stop here
									
									throw new SalesRequestNotFound("Cannot update the internal request before proceeding to TCS SR creation!");
								}
								
								Map<String,String> r = tcsApiPaymentService.tcsBusinessCashoutRequest2(makerCheckerService.buildCashOutSalesRequest(lr, 
																																			jsonRequest, 
																																			user_name, 
																																			account_id), 
										headers,
										approver_remarks);
								
								r.put("Currency",lr.get("CURRENCY").toString());
								
								ObjectMapper objectMapper = new ObjectMapper();
								
								String json = objectMapper.writeValueAsString(r);
								
								logger.info(String.format("Out Result:%s", json));
								
								logger.info("Return TCS response  ..");
								
								return new HashMap<>(r);
							}
							
							//If no local request found throw exception
							throw new SalesRequestNotFound("");
						}
						else {
							
							throw new SalesRequestNotFound("Invalid Action by User");
						}
						
					}
					else {
						
						//Process as usual Maker/Checker process, that is executing the SR after Checker2 either approve or reject
						
						//Get Sales Request, checkSalesRequest checks if SR is there and get it, otherwise an exception SalesRequestIsNotFound is thrown
						
						logger.info("Get Sales Request details from internal database  ..");
						
						Map<String,String> sr = checkSalesRequest(jsonRequest.get("request_id").toString(), tcsApiAuthService.extractUserName(headers));
						
						String svc_code=sr.get("Service");
						
						validateChecker(jsonRequest.get("request_id").toString(), privileges, svc_code,account_id);
						
						////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
						
						
						Optional<List<Map<String,Object>>> salesRequestCheckersLine = makerCheckerService.findSalesRequestCheckersLine(jsonRequest.get("request_id").toString());
						
						if(salesRequestCheckersLine.isPresent()) {
							
							Map<String,Object> salesRequestCheckerLine = salesRequestCheckersLine.get().get(0);
							
							logger.info("Current Checker "+getSalesRequestCurrentChecker(salesRequestCheckerLine));
							logger.info("Required Checkers "+getSalesRequestRequiredCheckers(salesRequestCheckerLine));
						}
						
						logger.info("Check user action input ..");
						
						if(jsonRequest.get("action").toString().contentEquals("REJECT")) {
							
							/*
							 * Execute the request then update the Checker Line
							 */
							
							logger.info("User Action is REJECT ..");
							
							logger.info("Execute the request, then update the Checker Line ..");
							
							logger.info("Extracting username  ..");
							
							jsonRequest.put("username",user_name);
							jsonRequest.put("check","false");
							
							logger.info("Calling TCS sales request exec function  ..");
							
							Map<String,String> result = tcsApiPaymentService.tcsSalesRequestExec(jsonRequest,headers);
							
							logger.info(String.format("tcsSalesRequestExec result=%s", result.get("Result")));
							logger.info(String.format("tcsSalesRequestExec message=%s", result.get("Message")));
							
							/*
							 * Update the checker line
							 */
							
							logger.info("Starting Update the checker line  ..");
							
							jsonRequest.put("checker_privilege", privileges.get(0));
							jsonRequest.put("user_id", user_id);
							jsonRequest.put("account_id", account_id);
							
							if(privileges.get(0).contentEquals("WebChecker1"))
								jsonRequest.put("status", "Checked1");
							if(privileges.get(0).contentEquals("WebChecker2"))
								jsonRequest.put("status", "Checked2");
							
							logger.info(String.format("User privilege found=%s",privileges.get(0)));
							logger.info("Update as Rejected  ..");
							
							makerCheckerService.rejectRequest(jsonRequest);
							
							/*
							 * Finalize the out result
							 */
							
							logger.info("Get currency for final result  ..");
							
							result.put("Currency", sr.get("Currency"));
							
							ObjectMapper objectMapper = new ObjectMapper();
							
							String json = objectMapper.writeValueAsString(result);
							
							logger.info(String.format("Out Result:%s", json));
							
							logger.info("Return TCS response  ..");
							
							return new HashMap<>(result);
						}
						
						if(jsonRequest.get("action").toString().contentEquals("APPROVE")) {
							
							/*
							 * Update the Checker Line then if privilege=Checker2 Exec the request
							 * 
							 * 								else return fake response
							 */
							
							//Update
							
							logger.info("User Action is APRROVE ..");
							
							logger.info("Update the Checker Line then if privilege=Checker2 Exec the request ..");
							
							if(privileges.get(0).contentEquals("WebChecker2")) {
								
								logger.info("privilege=WebChecker2 ..");
								
								logger.info("Extracting username  ..");
								
								jsonRequest.put("username",user_name);
								jsonRequest.put("check","false");
								
								
								/*
								 * Add condition check if request is CASHOUT then dont execute the request
								 */
								
								Map<String,String> result = new HashMap<>();
								
								logger.info("Check svc_code  ..");
								
								if(!svc_code.isEmpty() && svc_code.contentEquals("BZCASHOUT")) {
									
									//Return dummy response
									logger.info("Return dummy TCS response, Sales Request is BCASHOUT  ..");
									
									result = tcsApiPaymentService.tcsDummySalesRequestExec(sr);
									
								}
								else {
									
									//Do normally
									logger.info("Exec Sales Request normally ..");
									result = tcsApiPaymentService.tcsSalesRequestExec(jsonRequest,headers);
								}
								
								
								/*
								 * Update the checker2 line
								 * 
								 * Note: in case updating external DB is failed while SR is already executed,
								 * 		 this is not a risk as the SR could not be executed more than once.
								 */
								logger.info("Starting to update checker2 line ..");
								
								jsonRequest.put("checker_privilege", privileges.get(0));
								jsonRequest.put("user_id", user_id);
								jsonRequest.put("account_id", account_id);
								
								jsonRequest.put("status", "Checked2");
								
								makerCheckerService.approveRequest(jsonRequest);
								
								logger.info("Request approved as checked2 ..");
								
								/*
								 * Finlize out result
								 */
								
								result.put("Currency", sr.get("Currency"));
								
								ObjectMapper objectMapper = new ObjectMapper();
								
								String json = objectMapper.writeValueAsString(result);
								
								logger.info(String.format("Out Result:%s", json));
								
								logger.info("Return TCS response  ..");
								
								return new HashMap<>(result);
							}
							else {
								
								/*
								 * Update the checker1 line
								 */
								logger.info("privilege=WebChecker1 ..");
								
								jsonRequest.put("checker_privilege", privileges.get(0));
								jsonRequest.put("user_id", user_id);
								jsonRequest.put("account_id", account_id);
								
								jsonRequest.put("status", "Checked1");
								
								logger.info("Approve request as Checked1 ..");
								
								makerCheckerService.approveRequest(jsonRequest);
								
								logger.info("Request is approved ..");
								
								//Return dummy response as no Sales Order is generated in Checker1 level APPROVE action
								
								logger.info("Return dummy response as no Sales Order is generated ..");
								
								Map<String,String> result = new HashMap<>();
								
								/*
								 * 18072022
								 * 
								 * Added for multi checker flow to return sales request details instead of empty details 
								 */
								
								result = tcsApiPaymentService.tcsDummySalesRequestExec(sr);
								
								ObjectMapper objectMapper = new ObjectMapper();
								
								String json = objectMapper.writeValueAsString(result);
								
								logger.info(String.format("Out Result:%s", json));
								
								logger.info("Return TCS response  ..");
								
								return new HashMap<>(result);
							}
						}
						else {
							
							throw new InvalidCheckerAction("");
						}
											
					}
					
					
				}
				
			}
			else {
				
				
				response.put("Result", String.format("%d", -100));
				response.put("Message", "TRX_HAS_NO_CHECK_MODE");
				
				return response;
			}
			
			
			
		}
		catch(InvalidCheckerAction e) {
			
			logger.warn("Input param is null  ..",e);
			logger.warn("Prepare InvalidCheckerAction response  ..");
			
			response.put("Result", String.format("%d", -200));
			response.put("Message", "InvalidCheckerAction");
			
			return response;
			
		}
		catch(NullOrEmptyInputParameters e) {
			
			logger.warn("Input param is null  ..",e);
			logger.warn("Prepare NULL_OR_EMPTY_INPUT response  ..");
			
			response.put("Result", String.format("%d", -200));
			response.put("Message", "NULL_OR_EMPTY_INPUT");
			
			return response;
			
		}
		catch(UserHasNoPrivileges e) {
			
			logger.warn("Input param is null  ..",e);
			logger.warn("Prepare UserHasNoPrivileges response  ..");
			
			response.put("Result", String.format("%d", -200));
			response.put("Message", "UserHasNoPrivileges");
			
			return response;
			
		}
		catch(SalesRequestNotFound e) {
			
			logger.warn("SalesRequestNotFound  ..",e);
			logger.warn("Prepare SalesRequestNotFound response  ..");
			
			response.put("Result", String.format("%d", -200));
			response.put("Message", "SalesRequestNotFound");
			
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
    		value = "/v1/payment/bulk/sales/exec")
	@ResponseBody
	public Map<String,Object> execBulkSalesRequest(@RequestBody Map<String,Object> jsonRequest,
			@RequestHeader Map<String, String> headers){
		
		/*
		 * Added 18042022
		 * This is used to execute Bulk Related Sales Request (Approve or Reject)
		 */
		
		Map<String,Object> response = new HashMap<>();
		
		try {

			logger.info("Starting execBulkSalesRequest process ..");
			logger.info("Checking input params ..");

			TcsParamsValidationHelper.checkRequiredParams(env, "tcs.required.params.sales.request.exec", jsonRequest, logger);
			
			if((jsonRequest.get("check")==null || 
					jsonRequest.get("check").toString().isEmpty() || 
					jsonRequest.get("check").toString().equalsIgnoreCase("false"))) {
				
				logger.info("Calling in CheckOnly mode=false  ..");
				
				if(jsonRequest.get("password") == null || jsonRequest.get("password").toString().isEmpty()
						|| jsonRequest.get("username") == null || jsonRequest.get("username").toString().isEmpty()) {
					
					logger.warn("Password is mandatory  ..");
					logger.warn("Prepare PASSWORD_REQUIRED response  ..");
					
					response.put("Result", String.format("%d", -200));
					response.put("Message", "PASSWORD_REQUIRED");
					
					return response;
				}
				else {
					
					
					jsonRequest.put("check","false");
					
					logger.info("Calling TCS sales request exec function  ..");
					
					Map<String,String> result = tcsApiPaymentService.tcsSalesRequestExec(jsonRequest,headers);
					
					//Get authorized person details from internal database, just to get currency
					
					Optional<List<Map<String,String>>> salesRequest = makerCheckerService.findSalesRequest(jsonRequest.get("request_id").toString(),
							tcsApiAuthService.extractUserName(headers));
					
					if(salesRequest.isPresent()) {
						
						/*
						 * 
						 */
						List<Map<String,String>> r = salesRequest.get();
						
						//22-03-2022
						result.put("Currency", r.get(0).get("Currency"));
					}
					
					ObjectMapper objectMapper = new ObjectMapper();
					
					String json = objectMapper.writeValueAsString(result);
					
					logger.info(String.format("Out Result:%s", json));
					
					logger.info("Return TCS response  ..");
					
					return new HashMap<>(result);
				}
				
			}
			else {
				
				/*
				jsonRequest.put("username",tcsApiAuthService.extractUserName(headers));
				jsonRequest.put("password",tcsApiAuthService.extractUserPassword(headers));
				
				
				Map<String,String> result = tcsApiPaymentService.tcsCheckSalesRequestExec(jsonRequest,headers);
				
				return new HashMap<>(result);*/
				
				response.put("Result", String.format("%d", -100));
				response.put("Message", "TRX_HAS_NO_CHECK_MODE");
				
				return response;
			}
			
			
			
		}
		catch(NullOrEmptyInputParameters e) {
			
			logger.warn("Input param is null  ..",e);
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
    		value = "/v1/requests/find")
	@ResponseBody
	public Map<String,Object> findPendingRequests(@RequestParam String dest,
			@RequestParam String from,
			@RequestParam String to,
			@RequestParam Integer pageNumber,
			@RequestParam Integer pageSize,
			@RequestParam String status,
			@RequestParam String currency,
			@RequestHeader Map<String, String> headers){
		
		/*
		 * To find Pending Requests given Destination Number, Start & End Dates
		 * It supports Paging 
		 */
		
		Map<String,Object> response = new HashMap<>();
		Map<String,Object> request = new HashMap<>(); // for getting privileges
		
		
		
		try {
			

			logger.info("Starting findPendingRequests process ..");
			logger.info("Checking input params ..");
			
			request.put("username", tcsApiAuthService.extractUserName(headers));
			request.put("password", tcsApiAuthService.extractUserPassword(headers));
			
			/*
			 * To make sure to get requests related to the account where the user who is
			 * calling is the account created the request
			 */
			String source_id = tcsApiAuthService.extractUserAccountId(headers);
			
			List<String> privileges = tcsApiAuthService.tcsGetCorePrivileges(request);
			
			logger.info(String.format("Search params, source_id=%s, destination=%s, from=%s, to=%s, status=%s, currency=%s", 
					source_id,
					dest,
					from,
					to, 
					status,
					currency));
			
			Boolean self=false;
			
			for(String p:privileges) {
				
				if (p.contentEquals("WebMakerChekerViewSelf")) {
					
					self=true;
					break;
				}
				
			}
			
			if(self) {
				
				List<Map<String,String>> result = makerCheckerService.findSelfPendingRequests(request.get("username").toString(), 
						source_id,
						pageNumber,
						pageSize,
						dest,
						LocalDate.parse(from).atStartOfDay(),
						LocalDate.parse(to).plusDays(1).atStartOfDay(),
						status,
						currency);
				
				response.put("Result", 0);
				response.put("Message", "OK");
				response.put("Items", result);
			}
			else {
				
				/*
				 * 07062022
				 * 
				 * Added finding WebChecker privilege for search and multi checker feature
				 */
				
				request.put("filter", FILTER_CHECKER);
				privileges = tcsApiAuthService.tcsGetFilteredCorePrivileges(request);
				
				List<Map<String,String>> result = makerCheckerService.findPendingRequests(privileges, 
						source_id,
						pageNumber,
						pageSize,
						dest,
						LocalDate.parse(from).atStartOfDay(),
						LocalDate.parse(to).plusDays(1).atStartOfDay(),
						status,
						currency,
						tcsApiAuthService.extractUserName(headers));
				
				response.put("Result", 0);
				response.put("Message", "OK");
				response.put("Items", result);
			}
			
			
			return response;
			
		}
		catch(Exception e) {
			
			e.printStackTrace();
			
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
    		value = "/v1/bulk/requests/find")
	@ResponseBody
	public Map<String,Object> findBulkRelatedRequests(@RequestParam String bulk_ref,
			@RequestParam Integer pageNumber,
			@RequestParam Integer pageSize,
			@RequestHeader Map<String, String> headers){
		
		/*
		 * To find Pending BULK Requests given Bulk Ref Number
		 * It supports Paging
		 * 
		 *  Added 10042022
		 */
		
		Map<String,Object> response = new HashMap<>();
		Map<String,Object> request = new HashMap<>(); // for getting privileges
		
		
		
		try {
			

			logger.info("Starting findBulkRelatedRequests process ..");
			logger.info("Checking input params ..");
			
			request.put("username", tcsApiAuthService.extractUserName(headers));
			request.put("password", tcsApiAuthService.extractUserPassword(headers));
			
			/*
			 * To make sure to get requests related to the account where the user who is
			 * calling is the account created the request
			 */
			String source_id = tcsApiAuthService.extractUserAccountId(headers);
			
			List<String> privileges = tcsApiAuthService.tcsGetCorePrivileges(request);
			
			logger.info(String.format("Search params, source_id=%s, bulk_ref=%s", 
					source_id,
					bulk_ref));
			
			List<Map<String,String>> result = makerCheckerService.findBulkRelatedSalesRequests(privileges, 
					source_id,
					pageNumber,
					pageSize,
					bulk_ref,
					tcsApiAuthService.extractUserName(headers));
			
			response.put("Result", 0);
			response.put("Message", "OK");
			response.put("Items", result);
			
			return response;
			
		}
		catch(Exception e) {
			
			e.printStackTrace();
			
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
    		value = "/v1/requests/count")
	@ResponseBody
	public Map<String,Object> countPendingRequests(@RequestParam String dest,
			@RequestParam String from,
			@RequestParam String to,
			@RequestParam String status,
			@RequestParam String currency,
			@RequestHeader Map<String, String> headers){
		
		/*
		 * 
		 */
		
		Map<String,Object> response = new HashMap<>();
		Map<String,Object> request = new HashMap<>(); // for getting privileges
		
		
		try {
			
			logger.info("Starting countPendingRequests process ..");
			logger.info("Checking input params ..");
			
			request.put("username", tcsApiAuthService.extractUserName(headers));
			request.put("password", tcsApiAuthService.extractUserPassword(headers));
			
		} catch (InvalidKeyException | NoSuchPaddingException | NoSuchAlgorithmException
				| InvalidAlgorithmParameterException | BadPaddingException | IllegalBlockSizeException
				| InvalidKeySpecException | UnsupportedEncodingException e) {
			
			e.printStackTrace();
			
			response.put("Result", String.format("%d", -100));
			response.put("Message", "EXCEPTION_USER_EXTRACTION");
		}
		
		
		try {
			

			String source_id = tcsApiAuthService.extractUserAccountId(headers);
			String username = tcsApiAuthService.extractUserName(headers);
			
			List<String> privileges = tcsApiAuthService.tcsGetCorePrivileges(request);
			
			logger.info(String.format("Search params, source_id=%s, destination=%s, from=%s, to=%s, status=%s, currency=%s", 
					source_id,
					dest,
					from,
					to, 
					status,
					currency));
			
			Integer result = makerCheckerService.countPendingRequests(username, privileges, 
					source_id,
					dest,
					LocalDate.parse(from).atStartOfDay(),
					LocalDate.parse(to).plusDays(1).atStartOfDay(),
					status,
					currency);
			
			response.put("Result", 0);
			response.put("Message", "OK");
			response.put("Count", result);
			
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
    		value = "/v1/bulk/requests/count")
	@ResponseBody
	public Map<String,Object> countBulkRelatedRequests(@RequestParam String bulk_ref,
			@RequestParam String status,
			@RequestHeader Map<String, String> headers){
		
		/*
		 * To count Bulk Related Requests
		 */
		
		Map<String,Object> response = new HashMap<>();
		Map<String,Object> request = new HashMap<>(); // for getting privileges
		
		
		try {
			
			logger.info("Starting countBulkRelatedRequests process ..");
			logger.info("Checking input params ..");
			
			request.put("username", tcsApiAuthService.extractUserName(headers));
			request.put("password", tcsApiAuthService.extractUserPassword(headers));
			
		} catch (InvalidKeyException | NoSuchPaddingException | NoSuchAlgorithmException
				| InvalidAlgorithmParameterException | BadPaddingException | IllegalBlockSizeException
				| InvalidKeySpecException | UnsupportedEncodingException e) {
			
			e.printStackTrace();
			
			response.put("Result", String.format("%d", -100));
			response.put("Message", "EXCEPTION_USER_EXTRACTION");
		}
		
		
		try {
			

			String source_id = tcsApiAuthService.extractUserAccountId(headers);
			
			List<String> privileges = tcsApiAuthService.tcsGetCorePrivileges(request);
			
			logger.info(String.format("Search params, source_id=%s, bulk_ref=%s, status=%s", 
					source_id,
					bulk_ref,
					status));
			
			Integer result = makerCheckerService.countBulkRelatedRequests(privileges, 
					source_id,
					bulk_ref,
					status);
			
			response.put("Result", 0);
			response.put("Message", "OK");
			response.put("Count", result);
			
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
    		value = "/v1/business/cashout/request")
	@ResponseBody
	public Map<String,Object> businessCashoutRequest(@RequestBody Map<String,Object> jsonRequest,
			@RequestHeader Map<String, String> headers){
		
		/*
		 * For Business Cash Out scenario only where Business is the requester (Maker)
		 * And destination Agent is the Approver (Checker)
		 */
		
		Map<String,Object> response = new HashMap<>();
		
		try {

			logger.info("Starting businessCashoutRequest process ..");
			logger.info("Checking input params ..");

			TcsParamsValidationHelper.checkRequiredParams(env, "tcs.required.params.sales.request.cashout", jsonRequest, logger);
			
			if((jsonRequest.get("check")==null || 
					jsonRequest.get("check").toString().isEmpty() || 
					jsonRequest.get("check").toString().equalsIgnoreCase("false"))) {
				
				logger.info("Calling in CheckOnly mode=false  ..");
				
				if(jsonRequest.get("password") == null || jsonRequest.get("password").toString().isEmpty()) {
					
					logger.warn("Password is mandatory  ..");
					logger.warn("Prepare PASSWORD_REQUIRED response  ..");
					
					response.put("Result", String.format("%d", -200));
					response.put("Message", "PASSWORD_REQUIRED");
					
					return response;
				}
				else {
					
					logger.info("Extracting username  ..");
					
					jsonRequest.put("username",tcsApiAuthService.extractUserName(headers));
					jsonRequest.put("source_id",tcsApiAuthService.extractUserAccountId(headers));
					jsonRequest.put("user_id",tcsApiAuthService.extractUserId(headers));
					jsonRequest.put("check","false");
					
					//11-03-2022
					
					jsonRequest.put("code","BZCASHOUT");
					
					logger.info(String.format("Extracting Brand for code = %s ..", jsonRequest.get("code")));
					
					jsonRequest.put("brand",getBrandId(jsonRequest));
					
					/////////////////////////////////////////////////////////////////////////////////////////
					
					logger.info("Calling TCS payment function  ..");
					
					Map<String,String> result = tcsApiPaymentService.tcsBusinessCashoutRequest(jsonRequest,headers);
					
					logger.info(String.format("tcsBusinessCashoutRequest result=%s", result.get("Result")));
					logger.info(String.format("tcsBusinessCashoutRequest message=%s", result.get("Message")));
					
					
					if(result.get("Result").toString().contentEquals("0")) {
						
						logger.info("Create first checker record for approval flow ..");
						
						try {
							
							logger.info("Create first checker record for approval flow ..");
							
							result.put("account_id", jsonRequest.get("source_id").toString());
							result.put("user_id", jsonRequest.get("user_id").toString());
							
							Optional<Map<String,Object>> chk = makerCheckerService.createFirstChecker(result);
							
							if(chk.isPresent()) {
								
								logger.info(String.format("Created checker lines=%d ..", chk.get().get("created_rows")));
							}else {
								
								logger.error("createFirstChecker unknown error ..");
							}
							
						}
						catch(Exception e) {
							
							e.printStackTrace();
							logger.error(e.getMessage());
						}
					}
					else {
						
						logger.warn("tcsSalesRequest has failed, no checker line is created ..");
					}
					
					ObjectMapper objectMapper = new ObjectMapper();
					
					String json = objectMapper.writeValueAsString(result);
					
					logger.info(String.format("Out Result:%s", json));
					
					logger.info("Return TCS response  ..");
					
					return new HashMap<>(result);
				}
				
			}
			else {
				
				logger.info("Extracting username and password  ..");
				
				jsonRequest.put("username",tcsApiAuthService.extractUserName(headers));
				jsonRequest.put("password",tcsApiAuthService.extractUserPassword(headers));
				jsonRequest.put("source_id",tcsApiAuthService.extractUserAccountId(headers));
				
				//11-03-2022
				
				jsonRequest.put("code","BZCASHOUT");
				
				logger.info(String.format("Extracting Brand for code = %s ..", jsonRequest.get("code")));
				
				jsonRequest.put("brand",getBrandId(jsonRequest));
				
				/////////////////////////////////////////////////////////////////////////////////////////
				
				logger.info("Calling TCS payment function  ..");
				
				Map<String,String> result = tcsApiPaymentService.tcsCheckBusinessCashoutRequest(jsonRequest,headers);
				
				ObjectMapper objectMapper = new ObjectMapper();
				
				String json = objectMapper.writeValueAsString(result);
				
				logger.info(String.format("Out Result:%s", json));
				
				logger.info("Return TCS response  ..");
				
				return new HashMap<>(result);
			}
			
			
			
		}
		catch(BrandNotFound e) {
			
			response.put("Result", String.format("%d", -200));
			response.put("Message", "BRAND_NOT_FOUND");
			
			return response;
			
		}
		catch(NullOrEmptyInputParameters e) {
			
			logger.warn("Input param is null  ..",e);
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
    		value = "/v1/payment/voucher/check")
	@ResponseBody
	public Map<String,Object> checkVoucherBySalesOrder(@RequestBody Map<String,Object> jsonRequest,
			@RequestHeader Map<String, String> headers){
		
		/*
		 * To get voucher receiver details by sales order
		 * 
		 * Added 23-03-2022
		 */
		
		Map<String,Object> response = new HashMap<>();
		
		try {
			
			logger.info("Starting checkVoucherBySalesOrder process ..");
			logger.info("Checking input params ..");

			TcsParamsValidationHelper.checkRequiredParams(env, "tcs.required.params.payment.voucher.check", jsonRequest, logger);
			
			Optional<List<Map<String,Object>>> vouchers = voucherService.
					getVoucherDetails(jsonRequest.get("sales_order").toString());
			
			if(vouchers.isPresent()) {
				
				/*
				 * 
				 */
				logger.info(String.format("Sales order %s is found ..", jsonRequest.get("sales_order")));
				
				List<Map<String,Object>> r = vouchers.get();
				
				logger.info(String.format("Return Middleware response %s", r.get(0)));
				
				return r.get(0);
			}
			
			logger.info(String.format("Sales order %s is not found ..", jsonRequest.get("sales_order")));
			
			logger.info("Raise exception VOUCHER_NOT_FOUND  ..");
			
			throw new VoucherNotFound("VOUCHER_NOT_FOUND");
			
			
		}
		catch(VoucherNotFound e) {
			
			
			response.put("Result", String.format("%d", -200));
			response.put("Message", "VOUCHER_NOT_FOUND");
			
			return response;
			
		}
		catch(NullOrEmptyInputParameters e) {
			
			logger.warn("Input param is null  ..",e);
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
    		value = "/v1/bill/inquiry")
	@ResponseBody
	public Map<String,Object> checkPaymentGatewayBalance(@RequestBody Map<String,Object> jsonRequest,
			@RequestHeader Map<String, String> headers){
		
		/*
		 * To forward inquiries to Payment Gateway
		 * 
		 * Added 16-04-2022
		 */
		
		Map<String,Object> response = new HashMap<>();
		
		try {
			

			logger.info("Starting checkPaymentGatewayBalance process ..");
			logger.info("Checking input params ..");

			TcsParamsValidationHelper.checkRequiredParams(env, "tcs.required.params.bill.inquiry", jsonRequest, logger);
			
			return tcsApiPaymentService.checkPaymentGatewayBalance(jsonRequest, headers);
			
			
			
		}
		catch(NullOrEmptyInputParameters e) {
			
			logger.warn("Input param is null  ..",e);
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
    		value = "/v1/deprecated/business/cashout/exec")
	@ResponseBody
	public Map<String,Object> execBusinessCashoutRequest(@RequestBody Map<String,Object> jsonRequest,
			@RequestHeader Map<String, String> headers){
		
		/*
		 * jalshaibani
		 * 
		 * 05092022
		 * 
		 * Whole refactoring:
		 * 
		 * - This API just to record user actions
		 * - When called with check=true we need to insert an audit log record to show user has SEARCHED for the SO
		 * - When called with check=false we need to insert an audit log record to show user has CONFIRMED the SO
		 * 
		 * Validation Points:
		 * 1- Try to get Sales Order given user MSISDN as destination, if failed throw exception 
		 * 
		 */
		
		
		Map<String,Object> response = new HashMap<>();
		
		try {
			
			//Validate input params
			TcsParamsValidationHelper.checkRequiredParams(env, "tcs.required.params.sales.request.cashout.exec", jsonRequest, logger);
			
			Optional<Map<String,String>> salesOrder = salesOrderService.findCashOutSalesOrder(jsonRequest.get("request_id").toString(), 
					tcsApiAuthService.extractUserAccountNumber(headers));
			
			
			if(!salesOrder.isPresent()) {
				
				/*
				 * This part is responsible of checking the calling user if is Source or Destination
				 * 
				 * If not Source or Destination an exception should be thrown
				 * 
				 * Wrong conditions:
				 * 1- If user to action is not the destination
				 * 2- If user to action is the source regardless the ACTION
				 * 
				 */
				throw new SalesOrderNotFound("SALES_ORDER_NOT_FOUND");
				
			}
			
			/////////////////////////////////////////////////////////////////////////////////////////////////////
			
			logger.info("Starting execBusinessCashoutRequest process ..");
			logger.info("Checking input params ..");
			
			
			
			if((jsonRequest.get("check")==null || 
					jsonRequest.get("check").toString().isEmpty() || 
					jsonRequest.get("check").toString().equalsIgnoreCase("false"))) {
				
				logger.info("Calling in CheckOnly mode=false  ..");
				
				if(jsonRequest.get("password") == null || jsonRequest.get("password").toString().isEmpty()) {
					
					logger.warn("Password is mandatory  ..");
					logger.warn("Prepare PASSWORD_REQUIRED response  ..");
					
					response.put("Result", String.format("%d", -200));
					response.put("Message", "PASSWORD_REQUIRED");
					
					return response;
				}
				else {
					
					logger.info("Extracting username  ..");
					
					jsonRequest.put("username",tcsApiAuthService.extractUserName(headers));
					jsonRequest.put("check","false");
					
					//Add insert function for audit log type (CONFIRMED)
					
					Map<String,Object> entity = new HashMap<>();
					
					entity.put("Order_Number", jsonRequest.get("request_id"));
					entity.put("Action","CONFIRMED");
					entity.put("User_Id",tcsApiAuthService.extractUserId(headers));
					entity.put("Msisdn",tcsApiAuthService.extractUserAccountNumber(headers));
					
					Optional<String> audit_log=makerCheckerService.insertCashOutAuditLog(entity);
					
					if(!audit_log.isPresent()) {
						
						throw new SalesOrderNotFound("");
					}
					
					ObjectMapper objectMapper = new ObjectMapper();
					
					String json = objectMapper.writeValueAsString(salesOrder);
					
					logger.info(String.format("Out Result:%s", json));
					
					logger.info("Return TCS response  ..");
					
					return new HashMap<String,Object>(salesOrder.get());
				}
				
			}
			else {
				
				//insert function for audit log type (SEARCH)
				
				Map<String,Object> entity = new HashMap<>();
				
				entity.put("Order_Number", jsonRequest.get("request_id"));
				entity.put("Action","SEARCH");
				entity.put("User_Id",tcsApiAuthService.extractUserId(headers));
				entity.put("Msisdn",tcsApiAuthService.extractUserAccountNumber(headers));
				
				Optional<String> audit_log=makerCheckerService.insertCashOutAuditLog(entity);
				
				if(!audit_log.isPresent()) {
					
					throw new SalesOrderNotFound("");
				}
				
				ObjectMapper objectMapper = new ObjectMapper();
				
				String json = objectMapper.writeValueAsString(salesOrder);
				
				logger.info(String.format("Out Result:%s", json));
				
				logger.info("Return TCS response  ..");
				
				return new HashMap<String,Object>(salesOrder.get());
			}
			
		}
		catch(SalesOrderNotFound e) {
			
			logger.warn("SALES_ORDER_NOT_FOUND  ..",e);
			logger.warn("Prepare SALES_ORDER_NOT_FOUND response  ..");
			
			response.put("Result", String.format("%d", -200));
			response.put("Message", "SALES_ORDER_NOT_FOUND");
			
			return response;
			
		}
		catch(NullOrEmptyInputParameters e) {
			
			logger.warn("Input param is null  ..",e);
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
    		value = "/v1/backup/business/cashout/exec")
	@ResponseBody
	public Map<String,Object> execBusinessCashoutRequest_BAK30082022(@RequestBody Map<String,Object> jsonRequest,
			@RequestHeader Map<String, String> headers){
		
		/*
		 * To excute Business Cash Out only
		 * 
		 * 
		 * 
		 * add new logic to prevent the requester and non-destination accounts from executing
		 */
		
		
		Map<String,Object> response = new HashMap<>();
		
		try {
			
			//Validate input params
			TcsParamsValidationHelper.checkRequiredParams(env, "tcs.required.params.sales.request.cashout.exec", jsonRequest, logger);
			
			/*
			 * Check if request is fully approved by Checker 1 & 2 before processing
			 */
			if(!makerCheckerService.isValidCashoutToExecute(jsonRequest.get("request_id").toString()))
				throw new SalesRequestNotFound("");
			
			//16052022
			Optional<List<Map<String,String>>> salesRequest = makerCheckerService
					.findSalesRequest(jsonRequest.get("request_id").toString(),
							tcsApiAuthService.extractUserName(headers));
			
			if(salesRequest.isPresent()) {
				
				/*
				 * This part is responsible of checking the calling user if is Source or Destination
				 * 
				 * If not Source or Destination an exception should be thrown
				 * 
				 * Wrong conditions:
				 * 1- If user to action is not the destination
				 * 2- If user to action is the source and action is APPROVE
				 * 
				 */
				List<Map<String,String>> r = salesRequest.get();
				
				String dest_msisdn = r.get(0).get("Destination_Account");
				String source_msisdn = r.get(0).get("Source_Account");
				String action = (String) jsonRequest.get("action");
				
				logger.info(String.format("Sales Request Destination MSISDN %s", dest_msisdn));
				
				String user_msisdn=tcsApiAuthService.extractUserAccountNumber(headers);
				
				/*
				 * Wrong conditions:
				 * 1- If user to action is not the destination
				 * 2- If user to action is the source and action is APPROVE
				 */
				
				if(source_msisdn.contentEquals(user_msisdn) && action.contentEquals("APPROVE")) {
					
					throw new UserHasNoPrivileges("WRONG_DEST_ACCOUNT");
				}

				if(!source_msisdn.contentEquals(user_msisdn)) {
					
					if(!dest_msisdn.contentEquals(user_msisdn)) {
						
						throw new UserHasNoPrivileges("WRONG_DEST_ACCOUNT");
					}
				}		
			}
			
			/////////////////////////////////////////////////////////////////////////////////////////////////////
			
			logger.info("Starting execBusinessCashoutRequest process ..");
			logger.info("Checking input params ..");

			TcsParamsValidationHelper.checkRequiredParams(env, "tcs.required.params.sales.request.exec", jsonRequest, logger);
			
			if((jsonRequest.get("check")==null || 
					jsonRequest.get("check").toString().isEmpty() || 
					jsonRequest.get("check").toString().equalsIgnoreCase("false"))) {
				
				logger.info("Calling in CheckOnly mode=false  ..");
				
				if(jsonRequest.get("password") == null || jsonRequest.get("password").toString().isEmpty()) {
					
					logger.warn("Password is mandatory  ..");
					logger.warn("Prepare PASSWORD_REQUIRED response  ..");
					
					response.put("Result", String.format("%d", -200));
					response.put("Message", "PASSWORD_REQUIRED");
					
					return response;
				}
				else {
					
					logger.info("Extracting username  ..");
					
					jsonRequest.put("username",tcsApiAuthService.extractUserName(headers));
					jsonRequest.put("check","false");
					
					logger.info("Calling TCS sales request exec function  ..");
					
					Map<String,String> result = tcsApiPaymentService.tcsBusinessCashoutRequestExec(jsonRequest,headers);
				
					
					/*
					 * As currency used when the SR is created is not known, we use the currency retrieved with SR data
					 */
					List<Map<String,String>> r = salesRequest.get();
					
					//22-03-2022
					result.put("Currency", r.get(0).get("Currency"));
					
					ObjectMapper objectMapper = new ObjectMapper();
					
					String json = objectMapper.writeValueAsString(result);
					
					logger.info(String.format("Out Result:%s", json));
					
					logger.info("Return TCS response  ..");
					
					return new HashMap<>(result);
				}
				
			}
			else {
				
				response.put("Result", String.format("%d", -100));
				response.put("Message", "TRX_HAS_NO_CHECK_MODE");
				
				return response;
			}
			
			
			
		}
		catch(SalesRequestNotFound e) {
			
			logger.warn("SalesRequestNotFound  ..",e);
			logger.warn("Prepare SalesRequestNotFound response  ..");
			
			response.put("Result", String.format("%d", -200));
			response.put("Message", "SalesRequestNotFound");
			
			return response;
			
		}
		catch(NullOrEmptyInputParameters e) {
			
			logger.warn("Input param is null  ..",e);
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
    		value = "/v1_backup/business/cashout/exec")
	@ResponseBody
	public Map<String,Object> execBusinessCashoutRequest_Backup(@RequestBody Map<String,Object> jsonRequest,
			@RequestHeader Map<String, String> headers){
		
		/*
		 * To excute Business Cash Out only
		 * 
		 * 
		 * 
		 * add new logic to prevent the requester and non-destination accounts from executing
		 */
		
		
		Map<String,Object> response = new HashMap<>();
		
		try {
			
			//16052022
			Optional<List<Map<String,String>>> salesRequest = makerCheckerService.findSalesRequest(jsonRequest.get("request_id").toString(),
					tcsApiAuthService.extractUserName(headers));
			
			if(salesRequest.isPresent()) {
				
				/*
				 * 
				 */
				List<Map<String,String>> r = salesRequest.get();
				
				String dest_msisdn = r.get(0).get("Destination_Account");
				String source_msisdn = r.get(0).get("Source_Account");
				String action = (String) jsonRequest.get("action");
				
				logger.info(String.format("Sales Request Destination MSISDN %s", dest_msisdn));
				
				String user_msisdn=tcsApiAuthService.extractUserAccountNumber(headers);
				
				/*
				 * Wrong conditions:
				 * 1- If user to action is not the destination
				 * 2- If user to action is the source and action is APPROVE
				 */
				
				if(source_msisdn.contentEquals(user_msisdn) && action.contentEquals("APPROVE")) {
					
					throw new UserHasNoPrivileges("WRONG_DEST_ACCOUNT");
				}

				if(!source_msisdn.contentEquals(user_msisdn)) {
					
					if(!dest_msisdn.contentEquals(user_msisdn)) {
						
						throw new UserHasNoPrivileges("WRONG_DEST_ACCOUNT");
					}
				}		
			}
			
			/////////////////////////////////////////////////////////////////////////////////////////////////////
			
			logger.info("Starting execBusinessCashoutRequest process ..");
			logger.info("Checking input params ..");

			TcsParamsValidationHelper.checkRequiredParams(env, "tcs.required.params.sales.request.exec", jsonRequest, logger);
			
			if((jsonRequest.get("check")==null || 
					jsonRequest.get("check").toString().isEmpty() || 
					jsonRequest.get("check").toString().equalsIgnoreCase("false"))) {
				
				logger.info("Calling in CheckOnly mode=false  ..");
				
				if(jsonRequest.get("password") == null || jsonRequest.get("password").toString().isEmpty()) {
					
					logger.warn("Password is mandatory  ..");
					logger.warn("Prepare PASSWORD_REQUIRED response  ..");
					
					response.put("Result", String.format("%d", -200));
					response.put("Message", "PASSWORD_REQUIRED");
					
					return response;
				}
				else {
					
					logger.info("Extracting username  ..");
					
					jsonRequest.put("username",tcsApiAuthService.extractUserName(headers));
					jsonRequest.put("check","false");
					
					logger.info("Calling TCS sales request exec function  ..");
					
					Map<String,String> result = tcsApiPaymentService.tcsBusinessCashoutRequestExec(jsonRequest,headers);
					
					//Get authorized person details from internal database, just to get currency
					
					//Optional<List<Map<String,String>>> salesRequest = makerCheckerService.findSalesRequest(jsonRequest.get("request_id").toString());
					
					if(salesRequest.isPresent()) {
						
						/*
						 * 
						 */
						List<Map<String,String>> r = salesRequest.get();
						
						//22-03-2022
						result.put("Currency", r.get(0).get("Currency"));
					}
					
					ObjectMapper objectMapper = new ObjectMapper();
					
					String json = objectMapper.writeValueAsString(result);
					
					logger.info(String.format("Out Result:%s", json));
					
					logger.info("Return TCS response  ..");
					
					return new HashMap<>(result);
				}
				
			}
			else {
				
				/*
				jsonRequest.put("username",tcsApiAuthService.extractUserName(headers));
				jsonRequest.put("password",tcsApiAuthService.extractUserPassword(headers));
				
				
				Map<String,String> result = tcsApiPaymentService.tcsCheckSalesRequestExec(jsonRequest,headers);
				
				return new HashMap<>(result);*/
				
				response.put("Result", String.format("%d", -100));
				response.put("Message", "TRX_HAS_NO_CHECK_MODE");
				
				return response;
			}
			
			
			
		}
		catch(NullOrEmptyInputParameters e) {
			
			logger.warn("Input param is null  ..",e);
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
	
	@SuppressWarnings("unused")
	private String getRequestBrand(String code) {
		
		switch(code) {
		
		case "M2P":
			return env.getProperty("tcs.payment.business.m2p");
		case "M2B":
			return env.getProperty("tcs.payment.business.m2b");
		case "M2M":
			return env.getProperty("tcs.payment.business.m2m");
		case "B2P":
			return env.getProperty("tcs.payment.business.b2p");
		case "B2B":
			return env.getProperty("tcs.payment.business.b2b");
		case "B2M":
			return env.getProperty("tcs.payment.business.b2m");
		case "SELLFLOAT":
			return env.getProperty("tcs.float.sell");
		case "PUSHFLOAT":
			return env.getProperty("tcs.float.push");
		case "PULLFLOAT":
			return env.getProperty("tcs.float.pull");
		case "BZCASHOUT":
			return env.getProperty("tcs.payment.business.cashout");
		case "BZCASHIN":
			return env.getProperty("tcs.payment.business.cashin");
		case "MONEYUNALLOC":
			return env.getProperty("tcs.payment.money.unallocation");
			default: return "";
		}
	}
	
	private Object getBrandId(Map<String,Object> jsonRequest) throws BrandNotFound {
		
		//Change done for multicurrency operations
		//11-03-2022
		
		//Transaction currency=YER if not provided
		if(jsonRequest.get("currency") == null) {
			
			jsonRequest.put("currency", "YER");
		}
		
		logger.info(String.format("getBrandId for code:%s and currency:%s", jsonRequest.get("code"),jsonRequest.get("currency")));
		
		Optional<List<Map<String,Object>>> brand = brandLookupService.findBrand(jsonRequest.get("code").toString(), jsonRequest.get("currency").toString());
		
		if(!brand.isPresent()) {
			
			logger.warn(String.format("Brand not found for code = %s ..", jsonRequest.get("code")));
			
			throw new BrandNotFound("");
		}
		
		//Fix 24-03-2022
		//For returned list of brands with size=0
		
		List<Map<String,Object>> r = brand.get();
		
		if(r.size()==0)
			throw new BrandNotFound("");
		
		logger.info(String.format("List of Brand ID found for %s  = %d",jsonRequest.get("code"), r.size()));
		logger.info(String.format("Brand ID found for %s  = %d", jsonRequest.get("code"),r.get(0).get("Brand")));
		
		return r.get(0).get("Brand");
		
		
		//End of multicurrency change
	}
	
	@RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE, 
    		method = RequestMethod.POST,
    		value = "/v1/payment/order/find")
	@ResponseBody
	public Map<String,Object> findSalesOrder(@RequestBody Map<String,Object> jsonRequest,
			@RequestHeader Map<String, String> headers){
		
		/*
		 * This end point to get Sales Order by ID
		 * 
		 * 31052022
		 */
		
		Map<String,Object> response = new HashMap<>();
		
		try {
			
			logger.info("Starting findSalesOrder process ..");
			logger.info("Checking input params ..");

			TcsParamsValidationHelper.checkRequiredParams(env, "tcs.required.params.payment.order.find", jsonRequest, logger);
			
			String user_msisdn = tcsApiAuthService.extractUserAccountNumber(headers);
			
			logger.info(String.format("Extracting user MSISDN .. %s", user_msisdn));
			
			/*
			 * Note: sales_order.get() could return a 0 size list
			 * 
			 * As the user is free to enter the sales order number manually, 
			 * this case is likely to happen, therefore, the if(r.size() > 0) condition is added
			 */
			
			Optional<List<Map<String,String>>> sales_order = salesOrderService.findSalesOrder(jsonRequest.get("order_id").toString(),user_msisdn);
			
			if(sales_order.isPresent()) {
				
				/*
				 * 
				 */
				List<Map<String,String>> r = sales_order.get();
				
				if(r.size() > 0) {
					
					response.put("Sales_Order", r.get(0));
					response.put("Result", "0");
					response.put("Message", "OK");
					
					ObjectMapper objectMapper = new ObjectMapper();
					
					String json = objectMapper.writeValueAsString(response);
					
					logger.info(String.format("Out Result:%s", json));
					
					logger.info("Return TCS response  ..");
					
					return response;
				}
				else {
					
					response.put("Sales_Order",r);
					response.put("Result", "-1");
					response.put("Message", "NOT_FOUND");
					
					ObjectMapper objectMapper = new ObjectMapper();
					
					String json = objectMapper.writeValueAsString(response);
					
					logger.info(String.format("Out Result:%s", json));
					
					logger.info("Return TCS response  ..");
					
					return response;
				}
				
				
			}
			
			throw new SalesOrderNotFound("SALES_ORDER_NOT_FOUND");
			
			
		}
		catch(SalesOrderNotFound e) {
			
			logger.warn("SALES_ORDER_NOT_FOUND  ..",e);
			logger.warn("Prepare SALES_ORDER_NOT_FOUND response  ..");
			
			response.put("Result", String.format("%d", -200));
			response.put("Message", "SALES_ORDER_NOT_FOUND");
			
			return response;
			
		}
		catch(NullOrEmptyInputParameters e) {
			
			logger.warn("Input param is null  ..",e);
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
    		value = "/v1/payment/order/refund/find")
	@ResponseBody
	public Map<String,Object> findSalesOrderRefundTrx(@RequestBody Map<String,Object> jsonRequest,
			@RequestHeader Map<String, String> headers){
		
		/*
		 * This end point to get Sales Order Refund Transactions by Original SO ID
		 * 
		 * 31052022
		 */
		
		Map<String,Object> response = new HashMap<>();
		
		try {
			
			logger.info("Starting findSalesOrderRefundTrx process ..");
			logger.info("Checking input params ..");

			TcsParamsValidationHelper.checkRequiredParams(env, "tcs.required.params.payment.order.find", jsonRequest, logger);
			
			String user_msisdn = tcsApiAuthService.extractUserAccountNumber(headers);
			
			logger.info(String.format("Extracting user MSISDN .. %s", user_msisdn));
			
			Optional<List<Map<String,String>>> sales_order = salesOrderService.findSalesOrderRefundTrx(jsonRequest.get("order_id").toString(),user_msisdn);
			
			if(sales_order.isPresent()) {
				
				/*
				 * 
				 */
				List<Map<String,String>> r = sales_order.get();
				
				if(r.size() > 0) {
					
					response.put("Sales_Orders", r);
					response.put("Result", "0");
					response.put("Message", "OK");
					
					ObjectMapper objectMapper = new ObjectMapper();
					
					String json = objectMapper.writeValueAsString(response);
					
					logger.info(String.format("Out Result:%s", json));
					
					logger.info("Return TCS response  ..");
					
					return response;
				}
				else {
					
					response.put("Sales_Order",r);
					response.put("Result", "-1");
					response.put("Message", "NOT_FOUND");
					
					ObjectMapper objectMapper = new ObjectMapper();
					
					String json = objectMapper.writeValueAsString(response);
					
					logger.info(String.format("Out Result:%s", json));
					
					logger.info("Return TCS response  ..");
					
					return response;
				}
				
				
			}
			
			throw new Exception();
			
			
		}
		catch(NullOrEmptyInputParameters e) {
			
			logger.warn("Input param is null  ..",e);
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
		
	private Map<String,String> checkSalesRequest(String request_id,String username) throws SalesRequestNotFound{
		
		Optional<List<Map<String,String>>> salesRequest = makerCheckerService.findSalesRequest(request_id,
				username);
		
		
		if(!salesRequest.isPresent()) {
			
			/*
			 * If SR is not found exception is thrown
			 */
			
			throw new SalesRequestNotFound("");
		}
		
		logger.info("Sales Request details found  ..");
		logger.info(String.format("salesRequest size = %d ", salesRequest.get().size()));
		
		if(salesRequest.get().size() <= 0 || salesRequest.get().size() > 1) {
			
			logger.error("Wrong number of sales requests are found, service exiting with error ... ");
			throw new SalesRequestNotFound(String.format("salesRequest size = %d ", salesRequest.get().size()));
		}
			
		return salesRequest.get().get(0);
	}
	
	private void validateChecker(String request_id,List<String> privileges,String svc_code,String account_id) throws SalesRequestNotFound{
		
		/*
		 * Modification 22082022
		 * 
		 * Based on the latest changes in Cashout Maker/Checker flow, it is assumed that this function is to validate non-cashout SRs
		 * 
		 * 30082022: Will keep exclusion of BZCASHOUT trx and to be executed by its separated API
		 * 
		 * 19092022: 
		 * 
		 */
		
		logger.info("Check if isValidChecker ..");
		
		if(!makerCheckerService.isValidChecker(request_id
				, privileges,account_id)) {
			
			logger.warn("Checker is invalid! ..");
			logger.warn("System to throw SalesRequestNotFound exception! ..");
			
			throw new SalesRequestNotFound("");
		}
		
		/*
		if(!svc_code.contentEquals(BZCASHOUT)) {
			
			logger.info("Check if isValidChecker ..");
			
			if(!makerCheckerService.isValidChecker(request_id
					, privilege)) {
				
				logger.warn("Checker is invalid! ..");
				logger.warn("System to throw SalesRequestNotFound exception! ..");
				
				throw new SalesRequestNotFound("");
			}
		}
		else {
			
			logger.warn("Invalid Sales Request ID is provided .. ");
			
			throw new SalesRequestNotFound("");
		}*/
			
	}
	
	@RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE, 
    		method = RequestMethod.GET,
    		value = "/v1/business/forex/rates")
	@ResponseBody
	public Map<String,Object> getForexRates(){
		
		/*
		 * 01102022
		 * To retrieve forex rates
		 */
		
		Map<String,Object> response = new HashMap<>();
		
		Map<String,String> usd = new HashMap<>();
		usd.put("curr_en", env.getProperty("USD"));
		usd.put("curr_ar", "دولار أمريكي");
		usd.put("rate", "0.00");
		
		Map<String,String> sar = new HashMap<>();
		sar.put("curr_en", env.getProperty("SAR"));
		sar.put("curr_ar", "ريال سعودي");
		sar.put("rate", "0.00");
		
		Map<String,String> cby_usd = new HashMap<>();
		cby_usd.put("curr_en", env.getProperty("CBY_USD"));
		cby_usd.put("curr_ar", "لجنة المدفوعات");
		cby_usd.put("rate", "0.00");
		
		try {
			

			logger.info("Starting getForexRates process ..");
			
			Map<String,Object> r = tcsApiPaymentService.getLatestForexRates();
			
			if(r!=null) {
				
				usd.put("rate", r.get(env.getProperty("USD")).toString());
				sar.put("rate", r.get(env.getProperty("SAR")).toString());
				cby_usd.put("rate", r.get(env.getProperty("CBY_USD")).toString());
				
				response.put(env.getProperty("USD"),usd);
		        response.put(env.getProperty("SAR"),sar);
		        response.put(env.getProperty("CBY_USD"),cby_usd);
		        
				return response;
			}
			
			logger.warn("No rates found");
	        throw new Exception("No Rates Found");
	        
		}
		catch(Exception e) {
			
			e.printStackTrace();
			
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
    		value = "/v1/business/forex/rate/add")
	@ResponseBody
	public Map<String,Object> addNewRate(@RequestBody Map<String,Object> jsonRequest,
			@RequestHeader Map<String, String> headers){
		
		/*
		 * 01102022
		 * To add new rates
		 */
		
		Map<String,Object> response = new HashMap<>();
		
		try {

			logger.info("Starting addNewRate process ..");
			logger.info("Checking input params ..");
			
			if(jsonRequest.get(env.getProperty("USD")) == null || jsonRequest.get(env.getProperty("USD")).toString().isEmpty()) {
				
				logger.error(String.format("Validation error: Mandatory params, USD=%s", jsonRequest.get(env.getProperty("USD"))));
				throw new NullOrEmptyInputParameters("NULL_OR_EMPTY_INPUT");
			}
			
			if(jsonRequest.get(env.getProperty("SAR")) == null || jsonRequest.get(env.getProperty("SAR")).toString().isEmpty()) {
				
				logger.error(String.format("Validation error: Mandatory params, SAR=%s", jsonRequest.get(env.getProperty("SAR"))));
				throw new NullOrEmptyInputParameters("NULL_OR_EMPTY_INPUT");
			}
			
			if(jsonRequest.get(env.getProperty("CBY_USD")) == null || jsonRequest.get(env.getProperty("CBY_USD")).toString().isEmpty()) {
				
				logger.error(String.format("Validation error: Mandatory params, CBY_USD=%s", jsonRequest.get(env.getProperty("CBY_USD"))));
				throw new NullOrEmptyInputParameters("NULL_OR_EMPTY_INPUT");
			}
			
			Map<String,Object> request = jsonRequest;
			
			request.put("filter", FILTER_FOREX_MASTER);
			request.put("target_user", tcsApiAuthService.extractUserName(headers));
			
			List<String> privileges = tcsApiAuthService.tcsGetFilteredUserCorePrivileges(request);
			
			if(privileges == null || privileges.size()!=1)
				throw new UserHasNoPrivileges("");
			
			jsonRequest.put("Created_By", tcsApiAuthService.extractUserName(headers));
			
			Integer c = tcsApiPaymentService.addNewRates(jsonRequest);
			
			if(c==1) {
				
				response.put("Result", "0");
		        response.put("Message", "OK");
		        
		        return response;
			}
	        
			logger.error(String.format("Adding new rates process has failed"));
			
			throw new Exception("No Rates Added");
			
		}
		catch(UserHasNoPrivileges e) {
			
			logger.error("Exception is caught ..",e);
			logger.error(e.getMessage());
			
			response.put("Result", String.format("%d", -100));
			response.put("Message", "USER_HAS_NO_PRIVILEGES");
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
	
	
	private String getSalesRequestCurrentChecker(Map<String,Object> salesRequestCheckerLine) {
		
		//Optional<List<Map<String,Object>>> salesRequestCheckersLine = makerCheckerService.findSalesRequestCheckersLine(request_id);
		//Map<String,Object> salesRequestCheckerLine = salesRequestCheckersLine.get().get(0);
		
		return salesRequestCheckerLine.get("Current_Approver").toString();
	}
	
	private int getSalesRequestRequiredCheckers(Map<String,Object> salesRequestCheckerLine) {
		
		//Optional<List<Map<String,Object>>> salesRequestCheckersLine = makerCheckerService.findSalesRequestCheckersLine(request_id);
		//Map<String,Object> salesRequestCheckerLine = salesRequestCheckersLine.get().get(0);
		
		return Integer.parseInt(salesRequestCheckerLine.get("Checkers_Required").toString()); 
	}
}
