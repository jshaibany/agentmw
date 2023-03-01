package com.onecashye.web.security.service;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClientException;

import com.onecashye.web.security.helper.MappingParamsHelper;
import com.onecashye.web.security.helper.MatcherHelper;
import com.onecashye.web.security.telepin.service.TerminalService;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import reactor.netty.http.client.HttpClient;

@Service
@PropertySource({ "classpath:tcs.properties" })
public class TcsApiAccountService {

	Logger logger = LogManager.getLogger(TcsApiAccountService.class);
	
    private final Environment env;	
	private final TcsServiceInitializer tcsServiceInitializer;
	private final TerminalService terminalService;
	private HttpClient httpClient;

	
	@Autowired
	public TcsApiAccountService(TcsServiceInitializer tcsServiceInitializer,
			JwtSecurityService securityService,
			TerminalService terminalService,
			Environment env) {
		super();
		this.tcsServiceInitializer=tcsServiceInitializer;
		this.terminalService = terminalService;
		this.env=env;
		
		httpClient = HttpClient.create()
				  .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, tcsServiceInitializer.getTcsCallTimeout())
				  .responseTimeout(Duration.ofMillis(tcsServiceInitializer.getTcsCallTimeout()))
				  .doOnConnected(conn -> 
				    conn.addHandlerLast(new ReadTimeoutHandler(30))
				      .addHandlerLast(new WriteTimeoutHandler(tcsServiceInitializer.getTcsCallTimeout(), TimeUnit.MILLISECONDS)));
	}
	

	public Map<String,String> tcsCheckAccount(Map<String,Object> jsonRequest,Map<String, String> headers) {
		

		
		jsonRequest.put("username",tcsServiceInitializer.getTcsProxyUser());
		jsonRequest.put("password",tcsServiceInitializer.getTcsProxyPassword());
		
		String xml = MatcherHelper.convert(jsonRequest, tcsServiceInitializer.getTcsFunctionAccountExists());
		
		String uri = String.format("%s", tcsServiceInitializer.getTcsHost());

		try {
			
			logger.info("Starting tcsCheckAccount service ..");
			
			Map<String,String> result = TcsHttpClientService.tcsCall(uri,xml,httpClient);
			
			logger.info(String.format("TCS Result: %s",result));
			
			//Case Wrong PIN
			if(result.get("Result").equalsIgnoreCase("10001")) {
				
				Map<String,String> wrongPIN = new HashMap<>();
				
				wrongPIN.put("Result","-1");
				wrongPIN.put("Message","WRONG_PIN");
				
				return wrongPIN;
				
			}
			
			//Case Account Not Existed
			if(result.get("Result").equalsIgnoreCase("0") && result.get("param1").equalsIgnoreCase("0")) {
				
				Map<String,String> invalidAccount = new HashMap<>();
				
				invalidAccount.put("Result","-2");
				invalidAccount.put("Message","ACCOUNT_NOT_EXISTED");
				
				return invalidAccount;
				
			}
			
			//Case Account Exists
			if(result.get("Result").equalsIgnoreCase("0") && !result.get("param1").equalsIgnoreCase("0")) {
				
				Map<String,String> account = new HashMap<>();
				
				account.put("Result","0");
				account.put("Message","ACCOUNT_EXISTS");
				
				if(result.get("param10").equalsIgnoreCase("0") && result.get("param20").equalsIgnoreCase("0")) {
					
					account.put("Type","UNKNOWN");
					account.put("Registered","NO");
					account.put("Verified","NO");
					
					return account;
				}

				if(result.get("param10").equalsIgnoreCase("7") && result.get("param20").equalsIgnoreCase("7")) {
					
					account.put("Type","AGENT");
					account.put("Registered","YES");
					account.put("Verified","YES");
					
					return account;
				}
				
				if(result.get("param10").equalsIgnoreCase("7") && result.get("param20").equalsIgnoreCase("0")) {
					
					account.put("Type","AGENT");
					account.put("Registered","YES");
					account.put("Verified","NO");
					
					return account;
				}
				
				if(result.get("param10").equalsIgnoreCase("1") && result.get("param20").equalsIgnoreCase("0")) {
					
					account.put("Type","SUBSCRIBER");
					account.put("Registered","YES");
					account.put("Verified","NO");
					account.put("Upgraded","NO");
					
					return account;
				}

				if((result.get("param10").equalsIgnoreCase("1") || result.get("param10").equalsIgnoreCase("2"))  && result.get("param20").equalsIgnoreCase("1")) {
					
					account.put("Type","SUBSCRIBER");
					account.put("Registered","YES");
					account.put("Verified","YES");
					account.put("Upgraded","NO");
					
					return account;
				}
				
				if(result.get("param10").equalsIgnoreCase("2")  && result.get("param20").equalsIgnoreCase("2")) {
					
					account.put("Type","SUBSCRIBER");
					account.put("Registered","YES");
					account.put("Verified","YES");
					account.put("Upgraded","YES");
					
					return account;
				}
				
				//If non of the above is applicable, KYC Error
				
				account.put("ErrorKYC","DETECTED");
				
				if(result.get("param10").equalsIgnoreCase("1")) {
					
					account.put("Type","UNKNOWN");
					account.put("RegisterKYC","LowKYC");
					
				}
				
				if(result.get("param10").equalsIgnoreCase("7")) {
					
					account.put("Type","UNKNOWN");
					account.put("RegisterKYC","AgentKYC");
					
				}
				
				if(result.get("param20").equalsIgnoreCase("1")) {
					
					account.put("Type","UNKNOWN");
					account.put("VerifyKYC","LowKYC");
					
				}
				
				if(result.get("param20").equalsIgnoreCase("7")) {
					
					account.put("Type","UNKNOWN");
					account.put("VerifyKYC","AgentKYC");
					
				}
				
				return account;
			}
			
			//Otherwise return error
			Map<String,String> error = new HashMap<>();
			
			error.put("Result","-100");
			error.put("Message","GENERAL_ERROR");
			
			return error;
			
			
			
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

	public Map<String,String> tcsGetAccountInfoIso(Map<String,Object> jsonRequest,Map<String, String> headers) {
		
		String xml;
		String uri = String.format("%s", tcsServiceInitializer.getTcsHost());
		
		jsonRequest.put("username",tcsServiceInitializer.getTcsProxyUser());
		jsonRequest.put("password",tcsServiceInitializer.getTcsProxyPassword());
		
		if(jsonRequest.get("account").toString().length() <= 6) {
			
			logger.info(String.format("Provided value is alias pattern of 6 digits %s ..", jsonRequest.get("account").toString()));
			
			//jsonRequest.put("alias",jsonRequest.get("account"));
			//Updated 02102022
			//GETACCOUNTINFOISO works on account level alias not user alias, therefore in case this function is used for user alias
			//1- Get MSISDN using TerminalService class/ getUserMsisdnByAlias function
			//2- Replace account param with the result MSISDN
			//3- Use the getTcsFunctionGetAccountInfoIsoByAccount function instead of getTcsFunctionGetAccountInfoIsoByAlias
			
			logger.info(String.format("Trying to find account MSISDN for user alias %s ..", jsonRequest.get("account").toString()));
			String a = terminalService.getUserMsisdnByAlias(jsonRequest.get("account").toString());
			
			logger.info(String.format("Result= %s ..", a));
			
			jsonRequest.put("msisdn",a);
			//xml = MatcherHelper.convert(jsonRequest, tcsServiceInitializer.getTcsFunctionGetAccountInfoIsoByAlias());
			xml = MatcherHelper.convert(jsonRequest, tcsServiceInitializer.getTcsFunctionGetAccountInfoIsoByAccount());
		}	
		else {
			
			jsonRequest.put("msisdn",jsonRequest.get("account"));
			xml = MatcherHelper.convert(jsonRequest, tcsServiceInitializer.getTcsFunctionGetAccountInfoIsoByAccount());
		}
			
		
		try {
			
			
			logger.info("Starting tcsGetAccountInfoIso service ..");
			
			Map<String,String> result = TcsHttpClientService.tcsCall(uri,xml,httpClient);
			
			logger.info(String.format("TCS Result: %s",result));
			
			//Case Account Found
			if(result.get("Result").equalsIgnoreCase("0")) {
				
				return MappingParamsHelper.mapAccountInfo(result);
				
			}
			else {
				
				//Added 02102022
				//try to lookup as the provided number is account alias
				
				jsonRequest.put("alias",jsonRequest.get("account"));
				xml = MatcherHelper.convert(jsonRequest, tcsServiceInitializer.getTcsFunctionGetAccountInfoIsoByAlias());
				
				logger.info("Try to check by account alias ..");
				
				Map<String,String> result_alias = TcsHttpClientService.tcsCall(uri,xml,httpClient);
				
				logger.info(String.format("TCS Result: %s",result_alias));
				
				//Case Account Found
				if(result_alias.get("Result").equalsIgnoreCase("0")) {
					
					return MappingParamsHelper.mapAccountInfo(result_alias);
					
				}
			}
			
			result.put("Message","ACCOUNT_NOT_FOUND");
			
			return result;
			
			
			
		}
		catch(WebClientException e) {
			
			logger.error(String.format("Exception: %s",e.getMessage()));
			
			e.printStackTrace();
			Map<String,String> er = new HashMap<>();
			er.put("Result","-100");
			er.put("Error", e.getMessage());
			return er;
		}
		catch(Exception e) {
			
			logger.error(String.format("Exception: %s",e.getMessage()));
			
			e.printStackTrace();
			Map<String,String> er = new HashMap<>();
			er.put("Result","-100");
			er.put("Error", e.getMessage());
			return er;
		}
		
		
	}

	public Map<String,String> tcsGetAccountType(Map<String,Object> jsonRequest,Map<String, String> headers) {
		
		String xml;
		String uri = String.format("%s", tcsServiceInitializer.getTcsHost());
		String acct = jsonRequest.get("account").toString();
		
		jsonRequest.put("username",tcsServiceInitializer.getTcsProxyUser());
		jsonRequest.put("password",tcsServiceInitializer.getTcsProxyPassword());
		
		if(jsonRequest.get("account").toString().length() <= 6) {
			
			logger.info(String.format("Provided value is alias pattern of 6 digits %s ..", jsonRequest.get("account").toString()));
			
			//jsonRequest.put("alias",jsonRequest.get("account"));
			//Updated 02102022
			//GETACCOUNTINFOISO works on account level alias not user alias, therefore in case this function is used for user alias
			//1- Get MSISDN using TerminalService class/ getUserMsisdnByAlias function
			//2- Replace account param with the result MSISDN
			//3- Use the getTcsFunctionGetAccountInfoIsoByAccount function instead of getTcsFunctionGetAccountInfoIsoByAlias
			
			logger.info(String.format("Trying to find account MSISDN for user alias %s ..", jsonRequest.get("account").toString()));
			String a = terminalService.getUserMsisdnByAlias(jsonRequest.get("account").toString());
			
			logger.info(String.format("Result= %s ..", a));
			
			jsonRequest.put("account","");
			jsonRequest.put("msisdn",a);
			xml = MatcherHelper.convert(jsonRequest, tcsServiceInitializer.getTcsFunctionViewAccountType());
		}	
		else {
			
			//Added on 01082022 because of Telepin error when send MSISDN number without 967
			if(jsonRequest.get("account").toString().length() <=9){
				
				jsonRequest.put("account",String.format("%s%s", "967",jsonRequest.get("account")));
			}
			
			jsonRequest.put("msisdn",jsonRequest.get("account"));
			jsonRequest.put("account","");
			xml = MatcherHelper.convert(jsonRequest, tcsServiceInitializer.getTcsFunctionViewAccountType());
		}
			
		
		try {
			
			logger.info("Starting tcsGetAccountType service ..");
			
			Map<String,String> result = TcsHttpClientService.tcsCall(uri,xml,httpClient);
			
			logger.info(String.format("TCS Result: %s",result));
			
			//Case Account Found
			if(result.get("Result").equalsIgnoreCase("0")) {
				
				return MappingParamsHelper.mapViewAccountType(result);
				
			}
			else {
				
				//Added 02102022
				//try to lookup as the provided number is account alias
				
				jsonRequest.put("account",acct);
				jsonRequest.put("msisdn","");
				
				xml = MatcherHelper.convert(jsonRequest, tcsServiceInitializer.getTcsFunctionViewAccountType());
				
				logger.info("Try to check by account ID ..");
				
				Map<String,String> result_acctID = TcsHttpClientService.tcsCall(uri,xml,httpClient);
				
				logger.info(String.format("TCS Result: %s",result_acctID));
				
				//Case Account Found
				if(result_acctID.get("Result").equalsIgnoreCase("0")) {
					
					return MappingParamsHelper.mapViewAccountType(result_acctID);
					
				}
				else {
					
					//4th scenario if the provided value is account alias
					
					//Added 02102022
					//try to lookup as the provided number is account alias
					
					jsonRequest.put("alias",acct);
					xml = MatcherHelper.convert(jsonRequest, tcsServiceInitializer.getTcsFunctionGetAccountInfoIsoByAlias());
					
					logger.info("Try to check by account alias ..");
					
					Map<String,String> result_alias = TcsHttpClientService.tcsCall(uri,xml,httpClient);
					
					logger.info(String.format("TCS Result: %s",result_alias));
					
					//Case Account Found
					if(result_alias.get("Result").equalsIgnoreCase("0")) {
						
						Map<String,String> alias_result= MappingParamsHelper.mapAccountInfo(result_alias);
						
						String cust_id = alias_result.get(("Customer_ID"));
						
						jsonRequest.put("account",cust_id);
						jsonRequest.put("msisdn","");
						
						xml = MatcherHelper.convert(jsonRequest, tcsServiceInitializer.getTcsFunctionViewAccountType());
						
						logger.info("Try to check by account ID ..");
						
						result_acctID = TcsHttpClientService.tcsCall(uri,xml,httpClient);
						
						logger.info(String.format("TCS Result: %s",result_acctID));
						
						//Case Account Found
						if(result_acctID.get("Result").equalsIgnoreCase("0")) {
							
							return MappingParamsHelper.mapViewAccountType(result_acctID);
							
						}
						
					}
				}
			}
			
			result.put("Message","ACCOUNT_NOT_FOUND");
			
			return result;
			
			
			
		}
		catch(WebClientException e) {
			
			logger.error(String.format("Exception: %s",e.getMessage()));
			
			e.printStackTrace();
			Map<String,String> er = new HashMap<>();
			er.put("Result","-100");
			er.put("Error", e.getMessage());
			return er;
		}
		catch(Exception e) {
			
			logger.error(String.format("Exception: %s",e.getMessage()));
			
			e.printStackTrace();
			Map<String,String> er = new HashMap<>();
			er.put("Result","-100");
			er.put("Error", e.getMessage());
			return er;
		}
		
		
	}

	public Map<String,String> tcsCreateAccountNoTac(Map<String,Object> jsonRequest,Map<String, String> headers) {
		
		/*
		 * 
		 */
			
		
		try {
			
			logger.info("Starting tcsCreateAccountNoTac service ..");
			
			String xml;
			String uri = String.format("%s", tcsServiceInitializer.getTcsHost());
			
			jsonRequest.put("sms",env.getProperty("tcs.subscriber.creation.sms"));
				
			xml = MatcherHelper.convert(jsonRequest, tcsServiceInitializer.getTcsFunctionCreateAccountNoTac());
			
			Map<String,String> result = TcsHttpClientService.tcsCall(uri,xml,httpClient);
			
			logger.info(String.format("TCS Result: %s",result));
			
			//Case Account Created Successfully
			if(result.get("Result").equalsIgnoreCase("0")) {
				
				return MappingParamsHelper.mapCreateAccountNoTacResponse(result);
				
			}
			
			return result;
			
			
			
		}
		catch(WebClientException e) {
			
			logger.error(String.format("Exception: %s",e.getMessage()));
			
			e.printStackTrace();
			Map<String,String> er = new HashMap<>();
			er.put("Result","-100");
			er.put("Error", e.getMessage());
			return er;
		}
		catch(Exception e) {
			
			logger.error(String.format("Exception: %s",e.getMessage()));
			
			e.printStackTrace();
			Map<String,String> er = new HashMap<>();
			er.put("Result","-100");
			er.put("Error", e.getMessage());
			return er;
		}
		
		
	}
	
	public Map<String,String> tcsVerifyAccountLevel(Map<String,Object> jsonRequest,Map<String, String> headers) {
		
		/*
		 * 
		 */
			
		
		try {
			
			logger.info("Starting tcsVerifyAccountLevel service ..");
			
			String xml;
			String uri = String.format("%s", tcsServiceInitializer.getTcsHost());
			
			jsonRequest.put("verification_level","1");
				
			xml = MatcherHelper.convert(jsonRequest, tcsServiceInitializer.getTcsFunctionVerifyAccountLevel());
			
			Map<String,String> result = TcsHttpClientService.tcsCall(uri,xml,httpClient);
			
			logger.info(String.format("TCS Result: %s",result));
			
			//Case Account Created Successfully
			if(result.get("Result").equalsIgnoreCase("0")) {
				
				return MappingParamsHelper.mapSimpleResponse(result);
				
			}
			
			return result;
			
			
			
		}
		catch(WebClientException e) {
			
			logger.error(String.format("Exception: %s",e.getMessage()));
			
			e.printStackTrace();
			Map<String,String> er = new HashMap<>();
			er.put("Result","-100");
			er.put("Error", e.getMessage());
			return er;
		}
		catch(Exception e) {
			
			logger.error(String.format("Exception: %s",e.getMessage()));
			
			e.printStackTrace();
			Map<String,String> er = new HashMap<>();
			er.put("Result","-100");
			er.put("Error", e.getMessage());
			return er;
		}
		
		
	}

	public Map<String,String> tcsRegisterSubscriberLevel1(Map<String,Object> jsonRequest,Map<String, String> headers) {
		
		/*
		 * 01062022
		 */
			
		
		try {
			
			logger.info("Starting tcsRegisterSubscriberLevel1 service ..");
			
			String xml;
			String uri = String.format("%s", tcsServiceInitializer.getTcsHost());
				
			xml = MatcherHelper.convert(jsonRequest, tcsServiceInitializer.getTCSFunctionRegisterSubscriber());
			
			Map<String,String> result = TcsHttpClientService.tcsCall(uri,xml,httpClient);
			
			logger.info(String.format("TCS Result: %s",result));
			
			//Case Account Created Successfully
			if(result.get("Result").equalsIgnoreCase("0")) {
				
				return MappingParamsHelper.mapSimpleResponse(result);
				
			}
			
			return result;
			
			
			
		}
		catch(WebClientException e) {
			
			logger.error(String.format("Exception: %s",e.getMessage()));
			
			e.printStackTrace();
			Map<String,String> er = new HashMap<>();
			er.put("Result","-100");
			er.put("Error", e.getMessage());
			return er;
		}
		catch(Exception e) {
			
			logger.error(String.format("Exception: %s",e.getMessage()));
			
			e.printStackTrace();
			Map<String,String> er = new HashMap<>();
			er.put("Result","-100");
			er.put("Error", e.getMessage());
			return er;
		}
		
		
	}

	public Map<String,String> tcsBalanceMWallet(Map<String,Object> jsonRequest,Map<String, String> headers) {
		
		/*
		 * 
		 */
			
		
		try {
			
			logger.info("Starting tcsBalanceMWallet service ..");
			
			String xml;
			String uri = String.format("%s", tcsServiceInitializer.getTcsHost());
				
			xml = MatcherHelper.convert(jsonRequest, tcsServiceInitializer.getTcsFunctionBalanceMWallet());
			
			Map<String,String> result = TcsHttpClientService.tcsCall(uri,xml,httpClient);
			
			logger.info(String.format("TCS Result: %s",result));
			
			//Case OK
			if(result.get("Result").equalsIgnoreCase("0")) {
				
				return MappingParamsHelper.mapBalanceWalletResponse(result);
				
			}
			
			return result;
			
			
			
		}
		catch(WebClientException e) {
			
			logger.error(String.format("Exception: %s",e.getMessage()));
			
			e.printStackTrace();
			Map<String,String> er = new HashMap<>();
			er.put("Result","-100");
			er.put("Error", e.getMessage());
			return er;
		}
		catch(Exception e) {
			
			logger.error(String.format("Exception: %s",e.getMessage()));
			
			e.printStackTrace();
			Map<String,String> er = new HashMap<>();
			er.put("Result","-100");
			er.put("Error", e.getMessage());
			return er;
		}
		
		
	}
	
	public Map<String,String> tcsBalanceProxyOwner(Map<String,Object> jsonRequest,Map<String, String> headers) {
		
		/*
		 * 
		 */
			
		
		try {
			
			logger.info("Starting tcsBalanceProxyOwner service ..");
			
			String xml;
			String uri = String.format("%s", tcsServiceInitializer.getTcsHost());
				
			xml = MatcherHelper.convert(jsonRequest, tcsServiceInitializer.getTcsFunctionBalanceProxyOwner());
			
			Map<String,String> result = TcsHttpClientService.tcsCall(uri,xml,httpClient);
			
			logger.info(String.format("TCS Result: %s",result));
			
			//Case OK
			if(result.get("Result").equalsIgnoreCase("0")) {
				
				return MappingParamsHelper.mapBalanceProxyOwnerResponse(result);				
			}
			
			return MappingParamsHelper.mapBalanceProxyOwnerResponse(result);
			
			
			
		}
		catch(WebClientException e) {
			
			logger.error(String.format("Exception: %s",e.getMessage()));
			
			e.printStackTrace();
			Map<String,String> er = new HashMap<>();
			er.put("Result","-100");
			er.put("Error", e.getMessage());
			return er;
		}
		catch(Exception e) {
			
			logger.error(String.format("Exception: %s",e.getMessage()));
			
			e.printStackTrace();
			Map<String,String> er = new HashMap<>();
			er.put("Result","-100");
			er.put("Error", e.getMessage());
			return er;
		}
		
		
	}
	
	public Map<String,String> tcsChangeLanguage(Map<String,Object> jsonRequest,Map<String, String> headers) {
		
		/*
		 * 
		 */
			
		
		try {
			
			logger.info("Starting tcsChangeLanguage service ..");
			
			String xml;
			String uri = String.format("%s", tcsServiceInitializer.getTcsHost());
				
			xml = MatcherHelper.convert(jsonRequest, tcsServiceInitializer.getTcsFunctionChangeLanguage());
			
			Map<String,String> result = TcsHttpClientService.tcsCall(uri,xml,httpClient);
			
			logger.info(String.format("TCS Result: %s",result));
			
			//Case OK
			if(result.get("Result").equalsIgnoreCase("0")) {
				
				return MappingParamsHelper.mapSimpleResponse(result);
				
			}
			
			return result;
			
			
			
		}
		catch(WebClientException e) {
			
			logger.error(String.format("Exception: %s",e.getMessage()));
			
			e.printStackTrace();
			Map<String,String> er = new HashMap<>();
			er.put("Result","-100");
			er.put("Error", e.getMessage());
			return er;
		}
		catch(Exception e) {
			
			logger.error(String.format("Exception: %s",e.getMessage()));
			
			e.printStackTrace();
			Map<String,String> er = new HashMap<>();
			er.put("Result","-100");
			er.put("Error", e.getMessage());
			return er;
		}
		
		
	}

	public String getUserNotificationNumber(String username) {
		
		//get the user notification number
		
		Map<String,Object> r = terminalService.getUserNotificationNumber(username);
		
		if(r!=null) {
			
			if(r.get("mobile_number") != null && r.get("mobile_number").toString().isEmpty())
				return r.get("mobile_number").toString();
		}
		
		return "0";
	}

	public Map<String,String> tcsUpdateSubscriberParam39(Map<String,Object> jsonRequest,Map<String, String> headers) {
		
		/*
		 * 02082022 Added to update param39 of subscriber account for docs number upload status
		 */
			
		
		try {
			
			logger.info("Starting tcsUpdateSubscriberParam39 service ..");
			
			String xml;
			String uri = String.format("%s", tcsServiceInitializer.getTcsHost());
				
			xml = MatcherHelper.convert(jsonRequest, tcsServiceInitializer.getTCSFunctionUpdateSubscriberParam39());
			
			Map<String,String> result = TcsHttpClientService.tcsCall(uri,xml,httpClient);
			
			logger.info(String.format("TCS Result: %s",result));
			
			//Case Account Updated Successfully
			if(result.get("Result").equalsIgnoreCase("0")) {
				
				return MappingParamsHelper.mapSimpleResponse(result);
				
			}
			
			return result;
			
			
			
		}
		catch(WebClientException e) {
			
			logger.error(String.format("Exception: %s",e.getMessage()));
			
			e.printStackTrace();
			Map<String,String> er = new HashMap<>();
			er.put("Result","-100");
			er.put("Error", e.getMessage());
			return er;
		}
		catch(Exception e) {
			
			logger.error(String.format("Exception: %s",e.getMessage()));
			
			e.printStackTrace();
			Map<String,String> er = new HashMap<>();
			er.put("Result","-100");
			er.put("Error", e.getMessage());
			return er;
		}
		
		
	}

}
